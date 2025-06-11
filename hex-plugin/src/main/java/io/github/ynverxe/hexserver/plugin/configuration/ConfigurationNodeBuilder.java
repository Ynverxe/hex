package io.github.ynverxe.hexserver.plugin.configuration;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.*;

public class ConfigurationNodeBuilder {

  private final ConfigurationNode node;

  public ConfigurationNodeBuilder(ConfigurationNode node) {
    this.node = Objects.requireNonNull(node, "node");
  }

  public ConfigurationNodeBuilder() {
    this(CommentedConfigurationNode.root());
  }

  protected void set(@NotNull String path, @Nullable Object value) throws SerializationException {
    ConfigurationNode target = nodeAt(path);
    target.set(value);
  }

  protected ConfigurationNode nodeAt(@NotNull String path) {
    String[] pathParts = Objects.requireNonNull(path, "path").split("\\.");

    ConfigurationNode current = this.node;
    for (String pathPart : pathParts) {
      current = current.node(pathPart);
    }
    return current;
  }

  protected void handledSet(@NotNull String path, @Nullable Object value) {
    try {
      set(path, value);
    } catch (SerializationException e) {
      throw new RuntimeException(e);
    }
  }

  protected void setList(@NotNull String path, @NotNull Collection<?> list) {
    handledSet(path, list);
  }

  protected void setList(@NotNull String path, @NotNull Object... elements) {
    setList(path, Arrays.asList(elements));
  }

  protected <E> void appendToList(@NotNull String path, @NotNull Class<E> elementType, @NotNull Collection<E> list) {
    try {
      ConfigurationNode foundNode = nodeAt(path);
      List<E> found = foundNode != null ? foundNode.getList(elementType) : Collections.emptyList();
      List<E> temp = new ArrayList<>(found != null ? found : Collections.emptyList());
      temp.addAll(list);
      setList(path, temp);
    } catch (SerializationException e) {
      throw new RuntimeException(e);
    }
  }

  @SafeVarargs
  protected final <E> void appendToList(@NotNull String path, @NotNull Class<E> elementType, @NotNull E... elements) {
    appendToList(path, elementType, Arrays.asList(elements));
  }

  public @NotNull ConfigurationNode buildCopy() {
    return node.copy();
  }
}