package com.github.ynverxe.hexserver.extension.internal;

import com.github.ynverxe.hexserver.extension.ExtensionClassLoader;
import com.github.ynverxe.hexserver.extension.ExtensionMeta;
import com.github.ynverxe.hexserver.extension.HexExtension;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Class in charge of collect and discover extensions from a specific source.
 * It's implemented by {@link JarExtensionCollector} that collects JAR files
 * that would be packaged extensions.
 * The second implementation is for testing purposes on the test module.
 *
 * @see ExtensionDiscoverer
 */
@ApiStatus.Internal
@ApiStatus.NonExtendable
public interface ExtensionCollector {

  @NotNull ConcurrentHashMap<String, DiscoveredExtension> collect() throws Throwable;

  /**
   * Represents an extension that can be loaded in the future.
   */
  @ApiStatus.Internal
  interface FutureExtension {
    @NotNull HexExtension loadItself(@NotNull Object context) throws Exception;
  }

  /**
   * Represents a discovered extension from a JAR file.
   *
   * @param manifest The manifest found inside the JAR file
   * @param futureExtension The future extension
   * @param meta The extension meta created from the manifest data
   * @param classLoader The extension class loader
   */
  @ApiStatus.Internal
  record DiscoveredExtension(ExtensionManifest manifest, FutureExtension futureExtension, ExtensionMeta meta, ExtensionClassLoader classLoader) {
  }
}