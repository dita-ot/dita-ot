/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2013 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.writer;

import org.dita.dost.util.XMLUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import java.util.*;

import static org.dita.dost.util.Constants.*;

/**
 * Normalize simpletable content.
 *
 * <ul>
 *   <li>Rewrite table column names to {@code "col" num}, where {@code num} is the column number, and add column name to every entry.</li>
 * </ul>
 */
public final class NormalizeSimpleTableFilter extends NormalizeTableFilter {

    private static final String ATTRIBUTE_NAME_COLSPAN = "colspan";
    private static final String ATTRIBUTE_NAME_ROWSPAN = "rowspan";
    private static final String ATTR_X = "x";
    private static final String ATTR_Y = "y";

    private final Deque<String> classStack = new LinkedList<>();
    private int depth;
    private final Map<String, String> ns = new HashMap<>();

    private int rowNumber;
    private ArrayList<Span> previousRow;
    private ArrayList<Span> currentRow;
    private int currentColumn;

    public NormalizeSimpleTableFilter() {
        super();
        depth = 0;
    }

    @Override
    public void startPrefixMapping(final String prefix, final String uri) throws SAXException {
        ns.put(prefix, uri);
        getContentHandler().startPrefixMapping(prefix, uri);
    }

    @Override
    public void endPrefixMapping(final String prefix) throws SAXException {
        getContentHandler().endPrefixMapping(prefix);
        ns.remove(prefix);
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes atts)
            throws SAXException {
        depth++;
        if (depth == 1 && !ns.containsKey(DITA_OT_NS_PREFIX)) {
            super.startPrefixMapping(DITA_OT_NS_PREFIX, DITA_OT_NS);
        }

        final AttributesImpl res = new AttributesImpl(atts);
        final String cls = atts.getValue(ATTRIBUTE_NAME_CLASS);
        classStack.addFirst(cls);

        if (TOPIC_SIMPLETABLE.matches(cls)) {
            rowNumber = 0;
            previousRow = null;
            currentRow = null;
        } else if (TOPIC_STROW.matches(cls) || TOPIC_STHEAD.matches(cls)) {
            rowNumber++;
            currentRow = previousRow != null ? new ArrayList<>(Arrays.asList(new Span[previousRow.size()])) : new ArrayList<>();
            currentColumn = 0;
        } else if (TOPIC_STENTRY.matches(cls)) {
            final int colspan = getSpan(atts, ATTRIBUTE_NAME_COLSPAN);
            final int rowspan = getSpan(atts, ATTRIBUTE_NAME_ROWSPAN);
            final Span prev;
            if (previousRow != null) {
                prev = previousRow.get(currentColumn);
                if (prev != null && prev.y > 1) {
                    for (int i = 0; i < prev.x; i++) {
                        currentColumn = currentColumn + 1; //prev.x - 1;
                        grow(currentRow, currentColumn + 1);
                        currentRow.set(currentColumn, null);
                    }
                }
            } else {
                prev = new Span(1, 1);
            }
            grow(currentRow, currentColumn + colspan);
            final Span span = new Span(colspan, rowspan);

            currentRow.set(currentColumn, span);

            XMLUtils.addOrSetAttribute(res, DITA_OT_NS, ATTR_X, DITA_OT_NS_PREFIX + ":" + ATTR_X, "CDATA", Integer.toString(currentColumn + 1));
            XMLUtils.addOrSetAttribute(res, DITA_OT_NS, ATTR_Y, DITA_OT_NS_PREFIX + ":" + ATTR_Y, "CDATA", Integer.toString(rowNumber));

            currentColumn = currentColumn + colspan;
        }

        getContentHandler().startElement(uri, localName, qName, res);
    }

    private void grow(final ArrayList<?> array, final int size) {
        while (array.size() < size) {
            array.add(null);
        }
    }

    private int getSpan(final Attributes atts, final String name) {
        final String span = atts.getValue(name);
        if (span != null) {
            return Integer.parseInt(span);
        } else {
            return 1;
        }
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName) throws SAXException {
        getContentHandler().endElement(uri, localName, qName);
        final String cls = classStack.removeFirst();
        if (TOPIC_STROW.matches(cls) || TOPIC_STHEAD.matches(cls)) {
            previousRow = currentRow;
            currentRow = null;
            currentColumn = -1;
        }

        if (depth == 1) {
            super.endPrefixMapping(DITA_OT_NS_PREFIX);
        }
        depth--;
    }

    private static class Span {
        public final int x;
        public final int y;

        private Span(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}
