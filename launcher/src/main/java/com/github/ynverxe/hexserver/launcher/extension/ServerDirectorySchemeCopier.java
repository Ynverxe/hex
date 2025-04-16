package com.github.ynverxe.hexserver.launcher.extension;

import com.github.ynverxe.hexserver.launcher.util.ConnectionMaker;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

import static java.nio.file.Files.*;
import static java.nio.file.Files.delete;

/**
 * To allow creating your own launcher, maybe you would want to
 * have some default files in your server directory, for example, default extension config files.
 * This class looks for a resource named "server-files" in the current ClassLoader and creates
 * a list of all file paths that has to be present in the server directory where the server will run.
 * <p>
 * Alternatively, files with the ".url" extension will be interpreted as their content has to be downloaded and
 * put in the same path (relative to the server directory) removing the ".url" of the final filename. This option
 * can be disabled passing "--IgnoreUrlFiles" as an argument.
 */
public class ServerDirectorySchemeCopier {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServerDirectorySchemeCopier.class);

  private static final String FALLBACK_FILES_DIR_NAME = "server-files";

  private final @NotNull Path serverDir;
  private final boolean ignoreUrlFiles;

  public ServerDirectorySchemeCopier(@NotNull Path serverDir, boolean ignoreUrlFiles) {
    this.serverDir = Objects.requireNonNull(serverDir);
    this.ignoreUrlFiles = ignoreUrlFiles;
  }

  public boolean start() throws IOException, URISyntaxException {
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
      Iterator<Path> paths = stream.map(fallbackDirPath::relativize)
          .iterator();

      while (paths.hasNext()) {
        Path relativizedPath = paths.next(); // without the "server-dir" as parent
        Path fallbackResourcePath = fallbackDirPath.resolve(relativizedPath); // with "server-dir" as parent

        Path relativeToServerDir = relativeToServerDir(relativizedPath);

        // the full filename relative to the server dir
        String finalFileName = relativeToServerDir.toString();

        boolean isUrlFile = finalFileName.endsWith(".url");

        InputStream inputStream = null;
        try {
          if (isUrlFile && !ignoreUrlFiles) {
            finalFileName = finalFileName.substring(0, finalFileName.lastIndexOf("."));
            relativeToServerDir = Paths.get(finalFileName); // without the .url

            // get fallback file content
            Optional<String> line = lines(fallbackResourcePath)
                .findFirst();

            if (line.isEmpty()) {
              LOGGER.info("{} doesn't have a valid URL", relativizedPath);
              continue;
            }

            URL url = URI.create(line.get()).toURL();

            URLConnection connection = ConnectionMaker.make(url);
            inputStream = connection.getInputStream();
          } else {
            inputStream = newInputStream(fallbackResourcePath);
          }

          if (!exists(relativeToServerDir)) { // copy the resource
            copy(inputStream, relativeToServerDir);
            success.add(relativizedPath);
            LOGGER.info("File {} created successfully", relativeToServerDir);
          }
        } catch (Exception e) {
          for (Path path : success) {
            this.deleteFile(path);
          }
          throw e;
        } finally {
          if (inputStream != null) {
            inputStream.close();
          }
        }
      }
    } finally {
      if (runningInAJar) fs.close();
    }

    return !success.isEmpty();
  }

  private void deleteFile(Path path) throws IOException {
    if (isDirectory(path)) {
      list(path).forEach(someFile -> {
        try {
          if (isDirectory(someFile)) {
            deleteFile(someFile);
          }

          delete(someFile);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      });
    }

    delete(path);
  }

  private Path relativeToServerDir(@NotNull Path path) {
    return this.serverDir.resolve(path);
  }

  private boolean canStart() {
    return ServerDirectorySchemeCopier.class.getClassLoader().resources(FALLBACK_FILES_DIR_NAME)
        .findAny().isPresent();
  }
}