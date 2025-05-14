package com.github.ynverxe.hexserver.command.argument;

import com.github.ynverxe.hexserver.HexServer;
import com.github.ynverxe.hexserver.extension.HexExtension;
import com.github.ynverxe.hexserver.world.HexWorld;
import com.github.ynverxe.hexserver.world.HexWorldLookup;
import net.kyori.adventure.key.Key;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import org.jetbrains.annotations.NotNull;

public final class HexArgumentTypes {

  private HexArgumentTypes() {
  }

  public static @NotNull Argument<HexExtension> extensionArgument(@NotNull String id) {
    Argument<HexExtension> argument = ArgumentType.Word(id)
        .map(new ExtensionArgumentMapper());

    argument.setSuggestionCallback((sender, context, suggestion) -> {
      HexServer.instance().extensions().extensions().forEach(extension -> suggestion.addEntry(new SuggestionEntry(extension.name())));
    });

    return argument;
  }

  public static @NotNull Argument<HexWorld> worldArgument(@NotNull String id, @NotNull HexWorldLookup worldLookup) {
    Argument<HexWorld> worldArgument = ArgumentType.Word(id)
        .map(new KyoriKeyArgumentMapper())
        .map(new HexWorldArgumentMapper(worldLookup));

    worldArgument.setSuggestionCallback((sender, context, suggestion) -> {
      worldLookup.internalView().forEach(world -> suggestion.addEntry(new SuggestionEntry(world.key().toString())));
    });

    return worldArgument;
  }

  public static @NotNull Argument<HexWorld> worldArgument(@NotNull String id) {
    return worldArgument(id, HexServer.instance().extensionWorldLookup());
  }

  public static @NotNull Argument<Key> keyArgument(@NotNull String id) {
    return ArgumentType.Word(id).map(new KyoriKeyArgumentMapper());
  }
}