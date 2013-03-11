/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2010 All Rights Reserved.
 */
package org.dita.dost.util;

import static org.dita.dost.util.Constants.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.xml.transform.TransformerException;

import org.dita.dost.log.DITAOTJavaLogger;
import org.dita.dost.resolver.DitaURIResolverFactory;
import org.dita.dost.resolver.URIResolverAdapter;

/**
 * 
 * List Utils.
 *
 * @deprecated use {@link org.dita.dost.util.Job} instead
 */
@Deprecated
public final class ListUtils {
    /**
     * Private Constructor.
     */
    private ListUtils(){
        // nop
    }

    /**
     * 1. Try reading from dita.xml.properties.
     * 2. or, read dita.list.
     * 3. or, log exceptions.
     * @return Properties
     * @throws IOException IOException.
     * @deprecated use {@link org.dita.dost.util.Job} instead
     */
    @Deprecated
    public static Properties getDitaList() throws IOException{
        final Properties properties = new Properties();
        try{
            InputStream source = URIResolverAdapter.convertTOInputStream(DitaURIResolverFactory.getURIResolver().resolve(FILE_NAME_DITA_LIST_XML, null));
            if (source != null) {
                properties.loadFromXML(source);
            }
            else{
                source = URIResolverAdapter.convertTOInputStream(DitaURIResolverFactory.getURIResolver().resolve(FILE_NAME_DITA_LIST, null));
                if (source != null) {
                    properties.load(source);
                } else {
                    throw new IllegalStateException("List file " + FILE_NAME_DITA_LIST_XML + " or " + FILE_NAME_DITA_LIST + " not found");
                }
            }
        }catch(final TransformerException e){
            final DITAOTJavaLogger javaLogger = new DITAOTJavaLogger();
            javaLogger.logError(e.getMessage(), e) ;
        }
        return properties;
    }

}
