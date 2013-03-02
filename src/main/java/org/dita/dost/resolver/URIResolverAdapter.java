/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2010 All Rights Reserved.
 */
package org.dita.dost.resolver;

import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.InputSource;
/**
 * URIResolverAdapter class, convert source into inputsteam.
 */
public final class URIResolverAdapter {
    
    /**
     * Private default constructor to make class uninstantiable.
     */
    private URIResolverAdapter() {
        // nop
    }

    /**
     * Translate Source object to InputSource object.
     * @param source target object
     * @return InputSource instance if target object is instance of either SAXSource, DOMSource, StreamSource
     *         or their derived class. null, if not.
     */
    @Deprecated
    public static InputSource convertToInputSource(final Source source) {
        if(source==null){
            return null;
        }

        if (source instanceof SAXSource) {
            return ((SAXSource) source).getInputSource();
        } else if (source instanceof StreamSource) {
            final StreamSource ss = (StreamSource) source;
            final InputSource isource = new InputSource(ss.getInputStream());
            isource.setByteStream(ss.getInputStream());
            isource.setCharacterStream(ss.getReader());
            return isource;
        } else if (source instanceof DOMSource) {
            final StringWriter writer = new StringWriter();
            try {
                TransformerFactory.newInstance().newTransformer().transform(source, new StreamResult(writer));
                return new InputSource(new StringReader(writer.toString()));
            } catch (final Exception e) {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Translate Source object to InputStream object.
     * @param source target object
     * @return InputStream instance if target object is instance of either SAXSource, DOMSource, StreamSource
     *         or their derived class. null, if not.
     */
    @Deprecated
    public static InputStream convertTOInputStream(final Source source){
        final InputSource result=convertToInputSource(source);
        if(result==null){
            return null;
        }
        return result.getByteStream();
    }
}
