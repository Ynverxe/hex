package com.github.ynverxe.hexserver.launcher;

import com.github.ynverxe.hexserver.HexServerInitializer;
import com.github.ynverxe.hexserver.launcher.extension.ExtensionDownloader;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.nio.file.Paths;

public class HexServerLauncher {

  public static final String DONT_REGISTER_DEFAULT_LISTENERS_ARGUMENT = "DontRegisterDefaultListeners";
  public static final String IGNORE_URL_FILES_ARGUMENT = "IgnoreUrlFiles";

  public static void main(String[] args) throws Exception {
    String runDir = System.getProperty("hexserver.rundir", System.getProperty("user.dir"));

    Path runDirPath = Paths.get(runDir);

    new ServerDirectorySchemeCopier(runDirPath, isArgumentPresent(IGNORE_URL_FILES_ARGUMENT, args))
        .start();

    // Download extensions
    ExtensionDownloader downloader = new ExtensionDownloader(runDirPath);
    downloader.start();
    HexServerInitializer initializer = new HexServerInitializer(runDirPath);

    if (!isArgumentPresent(DONT_REGISTER_DEFAULT_LISTENERS_ARGUMENT, args)) {
      initializer.registerDefaultListeners();
    }

    initializer.start();
  }

  private static boolean isArgumentPresent(@NotNull String argument, @NotNull String[] args) {
    for (@NotNull String arg : args) {
      if (("-" + argument).equals(arg)) {
        return true;
      }
    }

    return false;
  }
}