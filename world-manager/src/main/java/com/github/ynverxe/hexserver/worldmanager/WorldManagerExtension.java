package com.github.ynverxe.hexserver.worldmanager;

import com.github.ynverxe.configuratehelper.handler.FastConfiguration;
import com.github.ynverxe.hexserver.extension.HexExtension;
import com.github.ynverxe.hexserver.util.MessageHandler;
import com.github.ynverxe.hexserver.worldmanager.command.GoToWorld;
import com.github.ynverxe.hexserver.worldmanager.command.ListWorldsCommand;
import com.github.ynverxe.hexserver.worldmanager.command.LoadWorldCommand;
import com.github.ynverxe.hexserver.worldmanager.command.WorldInfoCommand;
import io.leangen.geantyref.TypeToken;
import net.minestom.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class WorldManagerExtension extends HexExtension {

  private static final Logger LOGGER = LoggerFactory.getLogger(WorldManagerExtension.class);

  private WorldImporter importer;
  private MessageHandler messageHandler;

  public WorldManagerExtension() throws IOException {
  }

  @Override
  public void initialize() {
    try {
      this.importer = new WorldImporter(worldManager());
      this.messageHandler = new MessageHandler(configurationFactory());

      MinecraftServer.getCommandManager().register(
          new ListWorldsCommand(this), new WorldInfoCommand(this), new LoadWorldCommand(this.importer), new GoToWorld());

      loadWorldsFromManifest();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void terminate() {

  }

  private void loadWorldsFromManifest() throws IOException {
    FastConfiguration configuration = configurationFactory().create("worlds.yml", "worlds.yml");

    List<WorldConfigDefinition> worldDefinitions = configuration.node().get(new TypeToken<List<WorldConfigDefinition>>() {});

    for (WorldConfigDefinition worldDefinition : worldDefinitions) {
      try {
        importer.loadWorld(worldDefinition);
      } catch (Exception e) {
        LOGGER.error("Cannot load world '{}'", worldDefinition.name, e);
      }
    }
  }

  public WorldImporter importer() {
    return importer;
  }

  public MessageHandler messageHandler() {
    return messageHandler;
  }
}