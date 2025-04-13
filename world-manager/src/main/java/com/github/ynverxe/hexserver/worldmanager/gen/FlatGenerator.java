package com.github.ynverxe.hexserver.worldmanager.gen;

import com.github.ynverxe.hexserver.worldmanager.WorldConfigDefinition;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.generator.GenerationUnit;
import net.minestom.server.instance.generator.Generator;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.world.biome.Biome;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FlatGenerator implements Generator {

  private static final Logger LOGGER = LoggerFactory.getLogger(FlatGenerator.class);

  private final String presets;

  public FlatGenerator(@NotNull WorldConfigDefinition definition) {
    this(definition.generator.data.node("layers").getString());
  }

  public FlatGenerator(@NotNull String presets) {
    this.presets = Objects.requireNonNull(presets);
  }

  @Override
  public void generate(@NotNull GenerationUnit generationUnit) {
    VanillaWorldPresetParser.Data data = VanillaWorldPresetParser.parsePreset(presets);

    int minHeight = -64;
    for (VanillaWorldPresetParser.LayerEntry layer : data.layers) {
      Block block = Block.fromKey(layer.blockID());

      if (block == null) {
        LOGGER.warn("Unknown Block {}", layer.blockID());
        continue;
      }

      generationUnit.modifier().fillHeight(minHeight, minHeight += layer.count(), block);

      DynamicRegistry.Key<Biome> biomeKey = DynamicRegistry.Key.of(data.biome);
      generationUnit.modifier().fillBiome(biomeKey);
    }
  }

  public static final class VanillaWorldPresetParser {

    private VanillaWorldPresetParser() {
    }

    public static final class LayerEntry {
      int count;
      String blockID;

      LayerEntry(int count, String blockID) {
        this.count = count;
        this.blockID = blockID;
      }

      public int count() {
        return count;
      }

      public String blockID() {
        return blockID;
      }

      @Override
      public String toString() {
        return String.format("  count: %d\n  blockID: %s", count, blockID);
      }
    }

    public static Data parsePreset(String presetCode) {
      List<LayerEntry> entries = new ArrayList<>();

      String[] parts = presetCode.split(";");

      String blockList = parts[0];
      String biome = parts.length == 2 ? parts[1] : "minecraft:plains";

      String[] blocks = blockList.split(",");

      for (String block : blocks) {
        if (block.contains("*")) {
          String[] blockParts = block.split("\\*");
          int count = Integer.parseInt(blockParts[0].trim());
          String blockID = blockParts[1].trim();
          entries.add(new LayerEntry(count, blockID));
        } else {
          entries.add(new LayerEntry(1, block.trim()));
        }
      }

      return new Data(entries, biome);
    }

    public static final class Data {
      public final List<LayerEntry> layers;
      public final String biome;

      public Data(List<LayerEntry> layers, String biome) {
        this.layers = layers;
        this.biome = biome;
      }
    }
  }
}