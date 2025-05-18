package com.github.ynverxe.hexserver.launcher.util;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class ConnectionMaker {

  private static final List<Consumer<URLConnection>> CONFIGURATORS = new ArrayList<>();

  private ConnectionMaker() {
  }

  public static URLConnection make(@NotNull URL url) throws IOException {
    URLConnection connection = url.openConnection();
    connection.setUseCaches(false);
    connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");

    for (Consumer<URLConnection> configurator : CONFIGURATORS) {
      configurator.accept(connection);
    }

    connection.connect();

    return connection;
  }

  /**
   * WARNING: Strictly internal.
   */
  @ApiStatus.Internal
  public static void addConfigurator(@NotNull Consumer<URLConnection> configurator) {
    CONFIGURATORS.add(configurator);
  }
}