package com.github.ynverxe.hexserver.helper.configuration;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.Collection;

public class MinestomSource extends ConfigurationNodeBuilder {

  public MinestomSource(ConfigurationNode node) {
    super(node);
  }

  public MinestomSource() {
  }

  public void coordinates(@NotNull String coordinates) {
    handledSet("coordinates", coordinates);
  }

  public void repositories(@NotNull Collection<String> repositories) {
    appendToList("repositories", String.class, repositories);
  }

  public void repository(@NotNull String url) {
    appendToList("repositories", String.class, url);
  }

  public void mavenCentral() {
    repository("https://repo.maven.apache.org/maven2/");
  }

  public void jitpack() {
    repository("https://jitpack.io");
  }
}