package com.github.ynverxe.hexserver.worldimporter.gen;

import com.github.ynverxe.hexserver.worldimporter.WorldConfigDefinition;
import net.minestom.server.instance.generator.Generator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface GeneratorFactory {

  @Nullable Generator create(@NotNull WorldConfigDefinition worldConfigDefinition);

}