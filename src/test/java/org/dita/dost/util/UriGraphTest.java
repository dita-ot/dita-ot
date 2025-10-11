/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2022 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.util;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UriGraphTest {

  private final URI a = URI.create("file:///Volume/tmp/a.dita");
  private final URI b = URI.create("file:///Volume/tmp/b.dita");
  private final URI c = URI.create("file:///Volume/tmp/c.dita");

  private UriGraph act;

  @BeforeEach
  void setUp() {
    act = new UriGraph(16);
  }

  @Test
  void add() {
    act.add(a, b);
    assertTrue(act.isEdge(a, b));
  }

  @Test
  void add_duplicate() {
    act.add(a, b);
    act.add(a, b);
    assertTrue(act.isEdge(a, b));
  }

  @Test
  void remove() {
    act.add(a, b);
    act.remove(a, b);
    assertFalse(act.isEdge(a, b));
  }

  @Test
  void remove_notFound() {
    act.remove(a, b);
    assertFalse(act.isEdge(a, b));
  }

  @Test
  void isEdge() {
    act.add(a, b);
    assertTrue(act.isEdge(a, b));
  }

  @Test
  void isEdge_notFound() {
    assertFalse(act.isEdge(a, b));
  }

  @Test
  void getAll() {
    act.add(a, b);
    act.add(a, c);
    act.add(c, a);
    act.add(b, c);
    assertEquals(List.of(Map.entry(a, b), Map.entry(a, c), Map.entry(b, c), Map.entry(c, a)), act.getAll());
  }
}
