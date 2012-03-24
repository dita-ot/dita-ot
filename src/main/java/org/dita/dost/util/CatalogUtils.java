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
    /**directory to find catalog-dita.xml.*/
    private static String ditaDir;
    /**
     * Instances should NOT be constructed in standard programming.
     */
    private CatalogUtils() {
        // leave blank as designed
    }

    /**
     * Parse the catalog file to get catalog map.
     * @param ditaDir ditaDir to find catalog-dita.xml
     * @return catalog map
     * 
     */
    public static synchronized HashMap<String, String> getCatalog(final String ditaDir) {
        if (map != null) {
            return map;
        }

        final String catalogFilePath = (ditaDir == null) ? FILE_NAME_CATALOG : ditaDir + File.separator + FILE_NAME_CATALOG;

        map = new HashMap<String, String>();
        final CatalogParser parser = new CatalogParser(map, ditaDir);
        try {
            final XMLReader reader = StringUtils.getXMLReader();
            reader.setContentHandler(parser);
            reader.parse(catalogFilePath);
        } catch (final Exception e) {
            logger.logException(e);
        }

        return map;
    }

    /**
     * Set directory to find catalog-dita.xml.
     * @param ditaDir ditaDir
     */
    public static synchronized void setDitaDir(final String ditaDir){
        catalogResolver=null;
        CatalogUtils.ditaDir=ditaDir;
    }
    /**
     * Get the current set directory to find catalog-dita.xml.
     * @return ditaDir, empty string if ditaDir is set to null or "".
     */
    public static String getDitaDir(){
        if(StringUtils.isEmptyString(ditaDir)){
            return "";
        }
        return ditaDir+File.separator;
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

            final String catalogFilePath = getDitaDir() + FILE_NAME_CATALOG;

            final Catalog catalog = catalogResolver.getCatalog();
            try {
                catalog.parseCatalog(catalogFilePath);
            } catch (final Exception e) {
                logger.logException(e);
            }
        }

        return catalogResolver;
    }
}

