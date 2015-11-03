package org.dita.dost.writer;

import org.junit.Before;
import org.junit.Test;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.dita.dost.util.XMLUtils.AttributesBuilder;
import static org.junit.Assert.assertEquals;

public class NameGeneralizationFilterTest {

    final List<String> names = new ArrayList<>();
    final NameGeneralizationFilter filter = new NameGeneralizationFilter();

    public NameGeneralizationFilterTest() {
        filter.setContentHandler(new DefaultHandler() {
            @Override
            public void startElement(final String uri, final String localName, final String name,
                                     final Attributes atts) throws SAXException {
                names.add(localName);
            }
            @Override
            public void endElement(final String uri, final String localName, final String name) throws SAXException {
                names.add("/" + localName);
            }
        });
    }

    @Before
    public void setUp() {
        names.clear();
    }

    @Test
    public void testBase() throws SAXException {
        filter.startElement("", "topic", "topic", new AttributesBuilder().add("class", "- topic/topic ").build());
        filter.startElement("", "ph", "ph", new AttributesBuilder().add("class", "- topic/ph ").build());
        filter.endElement("", "ph", "ph");
        filter.endElement("", "topic", "topic");

        assertEquals(asList("topic", "ph", "/ph", "/topic"), names);
    }

    @Test
    public void testSpecialisation() throws SAXException {
        filter.startElement("", "concept", "concept", new AttributesBuilder().add("class", "+ topic/topic concept/concept ").build());
        filter.startElement("", "b", "b", new AttributesBuilder().add("class", "+ topic/ph hi-d/b ").build());
        filter.endElement("", "b", "b");
        filter.endElement("", "concept", "concept");

        assertEquals(asList("topic", "ph", "/ph", "/topic"), names);
    }

}