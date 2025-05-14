package com.github.ynverxe.hexserver.test.extension;

import com.github.ynverxe.hexserver.extension.HexExtension;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record TestExtensionDeclaration(String name, List<String> dependencies, List<String> softDependencies, Factory factory) {

  public TestExtensionDeclaration(String name) {
    this(name, List.of(), List.of(), TestExtension::new);
  }

  public TestExtensionDeclaration(List<String> dependencies, String name) {
    this(name, dependencies, List.of(), TestExtension::new);
  }

  public TestExtensionDeclaration withFactory(Factory factory) {
    return new TestExtensionDeclaration(name, dependencies, softDependencies, factory);
  }

  public interface Factory {
    HexExtension create(@NotNull Object context);
  }
}