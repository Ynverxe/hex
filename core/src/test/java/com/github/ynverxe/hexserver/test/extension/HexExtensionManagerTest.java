package com.github.ynverxe.hexserver.test.extension;

import com.github.ynverxe.hexserver.extension.HexExtension;
import com.github.ynverxe.hexserver.extension.HexExtensionManager;
import com.github.ynverxe.hexserver.test.TestServerInitializer;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HexExtensionManagerTest {

  @Test
  public void testLoadOrder() throws Throwable {
    HexExtensionManager manager = setup(
        new TestExtensionDeclaration(List.of("ExtensionB"), "ExtensionA"),
        new TestExtensionDeclaration("ExtensionB")
    );

    List<HexExtension> extensions = new ArrayList<>(manager.extensions());
    assertEquals("ExtensionB", extensions.get(0).name());
    assertEquals("ExtensionA", extensions.get(1).name());
  }

  @Test
  public void testDependencyCycleExclusion() throws Throwable {
    HexExtensionManager manager = setup(
        new TestExtensionDeclaration(List.of("ExtensionB"), "ExtensionA"),
        new TestExtensionDeclaration(List.of("ExtensionA"), "ExtensionB"),
        new TestExtensionDeclaration("ExtensionC")
    );

    assertFalse(manager.hasExtension("ExtensionA") &&
        manager.hasExtension("ExtensionB"), "Dependency cycle is present");

    assertTrue(manager.hasExtension("ExtensionC"), "ExtensionC was removed with no reason");
  }

  @Test
  public void testFailedDependencyHandling() throws Throwable {
    // Main and Dependency will be removed from the manager
    TestExtensionDeclaration dependency = new TestExtensionDeclaration("Dependency")
        .withFactory(context -> new TestExtension(context) {
          @Override
          protected void enable() {
            throw new RuntimeException();
          }
        });

    TestExtensionDeclaration main = new TestExtensionDeclaration(List.of("Dependency"), "Main");

    HexExtensionManager manager = setup(dependency, main);

    assertTrue(!manager.hasExtension("Main") && !manager.hasExtension("Dependency"), "Extensions were not removed");
  }

  private HexExtensionManager setup(TestExtensionDeclaration... declarations) throws Throwable {
    HexExtensionManager.Holder manager = HexExtensionManager.create(TestServerInitializer.EXTENSIONS_DIR, new TestExtensionCollector(declarations));
    TestServerInitializer.startServer(manager);
    return manager.extensionManager();
  }
}