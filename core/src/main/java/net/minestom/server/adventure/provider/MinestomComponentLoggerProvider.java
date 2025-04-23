package net.minestom.server.adventure.provider;

import com.github.ynverxe.hexserver.logging.AnsiComponentLoggerProvider;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.logger.slf4j.ComponentLoggerProvider;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("all")
public class MinestomComponentLoggerProvider implements ComponentLoggerProvider {

  private final AnsiComponentLoggerProvider delegate = new AnsiComponentLoggerProvider();

  @Override
  public @NotNull ComponentLogger logger(@NotNull LoggerHelper helper, @NotNull String name) {
    return delegate.logger(helper, name);
  }
}