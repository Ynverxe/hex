package com.github.ynverxe.hexserver.worldmanager;

import com.github.ynverxe.hexserver.HexServer;
import com.github.ynverxe.hexserver.util.KeyHashMap;
import com.github.ynverxe.hexserver.world.HexWorld;
import com.github.ynverxe.hexserver.world.HexWorldManager;
import com.github.ynverxe.hexserver.worldmanager.gen.FlatGenerator;
import com.github.ynverxe.hexserver.worldmanager.gen.GeneratorFactory;
import com.github.ynverxe.hexserver.worldmanager.load.AnvilHexWorldLoader;
import com.github.ynverxe.hexserver.worldmanager.load.HexWorldLoader;
import com.github.ynverxe.hexserver.worldmanager.load.PolarHexWorldLoader;
import net.kyori.adventure.key.Key;
import net.minestom.server.instance.generator.Generator;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class WorldImporter {

  private static final Logger LOGGER = LoggerFactory.getLogger(WorldImporter.class);

  private final KeyHashMap<HexWorldLoader> worldLoaders = new KeyHashMap<>();
  private final KeyHashMap<GeneratorFactory> generatorFactories = new KeyHashMap<>();

  private final HexWorldManager worldManager;

  public WorldImporter(@NotNull HexWorldManager worldManager) {
    this.worldManager = Objects.requireNonNull(worldManager);
    this.worldLoaders.put(Key.key("default:polar"), new PolarHexWorldLoader());
    this.worldLoaders.put(Key.key("default:anvil"), new AnvilHexWorldLoader());
    this.generatorFactories.put(Key.key("default:flat"), FlatGenerator::new);
    this.generatorFactories.put(Key.key("default:void"), worldConfigDefinition -> unit -> {});
  }

  public KeyHashMap<HexWorldLoader> worldLoaders() {
    return worldLoaders;
  }

  public KeyHashMap<GeneratorFactory> generatorFactories() {
    return generatorFactories;
  }

  public HexWorldManager worldManager() {
    return worldManager;
  }

  public void loadWorld(@NotNull WorldConfigDefinition definition) throws Exception {
    this.worldManager.ensureNameIsNotBusy(definition.name);

    Key loaderKey = Key.key(definition.loader);
    HexWorldLoader loader = worldLoaders.get(loaderKey);

    definition.path = HexServer.instance().serverDir().resolve(definition.path);

    HexWorld world = loader.load(definition);

    Key generatorKey = Key.key(definition.generator.type);
    GeneratorFactory generatorCreator = generatorFactories.get(generatorKey);

    if (generatorCreator == null) {
      LOGGER.warn("Unknown generator type '{}'", generatorKey);
    } else {
      Generator generator = generatorCreator.create(definition);

      if (generator == null) {
        LOGGER.warn("Factory '{}' returned a null generator", generatorKey);
      } else {
        world.setGenerator(generator);
      }
    }

    worldManager.register(world);
    LOGGER.info("World '{}' '{}' with definition '{}' loaded!", world.key(), world.getUuid(), definition);
  }
}