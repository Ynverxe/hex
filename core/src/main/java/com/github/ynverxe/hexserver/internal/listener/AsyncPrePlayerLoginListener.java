package com.github.ynverxe.hexserver.internal.listener;

import com.github.ynverxe.hexserver.HexServer;
import com.github.ynverxe.hexserver.internal.configuration.ServerConfiguration;
import com.github.ynverxe.hexserver.world.HexWorld;
import com.github.ynverxe.hexserver.world.HexWorldLookup;
import net.kyori.adventure.key.Key;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;

import java.util.function.Consumer;

public class AsyncPrePlayerLoginListener implements Consumer<AsyncPlayerConfigurationEvent> {

  private final HexServer hexServer;

  public AsyncPrePlayerLoginListener(HexServer hexServer) {
    this.hexServer = hexServer;
  }

  @Override
  public void accept(AsyncPlayerConfigurationEvent event) {
    HexWorldLookup worldManager = hexServer.extensionWorldLookup();
    ServerConfiguration configuration = hexServer.serverConfigurationValues();

    HexWorld world;

    if (configuration.spawnWorld() != null) {
      Key spawnWorldKey = Key.key(configuration.spawnWorld());
      world = worldManager.byKey(spawnWorldKey)
          .orElse(null);
    } else {
      world = worldManager.byIndex(0)
          .orElse(null);
    }

    if (world == null) {
      event.getPlayer().kick("No available instances :(");
      return;
    }

    event.setSpawningInstance(world);
  }
}