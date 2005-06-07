/*
 * @(#)CatalogUtils.java        1.0 2005-4-11
 *
 * 
 */
package org.dita.dost.util;


import java.util.HashMap;

import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;



/**
 * Class description goes here.
 * 
 * @version 1.0 2005-4-11
 * @author Zhang, Yuan Peng
 */

public class CatalogUtils {

    private static HashMap map=null;
    /**
     * 
     */
    public CatalogUtils() {
        super();
    }

    public static HashMap getCatalog(){
        if (map!=null){
            return map;
        }else{
            map = new HashMap();
            CatalogParser parser = new CatalogParser(map);
            try{
                if (System.getProperty("org.xml.sax.driver") == null){
                    //The default sax driver is set to xerces's sax driver
                    System.setProperty("org.xml.sax.driver","org.apache.xerces.parsers.SAXParser");
                }
                XMLReader reader = XMLReaderFactory.createXMLReader();
                reader.setContentHandler(parser);
                reader.parse("catalog-dita.xml");
            }catch (Exception e){
                e.printStackTrace(System.out);
            }
            
            return map;
        }
    }
    
    
}

