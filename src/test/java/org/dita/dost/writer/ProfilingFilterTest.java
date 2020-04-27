/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2012 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.writer;

import com.google.common.collect.ImmutableMap;
import org.dita.dost.TestUtils;
import org.dita.dost.util.FilterUtils;
import org.dita.dost.util.FilterUtils.Action;
import org.dita.dost.util.FilterUtils.FilterKey;
import org.dita.dost.util.XMLUtils;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXSource;
import java.io.InputStream;
import java.util.Map;

import static org.dita.dost.TestUtils.assertXMLEqual;
import static org.dita.dost.util.FilterUtils.Action.EXCLUDE;
import static org.dita.dost.util.FilterUtils.Action.INCLUDE;

public class ProfilingFilterTest {

    private final DocumentBuilder documentBuilder;
    private final TransformerFactory transformerFactory;

    public ProfilingFilterTest() throws ParserConfigurationException {
        final DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        builderFactory.setNamespaceAware(true);
        builderFactory.setIgnoringComments(true);
        documentBuilder = builderFactory.newDocumentBuilder();
        transformerFactory = TransformerFactory.newInstance();
    }

    @Test
    public void testNoFilter() throws Exception {
        test(new FilterUtils(false), "topic.dita", "topic.dita");
    }

    @Test
    public void testFilter() throws Exception {
        final Map<FilterKey, Action> filterMap = ImmutableMap.of(
                new FilterKey(QName.valueOf("platform"), null), INCLUDE,
                new FilterKey(QName.valueOf("default"), null), EXCLUDE,
                new FilterKey(QName.valueOf("audience"), null), EXCLUDE,
                new FilterKey(QName.valueOf("audience"), "novice"), INCLUDE,
                new FilterKey(QName.valueOf("platform"), "windows"), EXCLUDE
        );
        final FilterUtils filterUtils = new FilterUtils(false, filterMap, null, null);

        test(filterUtils, "topic.dita", "topic1.dita");
    }

    @Test
    public void testNoFilter_dita2() throws Exception {
        test(new FilterUtils(false), "topic_2.dita", "topic_2.dita");
    }

    @Test
    public void testFilter_dita2() throws Exception {
        final Map<FilterKey, Action> filterMap = ImmutableMap.of(
                new FilterKey(QName.valueOf("platform"), null), INCLUDE,
                new FilterKey(QName.valueOf("default"), null), EXCLUDE,
                new FilterKey(QName.valueOf("audience"), null), EXCLUDE,
                new FilterKey(QName.valueOf("audience"), "novice"), INCLUDE,
                new FilterKey(QName.valueOf("platform"), "windows"), EXCLUDE
        );
        final FilterUtils filterUtils = new FilterUtils(false, filterMap, null, null);

        test(filterUtils, "topic_2.dita", "topic1_2.dita");
    }


    @Test
    public void testFilter_xhtml() throws Exception {
        test(new FilterUtils(false), "map.ditamap", "map_xhtml.ditamap");
    }

    @Test
    public void testFilter_pdf() throws Exception {
        test(new FilterUtils(true), "map.ditamap", "map_pdf.ditamap");
    }

    private void test(final FilterUtils filterUtils, final String srcFile, final String expFile) throws Exception {
        filterUtils.setLogger(new TestUtils.TestLogger());

        final ProfilingFilter f = new ProfilingFilter();
        f.setParent(XMLUtils.getXMLReader());
        f.setFilterUtils(filterUtils);
        f.setLogger(new TestUtils.TestLogger());

        final Document act = documentBuilder.newDocument();
        try (InputStream src = getClass().getClassLoader().getResourceAsStream("ProfilingFilterTest/src/" + srcFile)) {
            final SAXSource s = new SAXSource(f, new InputSource(src));
            final DOMResult d = new DOMResult(act);
            transformerFactory.newTransformer().transform(s, d);
        }

        try (final InputStream exp = getClass().getClassLoader().getResourceAsStream("ProfilingFilterTest/exp/" + expFile)) {
            assertXMLEqual(documentBuilder.parse(exp), act);
        }
    }
}
