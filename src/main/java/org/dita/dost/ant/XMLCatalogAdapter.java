/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2023 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.ant;

import net.sf.saxon.lib.ResourceRequest;
import net.sf.saxon.lib.ResourceResolver;
import net.sf.saxon.trans.XPathException;
import org.apache.tools.ant.types.XMLCatalog;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;

/**
 * Adapter from {@link org.apache.tools.ant.types.XMLCatalog XMLCatalog} to {@link net.sf.saxon.lib.ResourceResolver ResourceResolver}.
 */
public class XMLCatalogAdapter implements ResourceResolver, URIResolver {

    private final XMLCatalog catalog;

    public XMLCatalogAdapter(XMLCatalog catalog) {
        this.catalog = catalog;
    }

    @Override
    public Source resolve(ResourceRequest request) throws XPathException {
        try {
            if (request.publicId != null) {
                final InputSource inputSource = catalog.resolveEntity(request.publicId, request.uri);
                if (inputSource.getByteStream() != null) {
                    return new StreamSource(inputSource.getByteStream(), inputSource.getSystemId());
                } else if (inputSource.getCharacterStream() != null) {
                    return new StreamSource(inputSource.getCharacterStream(), inputSource.getSystemId());
                } else {
                    return new StreamSource(inputSource.getSystemId());
                }
            } else {
                return catalog.resolve(request.relativeUri, request.baseUri);
            }
        } catch (TransformerException | IOException | SAXException e) {
            throw new XPathException(e);
        }
    }

    @Override
    public Source resolve(String href, String base) throws TransformerException {
        return catalog.resolve(href, base);
    }
}
