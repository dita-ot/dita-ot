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
public final class Graph {

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

  public boolean[][] getData() {
    boolean[][] res = new boolean[size][size];
    for (int i = 0; i < size; i++) {
      System.arraycopy(adjacentMatrix[i], 0, res[i], 0, size);
    }
    return res;
  }

  public void addEdge(int source, int destination) {
    if (source < 0 || destination < 0) {
      throw new IllegalArgumentException();
    }
    if (source >= size || destination >= size) {
      synchronized (this) {
        int newSize = Math.max(source, destination) + 1;
        boolean[][] newAdjacentMatrix = new boolean[newSize][newSize];
        for (int i = 0; i < size; i++) {
          System.arraycopy(adjacentMatrix[i], 0, newAdjacentMatrix[i], 0, size);
        }
        size = newSize;
        adjacentMatrix = newAdjacentMatrix;
      }
    }
    adjacentMatrix[source][destination] = true;
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
