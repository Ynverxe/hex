package com.github.ynverxe.hexserver.launcher.test;

import com.github.ynverxe.hexserver.launcher.extension.ExtensionDownloader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static java.nio.file.Files.*;

public class ExtensionDownloaderTest {

  private static final Path TEMP_DIR;

  static {
    try {
      TEMP_DIR = createTempDirectory("temp-server-dir");

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void testExtensionIsPresent() throws IOException {
    ExtensionDownloader downloader = new ExtensionDownloader(TEMP_DIR);
    downloader.start();

    Assertions.assertTrue(exists(
        TEMP_DIR.resolve("extensions/test-extension-all.jar")
    ));
  }

  @Test
  public void testPresentFileSkip() throws IOException {
    ExtensionDownloader downloader = new ExtensionDownloader(TEMP_DIR);
    Assertions.assertFalse(downloader.start()); // all extensions are present, no need of download them again
  }
}