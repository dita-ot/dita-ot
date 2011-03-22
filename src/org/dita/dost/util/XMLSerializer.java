/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2011 All Rights Reserved.
 */
package org.dita.dost.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Stack;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * XML serializer. Users a {@link javax.xml.transform.sax.TransformerHandler
 * TransformerHandler} as the underlying serializer.
 * 
 * <p><strong>Not thread-safe.</strong></p>
 * 
 * @since 1.5.3
 */
public class XMLSerializer {

    // Constants ---------------------------------------------------------------

    private static final Attributes EMPTY_ATTS = new AttributesImpl();

    // Variables ---------------------------------------------------------------

    private final TransformerHandler transformer;

    private OutputStream outStream;
    private Writer outWriter;

    private final Stack<String> elementStack = new Stack<String>();
    private AttributesImpl openAttributes;
    private boolean openStartElement;

    // Constructors ------------------------------------------------------------

    private XMLSerializer(final OutputStream out) {
        this.outStream = out;
        transformer = initializeTransformerHandler();
        transformer.setResult(new StreamResult(out));
    }

    private XMLSerializer(final Writer out) {
        this.outWriter = out;
        transformer = initializeTransformerHandler();
        transformer.setResult(new StreamResult(out));
    }

    private TransformerHandler initializeTransformerHandler() throws TransformerFactoryConfigurationError {
        final TransformerHandler t = null;
        final TransformerFactory tf = TransformerFactory.newInstance();
        if (tf.getFeature(SAXTransformerFactory.FEATURE)) {
            final SAXTransformerFactory stf = (SAXTransformerFactory) tf;
            try {
                return stf.newTransformerHandler();
            } catch (final TransformerConfigurationException e) {
                throw new RuntimeException("Unable to create an XML serializer: " + e.getMessage(), e);
            }
        }
        throw new RuntimeException(
                "Unable to create an XML serializer: transformer factor does not support transformer handler");
    }

    /**
     * Get serializer instance.
     * 
     * @param out output stream
     */
    public static XMLSerializer newInstance(final OutputStream out) {
        return new XMLSerializer(out);
    }

    /**
     * Get serializer instance.
     * 
     * @param out output writer
     */
    public static XMLSerializer newInstance(final Writer out) {
        return new XMLSerializer(out);
    }

    // Public methods ----------------------------------------------------------

    /**
     * Get underlying serializer.
     * 
     * @return serialization handler
     */
    public TransformerHandler getTransformerHandler() {
        return transformer;
    }

    /**
     * Close output.
     * 
     * @throws IOException if closing result output failed
     */
    public void close() throws IOException {
        if (outStream == null && outWriter == null) {
            throw new IllegalStateException();
        }
        if (outStream != null) {
            outStream.close();
        }
        if (outWriter != null) {
            outWriter.close();
        }
    }

    /**
     * Start document.
     * 
     * @throws SAXException if processing the event failed
     */
    public void writeStartDocument() throws SAXException {
        transformer.startDocument();
    }

    /**
     * End document.
     * 
     * @throws SAXException if processing the event failed
     */
    public void writeEndDocument() throws SAXException {
        transformer.endDocument();
    }

    /**
     * Writer start element without attributes.
     * 
     * @param qName element QName
     * @throws SAXException if processing the event failed
     */
    public void writeStartElement(final String qName) throws SAXException {
        processStartElement();
        elementStack.push(qName);
        openStartElement = true;
    }

    /**
     * Write attribute
     * 
     * @param qName attribute name
     * @param atts attribute value
     * @throws SAXException if processing the event failed
     */
    public void writeAttribute(final String qName, final String value) throws SAXException {
        if (openAttributes == null) {
            openAttributes = new AttributesImpl();
        }
        openAttributes.addAttribute("", qName, qName, "CDATA", value);
    }

    /**
     * Write end element.
     * 
     * @throws SAXException if processing the event failed
     */
    public void writeEndElement() throws SAXException {
        processStartElement();
        final String qName = elementStack.pop();
        transformer.endElement("", qName, qName);

    }

    /**
     * Write characters.
     * 
     * @param text character data
     * @throws SAXException if processing the event failed
     */
    public void writeCharacters(final String text) throws SAXException {
        processStartElement();
        final char[] ch = text.toCharArray();
        transformer.characters(ch, 0, ch.length);
    }

    /**
     * Write processing instruction.
     * 
     * @param target processing instruction name
     * @param data processing instruction data, {@code null} if no data
     * @throws SAXException if processing the event failed
     */
    public void writeProcessingInstruction(final String target, final String data) throws SAXException {
        processStartElement();
        transformer.processingInstruction(target, data != null ? data : "");
    }

    /**
     * Write comment.
     * 
     * @param data comment data
     * @throws SAXException if processing the event failed
     */
    public void writeComment(final String data) throws SAXException {
        processStartElement();
        final char[] ch = data.toCharArray();
        transformer.comment(ch, 0, ch.length);
    }

    // Private methods ---------------------------------------------------------

    private void processStartElement() throws SAXException {
        if (openStartElement) {
            final String qName = elementStack.peek();
            transformer.startElement("", qName, qName, openAttributes != null ? openAttributes : EMPTY_ATTS);
            openStartElement = false;
            openAttributes = null;
        }
    }

}
