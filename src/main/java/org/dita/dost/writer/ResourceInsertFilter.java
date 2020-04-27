/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2019 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.writer;

import org.dita.dost.util.XMLUtils.AttributesBuilder;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.net.URI;
import java.util.List;

import static javax.xml.XMLConstants.NULL_NS_URI;
import static org.dita.dost.util.Constants.*;

public final class ResourceInsertFilter extends AbstractXMLFilter {

    private boolean added = false;
    private List<URI> resources;

    public void setResources(final List<URI> resources) {
        this.resources = resources;
    }

    // SAX methods

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes atts)
            throws SAXException {
        super.startElement(uri, localName, qName, atts);
        if (!added) {
            for (URI resource : resources) {
                logger.info("add resource " + resource);
                super.startElement(NULL_NS_URI, MAP_TOPICREF.localName, MAP_TOPICREF.localName,
                        getAttributes()
                                .add(ATTRIBUTE_NAME_HREF, currentFile.resolve(".").relativize(resource).toString())
                                .add(ATTRIBUTE_NAME_PROCESSING_ROLE, ATTR_PROCESSING_ROLE_VALUE_RESOURCE_ONLY)
//                                .add(ATTRIBUTE_NAME_FORMAT, FIXME)
                                .build());
                super.endElement(NULL_NS_URI, MAP_TOPICREF.localName, MAP_TOPICREF.localName);
            }
            added = true;
        }
    }

    private AttributesBuilder getAttributes() {
        return new AttributesBuilder().add(ATTRIBUTE_NAME_CLASS, MAP_TOPICREF.toString());
    }

}
