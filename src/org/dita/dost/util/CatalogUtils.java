/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2005, 2006 All Rights Reserved.
 */
package org.dita.dost.util;


import java.io.File;
import java.util.HashMap;

import org.apache.xml.resolver.Catalog;
import org.apache.xml.resolver.CatalogManager;
import org.apache.xml.resolver.tools.CatalogResolver;
import org.dita.dost.log.DITAOTJavaLogger;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;



/**
 * Class description goes here.
 * 
 * @version 1.0 2005-4-11
 * @author Zhang, Yuan Peng
 */

public class CatalogUtils {

    private static HashMap<String, String> map=null;
    
    private static DITAOTJavaLogger logger = new DITAOTJavaLogger();
    
    public static CatalogResolver catalogResolver = null;
    
    /**
     * 
     */
    private CatalogUtils() {
        super();
    }

    /**
     * Parse the catalog file.
     * @param ditaDir
     * @return
     * 
     */
    public static HashMap<String, String> getCatalog(String ditaDir){
    	CatalogParser parser;
		XMLReader reader;
        String catalogFilePath;
    	
        if (map!=null){
            return map;
        }else{
            map = new HashMap<String, String>();
            parser = new CatalogParser(map, ditaDir);
            try{
                if (System.getProperty(Constants.SAX_DRIVER_PROPERTY) == null){
                    //The default sax driver is set to xerces's sax driver
                	StringUtils.initSaxDriver();
                }
                reader = XMLReaderFactory.createXMLReader();
                reader.setContentHandler(parser);
                catalogFilePath = (ditaDir == null) ? Constants.FILE_NAME_CATALOG : ditaDir+File.separator+Constants.FILE_NAME_CATALOG;
                reader.parse(catalogFilePath);
            }catch (Exception e){
                logger.logException(e);
            }
            
            return map;
        }
    }
    public static void initCatalogResolver(String ditaDir) {
        if (catalogResolver == null) {
            CatalogManager manager = new CatalogManager();
            manager.setIgnoreMissingProperties(true);
            manager.setUseStaticCatalog(false); // We'll use a private catalog.
            manager.setPreferPublic(true);
            //manager.setVerbosity(10);

            catalogResolver = new CatalogResolver(manager);

            String catalogFilePath = (ditaDir == null)? 
                Constants.FILE_NAME_CATALOG : 
                ditaDir + File.separator + Constants.FILE_NAME_CATALOG;

            Catalog catalog = catalogResolver.getCatalog();
            try {
                catalog.parseCatalog(catalogFilePath);
            } catch (Exception e) {
                logger.logException(e);
            }
        }
        
    }

    public static CatalogResolver getCatalogResolver() {
        if (catalogResolver == null) 
            initCatalogResolver(null);

        return catalogResolver;
    }
        
    
}

