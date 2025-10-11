/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2022 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class GraphTest {

  @Test
  void constructor_zero() {
    final Graph act = new Graph(0);
    assertEquals(0, act.getSize());
  }

  @Test
  void constructor_positive() {
    final Graph act = new Graph(10);
    assertEquals(10, act.getSize());
  }

  @Test
  void constructor_negative() {
    assertThrows(IllegalArgumentException.class, () -> new Graph(-10));
  }

  @Test
  void addEdge_topLeft() {
    final Graph act = new Graph(10);
    act.addEdge(0, 0);

    assertEdged(act, new int[][] { { 0, 0 } });
  }

  @Test
  void addEdge_topRight() {
    final Graph act = new Graph(10);
    act.addEdge(0, 9);

    assertEdged(act, new int[][] { { 0, 9 } });
  }

  @Test
  void addEdge_bottomRight() {
    final Graph act = new Graph(10);
    act.addEdge(9, 9);

    assertEdged(act, new int[][] { { 9, 9 } });
  }

  @Test
  void addEdge_bottomLeft() {
    final Graph act = new Graph(10);
    act.addEdge(9, 0);

    assertEdged(act, new int[][] { { 9, 0 } });
  }

  @Test
  void addEdge_self() {
    final Graph act = new Graph(10);
    act.addEdge(1, 1);

    assertEdged(act, new int[][] { { 1, 1 } });
  }

  @Test
  void addEdge_other() {
    final Graph act = new Graph(10);
    act.addEdge(0, 1);

    assertEdged(act, new int[][] { { 0, 1 } });
  }

  @Test
  void addEdge_bidirectional() {
    final Graph act = new Graph(10);
    act.addEdge(1, 2);
    act.addEdge(2, 1);

    assertEdged(act, new int[][] { { 1, 2 }, { 2, 1 } });
  }

  @Test
  void addEdge_circular() {
    final Graph act = new Graph(10);
    act.addEdge(0, 1);
    act.addEdge(1, 2);
    act.addEdge(2, 0);

    assertEdged(act, new int[][] { { 0, 1 }, { 1, 2 }, { 2, 0 } });
  }

  @Test
  void addEdge_all() {
    final Graph act = new Graph(2);
    act.addEdge(0, 0);
    act.addEdge(0, 1);
    act.addEdge(1, 0);
    act.addEdge(1, 1);

    assertEdged(act, new int[][] { { 0, 0 }, { 0, 1 }, { 1, 0 }, { 1, 1 } });
  }

  private void assertEdged(Graph graph, int[][] arcs) {
    for (int i = 0; i < 10; i++) {
      inner:for (int j = 0; j < 10; j++) {
        final boolean act = graph.isEdge(i, j);
        for (int[] arc : arcs) {
          if (i == arc[0] && j == arc[1]) {
            assertTrue(act);
            break inner;
          }
        }
        assertFalse(act);
      }
    }
  }

  @Test
  void removeEdge_single() {
    final Graph act = new Graph(10);
    act.addEdge(0, 1);
    act.removeEdge(0, 1);

    assertEdged(act, new int[][] {});
  }

  @Test
  void removeEdge_notExisting() {
    final Graph act = new Graph(10);
    act.removeEdge(0, 1);

    assertEdged(act, new int[][] {});
  }

  @Test
  void removeEdge_all() {
    final Graph act = new Graph(2);
    act.addEdge(0, 0);
    act.addEdge(0, 1);
    act.addEdge(1, 0);
    act.addEdge(1, 1);

    act.removeEdge(0, 0);
    act.removeEdge(0, 1);
    act.removeEdge(1, 0);
    act.removeEdge(1, 1);

    assertEdged(act, new int[][] {});
  }

  @Test
  void isEdge() {
    final Graph act = new Graph(2);
    act.addEdge(0, 1);

    assertTrue(act.isEdge(0, 1));
  }

  @Test
  void isEdge_notFound() {
    final Graph act = new Graph(2);

    assertFalse(act.isEdge(0, 1));
  }

  @Test
  void isEdge_negativeSource() {
    final Graph act = new Graph(2);

    assertThrows(IllegalArgumentException.class, () -> act.isEdge(-1, 0));
  }

  @Test
  void isEdge_negativeDestination() {
    final Graph act = new Graph(2);

    assertThrows(IllegalArgumentException.class, () -> act.isEdge(0, -1));
  }
}
