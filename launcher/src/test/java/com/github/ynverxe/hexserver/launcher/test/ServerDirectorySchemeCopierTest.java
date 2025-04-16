package com.github.ynverxe.hexserver.launcher.test;

import com.github.ynverxe.hexserver.launcher.extension.ServerDirectorySchemeCopier;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.Files.createTempDirectory;

@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)
public class ServerDirectorySchemeCopierTest {

  private static final Path TEMP_DIR;

  static {
    try {
      TEMP_DIR = createTempDirectory("temp-server-dir");

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @Order(1)
  public void testServerConfPresence() throws IOException, URISyntaxException {
    ServerDirectorySchemeCopier copier = new ServerDirectorySchemeCopier(TEMP_DIR, false);
    copier.start();

    Path targetFile = TEMP_DIR.resolve("awesome-text.txt");
    Assertions.assertTrue(
        Files.lines(targetFile)
            .anyMatch("HELLYEAH!"::equals)
    );
  }

  @Test
  @Order(2)
  public void testFileSkipDuePresence() throws IOException, URISyntaxException {
    ServerDirectorySchemeCopier copier = new ServerDirectorySchemeCopier(TEMP_DIR, false);
    Assertions.assertFalse(copier.start());

    Path targetFile = TEMP_DIR.resolve("awesome-text.txt");
    Files.delete(targetFile);
  }

  @Test
  @Order(3)
  public void testUrlFileIgnore() throws IOException, URISyntaxException {
    ServerDirectorySchemeCopier copier = new ServerDirectorySchemeCopier(TEMP_DIR, true);
    copier.start();

    Path targetFile = TEMP_DIR.resolve("awesome-text.txt.url");
    Assertions.assertTrue(
        Files.lines(targetFile)
            .anyMatch("https://gist.githubusercontent.com/Ynverxe/9758b2801828881e1642c7d802ea26da/raw/7241626ebffb0c53219d6e6e3c1ade4fd1164f79/awesome-text.txt"::equals)
    );
  }
}