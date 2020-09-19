/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2005, 2006 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.

 */
package org.dita.dost.util;

import static org.dita.dost.util.Constants.*;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.Properties;
import java.util.Vector;

import org.xml.sax.InputSource;
import org.xmlresolver.Catalog;
import org.xmlresolver.CatalogSource;
import org.xmlresolver.CatalogSource.InputSourceCatalogSource;
import org.xmlresolver.Resolver;

/**
 * General catalog file resolving utilities.
 * @version 1.0 2005-4-11
 * @author Zhang, Yuan Peng
 */

public final class CatalogUtils {

    /**apache catalogResolver.*/
    private static Resolver catalogResolver = null;
    /** Absolute directory to find catalog-dita.xml.*/
    private static File ditaDir;
    /**
     * Instances should NOT be constructed in standard programming.
     */
    private CatalogUtils() {
        // leave blank as designed
    }

    /**
     * Set directory to find catalog-dita.xml.
     * @param ditaDir ditaDir
     */
    public static synchronized void setDitaDir(final File ditaDir) {
        catalogResolver = null;
        CatalogUtils.ditaDir = ditaDir;
    }

    /**
     * Get Resolver.
     * @return Resolver
     */
    public static synchronized Resolver getCatalogResolver() {
        if (catalogResolver == null) {
            final Properties properties = new Properties();
            properties.setProperty("prefer", "public");

            final org.xmlresolver.Configuration configuration = new org.xmlresolver.Configuration(properties, null);
            final File catalogFilePath = new File(ditaDir, Configuration.pluginResourceDirs.get("org.dita.base") + File.separator + FILE_NAME_CATALOG);
            final InputSource inputSource = new InputSource(catalogFilePath.toURI().toString());
            final Vector<CatalogSource> catalogs = new Vector<>();
            catalogs.add(new InputSourceCatalogSource(inputSource));

            final Catalog catalog = new Catalog(configuration, catalogs);

            catalogResolver = new Resolver(catalog);
        }

        return catalogResolver;
    }
}

