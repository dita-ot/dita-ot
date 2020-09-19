/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2020 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.apache.xml.resolver;

import org.xmlresolver.CatalogSource;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static org.dita.dost.util.CatalogUtils.getCatalogResolver;

public class Catalog {

    /** The PUBLIC Catalog Entry type. */
    public static final int PUBLIC = 0;
    /** The URI Catalog Entry type. */
    public static final int URI = 1;

    /** The catalog manager in use for this instance. */
    protected CatalogManager catalogManager;
    /** The base URI for relative system identifiers in the catalog. */
    protected URL base;

    private org.xmlresolver.Catalog catalog = new org.xmlresolver.Catalog();

    public void parseCatalog(String file) throws MalformedURLException, IOException {
        catalog.addSource(new CatalogSource.UriCatalogSource(file));
    }

    protected Catalog newCatalog() {
        return new Catalog();
    }

    public void addEntry(final CatalogEntry entry) {

    }

    /** Perform character normalization on a URI reference. */
    protected String normalizeURI(String uriref) {
        return uriref;
    }

}
