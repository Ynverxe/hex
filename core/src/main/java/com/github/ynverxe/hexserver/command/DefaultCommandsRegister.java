package com.github.ynverxe.hexserver.command;

import com.github.ynverxe.hexserver.HexServer;
import net.minestom.server.command.CommandManager;
import org.jetbrains.annotations.NotNull;

public final class DefaultCommandsRegister {
  private DefaultCommandsRegister() {
  }

  public static void register(@NotNull CommandManager commandManager, @NotNull HexServer server) {
    commandManager.register(new GamemodeCommand());
  }
}