package com.github.ynverxe.hexserver.command.argument;

import com.github.ynverxe.hexserver.HexServer;
import com.github.ynverxe.hexserver.world.HexWorld;
import com.github.ynverxe.hexserver.world.HexWorldLookup;
import com.github.ynverxe.hexserver.world.HexWorldManager;
import net.kyori.adventure.key.Key;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;

import java.util.Objects;
import java.util.function.Function;

public class HexWorldArgumentMapper implements Function<Key, HexWorld> {

  private final HexWorldLookup worldLookup;

  public HexWorldArgumentMapper(HexWorldLookup worldLookup) {
    this.worldLookup = Objects.requireNonNull(worldLookup);
  }

  public HexWorldArgumentMapper() {
    this(HexServer.instance().extensionWorldLookup());
  }

  @Override
  public HexWorld apply(Key key) {
    if (key == null) return null;

    return worldLookup.byKey(key).orElseThrow(() -> new ArgumentSyntaxException("That world doesn't exists", key.toString(), 0));
  }
}