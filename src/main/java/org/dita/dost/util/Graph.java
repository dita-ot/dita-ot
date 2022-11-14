/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2022 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.util;

/**
 * Implementation of directed Graph using Adjacent Matrix.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Adjacency_matrix#Directed_graphs">Adjacent Matrix</a>
 */
public class Graph {
    private int size;
    private boolean adjacentMatrix[][];

    public Graph(int size) {
        if (size < 0) {
            throw new IllegalArgumentException();
        }
        this.size = size;
        adjacentMatrix = new boolean[size][size];
    }

    public int getSize() {
        return size;
    }

    public void addEdge(int source, int destination) {
        if (source < 0 || destination < 0) {
            throw new IllegalArgumentException();
        }
        if (source < size && destination < size) {
            adjacentMatrix[source][destination] = true;
        } else {
            synchronized (this) {
                int newSize = Math.max(source, destination);
                int newAdjacentMatrix[][] = new int[newSize][newSize];
                for (int i = 0; i < size; i++) {
                    System.arraycopy(adjacentMatrix[i], 0, newAdjacentMatrix[i], 0, size);
                }
                size = newSize;
            }
        }
    }

    public void removeEdge(int source, int destination) {
        if (source < 0 || destination < 0) {
            throw new IllegalArgumentException();
        }
        if (source < size && destination < size) {
            adjacentMatrix[source][destination] = false;
        }
    }

    public boolean isEdge(int source, int destination) {
        if (source < 0 || destination < 0) {
            throw new IllegalArgumentException();
        }
        if (destination < size && source < size) {
            return adjacentMatrix[source][destination];
        }
        return false;
    }

}