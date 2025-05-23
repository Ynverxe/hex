package com.github.ynverxe.hexserver.launcher;

import com.github.ynverxe.hexserver.launcher.file.ServerDirectorySchemeCopier;
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

    LOGGER.info("Starting HexServer on new process.");
    String classpath = buildClasspath(libraryDownloader.urls());

    ProcessBuilder processBuilder = createProcessBuilder(classpath, args);
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

  private static ProcessBuilder createProcessBuilder(String classpath, String[] args) {
    List<String> command = new ArrayList<>();
    command.add("java");
    System.getProperties().forEach((key, value) -> command.add("-D" + key + "=" + value));
    command.addAll(Arrays.asList("-cp", classpath, "com.github.ynverxe.hexserver.main.HexServerMain"));
    command.addAll(Arrays.asList(args));

    ProcessBuilder processBuilder = new ProcessBuilder(command);
    processBuilder.redirectInput(ProcessBuilder.Redirect.INHERIT);
    processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
    processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
    processBuilder.directory(new File(System.getProperty("user.dir")));
    processBuilder.environment().putAll(System.getenv());
    processBuilder.environment().put("parent-pid", String.valueOf(ProcessHandle.current().pid()));
    return processBuilder;
  }
}