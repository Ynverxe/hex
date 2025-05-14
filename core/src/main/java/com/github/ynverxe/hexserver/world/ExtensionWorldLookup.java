package com.github.ynverxe.hexserver.world;

import com.github.ynverxe.hexserver.extension.HexExtensionManager;
import com.github.ynverxe.hexserver.HexServer;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

public class ExtensionWorldLookup implements HexWorldLookup {

  private final HexExtensionManager extensionManager;

  public ExtensionWorldLookup(@NotNull HexServer hexServer) {
    this.extensionManager = hexServer.extensions();
  }

  @Override
  public @NotNull Stream<HexWorld> internalView() {
    return extensionManager.extensions()
        .stream()
        .flatMap(extension -> extension.worldManager().internalView());
  }
}