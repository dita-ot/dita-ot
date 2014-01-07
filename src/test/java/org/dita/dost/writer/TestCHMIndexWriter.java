/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2010 All Rights Reserved.
 */
package org.dita.dost.writer;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import nu.validator.htmlparser.dom.HtmlDocumentBuilder;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.dita.dost.TestUtils;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.index.IndexTerm;
import org.dita.dost.module.Content;
import org.dita.dost.module.ContentImpl;

public class TestCHMIndexWriter {

    private static File tempDir;
    private static final File resourceDir = TestUtils.getResourceDir(TestCHMIndexWriter.class);
    private static final File expDir = new File(resourceDir, "exp");

    @BeforeClass
    public static void setUp() throws IOException, DITAOTException {
        tempDir = TestUtils.createTempDir(TestCHMIndexWriter.class);
        TestUtils.resetXMLUnit();
        final DocumentBuilderFactory factory = new HTMLDocumentBuilderFactory();
        XMLUnit.setControlDocumentBuilderFactory(factory);
        XMLUnit.setTestDocumentBuilderFactory(factory);
        XMLUnit.setControlEntityResolver(null);
        XMLUnit.setTestEntityResolver(null);
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreComments(true);
    }

    @Test
    public void testWrite() throws DITAOTException, SAXException, IOException {
        final Content content = new ContentImpl();
        final IndexTerm indexterm1 = new IndexTerm();
        indexterm1.setTermName("name1");
        indexterm1.setTermKey("indexkey1");
        final IndexTerm indexterm2 = new IndexTerm();
        indexterm2.setTermName("name2");
        indexterm2.setTermKey("indexkey2");
        indexterm1.addSubTerm(indexterm2);
        final Collection<IndexTerm> collection = new ArrayList<IndexTerm>();
        collection.add(indexterm1);
        content.setCollection(collection);

        final CHMIndexWriter indexWriter = new CHMIndexWriter();
        indexWriter.setContent(content);
        final File outFile = new File(tempDir, "index.hhk");
        indexWriter.write(outFile.getAbsolutePath());

        assertXMLEqual(new InputSource(new File(expDir, "index.hhk").toURI().toString()),
                new InputSource(outFile.toURI().toString()));
    }

    @AfterClass
    public static void tearDown() throws IOException {
        TestUtils.forceDelete(tempDir);
    }

    private static class HTMLDocumentBuilderFactory extends DocumentBuilderFactory {

        @Override
        public Object getAttribute(final String arg0) throws IllegalArgumentException {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean getFeature(final String arg0) throws ParserConfigurationException {
            throw new UnsupportedOperationException();
        }

        @Override
        public DocumentBuilder newDocumentBuilder() throws ParserConfigurationException {
            return new HtmlDocumentBuilder();
        }

        @Override
        public void setAttribute(final String arg0, final Object arg1) throws IllegalArgumentException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setFeature(final String arg0, final boolean arg1) throws ParserConfigurationException {
            throw new UnsupportedOperationException();
        }

    }

}
