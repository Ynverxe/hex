package com.github.ynverxe.hexserver.terminal;

import com.github.ynverxe.hexserver.HexServer;
import net.minestom.server.command.CommandManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

public class ServerTerminal implements Runnable {

  public static final ServerTerminal INSTANCE = new ServerTerminal();

  private static final Logger LOGGER = LoggerFactory.getLogger(ServerTerminal.class);
  private final Thread thread = new Thread(this, "ServerTerminal-Thread");
  private final Scanner scanner = new Scanner(System.in);

  private ServerTerminal() {}

  @Override
  public void run() {
    while (true) {
      try {
        CommandManager commandManager = HexServer.instance().process().command();

        if (scanner.hasNext()) {
          String input = scanner.nextLine();
          commandManager.execute(commandManager.getConsoleSender(), input);
        }
      } catch (Exception e) {
        LOGGER.error("Cannot read input", e);
      }
    }
  }

  public void start() {
    if (thread.isAlive()) {
      return;
    }

    thread.setDaemon(true);
    thread.start();
  }
}