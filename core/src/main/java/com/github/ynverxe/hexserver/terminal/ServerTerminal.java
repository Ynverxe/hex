package com.github.ynverxe.hexserver.terminal;

import com.github.ynverxe.hexserver.HexServer;
import net.minestom.server.ServerProcess;
import net.minestom.server.command.CommandManager;
import net.minestom.server.command.builder.CommandResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;
import java.util.concurrent.atomic.AtomicReference;

public class ServerTerminal implements Runnable {

  private static final AtomicReference<ServerTerminal> INSTANCE = new AtomicReference<>(null);
  private static final Logger LOGGER = LoggerFactory.getLogger(ServerTerminal.class);

  private final Scanner scanner = new Scanner(System.in);
  private final HexServer hexServer = HexServer.instance();

  private boolean alive = true;

  private ServerTerminal() {
    this.hexServer.addShutdownListener(hexServer -> this.shutdown());

    Thread thread = Thread.ofVirtual().unstarted(this);
    thread.setName("ServerTerminalThread");
    thread.setDaemon(true);
    thread.start();
  }

  @Override
  public void run() {
    while (alive) {
      try {
        if (scanner.hasNext()) {
          String input = scanner.nextLine();
          submitInput(input);
        }
      } catch (Exception e) {
        LOGGER.error("Cannot read input", e);
      }
    }
  }

  private void shutdown() {
    alive = false;
    INSTANCE.set(null);
    scanner.close();
  }

  private void submitInput(String input) {
    ServerProcess process = this.hexServer.process();
    process.scheduler()
        .buildTask(() -> {
          CommandManager commandManager = process.command();
          commandManager.execute(commandManager.getConsoleSender(), input);
        }).schedule();
  }

  public static void init() {
    INSTANCE.getAndUpdate(serverTerminal -> {
      if (serverTerminal != null)
        throw new IllegalStateException("Cannot override instance");

      return new ServerTerminal();
    });
  }
}