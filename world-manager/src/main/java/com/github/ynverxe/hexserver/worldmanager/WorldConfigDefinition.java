package com.github.ynverxe.hexserver.worldmanager;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.nio.file.Path;
import java.util.Objects;

@ConfigSerializable
public class WorldConfigDefinition implements Cloneable {

  public @MonotonicNonNull String name;
  public @MonotonicNonNull GeneratorOptions generator;
  public @MonotonicNonNull Path path;
  public @MonotonicNonNull String dimension;
  public @MonotonicNonNull String loader;
  public @Nullable ConfigurationNode properties;

  public WorldConfigDefinition(@MonotonicNonNull String name, @MonotonicNonNull GeneratorOptions generator, @MonotonicNonNull Path path, @MonotonicNonNull String dimension, @MonotonicNonNull String loader, @Nullable ConfigurationNode properties) {
    this.name = Objects.requireNonNull(name);
    this.generator = Objects.requireNonNull(generator);
    this.path = Objects.requireNonNull(path);
    this.dimension = Objects.requireNonNull(dimension);
    this.loader = Objects.requireNonNull(loader);
    this.properties = properties;
  }

  public WorldConfigDefinition() {
  }

  @Override
  public WorldConfigDefinition clone() {
    try {
      return (WorldConfigDefinition) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new AssertionError();
    }
  }

  @Override
  public String toString() {
    return "{" +
        "name='" + name + '\'' +
        ", generator=" + generator +
        ", path=" + path +
        ", dimensionType='" + dimension + '\'' +
        ", loader='" + loader + '\'' +
        ", properties=" + properties +
        '}';
  }

  @ConfigSerializable
  public static class GeneratorOptions {
    public @MonotonicNonNull String type;
    public @MonotonicNonNull ConfigurationNode data;

    public GeneratorOptions(@MonotonicNonNull String type, @MonotonicNonNull ConfigurationNode data) {
      this.type = Objects.requireNonNull(type);
      this.data = Objects.requireNonNull(data);
    }

    public GeneratorOptions() {
    }

    @Override
    public String toString() {
      return "{" +
          "type='" + type + '\'' +
          ", data='" + data + '\'' +
          '}';
    }
  }
}