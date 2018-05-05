/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2018 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.writer;

import org.dita.dost.TestUtils;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import java.io.File;

import static org.dita.dost.TestUtils.assertXMLEqual;

public class NormalizeCodeblockTest {

    private static final File resourceDir = TestUtils.getResourceDir(NormalizeCodeblockTest.class);
    private static final File srcDir = new File(resourceDir, "src");
    private static final File expDir = new File(resourceDir, "exp");

    private final DocumentBuilder documentBuilder;
    private final SAXTransformerFactory transformerFactory;

    public NormalizeCodeblockTest() throws ParserConfigurationException {
        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        documentBuilder = documentBuilderFactory.newDocumentBuilder();
        transformerFactory = ((SAXTransformerFactory) TransformerFactory.newInstance());
    }

    @Test
    public void testOnlyText() throws Exception {
        final Document act = filter(new File(srcDir, "onlyText.xml"));
        final Document exp = documentBuilder.parse(new File(expDir, "onlyText.xml"));
        assertXMLEqual(exp, act);
    }

    @Test
    public void testNestedElement() throws Exception {
        final Document act = filter(new File(srcDir, "nestedElement.xml"));
        final Document exp = documentBuilder.parse(new File(expDir, "nestedElement.xml"));
        assertXMLEqual(exp, act);
    }

    private Document filter(final File file) throws Exception {
        final DOMResult result = new DOMResult(documentBuilder.newDocument());

        final TransformerHandler serializer = transformerFactory.newTransformerHandler();
        serializer.setResult(result);

        final NormalizeCodeblock filter = new NormalizeCodeblock();
        filter.setContentHandler(serializer);

        final XMLReader parser = XMLReaderFactory.createXMLReader();
        parser.setContentHandler(filter);
        parser.parse(file.toURI().toString());

        return (Document) result.getNode();
    }

}