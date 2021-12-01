/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2011 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.util;

import com.google.common.annotations.VisibleForTesting;
import net.sf.saxon.expr.instruct.TerminationException;
import net.sf.saxon.lib.*;
import net.sf.saxon.s9api.*;
import net.sf.saxon.s9api.streams.Step;
import org.apache.xml.resolver.tools.CatalogResolver;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.module.saxon.DelegatingCollationUriResolver;
import org.w3c.dom.*;
import org.xml.sax.*;
import org.xml.sax.helpers.AttributesImpl;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.stream.Stream;

import static javax.xml.XMLConstants.DEFAULT_NS_PREFIX;
import static javax.xml.XMLConstants.NULL_NS_URI;
import static net.sf.saxon.s9api.streams.Predicates.*;
import static net.sf.saxon.s9api.streams.Steps.child;
import static net.sf.saxon.s9api.streams.Steps.descendant;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.apache.commons.io.FileUtils.moveFile;
import static org.dita.dost.util.Constants.*;

/**
 * XML utility methods.
 *
 * @since 1.5.4
 * @author Jarno Elovirta
 */
public final class XMLUtils {

    private static final DocumentBuilderFactory factory;
    static {
        factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
    }
    private static final SAXParserFactory saxParserFactory;
    static {
        saxParserFactory = SAXParserFactory.newInstance();
        saxParserFactory.setNamespaceAware(true);
    }
    private DITAOTLogger logger;
    private final CatalogResolver catalogResolver;
    private final Processor processor;
    private final XsltCompiler xsltCompiler;

    public static final Attributes EMPTY_ATTRIBUTES = new AttributesImpl();

    /**
     * Get root element from Document node.
     */
    public static Step<XdmNode> rootElement() {
        return child().where(isElement()).first();
    }

    public XMLUtils() {
        catalogResolver = CatalogUtils.getCatalogResolver();
        final net.sf.saxon.Configuration config = new net.sf.saxon.Configuration();
        config.setURIResolver(catalogResolver);
        configureSaxonExtensions(config);
        configureSaxonCollationResolvers(config);
        processor = new Processor(config);
        xsltCompiler = processor.newXsltCompiler();
        xsltCompiler.setURIResolver(catalogResolver);
    }

