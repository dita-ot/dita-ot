package org.dita.dost.writer;

import org.dita.dost.TestUtils;
import org.dita.dost.util.XMLUtils;
import org.junit.Test;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import static javax.xml.XMLConstants.NULL_NS_URI;
import static org.dita.dost.util.Constants.*;
import static org.junit.Assert.*;

public class NormalizeFilterTest {

    @Test
    public void testCascade() throws Exception {
        final NormalizeFilter f = new NormalizeFilter();
        f.setLogger(new TestUtils.TestLogger());
        f.setContentHandler(new DefaultHandler() {
            @Override
            public void startElement(String uri, String localName,
                                     String qName, Attributes attributes) throws SAXException {
                assertEquals(ATTRIBUTE_CASCADE_VALUE_NOMERGE, attributes.getValue(ATTRIBUTE_NAME_CASCADE));
            }
        });
        f.startElement(NULL_NS_URI, MAP_MAP.localName, MAP_MAP.localName, new XMLUtils.AttributesBuilder()
                .add(ATTRIBUTE_NAME_CLASS, MAP_MAP.toString()).build());
    }

    @Test
    public void testExistingCascade() throws Exception {
        final NormalizeFilter f = new NormalizeFilter();
        f.setLogger(new TestUtils.TestLogger());
        f.setContentHandler(new DefaultHandler() {
            @Override
            public void startElement(String uri, String localName,
                                     String qName, Attributes attributes) throws SAXException {
                assertEquals(ATTRIBUTE_CASCADE_VALUE_MERGE, attributes.getValue(ATTRIBUTE_NAME_CASCADE));
            }
        });
        f.startElement(NULL_NS_URI, MAP_MAP.localName, MAP_MAP.localName, new XMLUtils.AttributesBuilder()
                .add(ATTRIBUTE_NAME_CLASS, MAP_MAP.toString())
                .add(ATTRIBUTE_NAME_CASCADE, ATTRIBUTE_CASCADE_VALUE_MERGE).build());
    }

}
