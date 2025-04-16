package com.github.ynverxe.hexserver.launcher.extension;

import com.github.ynverxe.hexserver.launcher.util.ConnectionMaker;
import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.gson.GsonConfigurationLoader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;
import java.util.jar.JarFile;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;

import static java.nio.file.Files.*;

public class ExtensionDownloader {

  private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionDownloader.class);

  private static final String MANIFEST_FILENAME = "extensions_manifest.json";

  private final @NotNull Path serverRootDir;
  private final @NotNull Path extensionManifestPath;
  private final @NotNull Path extensionDirPath;

  public ExtensionDownloader(@NotNull Path serverRootDir) throws IOException  {
    this.serverRootDir = Objects.requireNonNull(serverRootDir);
    this.extensionDirPath = serverRootDir.resolve("extensions");
    this.extensionManifestPath = serverRootDir.resolve(MANIFEST_FILENAME);
  }

  public boolean start() throws IOException {
    if (!exists(this.extensionDirPath)) {
      createDirectories(this.extensionDirPath);
    }

    if (!canStart()) {
      LOGGER.info("No extensions manifest found. ExtensionDownloader will not start.");
      return false;
    }

    Map<String, URL> extensions = readManifest();
    removePresentExtensions(extensions);
    if (extensions.isEmpty()) {
      LOGGER.info("No extensions to download");
      return false;
    }

    downloadExtensionFiles(extensions);
    return true;
  }

  private void downloadExtensionFiles(@NotNull Map<String, URL> urls) {
    LOGGER.info("Downloading extensions.");

    urls.forEach((extension, url) -> {
      try {
        downloadExtension(extension, url);
      } catch (Exception e) {
        LOGGER.error("Cannot download file: {}", url, e);
      }
    });
  }

  private void removePresentExtensions(@NotNull Map<String, URL> urls) throws IOException {
    if (!exists(this.extensionDirPath)) {
      return;
    }

    try (Stream<Path> files = list(this.extensionDirPath)) {
      for (Path path : files.toArray(Path[]::new)) {
        if (!path.toString().endsWith(".jar")) continue;

        try (JarFile jarFile = new JarFile(path.toFile())) {
          String name = readExtensionManifest(jarFile)
              .node("name").getString();

          if (name == null) { // Invalid extension
            continue;
          }

          urls.remove(name);
        }
      }
    }
  }

  private ConfigurationNode readExtensionManifest(JarFile jarFile) throws IOException {
    ZipEntry entry = jarFile.getEntry("extension.json");

    if (entry == null) return null;

    InputStream stream = jarFile.getInputStream(entry);

    return GsonConfigurationLoader.builder()
        .source(() -> new BufferedReader(new InputStreamReader(stream)))
        .build()
        .load();
  }

  private void downloadExtension(@NotNull String extension, @NotNull URL url) throws IOException {
    long start = System.currentTimeMillis();
    URLConnection connection = ConnectionMaker.make(url);

    try (InputStream stream = connection.getInputStream()) {
      String fileName = getFileName(connection);

      if (!fileName.endsWith(".jar")) {
        throw new IllegalStateException("File is not a jar file");
      }

      Path filePath = this.extensionDirPath.resolve(fileName);
      if (exists(filePath)) {
        delete(filePath);
      }

      copy(stream, filePath);

      JarFile file = new JarFile(filePath.toFile());
      ZipEntry entry = file.getEntry("extension.json");

      try {
        if (entry == null) {
          throw new IllegalStateException("Jar file is not an extension (missing extension.json)");
        }

        ConfigurationNode extensionManifest = readExtensionManifest(file);
        if (!extension.equals(extensionManifest.node("name").getString())) {
          throw new IllegalStateException("Declared extension '" + extension + "' in extensions_manifest.json doesn't match with the downloaded extension manifest name");
        }
      } catch (RuntimeException e) {
        file.close();
        delete(filePath);
        throw e;
      }

      LOGGER.info("Extension {} downloaded in {}ms", fileName, System.currentTimeMillis() - start);
    }
  }

  private @NotNull String getFileName(@NotNull URLConnection connection) {
    String contentDisposition = connection.getHeaderField("Content-Disposition");

    String fileName = null;
    if (contentDisposition != null && contentDisposition.contains("filename=")) {
      fileName = contentDisposition.split("filename=")[1].replace("\"", "").trim();
    }

    if (fileName == null) {
      Path path = Paths.get(connection.getURL().getPath());

      return path.getFileName().toString();
    }

    return fileName;
  }

  private Map<String, URL> readManifest() throws IOException {
    ConfigurationNode configurationNode = GsonConfigurationLoader.builder()
        .file(this.extensionManifestPath.toFile())
        .build().load();

    return configurationNode.get(new TypeToken<Map<String, URL>>() {});
  }

  private boolean canStart() throws IOException {
    if (!exists(this.extensionManifestPath)) {
      initPackagedManifest();
    }

    return exists(this.extensionManifestPath);
  }

  private void initPackagedManifest() throws IOException {
    InputStream stream = ExtensionDownloader.class
        .getClassLoader()
        .getResourceAsStream(MANIFEST_FILENAME);

    if (stream == null) {
      return;
    }

    copy(stream, this.extensionManifestPath);
  }
}