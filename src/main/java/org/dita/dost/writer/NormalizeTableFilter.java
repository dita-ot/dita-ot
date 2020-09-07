/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2013 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.writer;

import org.dita.dost.log.MessageUtils;
import org.dita.dost.util.Configuration;
import org.dita.dost.util.XMLUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import java.util.*;
import java.util.stream.Collectors;

import static javax.xml.XMLConstants.NULL_NS_URI;
import static org.dita.dost.util.Constants.*;

/**
 * Normalize table content.
 *
 * <ul>
 *   <li>Rewrite table column names to {@code "col" num}, where {@code num} is the column number, and add column name to every entry.</li>
 *   <li>Add column coordinates to entries</li>
 * </ul>
 */
public class NormalizeTableFilter extends AbstractXMLFilter {

    private static final String ATTRIBUTE_NAME_COLNAME = "colname";
    private static final String ATTRIBUTE_NAME_COLNUM = "colnum";
    private static final String ATTRIBUTE_NAME_COLWIDTH = "colwidth";
    private static final String COLUMN_NAME_COL = "col";
    private static final String ATTR_MORECOLS = "morecols";
    private static final String ATTR_X = "x";
    private static final String ATTR_Y = "y";

    private final Deque<String> classStack = new LinkedList<>();
    /** DITA class stack */
    private final Map<String, String> ns = new HashMap<>();
    private int depth;

    private final Deque<TableState> tableStack = new LinkedList<>();
    /** Cached table stack head */
    private TableState tableState;
    private Configuration.Mode processingMode;

    public NormalizeTableFilter() {
        super();
        depth = 0;
    }

    /** @deprecated since 2.3 */
    @Deprecated
    public void setProcessingMode(final Configuration.Mode processingMode) {
        this.processingMode = processingMode;
    }

    // SAX methods

    @Override
    public void startDocument() throws SAXException {
        final String mode = params.get(ANT_INVOKER_EXT_PARAM_PROCESSING_MODE);
        processingMode = mode != null ? Configuration.Mode.valueOf(mode.toUpperCase()) : Configuration.Mode.LAX;

        getContentHandler().startDocument();
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

        if (TOPIC_TGROUP.matches(cls)) {
            tableState = new TableState();
            tableStack.addFirst(tableState);
            tableState.cols = getColCount(atts);
            tableState.colSpec = new ArrayList<>();
        } else if (TOPIC_COLSPEC.matches(cls)) {
            processColspec(res);
        } else if (TOPIC_TBODY.matches(cls) || TOPIC_THEAD.matches(cls)) {
            if (tableState.columnNumberEnd < tableState.cols && tableState.cols != -1) {
                final int length = tableState.cols - tableState.totalColumns;
                for (int i = 0; i < length; i++) {
                    generateColSpec();
                }
            }
        } else if (TOPIC_ROW.matches(cls)) {
            // TODO: Inline me to (currentColumn + 1)
            tableState.columnNumber = 1; // initialize the column number
            tableState.columnNumberEnd = 0;
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
        } else if (TOPIC_ENTRY.matches(cls)) {
            processEntry(res);
        }

        getContentHandler().startElement(uri, localName, qName, res);
    }

    private void generateColSpec() throws SAXException {
        final AttributesImpl attr = new AttributesImpl();
        XMLUtils.addOrSetAttribute(attr, ATTRIBUTE_NAME_CLASS, TOPIC_COLSPEC.toString());
        processColspec(attr);
        getContentHandler().startElement(NULL_NS_URI, TOPIC_COLSPEC.localName, TOPIC_COLSPEC.localName, attr);
        getContentHandler().endElement(NULL_NS_URI, TOPIC_COLSPEC.localName, TOPIC_COLSPEC.localName);
    }

