/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2017 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.module;

import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.URLUtils.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.util.*;
import org.xml.sax.*;

/**
 * This class reads a list of DITAVAL files, and merges
 * conditions into a single file
 *
 * @since 2.5
 *
 * @author robander
 */
public final class MergeDitavalModule extends AbstractPipelineModuleImpl {

    /** Absolute paths for filter files. */
    private final List<File> ditavalFiles = new LinkedList<>();

    @Override
    public AbstractPipelineOutput execute(final AbstractPipelineInput input) throws DITAOTException {
        if (logger == null) {
            throw new IllegalStateException("Logger not set");
        }

        try {
            parseInputParameters(input);

            setMergedProperty();
            writeMergedDitaval();
        } catch (final DITAOTException e) {
            throw e;
        } catch (final Exception e) {
            throw new DITAOTException(e.getMessage(), e);
        }

        return null;
    }

    private void parseInputParameters(final AbstractPipelineInput input) {
        final File basedir = toFile(input.getAttribute(ANT_INVOKER_PARAM_BASEDIR));
        if (input.getAttribute(ANT_INVOKER_PARAM_DITAVAL) != null) {
            final String[] allDitavalFiles = input.getAttribute(ANT_INVOKER_PARAM_DITAVAL).split(File.pathSeparator);
            for (final String oneDitavalFile : allDitavalFiles) {
                logger.debug("Evaluating ditaval: " + oneDitavalFile);
                final URI ditavalInput = toURI(oneDitavalFile);
                URI usingDitavalInput;
                if (ditavalInput.isAbsolute()) {
                    usingDitavalInput = ditavalInput;
                } else if (ditavalInput.getPath() != null && ditavalInput.getPath().startsWith(URI_SEPARATOR)) {
                    usingDitavalInput = setScheme(ditavalInput, "file");
                } else {
                    usingDitavalInput = basedir.toURI().resolve(ditavalInput);
                }
                if (new File(usingDitavalInput).exists()) {
                    ditavalFiles.add(new File(usingDitavalInput));
                } else {
                    logger.error(
                            MessageUtils.getMessage("DOTJ071E", usingDitavalInput.toString()).toString());
                }
            }
        }
    }

    /**
     * Set external property with the name of the merged file.
     */
    private void setMergedProperty() {
        job.setProperty("dita.input.valfile", new File(job.tempDir, FILE_NAME_MERGED_DITAVAL).toString());
    }

    private void writeMergedDitaval() throws DITAOTException {
        final DocumentBuilder ditavalbuilder = XMLUtils.getDocumentBuilder();
        ditavalbuilder.setEntityResolver(CatalogUtils.getCatalogResolver());
        XMLStreamWriter export = null;
        try (OutputStream exportStream = job.getStore().getOutputStream(new File(job.tempDir, FILE_NAME_MERGED_DITAVAL).toURI())) {
            export = XMLOutputFactory.newInstance().createXMLStreamWriter(exportStream, "UTF-8");
            export.writeStartDocument();
            export.writeStartElement("val");
            export.writeNamespace(DITA_OT_NS_PREFIX, DITA_OT_NAMESPACE);
            for (File curDitaVal : ditavalFiles) {
                final Document doc = ditavalbuilder.parse(curDitaVal);
                final Element ditavalRoot = doc.getDocumentElement();
                final NodeList rootChildren = ditavalRoot.getChildNodes();
                logger.debug("Writing conditions from ditaval: " + curDitaVal);
                writeConditions(export, rootChildren, curDitaVal.toURI().resolve("."));
            }
            export.writeEndElement();
            export.writeEndDocument();
        } catch (final IOException e) {
            throw new DITAOTException("Failed to merge ditaval files: " + e.getMessage(), e);
        } catch (final XMLStreamException e) {
            throw new DITAOTException("Failed to serialize merged ditaval file: " + e.getMessage(), e);
        } catch (final SAXException e) {
            throw new DITAOTException("Failed to parse ditaval file: " + e.getMessage(), e);
        } finally {
            if (export != null) {
                try {
                    export.close();
                } catch (final XMLStreamException e) {
                    logger.error("Failed to close merged ditaval file: " + e.getMessage(), e);
                }
            }
        }

    }

    private void writeConditions(XMLStreamWriter export, NodeList conditionElements, URI ditavalDirectory) {
        try {
            for (int i = 0; i < conditionElements.getLength(); i++) {
                final Node node = conditionElements.item(i);
                switch (node.getNodeType()) {
                case Node.ELEMENT_NODE:
                    final Element elem = (Element) node;
                    export.writeStartElement(node.getNodeName());
                    final NamedNodeMap atts = node.getAttributes();
                    for (int j = 0; j < atts.getLength(); j++) {
                        final String value = atts.item(j).getNodeValue().trim();
                        if (value.indexOf(':') != -1) {
                            final String prefix = value.substring(0, value.indexOf(':'));
                            final String ns = elem.lookupNamespaceURI(prefix);
                            if (ns != null) {
                                export.writeNamespace(prefix, ns);
                            }
                        }
                    }
                    if (atts.getNamedItem(ATTRIBUTE_NAME_IMAGEREF) != null ||
                            atts.getNamedItem(ATTRIBUTE_NAME_IMG) != null) {
                        final String imagerefAtt = atts.getNamedItem(ATTRIBUTE_NAME_IMAGEREF) != null ?
                                atts.getNamedItem(ATTRIBUTE_NAME_IMAGEREF).getNodeValue() :    // DITA 1.1 and later: use @imageref on <startflag>, <endflag>
                                atts.getNamedItem(ATTRIBUTE_NAME_IMG).getNodeValue();          // Pre-DITA 1.1: use @img on <prop>
                        if (toURI(imagerefAtt).isAbsolute()) {
                            export.writeAttribute(DITA_OT_NS_PREFIX, DITA_OT_NAMESPACE, ATTRIBUTE_NAME_IMAGEREF_URI, imagerefAtt);
                        } else {
                            export.writeAttribute(DITA_OT_NS_PREFIX, DITA_OT_NAMESPACE, ATTRIBUTE_NAME_IMAGEREF_URI, ditavalDirectory.resolve(imagerefAtt).toString());
                        }
                    }
                    for (int j = 0; j < atts.getLength(); j++) {
                        export.writeAttribute(atts.item(j).getNodeName(), atts.item(j).getNodeValue());
                    }
                    writeConditions(export, node.getChildNodes(), ditavalDirectory);
                    export.writeEndElement();
                    break;
                case Node.TEXT_NODE:
                    export.writeCharacters(node.getNodeValue());
                    break;
                }
            }
        } catch (final XMLStreamException e) {
            logger.error("Failed to generate merged DITAVAL file: " + e.getMessage(), e);
        }
    }

}