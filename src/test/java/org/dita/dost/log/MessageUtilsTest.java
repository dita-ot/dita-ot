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
import java.util.Properties;

import org.dita.dost.TestUtils;

import org.junit.Before;
import org.junit.AfterClass;
import org.junit.Test;

public class MessageUtilsTest {

    private static final File resourceDir = TestUtils.getResourceDir(MessageUtilsTest.class);

    @Before
    public void setUp() throws Exception {
        final File f = new File(resourceDir, "messages.xml");
        InputStream in = null;
		try {
		    in = new FileInputStream(new File(f.getAbsolutePath()));
		    MessageUtils.getInstance().loadMessages(in);
		} catch (final FileNotFoundException e) {
		    throw new RuntimeException("Failed to load messages configuration file: " + e.getMessage(), e);
		} catch (final Exception e) {
		    throw new RuntimeException("Failed to load messages configuration file: " + e.getMessage(), e);
		} finally {
		    if (in != null) {
		        try {
		            in.close();
		        } catch (final IOException e) {
		            // NOOP
		        }
		    }
		}
    }

    @Test
    public void testLoadMessages() {
        final File f = new File(resourceDir, "messages.xml");
        InputStream in = null;
		try {
		    in = new FileInputStream(new File(f.getAbsolutePath()));
		    MessageUtils.getInstance().loadMessages(in);
		} catch (final FileNotFoundException e) {
		    throw new RuntimeException("Failed to load messages configuration file: " + e.getMessage(), e);
		} catch (final Exception e) {
		    throw new RuntimeException("Failed to load messages configuration file: " + e.getMessage(), e);
		} finally {
		    if (in != null) {
		        try {
		            in.close();
		        } catch (final IOException e) {
		            // NOOP
		        }
		    }
		}
    }

    @Test
    public void testGetMessageString() {
        final MessageBean exp = new MessageBean("XXX123F", "FATAL", "Fatal reason.","Fatal response.");
        assertEquals(exp.toString(), MessageUtils.getInstance().getMessage("XXX123F").toString());
    }

    @Test
    public void testGetMessageStringProperties() {
        final MessageBean exp = new MessageBean("XXX234E", "ERROR", "Error foo reason bar baz.", "Error foo response bar baz.");
        assertEquals(exp.toString(), MessageUtils.getInstance().getMessage("XXX234E", "foo", "bar baz").toString());
    }

    @Test
    public void testGetMessageStringMissing() {
        final MessageBean exp = new MessageBean("XXX234E", "ERROR", "Error foo reason %2.", "Error foo response %2.");
        assertEquals(exp.toString(), MessageUtils.getInstance().getMessage("XXX234E", "foo").toString());
    }

    @Test
    public void testGetMessageStringExtra() {
        final MessageBean exp = new MessageBean("XXX234E", "ERROR", "Error foo reason bar baz.", "Error foo response bar baz.");
        assertEquals(exp.toString(), MessageUtils.getInstance().getMessage("XXX234E", "foo", "bar baz", "qux").toString());
    }

    @AfterClass
    public static void tearDown() throws Exception {
        MessageUtils.getInstance().loadDefaultMessages();
    }
    
}
