package com.github.ynverxe.hexserver.launcher;

import com.github.ynverxe.hexserver.launcher.extension.ExtensionDownloader;
import com.github.ynverxe.hexserver.launcher.extension.ServerDirectorySchemeCopier;
import com.github.ynverxe.hexserver.launcher.library.LibraryDownloader;

import com.github.ynverxe.hexserver.launcher.logger.EnvironmentLogger;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class HexServerLauncher {

  private static final Logger LOGGER = LoggerFactory.getLogger(HexServerLauncher.class);
  public static final String IGNORE_URL_FILES_ARGUMENT = "IgnoreUrlFiles";

  public static void main(String[] args) throws Exception {
    EnvironmentLogger.logJvmInfo();

    String runDir = System.getProperty("hexserver.rundir", System.getProperty("user.dir"));

    Path runDirPath = Paths.get(runDir);

    // Copy server directory scheme
    new ServerDirectorySchemeCopier(runDirPath, isArgumentPresent(IGNORE_URL_FILES_ARGUMENT, args))
        .start();

    LibraryDownloader libraryDownloader = new LibraryDownloader(runDirPath);

    // Download extensions
    ExtensionDownloader downloader = new ExtensionDownloader(runDirPath);
    downloader.start();

    LOGGER.info("Starting HexServer on new process.");
    String classpath = buildClasspath(libraryDownloader.urls());
    List<String> command = new ArrayList<>();
    command.addAll(Arrays.asList("java","-cp", classpath, "com.github.ynverxe.hexserver.main.HexServerMain"));
    command.addAll(Arrays.asList(args));

    ProcessBuilder processBuilder = new ProcessBuilder(command);
    processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
    processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
    processBuilder.directory(new File(System.getProperty("user.dir")));

    processBuilder.environment().put("parent-pid", String.valueOf(ProcessHandle.current().pid()));

    Process process = processBuilder.start();
    long pid = process.pid();
    LOGGER.info("HexServer started on pid {}", pid);

    int exitCode = process.waitFor();
    LOGGER.info("HexServer process terminated with exit code {}", exitCode);
  }

  public static boolean isArgumentPresent(@NotNull String argument, @NotNull String[] args) {
    for (@NotNull String arg : args) {
      if (("--" + argument).equals(arg)) {
        return true;
      }
    }

    return false;
  }

  private static String buildClasspath(List<URL> urls) {
    if (urls.isEmpty()) return "";

    String separator = System.getProperty("os.name", "")
        .toLowerCase(Locale.ROOT).contains("windows") ? ";" : ":";

    StringBuilder builder = new StringBuilder();
    for (URL url : urls) {
      builder.append(url.getPath())
          .append(separator);
    }

    builder.deleteCharAt(builder.length() - 1);

    return builder.toString();
  }
}