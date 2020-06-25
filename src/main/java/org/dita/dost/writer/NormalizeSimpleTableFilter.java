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
import java.util.stream.Collectors;

import static org.dita.dost.util.Constants.*;

/**
 * Normalize simpletable content.
 *
 * <ul>
 *   <li>Add column coordinates to entries</li>
 * </ul>
 */
public final class NormalizeSimpleTableFilter extends AbstractXMLFilter {

    private static final String ATTRIBUTE_NAME_COLSPAN = "colspan";
    private static final String ATTRIBUTE_NAME_ROWSPAN = "rowspan";
    private static final String ATTR_X = "x";
    private static final String ATTR_Y = "y";

    /** DITA class stack */
    private final Deque<String> classStack = new LinkedList<>();
    private int depth;
    private final Map<String, String> ns = new HashMap<>();

    private final Deque<TableState> tableStack = new LinkedList<>();
    /** Cached table stack head */
    private TableState tableState;

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
            startPrefixMapping(DITA_OT_NS_PREFIX, DITA_OT_NS);
        }

        final AttributesImpl res = new AttributesImpl(atts);
        final String cls = atts.getValue(ATTRIBUTE_NAME_CLASS);
        classStack.addFirst(cls);

        if (TOPIC_SIMPLETABLE.matches(cls)) {
            tableState = new TableState();
            tableStack.addFirst(tableState);
        } else if (TOPIC_STROW.matches(cls) || TOPIC_STHEAD.matches(cls)) {
            tableState.rowNumber++;
            if (tableState.previousRow != null) {
                final List<Span> fromPrew = tableState.previousRow.stream()
                        .map(s -> {
                            if (s == null) {
                                return null;
                            }
                            return s.y > 1 ? new Span(s.x, s.y - 1) : null;
                        })
                        .collect(Collectors.toList());
                tableState.currentRow = new ArrayList<>(fromPrew);
            } else {
                tableState.currentRow = new ArrayList<>();
            }
            tableState.currentColumn = 0;
        } else if (TOPIC_STENTRY.matches(cls)) {
            processEntry(res);
        }

        getContentHandler().startElement(uri, localName, qName, res);
    }

    private void processEntry(AttributesImpl res) {
        final int colspan = getColSpan(res);
        final int rowspan = getRowSpan(res);
        Span prev;
        if (tableState.previousRow != null) {
            for (prev = tableState.previousRow.get(tableState.currentColumn); prev != null && prev.y > 1; prev = tableState.previousRow.get(tableState.currentColumn)) {
                for (int i = 0; i < prev.x; i++) {
                    tableState.currentColumn = tableState.currentColumn + 1; //prev.x - 1;
                    grow(tableState.currentRow, tableState.currentColumn + 1);
                }
            }
        } else {
            prev = new Span(1, 1);
        }
        grow(tableState.currentRow, tableState.currentColumn + colspan);
        final Span span = new Span(colspan, rowspan);

        tableState.currentRow.set(tableState.currentColumn, span);

        XMLUtils.addOrSetAttribute(res, DITA_OT_NS, ATTR_X, DITA_OT_NS_PREFIX + ":" + ATTR_X, "CDATA", Integer.toString(tableState.currentColumn + 1));
        XMLUtils.addOrSetAttribute(res, DITA_OT_NS, ATTR_Y, DITA_OT_NS_PREFIX + ":" + ATTR_Y, "CDATA", Integer.toString(tableState.rowNumber));

        tableState.currentColumn = tableState.currentColumn + colspan;
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName) throws SAXException {
        getContentHandler().endElement(uri, localName, qName);
        final String cls = classStack.removeFirst();
        if (TOPIC_SIMPLETABLE.matches(cls)) {
            tableStack.removeFirst();
            tableState = tableStack.peekFirst();
        } else if (TOPIC_STROW.matches(cls) || TOPIC_STHEAD.matches(cls)) {
            tableState.previousRow = tableState.currentRow;
            tableState.currentRow = null;
            tableState.currentColumn = -1;
        }

        if (depth == 1) {
            endPrefixMapping(DITA_OT_NS_PREFIX);
        }
        depth--;
    }

    private void grow(final List<?> array, final int size) {
        while (array.size() < size) {
            array.add(null);
        }
    }

    private int getColSpan(final Attributes atts) {
        final String span = atts.getValue(ATTRIBUTE_NAME_COLSPAN);
        if (span != null) {
            return Integer.parseInt(span);
        } else {
            return 1;
        }
    }

    private int getRowSpan(final Attributes atts) {
        final String span = atts.getValue(ATTRIBUTE_NAME_ROWSPAN);
        if (span != null) {
            return Integer.parseInt(span);
        } else {
            return 1;
        }
    }

    private static class Span {
        public final int x;
        public final int y;

        private Span(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    private static class TableState {
        /** Store row number */
        public int rowNumber = 0;
        public ArrayList<Span> previousRow;
        public ArrayList<Span> currentRow;
        public int currentColumn;
    }
}
