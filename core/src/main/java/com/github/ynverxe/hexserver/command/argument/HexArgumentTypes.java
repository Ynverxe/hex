package com.github.ynverxe.hexserver.command.argument;

import com.github.ynverxe.hexserver.HexServer;
import com.github.ynverxe.hexserver.world.HexWorld;
import com.github.ynverxe.hexserver.world.HexWorldLookup;
import net.kyori.adventure.key.Key;
import net.minestom.server.command.builder.ArgumentCallback;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import org.jetbrains.annotations.NotNull;

public final class HexArgumentTypes {

  private static final ArgumentCallback DEFAULT_CALLBACK = (sender, exception) -> sender.sendMessage(exception.getMessage());

  private HexArgumentTypes() {
  }

  public static @NotNull Argument<HexWorld> worldArgument(@NotNull String id, @NotNull HexWorldLookup worldLookup) {
    Argument<HexWorld> worldArgument = ArgumentType.Word(id)
        .map(new KyoriKeyArgumentMapper())
        .map(new HexWorldArgumentMapper(worldLookup));

    worldArgument.setSuggestionCallback((sender, context, suggestion) -> {
      worldLookup.internalView().forEach(world -> suggestion.addEntry(new SuggestionEntry(world.key().toString())));
    });

    worldArgument.setCallback(DEFAULT_CALLBACK);

    return worldArgument;
  }

  public static @NotNull Argument<HexWorld> worldArgument(@NotNull String id) {
    return worldArgument(id, HexServer.instance().extensionWorldLookup());
  }

  public static @NotNull Argument<Key> keyArgument(@NotNull String id) {
    Argument<Key> keyArgument = ArgumentType.Word(id).map(new KyoriKeyArgumentMapper());
    keyArgument.setCallback(DEFAULT_CALLBACK);
    return keyArgument;
  }
}