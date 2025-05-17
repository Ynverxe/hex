package com.github.ynverxe.hexserver.launcher.test;

import com.github.ynverxe.hexserver.launcher.file.ServerDirectorySchemeCopier;
import com.github.ynverxe.hexserver.launcher.util.FileDeleter;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.Files.createTempDirectory;
import static java.nio.file.Files.exists;

@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)
public class ServerDirectorySchemeCopierTest {

  private static final Path TEMP_DIR;
  private static final String TEST_FILENAME = "awesome-text.txt";

  static {
    try {
      TEMP_DIR = createTempDirectory("temp-server-dir");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @AfterEach
  public void clearTempDir() throws IOException {
    FileDeleter.clearDirectory(TEMP_DIR);
  }

  @Test
  @Order(1)
  public void testServerConfPresence() throws IOException, URISyntaxException {
    ServerDirectorySchemeCopier copier = new ServerDirectorySchemeCopier(TEMP_DIR, false);
    copier.start();

    Path targetFile = TEMP_DIR.resolve(TEST_FILENAME);
    Assertions.assertTrue(
        Files.lines(targetFile)
            .anyMatch("HELLYEAH!"::equals)
    );
  }

  @Test
  @Order(2)
  public void testFileSkipDuePresence() throws IOException, URISyntaxException {
    ServerDirectorySchemeCopier copier = new ServerDirectorySchemeCopier(TEMP_DIR, false);
    copier.start();
    Assertions.assertFalse(copier.start());
  }

  @Test
  @Order(3)
  public void testUrlFileIgnore() throws IOException, URISyntaxException {
    ServerDirectorySchemeCopier copier = new ServerDirectorySchemeCopier(TEMP_DIR, true);
    copier.start();

    Path targetFile = TEMP_DIR.resolve(TEST_FILENAME);
    Assertions.assertFalse(exists(targetFile));
  }
}