/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2022 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.util;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;

import java.util.*;
import org.junit.Test;

public class LangUtilsTest {

  @Test
  public void pair() {
    final Map.Entry<String, String> act = LangUtils.pair("left", "right");
    assertEquals("left", act.getKey());
    assertEquals("right", act.getValue());
  }

  @Test
  public void pair_null() {
    final Map.Entry<String, String> act = LangUtils.pair(null, null);
    assertEquals(null, act.getKey());
    assertEquals(null, act.getValue());
  }

  @Test
  public void zipWithIndex() {
    final List<String> src = asList("first", "second");
    final List<Map.Entry<String, Integer>> act = LangUtils.zipWithIndex(src).toList();
    assertEquals("first", act.get(0).getKey());
    assertEquals(0, act.get(0).getValue().intValue());
    assertEquals("second", act.get(1).getKey());
    assertEquals(1, act.get(1).getValue().intValue());
  }

  @Test
  public void zipWithIndex_null() {
    final List<String> src = new ArrayList<>();
    src.add(null);
    final List<Map.Entry<String, Integer>> act = LangUtils.zipWithIndex(src).toList();
    assertEquals(null, act.get(0).getKey());
    assertEquals(0, act.get(0).getValue().intValue());
  }

  @Test
  public void zipWithIndex_empty() {
    final List<String> src = Collections.emptyList();
    final List<Map.Entry<String, Integer>> act = LangUtils.zipWithIndex(src).toList();
    assertTrue(act.isEmpty());
  }
}
