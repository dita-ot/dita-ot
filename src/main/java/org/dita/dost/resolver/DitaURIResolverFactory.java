/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2010 All Rights Reserved.
 */
package org.dita.dost.resolver;

import java.io.File;
import java.io.FileInputStream;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXSource;

import org.xml.sax.InputSource;

/**
 * @author Alan
 * 
 * Hold resolver that you actually used. If no URIResolver is specified,
 * an anonymous DITA-OT default resolver is used.
 * 
 * Usage: DitaURIResolverFactory.getURIResolver().resolve(href, base);
 * 
 */
public final class DitaURIResolverFactory {
        
    private static URIResolver resolver = null;
    private static String path = null;
    static {
        // DITA-OT default URIResolver
        /**
         * The href parameter can be either absolute or relative path. If
         * relative path is encountered, this function will tend to change it
         * into an absolute path according to the basedir and tempdir defined in the
         * program. Then it will use this absolute path to find the
         * file. If no such file in the specific path is found or something goes
         * wrong while trying to open the file, null is returned.
         */
        resolver = new URIResolver() {
            @Override
            public Source resolve(final String href, final String base) throws TransformerException {
                File file = new File(href);
                if (!file.isAbsolute()) {
                    String parentDir=null;
                    if(base == null){
                        parentDir=path;
                    }
                    else{
                        parentDir=new File(base).getAbsolutePath();
                    }
                    file = new File(parentDir, href);
                }
                try {
                    return new SAXSource(new InputSource(new FileInputStream(file)));
                } catch (final Exception e) {
                    return null;
                }
            }
        };
    }
    
    /**
     * Private default constructor to make class uninstantiable.
     */
    private DitaURIResolverFactory() {
    }
    
    /**
     * Get URIResolver.
     * @return resolver
     */
    public static URIResolver getURIResolver() {
        return resolver;
    }
    /**
     * Set URIResolver.
     * @param resolver URIResolver
     */
    public static void setURIResolver(final URIResolver resolver) {
        DitaURIResolverFactory.resolver = resolver;
    }
    /**
     * Set DitaURIResolverFactory's path to create resolver.
     * @param path path
     */
    public static void setPath(final String path) {
        DitaURIResolverFactory.path = path;
    }
}
