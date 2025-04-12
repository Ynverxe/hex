package com.github.ynverxe.hexserver;

import com.github.ynverxe.configuratehelper.handler.FastConfiguration;
import com.github.ynverxe.configuratehelper.handler.source.URLConfigurationFactory;
import com.github.ynverxe.hexserver.internal.configuration.ServerConfiguration;
import com.github.ynverxe.hexserver.internal.message.MessageHandler;
import com.github.ynverxe.hexserver.world.ExtensionWorldLookup;
import net.minestom.server.ServerProcess;
import net.minestom.server.extensions.ExtensionManager;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

public final class HexServer {

  static volatile HexServer INSTANCE;

  private final Path serverDir;
  private final URLConfigurationFactory configurationFactory;
  private final FastConfiguration serverConfiguration;
  private final ServerConfiguration serverConfigurationValues;

  private final ServerProcess process;

  private final ExtensionWorldLookup extensionWorldLookup;

  private final MessageHandler messageHandler;
  private final ExtensionManager extensions;

  HexServer(Path serverDir, URLConfigurationFactory configurationFactory, FastConfiguration serverConfiguration, ServerConfiguration serverConfigurationValues, ExtensionManager extensions, ServerProcess process) throws IOException {
    this.serverDir = serverDir;
    this.configurationFactory = configurationFactory;
    this.serverConfiguration = serverConfiguration;
    this.serverConfigurationValues = serverConfigurationValues;
    this.process = process;
    this.extensions = extensions;
    this.messageHandler = new MessageHandler(this.configurationFactory);
    this.extensionWorldLookup = new ExtensionWorldLookup(this);
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

  public MessageHandler messageHandler() {
    return messageHandler;
  }

  public ExtensionManager extensions() {
    return extensions;
  }

  public static @NotNull HexServer instance() {
    return Objects.requireNonNull(INSTANCE, "HexServer#INSTANCE wasn't initialized yet. Initialize a server using HexServerInitializer");
  }
}