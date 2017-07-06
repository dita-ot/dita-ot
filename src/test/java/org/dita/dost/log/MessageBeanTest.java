/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2011 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.log;

import static org.dita.dost.log.MessageBean.Type.*;
import static org.junit.Assert.*;

import org.junit.Test;

public class MessageBeanTest {

    @Test
    public void testMessageBean() {
        final MessageBean m = new MessageBean(null, (MessageBean.Type) null, null, null);
        assertNotNull(m);
        assertNull(m.getId());
        assertNull(m.getReason());
        assertNull(m.getResponse());
        assertNull(m.getType());
    }

    @Test
    public void testMessageBeanStringStringStringString() {
        final MessageBean m = new MessageBean("foo", INFO, "baz", "qux");
        assertNotNull(m);
        assertEquals("foo", m.getId());
        assertEquals(INFO.toString(), m.getType());
        assertEquals("baz", m.getReason());
        assertEquals("qux", m.getResponse());
    }

    @Test
    public void testMessageBeanMessageBean() {
        final MessageBean o = new MessageBean("foo", INFO, "baz", "qux");
        final MessageBean m = new MessageBean(o);
        assertEquals("foo", m.getId());
        assertEquals(INFO.toString(), m.getType());
        assertEquals("baz", m.getReason());
        assertEquals("qux", m.getResponse());
    }

    @Test
    public void testGetId() {
        assertEquals("foo", new MessageBean("foo", INFO, "baz", "qux").getId());
        assertNull(new MessageBean(null, (MessageBean.Type) null, null, null).getId());
    }

    @Test
    public void testGetReason() {
        assertEquals("baz", new MessageBean("foo", INFO, "baz", "qux").getReason());
        assertNull(new MessageBean(null, (MessageBean.Type) null, null, null).getReason());
    }

    @Test
    public void testGetResponse() {
        assertEquals("qux", new MessageBean("foo", INFO, "baz", "qux").getResponse());
        assertNull(new MessageBean(null, (MessageBean.Type) null, null, null).getResponse());
    }

    @Test
    public void testGetType() {
        assertEquals(INFO.toString(), new MessageBean("foo", INFO, "baz", "qux").getType());
        assertNull(new MessageBean(null, (MessageBean.Type) null, null, null).getType());
    }


    @Test
    public void testToString() {
        assertEquals("[null][null] null", new MessageBean(null, (MessageBean.Type) null, null, null).toString());
        assertEquals("[foo][INFO] baz qux", new MessageBean("foo", INFO, "baz", "qux").toString());
        assertEquals("[foo][INFO] baz", new MessageBean("foo", INFO, "baz", null).toString());
    }

}
