package com.github.ynverxe.hexserver.test.extension;

import com.github.ynverxe.hexserver.extension.internal.ExtensionCollector;
import com.github.ynverxe.hexserver.extension.internal.ExtensionManifest;
import com.github.ynverxe.hexserver.extension.internal.SimpleExtensionMeta;
import com.github.ynverxe.hexserver.extension.ExtensionClassLoader;
import com.github.ynverxe.hexserver.test.TestServerInitializer;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class TestExtensionCollector implements ExtensionCollector {

  private final TestExtensionDeclaration[] declarations;

  public TestExtensionCollector(TestExtensionDeclaration... declarations) {
    this.declarations = declarations;
  }

  @Override
  public @NotNull ConcurrentHashMap<String, DiscoveredExtension> collect() {
    ConcurrentHashMap<String, DiscoveredExtension> extensions = new ConcurrentHashMap<>();
    for (TestExtensionDeclaration declaration : declarations) {
      ExtensionManifest manifest = new ExtensionManifest(
          declaration.name(),
          declaration.dependencies(),
          "com.github.ynverxe.hexserver.test.extension." + declaration.name(),
          "0.1.0",
          List.of("Ynverxe"),
          declaration.softDependencies()
      );

      SimpleExtensionMeta meta = SimpleExtensionMeta.create(manifest, Paths.get(""),
          TestServerInitializer.EXTENSIONS_DIR.resolve(declaration.name()));

      ExtensionClassLoader classLoader = new ExtensionClassLoader(meta.name());
      extensions.put(meta.name(), new DiscoveredExtension(manifest,
          declaration.factory()::create, meta, classLoader));
    }
    return extensions;
  }
}