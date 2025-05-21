package com.github.ynverxe.hexserver.extension.internal;

import com.github.ynverxe.hexserver.HexServer;
import com.github.ynverxe.hexserver.extension.HexExtensionManager;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.file.Files.exists;
import static java.nio.file.Files.list;

/**
 * Collect packaged extensions from a list of {@link Path} conformed by the
 * files inside the {@link HexExtensionManager#extensionsFolder()} and paths
 * specified using the -add-extension argument in {@link HexServer#startArguments()}.
 *
 * @see ExtensionDiscoverer
 */
@ApiStatus.Internal
public class JarExtensionCollector implements ExtensionCollector {

  public static final Pattern EXTENSION_ARGUMENT_PATTERN = Pattern.compile("-add-extension=(?:\"([^\"]+)\"|([^\\\\s]+))");

  private static final Logger LOGGER = LoggerFactory.getLogger(JarExtensionCollector.class);
  private final Path extensionsFolder;

  public JarExtensionCollector(Path extensionsFolder) {
    this.extensionsFolder = extensionsFolder;
  }

  @Override
  public @NotNull ConcurrentHashMap<String, DiscoveredExtension> collect() throws Throwable {
    ConcurrentHashMap<String, DiscoveredExtension> extensions = new ConcurrentHashMap<>();

    for (Path path : getPaths()) {
      try {
        DiscoveredExtension discoveredExtension = ExtensionDiscoverer.discoverExtension(path, this.extensionsFolder::resolve);

        String name = discoveredExtension.meta().name();
        if (extensions.containsKey(name)) {
          throw new IllegalArgumentException("Extension '" + name + "' was already discovered");
        }

        extensions.put(discoveredExtension.meta().name(), discoveredExtension);
      } catch (Throwable error) {
        LOGGER.error("Unexpected error while discovering extension at: {}", path, error);
      }
    }

    return extensions;
  }

  private Set<Path> getPaths() throws Throwable { // use Set to avoid repeated elements
    Stream<Path> argumentExtensions = localizeArgumentExtensions();
    Stream<Path> extensionOnFolder = exists(this.extensionsFolder) ? list(this.extensionsFolder) : Stream.empty();
    return Stream.concat(extensionOnFolder, argumentExtensions)
        .filter(path -> path.toString().endsWith(".jar"))
        .collect(Collectors.toSet());
  }

  private Stream<Path> localizeArgumentExtensions() {
    List<String> arguments = HexServer.instance().startArguments();

    List<Path> stream = arguments.stream()
        .map(EXTENSION_ARGUMENT_PATTERN::matcher)
        .filter(Matcher::find)
        .map(matcher -> matcher.group(1))
        .map(Paths::get)
        .toList();

    return stream.stream();
  }
}