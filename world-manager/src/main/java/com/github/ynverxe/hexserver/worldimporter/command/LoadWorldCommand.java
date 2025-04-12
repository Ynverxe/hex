package com.github.ynverxe.hexserver.worldimporter.command;

import com.github.ynverxe.hexserver.HexServer;
import com.github.ynverxe.hexserver.command.argument.KyoriKeyArgumentMapper;
import com.github.ynverxe.hexserver.world.HexWorldManager;
import com.github.ynverxe.hexserver.worldimporter.WorldConfigDefinition;
import com.github.ynverxe.hexserver.worldimporter.WorldImporter;
import com.github.ynverxe.hexserver.worldimporter.load.HexWorldLoader;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.BasicConfigurationNode;

import java.nio.file.Path;
import java.nio.file.Paths;

import static java.util.Objects.*;
import static net.minestom.server.command.builder.arguments.ArgumentType.*;
import static com.github.ynverxe.hexserver.command.argument.HexArgumentTypes.*;

public class LoadWorldCommand extends Command {

  private final HexWorldManager worldManager;

  public LoadWorldCommand(@NotNull WorldImporter importer) {
    super("load-world");
    this.worldManager = importer.worldManager();

    Argument<Path> pathArgument = String("path")
        .map((sender, s) -> Paths.get(s));

    Argument<Key> keyArgument = keyArgument("key");

    Argument<Key> loaderKeyArgument = keyArgument("loader")
        .setSuggestionCallback((sender, context, suggestion) -> {
          for (Key someLoaderKey : importer.worldLoaders().keySet()) {
            suggestion.addEntry(new SuggestionEntry(someLoaderKey.toString()));
          }
        });

    Argument<String> dimensionKeyArgument = Word("dimension")
        .setSuggestionCallback((sender, context, suggestion) -> {
          DynamicRegistry<DimensionType> dimensions = HexServer.instance().process().dimensionType();
          for (DimensionType value : dimensions.values()) {
            suggestion.addEntry(new SuggestionEntry(dimensions.getKey(value).key().toString()));
          }
        })
        .setDefaultValue("minecraft:overworld");

    Argument<Key> generatorKeyArgument = keyArgument("generator")
        .setSuggestionCallback((sender, context, suggestion) -> {
          for (Key someGenFactoryKey : importer.generatorFactories().keySet()) {
            suggestion.addEntry(new SuggestionEntry(someGenFactoryKey.toString()));
          }
        })
        .setDefaultValue(Key.key("default:void"));

    Argument<Boolean> persistentArgument = Boolean("persistent")
        .setDefaultValue(false);

    addSyntax((sender, context) -> loadWorld(
        context.get(pathArgument),
        context.get(keyArgument),
        context.get(loaderKeyArgument),
        context.get(dimensionKeyArgument),
        context.get(generatorKeyArgument),
        context.get(persistentArgument),
        sender,
        importer
    ), pathArgument, keyArgument, loaderKeyArgument, dimensionKeyArgument, generatorKeyArgument, persistentArgument);
  }

  private void loadWorld(@NotNull Path path, @NotNull Key worldKey, @NotNull Key loaderKey,
                         @Nullable String dimensionTypeKey, @Nullable Key generatorKey,
                         boolean persistent, @NotNull CommandSender sender, @NotNull WorldImporter importer) {
    if (worldManager.filter(world -> {
      HexWorldLoader.SourceData sourceData = HexWorldLoader.SourceData.fromHexWorld(world);

      return sourceData != null && sourceData.path().equals(path);
    }).isPresent()) {
      sender.sendMessage(Component.text("That world was already loaded!"));
      return;
    }

    if (worldManager.byKey(worldKey).isPresent()) {
      sender.sendMessage(Component.text("Key '" + worldKey + "' is already in use."));
      return;
    }

    WorldConfigDefinition.GeneratorOptions options = new WorldConfigDefinition.GeneratorOptions(
        requireNonNullElse(generatorKey.toString(), "default:void"), BasicConfigurationNode.root()
    );

    WorldConfigDefinition worldConfigDefinition = new WorldConfigDefinition(
        worldKey.toString(), options, path, requireNonNullElse(dimensionTypeKey, "minecraft:overworld"), loaderKey.toString(), null
    );

    try {
      importer.loadWorld(worldConfigDefinition);
    } catch (Exception e) {
      sender.sendMessage("Cannot load that world. Reason: " + e.getMessage());
    }

    // TODO: Make world persistent if specified
  }
}
