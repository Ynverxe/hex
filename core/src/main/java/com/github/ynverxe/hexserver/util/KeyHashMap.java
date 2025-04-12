package com.github.ynverxe.hexserver.util;

import net.kyori.adventure.key.Key;

import java.util.HashMap;
import java.util.Map;

public class KeyHashMap<T> extends HashMap<Key, T> {
  public KeyHashMap(int initialCapacity, float loadFactor) {
    super(initialCapacity, loadFactor);
  }

  public KeyHashMap(int initialCapacity) {
    super(initialCapacity);
  }

  public KeyHashMap() {
  }

  public KeyHashMap(Map<? extends Key, ? extends T> m) {
    super(m);
  }
}