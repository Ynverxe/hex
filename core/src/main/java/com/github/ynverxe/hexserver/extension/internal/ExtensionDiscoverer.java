package com.github.ynverxe.hexserver.extension.internal;

import com.github.ynverxe.hexserver.extension.ExtensionMeta;
import com.github.ynverxe.hexserver.extension.HexExtension;
import com.github.ynverxe.hexserver.extension.ExtensionClassLoader;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.function.Function;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * Utility class in charge of found and parse the
 * extension manifest inside a JAR file and create
 * the corresponding {@link ExtensionMeta} and {@link com.github.ynverxe.hexserver.extension.internal.ExtensionCollector.FutureExtension}.
 *
 * @see JarExtensionCollector
 */
@ApiStatus.Internal
public final class ExtensionDiscoverer {

  private ExtensionDiscoverer() {
  }

  public static @NotNull ExtensionCollector.DiscoveredExtension discoverExtension(
      @NotNull Path extensionPath,
      @NotNull Function<String, Path> extensionDirProvider) throws Exception {
    try (JarFile file = new JarFile(extensionPath.toFile())) {
      ZipEntry entry = file.getEntry("extension.conf");
      if (entry == null) {
        throw new IllegalArgumentException("extension.conf doesn't exists");
      }

      InputStream stream = file.getInputStream(entry);
      ExtensionManifest manifest = loadManifest(extensionPath, stream);;

      ExtensionMeta meta = SimpleExtensionMeta.create(manifest, extensionPath, extensionDirProvider.apply(manifest.name));

      JarFutureExtension extension = new JarFutureExtension(manifest.entryPoint, meta);
      return new ExtensionCollector.DiscoveredExtension(manifest, extension, meta, extension.classLoader);
    }
  }

  private static ExtensionManifest loadManifest(@NotNull Path extensionPath, @NotNull InputStream inputStream) {
    try {
      return HoconConfigurationLoader.builder()
          .source(() -> new BufferedReader(new InputStreamReader(inputStream)))
          .build()
          .load()
          .get(ExtensionManifest.class);
    } catch (ConfigurateException e) {
      throw new IllegalArgumentException("Invalid extension.conf at: " + extensionPath, e);
    }
  }

  @ApiStatus.Internal
  public static class JarFutureExtension implements ExtensionCollector.FutureExtension {

    final ExtensionClassLoader classLoader;

    private final String entryPoint;

    public JarFutureExtension(String entryPoint, ExtensionMeta meta) throws MalformedURLException {
      this.entryPoint = entryPoint;
      this.classLoader = new ExtensionClassLoader(meta);
    }

    public String entryPoint() {
      return entryPoint;
    }

    @Override
    public @NotNull HexExtension loadItself(@NotNull Object context) throws Exception {
      Class<?> entryPointClass = Class.forName(entryPoint, true, classLoader);
      return (HexExtension) entryPointClass.getDeclaredConstructor(Object.class)
          .newInstance(context);
    }
  }
}