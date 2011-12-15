/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2011 All Rights Reserved.
 */
package org.dita.dost.reader;

import static org.junit.Assert.*;
import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;

import java.io.File;
import java.io.StringReader;

import org.xml.sax.InputSource;
import org.dita.dost.TestUtils;
import org.dita.dost.util.MergeUtils;
import org.junit.Test;

public class MergeTopicParserTest {

    private final File resourceDir = new File("test-stub", MergeTopicParserTest.class.getSimpleName());
    private final File srcDir = new File(resourceDir, "src");
    private final File expDir = new File(resourceDir, "exp");

    @Test
    public void testGetContent() {
        final MergeTopicParser parser = new MergeTopicParser(new MergeUtils());
        parser.setLogger(new TestUtils.TestLogger());
        assertEquals(0, parser.getContent().getValue().toString().length());
    }

    @Test
    public void testReset() {
        final MergeTopicParser parser = new MergeTopicParser(new MergeUtils());
        parser.setLogger(new TestUtils.TestLogger());
        parser.parse("test.xml", srcDir.getAbsolutePath());
        final String exp = parser.getContent().getValue().toString();
        parser.reset();
        assertEquals(0, parser.getContent().getValue().toString().length());
        parser.parse("test.xml", srcDir.getAbsolutePath());
        final String act = parser.getContent().getValue().toString();
        assertEquals(exp, act);
    }

    @Test
    public void testParse() throws Exception {
        final MergeTopicParser parser = new MergeTopicParser(new MergeUtils());
        parser.setLogger(new TestUtils.TestLogger());
        parser.parse("test.xml", srcDir.getAbsolutePath());
        assertXMLEqual(new InputSource(new File(expDir, "test.xml").toURI().toString()),
                new InputSource(new StringReader(parser.getContent().getValue().toString())));
        parser.reset();
        parser.parse("test2.xml", srcDir.getAbsolutePath());
        assertXMLEqual(new InputSource(new File(expDir, "test2.xml").toURI().toString()),
                new InputSource(new StringReader(parser.getContent().getValue().toString())));
    }

}
