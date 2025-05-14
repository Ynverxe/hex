package com.github.ynverxe.hexserver.extension.internal;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.List;

@ConfigSerializable
@ApiStatus.Internal
public class ExtensionManifest {

  public @MonotonicNonNull String name;
  public @Nullable List<String> dependencies;
  public @MonotonicNonNull String entryPoint;
  public @MonotonicNonNull String version;
  public @Nullable List<String> authors;
  public @Nullable List<String> softDependencies;

  public ExtensionManifest() {
  }

  public ExtensionManifest(@MonotonicNonNull String name, @Nullable List<String> dependencies, @MonotonicNonNull String entryPoint, @MonotonicNonNull String version, @Nullable List<String> authors, @Nullable List<String> softDependencies) {
    this.name = name;
    this.dependencies = dependencies;
    this.entryPoint = entryPoint;
    this.version = version;
    this.authors = authors;
    this.softDependencies = softDependencies;
  }
}