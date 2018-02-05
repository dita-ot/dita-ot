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

import org.apache.xml.resolver.CatalogManager;
import org.apache.xml.resolver.tools.CatalogResolver;

/**
 * General catalog file resolving utilities.
 * @version 1.0 2005-4-11
 * @author Zhang, Yuan Peng
 */

public final class CatalogUtils {

    /**apache catalogResolver.*/
    private static CatalogResolver catalogResolver = null;
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
     * Get CatalogResolver.
     * @return CatalogResolver
     */
    public static synchronized CatalogResolver getCatalogResolver() {
        if (catalogResolver == null) {
            final CatalogManager manager = new CatalogManager();
            manager.setIgnoreMissingProperties(true);
            manager.setUseStaticCatalog(false); // We'll use a private catalog.
            manager.setPreferPublic(true);
            final File catalogFilePath = new File(ditaDir, Configuration.pluginResourceDirs.get("org.dita.base") + File.separator + FILE_NAME_CATALOG);
            manager.setCatalogFiles(catalogFilePath.toURI().toASCIIString());
            //manager.setVerbosity(10);
            catalogResolver = new CatalogResolver(manager);
        }

        return catalogResolver;
    }
}

