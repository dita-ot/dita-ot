/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2019 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.project;

import com.thaiopensource.relaxng.jaxp.CompactSyntaxSchemaFactory;
import org.dita.dost.util.XMLUtils;
import org.slf4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.dita.dost.util.XMLUtils.getValue;

public class XmlReader {

    public static final String NS = "https://www.dita-ot.org/project";

    public static final String ATTR_HREF = "href";
    public static final String ATTR_PATH = "path";
    public static final String ATTR_ID = "id";
    public static final String ATTR_IDREF = "idref";
    public static final String ATTR_NAME = "name";
    public static final String ATTR_TRANSTYPE = "transtype";
    public static final String ATTR_VALUE = "value";
    public static final String ELEM_CONTEXT = "context";
    public static final String ELEM_DELIVERABLE = "deliverable";
    public static final String ELEM_DITAVAL = "ditaval";
    public static final String ELEM_INCLUDE = "include";
    public static final String ELEM_INPUT = "input";
    public static final String ELEM_OUTPUT = "output";
    public static final String ELEM_PARAM = "param";
    public static final String ELEM_PROFILE = "profile";
    public static final String ELEM_PROJECT = "project";
    public static final String ELEM_PUBLICATION = "publication";

    private final Validator validator;
    private final DocumentBuilder documentBuilder;
    private final XMLReader xmlReader;
    private final SAXTransformerFactory saxTransformerFactory;
    private Logger logger;
    private boolean lax;

