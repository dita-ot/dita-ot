/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */
package org.dita.dost.reader;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.dita.dost.util.Constants.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import org.dita.dost.TestUtils;
import org.dita.dost.util.Job;
import org.junit.Test;

public class MergeMapParserTest {

    final File resourceDir = TestUtils.getResourceDir(MergeMapParserTest.class);
    private final File srcDir = new File(resourceDir, "src");
    private final File expDir = new File(resourceDir, "exp");

    @Test
    public void testReadStringString() throws SAXException, IOException {
        final MergeMapParser parser = new MergeMapParser();
        parser.setLogger(new TestUtils.TestLogger());
        parser.setJob(new Job(srcDir));
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        output.write("<wrapper>".getBytes(UTF8));
        parser.setOutputStream(output);
        parser.read(new File(srcDir, "test.ditamap").getAbsoluteFile(), srcDir.getAbsoluteFile());
        output.write("</wrapper>".getBytes(UTF8));
        assertXMLEqual(new InputSource(new File(expDir, "merged.xml").toURI().toString()),
                new InputSource(new ByteArrayInputStream(output.toByteArray())));
    }
    
    @Test
    public void testReadSpace() throws SAXException, IOException {
        final MergeMapParser parser = new MergeMapParser();
        parser.setLogger(new TestUtils.TestLogger());
        parser.setJob(new Job(srcDir));
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        output.write("<wrapper>".getBytes(UTF8));
        parser.setOutputStream(output);
        parser.read(new File(srcDir, "space in map name.ditamap").getAbsoluteFile(), srcDir.getAbsoluteFile());
        output.write("</wrapper>".getBytes(UTF8));
        assertXMLEqual(new InputSource(new File(expDir, "merged.xml").toURI().toString()),
                new InputSource(new ByteArrayInputStream(output.toByteArray())));
    }
    
}
