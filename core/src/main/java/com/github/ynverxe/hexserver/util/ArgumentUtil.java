package com.github.ynverxe.hexserver.util;

import org.jetbrains.annotations.NotNull;

public final class ArgumentUtil {

  private ArgumentUtil() {
  }

  public static boolean isArgumentPresent(@NotNull String argument, @NotNull String[] args) {
    for (@NotNull String arg : args) {
      if (("--" + argument).equals(arg)) {
        return true;
      }
    }

    return false;
  }
}