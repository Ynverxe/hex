package com.github.ynverxe.hexserver.world;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.examination.Examinable;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.IChunkLoader;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

public class HexWorld extends InstanceContainer implements Keyed, Examinable {

  private @Nullable Holder holder;
  private final @NotNull String name;
  private final @NotNull Map<String, Function<HexWorld, Object>> properties = new LinkedHashMap<>();

  public HexWorld(@NotNull UUID uuid, DynamicRegistry.@NotNull Key<DimensionType> dimensionType, @NotNull String name) {
    this(uuid, dimensionType, dimensionType.key(), name);
  }

  public HexWorld(@NotNull UUID uuid, DynamicRegistry.@NotNull Key<DimensionType> dimensionType, @NotNull Key dimensionName, @NotNull String name) {
    this(uuid, dimensionType, null, dimensionName, name);
  }

  public HexWorld(@NotNull UUID uuid, DynamicRegistry.@NotNull Key<DimensionType> dimensionType, @Nullable IChunkLoader loader, @NotNull String name) {
    this(uuid, dimensionType, loader, dimensionType.key(), name);
  }

  public HexWorld(@NotNull UUID uuid, DynamicRegistry.@NotNull Key<DimensionType> dimensionType, @Nullable IChunkLoader loader, @NotNull Key dimensionName, @NotNull String name) {
    this(MinecraftServer.getDimensionTypeRegistry(), uuid, dimensionType, loader, dimensionName, name);
  }

  public HexWorld(@NotNull DynamicRegistry<DimensionType> dimensionTypeRegistry, @NotNull UUID uuid, DynamicRegistry.@NotNull Key<DimensionType> dimensionType, @Nullable IChunkLoader loader, @NotNull Key dimensionName, @NotNull String name) {
    super(dimensionTypeRegistry, uuid, dimensionType, loader, dimensionName);
    this.name = Objects.requireNonNull(name);
    initProperties();
  }

  @Override
  public final @NotNull Key key() {
    return Key.key(isAnonymous() ? "anonymous" : this.holder.id(), this.name);
  }

  public @NotNull String name() {
    return name;
  }

  public @Nullable Holder holder() {
    return holder;
  }

  void setHolder(@Nullable Holder holder) {
    this.holder = holder;
  }

  public boolean isAnonymous() {
    return holder == null;
  }

  @Contract("_, _ -> this")
  public @NotNull HexWorld property(@NotNull String key, @NotNull Function<HexWorld, Object> valueProvider) {
    this.properties.put(key, valueProvider);
    return this;
  }

  public @NotNull Component buildProperties() {
    TextComponent.Builder builder = Component.text();

    this.properties.forEach((key, valueProvider) -> {
      builder.append(Component.text(key)).append(Component.text(": "))
          .append(Component.text(Objects.toString(valueProvider.apply(this))));
    });

    return builder.build();
  }

  @SuppressWarnings("all")
  public @NotNull <T extends Component> T formatWithProperties(@NotNull T component) {
    for (Map.Entry<String, Function<HexWorld, Object>> entry : this.properties.entrySet()) {
      component = (T) component.replaceText(builder -> {
        builder.matchLiteral("{" + entry.getKey() + "}").replacement(Objects.toString(entry.getValue().apply(this)));
      });
    }

    return component;
  }

  private void initProperties() {
    property("key", HexWorld::key);
    property("player count", world -> world.getPlayers().size());
  }

  public interface Holder {
    @NotNull String id();
  }
}