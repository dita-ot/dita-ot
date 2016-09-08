/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2011 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.log;

import static org.junit.Assert.*;

import org.junit.Test;

public class MessageBeanTest {

    @Test
    public void testMessageBean() {
        final MessageBean m = new MessageBean(null, null, null, null);
        assertNotNull(m);
        assertNull(m.getId());
        assertNull(m.getReason());
        assertNull(m.getResponse());
        assertNull(m.getType());
    }

    @Test
    public void testMessageBeanStringStringStringString() {
        final MessageBean m = new MessageBean("foo", "bar", "baz", "qux");
        assertNotNull(m);
        assertEquals("foo", m.getId());
        assertEquals("bar", m.getType());
        assertEquals("baz", m.getReason());
        assertEquals("qux", m.getResponse());
    }

    @Test
    public void testMessageBeanMessageBean() {
        final MessageBean o = new MessageBean("foo", "bar", "baz", "qux");
        final MessageBean m = new MessageBean(o);
        assertEquals("foo", m.getId());
        assertEquals("bar", m.getType());
        assertEquals("baz", m.getReason());
        assertEquals("qux", m.getResponse());
    }

    @Test
    public void testGetId() {
        assertEquals("foo", new MessageBean("foo", "bar", "baz", "qux").getId());
        assertNull(new MessageBean(null, null, null, null).getId());
    }

    @Test
    public void testGetReason() {
        assertEquals("baz", new MessageBean("foo", "bar", "baz", "qux").getReason());
        assertNull(new MessageBean(null, null, null, null).getReason());
    }

    @Test
    public void testGetResponse() {
        assertEquals("qux", new MessageBean("foo", "bar", "baz", "qux").getResponse());
        assertNull(new MessageBean(null, null, null, null).getResponse());
    }

    @Test
    public void testGetType() {
        assertEquals("bar", new MessageBean("foo", "bar", "baz", "qux").getType());
        assertNull(new MessageBean(null, null, null, null).getType());
    }


    @Test
    public void testToString() {
        assertEquals("[null][null] null", new MessageBean(null, null, null, null).toString());
        assertEquals("[foo][bar] baz qux", new MessageBean("foo", "bar", "baz", "qux").toString());
        assertEquals("[foo][bar] baz", new MessageBean("foo", "bar", "baz", null).toString());
    }

}
