package com.github.ynverxe.hexserver.worldmanager.command;

import com.github.ynverxe.hexserver.command.argument.HexArgumentTypes;
import com.github.ynverxe.hexserver.world.HexWorld;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.entity.Player;

import static com.github.ynverxe.hexserver.util.command.CommandConditionUtil.*;

public class GoToWorld extends Command {
  public GoToWorld() {
    super("goto", "gt");

    Argument<HexWorld> worldArgument = HexArgumentTypes.worldArgument("world-key");

    onlyPlayer(this, true);

    addSyntax((sender, context) -> {
      HexWorld world = context.get(worldArgument);

      Player player = (Player) sender;
      if (player.getInstance() != world) {
        player.setInstance(world);
      }
    }, worldArgument);
  }
}