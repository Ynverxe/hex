package com.github.ynverxe.hexserver.world;

import com.github.ynverxe.hexserver.HexServer;
import net.kyori.adventure.key.Key;
import net.minestom.server.instance.InstanceManager;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.stream.Stream;

public class HexWorldManager implements HexWorldLookup, HexWorld.Holder {

  private final InstanceManager instanceManager;
  private final String id;

  public HexWorldManager(@NotNull String id) {
    this.instanceManager = new InstanceManager(HexServer.instance().process());
    this.id = Objects.requireNonNull(id, "id");
  }

  @Override
  public @NotNull Stream<HexWorld> internalView() {
    return this.instanceManager.getInstances().stream()
        .filter(instance -> instance instanceof HexWorld)
        .map(instance -> (HexWorld) instance);
  }

  @Override
  public int count() {
    return instanceManager.getInstances().size();
  }

  public void register(@NotNull HexWorld world) throws IllegalArgumentException {
    ensureNameIsNotBusy(world.name());
    if (world.holder() != null)
      throw new IllegalArgumentException("World already has a holder");

    world.setHolder(this);

    this.instanceManager.registerInstance(world);
  }

  public boolean unregister(@NotNull HexWorld world) {
    if (world.holder() != this) return false;

    this.instanceManager.unregisterInstance(world);
    world.setHolder(null);

    return filter(other -> Objects.equals(world, other)).isEmpty();
  }

  @Override
  public @NotNull String id() {
    return id;
  }

  @ApiStatus.Internal
  public void ensureNameIsNotBusy(@NotNull String name) {
    if (byName(name).isPresent()) {
      throw new IllegalArgumentException("Key '" + name + "' is already in use");
    }
  }
}