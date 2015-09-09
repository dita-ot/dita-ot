/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */
package org.dita.dost.util;

import static javax.xml.XMLConstants.*;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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
 * <p>When possible, use {@link javax.xml.stream.XMLStreamWriter XMLStreamWriter} instead.</p>
 * 
 * <p><strong>Not thread-safe.</strong></p>
 * 
 * @since 1.5.3
 * @author Jarno Elovirta
 */
public class XMLSerializer {

    // Constants ---------------------------------------------------------------

    private static final Attributes EMPTY_ATTS = new AttributesImpl();

    // Variables ---------------------------------------------------------------

    private final TransformerHandler transformer;

    private OutputStream outStream;
    private Writer outWriter;

    private final LinkedList<QName> elementStack = new LinkedList<>();
    private AttributesImpl openAttributes;
    private boolean openStartElement;

    // Constructors ------------------------------------------------------------

    private XMLSerializer(final OutputStream out) {
        outStream = out;
        transformer = initializeTransformerHandler();
        transformer.setResult(new StreamResult(out));
    }

    private XMLSerializer(final Writer out) {
        outWriter = out;
        transformer = initializeTransformerHandler();
        transformer.setResult(new StreamResult(out));
    }

    private TransformerHandler initializeTransformerHandler() throws TransformerFactoryConfigurationError {
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
        while (!elementStack.isEmpty()) {
            writeEndElement();
        }
        transformer.endDocument();
    }

    /**
     * Write start element without attributes.
     * 
     * @param qName element QName
     * @throws SAXException if processing the event failed
     */
    public void writeStartElement(final String qName) throws SAXException {
        writeStartElement(null, qName);
    }

    /**
     * Write start element without attributes.
     * 
     * @param qName element QName
     * @throws SAXException if processing the event failed
     */
    public void writeStartElement(final String uri, final String qName) throws SAXException {
        processStartElement();
        final QName res = new QName(uri, qName);
        addNamespace(res.uri, res.prefix, res);
        elementStack.addFirst(res); // push
        openStartElement = true;
    }

    /**
     * Write namepace prefix.
     * 
     * @param prefix namespace prefix
     * @param uri namespace URI
     * @throws SAXException if processing the event failed
     * @throws IllegalStateException if start element is not open
     * @throws IllegalArgumentException if prefix is already bound
     */
    public void writeNamespace(final String prefix, final String uri) {
        if (!openStartElement) {
            throw new IllegalStateException("Current state does not allow Namespace writing");
        }
        final QName qName = elementStack.getFirst(); // peek
        for (final NamespaceMapping p: qName.mappings) {
            if (p.prefix.equals(prefix) && p.uri.equals(uri)) {
                return;
            } else if (p.prefix.equals(prefix)) {
                throw new IllegalArgumentException("Prefix " + prefix + " already bound to " + uri);
            }
        }
        qName.mappings.add(new NamespaceMapping(prefix, uri, true));
    }

    /**
     * Write attribute
     * 
     * @param qName attribute name
     * @param value attribute value
     * @throws SAXException if processing the event failed
     * @throws IllegalStateException if start element is not open
     */
    public void writeAttribute(final String qName, final String value) throws SAXException {
        writeAttribute(NULL_NS_URI, qName, value);
    }

    /**
     * Write attribute
     * 
     * @param uri namespace URI
     * @param qName attribute name
     * @param value attribute value
     * @throws SAXException if processing the event failed
     * @throws IllegalStateException if start element is not open
     */
    public void writeAttribute(final String uri, final String qName, final String value) {
        if (!openStartElement) {
            throw new IllegalStateException("Current state does not allow Attribute writing");
        }
        if (openAttributes == null) {
            openAttributes = new AttributesImpl();
        }
        final QName att = new QName(uri, qName);
        addNamespace(uri, att.prefix, elementStack.getFirst()); // peek
        openAttributes.addAttribute(uri, att.localName, qName, "CDATA", value);
    }

    /**
     * Write end element.
     * 
     * @throws SAXException if processing the event failed
     */
    public void writeEndElement() throws SAXException {
        processStartElement();
        final QName qName = elementStack.remove(); // pop
        transformer.endElement(qName.uri, qName.localName, qName.qName);
        for (final NamespaceMapping p: qName.mappings) {
            if (p.newMapping) {
                transformer.endPrefixMapping(p.prefix);
            }
        }
    }

    /**
     * Write characters.
     * 
     * @param text character data
     * @throws SAXException if processing the event failed
     * @throws IllegalStateException if start element is not open
     */
    public void writeCharacters(final String text) throws SAXException {
        if (elementStack.isEmpty()) {
            throw new IllegalStateException("Current state does not allow Character writing");
        }
        final char[] ch = text.toCharArray();
        writeCharacters(ch, 0, ch.length);
    }

    /**
     * Write characters.
     * 
     * @param ch character data array
     * @param start start index
     * @param length length data to write
     * @throws SAXException if processing the event failed
     * @throws IllegalStateException if start element is not open
     */
    public void writeCharacters(final char[] ch, final int start, final int length) throws SAXException {
        if (elementStack.isEmpty()) {
            throw new IllegalStateException("Current state does not allow Character writing");
        }
        processStartElement();
        transformer.characters(ch, start, length);
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
            final QName qName = elementStack.getFirst(); // peek
            for (final NamespaceMapping p: qName.mappings) {
                if (p.newMapping) {
                    transformer.startPrefixMapping(p.prefix, p.uri);
                }
            }
            final Attributes atts = openAttributes != null ? openAttributes : EMPTY_ATTS;
            transformer.startElement(qName.uri, qName.localName, qName.qName, atts);
            openStartElement = false;
            openAttributes = null;
        }
    }

    private void addNamespace(final String uri, final String prefix, final QName current) {
        if (uri != null && uri.equals(NULL_NS_URI) && !prefix.equals(DEFAULT_NS_PREFIX)) {
            throw new IllegalArgumentException("Undeclaring prefix " + prefix + " not allowed");
        }
        if (uri != null) {
            // attempt to find apping in stack
            boolean found = false;
            stack: for (final QName e: elementStack) {
                for (final NamespaceMapping m: e.mappings) {
                    if (m.uri.equals(uri) && m.prefix.equals(prefix)) {
                        found = true;
                        break stack;
                    } else if (m.prefix.equals(prefix)) {
                        break stack;
                    }
                }
            }
            // skip xmlns=""
            if (!found && uri.equals(NULL_NS_URI) && prefix.equals(DEFAULT_NS_PREFIX)) {
                return;
            }
            current.mappings.add(new NamespaceMapping(prefix, uri, !found));
        }
    }

    // Private inner classes ---------------------------------------------------

    private static final class QName {

        final String uri;
        final String localName;
        final String prefix;
        final String qName;
        final List<NamespaceMapping> mappings;

        QName(final String uri, final String qName) {
            final int i = qName.indexOf(':');
            this.uri = uri != null ? uri : DEFAULT_NS_PREFIX;
            localName = i != -1 ? qName.substring(i + 1) : qName;
            prefix = i != -1 ? qName.substring(0, i) : DEFAULT_NS_PREFIX;
            this.qName = qName;
            mappings = new ArrayList<>(5);
        }

    }

    private static final class NamespaceMapping {

        final String prefix;
        final String uri;
        final boolean newMapping;

        NamespaceMapping(final String prefix, final String uri, final boolean newMapping) {
            this.prefix = prefix;
            this.uri = uri;
            this.newMapping = newMapping;
        }

    }

}
