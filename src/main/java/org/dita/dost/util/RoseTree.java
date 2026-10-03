/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2025 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * Mutable Rose Tree data structure.
 *
 * @param <T> value type
 */
public class RoseTree<T> {

  private final T value;
  private List<RoseTree<T>> children;

  /**
   * Create a new Rose Tree.
   *
   * @param value    node value
   * @param children child nodes
   */
  public RoseTree(T value, List<RoseTree<T>> children) {
    this.value = value;
    this.children = children != null ? new ArrayList<>(children) : null;
  }

  /**
   * Create new tree with no children.
   *
   * @param value node value
   */
  public RoseTree(T value) {
    this.value = value;
    this.children = null;
  }

  /**
   * Add child tree.
   *
   * @param child child tree
   */
  public synchronized void addChild(RoseTree<T> child) {
    if (children == null) {
      children = new ArrayList<>();
    }
    children.add(child);
  }

  /**
   * Get node value.
   *
   * @return node value
   */
  public T getValue() {
    return value;
  }

  /**
   * Get all children of tree.
   *
   * @return tree children
   */
  public List<RoseTree<T>> getChildren() {
    if (children == null) {
      return Collections.emptyList();
    }
    return Collections.unmodifiableList(children);
  }

  /**
   * Flatten tree values in depth-first order.
   *
   * @return all tree node values
   */
  public Stream<T> flatten() {
    if (children == null) {
      return Stream.of(value);
    }
    return Stream.concat(Stream.of(value), children.stream().flatMap(RoseTree::flatten));
  }
}
