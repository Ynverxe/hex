package com.github.ynverxe.hexserver.launcher.logger;

import com.github.ynverxe.hexserver.launcher.HexServerLauncher;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.nio.file.Path;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public final class EnvironmentLogger {

  private static final Logger LOGGER = LoggerFactory.getLogger("Environment");

  private EnvironmentLogger() {}

  public static void logJvmInfo() {
    RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
    LOGGER.debug("JVM Vendor: {}", runtime.getVmVendor());
    LOGGER.debug("JVM Version: {}", runtime.getVmVersion());
    LOGGER.debug("JVM Name: {}", runtime.getVmName());
  }

  public static void logComponentsInfo(@NotNull Path coreJarPath) throws IOException  {
    String launcherVersion = HexServerLauncher.class.getPackage().getImplementationVersion();
    String coreVersion;

    try (JarFile coreJarFile = new JarFile(coreJarPath.toFile())) {
      Manifest manifest = coreJarFile.getManifest();
      coreVersion = manifest.getMainAttributes().getValue(Attributes.Name.IMPLEMENTATION_VERSION);
    }

    LOGGER.debug("Launcher version: {}", launcherVersion);
    LOGGER.debug("Server version: {}", coreVersion);
  }

  public static void logMinestomCoordinates(@NotNull String coordinates) {
    LOGGER.debug("Minestom maven coordinates: {}", coordinates);
  }
}