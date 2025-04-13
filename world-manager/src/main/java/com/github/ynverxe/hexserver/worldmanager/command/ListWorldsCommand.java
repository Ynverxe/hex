package com.github.ynverxe.hexserver.worldmanager.command;

import com.github.ynverxe.hexserver.HexServer;
import com.github.ynverxe.hexserver.internal.message.MessageHandler;
import com.github.ynverxe.hexserver.world.HexWorldManager;
import com.github.ynverxe.hexserver.worldmanager.WorldManagerExtension;
import com.github.ynverxe.hexserver.worldmanager.load.HexWorldLoader;
import net.kyori.adventure.text.Component;
import net.minestom.server.command.builder.Command;
import org.jetbrains.annotations.NotNull;

public class ListWorldsCommand extends Command {
  public ListWorldsCommand(@NotNull WorldManagerExtension extension) {
    super("list-worlds", "lws", "lw");

    HexServer server = HexServer.instance();
    HexWorldManager worldManager = extension.worldManager();
    MessageHandler messageHandler = extension.messageHandler();

    addSyntax((sender, context) -> {
      Component header = messageHandler.find("list-worlds.header", "{count}", worldManager.count());
      sender.sendMessage(header);

      server.extensionWorldLookup().internalView().forEach(world -> {
        HexWorldLoader.SourceData sourceData = HexWorldLoader.SourceData.fromHexWorld(world);

        String loader = sourceData != null ? sourceData.key().toString() : "none";
        String path = sourceData != null ? sourceData.path().toString() : "none";
        Component entry = messageHandler.find("list-worlds.entry", "{key}", world.key(), "{path}", path, "{loader}", loader);

        sender.sendMessage(entry);
      });
    });
  }
}