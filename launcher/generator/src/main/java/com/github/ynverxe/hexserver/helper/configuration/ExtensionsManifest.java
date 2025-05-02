package com.github.ynverxe.hexserver.helper.configuration;

import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.Objects;

public class ExtensionsManifest extends ConfigurationNodeBuilder {
  public void extension(@NotNull String name, @NotNull URI uri) {
    handledSet(name, Objects.requireNonNull(uri));
  }
}