/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2011 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.index;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class IndexTermTargetTest {

    private final IndexTermTarget simple = new IndexTermTarget();

    @Before
    public void setUp() throws Exception {
        simple.setTargetName("name");
        simple.setTargetURI("base");
    }

    @Test
    public void testHashCode() {
        assertEquals(simple.hashCode(), simple.hashCode());
        final IndexTermTarget s = new IndexTermTarget();
        s.setTargetName("name");
        s.setTargetURI("base");
        assertEquals(simple.hashCode(), s.hashCode());
    }

    @Test
    public void testGetTargetName() {
        final IndexTermTarget i = new IndexTermTarget();
        assertNull(i.getTargetName());
        i.setTargetName("");
        assertEquals("", i.getTargetName());
        i.setTargetName("name");
        assertEquals("name", i.getTargetName());
    }

    @Test
    public void testSetTargetName() {
        new IndexTermTarget().setTargetName(null);
        new IndexTermTarget().setTargetName("");
    }

    @Test
    public void testGetTargetURI() {
        final IndexTermTarget i = new IndexTermTarget();
        assertNull(i.getTargetURI());
        i.setTargetURI("");
        assertEquals("", i.getTargetURI());
        i.setTargetURI("name");
        assertEquals("name", i.getTargetURI());
    }

    @Test
    public void testSetTargetURI() {
        new IndexTermTarget().setTargetURI(null);
        new IndexTermTarget().setTargetURI("");
    }

    @Test
    public void testEqualsObject() {
        assertTrue(simple.equals(simple));
        final IndexTermTarget s = new IndexTermTarget();
        s.setTargetName("name");
        s.setTargetURI("base");
        assertTrue(simple.equals(s));
        assertFalse(simple.equals(null));
        assertFalse(simple.equals(""));
    }

    @Test @Ignore
    public void testToString() {
        fail("Not yet implemented");
    }

}
