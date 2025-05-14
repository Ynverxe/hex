package com.github.ynverxe.hexserver.main;

import com.github.ynverxe.hexserver.HexServerInitializer;
import com.github.ynverxe.hexserver.internal.ParentProcessChecker;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static com.github.ynverxe.hexserver.util.ArgumentUtil.isArgumentPresent;

/**
 * Default entry point, used in launcher module.
 */
public class HexServerMain {

  public static final String DONT_REGISTER_DEFAULT_LISTENERS_ARGUMENT = "DontRegisterDefaultListeners";
  private static volatile boolean STARTED = false;

  public static void main(String[] args) throws Throwable {
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
}