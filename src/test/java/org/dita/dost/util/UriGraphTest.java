/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2022 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.util;

import org.junit.Before;
import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UriGraphTest {
    private final URI a = URI.create("file:///Volume/tmp/a.dita");
    private final URI b = URI.create("file:///Volume/tmp/b.dita");

    private UriGraph act;

    @Before
    public void setUp() {
        act = new UriGraph(16);
    }

    @Test
    public void add() {
        act.add(a, b);
        assertTrue(act.isEdge(a, b));
    }

    @Test
    public void remove() {
        act.add(a, b);
        act.remove(a, b);
        assertFalse(act.isEdge(a, b));
    }

    @Test
    public void isEdge_notFound() {
        assertFalse(act.isEdge(a, b));
    }
}