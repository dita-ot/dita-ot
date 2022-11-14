/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2022 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.util;

import org.junit.Test;

import static org.junit.Assert.*;

public class GraphTest {

    @Test
    public void constructor_zero() {
        final Graph act = new Graph(0);
        assertEquals(0, act.getSize());
    }

    @Test
    public void constructor_positive() {
        final Graph act = new Graph(10);
        assertEquals(10, act.getSize());
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_negative() {
        final Graph act = new Graph(-10);
    }

    @Test
    public void addEdge_topLeft() {
        final Graph act = new Graph(10);
        act.addEdge(0, 0);

        assertEdged(act, new int[][]{
                {0, 0}
        });
    }

    @Test
    public void addEdge_topRight() {
        final Graph act = new Graph(10);
        act.addEdge(0, 9);

        assertEdged(act, new int[][]{
                {0, 9}
        });
    }

    @Test
    public void addEdge_bottomRight() {
        final Graph act = new Graph(10);
        act.addEdge(9, 9);

        assertEdged(act, new int[][]{
                {9, 9}
        });
    }

    @Test
    public void addEdge_bottomLeft() {
        final Graph act = new Graph(10);
        act.addEdge(9, 0);

        assertEdged(act, new int[][]{
                {9, 0}
        });
    }

    @Test
    public void addEdge_self() {
        final Graph act = new Graph(10);
        act.addEdge(1, 1);

        assertEdged(act, new int[][]{
                {1, 1}
        });
    }

    @Test
    public void addEdge_other() {
        final Graph act = new Graph(10);
        act.addEdge(0, 1);

        assertEdged(act, new int[][]{
                {0, 1}
        });
    }

    @Test
    public void addEdge_bidirectional() {
        final Graph act = new Graph(10);
        act.addEdge(1, 2);
        act.addEdge(2, 1);

        assertEdged(act, new int[][]{
                {1, 2},
                {2, 1}
        });
    }

    @Test
    public void addEdge_circular() {
        final Graph act = new Graph(10);
        act.addEdge(0, 1);
        act.addEdge(1, 2);
        act.addEdge(2, 0);

        assertEdged(act, new int[][]{
                {0, 1},
                {1, 2},
                {2, 0}
        });
    }

    @Test
    public void addEdge_all() {
        final Graph act = new Graph(2);
        act.addEdge(0, 0);
        act.addEdge(0, 1);
        act.addEdge(1, 0);
        act.addEdge(1, 1);

        assertEdged(act, new int[][]{
                {0, 0},
                {0, 1},
                {1, 0},
                {1, 1}
        });
    }

    private void assertEdged(Graph graph, int[][] arcs) {
        for (int i = 0; i < 10; i++) {
            inner: for (int j = 0; j < 10; j++) {
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
    public void removeEdge_single() {
        final Graph act = new Graph(10);
        act.addEdge(0, 1);
        act.removeEdge(0, 1);

        assertEdged(act, new int[][]{});
    }

    @Test
    public void removeEdge_notExisting() {
        final Graph act = new Graph(10);
        act.removeEdge(0, 1);

        assertEdged(act, new int[][]{});
    }

    @Test
    public void removeEdge_all() {
        final Graph act = new Graph(2);
        act.addEdge(0, 0);
        act.addEdge(0, 1);
        act.addEdge(1, 0);
        act.addEdge(1, 1);

        act.removeEdge(0, 0);
        act.removeEdge(0, 1);
        act.removeEdge(1, 0);
        act.removeEdge(1, 1);

        assertEdged(act, new int[][]{});
    }

    @Test
    public void isEdge() {
        final Graph act = new Graph(2);
        act.addEdge(0, 1);

        assertTrue(act.isEdge(0, 1));
    }

    @Test
    public void isEdge_notFound() {
        final Graph act = new Graph(2);

        assertFalse(act.isEdge(0, 1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void isEdge_negativeSource() {
        final Graph act = new Graph(2);

        assertFalse(act.isEdge(-1, 0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void isEdge_negativeDestination() {
        final Graph act = new Graph(2);

        assertFalse(act.isEdge(0, -1));
    }
}