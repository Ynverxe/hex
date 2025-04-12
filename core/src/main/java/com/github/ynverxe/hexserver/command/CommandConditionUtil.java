package com.github.ynverxe.hexserver.command;

import net.kyori.adventure.text.Component;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.condition.CommandCondition;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class CommandConditionUtil {

  private CommandConditionUtil() {
  }

  public static void permissionLevel(@NotNull Command command, int minPermissionLevel, @NotNull Component insufficientPermissionMessage) {
    command.setCondition((sender, commandString) -> {
      if (!(sender instanceof Player) || ((Player) sender).getPermissionLevel() >= minPermissionLevel) {
        return true;
      }
      sender.sendMessage(insufficientPermissionMessage);
      return false;
    });
  }

  public static void permissionLevel(@NotNull Command command, int minPermissionLevel) {
    permissionLevel(command, minPermissionLevel, Component.text("Insufficient permission"));
  }

  public static void onlyPlayer(@NotNull Command command, @NotNull Component consoleMessage, boolean merge) {
    CommandCondition currentCondition = command.getCondition();

    CommandCondition newCondition = (sender, commandString) -> {
      if (sender instanceof Player) {
        return true;
      }
      sender.sendMessage(consoleMessage);
      return false;
    };

    command.setCondition(merge ? (sender, commandString) -> (currentCondition == null || currentCondition.canUse(sender, commandString)) && newCondition.canUse(sender, commandString) : newCondition);
  }

  public static void onlyPlayer(@NotNull Command command, boolean merge) {
    onlyPlayer(command, Component.text("Only players can execute this command"), merge);
  }
}