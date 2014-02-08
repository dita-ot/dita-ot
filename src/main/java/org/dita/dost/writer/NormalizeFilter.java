/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */
package org.dita.dost.writer;

import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.Configuration.configuration;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Deque;

import org.dita.dost.util.XMLUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Normalize content.
 * 
 * <ul>
 *   <li>Rewrite table column names to {@code "col" num}, where {@code num} is the column number, and add column name to every entry.</li>
 * </ul>
 */
public final class NormalizeFilter extends AbstractXMLFilter {

    private static final String ATTRIBUTE_NAME_COLNAME = "colname";
    private static final String ATTRIBUTE_NAME_COLNUM = "colnum";
    private static final String COLUMN_NAME_COL = "col";

    private List<String> colSpec;
    /** ColumnNumber is used to adjust column name */
    private int columnNumber; //
    /** columnNumberEnd is the end value for current entry */
    private int columnNumberEnd;
    /** Stack to store colspec list */
    private final Deque<List<String>> colSpecStack;
    /** Stack to store rowNum */
    private final Deque<Integer> rowNumStack;
    /** Stack to store columnNumber */
    private final Deque<Integer> columnNumberStack;
    /** Stack to store columnNumberEnd */
    private final Deque<Integer> columnNumberEndStack;
    /** Stack to store rowsMap */
    private final Deque<Map<String, Integer>> rowsMapStack;
    /** Stack to store colSpanMap */
    private final Deque<Map<String, Integer>> colSpanMapStack;
    /** Store row number */
    private int rowNumber;
    /** Store total column count */
    private int totalColumns;
    /** store morerows attribute */
    private Map<String, Integer> rowsMap;
    private Map<String, Integer> colSpanMap;
    /** DITA class stack */
    private final Deque<String> classStack = new LinkedList<String>();

    public NormalizeFilter() {
        super();
        columnNumber = 1;
        columnNumberEnd = 0;
        // initialize row number
        rowNumber = 0;
        // initialize total column count
        totalColumns = 0;
        // initialize the map
        rowsMap = new HashMap<String, Integer>();
        colSpanMap = new HashMap<String, Integer>();
        colSpec = null;
        // initial the stack
        colSpecStack = new ArrayDeque<List<String>>();
        rowNumStack = new ArrayDeque<Integer>();
        columnNumberStack = new ArrayDeque<Integer>();
        columnNumberEndStack = new ArrayDeque<Integer>();
        rowsMapStack = new ArrayDeque<Map<String, Integer>>();
        colSpanMapStack = new ArrayDeque<Map<String, Integer>>();
    }

