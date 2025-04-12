package com.github.ynverxe.hexserver.worldimporter.load;

import com.github.ynverxe.hexserver.worldimporter.WorldConfigDefinition;
import net.hollowcube.polar.PolarLoader;
import net.minestom.server.instance.IChunkLoader;
import org.jetbrains.annotations.NotNull;

public class PolarHexWorldLoader implements HexWorldLoader.Standalone {
  @Override
  public @NotNull IChunkLoader createChunkLoader(@NotNull WorldConfigDefinition worldConfigDefinition) throws Exception {
    return new PolarLoader(worldConfigDefinition.path);
  }
}