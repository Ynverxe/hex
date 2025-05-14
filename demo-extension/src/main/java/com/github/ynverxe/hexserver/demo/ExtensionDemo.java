package com.github.ynverxe.hexserver.demo;

import com.github.ynverxe.hexserver.extension.HexExtension;
import org.jetbrains.annotations.NotNull;

public class ExtensionDemo extends HexExtension {
  public ExtensionDemo(@NotNull Object context) {
    super(context);
  }

  @Override
  public void enable() {
    Class<ExtensionDemoDependency> demoDependencyClass = ExtensionDemoDependency.class;

  }
}