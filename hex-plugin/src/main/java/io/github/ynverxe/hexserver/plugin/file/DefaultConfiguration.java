package io.github.ynverxe.hexserver.plugin.file;

import io.github.ynverxe.hexserver.plugin.configuration.MinestomSource;
import io.github.ynverxe.hexserver.plugin.configuration.ServerConfiguration;

import java.util.function.Supplier;

public final class DefaultConfiguration<T> {

  public static final DefaultConfiguration<MinestomSource> MINESTOM_SOURCE = new DefaultConfiguration<>("minestom-source.conf", MinestomSource::new);
  public static final DefaultConfiguration<ServerConfiguration> SERVER_CONFIGURATION = new DefaultConfiguration<>("config.conf", ServerConfiguration::new);

  private final String filePath;
  private final Supplier<T> factory;

  private DefaultConfiguration(String filePath, Supplier<T> factory) {
    this.filePath = filePath;
    this.factory = factory;
  }

  public String filePath() {
    return filePath;
  }

  public Supplier<T> factory() {
    return factory;
  }
}