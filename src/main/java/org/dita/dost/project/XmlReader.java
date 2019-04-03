/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2019 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.project;

import com.thaiopensource.relaxng.jaxp.CompactSyntaxSchemaFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
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
import java.util.Optional;
import java.util.stream.Collectors;

import static org.dita.dost.util.XMLUtils.*;

public class XmlReader {

    public static final String ATTR_HREF = "href";
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
    public static final String ELEM_PUBLICATION = "publication";

    private final Validator validator;
    private final DocumentBuilder documentBuilder;
    private final SAXTransformerFactory saxTransformerFactory;

    public XmlReader() {
        try {
            documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
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

    /**
     * Read and validate project file.
     *
     * @param file input project file
     * @return project
     */
    public ProjectBuilder read(final URI file) throws IOException {
        try (InputStream in = file.toURL().openStream()) {
            return read(in, file);
        }
    }

    /**
     * Read and validate project file.
     *
     * @param in   input project file stream
     * @param file input project file URI, may be {@code null}
     * @return project
     */
    public ProjectBuilder read(final InputStream in, final URI file) throws IOException {
        try {
            final Document document = readDocument(in, file);
            final Element project = document.getDocumentElement();
            return new ProjectBuilder(
                    getChildElements(project, ELEM_DELIVERABLE).stream()
                            .map(this::readDeliverable)
                            .collect(Collectors.toList()),
                    getChildElements(project, ELEM_INCLUDE).stream()
                            .map(this::getHref)
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .collect(Collectors.toList()),
                    getChildElements(project, ELEM_PUBLICATION).stream()
                            .map(this::readPublication)
                            .collect(Collectors.toList()),
                    getChildElements(project, ELEM_CONTEXT).stream()
                            .map(this::readContext)
                            .collect(Collectors.toList())
            );
        } catch (SAXException e) {
            throw new IOException(e);
        } catch (TransformerConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Read and validate project file.
     *
     * @param in   input project file
     * @param file
     * @return project file document
     */
    private Document readDocument(InputStream in, URI file) throws TransformerConfigurationException, SAXException, IOException {
        final Document document = documentBuilder.newDocument();
        if (file != null) {
            document.setDocumentURI(file.toString());
        }

        final TransformerHandler domSerializer = saxTransformerFactory.newTransformerHandler();
        domSerializer.setResult(new DOMResult(document));
        final InputSource inputSource = new InputSource(in);
        if (file != null) {
            inputSource.setSystemId(file.toString());
        }
        validator.validate(new SAXSource(inputSource), new SAXResult(domSerializer));

        return document;
    }

    private ProjectBuilder.Deliverable readDeliverable(final Element deliverable) {
        return new ProjectBuilder.Deliverable(
                getValue(deliverable, ATTR_NAME),
                getValue(deliverable, ATTR_ID),
                getChildElement(deliverable, ELEM_CONTEXT)
                        .map(this::readContext)
                        .orElse(null),
                getChildElement(deliverable, ELEM_OUTPUT)
                        .flatMap(this::getHref)
                        .orElse(null),
                getChildElement(deliverable, ELEM_PUBLICATION)
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
                getChildElements(publication, ELEM_PARAM).stream()
                        .map(param -> new ProjectBuilder.Publication.Param(
                                getValue(param, ATTR_NAME),
                                getValue(param, ATTR_VALUE),
                                getHref(param).orElse(null)
                        ))
                        .collect(Collectors.toList())
        );
    }

    private ProjectBuilder.Context readContext(final Element context) {
        return new ProjectBuilder.Context(
                getValue(context, ATTR_NAME),
                getValue(context, ATTR_ID),
                getValue(context, ATTR_IDREF),
                getChildElement(context, ELEM_INPUT)
                        .flatMap(this::getHref)
                        .orElse(null),
                getChildElement(context, ELEM_PROFILE)
                        .map(inputs -> getChildElements(inputs, ELEM_DITAVAL).stream()
                                .map(this::getHref)
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .collect(Collectors.toList())
                        )
                        .map(ProjectBuilder.Deliverable.Profile::new)
                        .orElse(null)
        );
    }

    private Optional<URI> getHref(final Element elem) {
        return getAttribute(elem, ATTR_HREF)
                .map(uri -> {
                    try {
                        return new URI(uri);
                    } catch (URISyntaxException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    private Optional<String> getAttribute(final Element elem, final String attrName) {
        final Attr attr = elem.getAttributeNode(attrName);
        if (attr != null && !attr.getValue().isEmpty()) {
            return Optional.of(attr.getValue());
        }
        return Optional.empty();
    }
}
