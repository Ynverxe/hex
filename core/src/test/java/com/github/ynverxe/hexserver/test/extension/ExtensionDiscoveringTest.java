package com.github.ynverxe.hexserver.test.extension;

import com.github.ynverxe.hexserver.HexServer;
import com.github.ynverxe.hexserver.extension.HexExtension;
import com.github.ynverxe.hexserver.test.TestServerInitializer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ExtensionDiscoveringTest {

  @Test
  public void testExtensionJarLoading() throws Throwable {
    TestServerInitializer.startServer();

    HexExtension extension = HexServer.instance().extensions()
        .findExtension("ExtensionDemo");

    assertNotNull(extension, "ExtensionDemo wasn't loaded");

    assertEquals(List.of("ExtensionDemoDependency"), extension.dependencies());
    assertEquals(List.of("Ynverxe"), extension.authors());
    assertEquals("0.1.0", extension.version());
  }
}