    public XmlReader() {
        try {
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            documentBuilder = factory.newDocumentBuilder();
            final SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            saxParserFactory.setNamespaceAware(true);
            xmlReader = saxParserFactory.newSAXParser().getXMLReader();
        } catch (ParserConfigurationException | SAXException e) {
            throw new RuntimeException(e);
        }
        saxTransformerFactory = (SAXTransformerFactory) TransformerFactory.newInstance();
        final SchemaFactory f = new CompactSyntaxSchemaFactory();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("project.rnc")) {
            validator = f.newSchema(new StreamSource(in)).newValidator();
        } catch (IOException | SAXException e) {
            throw new RuntimeException("Failed to read project schema: " + e.getMessage(), e);
        }
    }

    public void setLogger(final Logger logger) {
        this.logger = logger;
    }

    public void setLax(final boolean lax) {
        this.lax = lax;
    }

    /**
     * Read and validate project file.
     *
     * @param file input project file
     * @return project
     */
    public ProjectBuilder read(final URI file) throws IOException, SAXParseException {
        try (InputStream in = file.toURL().openStream()) {
            return read(in, file);
        }
    }

    /**
     * Read and validate project file.
     *
     * @param in   input project file stream
     * @param file input project file URI, may be {@code null}
     * @return project or {@code null} if none found
     */
    public ProjectBuilder read(final InputStream in, final URI file) throws IOException, SAXParseException {
        final Document document = readDocument(in, file);
        final List<Element> projects = XMLUtils.toList(document.getElementsByTagNameNS(NS, ELEM_PROJECT));
        if (projects.isEmpty()) {
            return null;
        } else if (projects.size() > 1) {
            logger.warn("Found {} project elements, using first", projects.size());
        }
        final Element project = projects.get(0);
        return new ProjectBuilder(
                getChildren(project, ELEM_DELIVERABLE)
                        .map(this::readDeliverable)
                        .collect(Collectors.toList()),
                getChildren(project, ELEM_INCLUDE)
                        .map(this::getHref)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList()),
                getChildren(project, ELEM_PUBLICATION)
                        .map(this::readPublication)
                        .collect(Collectors.toList()),
                getChildren(project, ELEM_CONTEXT)
                        .map(this::readContext)
                        .collect(Collectors.toList())
        );
    }

    /**
     * Read and validate project file.
     *
     * @param in   input project file
     * @param file
     * @return project file document
     */
    private Document readDocument(final InputStream in, final URI file) throws SAXParseException, IOException {
        final XMLReader reader = lax ? new IgnoringXmlFilter(xmlReader) : xmlReader;
        final InputSource inputSource = new InputSource(in);
        if (file != null) {
            inputSource.setSystemId(file.toString());
        }
        final SAXSource source = new SAXSource(reader, inputSource);
        final TransformerHandler domSerializer;
        try {
            domSerializer = saxTransformerFactory.newTransformerHandler();
        } catch (TransformerConfigurationException e) {
            throw new RuntimeException(e);
        }
        final Document document = documentBuilder.newDocument();
        if (file != null) {
            document.setDocumentURI(file.toString());
        }
        domSerializer.setResult(new DOMResult(document));
        final SAXResult result = new SAXResult(domSerializer);

        try {
            validator.validate(source, result);
        } catch (SAXException e) {
            if (e instanceof SAXParseException) {
                throw (SAXParseException) e;
            } else {
                throw new SAXParseException(e.getMessage(), null);
            }
        }
        if (document.getDocumentElement() == null) {
            throw new SAXParseException("No project file elements found", null, file.toString(), -1, -1);
        }

        return document;
    }

    private ProjectBuilder.Deliverable readDeliverable(final Element deliverable) {
        return new ProjectBuilder.Deliverable(
                getValue(deliverable, ATTR_NAME),
                getValue(deliverable, ATTR_ID),
                getChild(deliverable, ELEM_CONTEXT)
                        .map(this::readContext)
                        .orElse(null),
                getChild(deliverable, ELEM_OUTPUT)
                        .flatMap(this::getHref)
                        .orElse(null),
                getChild(deliverable, ELEM_PUBLICATION)
                        .map(this::readPublication)
                        .orElse(null)
        );
    }

    private ProjectBuilder.Publication readPublication(final Element publication) {
        return new ProjectBuilder.Publication(
                getValue(publication, ATTR_NAME),
                getValue(publication, ATTR_ID),
                getValue(publication, ATTR_IDREF),
                getValue(publication, ATTR_TRANSTYPE),
                getChildren(publication, ELEM_PARAM)
                        .map(param -> new ProjectBuilder.Publication.Param(
                                getValue(param, ATTR_NAME),
                                getValue(param, ATTR_VALUE),
                                getHref(param).orElse(null),
                                getFile(param).orElse(null)
                        ))
                        .collect(Collectors.toList())
        );
    }

    private ProjectBuilder.Context readContext(final Element context) {
        return new ProjectBuilder.Context(
                getValue(context, ATTR_NAME),
                getValue(context, ATTR_ID),
                getValue(context, ATTR_IDREF),
                getChildren(context, ELEM_INPUT)
                        .map(this::getHref)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList()),
                getChild(context, ELEM_PROFILE)
                        .map(inputs -> getChildren(inputs, ELEM_DITAVAL)
                                .map(this::getHref)
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .collect(Collectors.toList())
                        )
                        .map(ProjectBuilder.Deliverable.Profile::new)
                        .orElse(null)
        );
    }

    private Optional<Element> getChild(final Element project, final String localName) {
        return XMLUtils.getChildElement(project, NS, localName);
    }

    private Stream<Element> getChildren(final Element project, final String localName) {
        return XMLUtils.getChildElements(project, NS, localName).stream();
    }

    private Optional<URI> getHref(final Element elem) {
        return getAttribute(elem, ATTR_HREF).map(this::toUri);
    }

    private Optional<URI> getFile(final Element elem) {
        return getAttribute(elem, ATTR_PATH).map(this::toUri);
    }

    private Optional<String> getAttribute(final Element elem, final String attrName) {
        final Attr attr = elem.getAttributeNode(attrName);
        if (attr != null && !attr.getValue().isEmpty()) {
            return Optional.of(attr.getValue());
        }
        return Optional.empty();
    }

    private URI toUri(final String uri) {
        try {
            return new URI(uri);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

}
