/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2011 All Rights Reserved.
 */
package org.dita.dost.log;

import static org.junit.Assert.*;

import org.junit.Test;

public class MessageBeanTest {

    @Test
    public void testMessageBean() {
        final MessageBean m = new MessageBean();
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
        assertNull(new MessageBean().getId());
    }

    @Test
    public void testSetId() {
        final MessageBean m = new MessageBean();
        m.setId("foo");
        assertEquals("foo", m.getId());
        m.setId(null);
        assertNull(m.getId());
    }

    @Test
    public void testGetReason() {
        assertEquals("baz", new MessageBean("foo", "bar", "baz", "qux").getReason());
        assertNull(new MessageBean().getReason());
    }

    @Test
    public void testSetReason() {
        final MessageBean m = new MessageBean();
        m.setReason("baz");
        assertEquals("baz", m.getReason());
        m.setReason(null);
        assertNull(m.getReason());
    }

    @Test
    public void testGetResponse() {
        assertEquals("qux", new MessageBean("foo", "bar", "baz", "qux").getResponse());
        assertNull(new MessageBean().getResponse());
    }

    @Test
    public void testSetResponse() {
        final MessageBean m = new MessageBean();
        m.setResponse("qux");
        assertEquals("qux", m.getResponse());
        m.setResponse(null);
        assertNull(m.getResponse());
    }

    @Test
    public void testGetType() {
        assertEquals("bar", new MessageBean("foo", "bar", "baz", "qux").getType());
        assertNull(new MessageBean().getType());
    }

    @Test
    public void testSetType() {
        final MessageBean m = new MessageBean();
        m.setType("bar");
        assertEquals("bar", m.getType());
        m.setType(null);
        assertNull(m.getType());
    }

    @Test
    public void testToString() {
        assertEquals("[null][null] null", new MessageBean().toString());
        assertEquals("[foo][bar] baz qux", new MessageBean("foo", "bar", "baz", "qux").toString());
        assertEquals("[foo][bar] baz", new MessageBean("foo", "bar", "baz", null).toString());
    }

}
