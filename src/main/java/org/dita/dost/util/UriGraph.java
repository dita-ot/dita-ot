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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Directed Graph for URIs.
 *
 * <h2> Thread safety</h2>
 *
 * <p>{@code UriGraph} objects are safe for use by multiple concurrent threads.</p>
 */
public class UriGraph {

  private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
  private final Lock writeLock = readWriteLock.writeLock();
  private final Lock readLock = readWriteLock.readLock();

  private final Graph graph;
  private final Map<URI, Integer> indexMap;

  public UriGraph(int initialSize) {
    indexMap = new HashMap<>();
    graph = new Graph(initialSize);
  }

  public void add(URI from, URI to) {
    assert from.isAbsolute();
    assert to.isAbsolute();
    writeLock.lock();
    try {
      int source = indexMap.computeIfAbsent(from.normalize(), uri -> indexMap.size());
      int destination = indexMap.computeIfAbsent(to.normalize(), uri -> indexMap.size());
      graph.addEdge(source, destination);
    } finally {
      writeLock.unlock();
    }
  }

  public void remove(URI from, URI to) {
    assert from.isAbsolute();
    assert to.isAbsolute();
    writeLock.lock();
    try {
      Integer source = indexMap.get(from);
      Integer destination = indexMap.get(to);
      if (source != null && destination != null) {
        graph.removeEdge(source, destination);
      }
    } finally {
      writeLock.unlock();
    }
  }

  public boolean contains(URI from, URI to) {
    assert from.isAbsolute();
    assert to.isAbsolute();
    readLock.lock();
    try {
      Integer source = indexMap.get(from);
      Integer destination = indexMap.get(to);
      if (source != null && destination != null) {
        return graph.isEdge(source, destination);
      }
      return false;
    } finally {
      readLock.unlock();
    }
  }

  public Map<URI, List<URI>> getAll() {
    readLock.lock();
    try {
      Map<URI, List<URI>> res = new HashMap<>();
      int size = graph.getSize();
      URI[] uris = new URI[size];
      for (Map.Entry<URI, Integer> entry : indexMap.entrySet()) {
        uris[entry.getValue()] = entry.getKey();
      }
      for (int i = 0; i < size; i++) {
        for (int j = 0; j < size; j++) {
          if (graph.isEdge(i, j)) {
            var value = res.getOrDefault(uris[i], new ArrayList<>());
            value.add(uris[j]);
            res.put(uris[i], value);
          }
        }
      }
      return res;
    } finally {
      readLock.unlock();
    }
  }

  public List<URI> get(URI from) {
    readLock.lock();
    try {
      Integer i = indexMap.get(from);
      if (i == null) {
        return Collections.emptyList();
      }
      boolean[] data = graph.getData(i);
      int size = data.length;
      List<URI> res = new ArrayList<>(size);
      for (Map.Entry<URI, Integer> entry : indexMap.entrySet()) {
        if (data[entry.getValue()]) {
          res.add(entry.getKey());
        }
      }
      return res;
    } finally {
      readLock.unlock();
    }
  }
}
