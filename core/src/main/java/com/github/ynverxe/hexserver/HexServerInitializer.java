package com.github.ynverxe.hexserver;

import com.github.ynverxe.configuratehelper.handler.FastConfiguration;
import com.github.ynverxe.configuratehelper.handler.source.URLConfigurationFactory;
import com.github.ynverxe.hexserver.internal.configuration.ServerConfiguration;
import com.github.ynverxe.hexserver.internal.listener.DefaultListenersRegister;
import com.github.ynverxe.hexserver.terminal.ServerTerminal;
import net.minestom.server.MinecraftServer;
import net.minestom.server.ServerProcess;
import net.minestom.server.extensions.ExtensionManager;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.util.NamingSchemes;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;

public final class HexServerInitializer {

  private static final Logger LOGGER = LoggerFactory.getLogger(HexServerInitializer.class);

  private final Path serverDir;
  private final URLConfigurationFactory configurationFactory;
  private final FastConfiguration serverConfiguration;
  private final ServerConfiguration serverConfigurationValues;

  private final MinecraftServer server;
  private final ServerProcess process;

  private boolean registerDefaultListeners;

  // I make my own ExtensionManager, because I don't like that ExtensionBootstrap starts the extensions on #init
  // That causes Extensions being loaded and expecting that HexServer#INSTANCE was initialized
  private final ExtensionManager extensionManager;

  public HexServerInitializer(@NotNull Path serverDir) throws IOException {
    this.serverDir = serverDir;

    Path extensionsDir = this.serverDir.resolve("extensions");
    System.setProperty("minestom.extension.folder", extensionsDir.toString());

    this.server = MinecraftServer.init();
    this.process = MinecraftServer.process();
    this.extensionManager = new ExtensionManager(this.process);

    if (!Files.exists(serverDir)) {
      try {
        Files.createDirectories(serverDir);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    this.configurationFactory = URLConfigurationFactory.newBuilder()
        .classLoader(HexServer.class.getClassLoader())
        .fallbackContentRoot("")
        .destContentRoot(this.serverDir)
        .configurationLoaderFactory(
            () -> YamlConfigurationLoader.builder()
                .nodeStyle(NodeStyle.BLOCK)
                .indent(2)
                .defaultOptions(options -> options.serializers(builder -> builder.registerAnnotatedObjects(
                    ObjectMapper.factoryBuilder().defaultNamingScheme(NamingSchemes.LOWER_CASE_DASHED).build()
                )))
        )
        .build();

    this.serverConfiguration = this.configurationFactory.create("config.yml", "config.yml");
    this.serverConfigurationValues = this.serverConfiguration.node().get(ServerConfiguration.class);

    this.process.scheduler().buildShutdownTask(this.extensionManager::shutdown);
  }

  @Contract("-> this")
  public HexServerInitializer registerDefaultListeners() {
    this.registerDefaultListeners = true;
    return this;
  }

  public @NotNull HexServer start() throws IOException, ClassNotFoundException {
    return start(this.serverConfigurationValues.ip(), this.serverConfigurationValues.port());
  }

  public @NotNull HexServer start(@NotNull String ip, int port) throws IOException, ClassNotFoundException {
    return this.start(new InetSocketAddress(ip, port));
  }

  public @NotNull HexServer start(@NotNull SocketAddress socketAddress) throws IOException, ClassNotFoundException {
    synchronized (HexServerInitializer.class) {
      if (HexServer.INSTANCE != null) {
        throw new IllegalStateException("There's already a server running on this process. Stop that server to start a new one.");
      }

      HexServer server = new HexServer(this.serverDir, this.configurationFactory, this.serverConfiguration, this.serverConfigurationValues, this.extensionManager, this.process);
      MinecraftServer.setBrandName(this.serverConfigurationValues.brandName());
      HexServer.INSTANCE = server;

      this.server.start(socketAddress);

      this.extensionManager.start();

      this.extensionManager.gotoPreInit();
      this.extensionManager.gotoInit();
      this.extensionManager.gotoPostInit();

      if (registerDefaultListeners) {
        DefaultListenersRegister.register(this.process.eventHandler(), server);
      }

      ServerTerminal.INSTANCE.start();

      return server;
    }
  }

  public ServerProcess process() {
    return process;
  }
}