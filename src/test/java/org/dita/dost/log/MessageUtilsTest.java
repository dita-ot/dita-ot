/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */
package org.dita.dost.log;

import static org.junit.Assert.*;

import java.io.File;
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
        MessageUtils.loadMessages(f.getAbsolutePath());
    }

    @Test
    public void testLoadDefaultMessages() {
        MessageUtils.loadDefaultMessages();
    }

    @Test
    public void testLoadMessages() {
        final File f = new File(resourceDir, "messages.xml");
        MessageUtils.loadMessages(f.getAbsolutePath());
    }

    @Test
    public void testGetMessageString() {
        final MessageBean exp = new MessageBean();
        exp.setId("XXX123F");
        exp.setType("FATAL");
        exp.setReason("Fatal reason.");
        exp.setResponse("Fatal response.");
        assertEquals(exp.toString(), MessageUtils.getMessage("XXX123F").toString());
    }

    @Test
    public void testGetMessageStringProperties() {
        final Properties props = new Properties();
        props.put("%1", "foo");
        props.put("%2", "bar baz");
        final MessageBean exp = new MessageBean();
        exp.setId("XXX234E");
        exp.setType("ERROR");
        exp.setReason("Error foo reason bar baz.");
        exp.setResponse("Error foo response bar baz.");
        assertEquals(exp.toString(), MessageUtils.getMessage("XXX234E", props).toString());
    }

    @Test
    public void testGetMessageStringMissing() {
        final Properties props = new Properties();
        props.put("%1", "foo");
        final MessageBean exp = new MessageBean();
        exp.setId("XXX234E");
        exp.setType("ERROR");
        exp.setReason("Error foo reason %2.");
        exp.setResponse("Error foo response %2.");
        assertEquals(exp.toString(), MessageUtils.getMessage("XXX234E", props).toString());
    }

    @Test
    public void testGetMessageStringExtra() {
        final Properties props = new Properties();
        props.put("%1", "foo");
        props.put("%2", "bar baz");
        props.put("%3", "qux");
        final MessageBean exp = new MessageBean();
        exp.setId("XXX234E");
        exp.setType("ERROR");
        exp.setReason("Error foo reason bar baz.");
        exp.setResponse("Error foo response bar baz.");
        assertEquals(exp.toString(), MessageUtils.getMessage("XXX234E", props).toString());
    }

    @AfterClass
    public static void tearDown() throws Exception {
        MessageUtils.loadDefaultMessages();
    }
    
}
