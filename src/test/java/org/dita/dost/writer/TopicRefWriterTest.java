/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2020 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.writer;

import org.dita.dost.TestUtils;
import org.dita.dost.store.StreamStore;
import org.dita.dost.util.Job;
import org.dita.dost.util.XMLUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXSource;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static java.net.URI.create;

@RunWith(Parameterized.class)
public class TopicRefWriterTest {

    private final String src;
    private final String exp;
    private final TransformerFactory transformerFactory;
    private final DocumentBuilder db;

    private TopicRefWriter reader;
    private XMLReader parser;

    @Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {
                        "image",
                        "<image class='- topic/image ' href='same.png'/>",
                        "<image class='- topic/image ' href='same.png'/>"
                },
                {
                        "object",
                        "<object class='- topic/object ' data='same.pdf'/>",
                        "<object class='- topic/object ' data='same.pdf'/>"
                },
                {
                        "xrefSame",
                        "<xref class='- topic/xref ' href='same.dita'/>",
                        "<xref class='- topic/xref ' href='same.dita'/>"
                },
                {
                        "xrefWithoutId",
                        "<xref class='- topic/xref ' href='source.dita'/>",
                        "<xref class='- topic/xref ' href='change.dita'/>"
                },
                {
                        "xrefWithId",
                        "<xref class='- topic/xref ' href='source.dita#from'/>",
                        "<xref class='- topic/xref ' href='change.dita#to'/>"
                },
                {
                        "xrefWithElementId",
                        "<xref class='- topic/xref ' href='source.dita#from/element'/>",
                        "<xref class='- topic/xref ' href='change.dita#to/element'/>"
                },
                {
                        "xrefWithFileChange",
                        "<xref class='- topic/xref ' href='source.dita#other'/>",
                        "<xref class='- topic/xref ' href='change.dita#other'/>"
                },
                {
                        "xrefWithFileChangeElementId",
                        "<xref class='- topic/xref ' href='source.dita#other/element'/>",
                        "<xref class='- topic/xref ' href='change.dita#other/element'/>"
                },
                {
                        "xrefUnmapped",
                        "<xref class='- topic/xref ' href='unmapped.dita'/>",
                        "<xref class='- topic/xref ' href='unmapped.dita'/>"
                },
                {
                        "xrefUnmappedWithTopicId",
                        "<xref class='- topic/xref ' href='unmapped.dita#topic'/>",
                        "<xref class='- topic/xref ' href='unmapped.dita#topic'/>"
                },
                {
                        "xrefUnmappedWithElementId",
                        "<xref class='- topic/xref ' href='unmapped.dita#topic/element'/>",
                        "<xref class='- topic/xref ' href='unmapped.dita#topic/element'/>"
                }
        });
    }

    public TopicRefWriterTest(final String name, final String src, final String exp) throws ParserConfigurationException {
        this.src = src;
        this.exp = exp;
        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        db = documentBuilderFactory.newDocumentBuilder();
        transformerFactory = TransformerFactory.newInstance();
    }

    @Before
    public void setUp() throws Exception {
        final File tempDir = new File(URI.create("file:/tmp"));

        reader = new TopicRefWriter();
        reader.setLogger(new TestUtils.TestLogger());
        reader.setJob(new Job(tempDir, new StreamStore(tempDir, new XMLUtils())));
        reader.setCurrentFile(create("file:/tmp/dir/bar.dita"));

        reader.setup(map(
                "file:/tmp/dir/same.dita", "file:/tmp/dir/same.dita"
        ));
        reader.setChangeTable(map(
                "file:/tmp/dir/same.dita", "file:/tmp/dir/same.dita",
                "file:/tmp/dir/source.dita", "file:/tmp/dir/change.dita",
                "file:/tmp/dir/source.dita#from", "file:/tmp/dir/change.dita#to"
        ));
        reader.setFixpath(null);

        parser = XMLUtils.getXMLReader();
        reader.setParent(parser);
    }

    private Map<URI, URI> map(String... arg) {
        final Map<URI, URI> res = new HashMap<>();
        for (int i = 0; i < arg.length; i++) {
            res.put(URI.create(arg[i]), URI.create(arg[++i]));
        }
        return res;
    }

    @Test
    public void test_image() {
        assertEquals(exp, run(src));
    }

    private void assertEquals(final String exp, final Document act) {
        try {
            try (Reader in = new StringReader(exp)) {
                final Diff d = DiffBuilder
                        .compare(db.parse(new InputSource(in)))
                        .withTest(act)
                        .ignoreWhitespace()
                        .normalizeWhitespace()
                        .build();
                if (d.hasDifferences()) {
                    throw new AssertionError(d.toString());
                }
            }
        } catch (IOException | SAXException e) {
            throw new RuntimeException(e);
        }
    }

    private Document run(final String content) {
        try {
            final Document doc = db.newDocument();
            final InputSource inputSource = new InputSource(new StringReader(content));
            transformerFactory.newTransformer().transform(
                    new SAXSource(reader, inputSource),
                    new DOMResult(doc)
            );
            return doc;
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }
}
