package com.github.ynverxe.hexserver;

import io.github.ynverxe.configuratehelper.handler.FastConfiguration;
import io.github.ynverxe.configuratehelper.handler.source.URLConfigurationFactory;
import com.github.ynverxe.hexserver.extension.HexExtensionManager;
import com.github.ynverxe.hexserver.internal.configuration.ServerConfiguration;
import com.github.ynverxe.hexserver.world.ExtensionWorldLookup;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.minestom.server.ServerProcess;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;

public final class HexServer {

  private static final ComponentLogger LOGGER = ComponentLogger.logger();

  static volatile HexServer INSTANCE;

  private final Path serverDir;
  private final URLConfigurationFactory configurationFactory;
  private final FastConfiguration serverConfiguration;
  private final ServerConfiguration serverConfigurationValues;

  private final ServerProcess process;

  private final ExtensionWorldLookup extensionWorldLookup;

  private final HexExtensionManager.Holder extensionManagerHolder;
  private final List<Consumer<HexServer>> shutdownListeners = new ArrayList<>();

  private final List<String> startArguments;

  private final @NotNull InstanceContainer fallbackWorld = new InstanceContainer(UUID.randomUUID(), DimensionType.OVERWORLD);

  HexServer(Path serverDir, URLConfigurationFactory configurationFactory, FastConfiguration serverConfiguration, ServerConfiguration serverConfigurationValues, HexExtensionManager.Holder extensionManagerHolder, ServerProcess process, List<String> startArguments) throws IOException {
    this.serverDir = serverDir;
    this.configurationFactory = configurationFactory;
    this.serverConfiguration = serverConfiguration;
    this.serverConfigurationValues = serverConfigurationValues;
    this.process = process;
    this.extensionManagerHolder = extensionManagerHolder;
    this.startArguments = startArguments;
    this.extensionWorldLookup = new ExtensionWorldLookup(this);

    this.process.scheduler().buildShutdownTask(this::handleShutdown);
    this.addShutdownListener(hexServer -> this.extensionManagerHolder.shutdownCaller().run());
  }

  public List<String> startArguments() {
    return startArguments;
  }

  public ServerProcess process() {
    return process;
  }

  public ExtensionWorldLookup extensionWorldLookup() {
    return extensionWorldLookup;
  }

  public Path serverDir() {
    return serverDir;
  }

  public URLConfigurationFactory configurationFactory() {
    return configurationFactory;
  }

  public FastConfiguration serverConfiguration() {
    return serverConfiguration;
  }

  public ServerConfiguration serverConfigurationValues() {
    return serverConfigurationValues;
  }

  public void shutdown() {
    this.process.stop();
  }

  public HexExtensionManager extensions() {
    return extensionManagerHolder.extensionManager();
  }

  public void addShutdownListener(@NotNull Consumer<HexServer> listener) {
    this.shutdownListeners.add(Objects.requireNonNull(listener));
  }

  private void handleShutdown() {
    synchronized (HexServer.class) {
      for (Consumer<HexServer> shutdownListener : this.shutdownListeners) {
        try {
          shutdownListener.accept(this);
        } catch (Exception error) {
          LOGGER.error("Shutdown listener generated an unexpected error", error);
        }
      }

      HexServer.INSTANCE = null;
    }
  }

  public @NotNull InstanceContainer fallbackWorld() {
    return fallbackWorld;
  }

  public static @NotNull HexServer instance() {
    return Objects.requireNonNull(INSTANCE, "HexServer#INSTANCE wasn't initialized yet. Initialize a server using HexServerInitializer");
  }

  public static @NotNull Optional<HexServer> optionalInstance() {
    return Optional.ofNullable(INSTANCE);
  }
}