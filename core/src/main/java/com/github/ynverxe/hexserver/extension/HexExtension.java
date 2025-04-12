package com.github.ynverxe.hexserver.extension;

import com.github.ynverxe.configuratehelper.handler.source.URLConfigurationFactory;
import com.github.ynverxe.hexserver.HexServer;
import com.github.ynverxe.hexserver.internal.message.MessageHandler;
import com.github.ynverxe.hexserver.world.HexWorldManager;
import net.minestom.server.extensions.Extension;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import static java.nio.file.Files.copy;
import static java.nio.file.Files.exists;

public abstract class HexExtension extends Extension {

  private final URLConfigurationFactory configurationFactory;
  private final MessageHandler messageHandler;

  private final @NotNull HexWorldManager worldManager;

  public HexExtension() throws IOException {
    this.configurationFactory = HexServer.instance().configurationFactory()
        .toBuilder()
        .destContentRoot(getDataDirectory())
        .classLoader(getClass().getClassLoader())
        .build();
    this.messageHandler = new MessageHandler(this.configurationFactory);
    this.worldManager = new HexWorldManager(getOrigin().getName().toLowerCase(Locale.ROOT));
  }

  public URLConfigurationFactory configurationFactory() {
    return configurationFactory;
  }

  public MessageHandler messageHandler() {
    return messageHandler;
  }

  public @NotNull HexWorldManager worldManager() {
    return worldManager;
  }

  protected void initResourceIfMissing(String fallbackPath, String destPath) throws IOException {
    if (exists(this.getDataDirectory().resolve(destPath))) {
      return;
    }

    InputStream fallback = getClass().getClassLoader().getResourceAsStream(fallbackPath);

    if (fallback == null) {
      getLogger().error("Cannot found fallback content for '{}'", destPath);
      return;
    }

    copy(fallback, this.getDataDirectory().resolve(destPath));
  }
}