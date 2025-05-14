package com.github.ynverxe.hexserver.extension;

import com.github.ynverxe.hexserver.HexServer;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Optional;

/**
 * The class loader in charge of loading an extension's classes.
 * This class loader also allows an extension to access classes of its dependencies.
 */
public final class ExtensionClassLoader extends URLClassLoader {

  private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionClassLoader.class);

  static {
    ClassLoader.registerAsParallelCapable();
  }

  private HexExtensionManager extensionManager;
  private ExtensionMeta extensionMeta;

  public ExtensionClassLoader(@NotNull ExtensionMeta meta) throws MalformedURLException {
    super(meta.name() + ":class_loader", new URL[]{meta.sourceJar().toUri().toURL()}, HexServer.class.getClassLoader());
  }

  /**
   * Reserved for testing purposes.
   *
   * @param extensionName The extension name
   */
  @ApiStatus.Internal
  public ExtensionClassLoader(String extensionName) {
    super(extensionName + ":class_loader", new URL[] {}, HexServer.class.getClassLoader());
  }

  void init(HexExtensionManager extensionManager, ExtensionMeta extensionMeta) {
    this.extensionManager = extensionManager;
    this.extensionMeta = extensionMeta;
  }

  @Override
  protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
    if (extensionManager == null) {
      LOGGER.warn("findClass was invoked but extensionManager wasn't initialized");
    }

    try {
      return super.loadClass(name, resolve);
    } catch (ClassNotFoundException error) {
      if (extensionManager != null) {
        for (String dependency : extensionMeta.fullDependencies()) {
          Optional<ExtensionClassLoader> optionalClassLoader = extensionManager.extension(dependency)
              .map(HexExtension::classLoader);

          if (optionalClassLoader.isEmpty()) continue;

          ExtensionClassLoader classLoader = optionalClassLoader.get();

          try {
            return classLoader.loadClass(name);
          } catch (ClassNotFoundException ignored) {}
        }
      }

      throw error;
    }
  }
}