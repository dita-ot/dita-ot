/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */
package org.dita.dost.util;

import static javax.xml.XMLConstants.NULL_NS_URI;
import static org.dita.dost.util.Constants.*;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.MessageUtils;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;

/**
 * XML utility methods.
 * 
 * @since 1.5.4
 * @author Jarno Elovirta
 */
public final class XMLUtils {

    /** Private constructor to make class uninstantiable. */
    private XMLUtils() {}

    /**
     * Add or set attribute.
     * 
     * @param atts attributes
     * @param uri namespace URI
     * @param localName local name
     * @param qName qualified name
     * @param type attribute type
     * @param value attribute value
     */
    public static void addOrSetAttribute(final AttributesImpl atts, final String uri, final String localName,
            final String qName, final String type, final String value) {
        final int i = atts.getIndex(qName);
        if (i != -1) {
            atts.setAttribute(i, uri, localName, qName, type, value);
        } else {
            atts.addAttribute(uri, localName, qName, type, value);
        }
    }

    /**
     * Add or set attribute. Convenience method for {@link #addOrSetAttribute(AttributesImpl, String, String, String, String, String)}.
     * 
     * @param atts attributes
     * @param localName local name
     * @param value attribute value
     */
    public static void addOrSetAttribute(final AttributesImpl atts, final String localName, final String value) {
        addOrSetAttribute(atts, NULL_NS_URI, localName, localName, "CDATA", value);
    }

    /**
     * Add or set attribute. Convenience method for {@link #addOrSetAttribute(AttributesImpl, String, String, String, String, String)}.
     * 
     * @param atts attributes
     * @param att attribute node
     */
    public static void addOrSetAttribute(final AttributesImpl atts, final Node att) {
        if (att.getNodeType() != Node.ATTRIBUTE_NODE) {
            throw new IllegalArgumentException();
        }
        final Attr a = (Attr) att;
        String localName = a.getLocalName();
        if (localName == null) {
            localName = a.getName();
            final int i = localName.indexOf(':');
            if (i != -1) {
                localName = localName.substring(i + 1);
            }
        }
        addOrSetAttribute(atts,
                a.getNamespaceURI() != null ? a.getNamespaceURI() : NULL_NS_URI,
                        localName,
                                a.getName() != null ? a.getName() : localName,
                                        a.isId() ? "ID" : "CDATA",
                                                a.getValue());
    }

    /**
     * Remove an attribute from the list. Do nothing if attribute does not exist.
     * 
     * @param atts attributes
     * @param qName QName of the attribute to remove
     */
    public static void removeAttribute(final AttributesImpl atts, final String qName) {
        final int i = atts.getIndex(qName);
        if (i != -1) {
            atts.removeAttribute(i);
        }
    }
    
    /**
     * Get element node string value.
     * 
     * @param element element to get string value for
     * @return concatenated text node descendant values
     */
    public static String getStringValue(final Element element) {
        final StringBuilder buf = new StringBuilder();
        final NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            final Node n = children.item(i);
            switch (n.getNodeType()) {
            case Node.TEXT_NODE:
                buf.append(n.getNodeValue());
                break;
            case Node.ELEMENT_NODE:
                buf.append(getStringValue((Element) n));
                break;
            }
        }
        return buf.toString();
    }
    
    /**
     * Transform file with XML filters.
     * 
     * @param inputFile file to transform and replace
     * @param filters XML filters to transform file with
     */
    public static void transform(final File inputFile, final List<XMLFilter> filters) throws DITAOTException {
        final File outputFile = new File(inputFile.getAbsolutePath() + FILE_EXTENSION_TEMP);
        InputStream in = null;
        OutputStream out = null;
        try {
            final Transformer transformer = TransformerFactory.newInstance().newTransformer();
            XMLReader reader = StringUtils.getXMLReader();
            for (final XMLFilter filter: filters) {
                // ContentHandler must be reset so e.g. Saxon 9.1 will reassign ContentHandler
                // when reusing filter with multiple Transformers.
                filter.setContentHandler(null);
                filter.setParent(reader);
                reader = filter;
            }
            in = new BufferedInputStream(new FileInputStream(inputFile));
            out = new BufferedOutputStream(new FileOutputStream(outputFile));
            final Source source = new SAXSource(reader, new InputSource(in));
            final Result result = new StreamResult(out);
            transformer.transform(source, result);
        } catch (final Exception e) {
            throw new DITAOTException("Failed to transform " + inputFile + ": " + e.getMessage(), e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (final IOException e) {
                    // ignore
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (final IOException e) {
                    // ignore
                }
            }
        }
        // replace original file
        try {
            if (!inputFile.delete()) {
                throw new DITAOTException("Failed to delete " + outputFile);
            }
            if (!outputFile.renameTo(inputFile)) {
                throw new DITAOTException("Failed to move " + inputFile);
            }
        } catch (final Exception e) {
            throw new DITAOTException("Failed to replace " + inputFile + ": " + e.getMessage());
        }
    }
    
    /**
     * Convenience builder for {@link org.xml.sax.Attributes SAX Attributes}.
     */
    public static final class AttributesBuilder {
    	
    	final AttributesImpl atts;
    	
    	/**
    	 * Construct empty attributes builder.
    	 */
    	public AttributesBuilder() {
    		atts = new AttributesImpl();
    	}
    	
    	/**
    	 * Construct attributes builder with initial attribute set.
    	 * 
    	 * @param atts initial attributes
    	 */
    	public AttributesBuilder(final Attributes atts) {
    		this.atts = new AttributesImpl(atts);
    	}
    	
        /**
         * Add or set attribute.
         * 
         * @param uri namespace URI
         * @param localName local name
         * @param qName qualified name
         * @param type attribute type
         * @param value attribute value
         * @return this builder
         */
        public AttributesBuilder add(final String uri, final String localName,
                final String qName, final String type, final String value) {
            final int i = atts.getIndex(uri, localName);
            if (i != -1) {
                atts.setAttribute(i, uri, localName, qName, type, value);
            } else {
                atts.addAttribute(uri, localName, qName, type, value);
            }
            return this;
        }
        
        /**
         * Add or set attribute. Convenience method for {@link #add(String, String, String, String, String)}.
         * 
         * @param localName local name
         * @param value attribute value
         * @return this builder
         */
        public AttributesBuilder add(final String localName, final String value) {
            return add(NULL_NS_URI, localName, localName, "CDATA", value);
        }
    	
        /**
         * Add or set attribute. Convenience method for {@link #add(String, String, String, String, String)}.
         * 
         * @param uri namespace URI
         * @param localName local name
         * @param value attribute value
         * @return this builder
         */
        public AttributesBuilder add(final String uri, final String localName, final String value) {
            return add(uri, localName, localName, "CDATA", value);
        }
        
        /**
         * Add or set all attributes.
         * 
         * @param attrs attributes to add or set
         */
        public void addAll(final Attributes attrs) {
            for (int i = 0; i < attrs.getLength(); i++) {
                add(attrs.getURI(i), attrs.getLocalName(i), attrs.getQName(i), attrs.getType(i), attrs.getValue(i));
            }
        }
        
        /**
         * Returns a newly-created Attributes based on the contents of the builder.
         * @return new attributes
         */
    	public Attributes build() {
    		return new AttributesImpl(atts);
    	}
    	
    }
    
}
