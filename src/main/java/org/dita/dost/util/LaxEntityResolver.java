/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2022 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.util;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xmlresolver.utils.URIUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class LaxEntityResolver implements EntityResolver {
    private final EntityResolver parent;

    public LaxEntityResolver(EntityResolver parent) {
        this.parent = parent;
    }

    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        String normalized = systemId;
        try {
            new URI(systemId);
        } catch (URISyntaxException e) {
            normalized = URIUtils.normalizeURI(systemId);
        }
        return parent.resolveEntity(publicId, normalized);
    }
}
