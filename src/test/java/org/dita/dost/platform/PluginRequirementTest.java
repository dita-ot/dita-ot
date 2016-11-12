/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2011 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.platform;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

public class PluginRequirementTest {

    @Test
    public void testPluginRequirement() {
        assertNotNull(new PluginRequirement());
    }

    @Test
    public void testAddPlugins() {
        final PluginRequirement pr = new PluginRequirement();
        pr.addPlugins("foo | bar | baz");
        try {
            pr.addPlugins(null);
            fail();
        } catch (final NullPointerException e) {}
    }

    @Test
    public void testSetRequired() {
        final PluginRequirement pr = new PluginRequirement();
        pr.setRequired(true);
        pr.setRequired(false);
    }

    @Test
    public void testGetPlugins() {
        final PluginRequirement pr = new PluginRequirement();
        pr.addPlugins("foo | bar | baz");

        final List<String> act = new ArrayList<String>();
        for (final Iterator<String> i = pr.getPlugins(); i.hasNext();) {
            act.add(i.next());
        }

        assertArrayEquals(new String[] {"foo ", " bar ", " baz"},
                act.toArray(new String[0]));
    }

    @Test
    public void testGetRequired() {
        final PluginRequirement pr = new PluginRequirement();
        assertTrue(pr.getRequired());
        pr.setRequired(true);
        assertTrue(pr.getRequired());
        pr.setRequired(false);
        assertFalse(pr.getRequired());
    }

    @Test
    public void testToString() {
        final PluginRequirement pr = new PluginRequirement();
        pr.addPlugins("foo | bar | baz");
        assertEquals("[foo ,  bar ,  baz]", pr.toString());
    }

}
