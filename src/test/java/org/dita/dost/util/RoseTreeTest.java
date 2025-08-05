/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2025 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

public class RoseTreeTest {

  @Test
  public void testConstructorWithValueAndChildren() {
    var tree = new RoseTree<>("root", List.of(new RoseTree<>("child1"), new RoseTree<>("child2")));

    assertEquals("root", tree.getValue());
    assertEquals(2, tree.getChildren().size());
    assertEquals("child1", tree.getChildren().get(0).getValue());
    assertEquals("child2", tree.getChildren().get(1).getValue());
  }

  @Test
  public void testConstructorWithValueOnly() {
    var tree = new RoseTree<>("root");

    assertEquals("root", tree.getValue());
    assertTrue(tree.getChildren().isEmpty());
  }

  @Test
  public void testConstructorWithNullChildren() {
    var tree = new RoseTree<>("root", null);

    assertEquals("root", tree.getValue());
    assertTrue(tree.getChildren().isEmpty());
  }

  @Test
  public void testAddChild() {
    var tree = new RoseTree<>("root");

    tree.addChild(new RoseTree<>("child"));

    assertEquals(1, tree.getChildren().size());
    assertEquals("child", tree.getChildren().get(0).getValue());
  }

  @Test
  public void testAddChildMultiple() {
    var tree = new RoseTree<>("root");

    tree.addChild(new RoseTree<>("child1"));
    tree.addChild(new RoseTree<>("child2"));

    assertEquals(2, tree.getChildren().size());
    assertEquals("child1", tree.getChildren().get(0).getValue());
    assertEquals("child2", tree.getChildren().get(1).getValue());
  }

  @Test
  public void testAddChildToTreeWithExistingChildren() {
    var tree = new RoseTree<>("root", List.of(new RoseTree<>("child1")));
    var newChild = new RoseTree<>("child3");

    tree.addChild(newChild);

    assertEquals(2, tree.getChildren().size());
    assertEquals("child1", tree.getChildren().get(0).getValue());
    assertEquals("child3", tree.getChildren().get(1).getValue());
  }

  @Test
  public void testGetValue() {
    var tree = new RoseTree<>(42);

    assertEquals(42, tree.getValue());
  }

  @Test
  public void testGetChildrenEmpty() {
    var tree = new RoseTree<>("root");

    var children = tree.getChildren();

    assertNotNull(children);
    assertTrue(children.isEmpty());
  }

  @Test
  public void testGetChildrenWithChildren() {
    var tree = new RoseTree<>("root", List.of(new RoseTree<>("child1"), new RoseTree<>("child2")));

    var children = tree.getChildren();

    assertEquals(2, children.size());
    assertEquals("child1", children.get(0).getValue());
    assertEquals("child2", children.get(1).getValue());
  }

  @Test
  public void testFlattenSingleNode() {
    var tree = new RoseTree<>("root");

    var flattened = tree.flatten().toList();

    assertEquals(1, flattened.size());
    assertEquals("root", flattened.get(0));
  }

  @Test
  public void testFlattenWithChildren() {
    var tree = new RoseTree<>("root", List.of(new RoseTree<>("child1"), new RoseTree<>("child2")));

    var flattened = tree.flatten().toList();

    assertEquals(3, flattened.size());
    assertEquals("root", flattened.get(0));
    assertEquals("child1", flattened.get(1));
    assertEquals("child2", flattened.get(2));
  }

  @Test
  public void testFlattenNestedTree() {
    var tree = new RoseTree<>(
      "root",
      List.of(
        new RoseTree<>("child1", List.of(new RoseTree<>("grandchild1"), new RoseTree<>("grandchild2"))),
        new RoseTree<>("child2")
      )
    );

    var flattened = tree.flatten().toList();

    assertEquals(5, flattened.size());
    assertEquals("root", flattened.get(0));
    assertEquals("child1", flattened.get(1));
    assertEquals("grandchild1", flattened.get(2));
    assertEquals("grandchild2", flattened.get(3));
    assertEquals("child2", flattened.get(4));
  }

  @Test
  public void testFlattenComplexTree() {
    var leaf4 = new RoseTree<>("leaf4");

    var tree = new RoseTree<>(
      "root",
      List.of(
        new RoseTree<>("branch1", List.of(new RoseTree<>("leaf1"), new RoseTree<>("leaf2"))),
        new RoseTree<>("branch2", List.of(new RoseTree<>("leaf3"))),
        new RoseTree<>("branch3", List.of(leaf4))
      )
    );

    var flattened = tree.flatten().toList();

    assertEquals(8, flattened.size());
    assertEquals("root", flattened.get(0));
    assertEquals("branch1", flattened.get(1));
    assertEquals("leaf1", flattened.get(2));
    assertEquals("leaf2", flattened.get(3));
    assertEquals("branch2", flattened.get(4));
    assertEquals("leaf3", flattened.get(5));
    assertEquals("branch3", flattened.get(6));
    assertEquals("leaf4", flattened.get(7));
  }

  @Test
  public void testFlattenWithNullChildren() {
    var tree = new RoseTree<>("root", null);

    var flattened = tree.flatten().toList();

    assertEquals(1, flattened.size());
    assertEquals("root", flattened.get(0));
  }

  @Test
  public void testGenericTypes() {
    var intTree = new RoseTree<>(42);
    var stringTree = new RoseTree<>("test");
    var doubleTree = new RoseTree<>(3.14);

    assertEquals(42, intTree.getValue());
    assertEquals("test", stringTree.getValue());
    assertEquals(3.14, doubleTree.getValue());
  }
}
