package com.github.ynverxe.hexserver.test;

import io.github.ynverxe.configuratehelper.handler.source.URLConfigurationFactory;
import com.github.ynverxe.hexserver.HexServer;
import com.github.ynverxe.hexserver.HexServerInitializer;
import com.github.ynverxe.hexserver.extension.HexExtensionManager;
import com.github.ynverxe.hexserver.extension.internal.JarExtensionCollector;
import com.github.ynverxe.hexserver.internal.configuration.ServerConfiguration;
import net.minestom.server.MinecraftServer;
import net.minestom.server.ServerProcess;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TestServerInitializer {

  public static final Path SERVER_DIR;
  public static final Path EXTENSIONS_DIR;

  static {
    try {
      SERVER_DIR = Files.createTempDirectory("test-server");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    SERVER_DIR.toFile().deleteOnExit();
    EXTENSIONS_DIR = SERVER_DIR.resolve("extensions");
  }

  public static void startServer(@NotNull HexExtensionManager.Holder extensionManagerHolder) throws Throwable {
    System.setProperty("hexserver.testing", "true");

    HexServer.optionalInstance()
        .ifPresent(HexServer::shutdown);

    MinecraftServer server = MinecraftServer.init();
    ServerProcess process = MinecraftServer.process();

    List<String> arguments = tokenizeString(System.getProperty("server-arguments", ""));

    HexServerInitializer initializer = new HexServerInitializer(
        SERVER_DIR,
        URLConfigurationFactory.newBuilder()
            .destContentRoot(SERVER_DIR)
            .configurationLoaderFactory(() -> {
              throw new UnsupportedOperationException();
            })
            .build(),
        null,
        new ServerConfiguration(InetAddress.getLocalHost().getHostAddress(), "", 25565 ,"test-server"),
        server,
        process,
        false,
        extensionManagerHolder
    ) {};

    initializer.startArguments(arguments);

    initializer.start();
  }

  public static void startServer() throws Throwable {
    Path tempExtensionsDir = Files.createTempDirectory("extensions");

    HexExtensionManager.Holder holder = HexExtensionManager.create(tempExtensionsDir, new JarExtensionCollector(tempExtensionsDir));
    startServer(holder);
  }

  public static List<String> tokenizeString(String input) {
    String regex = "([^\"]\\S*|\".+?\")\\s*";
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(input);

    List<String> tokens = new ArrayList<>();

    while (matcher.find()) {
      String token = matcher.group();
      if (token.startsWith("\"") && token.endsWith("\"")) {
        token = token.substring(1, token.length() - 1);
      }
      tokens.add(token);
    }

    return tokens;
  }
}