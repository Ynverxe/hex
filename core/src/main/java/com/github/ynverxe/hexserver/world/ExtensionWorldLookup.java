package com.github.ynverxe.hexserver.world;

import com.github.ynverxe.hexserver.HexServer;
import com.github.ynverxe.hexserver.extension.HexExtension;
import net.minestom.server.extensions.ExtensionManager;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

public class ExtensionWorldLookup implements HexWorldLookup {

  private final ExtensionManager extensionManager;

  public ExtensionWorldLookup(@NotNull HexServer hexServer) {
    this.extensionManager = hexServer.extensions();
  }

  @Override
  public @NotNull Stream<HexWorld> internalView() {
    return extensionManager.getExtensions()
        .stream()
        .filter(extension -> extension instanceof HexExtension)
        .flatMap(extension -> ((HexExtension) extension).worldManager().internalView());
  }
}