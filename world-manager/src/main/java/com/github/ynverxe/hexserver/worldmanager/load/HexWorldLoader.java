package com.github.ynverxe.hexserver.worldmanager.load;

import com.github.ynverxe.hexserver.world.HexWorld;
import com.github.ynverxe.hexserver.worldmanager.WorldConfigDefinition;
import net.kyori.adventure.key.Key;
import net.minestom.server.instance.IChunkLoader;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.UUID;

public interface HexWorldLoader {

  @NotNull HexWorld load(@NotNull WorldConfigDefinition worldConfigDefinition) throws Exception;

  record SourceData(@NotNull Key key, @NotNull Path path) {
    public static final String SOURCE_DATA_KEY = "source_data";

    public static @Nullable SourceData fromHexWorld(@NotNull HexWorld world) {
      return world.tagHandler().getTag(Tag.Transient(SOURCE_DATA_KEY));
    }
  }

  interface Standalone extends HexWorldLoader {
    @Override
    default @NotNull HexWorld load(@NotNull WorldConfigDefinition worldConfigDefinition) throws Exception {
      IChunkLoader loader = createChunkLoader(worldConfigDefinition);
      HexWorld world = new HexWorld(UUID.randomUUID(),
          DynamicRegistry.Key.of(worldConfigDefinition.dimension), loader, worldConfigDefinition.name);

      Tag<SourceData> sourceDataTag = Tag.Transient(SourceData.SOURCE_DATA_KEY);
      world.tagHandler().setTag(sourceDataTag, new SourceData(Key.key(worldConfigDefinition.loader), worldConfigDefinition.path));

      return world;
    }

    @NotNull IChunkLoader createChunkLoader(@NotNull WorldConfigDefinition worldConfigDefinition) throws Exception;
  }
}