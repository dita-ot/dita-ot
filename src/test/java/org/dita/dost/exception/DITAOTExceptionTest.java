/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2011 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.exception;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.dita.dost.log.MessageBean;
import org.junit.Test;

public class DITAOTExceptionTest {

    @Test
    public void testDITAOTException() {
        new DITAOTException();
    }

    @Test
    public void testDITAOTExceptionString() {
        new DITAOTException((String) null);
        new DITAOTException("test");
    }

    @Test
    public void testDITAOTExceptionThrowable() {
        new DITAOTException((Throwable) null);
        new DITAOTException(new RuntimeException());
    }

    @Test
    public void testDITAOTExceptionStringThrowable() {
        new DITAOTException(null, null);
        new DITAOTException("test", new RuntimeException());
    }

    @Test
    public void testDITAOTExceptionMessageBeanThrowableString() {
        new DITAOTException(null, null, null);
        new DITAOTException(new MessageBean(null, null, null, null), new RuntimeException(), "test");
    }

    @Test
    public void testGetMessageBean() {
        final MessageBean m = new MessageBean(null, null, null, null);
        final DITAOTException e = new DITAOTException(m, null, "test");
        assertSame(m, e.getMessageBean());
    }

    @Test
    public void testAlreadyCaptured() {
        final DITAOTException e = new DITAOTException();
        assertEquals(e.alreadyCaptured(), false);
        e.setCaptured(true);
        assertEquals(e.alreadyCaptured(), true);
    }

    @Test
    public void testSetCaptured() {
        final DITAOTException e = new DITAOTException();
        e.setCaptured(false);
        assertEquals(e.alreadyCaptured(), false);
        e.setCaptured(true);
        assertEquals(e.alreadyCaptured(), true);
    }

}
