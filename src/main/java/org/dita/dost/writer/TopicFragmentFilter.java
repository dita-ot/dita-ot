/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2014 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.writer;

import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.URLUtils.*;
import static org.dita.dost.util.XMLUtils.*;

import java.net.URI;
import java.util.*;

import org.dita.dost.util.DitaClass;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Resolves same topic fragment identifies in topics.
 */
public final class TopicFragmentFilter extends AbstractXMLFilter {

    public static final String PARAM_ATTRIBUTES = "attributes";

    private final Deque<DitaClass> classes = new LinkedList<>();
    private final Deque<String> topics = new ArrayDeque<>();

    List<String> attrNames;

    public TopicFragmentFilter() {
        super();
        this.attrNames = Collections.singletonList(ATTRIBUTE_NAME_HREF);
    }

    public TopicFragmentFilter(final String... attrNames) {
        super();
        this.attrNames = Arrays.asList(attrNames);
    }

    @Override
    public void startDocument() throws SAXException {
        if (attrNames == null) {
            attrNames = Arrays.asList(params.get(PARAM_ATTRIBUTES).split(","));
        }
        classes.clear();
        topics.clear();

        super.startDocument();
    }

    @Override
    public void startElement(final String uri,
            final String localName,
            final String qName,
            final Attributes atts)
            throws SAXException {
        Attributes res = atts;
        final DitaClass cls = DitaClass.getInstance(atts);
        classes.addFirst(cls);
        if (TOPIC_TOPIC.matches(cls)) {
            topics.addFirst(atts.getValue(ATTRIBUTE_NAME_ID));
        } else {
            for (final String attrName: attrNames) {
                URI href = toURI(atts.getValue(attrName));
                if (href != null && isLocalDitaReference(atts, attrName)) {
                    final String fragment = href.getFragment();
                    if (fragment != null && fragment.startsWith(".")) {
                        href = setFragment(href, topics.peekFirst() + fragment.substring(1));
                        res = new AttributesImpl(res);
                        addOrSetAttribute((AttributesImpl) res, attrName, href.toString());
                    }
                }
            }
        }
        super.startElement(uri, localName, qName, res);
    }

    private boolean isLocalDitaReference(final Attributes atts, final String attr) {
        switch (attr) {
            case ATTRIBUTE_NAME_CONREF:
            case ATTRIBUTE_NAME_CONREFEND:
                return true;
            default:
                final String format = atts.getValue(ATTRIBUTE_NAME_FORMAT);
                return format == null || format.equals(ATTR_FORMAT_VALUE_DITA);
        }
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
