package com.github.ynverxe.hexserver.util;

import com.github.ynverxe.configuratehelper.handler.FastConfiguration;
import com.github.ynverxe.configuratehelper.handler.source.URLConfigurationFactory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.PatternReplacementResult;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public final class MessageHandler {

  private final @NotNull Supplier<ConfigurationNode> nodeProvider;

  public MessageHandler(@NotNull Supplier<@Nullable ConfigurationNode> nodeProvider, @NotNull String nodePath) {
    Objects.requireNonNull(nodeProvider);

    this.nodeProvider = () -> {
      ConfigurationNode node = nodeProvider.get();
      return Objects.requireNonNullElseGet(node, BasicConfigurationNode::root).node((Object[]) nodePath.split("\\."));
    };
  }

  public MessageHandler(@NotNull URLConfigurationFactory configurationFactory) throws IOException {
    this(configurationFactory.create("messages.yml", "messages.yml"));
  }

  public MessageHandler(@NotNull FastConfiguration fastConfiguration, @NotNull String node) {
    this(fastConfiguration::node, node);
  }

  public MessageHandler(@NotNull FastConfiguration fastConfiguration) {
    this(fastConfiguration, "");
  }

  public MessageHandler(@NotNull ConfigurationNode node) {
    this(() -> node, "");
  }

  public Component find(@NotNull String path, @UnknownNullability Object @NotNull... replacements) {
    Object[] separatedPath = path.split("\\.");

    Object found = root().node(separatedPath).raw();

    Component message;
    if (found instanceof String) {
      message = MiniMessage.miniMessage().deserialize(Objects.toString(found));
    } else if (found instanceof List<?>) {
      TextComponent.Builder builder = Component.text();

      for (Object element : ((List<?>) found)) {
        String elementToString = element.toString();
        Component elementAsComponent = MiniMessage.miniMessage().deserialize(elementToString);

        builder.append(elementAsComponent).appendNewline();
      }

      message = builder.asComponent();
    } else {
      message = Component.text(path);
    }

    for (int i = 0; i < replacements.length; i++) {
      Object key = replacements[i++];
      Object value = replacements[i];

      message = message.replaceText(builder -> builder.matchLiteral(key.toString()).replacement(value.toString())
          .condition((first, second) -> PatternReplacementResult.REPLACE));
    }

    return message;
  }

  private ConfigurationNode root() {
    return Objects.requireNonNullElseGet(this.nodeProvider.get(), BasicConfigurationNode::root);
  }
}