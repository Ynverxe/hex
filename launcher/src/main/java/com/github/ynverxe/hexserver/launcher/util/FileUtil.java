package com.github.ynverxe.hexserver.launcher.util;

import java.io.IOException;
import java.nio.file.Path;

import static java.nio.file.Files.*;
import static java.nio.file.Files.delete;

public final class FileUtil {
  private FileUtil() {
  }

  public static void deleteFile(Path path) throws IOException {
    if (!exists(path)) return;
    if (isDirectory(path)) {
      clearDirectory(path);
    }
    delete(path);
  }

  public static void clearDirectory(Path path) throws IOException {
    if (!exists(path)) return;

    list(path).forEach(someFile -> {
      try {
        deleteFile(someFile);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });
  }
}