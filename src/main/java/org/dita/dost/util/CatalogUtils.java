/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2005, 2006 All Rights Reserved.
 */
package org.dita.dost.util;

import static org.dita.dost.util.Constants.*;

import java.io.File;
import java.util.HashMap;

import org.apache.xml.resolver.Catalog;
import org.apache.xml.resolver.CatalogManager;
import org.apache.xml.resolver.tools.CatalogResolver;
import org.dita.dost.log.DITAOTJavaLogger;
import org.xml.sax.XMLReader;

/**
 * General catalog file resolving utilities.
 * @version 1.0 2005-4-11
 * @author Zhang, Yuan Peng
 */

public final class CatalogUtils {
    /**map to keep the resolved catalog mappings.*/
    private static HashMap<String, String> map=null;
    /**logger to log informations.*/
    private static DITAOTJavaLogger logger = new DITAOTJavaLogger();
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
     * Parse the catalog file to get catalog map.
     * @param ditaDir absolute path to directory to find catalog-dita.xml
     * @return catalog map
     * @deprecated use Apache Commons Catalog Resolver instead
     */
    @Deprecated
    public static synchronized HashMap<String, String> getCatalog(final File ditaDir) {
        if (map != null) {
            return map;
        }

        final File catalogFilePath = (ditaDir == null) ? new File(FILE_NAME_CATALOG) : new File(ditaDir, FILE_NAME_CATALOG);

        map = new HashMap<String, String>();
        final CatalogParser parser = new CatalogParser(map, ditaDir.getAbsolutePath());
        try {
            final XMLReader reader = StringUtils.getXMLReader();
            reader.setContentHandler(parser);
            reader.parse(catalogFilePath.toURI().toASCIIString());
        } catch (final Exception e) {
            logger.logException(e);
        }

        return map;
    }

    /**
     * Set directory to find catalog-dita.xml.
     * @param ditaDir ditaDir
     */
    public static synchronized void setDitaDir(final File ditaDir){
        catalogResolver=null;
        CatalogUtils.ditaDir=ditaDir;
    }
    /**
     * Get the current set directory to find catalog-dita.xml.
     * @return ditaDir, empty string if ditaDir is set to null or "".
     * @deprecated access ditaDir directly
     */
    @Deprecated
    public static File getDitaDir(){
        return ditaDir;
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

            //manager.setVerbosity(10);
            catalogResolver = new CatalogResolver(manager);

            final File catalogFilePath = new File(ditaDir, FILE_NAME_CATALOG);

            final Catalog catalog = catalogResolver.getCatalog();
            try {
                catalog.parseCatalog(catalogFilePath.toURI().toURL());
            } catch (final Exception e) {
                logger.logException(e);
            }
        }

        return catalogResolver;
    }
}

