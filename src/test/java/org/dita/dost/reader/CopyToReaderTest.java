/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2016 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.reader;

import org.dita.dost.TestUtils;
import org.dita.dost.util.Job;
import org.dita.dost.util.XMLUtils;
import org.junit.Test;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class CopyToReaderTest {

    final File resourceDir = TestUtils.getResourceDir(CopyToReaderTest.class);
    final File srcDir = new File(resourceDir, "src");

    @Test
    public void testGetCopytoMap() throws Exception {
        final CopyToReader reader = new CopyToReader();
        final URI inputFile = new File(srcDir, "test.ditamap").toURI();
        reader.setCurrentFile(inputFile);
        reader.setJob(new Job(srcDir));
        reader.setLogger(new TestUtils.TestLogger());

        final XMLReader parser = XMLUtils.getXMLReader();
        parser.setContentHandler(reader);
        reader.setContentHandler(new DefaultHandler());
        parser.parse(inputFile.toString());

        final Map<URI, URI> exp = new HashMap<>();
        exp.put(inputFile.resolve("direct.dita"), inputFile.resolve("topic.dita"));
        exp.put(inputFile.resolve("keyref.dita"), inputFile.resolve("topic.dita"));
        exp.put(inputFile.resolve("b.dita"), inputFile.resolve("a.dita"));
        exp.put(inputFile.resolve("skip-b.dita"), inputFile.resolve("skip-a.dita"));

        assertEquals(exp, reader.getCopyToMap());
    }
}
