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
    assertTrue(act.contains(a, b));
  }

  @Test
  void add_same() {
    act.add(a, a);
    assertTrue(act.contains(a, a));
  }

  @Test
  void add_duplicate() {
    act.add(a, b);
    act.add(a, b);
    assertTrue(act.contains(a, b));
  }

  @Test
  void remove() {
    act.add(a, b);
    act.remove(a, b);
    assertFalse(act.contains(a, b));
  }

  @Test
  void remove_notFound() {
    act.remove(a, b);
    assertFalse(act.contains(a, b));
  }

  @Test
  void contains() {
    act.add(a, b);
    assertTrue(act.contains(a, b));
  }

  @Test
  void contains_notFound() {
    assertFalse(act.contains(a, b));
  }

  @Test
  void getAll() {
    act.add(a, b);
    act.add(a, c);
    act.add(c, a);
    act.add(b, c);
    assertEquals(Map.of(a, List.of(b, c), b, List.of(c), c, List.of(a)), act.getAll());
  }

  @Test
  void get() {
    act.add(a, b);
    act.add(a, c);
    act.add(c, a);
    act.add(b, c);
    assertEquals(List.of(b, c), act.get(a));
    assertEquals(List.of(a), act.get(c));
    assertEquals(List.of(c), act.get(b));
  }
}
