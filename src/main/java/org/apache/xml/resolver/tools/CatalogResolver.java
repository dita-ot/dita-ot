/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2020 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.apache.xml.resolver.tools;

import org.apache.tools.ant.types.XMLCatalog;
import org.apache.xml.resolver.Catalog;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import org.apache.tools.ant.types.resolver.ApacheCatalog;
import org.xml.sax.SAXException;
import org.xmlresolver.Resolver;

import java.io.IOException;

import static org.dita.dost.util.CatalogUtils.getCatalogResolver;

/**
 * An API comptible bridge to org.xmlresolver.
 */
public class CatalogResolver implements EntityResolver, URIResolver {

    private Resolver resolver;

    public CatalogResolver() {}

//    public CatalogResolver(boolean privateCatalog) {}
//    public CatalogResolver(CatalogManager manager) {}

    /** Return the underlying catalog */
    public Catalog getCatalog() {
        return new ApacheCatalog();
    }

    /** Implements the guts of the resolveEntity method for the SAX interface. */
    public String getResolvedEntity(String publicId, String systemId) {
        try {
            final InputSource inputSource = getCatalogResolver().resolveEntity(systemId, publicId);
            if (inputSource != null) {
                return inputSource.getSystemId();
            }
        } catch (SAXException | IOException e) {
            // Ignore
        }
        return null;
    }

    /** JAXP URIResolver API */
    @Override
    public Source resolve(String href, String base) throws TransformerException {
        return getCatalogResolver().resolve(href, base);
    }

    /** Implements the resolveEntity method for the SAX interface. */
    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        return getCatalogResolver().resolveEntity(systemId, publicId);
    }

    public void setXMLCatalog(XMLCatalog xmlCatalog) {
        System.err.println("setXMLCatalog " + xmlCatalog);
    }

    public void parseCatalog(String catalog) {
        System.out.println("parseCatalog " + catalog);
    }

}
