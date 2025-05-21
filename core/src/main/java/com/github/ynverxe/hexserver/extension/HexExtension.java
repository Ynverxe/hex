package com.github.ynverxe.hexserver.extension;

import com.github.ynverxe.configuratehelper.handler.factory.ConfigurationLoaderFactory;
import com.github.ynverxe.configuratehelper.handler.source.URLConfigurationFactory;
import com.github.ynverxe.hexserver.HexServer;
import com.github.ynverxe.hexserver.world.HexWorld;
import com.github.ynverxe.hexserver.world.HexWorldManager;
import net.kyori.adventure.key.Namespaced;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.minestom.server.MinecraftServer;
import net.minestom.server.ServerProcess;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventHandler;
import net.minestom.server.event.EventNode;
import net.minestom.server.timer.Schedulable;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import static java.nio.file.Files.copy;
import static java.nio.file.Files.exists;

/**
 * Represents an extension in the HexServer environment. An extension is an independent module that can have its own working directory,
 * configuration files, class loader, and world manager. Extensions are typically packaged as JAR files and are managed by the {@link HexExtensionManager},
 * which is responsible for loading, enabling, disabling, and reloading them.
 *
 * <h2>Extension Management</h2>
 * Extensions are discovered and loaded through the {@link com.github.ynverxe.hexserver.extension.internal.JarExtensionCollector}
 * and {@link com.github.ynverxe.hexserver.extension.internal.ExtensionDiscoverer}. These classes ensure that extensions are correctly
 * identified and integrated into the server environment.
 *
 * <h2>Capabilities</h2>
 * An extension has access to both Hex and Minestom classes, allowing it to:
 * <ul>
 *     <li>Register event listeners.</li>
 *     <li>Schedule and manage tasks.</li>
 *     <li>Register and associate {@link com.github.ynverxe.hexserver.world.HexWorld} instances to the extension's {@link HexWorldManager}.</li>
 *     <li>Extend and modify server functionality.</li>
 * </ul>
 *
 * <h2>Dependency Management</h2>
 * Extensions can declare dependencies, which are categorized as:
 * <ul>
 *     <li><b>Soft Dependencies:</b> Optional extensions that enhance functionality but are not critical. The extension can function
 *     even if these dependencies are missing or fail to load.</li>
 *     <li><b>Hard Dependencies:</b> Required extensions without which the extension cannot operate. If a hard dependency fails to load,
 *     the dependent extension will not be enabled or retained by the extension manager.</li>
 * </ul>
 * The {@link HexExtensionManager} automatically calculates the appropriate load order to ensure that all dependencies
 * of an extension are resolved before it is loaded. This reduces the need for manual dependency management.
 *
 * <h2>Extension Configuration</h2>
 * Each extension must include an <code>extension.conf</code> file, which defines essential metadata such as:
 * <ul>
 *     <li><b>version:</b> The version of the extension.</li>
 *     <li><b>name:</b> The name of the extension.</li>
 *     <li><b>entry-point:</b> The main class of the extension.</li>
 *     <li><b>dependencies:</b> A list of required or optional dependencies.</li>
 *     <li><b>authors:</b> The creators of the extension.</li>
 * </ul>
 *
 * The configuration file must adhere to the format defined by {@link com.github.ynverxe.hexserver.extension.internal.ExtensionManifest}.
 *
 * <h3>Example configuration:</h3>
 * <pre>
 * version = "0.1.0"
 * name = "ExtensionDemo"
 * entry-point = "com.github.ynverxe.hexserver.demo.ExtensionDemo"
 * dependencies = ["ExtensionDemoDependency"]
 * authors = ["Ynverxe"]
 * </pre>
 *
 * <h2>Recommended Practices</h2>
 * When registering an event listener, scheduling a task, or registering a {@link com.github.ynverxe.hexserver.world.HexWorld} instance,
 * always use the corresponding extension's associated objects.
 * This ensures that the extension's activity is properly tracked, allowing for complete cleanup of its components when it is disabled.
 */
public class HexExtension implements ExtensionMeta, EventHandler<Event>, Schedulable, Namespaced {

  protected final @NotNull HexExtensionManager extensionManager;
  protected final @NotNull URLConfigurationFactory configurationFactory;
  private final @NotNull ComponentLogger logger;
  private final @NotNull ExtensionMeta extensionMeta;
  private final @NotNull ExtensionClassLoader classLoader;
  private final @NotNull EventNode<Event> eventNode;
  private final @NotNull Scheduler scheduler;
  private final @NotNull HexWorldManager worldManager;
  private final @NotNull String namespace;

  private boolean enabled;
  boolean removed;

