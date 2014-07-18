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
 * DitaMetaWriter reads dita topic file and insert the metadata information into it.
 * 
 * @author Zhang, Yuan Peng
 */
public final class DitaMetaWriter extends AbstractDitaMetaWriter {

    private static final Map<String, List<String>> moveTable;
    static{
        final Map<String, List<String>> mt = new HashMap<String, List<String>>(32);
        mt.put(MAP_SEARCHTITLE.matcher, asList(TOPIC_TITLEALTS.localName, TOPIC_SEARCHTITLE.localName));
        mt.put(TOPIC_AUDIENCE.matcher, asList(TOPIC_PROLOG.localName, TOPIC_METADATA.localName, TOPIC_AUDIENCE.localName));
        mt.put(TOPIC_AUTHOR.matcher, asList(TOPIC_PROLOG.localName, TOPIC_AUTHOR.localName));
        mt.put(TOPIC_CATEGORY.matcher, asList(TOPIC_PROLOG.localName, TOPIC_METADATA.localName, TOPIC_CATEGORY.localName));
        mt.put(TOPIC_COPYRIGHT.matcher, asList(TOPIC_PROLOG.localName, TOPIC_COPYRIGHT.localName));
        mt.put(TOPIC_CRITDATES.matcher, asList(TOPIC_PROLOG.localName, TOPIC_CRITDATES.localName));
        mt.put(TOPIC_DATA.matcher, asList(TOPIC_PROLOG.localName, TOPIC_DATA.localName));
        mt.put(TOPIC_DATA_ABOUT.matcher, asList(TOPIC_PROLOG.localName, TOPIC_DATA_ABOUT.localName));
        mt.put(TOPIC_FOREIGN.matcher, asList(TOPIC_PROLOG.localName, TOPIC_FOREIGN.localName));
        mt.put(TOPIC_KEYWORDS.matcher, asList(TOPIC_PROLOG.localName, TOPIC_METADATA.localName, TOPIC_KEYWORDS.localName));
        mt.put(TOPIC_OTHERMETA.matcher, asList(TOPIC_PROLOG.localName, TOPIC_METADATA.localName, TOPIC_OTHERMETA.localName));
        mt.put(TOPIC_PERMISSIONS.matcher, asList(TOPIC_PROLOG.localName, TOPIC_PERMISSIONS.localName));
        mt.put(TOPIC_PRODINFO.matcher, asList(TOPIC_PROLOG.localName, TOPIC_METADATA.localName, TOPIC_PRODINFO.localName));
        mt.put(TOPIC_PUBLISHER.matcher, asList(TOPIC_PROLOG.localName, TOPIC_PUBLISHER.localName));
        mt.put(TOPIC_RESOURCEID.matcher, asList(TOPIC_PROLOG.localName, TOPIC_RESOURCEID.localName));
        mt.put(MAP_MAP.matcher, asList(TOPIC_TITLEALTS.localName, TOPIC_SEARCHTITLE.localName));
        mt.put(TOPIC_SOURCE.matcher, asList(TOPIC_PROLOG.localName, TOPIC_SOURCE.localName));
        mt.put(TOPIC_UNKNOWN.matcher, asList(TOPIC_PROLOG.localName, TOPIC_UNKNOWN.localName));
        moveTable = Collections.unmodifiableMap(mt);
    }

    private static final Map<String, Integer> compareTable;
    static{
        final Map<String, Integer> ct = new HashMap<String, Integer>(32);
        ct.put(TOPIC_TITLEALTS.localName, 1);
        ct.put(TOPIC_NAVTITLE.localName, 2);
        ct.put(TOPIC_SEARCHTITLE.localName, 3);
        ct.put(TOPIC_ABSTRACT.localName, 4);
        ct.put(TOPIC_SHORTDESC.localName, 5);
        ct.put(TOPIC_PROLOG.localName, 6);
        ct.put(TOPIC_AUTHOR.localName, 7);
        ct.put(TOPIC_SOURCE.localName, 8);
        ct.put(TOPIC_PUBLISHER.localName, 9);
        ct.put(TOPIC_COPYRIGHT.localName, 10);
        ct.put(TOPIC_CRITDATES.localName, 11);
        ct.put(TOPIC_PERMISSIONS.localName, 12);
        ct.put(TOPIC_METADATA.localName, 13);
        ct.put(TOPIC_AUDIENCE.localName, 14);
        ct.put(TOPIC_CATEGORY.localName, 15);
        ct.put(TOPIC_KEYWORDS.localName, 16);
        ct.put(TOPIC_PRODINFO.localName, 17);
        ct.put(TOPIC_OTHERMETA.localName, 18);
        ct.put(TOPIC_RESOURCEID.localName, 19);
        ct.put(TOPIC_DATA.localName, 20);
        ct.put(TOPIC_DATA_ABOUT.localName, 21);
        ct.put(TOPIC_FOREIGN.localName, 22);
        ct.put(TOPIC_UNKNOWN.localName, 23);
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
        return TOPIC_TOPIC;
    }

    @Override
    protected DitaClass rewriteDitaClass(String next) {
        return new DitaClass("- topic/" + next + " ");
    }

    @Override
    protected boolean isEndPoint(final String classAttrValue) {
        return TOPIC_TOPIC.matches(classAttrValue) ||
               TOPIC_RELATED_LINKS.matches(classAttrValue) ||
               TOPIC_BODY.matches(classAttrValue);
    }

    @Override
    protected boolean isStartPoint(final String classAttrValue) {
        return TOPIC_PROLOG.matches(classAttrValue) ||
               TOPIC_ABSTRACT.matches(classAttrValue) ||
               TOPIC_SHORTDESC.matches(classAttrValue) ||
               TOPIC_TITLEALTS.matches(classAttrValue);
    }

}
