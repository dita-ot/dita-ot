/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2010 All Rights Reserved.
 */
package org.dita.dost.util;

import static org.dita.dost.util.Constants.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
            javaLogger.logException(e);
        }
        return properties;
    }

    /**
     * @param name name
     * @param base base
     * @param isXML whether is xml format
     * @deprecated -never used right now
     * @return Properties
     * @throws IOException exception
     */
    @Deprecated
    public static Properties loadList(final String name, final String base, final boolean isXML) throws IOException{
        final Properties properties = new Properties();
        try {
            final InputStream source = URIResolverAdapter.convertTOInputStream(DitaURIResolverFactory.getURIResolver().resolve(name, base));
            if(isXML){
                properties.loadFromXML(source);
            }
            else{
                properties.load(source);
            }
        } catch (final TransformerException e) {
            final DITAOTJavaLogger javaLogger = new DITAOTJavaLogger();
            javaLogger.logException(e);
        }
        return properties;
    }

    /**
     * How?
     * @param name name
     * @param base base
     * @param isXML whether is xml format
     * @param properties properties
     * @throws IOException IOException
     * @deprecated -never used right now
     */
    @Deprecated
    public static void storeList(final String name, final String base, final boolean isXML, final Properties properties) throws IOException{
        OutputStream in = null;
        try {
            URIResolverAdapter.convertTOInputStream(DitaURIResolverFactory.getURIResolver().resolve(name, base));
            in = new FileOutputStream(name);
            if(isXML){
                properties.storeToXML(in, null);
            }
            else{
                properties.store(in, null);
            }
        } catch (final TransformerException e) {
            final DITAOTJavaLogger javaLogger = new DITAOTJavaLogger();
            javaLogger.logException(e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (final IOException e) {
                    final DITAOTJavaLogger javaLogger = new DITAOTJavaLogger();
                    javaLogger.logException(e);
                }
            }
        }
    }
}
