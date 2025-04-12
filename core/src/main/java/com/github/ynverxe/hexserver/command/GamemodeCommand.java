package com.github.ynverxe.hexserver.command;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;

import static com.github.ynverxe.hexserver.command.CommandConditionUtil.*;

public class GamemodeCommand extends Command {
  public GamemodeCommand() {
    super("gamemode", "gm");

    onlyPlayer(this, true);

    ArgumentEnum<GameMode> gameModeArgument = ArgumentType.Enum("gamemode", GameMode.class);
    gameModeArgument.setFormat(ArgumentEnum.Format.LOWER_CASED);
    addSyntax((sender, context) -> {
      GameMode gameMode = context.get(gameModeArgument);
      ((Player) sender).setGameMode(gameMode);
    }, gameModeArgument);
  }
}