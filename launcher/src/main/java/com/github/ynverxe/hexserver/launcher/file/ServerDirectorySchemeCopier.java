package com.github.ynverxe.hexserver.launcher.file;

import com.github.ynverxe.hexserver.launcher.util.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import com.github.ynverxe.hexserver.launcher.file.RemoteResourceDownloader.*;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

import static java.nio.file.Files.*;

/**
 * To allow creating your own launcher, maybe you would want to
 * have some default files in your server directory, for example, default extension config files.
 * This class looks for a resource named "server-files" in the current ClassLoader and creates
 * a list of all file paths that has to be present in the server directory where the server will run.
 * <p>
 * Alternatively, files with the ".url.conf" extension will be interpreted as their content has to be downloaded and
 * put in the same path (relative to the server directory) removing the ".url" of the final filename. This option
 * can be disabled passing "--IgnoreUrlFiles" as an argument.
 */
public class ServerDirectorySchemeCopier {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServerDirectorySchemeCopier.class);

  public static final String REMOTE_RESOURCE_FILES_EXTENSION = ".url.conf";
  public static final String FALLBACK_FILES_DIR_NAME = "server-files";

  private final @NotNull Path serverDir;
  private final boolean ignoreUrlFiles;

  public ServerDirectorySchemeCopier(@NotNull Path serverDir, boolean ignoreUrlFiles) {
    this.serverDir = Objects.requireNonNull(serverDir);
    this.ignoreUrlFiles = ignoreUrlFiles;
  }

  public boolean start() throws URISyntaxException, IOException {
    if (!canStart()) {
      return false;
    }

    List<Path> success = new ArrayList<>();

    URL fallbackDirURL = ServerDirectorySchemeCopier.class.getClassLoader()
        .getResource(FALLBACK_FILES_DIR_NAME);

    boolean runningInAJar = "jar".equals(fallbackDirURL.getProtocol());
    FileSystem fs = runningInAJar ? FileSystems.newFileSystem(fallbackDirURL.toURI(), Collections.emptyMap()) : FileSystems.getDefault(); // necessary if it's run from a .jar

    Path fallbackDirPath = Paths.get(fallbackDirURL.toURI());

    try (Stream<Path> stream = Files.list(fallbackDirPath)) {
      handlePaths(stream.iterator(), fallbackDirPath, success);
    } finally {
      if (runningInAJar) fs.close();
    }

    return !success.isEmpty();
  }

  private void handlePaths(Iterator<Path> paths, Path fallbackDirPath, List<Path> success) throws IOException {
    while (paths.hasNext()) {
      final Path path = paths.next(); // with "server-dir" as parent
      final Path relativizedPath = fallbackDirPath.relativize(path); // without the "server-dir" as parent

      Path destinationFile = relativeToServerDir(relativizedPath);

      // the full filename relative to the server dir
      String destinationFilePath = destinationFile.toString();

      if (Files.isDirectory(path)) {
        handlePaths(list(path).iterator(), fallbackDirPath, success);
      } else {
        handleFile(relativizedPath, path, destinationFile, destinationFilePath, success);
      }
    }
  }

  private void handleFile(
      Path relativizedPath,
      Path fallbackResourcePath,
      Path destinationFile,
      String destinationFilePath,
      List<Path> success
  ) throws IOException {
    boolean isUrlFile = destinationFilePath.endsWith(REMOTE_RESOURCE_FILES_EXTENSION);
    try {
      if (isUrlFile && !ignoreUrlFiles) {
        destinationFilePath = destinationFilePath.substring(0, destinationFilePath.lastIndexOf(REMOTE_RESOURCE_FILES_EXTENSION));
        destinationFile = Paths.get(destinationFilePath); // without the .url

        // file already exists
        if (exists(destinationFile))
          return;

        // construct definition
        RemoteResourceDefinition definition;
        try (InputStream definitionContent = newInputStream(fallbackResourcePath)) {
          definition = parseUrlFile(definitionContent);
        }

        Listener listener = new ProgressiveFileCopy(destinationFile);
        RemoteResourceDownloader.content(definition, listener);
      } else {
        // file already exists
        if (exists(destinationFile))
          return;

        Listener listener = new ProgressiveFileCopy(destinationFile);
        InputStream content = newInputStream(fallbackResourcePath);
        RemoteResourceDownloader.consumeContent(content, listener);
      }
    } catch (Exception e) {
      for (Path path : success) {
        FileUtil.deleteFile(path);
      }
      throw new RuntimeException("Cannot download file '" + relativizedPath + "'", e);
    }

    success.add(relativizedPath);
    LOGGER.info("File {} created successfully", destinationFile);
  }

  private Path relativeToServerDir(@NotNull Path path) {
    return this.serverDir.resolve(path.toString());
  }

  private boolean canStart() {
    return ServerDirectorySchemeCopier.class.getClassLoader().resources(FALLBACK_FILES_DIR_NAME)
        .findAny().isPresent();
  }

  private RemoteResourceDefinition parseUrlFile(InputStream stream) throws ConfigurateException {
    return HoconConfigurationLoader.builder()
        .source(() -> new BufferedReader(new InputStreamReader(stream)))
        .build()
        .load()
        .get(RemoteResourceDefinition.class);
  }

  static class ProgressiveFileCopy implements Listener {

    private final Path destination;
    private OutputStream stream;

    public ProgressiveFileCopy(Path destination) throws IOException {
      this.destination = destination;
      FileUtil.createParentDirectories(destination);
      this.stream = Files.newOutputStream(destination, StandardOpenOption.CREATE_NEW);
    }

    @Override
    public void onRead(byte read) throws IOException {
      stream.write(read);
    }

    @Override
    public void onInvalidChecksum() throws IOException {
      Files.deleteIfExists(destination);
    }

    @Override
    public void close() throws IOException {
      stream.close();
      stream = null;
    }
  }
}