package com.github.ynverxe.hexserver.lab.extension;

import com.github.ynverxe.hexserver.HexServer;
import com.github.ynverxe.hexserver.extension.HexExtension;
import com.github.ynverxe.hexserver.world.HexWorld;
import net.minestom.server.command.builder.Command;
import net.minestom.server.instance.block.Block;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class LabExtension extends HexExtension {
  public LabExtension(@NotNull Object context) {
    super(context);
  }

  @Override
  protected void enable() throws Exception {
    HexWorld world = new HexWorld(UUID.randomUUID(), DimensionType.OVERWORLD, "world");
    worldManager().register(world);

    world.setGenerator(unit -> unit.modifier().fillHeight(0, 40, Block.GRASS_BLOCK));
    Command command = new Command("test");
    command.addSyntax((sender, context) -> {
      sender.sendMessage("Test");
    });
    HexServer.instance().process().command()
        .register(command);
  }
}