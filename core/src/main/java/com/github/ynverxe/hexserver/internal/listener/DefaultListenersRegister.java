package com.github.ynverxe.hexserver.internal.listener;

import com.github.ynverxe.hexserver.HexServer;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import org.jetbrains.annotations.NotNull;

public final class DefaultListenersRegister {

  public static void register(@NotNull GlobalEventHandler eventHandler, @NotNull HexServer hexServer) {
    eventHandler.addListener(AsyncPlayerConfigurationEvent.class, new AsyncPrePlayerLoginListener(hexServer));
  }
}