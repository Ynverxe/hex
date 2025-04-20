package com.github.ynverxe.hexserver.launcher.library;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.List;

@ConfigSerializable
public class MinestomSource {

  private @MonotonicNonNull String coordinates;
  private @MonotonicNonNull List<String> repositories;

  public MinestomSource() {
  }

  public MinestomSource(String coordinates, List<String> repositories) {
    this.coordinates = coordinates;
    this.repositories = repositories;
  }

  public String coordinates() {
    return coordinates;
  }

  public List<String> repositories() {
    return repositories;
  }
}