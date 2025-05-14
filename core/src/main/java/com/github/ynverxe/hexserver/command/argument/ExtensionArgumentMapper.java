package com.github.ynverxe.hexserver.command.argument;

import com.github.ynverxe.hexserver.HexServer;
import com.github.ynverxe.hexserver.extension.HexExtension;

import java.util.function.Function;

public class ExtensionArgumentMapper implements Function<String, HexExtension> {
  @Override
  public HexExtension apply(String s) {
    return HexServer.instance().extensions().findExtension(s);
  }
}