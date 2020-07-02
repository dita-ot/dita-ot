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
 * Reads topic files and inserts the metadata.
 */
public final class DitaMetaWriter extends AbstractDitaMetaWriter {

    private static final List<DitaClass> titlealtsPosition = Collections.unmodifiableList(Collections.singletonList(
            TOPIC_TITLE
    ));
    private static final List<DitaClass> titlealtsOrder = Collections.unmodifiableList(Arrays.asList(
            TOPIC_NAVTITLE,
            MAP_SEARCHTITLE,
            TOPIC_SEARCHTITLE
    ));
    private static final List<DitaClass> prologPosition = Collections.unmodifiableList(Arrays.asList(
            TOPIC_TITLE,
            TOPIC_TITLEALTS,
            TOPIC_SHORTDESC,
            TOPIC_ABSTRACT
    ));
    private static final List<DitaClass> prologOrder = Collections.unmodifiableList(Arrays.asList(
            TOPIC_AUTHOR,
            TOPIC_SOURCE,
            TOPIC_PUBLISHER,
            TOPIC_COPYRIGHT,
            TOPIC_CRITDATES,
            TOPIC_PERMISSIONS,
            TOPIC_METADATA,
            TOPIC_RESOURCEID,
            TOPIC_DATA,
            TOPIC_DATA_ABOUT,
            TOPIC_FOREIGN,
            TOPIC_UNKNOWN
    ));
    private static final List<DitaClass> metadataPosition = Collections.unmodifiableList(Arrays.asList(
            TOPIC_AUTHOR,
            TOPIC_SOURCE,
            TOPIC_PUBLISHER,
            TOPIC_COPYRIGHT,
            TOPIC_CRITDATES,
            TOPIC_PERMISSIONS
    ));
    private static final List<DitaClass> metadataOrder = Collections.unmodifiableList(Arrays.asList(
            TOPIC_AUDIENCE,
            TOPIC_CATEGORY,
            TOPIC_KEYWORDS,
            TOPIC_PRODINFO,
            TOPIC_OTHERMETA
    ));

    public Document process(final Document doc) {
        final Element root = getMatchingTopicElement(doc.getDocumentElement());
        if (root == null) {
            return doc;
        }
        if (hasMetadata(titlealtsOrder)) {
            final Element titlealts = findMetadataContainer(root, titlealtsPosition, TOPIC_TITLEALTS);
            processMetadata(titlealts, titlealtsOrder);
        }
        if (hasMetadata(prologOrder) || hasMetadata(metadataOrder)) {
            final Element prolog = findMetadataContainer(root, prologPosition, TOPIC_PROLOG);
            processMetadata(prolog, prologOrder);
            if (hasMetadata(metadataOrder)) {
                final Element metadata = findMetadataContainer(prolog, metadataPosition, TOPIC_METADATA);
                processMetadata(metadata, metadataOrder);
            }
        }
        return doc;
    }

}
