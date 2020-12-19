/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2013 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.writer;

import org.dita.dost.TestUtils;
import org.dita.dost.TestUtils.CachingLogger;
import org.dita.dost.store.StreamStore;
import org.dita.dost.util.Job;
import org.dita.dost.util.KeyDef;
import org.dita.dost.util.KeyScope;
import org.dita.dost.util.XMLUtils;
import org.dita.dost.util.XMLUtils.AttributesBuilder;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static javax.xml.XMLConstants.NULL_NS_URI;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.URLUtils.toURI;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ConkeyrefFilterTest {

    private Map<String, KeyDef> toMap(final KeyDef keyDef) {
        final Map<String, KeyDef> res = new HashMap<>();
        res.put(keyDef.keys, keyDef);
        return res;
    }

    private KeyScope createKeyScope(final Map<String, KeyDef> keyDefinition) {
        return new KeyScope(null, null, keyDefinition, Collections.emptyList());
    }

    private Job job;
    private URI srcDir;

    @Before
    public void setUp() throws IOException {
        final File tempDir = new File(System.getProperty("java.io.tmpdir"));
        job = new Job(tempDir, new StreamStore(tempDir, new XMLUtils()));
        srcDir = new File(".").getCanonicalFile().toURI();
    }

    @Test
    public void testKey() throws SAXException, IOException {
        final ConkeyrefFilter f = getConkeyrefFilter();
        f.setKeyDefinitions(createKeyScope(toMap(new KeyDef("foo", toURI("library.dita"), ATTR_SCOPE_VALUE_LOCAL, ATTR_FORMAT_VALUE_DITA, toURI("main.ditamap"), null))));
        f.setContentHandler(new DefaultHandler() {
            @Override
            public void startElement(final String uri, final String localName, final String qName, final Attributes atts) throws SAXException {
                assertEquals("library.dita", atts.getValue(ATTRIBUTE_NAME_CONREF));
            }
        });

        f.startElement(NULL_NS_URI, TOPIC_P.localName, TOPIC_P.localName, new AttributesBuilder()
                .add(ATTRIBUTE_NAME_CONKEYREF, "foo")
                .build());
    }

    @Test
    public void testKeyAndElement() throws SAXException, IOException {
        final ConkeyrefFilter f = getConkeyrefFilter();
        f.setKeyDefinitions(createKeyScope(toMap(new KeyDef("foo", toURI("library.dita"), ATTR_SCOPE_VALUE_LOCAL, ATTR_FORMAT_VALUE_DITA, toURI("main.ditamap"), null))));
        f.setContentHandler(new DefaultHandler() {
            @Override
            public void startElement(final String uri, final String localName, final String qName, final Attributes atts) throws SAXException {
                // FIXME: this would be right only for maps, for topics the root topic ID should be added
                assertEquals("library.dita#bar", atts.getValue(ATTRIBUTE_NAME_CONREF));
            }
        });

        f.startElement(NULL_NS_URI, TOPIC_P.localName, TOPIC_P.localName, new AttributesBuilder()
                .add(ATTRIBUTE_NAME_CONKEYREF, "foo/bar")
                .build());
    }

    @Test
    public void testElementInTarget() throws SAXException, IOException {
        final ConkeyrefFilter f = getConkeyrefFilter();
        f.setKeyDefinitions(createKeyScope(toMap(new KeyDef("foo", toURI("library.dita#baz"), ATTR_SCOPE_VALUE_LOCAL, ATTR_FORMAT_VALUE_DITA, toURI("main.ditamap"), null))));
        f.setContentHandler(new DefaultHandler() {
            @Override
            public void startElement(final String uri, final String localName, final String qName, final Attributes atts) throws SAXException {
                assertEquals("library.dita#baz/bar", atts.getValue(ATTRIBUTE_NAME_CONREF));
            }
        });

        f.startElement(NULL_NS_URI, TOPIC_P.localName, TOPIC_P.localName, new AttributesBuilder()
                .add(ATTRIBUTE_NAME_CONKEYREF, "foo/bar")
                .build());
    }

    @Test
    public void testRelativePaths() throws SAXException, IOException {
        final ConkeyrefFilter f = getConkeyrefFilter();
        f.setCurrentFile(job.tempDirURI.resolve("product/sub%20folder/this.dita"));
        f.setKeyDefinitions(createKeyScope(toMap(new KeyDef("foo", toURI("common/library.dita"), ATTR_SCOPE_VALUE_LOCAL, ATTR_FORMAT_VALUE_DITA, toURI("main.ditamap"), null))));
        f.setContentHandler(new DefaultHandler() {
            @Override
            public void startElement(final String uri, final String localName, final String qName, final Attributes atts) throws SAXException {
                assertEquals("../../common/library.dita", atts.getValue(ATTRIBUTE_NAME_CONREF));
            }
        });

        f.startElement(NULL_NS_URI, TOPIC_P.localName, TOPIC_P.localName, new AttributesBuilder()
                .add(ATTRIBUTE_NAME_CONKEYREF, "foo")
                .build());
    }

    @Test
    public void testRelativePathsWithUplevels() throws SAXException, IOException {
        job.setInputDir(new File(".").getCanonicalFile().toURI());
        job.add(new Job.FileInfo.Builder()
                .src(srcDir.resolve("maps/root.map"))
                .uri(URI.create("maps/root.map"))
                .format(ATTR_FORMAT_VALUE_DITAMAP)
                .isInput(true)
                .build());
        job.add(new Job.FileInfo.Builder()
                .src(srcDir.resolve("product/topic.dita"))
                .uri(URI.create("product/this.dita"))
                .format(ATTR_FORMAT_VALUE_DITA)
                .hasConref(true)
                .build());
        job.add(new Job.FileInfo.Builder()
                .src(srcDir.resolve("common/library.dita"))
                .uri(URI.create("common/library.dita"))
                .format(ATTR_FORMAT_VALUE_DITA)
                .build());

        final ConkeyrefFilter f = getConkeyrefFilter();
        f.setCurrentFile(job.tempDirURI.resolve("product/topic.dita"));
        f.setKeyDefinitions(createKeyScope(toMap(new KeyDef("foo", URI.create("../common/library.dita"), ATTR_SCOPE_VALUE_LOCAL, ATTR_FORMAT_VALUE_DITA, job.tempDirURI.resolve("maps/root.map"), null))));
        f.setContentHandler(new DefaultHandler() {
            @Override
            public void startElement(final String uri, final String localName, final String qName, final Attributes atts) throws SAXException {
                assertEquals("../common/library.dita", atts.getValue(ATTRIBUTE_NAME_CONREF));
            }
        });

        f.startElement(NULL_NS_URI, TOPIC_P.localName, TOPIC_P.localName, new AttributesBuilder()
                .add(ATTRIBUTE_NAME_CONKEYREF, "foo")
                .build());
    }

    @Test
    public void testRelativePathsWithUplevels_flat() throws SAXException, IOException {
        job.setInputDir(new File(".").getCanonicalFile().toURI());
        job.add(new Job.FileInfo.Builder()
                .src(srcDir.resolve("maps/root.map"))
                .uri(URI.create("ROOT.map"))
                .format(ATTR_FORMAT_VALUE_DITAMAP)
                .isInput(true)
                .build());
        job.add(new Job.FileInfo.Builder()
                .src(srcDir.resolve("product/topic.dita"))
                .uri(URI.create("THIS.dita"))
                .format(ATTR_FORMAT_VALUE_DITA)
                .hasConref(true)
                .build());
        job.add(new Job.FileInfo.Builder()
                .src(srcDir.resolve("common/library.dita"))
                .uri(URI.create("LIBRARY.dita"))
                .format(ATTR_FORMAT_VALUE_DITA)
                .build());

        final ConkeyrefFilter f = getConkeyrefFilter();
        f.setCurrentFile(job.tempDirURI.resolve("topic.dita"));
        f.setKeyDefinitions(createKeyScope(toMap(new KeyDef("foo", URI.create("LIBRARY.dita"), ATTR_SCOPE_VALUE_LOCAL, ATTR_FORMAT_VALUE_DITA, URI.create("main.ditamap"), null))));
        f.setContentHandler(new DefaultHandler() {
            @Override
            public void startElement(final String uri, final String localName, final String qName, final Attributes atts) throws SAXException {
                assertEquals("LIBRARY.dita", atts.getValue(ATTRIBUTE_NAME_CONREF));
            }
        });

            f.startElement(NULL_NS_URI, TOPIC_P.localName, TOPIC_P.localName, new AttributesBuilder()
                .add(ATTRIBUTE_NAME_CONKEYREF, "foo")
                .build());
    }

    @Test
    public void testMissingKey() throws SAXException, IOException {
        final ConkeyrefFilter f = getConkeyrefFilter();
        f.setKeyDefinitions(createKeyScope(Collections.emptyMap()));
        f.setContentHandler(new DefaultHandler() {
            @Override
            public void startElement(final String uri, final String localName, final String qName, final Attributes atts) throws SAXException {
                assertNull(atts.getValue(ATTRIBUTE_NAME_CONREF));
            }
        });
        final TestUtils.CachingLogger l = new TestUtils.CachingLogger();
        f.setLogger(l);

        f.startElement(NULL_NS_URI, TOPIC_P.localName, TOPIC_P.localName, new AttributesBuilder()
                .add(ATTRIBUTE_NAME_CONKEYREF, "foo/bar")
                .build());

        assertEquals(1, l.getMessages().size());
        assertEquals(CachingLogger.Message.Level.ERROR, l.getMessages().get(0).level);
    }

    private ConkeyrefFilter getConkeyrefFilter() throws IOException {
        final ConkeyrefFilter f = new ConkeyrefFilter();
        f.setLogger(new TestUtils.TestLogger());
        f.setJob(job);
        f.setCurrentFile(job.tempDirURI.resolve("this.dita"));
        return f;
    }

}
