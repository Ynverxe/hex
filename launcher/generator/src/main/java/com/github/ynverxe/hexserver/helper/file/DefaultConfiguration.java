package com.github.ynverxe.hexserver.helper.file;

import com.github.ynverxe.hexserver.helper.configuration.ExtensionsManifest;
import com.github.ynverxe.hexserver.helper.configuration.MinestomSource;
import com.github.ynverxe.hexserver.helper.configuration.ServerConfiguration;

import java.util.function.Supplier;

public final class DefaultConfiguration<T> {

  public static final DefaultConfiguration<MinestomSource> MINESTOM_SOURCE = new DefaultConfiguration<>("minestom-source.conf", MinestomSource::new);
  public static final DefaultConfiguration<ExtensionsManifest> EXTENSIONS_MANIFEST = new DefaultConfiguration<>("extensions_manifest.conf", ExtensionsManifest::new);
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