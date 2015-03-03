package org.dita.dost.writer;

import static javax.xml.XMLConstants.NULL_NS_URI;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.URLUtils.toURI;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import org.dita.dost.TestUtils;
import org.dita.dost.TestUtils.CachingLogger;
import org.dita.dost.util.Job;
import org.dita.dost.util.KeyDef;
import org.dita.dost.util.XMLUtils.AttributesBuilder;
import org.junit.Test;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class ConkeyrefFilterTest {

    @Test
    public void testKey() throws SAXException, IOException {
        final ConkeyrefFilter f = getConkeyrefFilter();
        f.setKeyDefinitions(Arrays.asList(new KeyDef("foo", toURI("library.dita"), ATTR_SCOPE_VALUE_LOCAL, toURI("main.ditamap"))));
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
        f.setKeyDefinitions(Arrays.asList(new KeyDef("foo", toURI("library.dita"), ATTR_SCOPE_VALUE_LOCAL, toURI("main.ditamap"))));
        f.setContentHandler(new DefaultHandler() {
            @Override
            public void startElement(final String uri, final String localName, final String qName, final Attributes atts) throws SAXException {
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
        f.setKeyDefinitions(Arrays.asList(new KeyDef("foo", toURI("library.dita#baz"), ATTR_SCOPE_VALUE_LOCAL, toURI("main.ditamap"))));
        f.setContentHandler(new DefaultHandler() {
            @Override
            public void startElement(final String uri, final String localName, final String qName, final Attributes atts) throws SAXException {
                assertEquals("library.dita#bar", atts.getValue(ATTRIBUTE_NAME_CONREF));
            }
        });
        
        f.startElement(NULL_NS_URI, TOPIC_P.localName, TOPIC_P.localName, new AttributesBuilder()
            .add(ATTRIBUTE_NAME_CONKEYREF, "foo/bar")
            .build());
    }

    @Test
    public void testRelativePaths() throws SAXException, IOException {
        final ConkeyrefFilter f = getConkeyrefFilter();
        f.setCurrentFile(new File("product/sub folder/this.dita"));
        f.setKeyDefinitions(Arrays.asList(new KeyDef("foo", toURI("common/library.dita"), ATTR_SCOPE_VALUE_LOCAL, toURI("main.ditamap"))));
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
    public void testMissingKey() throws SAXException, IOException {
        final ConkeyrefFilter f = getConkeyrefFilter();
        f.setKeyDefinitions(Collections.<KeyDef> emptyList());
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
        f.setJob(new Job(new File(".").getAbsoluteFile()));
        f.setCurrentFile(new File("this.dita"));
        return f;
    }
    
    

}
