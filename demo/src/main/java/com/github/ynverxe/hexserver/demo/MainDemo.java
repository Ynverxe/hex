package com.github.ynverxe.hexserver.demo;

import com.github.ynverxe.hexserver.HexServerInitializer;

import java.nio.file.Paths;

public class MainDemo {

  public static void main(String[] args) throws Exception {
    HexServerInitializer hexServerInitializer = new HexServerInitializer(Paths.get(System.getProperty("user.dir")  + "/run"));
    hexServerInitializer.registerDefaultListeners();
    hexServerInitializer.registerDefaultCommands();
    hexServerInitializer.start();
  }
}