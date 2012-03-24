/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2011 All Rights Reserved.
 */
package org.dita.dost.reader;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import java.io.File;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.InputSource;

import org.apache.xml.resolver.tools.ResolvingXMLReader;

import org.xml.sax.XMLReader;

import org.apache.xml.resolver.tools.CatalogResolver;

import org.xml.sax.SAXException;

import org.custommonkey.xmlunit.XMLUnit;
import org.dita.dost.TestUtils;
import org.dita.dost.exception.DITAOTException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


public class MapMetaReaderTest {

    private static final File resourceDir = new File(TestUtils.testStub, MapMetaReaderTest.class.getSimpleName());
    private static final File srcDir = new File(resourceDir, "src");
    private static final File expDir = new File(resourceDir, "exp");
    private static File tempDir;
    private static AbstractReader reader;
    //private static final File baseDir = new File(resourceDir, "DITA-OT1.5");

    @BeforeClass
    public static void setUp() throws Exception{
        tempDir = TestUtils.createTempDir(MapMetaReaderTest.class);

        final TransformerFactory tf = TransformerFactory.newInstance();
        final XMLReader parser = new ResolvingXMLReader();
        parser.setEntityResolver(new CatalogResolver());
        for (final File f: srcDir.listFiles()) {
            tf.newTransformer().transform(new SAXSource(parser, new InputSource(f.toURI().toString())),
                    new StreamResult(new File(tempDir, f.getName())));
        }

        reader = new MapMetaReader();
        reader.setLogger(new TestUtils.TestLogger());

        final File mapFile = new File(tempDir, "test.ditamap");
        reader.read(mapFile.getAbsolutePath());
    }

    @Test
    public void testRead() throws DITAOTException, SAXException, IOException, ParserConfigurationException{
        final DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        db.setEntityResolver(new CatalogResolver());

        XMLUnit.setNormalizeWhitespace(true);
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreDiffBetweenTextAndCDATA(true);
        XMLUnit.setIgnoreComments(true);

        assertXMLEqual(db.parse(new File(expDir, "test.ditamap")),
                db.parse(new File(tempDir, "test.ditamap.temp")));
    }

    @AfterClass
    public static void tearDown() throws IOException {
        TestUtils.forceDelete(tempDir);
    }

}
