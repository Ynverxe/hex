package com.github.ynverxe.hexserver.main;

import com.github.ynverxe.hexserver.HexServerInitializer;
import com.github.ynverxe.hexserver.internal.ParentProcessChecker;
import org.checkerframework.checker.units.qual.A;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Supplier;

import static com.github.ynverxe.hexserver.util.ArgumentUtil.isArgumentPresent;

/**
 * Default entry point, used in launcher module.
 */
public class HexServerMain {

  public static final String DONT_REGISTER_DEFAULT_LISTENERS_ARGUMENT = "DontRegisterDefaultListeners";
  private static volatile boolean STARTED = false;

  public static void main(String[] args) throws IOException, ClassNotFoundException {
    synchronized (HexServerMain.class) {
      if (STARTED) {
        throw new IllegalArgumentException("main(String[]) was already been called");
      }

      STARTED = true;

      if (System.getenv("parent-pid") != null) {
        ParentProcessChecker.INSTANCE.start();
      }

      String runDir = System.getProperty("hexserver.rundir", System.getProperty("user.dir"));

      Path runDirPath = Paths.get(runDir);

      HexServerInitializer initializer = new HexServerInitializer(runDirPath);

      if (!isArgumentPresent(DONT_REGISTER_DEFAULT_LISTENERS_ARGUMENT, args)) {
        initializer.registerDefaultListeners();
      }

      initializer.startArguments(List.of(args));

      initializer.start();
    }
  }

  public static boolean started() {
    return STARTED;
  }

  public static @NotNull Optional<List<String>> arguments() {
    return Optional.ofNullable(ARGUMENTS)
        .map(Collections::unmodifiableList);
  }

  @ApiStatus.Internal
  public static void requiresMainStarted(@NotNull Object consumer) {
    if (!started()) {
      throw requiresMainStartedException(consumer).get();
    }
  }

  @ApiStatus.Internal
  public static Supplier<IllegalStateException> requiresMainStartedException(@NotNull Object consumer) {
    return () -> new IllegalStateException(consumer + " cannot be used before main is started");
  }
}