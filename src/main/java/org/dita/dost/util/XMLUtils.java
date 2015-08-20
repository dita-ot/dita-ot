/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */
package org.dita.dost.util;

import static javax.xml.XMLConstants.NULL_NS_URI;
import static org.apache.commons.io.FileUtils.*;
import static org.dita.dost.util.Constants.*;

import java.io.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.dita.dost.exception.DITAOTException;
import org.w3c.dom.*;

import org.xml.sax.*;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLReaderFactory;

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
     * List descendant elements by DITA class.
     *
     * @param elem root element
     * @param cls DITA class to match elements
     * @param deep {@code true} to read descendants, {@code false} to read only direct children
     * @raturn list of matching elements
     */
    public static List<Element> getChildElements(final Element elem, final DitaClass cls, final boolean deep) {
        final NodeList children = deep ? elem.getElementsByTagName("*") : elem.getChildNodes();
        final List<Element> res = new ArrayList<>(children.getLength());
        for (int i = 0; i < children.getLength(); i++) {
            final Node child = children.item(i);
            if (cls.matches(child)) {
                res.add((Element) child);
            }
        }
        return res;
    }

    /**
     * List chilid elements by DITA class.
     *
     * @param elem root element
     * @param cls DITA class to match elements
     * @raturn list of matching elements
     */
    public static List<Element> getChildElements(final Element elem, final DitaClass cls) {
        return getChildElements(elem, cls, false);
    }

    /**
     * List child elements elements.
     *
     * @param elem root element
     * @raturn list of matching elements
     */
    public static List<Element> getChildElements(final Element elem) {
        return getChildElements(elem, false);
    }

    /**
     * List child elements elements.
     *
     * @param elem root element
     * @param deep {@code true} to read descendants, {@code false} to read only direct children
     * @raturn list of matching elements
     */
    public static List<Element> getChildElements(final Element elem, final boolean deep) {
        final NodeList children = deep ? elem.getElementsByTagName("*") : elem.getChildNodes();
        final List<Element> res = new ArrayList<>(children.getLength());
        for (int i = 0; i < children.getLength(); i++) {
            final Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                res.add((Element) child);
            }
        }
        return res;
    }

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
     * Transform file with XML filters. Only file URIs are supported.
     *
     * @param input absolute URI to transform and replace
     * @param filters XML filters to transform file with, may be an empty list
     */
    public static void transform(final URI input, final List<XMLFilter> filters) throws DITAOTException {
        assert input.isAbsolute();
        if (!input.getScheme().equals("file")) {
            throw new IllegalArgumentException("Only file URI scheme supported: " + input);
        }

        transform(new File(input), filters);
    }

    /**
     * Transform file with XML filters.
     * 
     * @param inputFile file to transform and replace
     * @param filters XML filters to transform file with, may be an empty list
     */
    public static void transform(final File inputFile, final List<XMLFilter> filters) throws DITAOTException {
        final File outputFile = new File(inputFile.getAbsolutePath() + FILE_EXTENSION_TEMP);
        transform(inputFile, outputFile, filters);
        try {
            deleteQuietly(inputFile);
            moveFile(outputFile, inputFile);
        } catch (final RuntimeException e) {
            throw e;
        } catch (final Exception e) {
            throw new DITAOTException("Failed to replace " + inputFile + ": " + e.getMessage());
        }
    }
    
    /**
     * Transform file with XML filters.
     * 
     * @param inputFile input file
     * @param outputFile output file
     * @param filters XML filters to transform file with, may be an empty list
     */
    public static void transform(final File inputFile, final File outputFile, final List<XMLFilter> filters) throws DITAOTException {
        if (!outputFile.getParentFile().exists() && !outputFile.getParentFile().mkdirs()) {
            throw new DITAOTException("Failed to create output directory " + outputFile.getParentFile().getAbsolutePath());
        }
        
        InputStream in = null;
        OutputStream out = null;
        try {
            final Transformer transformer = TransformerFactory.newInstance().newTransformer();
            XMLReader reader = getXMLReader();
            for (final XMLFilter filter : filters) {
                // ContentHandler must be reset so e.g. Saxon 9.1 will reassign ContentHandler
                // when reusing filter with multiple Transformers.
                filter.setContentHandler(null);
                filter.setParent(reader);
                reader = filter;
            }
            in = new BufferedInputStream(new FileInputStream(inputFile));
            out = new BufferedOutputStream(new FileOutputStream(outputFile));
            final Source source = new SAXSource(reader, new InputSource(in));
            source.setSystemId(inputFile.toURI().toString());
            final Result result = new StreamResult(out);
            transformer.transform(source, result);
        } catch (final RuntimeException e) {
            throw e;
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
    }

    /**
     * Transform file with XML filters.
     *
     * @param input input file
     * @param output output file
     * @param filters XML filters to transform file with, may be an empty list
     */
    public static void transform(final URI input, final URI output, final List<XMLFilter> filters) throws DITAOTException {
        InputSource src = null;
        StreamResult result = null;
        try {
            final Transformer transformer = TransformerFactory.newInstance().newTransformer();
            XMLReader reader = getXMLReader();
            for (final XMLFilter filter : filters) {
                // ContentHandler must be reset so e.g. Saxon 9.1 will reassign ContentHandler
                // when reusing filter with multiple Transformers.
                filter.setContentHandler(null);
                filter.setParent(reader);
                reader = filter;
            }
            src = new InputSource(input.toString());
            final Source source = new SAXSource(reader, src);
            result = new StreamResult(output.toString());
            transformer.transform(source, result);
        } catch (final RuntimeException e) {
            throw e;
        } catch (final Exception e) {
            throw new DITAOTException("Failed to transform " + input + ": " + e.getMessage(), e);
        } finally {
            try {
                close(src);
            } catch (final IOException e) {
                // NOOP
            }
            try {
                close(result);
            } catch (final IOException e) {
                // NOOP
            }
        }
    }

    /** Close input source. */
    public static void close(final InputSource input) throws IOException {
        if (input != null) {
            final InputStream i = input.getByteStream();
            if (i != null) {
                i.close();
            } else {
                final Reader w = input.getCharacterStream();
                if (w != null) {
                    w.close();
                }
            }
        }
    }

    /** Close source. */
    public static void close(final Source input) throws IOException {
        if (input != null && input instanceof StreamSource) {
            final StreamSource s = (StreamSource) input;
            final InputStream i = s.getInputStream();
            if (i != null) {
                i.close();
            } else {
                final Reader w = s.getReader();
                if (w != null) {
                    w.close();
                }
            }
        }
    }

    /** Close result. */
    public static void close(final Result result) throws IOException {
        if (result != null && result instanceof StreamResult) {
            final StreamResult r = (StreamResult) result;
            final OutputStream o = r.getOutputStream();
            if (o != null) {
                o.close();
            } else {
                final Writer w = r.getWriter();
                if (w != null) {
                    w.close();
                }
            }
        }
    }

    /**
     * Escape XML characters.
     * Suggested by hussein_shafie
     * @param s value needed to be escaped
     * @return escaped value
     */
    public static String escapeXML(final String s){
        final char[] chars = s.toCharArray();
        return escapeXML(chars, 0, chars.length);
    }

    /**
     * Escape XML characters.
     * Suggested by hussein_shafie
     * @param chars char arrays
     * @param offset start position
     * @param length arrays lenth
     * @return escaped value
     */
    public static String escapeXML(final char[] chars, final int offset, final int length){
        final StringBuilder escaped = new StringBuilder();

        final int end = offset + length;
        for (int i = offset; i < end; ++i) {
            final char c = chars[i];

            switch (c) {
            case '\'':
                escaped.append("&apos;");
                break;
            case '\"':
                escaped.append("&quot;");
                break;
            case '<':
                escaped.append("&lt;");
                break;
            case '>':
                escaped.append("&gt;");
                break;
            case '&':
                escaped.append("&amp;");
                break;
            default:
                escaped.append(c);
            }
        }

        return escaped.toString();
    }

    /**
     * Get preferred SAX parser.
     *
     * Preferred XML readers are in order:
     *
     * <ol>
     *   <li>{@link Constants#SAX_DRIVER_DEFAULT_CLASS Xerces}</li>
     *   <li>{@link Constants#SAX_DRIVER_SUN_HACK_CLASS Sun's Xerces}</li>
     *   <li>{@link Constants#SAX_DRIVER_CRIMSON_CLASS Crimson}</li>
     * </ol>
     *
     * @return XML parser instance.
     * @throws org.xml.sax.SAXException if instantiating XMLReader failed
     */
    public static XMLReader getXMLReader() throws SAXException {
        XMLReader reader;
        if (System.getProperty(SAX_DRIVER_PROPERTY) != null) {
            return XMLReaderFactory.createXMLReader();
        }
        try {
            Class.forName(SAX_DRIVER_DEFAULT_CLASS);
            reader = XMLReaderFactory.createXMLReader(SAX_DRIVER_DEFAULT_CLASS);
        } catch (final ClassNotFoundException e) {
            try {
                Class.forName(SAX_DRIVER_SUN_HACK_CLASS);
                reader = XMLReaderFactory.createXMLReader(SAX_DRIVER_SUN_HACK_CLASS);
            } catch (final ClassNotFoundException ex) {
                try {
                    Class.forName(SAX_DRIVER_CRIMSON_CLASS);
                    reader = XMLReaderFactory.createXMLReader(SAX_DRIVER_CRIMSON_CLASS);
                } catch (final ClassNotFoundException exc){
                    reader = XMLReaderFactory.createXMLReader();
                }
            }
        }
        if (Configuration.DEBUG) {
            reader = new DebugXMLReader(reader);
        }
        return reader;
    }

    /**
     * Get DOM parser.
     *
     * @return DOM document builder instance.
     * @throws RuntimeException if instantiating DocumentBuilder failed
     */
    public static DocumentBuilder getDocumentBuilder() {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
        } catch (final ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
        if (Configuration.DEBUG) {
            builder = new DebugDocumentBuilder(builder);
        }
        return builder;
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
         * Add or set attribute. Convenience method for {@link #add(String, String, String, String, String)}.
         * 
         * @param attr DOM attribute node
         * @return this builder
         */
        public AttributesBuilder add(final Attr attr) {
            return add(attr.getNamespaceURI() != null ? attr.getNamespaceURI() : "",
                       attr.getLocalName() != null ? attr.getLocalName() : attr.getNodeName(),
                       attr.getNodeName(),
                       attr.isId() ? "ID" : "CDATA",
                       attr.getNodeValue());
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

    /**
     * Debug URI resolver wrapper that logs calls to resolve, not intended for end users.
     */
    public static final class DebugURIResolver implements URIResolver {
        private final URIResolver r;
        public DebugURIResolver(final URIResolver r) {
            this.r = r;
        }

        @Override
        public Source resolve(final String href, final String base) throws TransformerException {
            Configuration.logger.info("XSLT parse: " + href);
            return r.resolve(href, base);
        }
    }

    /**
     * Debug XMLReader wrapper that logs calls to parse, not intended for end users.
     */
    private final static class DebugXMLReader implements XMLReader {
        private final XMLReader r;
        DebugXMLReader(final XMLReader r) {
            this.r = r;
        }

        @Override
        public boolean getFeature(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
            return r.getFeature(name);
        }

        @Override
        public void setFeature(String name, boolean value) throws SAXNotRecognizedException, SAXNotSupportedException {
            r.setFeature(name, value);
        }

        @Override
        public Object getProperty(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
            return r.getProperty(name);
        }

        @Override
        public void setProperty(String name, Object value) throws SAXNotRecognizedException, SAXNotSupportedException {
            r.setProperty(name, value);
        }

        @Override
        public void setEntityResolver(EntityResolver resolver) {
            r.setEntityResolver(resolver);
        }

        @Override
        public EntityResolver getEntityResolver() {
            return r.getEntityResolver();
        }

        @Override
        public void setDTDHandler(DTDHandler handler) {
            r.setDTDHandler(handler);
        }

        @Override
        public DTDHandler getDTDHandler() {
            return r.getDTDHandler();
        }

        @Override
        public void setContentHandler(ContentHandler handler) {
            r.setContentHandler(handler);
        }

        @Override
        public ContentHandler getContentHandler() {
            return r.getContentHandler();
        }

        @Override
        public void setErrorHandler(ErrorHandler handler) {
            r.setErrorHandler(handler);
        }

        @Override
        public ErrorHandler getErrorHandler() {
            return r.getErrorHandler();
        }

        @Override
        public void parse(InputSource input) throws IOException, SAXException {
            Configuration.logger.info("SAX parse: " + (input.getSystemId() != null ? input.getSystemId() : input.toString()));
            r.parse(input);
        }

        @Override
        public void parse(String systemId) throws IOException, SAXException {
            Configuration.logger.info("SAX parse: " + systemId);
            r.parse(systemId);
        }
    }

    /**
     * Debug DocumentBuilder wrapper that logs calls to parse, not intended for end users.
     */
    private static final class DebugDocumentBuilder extends DocumentBuilder {
        private final DocumentBuilder b;
        public DebugDocumentBuilder(final DocumentBuilder b) {
            this.b = b;
        }

        @Override
        public Document parse(InputSource is) throws SAXException, IOException {
            Configuration.logger.info("DOM parse: " + (is.getSystemId() != null ? is.getSystemId() : is.toString()));
            return b.parse(is);
        }

        @Override
        public boolean isNamespaceAware() {
            return b.isNamespaceAware();
        }

        @Override
        public boolean isValidating() {
            return b.isValidating();
        }

        @Override
        public void setEntityResolver(EntityResolver er) {
            b.setEntityResolver(er);
        }

        @Override
        public void setErrorHandler(ErrorHandler eh) {
            b.setErrorHandler(eh);
        }

        @Override
        public Document newDocument() {
            return b.newDocument();
        }

        @Override
        public DOMImplementation getDOMImplementation() {
            return b.getDOMImplementation();
        }
    }
}
