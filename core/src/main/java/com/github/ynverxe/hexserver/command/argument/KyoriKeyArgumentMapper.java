package com.github.ynverxe.hexserver.command.argument;

import net.kyori.adventure.key.Key;

import java.util.function.Function;

public class KyoriKeyArgumentMapper implements Function<String, Key> {

  @Override
  public Key apply(String s) {
    if (!s.matches("(?:([a-z0-9_\\-.]+:)?|:)[a-z0-9_\\-./]+")) {
      return null;
    }

    return Key.key(s);
  }
}