  private @Nullable Task schedulerTicker;

  public HexExtension(@NotNull Object context) {
    InstantiationContext values = (InstantiationContext) context;
    this.extensionManager = Objects.requireNonNull(values.extensionManager);
    this.extensionMeta = Objects.requireNonNull(values.meta);
    this.namespace = name().toLowerCase();

    ClassLoader classLoader = getClass().getClassLoader();
    boolean testing = Boolean.parseBoolean(System.getProperty("hexserver.testing", "false"));

    if (!(classLoader instanceof ExtensionClassLoader)) {
      if (!testing) {
        throw new IllegalStateException("This class wasn't loaded by an ExtensionClassLoader");
      }

      this.classLoader = new ExtensionClassLoader(name());
    } else {
      this.classLoader = (ExtensionClassLoader) classLoader;
    }

    this.logger = ComponentLogger.logger(getClass());
    this.configurationFactory = URLConfigurationFactory.newBuilder()
        .destContentRoot(this.directory())
        .fallbackContentRoot("")
        .classLoader(this.classLoader)
        .configurationLoaderFactory(this.createConfigurationFactory())
        .build();

    this.worldManager = new ExtensionWorldManager(this);
    this.eventNode = EventNode.all(this.name() + "_EventNode");
    this.scheduler = Scheduler.newScheduler();
  }

  /**
   * Enables internally this extension. Then calls {@link #enable()}.
   *
   * @throws Exception If the {@link #enable()} method throws an error.
   */
  final void internalEnable() throws Exception {
    ServerProcess process = HexServer.instance().process();

    process.eventHandler().addChild(this.eventNode);

    this.schedulerTicker = process.scheduler().buildTask(this.scheduler::process)
        .repeat(TaskSchedule.immediate()).schedule();

    this.enabled = true;

    enable();
  }

  /**
   * Finalizes this extension internally and its components. Then calls {@link #disable()}.
   *
   * @throws Exception If the {@link #disable()} method throws an Exception.
   */
  final void terminate() throws Exception {
    if (this.schedulerTicker != null) {
      this.schedulerTicker.cancel();
    }

    try {
      disable();
    } finally {
      try {
        ServerProcess process = HexServer.instance().process();
        process.eventHandler().removeChild(this.eventNode);
        this.worldManager.internalView().forEach(hexWorld -> {
          hexWorld.clearPlayers();
          this.worldManager.unregister(hexWorld);
        });
      } finally {
        this.enabled = false;
      }
    }
  }


  /**
   * Abstract logic to enable this extension.
   *
   * @throws Exception If any exception is thrown.
   */
  protected void enable() throws Exception {}

  /**
   * Abstract logic to disable this extension.
   *
   * @throws Exception If any exception is thrown.
   */
  protected void disable() throws Exception {}

  /**
   * Abstract logic to reload this extension. This method is supposed
   * to be called internally by the extension or by the manager. But the manager
   * will never call it if {@link #supportsReload()} returns false.
   *
   * @throws Exception If any exception is thrown.
   */
  protected void reload() throws Exception {
    if (!supportsReload()) {
      throw new IllegalArgumentException("This extension cannot be reloaded");
    }
  }

  /**
   * @return the {@link HexExtensionManager} that holds this extension.
   */
  public final @NotNull HexExtensionManager extensionManager() {
    return extensionManager;
  }

  /**
   * @return true if this extension is enabled, false otherwise.
   */
  public final boolean enabled() {
    return enabled;
  }

  /**
   * @return true if this extension can be reloaded, false otherwise.
   */
  public boolean supportsReload() {
    return false;
  }

  /**
   * The {@link Scheduler} associated with this extension. The scheduler
   * will process tasks as long as this extension is enabled.
   * <p>
   * It's updated by a task registered on {@link MinecraftServer#getSchedulerManager()}
   * at enabling time.
   *
   * @return this extension's {@link Scheduler}.
   */
  @Override
  public final @NotNull Scheduler scheduler() {
    return scheduler;
  }

  /**
   * The {@link EventNode} associated with this extension. The event node
   * will listen to events processed by {@link MinecraftServer#getGlobalEventHandler()}
   * as long as this extension is enabled.
   *
   * @return this extension's {@link EventNode}.
   */
  @Override
  public final @NotNull EventNode<Event> eventNode() {
    return eventNode;
  }

  /**
   * The {@link HexWorldManager} associated with this extension. It
   * will hold all the {@link com.github.ynverxe.hexserver.world.HexWorld} instances
   * registered as long as this extension is enabled.
   * <p>
   * {@link com.github.ynverxe.hexserver.world.HexWorld} instances held by this manager
   * will be unregistered from its {@link net.minestom.server.instance.InstanceManager}
   * and unloaded.
   *
   * @return this extension's world manager.
   */
  public final @NotNull HexWorldManager worldManager() {
    return worldManager;
  }

