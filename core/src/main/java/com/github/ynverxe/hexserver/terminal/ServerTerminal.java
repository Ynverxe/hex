package com.github.ynverxe.hexserver.terminal;

import com.github.ynverxe.hexserver.HexServer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.timer.TaskSchedule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;
import java.util.concurrent.atomic.AtomicReference;

public class ServerTerminal implements Runnable {

  public static final AtomicReference<ServerTerminal> INSTANCE = new AtomicReference<>(null);

  private static final Logger LOGGER = LoggerFactory.getLogger(ServerTerminal.class);
  private final Scanner scanner = new Scanner(System.in);

  private ServerTerminal() {}

  @Override
  public void run() {
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

  private void start() {
    MinecraftServer.getSchedulerManager()
        .buildTask(this)
        .repeat(TaskSchedule.immediate())
        .schedule();

    HexServer.instance().addShutdownListener(hexServer -> {
      INSTANCE.set(null);
      scanner.close();
    });
  }

  public static void init() {
    INSTANCE.getAndUpdate(serverTerminal -> {
      if (serverTerminal != null)
        throw new IllegalStateException("Cannot override instance");

      ServerTerminal terminal = new ServerTerminal();
      terminal.start();
      return terminal;
    });
  }
}