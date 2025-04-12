package com.github.ynverxe.hexserver.world;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface HexWorldLookup {

  @NotNull Stream<HexWorld> internalView();

  default int count() {
    return (int) internalView().count();
  }

  default @NotNull Optional<HexWorld> byIndex(int index) {
    int currentIndex = -1;
    Iterator<HexWorld> iterator = internalView().iterator();
    while (iterator.hasNext()) {
      HexWorld world = iterator.next();

      if (++currentIndex == index) {
        return Optional.of(world);
      }
    }

    return Optional.empty();
  }

  default @NotNull Optional<HexWorld> filter(@NotNull Predicate<HexWorld> criteria) {
    return internalView()
        .filter(criteria)
        .findFirst();
  }

  default @NotNull Optional<HexWorld> byName(@NotNull String name) {
    return filter(hexWorld -> hexWorld.name().equals(name));
  }

  default @NotNull Optional<HexWorld> byKey(@NotNull Key key) {
    return filter(hexWorld -> hexWorld.key().equals(key));
  }

  @SuppressWarnings("all")
  default @NotNull Optional<HexWorld> byKey(@NotNull String namespace, @NotNull String value) {
    return byKey(Key.key(namespace, value));
  }

  default @NotNull Set<HexWorld> byNamespace(@NotNull String namespace) {
    return internalView()
        .filter(world -> world.key().namespace().equals(namespace))
        .collect(Collectors.toSet());
  }
}