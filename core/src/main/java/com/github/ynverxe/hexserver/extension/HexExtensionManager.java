package com.github.ynverxe.hexserver.extension;

import com.github.ynverxe.hexserver.HexServer;
import com.github.ynverxe.hexserver.extension.internal.ExtensionCollector;
import com.github.ynverxe.hexserver.extension.internal.ExtensionCollector.DiscoveredExtension;
import com.github.ynverxe.hexserver.util.GraphSort;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * This class is responsible for collect, load, register, enable, disable and reloading
 * {@link HexExtension} instances.
 * <p>
 * <h2>Collection and evaluation phase:</h2>
 * When {@link ExtensionCollector#collect()} is called, every collected extension
 * is evaluated in the following way:
 * <ul>
 *   <li>Check if the extension name matches the {@link #EXTENSION_NAME_PATTERN}, if not, the extension is discarded.</li>
 *   <li>The dependencies of the extension are registered as graph edges.</li>
 *   <li>Check if there's a missing hard dependency, and if there's one, the extension is discarded.</li>
 * </ul>
 *
 * After that, the load order is calculated using topologically ordering. Extensions that are involved in a
 * dependency cycle are discarded.
 *
 * <h2>Load and enable phase:</h2>
 * According to the load order, {@link HexExtension} instances are created and enabled. If an extension
 * produces an error during this phase, the extension is discarded.
 *
 * <h2>Extension discarding</h2>
 * As mentioned, during the initial phases until the enable phase, some extensions may be discarded
 * during the process. When an extension is discarded all the extensions who strictly depended on
 * that extension (hard dependencies) will be discarded and the same process apply for those extensions.
 * This is recursive operation.
 * <p>
 * <h3>Mutability isn't accepted yet.</h3>
 * <p>
 * To get an instance you can use {@link HexServer#extensions()}.
 */
public final class HexExtensionManager {

  private static final Pattern EXTENSION_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9]+$");
  static final Logger LOGGER = LoggerFactory.getLogger(HexExtensionManager.class);

  private final Path extensionsFolder;

  private final @NotNull List<String> loadOrder = new ArrayList<>();
  private final @NotNull List<ExtensionDependency> graphEdges = new ArrayList<>();
  private final @NotNull Map<String, HexExtension> graphNodes = new ConcurrentHashMap<>();

  private final ExtensionCollector collector;

  @ApiStatus.Internal
  private HexExtensionManager(@NotNull Path extensionsFolder, @NotNull ExtensionCollector collector) {
    this.extensionsFolder = extensionsFolder;
    this.collector = collector;
  }
  
  @ApiStatus.Internal
  public static @NotNull HexExtensionManager.Holder create(@NotNull Path extensionsFolder, @NotNull ExtensionCollector collector) {
    HexExtensionManager manager = new HexExtensionManager(extensionsFolder, collector);
    return new Holder(manager, manager::start, manager::shutdown);
  }

  public @NotNull Path extensionsFolder() {
    return extensionsFolder;
  }

  public @NotNull Map<String, HexExtension> extensionMap() {
    return graphNodes;
  }

  public @NotNull List<HexExtension> extensions() {
    List<HexExtension> extensions = new ArrayList<>();
    for (String extensionName : loadOrder) {
      HexExtension extension = this.graphNodes.get(extensionName);
      if (extension == null) continue;
      extensions.add(extension);
    }

    return extensions;
  }

  public boolean hasExtension(@NotNull String extensionName) {
    return this.graphNodes.containsKey(extensionName);
  }

  public boolean isExtensionEnabled(@NotNull String extensionName) {
    return extension(extensionName).map(HexExtension::enabled).orElse(false);
  }

  public @Nullable HexExtension findExtension(@NotNull String extensionName) {
    return this.graphNodes.get(extensionName);
  }

  public @NotNull Optional<HexExtension> extension(@NotNull String extensionName) {
    return Optional.ofNullable(this.graphNodes.get(extensionName));
  }

  public <T extends HexExtension> @NotNull Optional<T> extension(@NotNull String extensionName, @NotNull Class<T> expectedType) {
    return extension(extensionName)
        .map(extension -> expectedType.isInstance(extension) ? expectedType.cast(extension) : null);
  }

  private void shutdown() {
    for (HexExtension extension : this.extensions()) {
      if (extension == null || !extension.enabled()) continue;

      terminateExtension(extension);
    }
  }

  public void reload() {
    for (HexExtension value : this.extensions()) {
      if (!value.supportsReload()) continue;

      LOGGER.info("Reloading extension '{}'", value.name());
      try {
        value.reload();
      } catch (Throwable e) {
        LOGGER.error("Unexpected error when reloading extension {}", value.name(), e);
      }
    }
  }

  private void start() {
    this.loadExtensions();

    for (HexExtension extension : this.extensions()) {
      if (extension.enabled() || extension.removed) continue;

      LOGGER.info("Enabling extension '{}'", extension.name());

      try {
        extension.internalEnable();
        extension.enabled = true;
      } catch (Throwable e) {
        LOGGER.error("Unexpected error while enabling extension {}", extension.name(), e);
        discardExtension(extension.name(), this.graphNodes::remove);
      }
    }
  }

  private void terminateExtension(@NotNull HexExtension extension) {
    LOGGER.info("Terminating extension '{}'", extension.name());

    try {
      extension.terminate();
    } catch (Exception e) {
      LOGGER.error("Unexpected exception while disabling extension '{}'", extension.name(), e);
    }
  }

  // ------- load logic -------

  private void analyzeDiscoveredExtensions(@NotNull Map<String, DiscoveredExtension> discoveredExtensions) {
    // remove extensions with invalid names
    for (Map.Entry<String, DiscoveredExtension> entry : discoveredExtensions.entrySet()) {
      ExtensionMeta meta = entry.getValue().meta();

      if (!EXTENSION_NAME_PATTERN.matcher(meta.name()).matches()) {
        LOGGER.error("Extension name '{}' cannot have special characters or be empty.", meta.name());

        discoveredExtensions.remove(entry.getKey());
      }
    }

    computeGraphEdges(discoveredExtensions);
    detectMissingDependencies(discoveredExtensions);

    // compute execution order
    GraphSort<String> sort = GraphSort.sort(discoveredExtensions.keySet(), key -> {
      ExtensionMeta meta = discoveredExtensions.get(key).meta();
      return meta.fullDependencies();
    });

    for (String cycle : sort.cycles()) {
      LOGGER.warn("Detected extension dependency cycle '{}'. Extensions will be ignored.", cycle);
    }

    this.loadOrder.addAll(sort.values());
  }

  @ApiStatus.Internal
  private void loadExtensions() {
    Map<String, DiscoveredExtension> discoveredExtensions;
    try {
      discoveredExtensions = this.collector.collect();
    } catch (Throwable e) {
      throw new RuntimeException("Cannot discover extensions", e);
    }

    analyzeDiscoveredExtensions(discoveredExtensions);

    for (String extensionName : this.loadOrder) {
      DiscoveredExtension discoveredExtension = discoveredExtensions.get(extensionName);

      LOGGER.info("Loading extension '{}'", extensionName);
      try {
        loadExtension(discoveredExtension);
      } catch (Throwable e) {
        if (e instanceof ClassNotFoundException) {
          LOGGER.error("Cannot found entrypoint for extension '{}'", discoveredExtension.meta().name(), e);
        } else {
          LOGGER.error("Cannot load extension '{}'", discoveredExtension.meta().name(), e);
        }

        discardExtension(extensionName, name -> {
          DiscoveredExtension extension = discoveredExtensions.remove(name);
          return extension != null ? extension.meta() : null;
        });
      }
    }
  }

  private void loadExtension(DiscoveredExtension discoveredExtension) throws Throwable {
    Path extensionDataDir = discoveredExtension.meta().directory();

    if (!Files.exists(extensionDataDir)) {
      Files.createDirectories(extensionDataDir);
    }

    discoveredExtension.classLoader().init(this, discoveredExtension.meta());

    HexExtension.InstantiationContext values = new HexExtension.InstantiationContext(this, discoveredExtension.meta());
    HexExtension extension = discoveredExtension.futureExtension().loadItself(values);

    this.graphNodes.put(extension.name(), extension);
  }

  private void computeGraphEdges(@NotNull Map<String, DiscoveredExtension> discoveredExtensions) {
    for (DiscoveredExtension entry : discoveredExtensions.values()) {
      ExtensionMeta meta = entry.meta();

      for (String dependency : meta.dependencies()) {
        this.graphEdges.add(new ExtensionDependency(meta.name(), dependency, false));
      }

      for (String softDependency : meta.softDependencies()) {
        this.graphEdges.add(new ExtensionDependency(meta.name(), softDependency, true));
      }
    }
  }

  private void detectMissingDependencies(Map<String, DiscoveredExtension> discoveredExtensions) {
    for (ExtensionDependency entry : this.graphEdges) {
      if (entry.soft) continue;

      String dependency = entry.dependencyName;

      if (discoveredExtensions.containsKey(dependency))
        continue;

      discardExtension(entry.dependentName, name -> {
        DiscoveredExtension extension = discoveredExtensions.remove(name);
        return extension != null ? extension.meta() : null;
      });
    }
  }

  private void discardExtension(String key, Function<String, ExtensionMeta> remover) {
    ExtensionMeta removed = remover.apply(key);

    if (removed != null) {
      if (removed instanceof HexExtension extension) {
        extension.removed = true;
        terminateExtension(extension);
      }

      Stream<String> dependents = filterEdges(edge -> edge.dependencyName.equals(key) && !edge.soft)
          .map(ExtensionDependency::dependentName);

      dependents.forEach(dependent -> this.discardExtension(dependent, remover));
    }
  }

  private Stream<ExtensionDependency> filterEdges(Predicate<ExtensionDependency> filter) {
    return this.graphEdges
        .stream()
        .filter(filter);
  }

  private record ExtensionDependency(String dependentName, String dependencyName, boolean soft) {
  }
  
  public record Holder(HexExtensionManager extensionManager, Runnable startCaller, Runnable shutdownCaller) {
  }
}