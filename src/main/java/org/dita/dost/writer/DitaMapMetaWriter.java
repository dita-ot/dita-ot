/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2007 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.writer;

import org.dita.dost.util.DitaClass;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.*;

import static org.dita.dost.util.Constants.*;

/**
 * Reads map files and inserts the metadata.
 */
public final class DitaMapMetaWriter extends AbstractDitaMetaWriter {

    private static final List<DitaClass> topicmetaPosition = Collections.unmodifiableList(Collections.singletonList(
            TOPIC_TITLE
    ));
    private static final List<DitaClass> topicmetaOrder = Collections.unmodifiableList(Arrays.asList(
            TOPIC_NAVTITLE,
            MAP_LINKTEXT,
            MAP_SEARCHTITLE,
            MAP_SHORTDESC,
            TOPIC_AUTHOR,
            TOPIC_SOURCE,
            TOPIC_PUBLISHER,
            TOPIC_COPYRIGHT,
            TOPIC_CRITDATES,
            TOPIC_PERMISSIONS,
            TOPIC_METADATA,
            TOPIC_AUDIENCE,
            TOPIC_CATEGORY,
            TOPIC_KEYWORDS,
            TOPIC_PRODINFO,
            TOPIC_OTHERMETA,
            TOPIC_RESOURCEID,
            TOPIC_DATA,
            TOPIC_DATA_ABOUT,
            TOPIC_FOREIGN,
            TOPIC_UNKNOWN
    ));

    public Document process(final Document doc) {
        Element root = doc.getDocumentElement();
        if (root.getTagName().equals(ELEMENT_NAME_DITA)) {
            root = getFirstChildElement(root, TOPIC_TOPIC);
        }
        if (hasMetadata(topicmetaOrder)) {
            final Element prolog = findMetadataContainer(root, topicmetaPosition, TOPIC_PROLOG);
            processMetadata(prolog, topicmetaOrder);
        }
        return doc;
    }

}
