package com.github.ynverxe.hexserver.test;

import com.github.ynverxe.configuratehelper.handler.FastConfiguration;
import com.github.ynverxe.configuratehelper.handler.source.URLConfigurationFactory;
import com.github.ynverxe.hexserver.HexServerInitializer;
import com.github.ynverxe.hexserver.internal.configuration.ServerConfiguration;
import net.minestom.server.MinecraftServer;
import net.minestom.server.ServerProcess;
import net.minestom.server.extensions.ExtensionManager;

import java.io.File;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class TestServerInitializer extends HexServerInitializer {

  public TestServerInitializer(Path serverDir, URLConfigurationFactory configurationFactory, FastConfiguration serverConfiguration, ServerConfiguration serverConfigurationValues, MinecraftServer server, ServerProcess process, boolean registerDefaultListeners, ExtensionManager extensionManager) {
    super(serverDir, configurationFactory, serverConfiguration, serverConfigurationValues, server, process, registerDefaultListeners, extensionManager);
  }

  public static void startServer() throws Throwable {
    File file = Files.createTempDirectory("test-server").toFile();
    file.deleteOnExit();

    String userDir = System.getProperty("user.dir");
    Path serverDirPath = Paths.get(userDir, "test-server");

    MinecraftServer server = MinecraftServer.init();
    ServerProcess process = MinecraftServer.process();

    HexServerInitializer initializer = new TestServerInitializer(
        serverDirPath,
        URLConfigurationFactory.newBuilder()
            .destContentRoot(serverDirPath)
            .configurationLoaderFactory(() -> {
              throw new UnsupportedOperationException();
            })
            .build(),
        null,
        new ServerConfiguration(InetAddress.getLocalHost().getHostAddress(), "", 25565 ,"test-server"),
        server,
        process,
        false,
        new ExtensionManager(process)
    );

    initializer.start();
  }
}