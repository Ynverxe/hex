package com.github.ynverxe.hexserver.worldimporter.load;

import com.github.ynverxe.hexserver.worldimporter.WorldConfigDefinition;
import net.kyori.adventure.key.Key;
import net.minestom.server.instance.IChunkLoader;
import net.minestom.server.instance.anvil.AnvilLoader;
import org.jetbrains.annotations.NotNull;

public class AnvilHexWorldLoader implements HexWorldLoader.Standalone {
  @Override
  public @NotNull IChunkLoader createChunkLoader(@NotNull WorldConfigDefinition worldConfigDefinition) throws Exception {
    return new AnvilLoader(worldConfigDefinition.path);
  }
}