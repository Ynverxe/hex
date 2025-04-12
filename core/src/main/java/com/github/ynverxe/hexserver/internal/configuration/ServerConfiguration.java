package com.github.ynverxe.hexserver.internal.configuration;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public class ServerConfiguration {

  private @MonotonicNonNull String ip;
  private @Nullable String spawnWorld;
  private int port;
  private @MonotonicNonNull String brandName;

  public ServerConfiguration() {
  }

  public @MonotonicNonNull String ip() {
    return ip;
  }

  public int port() {
    return port;
  }

  public @Nullable String spawnWorld() {
    return spawnWorld;
  }

  public @MonotonicNonNull String brandName() {
    return brandName;
  }
}