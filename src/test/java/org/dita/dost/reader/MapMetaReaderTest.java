/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2011 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.reader;

import static org.dita.dost.TestUtils.assertXMLEqual;
import java.io.File;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.dita.dost.util.Job;
import org.dita.dost.util.XMLUtils;
import org.xml.sax.InputSource;

import org.apache.xml.resolver.tools.ResolvingXMLReader;

import org.xml.sax.XMLReader;

import org.apache.xml.resolver.tools.CatalogResolver;

import org.xml.sax.SAXException;

import org.dita.dost.TestUtils;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.util.CatalogUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


public class MapMetaReaderTest {

    private static final File resourceDir = TestUtils.getResourceDir(MapMetaReaderTest.class);
    private static final File srcDir = new File(resourceDir, "src");
    private static final File expDir = new File(resourceDir, "exp");
    private static File tempDir;
    private static MapMetaReader reader;
    //private static final File baseDir = new File(resourceDir, "DITA-OT1.5");

    @BeforeClass
    public static void setUp() throws Exception{
        CatalogUtils.setDitaDir(new File("src" + File.separator + "main").getAbsoluteFile());
        tempDir = TestUtils.createTempDir(MapMetaReaderTest.class);
        for (final File f: srcDir.listFiles()) {
            TestUtils.normalize(f, new File(tempDir, f.getName()));
        }

        reader = new MapMetaReader();
        reader.setLogger(new TestUtils.TestLogger());
        reader.setJob(new Job(tempDir));

        final File mapFile = new File(tempDir, "test.ditamap");
        reader.read(mapFile.getAbsoluteFile());
    }

    @Test
    public void testRead() throws DITAOTException, SAXException, IOException, ParserConfigurationException{
        final DocumentBuilder db = XMLUtils.getDocumentBuilder();
        db.setEntityResolver(CatalogUtils.getCatalogResolver());

        assertXMLEqual(db.parse(new File(expDir, "test.ditamap")),
                db.parse(new File(tempDir, "test.ditamap")));
    }

    @AfterClass
    public static void tearDown() throws IOException {
        TestUtils.forceDelete(tempDir);
    }

}
