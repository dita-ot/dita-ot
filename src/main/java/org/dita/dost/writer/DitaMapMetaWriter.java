/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.writer;

import org.dita.dost.util.DitaClass;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.dita.dost.util.Constants.*;

/**
 * 
 * @author Jian Le Shen
 */
public final class DitaMapMetaWriter extends AbstractDitaMetaWriter {

    private static final Map<String, List<String>> moveTable;
    static{
        final Map<String, List<String>> mt = new HashMap<String, List<String>>(32);
        mt.put(MAP_SEARCHTITLE.matcher, asList(MAP_TOPICMETA.localName, MAP_SEARCHTITLE.localName));
        mt.put(TOPIC_AUDIENCE.matcher, asList(MAP_TOPICMETA.localName, TOPIC_AUDIENCE.localName));
        mt.put(TOPIC_AUTHOR.matcher, asList(MAP_TOPICMETA.localName, TOPIC_AUTHOR.localName));
        mt.put(TOPIC_CATEGORY.matcher, asList(MAP_TOPICMETA.localName, TOPIC_CATEGORY.localName));
        mt.put(TOPIC_COPYRIGHT.matcher, asList(MAP_TOPICMETA.localName, TOPIC_COPYRIGHT.localName));
        mt.put(TOPIC_CRITDATES.matcher, asList(MAP_TOPICMETA.localName, TOPIC_CRITDATES.localName));
        mt.put(TOPIC_DATA.matcher, asList(MAP_TOPICMETA.localName, TOPIC_DATA.localName));
        mt.put(TOPIC_DATA_ABOUT.matcher, asList(MAP_TOPICMETA.localName, TOPIC_DATA_ABOUT.localName));
        mt.put(TOPIC_FOREIGN.matcher, asList(MAP_TOPICMETA.localName, TOPIC_FOREIGN.localName));
        mt.put(TOPIC_KEYWORDS.matcher, asList(MAP_TOPICMETA.localName, TOPIC_KEYWORDS.localName));
        mt.put(TOPIC_OTHERMETA.matcher, asList(MAP_TOPICMETA.localName, TOPIC_OTHERMETA.localName));
        mt.put(TOPIC_PERMISSIONS.matcher, asList(MAP_TOPICMETA.localName, TOPIC_PERMISSIONS.localName));
        mt.put(TOPIC_PRODINFO.matcher, asList(MAP_TOPICMETA.localName, TOPIC_PRODINFO.localName));
        mt.put(TOPIC_PUBLISHER.matcher, asList(MAP_TOPICMETA.localName, TOPIC_PUBLISHER.localName));
        mt.put(TOPIC_RESOURCEID.matcher, asList(MAP_TOPICMETA.localName, TOPIC_RESOURCEID.localName));
        mt.put(TOPIC_SOURCE.matcher, asList(MAP_TOPICMETA.localName, TOPIC_SOURCE.localName));
        mt.put(TOPIC_UNKNOWN.matcher, asList(MAP_TOPICMETA.localName, TOPIC_UNKNOWN.localName));
        moveTable = Collections.unmodifiableMap(mt);
    }

    private static final Map<String, Integer> compareTable;
    static{
        final Map<String, Integer> ct = new HashMap<String, Integer>(32);
        ct.put(MAP_TOPICMETA.localName, 1);
        ct.put(TOPIC_SEARCHTITLE.localName, 2);
        ct.put(TOPIC_SHORTDESC.localName, 3);
        ct.put(TOPIC_AUTHOR.localName, 4);
        ct.put(TOPIC_SOURCE.localName, 5);
        ct.put(TOPIC_PUBLISHER.localName, 6);
        ct.put(TOPIC_COPYRIGHT.localName, 7);
        ct.put(TOPIC_CRITDATES.localName, 8);
        ct.put(TOPIC_PERMISSIONS.localName, 9);
        ct.put(TOPIC_AUDIENCE.localName, 10);
        ct.put(TOPIC_CATEGORY.localName, 11);
        ct.put(TOPIC_KEYWORDS.localName, 12);
        ct.put(TOPIC_PRODINFO.localName, 13);
        ct.put(TOPIC_OTHERMETA.localName, 14);
        ct.put(TOPIC_RESOURCEID.localName, 15);
        ct.put(TOPIC_DATA.localName, 16);
        ct.put(TOPIC_DATA_ABOUT.localName, 17);
        ct.put(TOPIC_FOREIGN.localName, 18);
        ct.put(TOPIC_UNKNOWN.localName, 19);
        compareTable = Collections.unmodifiableMap(ct);
    }

    @Override
    protected Map<String, List<String>> moveTable() {
        return moveTable;
    }

    @Override
    protected Map<String, Integer> compareTable() {
        return compareTable;
    }

    @Override
    protected DitaClass getRootClass() {
        return MAP_MAP;
    }

    @Override
    protected DitaClass rewriteDitaClass(String next) {
        return new DitaClass("- map/" + next + " ");
    }

    @Override
    protected boolean isStartPoint(String classAttrValue) {
        return MAP_TOPICMETA.matches(classAttrValue);
    }

    @Override
    protected boolean isEndPoint(String classAttrValue) {
        return MAP_NAVREF.matches(classAttrValue) ||
               MAP_ANCHOR.matches(classAttrValue) ||
               MAP_TOPICREF.matches(classAttrValue) ||
               MAP_RELTABLE.matches(classAttrValue);
    }

}
