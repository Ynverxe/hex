package io.github.ynverxe.hexserver.plugin.configuration;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ServerConfiguration extends ConfigurationNodeBuilder {

  public ServerConfiguration() {
    port(25565);
    ip("127.0.0.1");
    brandName("simple-hex-server");
    spawnWorld("world");
  }

  public void port(int port) {
    handledSet("port", port);
  }

  public void ip(@NotNull String ip) {
    handledSet("ip", Objects.requireNonNull(ip));
  }

  public void brandName(@NotNull String brandName) {
    handledSet("brand-name", Objects.requireNonNull(brandName));
  }

  public void spawnWorld(@NotNull String world) {
    handledSet("spawn-world", Objects.requireNonNull(world));
  }
}