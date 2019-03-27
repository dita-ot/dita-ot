/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2019 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.project;

import org.dita.dost.util.XMLUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.dita.dost.util.XMLUtils.*;

public class XmlReader {

    public Project read(final URI file) throws IOException {
        try (InputStream in = file.toURL().openStream()) {
            return read(in);
        }
    }

    public Project read(final InputStream in) throws IOException {
        try {
            final Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
            final Element project = document.getDocumentElement();
            return new Project(
                    getChildElements(project, "deliverable").stream()
                            .map(this::readDeliverable)
                            .collect(Collectors.toList()),
                    getChildElements(project, "include").stream()
                            .map(include -> getAttribute(include, "href"))
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .map(this::toURI)
                            .map(include -> new Project.ProjectRef(include))
                            .collect(Collectors.toList()),
                    getChildElements(project, "publication").stream()
                            .map(this::readPublication)
                            .collect(Collectors.toList()),
                    getChildElements(project, "context").stream()
                            .map(this::readContext)
                            .collect(Collectors.toList())
            );
        } catch (ParserConfigurationException | SAXException e) {
            throw new IOException(e);
        }
    }

    private Project.Deliverable readDeliverable(final Element deliverable) {
        final String name = getValue(deliverable, "name");
        return new Project.Deliverable(
                name,
                getChildElement(deliverable, "context")
                        .map(this::readContext)
                        .orElse(null),
                getChildElement(deliverable, "output")
                        .map(XMLUtils::getStringValue)
                        .map(this::toURI)
                        .orElse(null),
                getChildElement(deliverable, "publication")
                        .map(this::readPublication)
                        .orElse(null)
        );
    }

    private Project.Publication readPublication(final Element publication) {
        return new Project.Publication(
                getValue(publication, "name"),
                getValue(publication, "id"),
                getValue(publication, "idref"),
                getValue(publication, "transtype"),
                getChildElements(publication, "param").stream()
                        .map(param -> new Project.Publication.Param(
                                getValue(param, "name"),
                                getValue(param, "value"),
                                getAttribute(param, "href").map(this::toURI).orElse(null)
                        ))
                        .collect(Collectors.toList())
        );
    }

    private URI toURI(final String uri) {
        try {
            return new URI(uri);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private Optional<String> getAttribute(final Element elem, final String attrName) {
        final Attr attr = elem.getAttributeNode(attrName);
        if (attr != null && !attr.getValue().isEmpty()) {
            return Optional.of(attr.getValue());
        }
        return Optional.empty();
    }

    private Project.Context readContext(final Element context) {
        return new Project.Context(
                getValue(context, "name"),
                getValue(context, "id"),
                getValue(context, "idref"),
                getChildElement(context, "inputs")
                        .map(inputs -> getChildElements(inputs, "input").stream()
                                .map(input -> getAttribute(input, "href").map(this::toURI))
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .map(input -> new Project.Deliverable.Inputs.Input(input))
                                .collect(Collectors.toList())
                        )
                        .map(inputs -> new Project.Deliverable.Inputs(inputs))
                        .orElse(null),
                getChildElement(context, "profiles")
                        .map(inputs -> getChildElements(inputs, "ditaval").stream()
                                .map(input -> getAttribute(input, "href").map(this::toURI))
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .map(input -> new Project.Deliverable.Profile.DitaVal(input))
                                .collect(Collectors.toList())
                        )
                        .map(inputs -> new Project.Deliverable.Profile(inputs))
                        .orElse(null)
        );
    }
}
