package com.github.ynverxe.hexserver.launcher.util;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

public final class ConnectionMaker {

  private ConnectionMaker() {
  }

  public static URLConnection make(@NotNull URL url) throws IOException {
    URLConnection connection = url.openConnection();
    connection.setUseCaches(false);
    connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
    connection.connect();

    return connection;
  }
}