  /**
   * @return this extension's name, defined by its manifest.
   */
  @Override
  public final @NotNull String name() {
    return extensionMeta.name();
  }

  @Override
  @SuppressWarnings("all")
  public final @NotNull String namespace() {
    return namespace;
  }

  /**
   * @return this extension's version, defined by its manifest.
   */
  @Override
  public final @NotNull String version() {
    return extensionMeta.version();
  }

  /**
   * @return this extension's authors, defined by its manifest.
   */
  @Override
  public final @NotNull List<String> authors() {
    return extensionMeta.authors();
  }

  /**
   * @return this extension's working directory, it's the result
   * of <code>{@link HexExtensionManager#extensionsFolder()} + {@link ExtensionMeta#name()}</code>
   */
  @Override
  public final @NotNull Path directory() {
    return extensionMeta.directory();
  }

  /**
   * @return this extension's source JAR path.
   */
  @Override
  public final @NotNull Path sourceJar() {
    return extensionMeta.sourceJar();
  }

  /**
   * @return this extension's dependencies, defined by its manifest.
   */
  @Override
  public final @NotNull List<String> dependencies() {
    return extensionMeta.dependencies();
  }

  /**
   * @return this extension's soft dependencies, defined by its manifest.
   */
  @Override
  public final @NotNull List<String> softDependencies() {
    return extensionMeta.softDependencies();
  }

  /**
   * @return the combination of this extension's hard and soft dependencies defined by its manifest.
   */
  @Override
  public @NotNull List<String> fullDependencies() {
    return extensionMeta.fullDependencies();
  }

  /**
   * @return this extension's classloader.
   */
  public final @NotNull ExtensionClassLoader classLoader() {
    return classLoader;
  }

  /**
   * The {@link URLConfigurationFactory} associated with this extension.
   * <p>
   * It's a utility class that is preconfigured to create {@link com.github.ynverxe.configuratehelper.handler.FastConfiguration}
   * inside from the {@link #directory()} directory and look for fallback configuration sources inside this extension's JAR file.
   *
   * @return this extension's configuration factory.
   */
  public @NotNull URLConfigurationFactory configurationFactory() {
    return configurationFactory;
  }

  /**
   * @return the {@link ComponentLogger} associated with this extension.
   */
  public @NotNull ComponentLogger logger() {
    return logger;
  }

  /**
   * Creates a new {@link ConfigurationLoaderFactory} used to configure {@link #configurationFactory}.
   * <p>
   * <b>WARNING</b>: Configurate library isn't shaded, be sure of using the correct version defined in <b>libs.versions.toml</b>.
   * @return a new instance of {@link ConfigurationLoaderFactory}.
   */
  protected @NotNull ConfigurationLoaderFactory createConfigurationFactory() {
    return HoconConfigurationLoader::builder;
  }

  /**
   * Copies a resource inside this extension's jar into the data directory if it doesn't exist.
   *
   * @param fallbackPath The path to the fallback content.
   * @param destPath The path where the file will be copied.
   *
   * @throws IOException If there's an error thrown by {@link java.nio.file.Files#copy}.
   * @throws IllegalArgumentException If the fallback resource at the specified path doesn't exist.
   */
  protected final void computeResource(@NotNull String fallbackPath, @NotNull String destPath) throws IllegalArgumentException, IOException {
    if (exists(this.directory().resolve(destPath))) {
      return;
    }

    InputStream fallback = getClass().getClassLoader().getResourceAsStream(fallbackPath);

    if (fallback == null) {
      throw new IllegalArgumentException("Cannot found fallback content for '" + fallbackPath + "'");
    }

    copy(fallback, this.directory().resolve(destPath));
  }

  record InstantiationContext(HexExtensionManager extensionManager, ExtensionMeta meta) {
  }

  static final class ExtensionWorldManager extends HexWorldManager {
    private final WeakReference<HexExtension> extensionWeakReference;

    public ExtensionWorldManager(HexExtension extension) {
      super(extension.namespace());
      this.extensionWeakReference = new WeakReference<>(extension);
    }

    @Override
    public void register(@NotNull HexWorld world) throws IllegalArgumentException {
      HexExtension extension = this.extensionWeakReference.get();

      if (extension == null || !extension.enabled()) {
        throw new IllegalArgumentException("Owner isn't enabled");
      }

      super.register(world);
    }
  }
}