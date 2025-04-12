package com.github.ynverxe.hexserver.internal.message;

import com.github.ynverxe.configuratehelper.handler.FastConfiguration;
import com.github.ynverxe.configuratehelper.handler.source.URLConfigurationFactory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.PatternReplacementResult;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public final class MessageHandler {

  private final FastConfiguration messageSource;

  public MessageHandler(@NotNull URLConfigurationFactory configurationFactory) throws IOException {
    this.messageSource = configurationFactory.create("messages.yml", "messages.yml");
  }

  public Component find(@NotNull String path, @UnknownNullability Object @NotNull... replacements) {
    Object[] separatedPath = path.split("\\.");

    Object found = messageSource.node().node(separatedPath).raw();

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
}