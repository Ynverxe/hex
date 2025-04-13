package com.github.ynverxe.hexserver.worldmanager.command;

import com.github.ynverxe.hexserver.command.argument.HexArgumentTypes;
import com.github.ynverxe.hexserver.util.MessageHandler;
import com.github.ynverxe.hexserver.worldmanager.WorldManagerExtension;
import com.github.ynverxe.hexserver.world.HexWorld;
import com.github.ynverxe.hexserver.worldmanager.load.HexWorldLoader;
import net.kyori.adventure.text.Component;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

public class WorldInfoCommand extends Command {
  public WorldInfoCommand(@NotNull WorldManagerExtension extension) {
    super("world-info", "winfo", "wi");

    MessageHandler messageHandler = extension.messageHandler();

    Argument<HexWorld> worldArgument = HexArgumentTypes.worldArgument("world-key");

    addSyntax((sender, context) -> {
      HexWorld world = context.get(worldArgument);

      int playerCount = world.getPlayers().size();
      long entityCount = world.getEntities().stream().filter(entity -> !(entity instanceof Player))
          .count();

      HexWorldLoader.SourceData sourceData = HexWorldLoader.SourceData.fromHexWorld(world);

      String loader = sourceData != null ? sourceData.key().toString() : "none";
      String path = sourceData != null ? sourceData.path().toString() : "none";

      Component message = messageHandler.find("world-info",
          "{key}", world.key(), "{uuid}", world.getUuid(), "{path}", path, "{dimension}", world.getDimensionType().key(),
          "{loader}", loader, "{generator}", world.generator(), "{player count}", playerCount, "{entity count}", entityCount);

      sender.sendMessage(message);
    }, worldArgument);
  }
}