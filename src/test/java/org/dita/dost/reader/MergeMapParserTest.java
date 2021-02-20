/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2011 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.reader;

import org.dita.dost.TestUtils;
import org.dita.dost.store.CacheStore;
import org.dita.dost.store.StreamStore;
import org.dita.dost.util.Job;
import org.dita.dost.util.XMLUtils;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import static org.dita.dost.TestUtils.assertXMLEqual;
import static org.dita.dost.util.Constants.UTF8;

public class MergeMapParserTest {

    final File resourceDir = TestUtils.getResourceDir(MergeMapParserTest.class);
    private final File srcDir = new File(resourceDir, "src");
    private final File expDir = new File(resourceDir, "exp");

    @Before
    public void setUp() {
    }

    @Test
    public void testReadStringString() throws SAXException, IOException {
        final MergeMapParser parser = new MergeMapParser();
        parser.setLogger(new TestUtils.TestLogger());
        parser.setJob(new Job(srcDir, new StreamStore(srcDir, new XMLUtils())));
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
        parser.setJob(new Job(srcDir, new StreamStore(srcDir, new XMLUtils())));
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        output.write("<wrapper>".getBytes(UTF8));
        parser.setOutputStream(output);
        parser.read(new File(srcDir, "space in map name.ditamap").getAbsoluteFile(), srcDir.getAbsoluteFile());
        output.write("</wrapper>".getBytes(UTF8));
        assertXMLEqual(new InputSource(new File(expDir, "merged.xml").toURI().toString()),
                new InputSource(new ByteArrayInputStream(output.toByteArray())));
    }

    @Test
    public void testComposite() throws SAXException, IOException {
        final MergeMapParser parser = new MergeMapParser();
        parser.setLogger(new TestUtils.TestLogger());
        parser.setJob(new Job(srcDir, new StreamStore(srcDir, new XMLUtils())));
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        output.write("<wrapper>".getBytes(UTF8));
        parser.setOutputStream(output);
        parser.read(new File(srcDir, "testcomposite.ditamap").getAbsoluteFile(), srcDir.getAbsoluteFile());
        output.write("</wrapper>".getBytes(UTF8));

        assertXMLEqual(new InputSource(new File(expDir, "mergedwithditasub.xml").toURI().toString()),
                new InputSource(new ByteArrayInputStream(output.toByteArray())));
    }

    @Test
    public void testSubtopic() throws SAXException, IOException {
        final MergeMapParser parser = new MergeMapParser();
        parser.setLogger(new TestUtils.TestLogger());
        parser.setJob(new Job(srcDir, new StreamStore(srcDir, new XMLUtils())));
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        output.write("<wrapper>".getBytes(UTF8));
        parser.setOutputStream(output);
        parser.read(new File(srcDir, "testsubtopic.ditamap").getAbsoluteFile(), srcDir.getAbsoluteFile());
        output.write("</wrapper>".getBytes(UTF8));

        assertXMLEqual(new InputSource(new File(expDir, "mergedsub.xml").toURI().toString()),
                new InputSource(new ByteArrayInputStream(output.toByteArray())));
    }

    @Test
    public void testCacheStore() throws SAXException, IOException, ParserConfigurationException {
        final MergeMapParser parser = new MergeMapParser();
        parser.setLogger(new TestUtils.TestLogger());
        final File tmpDir = new File(resourceDir, "tmpRandom");
        final CacheStore store = new CacheStore(tmpDir, new XMLUtils());
        final Job job = new Job(tmpDir, store);
        final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        for (String child : new String[]{"test.ditamap", "test.xml", "test2.xml"}) {
            final Document doc = builder.parse(new File(srcDir, child));
            store.writeDocument(doc, new File(tmpDir, child).toURI());
        }
        parser.setJob(job);
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        output.write("<wrapper>".getBytes(UTF8));
        parser.setOutputStream(output);
        parser.read(new File(tmpDir, "test.ditamap").getAbsoluteFile(), srcDir.getAbsoluteFile());
        output.write("</wrapper>".getBytes(UTF8));
        assertXMLEqual(new InputSource(new File(expDir, "merged.xml").toURI().toString()),
                new InputSource(new ByteArrayInputStream(output.toByteArray())));
    }

}