    private void processEntry(AttributesImpl res) throws SAXException {
        try {
            tableState.columnNumber = getStartNumber(res, tableState.columnNumberEnd);
            final int colspan = getColSpan(res);
            final int rowspan = getRowSpan(res);
            Span prev;
            if (tableState.previousRow != null) {
                for (prev = tableState.previousRow.get(tableState.currentColumn); prev != null && prev.y > 1; prev = tableState.previousRow.get(tableState.currentColumn)) {
                    for (int i = 0; i < prev.x; i++) {
                        tableState.currentColumn = tableState.currentColumn + 1; //prev.x - 1;
                        grow(tableState.currentRow, tableState.currentColumn + 1);
//                        tableState.currentRow.set(tableState.currentColumn, null);
                    }
                }
            } else {
                prev = new Span(1, 1);
            }
            grow(tableState.currentRow, tableState.currentColumn + colspan);
            final Span span = new Span(colspan, rowspan);

            tableState.currentRow.set(tableState.currentColumn, span);

            XMLUtils.addOrSetAttribute(res, ATTRIBUTE_NAME_COLNAME, COLUMN_NAME_COL + (tableState.currentColumn + 1));
            if (res.getValue(ATTRIBUTE_NAME_NAMEST) != null) {
                XMLUtils.addOrSetAttribute(res, ATTRIBUTE_NAME_NAMEST, COLUMN_NAME_COL + tableState.columnNumber);
            }
            if (res.getValue(ATTRIBUTE_NAME_NAMEEND) != null) {
                XMLUtils.addOrSetAttribute(res, ATTRIBUTE_NAME_NAMEEND, COLUMN_NAME_COL + getEndNumber(res, tableState.columnNumber));
                XMLUtils.addOrSetAttribute(res, DITA_OT_NS, ATTR_MORECOLS, DITA_OT_NS_PREFIX + ":" + ATTR_MORECOLS, "CDATA", Integer.toString(span.x - 1));
            }
            // Add extensions
            XMLUtils.addOrSetAttribute(res, DITA_OT_NS, ATTR_X, DITA_OT_NS_PREFIX + ":" + ATTR_X, "CDATA", Integer.toString(tableState.currentColumn + 1));
            XMLUtils.addOrSetAttribute(res, DITA_OT_NS, ATTR_Y, DITA_OT_NS_PREFIX + ":" + ATTR_Y, "CDATA", Integer.toString(tableState.rowNumber));

            tableState.currentColumn = tableState.currentColumn + colspan;
            tableState.columnNumberEnd = getEndNumber(res, tableState.columnNumber);
        } catch (IndexOutOfBoundsException e) {
            if (processingMode == Configuration.Mode.STRICT) {
                throw new SAXException(MessageUtils.getMessage("DOTJ082E").setLocation(res).toString());
            } else {
                logger.error(MessageUtils.getMessage("DOTJ082E").setLocation(res).toString());
            }
        }
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName) throws SAXException {
        getContentHandler().endElement(uri, localName, qName);
        final String cls = classStack.removeFirst();
        if (TOPIC_TGROUP.matches(cls)) {
            tableStack.removeFirst();
            tableState = tableStack.peekFirst();
        } else if (TOPIC_ROW.matches(cls)) {
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
        final String start = atts.getValue(ATTRIBUTE_NAME_NAMEST);
        final String end = atts.getValue(ATTRIBUTE_NAME_NAMEEND);
        if (start != null && end != null) {
            final int colnumStart = tableState.colSpec.indexOf(start);
            final int colnumEnd = tableState.colSpec.indexOf(end);
            // TODO: handle missing column definition
            final int ret = colnumEnd - colnumStart + 1;
            return ret <= 0 ? 1 : ret;
        } else {
            return 1;
        }
    }

    private int getRowSpan(final Attributes atts) {
        final String span = atts.getValue(ATTRIBUTE_NAME_MOREROWS);
        if (span != null) {
            return Integer.parseInt(span) + 1;
        } else {
            return 1;
        }
    }

    private int getColCount(final Attributes atts) throws SAXException {
        final String c = atts.getValue(ATTRIBUTE_NAME_COLS).trim();
        try {
            return Integer.parseInt(c);
        } catch (final NumberFormatException e) {
            if (processingMode == Configuration.Mode.STRICT) {
                throw new SAXException(MessageUtils.getMessage("DOTJ062E", ATTRIBUTE_NAME_COLS, c).setLocation(atts).toString());
            } else {
                logger.error(MessageUtils.getMessage("DOTJ062E", ATTRIBUTE_NAME_COLS, c).setLocation(atts).toString());
            }
            return -1;
        }
    }

    private void processColspec(final AttributesImpl res) {
        tableState.columnNumber = tableState.columnNumberEnd + 1;
        final String colName = res.getValue(ATTRIBUTE_NAME_COLNAME) != null ? res.getValue(ATTRIBUTE_NAME_COLNAME) : COLUMN_NAME_COL + tableState.columnNumber;
        grow(tableState.colSpec, tableState.columnNumber);
        tableState.colSpec.set(tableState.columnNumber - 1, colName);

        final String colNum = res.getValue(ATTRIBUTE_NAME_COLNUM);
        if (colNum == null || colNum.isEmpty()) {
            XMLUtils.addOrSetAttribute(res, ATTRIBUTE_NAME_COLNUM, Integer.toString(tableState.columnNumber));
        }

        tableState.columnNumberEnd = tableState.columnNumber;
        // change the col name of colspec
        XMLUtils.addOrSetAttribute(res, ATTRIBUTE_NAME_COLNAME, COLUMN_NAME_COL + tableState.columnNumber);
        // total columns count
        tableState.totalColumns = tableState.columnNumberEnd;
        final String colWidth = res.getValue(ATTRIBUTE_NAME_COLWIDTH);
        // OASIS Table Model defaults to 1*, but that will disables automatic column layout
        if (colWidth == null) {
            //XMLUtils.addOrSetAttribute(res, ATTRIBUTE_NAME_COLWIDTH, "1*");
        } else if (colWidth.isEmpty()) {
            XMLUtils.removeAttribute(res, ATTRIBUTE_NAME_COLWIDTH);
        } else if (colWidth.equals("*")) {
            XMLUtils.addOrSetAttribute(res, ATTRIBUTE_NAME_COLWIDTH, "1*");
        }
    }

    private int getEndNumber(final Attributes atts, final int columnStart) {
        final String end = atts.getValue(ATTRIBUTE_NAME_NAMEEND);
        if (end == null) {
            return columnStart;
        } else {
            int ret = tableState.colSpec.indexOf(end) + 1;
            if (ret == 0) {
                return columnStart;
            }
            return ret;
        }
    }

    private int getStartNumber(final Attributes atts, final int previousEnd) {
        final String num = atts.getValue(ATTRIBUTE_NAME_COLNUM);
        final String start = atts.getValue(ATTRIBUTE_NAME_NAMEST);
        final String name = atts.getValue(ATTRIBUTE_NAME_COLNAME);
        if (num != null) {
            return Integer.parseInt(num);
        } else if (start != null) {
            final int ret = tableState.colSpec.indexOf(start) + 1;
            if (ret == 0) {
                return previousEnd + 1;
            }
            return ret;
        } else if (name != null) {
            final int ret = tableState.colSpec.indexOf(name) + 1;
            if (ret == 0) {
                return previousEnd + 1;
            }
            return ret;
        } else {
            return previousEnd + 1;
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
        public List<String> colSpec;
        public int rowNumber = 0;
        public ArrayList<Span> previousRow;
        public ArrayList<Span> currentRow;
        public int currentColumn;
        /** ColumnNumber is used to adjust column name */
        public int columnNumber = 1;
        /** columnNumberEnd is the end value for current entry */
        public int columnNumberEnd = 0;
        /** Store total column count */
        public int totalColumns = 0;
        /** Number of cols in tgroup */
        public int cols;
    }
}
