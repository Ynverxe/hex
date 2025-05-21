package com.github.ynverxe.hexserver;

import com.github.ynverxe.configuratehelper.handler.FastConfiguration;
import com.github.ynverxe.configuratehelper.handler.source.URLConfigurationFactory;
import com.github.ynverxe.hexserver.extension.HexExtensionManager;
import com.github.ynverxe.hexserver.extension.internal.JarExtensionCollector;
import com.github.ynverxe.hexserver.internal.configuration.ServerConfiguration;
import com.github.ynverxe.hexserver.terminal.ServerTerminal;
import net.minestom.server.MinecraftServer;
import net.minestom.server.ServerProcess;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.util.NamingSchemes;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class HexServerInitializer {

  private static final Logger LOGGER = LoggerFactory.getLogger(HexServerInitializer.class);

  private final Path serverDir;
  private final URLConfigurationFactory configurationFactory;
  private final FastConfiguration serverConfiguration;
  private final ServerConfiguration serverConfigurationValues;

  private final MinecraftServer server;
  private final ServerProcess process;

  private final HexExtensionManager.Holder extensionManagerHolder;

  private @NotNull List<String> startArguments = Collections.emptyList();

  /**
   * Constructor reserved for testing purposes.
   *
   * @param serverDir The directory where the server will run
   * @param configurationFactory The configuration factory used to load server files
   * @param serverConfiguration The server configuration
   * @param serverConfigurationValues The server configuration values
   * @param server The server
   * @param process The server process
   * @param registerDefaultListeners If true, default listeners will be registered
   * @param extensionManagerHolder The extension manager
   */
  @ApiStatus.Internal
  protected HexServerInitializer(Path serverDir, URLConfigurationFactory configurationFactory, FastConfiguration serverConfiguration, ServerConfiguration serverConfigurationValues, MinecraftServer server, ServerProcess process, boolean registerDefaultListeners, HexExtensionManager.Holder extensionManagerHolder) {
    this.serverDir = serverDir;
    this.configurationFactory = configurationFactory;
    this.serverConfiguration = serverConfiguration;
    this.serverConfigurationValues = serverConfigurationValues;
    this.server = server;
    this.process = process;
    this.extensionManagerHolder = extensionManagerHolder;
  }

  public HexServerInitializer(@NotNull Path serverDir) throws IOException {
    this.serverDir = serverDir;

    this.server = MinecraftServer.init();
    this.process = MinecraftServer.process();

    Path extensionsFolder = serverDir.resolve("extensions");
    this.extensionManagerHolder = HexExtensionManager.create(extensionsFolder, new JarExtensionCollector(extensionsFolder));

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
            () -> HoconConfigurationLoader.builder()
                .emitComments(true)
                .defaultOptions(options -> options.serializers(builder -> builder.registerAnnotatedObjects(
                    ObjectMapper.factoryBuilder().defaultNamingScheme(NamingSchemes.LOWER_CASE_DASHED).build()
                )))
        )
        .build();

    this.serverConfiguration = this.configurationFactory.create("config.conf", "config.conf");
    this.serverConfigurationValues = this.serverConfiguration.node().get(ServerConfiguration.class);
  }

  public void startArguments(@NotNull List<String> startArguments) {
    this.startArguments = Objects.requireNonNull(startArguments);
  }

  public @NotNull HexServer start() throws Throwable {
    return start(this.serverConfigurationValues.ip(), this.serverConfigurationValues.port());
  }

  public @NotNull HexServer start(@NotNull String ip, int port) throws Throwable {
    return this.start(new InetSocketAddress(ip, port));
  }

  public @NotNull HexServer start(@NotNull SocketAddress socketAddress) throws Throwable {
    synchronized (HexServerInitializer.class) {
      if (HexServer.INSTANCE != null) {
        throw new IllegalStateException("There's already a server running on this process. Stop that server to start a new one.");
      }

      HexServer server = new HexServer(
          this.serverDir,
          this.configurationFactory,
          this.serverConfiguration,
          this.serverConfigurationValues,
          this.extensionManagerHolder,
          this.process,
          Collections.unmodifiableList(this.startArguments)
      );

      MinecraftServer.setBrandName(this.serverConfigurationValues.brandName());
      HexServer.INSTANCE = server;

      this.server.start(socketAddress);

      this.extensionManagerHolder.startCaller().run();

      ServerTerminal.init();

      return server;
    }
  }

  public ServerProcess process() {
    return process;
  }
}