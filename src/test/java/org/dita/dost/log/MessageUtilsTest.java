/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2011 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.log;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.dita.dost.TestUtils;

import org.junit.Test;

public class MessageUtilsTest {

    private static final File resourceDir = TestUtils.getResourceDir(MessageUtilsTest.class);

    @Test
    public void testGetMessageString() {
        final MessageBean exp = new MessageBean("XXX123F", "FATAL", "Fatal reason.","Fatal response.");
        assertEquals(exp.toString(), MessageUtils.getMessage("XXX123F").toString());
    }

    @Test
    public void testGetMessageStringProperties() {
        final MessageBean exp = new MessageBean("XXX234E", "ERROR", "Error foo reason bar baz.", "Error foo response bar baz.");
        assertEquals(exp.toString(), MessageUtils.getMessage("XXX234E", "foo", "bar baz").toString());
    }

    @Test
    public void testGetMessageStringMissing() {
        final MessageBean exp = new MessageBean("XXX234E", "ERROR", "Error foo reason {1}.", "Error foo response {1}.");
        assertEquals(exp.toString(), MessageUtils.getMessage("XXX234E", "foo").toString());
    }

    @Test
    public void testGetMessageStringExtra() {
        final MessageBean exp = new MessageBean("XXX234E", "ERROR", "Error foo reason bar baz.", "Error foo response bar baz.");
        assertEquals(exp.toString(), MessageUtils.getMessage("XXX234E", "foo", "bar baz", "qux").toString());
    }
    
}
