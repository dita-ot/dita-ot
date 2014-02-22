package org.dita.dost.writer;

import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.URLUtils.*;
import static org.dita.dost.util.XMLUtils.*;

import java.net.URI;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;

import org.dita.dost.util.DitaClass;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * Resolves same topic fragment identifies in topics.
 */
public final class TopicFragmentFilter extends XMLFilterImpl {

    final Deque<DitaClass> classes = new LinkedList<DitaClass>();
    final Deque<String> topics = new ArrayDeque<String>();
    
    @Override
    public void startElement(final String uri,
            final String localName,
            final String qName,
            final Attributes atts)
            throws SAXException {
        Attributes res = atts;
        final DitaClass cls = atts.getValue(ATTRIBUTE_NAME_CLASS) != null ? new DitaClass(atts.getValue(ATTRIBUTE_NAME_CLASS)) : null;
        classes.addFirst(cls);
        if (TOPIC_TOPIC.matches(cls)) {
            topics.addFirst(atts.getValue(ATTRIBUTE_NAME_ID));
        } else {
            URI href = toURI(atts.getValue(ATTRIBUTE_NAME_HREF));
            final String format = atts.getValue(ATTRIBUTE_NAME_FORMAT);
            if (href != null && (format == null || format.equals(ATTR_FORMAT_VALUE_DITA))) {
                final String fragment = href.getFragment(); 
                if (fragment != null && fragment.startsWith(".")) {
                    href = setFragment(href, topics.peekFirst() + fragment.substring(1));
                    res = new AttributesImpl(res);
                    addOrSetAttribute((AttributesImpl) res, ATTRIBUTE_NAME_HREF, href.toString());
                }
            }
        }
        super.startElement(uri, localName, qName, res);
    }
    
    @Override
    public void endElement(final String uri,
            final String localName,
            final String qName)
            throws SAXException {
        final DitaClass cls = classes.removeFirst();
        if (TOPIC_TOPIC.matches(cls)) {
            topics.removeFirst();
        }
        super.endElement(uri, localName, qName);
    }
    
}