    // SAX methods

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes atts)
            throws SAXException {
        final AttributesImpl res = new AttributesImpl(atts);
        final String cls = atts.getValue(ATTRIBUTE_NAME_CLASS);
        classStack.addFirst(cls);
        if (TOPIC_TGROUP.matches(cls)) {
            // push into the stack.
            if (colSpec != null) {
                colSpecStack.addFirst(colSpec);
                rowNumStack.addFirst(rowNumber);
                columnNumberStack.addFirst(columnNumber);
                columnNumberEndStack.addFirst(columnNumberEnd);
                rowsMapStack.addFirst(rowsMap);
                colSpanMapStack.addFirst(colSpanMap);
            }
            columnNumber = 1; // initialize the column number
            columnNumberEnd = 0;// totally columns
            rowsMap = new HashMap<String, Integer>();
            colSpanMap = new HashMap<String, Integer>();
            // new table initialize the col list
            colSpec = new ArrayList<String>(16);
            // new table initialize the col list
            rowNumber = 0;
        } else if (TOPIC_ROW.matches(cls)) {
            columnNumber = 1; // initialize the column number
            columnNumberEnd = 0;
            // store the row number
            rowNumber++;
        } else if (TOPIC_COLSPEC.matches(cls)) {
            columnNumber = columnNumberEnd + 1;
            if (atts.getValue(ATTRIBUTE_NAME_COLNAME) != null) {
                colSpec.add(atts.getValue(ATTRIBUTE_NAME_COLNAME));
            } else {
                colSpec.add(COLUMN_NAME_COL + columnNumber);
            }
            columnNumberEnd = columnNumber;
            // change the col name of colspec
            XMLUtils.addOrSetAttribute(res, ATTRIBUTE_NAME_COLNAME, COLUMN_NAME_COL + columnNumber);
            // total columns count
            totalColumns = columnNumberEnd;
        } else if (TOPIC_ENTRY.matches(cls)) {
            columnNumber = getStartNumber(atts, columnNumberEnd);
            if (columnNumber > columnNumberEnd) {
                // The first row
                if (rowNumber == 1) {
                    XMLUtils.addOrSetAttribute(res, ATTRIBUTE_NAME_COLNAME, COLUMN_NAME_COL + columnNumber);
                    if (atts.getValue(ATTRIBUTE_NAME_NAMEST) != null) {
                        XMLUtils.addOrSetAttribute(res, ATTRIBUTE_NAME_NAMEST, COLUMN_NAME_COL + columnNumber);
                    }
                    if (atts.getValue(ATTRIBUTE_NAME_NAMEEND) != null) {
                        XMLUtils.addOrSetAttribute(res, ATTRIBUTE_NAME_NAMEEND, COLUMN_NAME_COL + getEndNumber(atts, columnNumber));
                    }
                // other row
                } else {
                    int offset = 0;
                    int currentCol = columnNumber;
                    while (currentCol <= totalColumns) {
                        int previous_offset = offset;
                        // search from first row
                        for (int row = 1; row < rowNumber; row++) {
                            final String pos = Integer.toString(row) + "-" + Integer.toString(currentCol);
                            if (rowsMap.containsKey(pos)) {
                                // get total span rows
                                final int totalSpanRows = rowsMap.get(pos);
                                if (rowNumber <= totalSpanRows) {
                                    offset += colSpanMap.get(pos);
                                }
                            }
                        }
                        if (offset > previous_offset) {
                            currentCol = columnNumber + offset;
                            previous_offset = offset;
                        } else {
                            break;
                        }
                    }
                    columnNumber = columnNumber + offset;
                    // if has morerows attribute
                    if (atts.getValue(ATTRIBUTE_NAME_MOREROWS) != null) {
                        final String pos = Integer.toString(rowNumber) + "-" + Integer.toString(columnNumber);
                        // total span rows
                        rowsMap.put(pos, Integer.parseInt(atts.getValue(ATTRIBUTE_NAME_MOREROWS)) + rowNumber);
                        colSpanMap.put(pos, getColumnSpan(atts));
                    }
                    XMLUtils.addOrSetAttribute(res, ATTRIBUTE_NAME_COLNAME, COLUMN_NAME_COL + columnNumber);
                    if (atts.getValue(ATTRIBUTE_NAME_NAMEST) != null) {
                        XMLUtils.addOrSetAttribute(res, ATTRIBUTE_NAME_NAMEST, COLUMN_NAME_COL + columnNumber);
                    }
                    if (atts.getValue(ATTRIBUTE_NAME_NAMEEND) != null) {
                        XMLUtils.addOrSetAttribute(res, ATTRIBUTE_NAME_NAMEEND, COLUMN_NAME_COL + getEndNumber(atts, columnNumber));
                    }
                }
            }
            columnNumberEnd = getEndNumber(atts, columnNumber);
        } else if (MAP_MAP.matches(cls)) {
            if (res.getIndex(ATTRIBUTE_NAME_CASCADE) == -1) {
                XMLUtils.addOrSetAttribute(res, ATTRIBUTE_NAME_CASCADE,
                        configuration.containsKey("default.cascade")
                        ? configuration.get("default.cascade")
                        : ATTRIBUTE_CASCADE_VALUE_MERGE); 
            }
        }
        getContentHandler().startElement(uri, localName, qName, res);
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName) throws SAXException {
        getContentHandler().endElement(uri, localName, qName);
        if (TOPIC_TGROUP.matches(classStack.removeFirst())) {
            // has tgroup tag
            if (!colSpecStack.isEmpty()) {
                colSpec = colSpecStack.peekFirst();
                rowNumber = rowNumStack.peekFirst();
                columnNumber = columnNumberStack.peekFirst();
                columnNumberEnd = columnNumberEndStack.peekFirst();
                rowsMap = rowsMapStack.peekFirst();
                colSpanMap = colSpanMapStack.peekFirst();
                colSpecStack.removeFirst();
                rowNumStack.removeFirst();
                columnNumberStack.removeFirst();
                columnNumberEndStack.removeFirst();
                rowsMapStack.removeFirst();
                colSpanMapStack.removeFirst();
            } else {
                // no more tgroup tag
                colSpec = null;
                rowNumber = 0;
                columnNumber = 1;
                columnNumberEnd = 0;
                rowsMap = null;
                colSpanMap = null;
            }
        }
        
    }

    // Private methods

    private int getColumnSpan(final Attributes atts) {
        final String start = atts.getValue(ATTRIBUTE_NAME_NAMEST);
        final String end = atts.getValue(ATTRIBUTE_NAME_NAMEEND);
        if (start == null || end == null) {
            return 1;
        } else {
            final int ret = colSpec.indexOf(end) - colSpec.indexOf(start) + 1;
            if (ret <= 0) {
                return 1;
            }
            return ret;
        }
    }

    private int getEndNumber(final Attributes atts, final int columnStart) {
        final String end = atts.getValue(ATTRIBUTE_NAME_NAMEEND);
        int ret;
        if (end == null) {
            return columnStart;
        } else {
            ret = colSpec.indexOf(end) + 1;
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
            final int ret = colSpec.indexOf(start) + 1;
            if (ret == 0) {
                return previousEnd + 1;
            }
            return ret;
        } else if (name != null) {
            final int ret = colSpec.indexOf(name) + 1;
            if (ret == 0) {
                return previousEnd + 1;
            }
            return ret;
        } else {
            return previousEnd + 1;
        }
    }

}
