package com.github.ynverxe.hexserver.launcher.extension;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.file.Files.*;

public class ExtensionDownloader {

  private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionDownloader.class);

  private static final Charset CHARSET = StandardCharsets.UTF_8;
  private static final Function<String, URL> SAFE_URL_MAPPER = line -> {
    try {
      return new URI(line).toURL();
    } catch (MalformedURLException | URISyntaxException e) {
      LOGGER.error("Cannot process URL {}", line, e);
      return null;
    }
  };

  private static final String SOURCE_ATTRIBUTE_KEY = "extension-source";
  private static final String MANIFEST_FILENAME = "extensions_manifest.txt";

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

    List<URL> urls = readManifest();
    removePresentExtensions(urls);
    if (urls.isEmpty()) {
      LOGGER.info("No extensions to download");
      return false;
    }

    downloadExtensionFiles(urls);
    return true;
  }

  private void downloadExtensionFiles(@NotNull List<URL> urls) {
    LOGGER.info("Downloading extensions.");

    for (URL url : urls) {
      try {
        downloadExtension(url);
      } catch (Exception e) {
        LOGGER.error("Cannot download file: {}", url, e);
      }
    }
  }

  private void removePresentExtensions(@NotNull List<URL> urls) throws IOException {
    if (!exists(this.extensionDirPath)) {
      return;
    }

    try (Stream<Path> files = list(this.extensionDirPath)) {
      for (Path path : files.toArray(Path[]::new)) {
        var view = getFileAttributeView(path, UserDefinedFileAttributeView.class);

        if (!view.list().contains(SOURCE_ATTRIBUTE_KEY)) {
          continue;
        }

        // read attribute
        int size = view.size(SOURCE_ATTRIBUTE_KEY);

        if (size == 0) continue;

        ByteBuffer byteBuffer = ByteBuffer.allocate(size);
        view.read(SOURCE_ATTRIBUTE_KEY, byteBuffer);
        byteBuffer.flip();

        char[] array = CHARSET.decode(byteBuffer).array();
        String urlString = new String(array);

        urls.removeIf(url -> url.toString().equals(urlString));
      }
    }
  }

  private void downloadExtension(@NotNull URL url) throws IOException {
    long start = System.currentTimeMillis();
    URLConnection connection = url.openConnection();
    connection.setUseCaches(false);
    connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
    connection.connect();

    try (InputStream stream = connection.getInputStream()) {
      String fileName = getFileName(connection);

      if (!fileName.endsWith(".jar")) {
        throw new IllegalStateException("File is not a jar file");
      }

      Path filePath = this.extensionDirPath.resolve(fileName);
      copy(stream, filePath);

      try {
        checkExtensionFileIsPresent(filePath);

        var view = getFileAttributeView(filePath, UserDefinedFileAttributeView.class);
        ByteBuffer buffer = CHARSET.encode(url.toString());
        view.write(SOURCE_ATTRIBUTE_KEY, buffer);
      } catch (Exception e) {
        delete(filePath);
        throw e;
      }

      LOGGER.info("Extension {} downloaded in {}ms", fileName, System.currentTimeMillis() - start);
    }
  }

  private void checkExtensionFileIsPresent(Path path) throws IOException {
    try (JarFile file = new JarFile(path.toFile())) {
      if (file.getEntry("extension.json") == null) {
        throw new IllegalStateException("Jar file is not an extension (missing extension.json)");
      }
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

  private List<URL> readManifest() throws IOException {
    try (Stream<String> lines = lines(this.extensionManifestPath)) {
      return lines.map(SAFE_URL_MAPPER)
          .filter(Objects::nonNull)
          .collect(Collectors.toList());
    }
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