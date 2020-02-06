/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2012 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.writer;

import java.io.File;
import java.io.InputStream;
import java.util.Collections;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXSource;

import org.dita.dost.TestUtils;
import org.dita.dost.reader.DitaValReader;
import org.dita.dost.util.FilterUtils;
import org.dita.dost.util.Job;
import org.dita.dost.util.XMLUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import static org.dita.dost.TestUtils.assertXMLEqual;
import static org.dita.dost.TestUtils.createTempDir;

public class ProfilingFilterTest {

    @BeforeClass
    public static void setUp() {
    }

    @Test
    public void testNoFilter() throws Exception {
        test(new FilterUtils(false), "topic.dita", "topic.dita");
    }

    @Test
    public void testFilter() throws Exception {
        final DitaValReader filterReader = new DitaValReader();
        filterReader.read(new File(getClass().getClassLoader().getResource("ProfilingFilterTest/src/topic1.ditaval").toURI()).getAbsoluteFile());
        final FilterUtils filterUtils = new FilterUtils(false, filterReader.getFilterMap(), null, null);
        filterUtils.setLogger(new TestUtils.TestLogger());
        test(filterUtils, "topic.dita", "topic1.dita");

        test(new FilterUtils(false), "map.ditamap", "map_xhtml.ditamap");
        test(new FilterUtils(true), "map.ditamap", "map_pdf.ditamap");
    }

    private void test(final FilterUtils filterUtils, final String srcFile, final String expFile) throws Exception {
        final Transformer t = TransformerFactory.newInstance().newTransformer();
        final InputStream src = getClass().getClassLoader().getResourceAsStream("ProfilingFilterTest/src/" + srcFile);
        final ProfilingFilter f = new ProfilingFilter();
        f.setParent(XMLUtils.getXMLReader());
        filterUtils.setLogger(new TestUtils.TestLogger());
        f.setFilterUtils(filterUtils);
        f.setLogger(new TestUtils.TestLogger());
        filterUtils.setJob(new Job(createTempDir(KeyrefPaserTest.class)));
        final SAXSource s = new SAXSource(f, new InputSource(src));
        final DOMResult d = new DOMResult();
        t.transform(s, d);

        final DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        builderFactory.setNamespaceAware(true);
        builderFactory.setIgnoringComments(true);
        final DocumentBuilder db = builderFactory.newDocumentBuilder();
        try (final InputStream exp = getClass().getClassLoader().getResourceAsStream("ProfilingFilterTest/exp/" + expFile)) {
            assertXMLEqual(db.parse(exp), (Document) d.getNode());
        }
    }


}
