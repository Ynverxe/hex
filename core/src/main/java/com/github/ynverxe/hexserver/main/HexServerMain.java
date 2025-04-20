package com.github.ynverxe.hexserver.main;

import com.github.ynverxe.hexserver.HexServerInitializer;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.github.ynverxe.hexserver.util.ArgumentUtil.isArgumentPresent;

/**
 * Default entry point, used in launcher module.
 */
public class HexServerMain {

  public static final String DONT_REGISTER_DEFAULT_LISTENERS_ARGUMENT = "DontRegisterDefaultListeners";

  public static void main(String[] args) throws IOException, ClassNotFoundException {
    String runDir = System.getProperty("hexserver.rundir", System.getProperty("user.dir"));

    Path runDirPath = Paths.get(runDir);

    HexServerInitializer initializer = new HexServerInitializer(runDirPath);

    if (!isArgumentPresent(DONT_REGISTER_DEFAULT_LISTENERS_ARGUMENT, args)) {
      initializer.registerDefaultListeners();
    }

    initializer.start();
  }
}