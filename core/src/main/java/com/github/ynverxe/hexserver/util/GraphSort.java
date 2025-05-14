package com.github.ynverxe.hexserver.util;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

public class GraphSort<V> {

  private final List<V> values;
  private final Collection<String> cycles;

  private GraphSort(List<V> values, Collection<String> cycles) {
    this.values = values;
    this.cycles = cycles;
  }

  public List<V> values() {
    return values;
  }

  public Collection<String> cycles() {
    return cycles;
  }

  public static <V> GraphSort<V> sort(Collection<V> nodes, Function<V, @NotNull Iterable<V>> childExtractor) {
    List<V> ordered = new ArrayList<>();
    Map<V, State> state = new HashMap<>();
    List<String> cycleList = new ArrayList<>();

    for (V node : nodes) {
      if (state.getOrDefault(node, State.UNVISITED) == State.UNVISITED) {
        visit(node, childExtractor, ordered, state, cycleList, new LinkedList<>());
      }
    }

    return new GraphSort<>(ordered, cycleList);
  }

  private static <V> void visit(
      V node, Function<V, @NotNull Iterable<V>> childExtractor,
      List<V> ordered, Map<V, State> state, List<String> cycleList, Deque<V> path) {
    state.put(node, State.VISITING);
    path.add(node);

    for (V child : childExtractor.apply(node)) {
      State childState = state.getOrDefault(child, State.UNVISITED);

      if (childState == State.UNVISITED) {
        visit(child, childExtractor, ordered, state, cycleList, path);
      } else if (childState == State.VISITING) {
        cycleList.add(String.join(" -> ", (Iterable<? extends CharSequence>) path) + " -> " + child);
        state.put(child, State.CYCLIC);
      }
    }

    path.removeLast();

    if (state.get(node) != State.CYCLIC) {
      state.put(node, State.VISITED);
      ordered.add(node);
    }
  }

  private enum State { UNVISITED, VISITING, VISITED, CYCLIC }
}