    /**
     * Registers Saxon full integrated function definitions.
     *
     * The intgrated function should be an instance of net.sf.saxon.lib.ExtensionFunctionDefinition abstract class.
     * @see <a href="https://www.saxonica.com/html/documentation/extensibility/integratedfunctions/ext-full-J.html">Saxon
     *      Java extension functions: full interface</a>
     */
    @VisibleForTesting
    static void configureSaxonExtensions(final net.sf.saxon.Configuration conf) {
        for (ExtensionFunctionDefinition def : ServiceLoader.load(ExtensionFunctionDefinition.class)) {
            try {
                conf.registerExtensionFunction(def.getClass().newInstance());
            } catch (InstantiationException e) {
                throw new RuntimeException("Failed to register " + def.getFunctionQName().getDisplayName()
                        + ". Cannot create instance of " + def.getClass().getName() + ": " + e.getMessage(), e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Registers collation URI resolvers.
     */
    @VisibleForTesting
    static void configureSaxonCollationResolvers(final net.sf.saxon.Configuration conf) {
        for (DelegatingCollationUriResolver resolver : ServiceLoader.load(DelegatingCollationUriResolver.class)) {
            try {
                final DelegatingCollationUriResolver newResolver = resolver.getClass().newInstance();
                final CollationURIResolver currentResolver = conf.getCollationURIResolver();
                if (currentResolver != null) {
                    newResolver.setBaseResolver(currentResolver);
                }
                conf.setCollationURIResolver(newResolver);
            } catch (InstantiationException e) {
                throw new RuntimeException("Failed to register " + resolver.getClass().getSimpleName()
                        + ". Cannot create instance of " + resolver.getClass().getName() + ": " + e.getMessage(), e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void setLogger(final DITAOTLogger logger) {
        this.logger = logger;
    }

    /** Convert DOM NodeList to List. */
    public static <T> List<T> toList(final NodeList nodes) {
        final List<T> res = new ArrayList<>(nodes.getLength());
        for (int i = 0; i < nodes.getLength(); i++) {
            res.add((T) nodes.item(i));
        }
        return res;
    }

    public static MessageListener2 toMessageListener(final DITAOTLogger logger) {
        return new MessageListener2() {
            @Override
            public void message(XdmNode content, net.sf.saxon.s9api.QName code, boolean terminate, SourceLocator locator) {
                final Optional<String> errorCode = content.select(descendant(isProcessingInstruction()).where(hasLocalName("error-code")))
                        .findAny()
                        .map(XdmItem::getStringValue);
                final String level = terminate
                        ? "FATAL"
                        : content.select(descendant(isProcessingInstruction()).where(hasLocalName("level")))
                            .findAny()
                            .map(XdmItem::getStringValue)
                            .orElse("INFO");
                final String msg = content.getStringValue();
                switch (level) {
                    case "FATAL":
                        final TerminationException err = new TerminationException(msg);
                        errorCode.ifPresent(err::setErrorCode);
                        throw new SaxonApiUncheckedException(err);
                    case "ERROR":
                        logger.error(msg);
                        break;
                    case "WARN":
                        logger.warn(msg);
                        break;
                    case "INFO":
                        logger.info(msg);
                        break;
                    case "DEBUG":
                        logger.debug(msg);
                        break;
                    default:
                        logger.error("Message level " + level + " not supported");
                        logger.info(msg);
                }
            }
        };
    }

    public static ErrorReporter toErrorReporter(final DITAOTLogger logger) {
        return (XmlProcessingError error) -> {
            if (error.isWarning()) {
                logger.warn(error.getMessage());
            } else {
                if (error.getCause() instanceof FileNotFoundException) {
                    error.asWarning();
                    final StringBuilder buf = new StringBuilder()
                            .append(error.getLocation().getSystemId())
                            .append(":")
                            .append(error.getLocation().getLineNumber())
                            .append(":")
                            .append(error.getLocation().getColumnNumber())
                            .append(": ")
                            .append(error.getMessage());
                    logger.warn(buf.toString());
                } else {
                    logger.error(error.getMessage());
                }
            }
        };
    }

    /**
     * Get prefix from QName.
     */
    public static String getPrefix(final String qname) {
        final int sep = qname.indexOf(':');
        return sep != -1 ? qname.substring(0, sep) : DEFAULT_NS_PREFIX;
    }

    /**
     * List descendant elements by DITA class.
     *
     * @param elem root element
     * @param cls DITA class to match elements
     * @param deep {@code true} to read descendants, {@code false} to read only direct children
     * @return list of matching elements
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
     * Get first child element by element name.
     *
     * @param elem root element
     * @param ns namespace URI, {@code null} for empty namespace
     * @param name element name to match element
     * @return matching element
     */
    public static Optional<Element> getChildElement(final Element elem, final String ns, final String name) {
        final NodeList children = elem.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            final Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                if (Objects.equals(child.getNamespaceURI(), ns) && name.equals(child.getLocalName())) {
                    return Optional.of((Element) child);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Get first child element by DITA class.
     *
     * @param elem root element
     * @param cls DITA class to match element
     * @return matching element
     */
    public static Optional<Element> getChildElement(final Element elem, final DitaClass cls) {
        final NodeList children = elem.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            final Node child = children.item(i);
            if (cls.matches(child)) {
                return Optional.of((Element) child);
            }
        }
        return Optional.empty();
    }

    /**
     * List child elements by element name.
     *
     * @param ns namespace URL, {@code null} for empty namespace
     * @param elem root element
     * @param name element local name to match elements
     * @return list of matching elements
     */
    public static List<Element> getChildElements(final Element elem, final String ns, final String name) {
        final NodeList children =  elem.getChildNodes();
        final List<Element> res = new ArrayList<>(children.getLength());
        for (int i = 0; i < children.getLength(); i++) {
            final Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                if (Objects.equals(child.getNamespaceURI(), ns) && name.equals(child.getLocalName())) {
                    res.add((Element) child);
                }
            }
        }
        return res;
    }

    /**
     * List child elements by DITA class.
     *
     * @param elem root element
     * @param cls DITA class to match elements
     * @return list of matching elements
     */
    public static List<Element> getChildElements(final Element elem, final DitaClass cls) {
        return getChildElements(elem, cls, false);
    }

    /**
     * List child elements elements.
     *
     * @param elem root element
     * @return list of matching elements
     */
    public static List<Element> getChildElements(final Element elem) {
        return getChildElements(elem, false);
    }

    /**
     * List child elements elements.
     *
     * @param elem root element
     * @param deep {@code true} to read descendants, {@code false} to read only direct children
     * @return list of matching elements
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
     * Checks if the closest DITA ancestor {@code <foreign>} or {@code <unknown>}
     *
     * @param classes stack of class attributes for open elements
     * @return true if closest DITA ancestor is {@code <foreign>} or {@code <unknown>}, otherwise false
     */
    public static boolean nonDitaContext(final Deque<DitaClass> classes) {
        final Iterator<DitaClass> it = classes.iterator();
        it.next(); // Skip first, because we're checking if current element is inside non-DITA context
        while (it.hasNext()) {
            final DitaClass cls = it.next();
            if (cls != null && cls.isValid() &&
                    (TOPIC_FOREIGN.matches(cls) || TOPIC_UNKNOWN.matches(cls))) {
                return true;
            } else if (cls != null && cls.isValid()) {
                return false;
            }
        }
        return false;
    }

    /**
     * Get specific element node from child nodes.
     *
     * @param element    parent node
     * @param classValue DITA class to search for
     * @return element node, {@code null} if not found
     */
    public static Element getElementNode(final Element element, final DitaClass classValue) {
        final NodeList list = element.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            final Node node = list.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                final Element child = (Element) node;
                if (classValue.matches(child)) {
                    return child;
                }
            }
        }
        return null;
    }

    private static final List<String> excludeList;
    static {
        final List<String> el = new ArrayList<>();
        el.add(TOPIC_INDEXTERM.toString());
        el.add(TOPIC_DRAFT_COMMENT.toString());
        el.add(TOPIC_REQUIRED_CLEANUP.toString());
        el.add(TOPIC_DATA.toString());
        el.add(TOPIC_DATA_ABOUT.toString());
        el.add(TOPIC_UNKNOWN.toString());
        el.add(TOPIC_FOREIGN.toString());
        excludeList = Collections.unmodifiableList(el);
    }

    /**
     * Get text value of a node.
     *
     * @param root root node
     * @return text value
     */
    public static String getText(final Node root) {
        if (root == null) {
            return "";
        } else {
            final StringBuilder result = new StringBuilder(1024);
            if (root.hasChildNodes()) {
                final NodeList list = root.getChildNodes();
                for (int i = 0; i < list.getLength(); i++) {
                    final Node childNode = list.item(i);
                    if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                        final Element e = (Element) childNode;
                        final String value = e.getAttribute(ATTRIBUTE_NAME_CLASS);
                        if (!excludeList.contains(value)) {
                            final String s = getText(e);
                            result.append(s);
                        }
                    } else if (childNode.getNodeType() == Node.TEXT_NODE) {
                        result.append(childNode.getNodeValue());
                    }
                }
            } else if (root.getNodeType() == Node.TEXT_NODE) {
                result.append(root.getNodeValue());
            }
            return result.toString();
        }
    }

    /**
     * Search for the special kind of node by specialized value. Equivalent to XPath
     *
     * <pre>$root//*[contains(@class, $classValue)][@*[name() = $attrName and . = $searchKey]]</pre>
     *
     * @param root       place may have the node.
     * @param searchKey  keyword for search.
     * @param attrName   attribute name for search.
     * @param classValue class value for search.
     * @return matching element, {@code null} if not found
     */
    public static Element searchForNode(final Element root, final String searchKey, final String attrName,
                                  final DitaClass classValue) {
        if (root == null) {
            return null;
        }
        final Queue<Element> queue = new LinkedList<>();
        queue.offer(root);
        while (!queue.isEmpty()) {
            final Element pe = queue.poll();
            final NodeList pchildrenList = pe.getChildNodes();
            for (int i = 0; i < pchildrenList.getLength(); i++) {
                final Node node = pchildrenList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    queue.offer((Element) node);
                }
            }
            if (pe.getAttribute(ATTRIBUTE_NAME_CLASS) == null || !classValue.matches(pe)) {
                continue;
            }
            final Attr value = pe.getAttributeNode(attrName);
            if (value == null) {
                continue;
            }
            if (searchKey.equals(value.getValue())) {
                return pe;
            }
        }
        return null;
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
     * @param name name
     * @param value attribute value
     */
    public static void addOrSetAttribute(final AttributesImpl atts, final QName name, final String value) {
        addOrSetAttribute(atts, name.getNamespaceURI(), name.getLocalPart(),
                name.getPrefix().isEmpty() ? name.getLocalPart() : (name.getPrefix()  + ":" + name.getLocalPart()),
                "CDATA", value);
    }

    /**
     * Add or set attribute. Convenience method for {@link #addOrSetAttribute(AttributesImpl, String, String, String, String, String)}.
     *
     * @param atts attributes
     * @param attr attribute to add
     */
    public static void addOrSetAttribute(final AttributesImpl atts, final XdmNode attr) {
        final net.sf.saxon.s9api.QName name = attr.getNodeName();
        addOrSetAttribute(atts, name.getNamespaceURI(), name.getLocalName(),
                name.getPrefix().isEmpty() ? name.getLocalName() : (name.getPrefix()  + ":" + name.getLocalName()),
                "CDATA", attr.getStringValue());
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
     * @deprecated since 3.5
     */
    @Deprecated
    public void transform(final URI input, final List<XMLFilter> filters) throws DITAOTException {
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
     * @deprecated since 3.5
     */
    @Deprecated
    public void transform(final File inputFile, final List<XMLFilter> filters) throws DITAOTException {
        final File outputFile = new File(inputFile.getAbsolutePath() + FILE_EXTENSION_TEMP);
        transformFile(inputFile, outputFile, filters);
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
     * @deprecated since 3.5
     */
    @Deprecated
    public void transform(final File inputFile, final File outputFile, final List<XMLFilter> filters) throws DITAOTException {
        if (inputFile.equals(outputFile)) {
            transform(inputFile, filters);
        } else {
            transformFile(inputFile, outputFile, filters);
        }
    }

    /**
     * @deprecated since 3.5
     */
    @Deprecated
    private void transformFile(final File inputFile, final File outputFile, final List<XMLFilter> filters) throws DITAOTException {
        if (!outputFile.getParentFile().exists() && !outputFile.getParentFile().mkdirs()) {
            throw new DITAOTException("Failed to create output directory " + outputFile.getParentFile().getAbsolutePath());
        }

        try (final InputStream in = new BufferedInputStream(new FileInputStream(inputFile));
             final OutputStream out = new BufferedOutputStream(new FileOutputStream(outputFile))) {
            XMLReader reader = getXMLReader();
            for (final XMLFilter filter : filters) {
                // ContentHandler must be reset so e.g. Saxon 9.1 will reassign ContentHandler
                // when reusing filter with multiple Transformers.
                filter.setContentHandler(null);
                filter.setParent(reader);
                reader = filter;
            }

            final Serializer result = processor.newSerializer(out);
            final ContentHandler serializer = result.getContentHandler();
            reader.setContentHandler(serializer);

            final InputSource inputSource = new InputSource(in);
            inputSource.setSystemId(inputFile.toURI().toString());

            reader.parse(inputSource);
        } catch (final RuntimeException e) {
            throw e;
        } catch (final Exception e) {
            throw new DITAOTException("Failed to transform " + inputFile + ": " + e.getMessage(), e);
        }
    }

    /**
     * Transform file with XML filters.
     *
     * @param input input file
     * @param output output file
     * @param filters XML filters to transform file with, may be an empty list
     * @deprecated since 3.5
     */
    @Deprecated
    public void transform(final URI input, final URI output, final List<XMLFilter> filters) throws DITAOTException {
        if (input.equals(output)) {
            transform(input, filters);
        } else {
            transformURI(input, output, filters);
        }
    }

    /**
     * @deprecated since 3.5
     */
    @Deprecated
    private void transformURI(final URI input, final URI output, final List<XMLFilter> filters) throws DITAOTException {
        final File outputFile = new File(output);
        if (!outputFile.getParentFile().exists() && !outputFile.getParentFile().mkdirs()) {
            throw new DITAOTException("Failed to create output directory " + outputFile.getParentFile().getAbsolutePath());
        }

        try {
            XMLReader reader = getXMLReader();
            for (final XMLFilter filter : filters) {
                // ContentHandler must be reset so e.g. Saxon 9.1 will reassign ContentHandler
                // when reusing filter with multiple Transformers.
                filter.setContentHandler(null);
                filter.setParent(reader);
                reader = filter;
            }

            final Serializer result = processor.newSerializer(outputFile);
            final ContentHandler serializer = result.getContentHandler();
            reader.setContentHandler(serializer);

            final InputSource inputSource = new InputSource(input.toString());

            reader.parse(inputSource);
        } catch (final RuntimeException e) {
            throw e;
        } catch (final Exception e) {
            throw new DITAOTException("Failed to transform " + input + ": " + e.getMessage(), e);
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
     *
     * @param s value needed to be escaped
     * @return escaped value
     */
    public static String escapeXML(final String s) {
        final char[] chars = s.toCharArray();
        return escapeXML(chars, 0, chars.length);
    }

    /**
     * Escape XML characters.
     *
     * @param chars char arrays
     * @param offset start position
     * @param length arrays lenth
     * @return escaped value
     */
    public static String escapeXML(final char[] chars, final int offset, final int length) {
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
     * @return XML parser instance.
     * @throws org.xml.sax.SAXException if instantiating XMLReader failed
     */
    public static XMLReader getXMLReader() throws SAXException {
        try {
            final XMLReader reader = saxParserFactory.newSAXParser().getXMLReader();
            return Configuration.DEBUG ? new DebugXMLReader(reader) : reader;
        } catch (ParserConfigurationException e) {
            throw new SAXException(e);
        }
    }

    /**
     * Get DOM parser.
     *
     * @return DOM document builder instance.
     * @throws RuntimeException if instantiating DocumentBuilder failed
     */
    public static DocumentBuilder getDocumentBuilder() {
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
        } catch (final ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
        if (Configuration.DEBUG) {
            builder = new DebugDocumentBuilder(builder);
        }
        builder.setEntityResolver(CatalogUtils.getCatalogResolver());
        return builder;
    }

    /**
     * Write DOM document to file.
     *
     * @param doc document to store
     * @param dst absolute destination file
     * @throws IOException if serializing file fails
     */
    public void writeDocument(final Document doc, final File dst) throws IOException {
        try {
            final Serializer serializer = processor.newSerializer(dst);
            final XdmNode source = processor.newDocumentBuilder().wrap(doc);
            serializer.serializeNode(source);
        } catch (SaxonApiException e) {
            throw new IOException(e);
        }
    }

    /**
     * Write DOM document to SAX pipe.
     *
     * @param doc document to store
     * @param dst SAX pipe
     * @throws IOException if serializing file fails
     */
    public void writeDocument(final Node doc, final ContentHandler dst) throws IOException {
        writeDocument(processor.newDocumentBuilder().wrap(doc), dst);
    }

    /**
     * Write XdmNode to SAX pipe.
     *
     * @param source XdmNode to store
     * @param dst SAX pipe
     * @throws IOException if serializing file fails
     */
    public void writeDocument(final XdmNode source, final ContentHandler dst) throws IOException {
        try {
            final SAXDestination destination = new SAXDestination(dst);
            processor.writeXdmValue(source, destination);
        } catch (SaxonApiException e) {
            throw new IOException(e);
        }
    }

    /**
     * Get common S9API processor.
     */
    public Processor getProcessor() {
        return processor;
    }

    public XsltCompiler getXsltCompiler() {
        XsltCompiler res = processor.newXsltCompiler();
        res.setURIResolver(catalogResolver);
        return res;
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
         * @param name name
         * @param value attribute value
         * @return this builder
         */
        public AttributesBuilder add(final QName name, final String value) {
            return add(name.getNamespaceURI(), name.getLocalPart(), name.getPrefix()  + ":" + name.getLocalPart(), "CDATA", value);
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
            System.out.println("XSLT parse: " + href);
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
            System.out.println("SAX parse: " + (input.getSystemId() != null ? input.getSystemId() : input.toString()));
            r.parse(input);
        }

        @Override
        public void parse(String systemId) throws IOException, SAXException {
            System.out.println("SAX parse: " + systemId);
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
            System.out.println("DOM parse: " + (is.getSystemId() != null ? is.getSystemId() : is.toString()));
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

    /**
     * Get attribute value.
     *
     * @param elem attribute parent element
     * @param attrName attribute name
     * @return attribute value, {@code null} if not set
     */
    public static String getValue(final Element elem, final String attrName) {
        final Attr attr = elem.getAttributeNode(attrName);
        if (attr != null && !attr.getValue().isEmpty()) {
            return attr.getValue();
        }
        return null;
    }

    /**
     * Get cascaded attribute value.
     *
     * @param elem attribute parent element
     * @param attrName attribute name
     * @return attribute value, {@code null} if not set
     */
    public static String getCascadeValue(final Element elem, final String attrName) {
        Element current = elem;
        while (current != null) {
            final Attr attr = current.getAttributeNode(attrName);
            if (attr != null) {
                return attr.getValue();
            }
            final Node parent = current.getParentNode();
            if (parent != null && parent.getNodeType() == Node.ELEMENT_NODE) {
                current = (Element) parent;
            } else {
                break;
            }
        }
        return null;
    }

    /**
     * Stream of element ancestor elements.
     * @param element start element
     * @return stream of ancestor elements
     */
    public static Stream<Element> ancestors(final Element element) {
        final Stream.Builder<Element> builder = Stream.builder();
        for (Node current = element.getParentNode(); current != null; current = current.getParentNode()) {
            if (current.getNodeType() == Node.ELEMENT_NODE) {
                builder.accept((Element) current);
            }
        }
        return builder.build();
    }

    /**
     * Insert fragment before reference element
     * @param ref node to insert before
     * @param fragment content to insert
     */
    public static void insertBefore(final Node ref, final DocumentFragment fragment) {
        final Document doc = ref.getOwnerDocument();
        final Node parent = ref.getParentNode();
        final List<Node> children = toList(fragment.getChildNodes());
        for (final Node child : children) {
            parent.insertBefore(doc.importNode(child, true), ref);
        }
    }

    /**
     * Insert fragment after reference element
     * @param ref node to insert after
     * @param fragment content to insert
     */
    public static  void insertAfter(final Node ref, final DocumentFragment fragment) {
        final Document doc = ref.getOwnerDocument();
        final Node parent = ref.getParentNode();
        final List<Node> children = toList(fragment.getChildNodes());
        final Node nextSibling = ref.getNextSibling();
        if (nextSibling != null) {
            for (final Node child : children) {
                parent.insertBefore(doc.importNode(child, true), nextSibling);
            }
        } else {
            for (final Node child : children) {
                parent.appendChild(doc.importNode(child, true));
            }
        }
    }
}
