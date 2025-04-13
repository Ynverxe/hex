package com.github.ynverxe.hexserver.launcher.log;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.ansi.ANSIComponentSerializer;
import org.jetbrains.annotations.Nullable;
import org.tinylog.core.LogEntry;
import org.tinylog.writers.AbstractFormatPatternWriter;

import java.util.Map;

public class HexWriter extends AbstractFormatPatternWriter {

  /**
   * @param properties Configuration for writer
   */
  public HexWriter(Map<String, String> properties) {
    super(properties);
  }

  @Override
  public void write(LogEntry logEntry) throws Exception {
    TextColor color = pickColor(logEntry);

    String rendered = render(logEntry);
    if (color == null) {
      System.out.print(rendered);
    } else {
      System.out.print(ANSIComponentSerializer.ansi().serialize(Component.text(rendered, color)));
    }
  }

  @Override
  public void flush() throws Exception {

  }

  @Override
  public void close() throws Exception {

  }

  private @Nullable TextColor pickColor(LogEntry entry) {
    return switch (entry.getLevel()) {
      case WARN -> NamedTextColor.YELLOW;
      case ERROR, TRACE -> NamedTextColor.RED;
      case DEBUG -> NamedTextColor.DARK_PURPLE;
      default -> null;
    };
  }
}