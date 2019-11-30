/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2011 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.exception;

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
        new DITAOTException(null, (Throwable) null);
        new DITAOTException("test", new RuntimeException());
    }

    @Test
    public void testDITAOTExceptionMessageBeanThrowableString() {
        new DITAOTException(null, null, null);
        new DITAOTException(new MessageBean(null, (MessageBean.Type) null, null, null), new RuntimeException(), "test");
    }

}
