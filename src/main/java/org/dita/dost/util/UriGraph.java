/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2022 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.util;

import java.net.URI;
import java.util.*;

/**
 * Directed Graph for URIs.
 */
public class UriGraph {

  private Graph graph;
  private Map<URI, Integer> indexMap;

  public UriGraph(int initialSize) {
    indexMap = new HashMap<>();
    graph = new Graph(initialSize);
  }

  public synchronized void add(URI from, URI to) {
    assert from.isAbsolute();
    assert to.isAbsolute();
    int source = indexMap.computeIfAbsent(from.normalize(), uri -> indexMap.size());
    int destination = indexMap.computeIfAbsent(to.normalize(), uri -> indexMap.size());
    graph.addEdge(source, destination);
  }

  public synchronized void remove(URI from, URI to) {
    assert from.isAbsolute();
    assert to.isAbsolute();
    Integer source = indexMap.get(from);
    Integer destination = indexMap.get(to);
    if (source != null && destination != null) {
      graph.removeEdge(source, destination);
    }
  }

  // TODO: Rename to contains
  public boolean isEdge(URI from, URI to) {
    assert from.isAbsolute();
    assert to.isAbsolute();
    Integer source = indexMap.get(from);
    Integer destination = indexMap.get(to);
    if (source != null && destination != null) {
      return graph.isEdge(source, destination);
    }
    return false;
  }

  // TODO: This should be Map<URI, List<URI>>
  public List<Map.Entry<URI, URI>> getAll() {
    List<Map.Entry<URI, URI>> res = new ArrayList<>();
    // TODO: This should access the data directly
    boolean[][] data = graph.getData();
    int size = data.length;
    URI[] uris = new URI[size];
    for (Map.Entry<URI, Integer> entry : indexMap.entrySet()) {
      uris[entry.getValue()] = entry.getKey();
    }
    for (int i = 0; i < size; i++) {
      for (int j = 0; j < size; j++) {
        if (data[i][j]) {
          res.add(Map.entry(uris[i], uris[j]));
        }
      }
    }
    return res;
  }
}
