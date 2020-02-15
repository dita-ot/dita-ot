/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2020 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.writer.include;

import org.dita.dost.exception.DITAOTXMLErrorHandler;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.util.Job;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import java.io.IOException;
import java.net.URI;

import static org.dita.dost.util.CatalogUtils.getCatalogResolver;
import static org.dita.dost.util.Constants.ATTRIBUTE_NAME_HREF;
import static org.dita.dost.util.URLUtils.stripFragment;
import static org.dita.dost.util.URLUtils.toURI;
import static org.dita.dost.util.XMLUtils.getDocumentBuilder;

final class IncludeXml {
    private final Job job;
    private final URI currentFile;
    private final ContentHandler contentHandler;
    private final DITAOTLogger logger;

    IncludeXml(Job job, URI currentFile, ContentHandler contentHandler, DITAOTLogger logger) {
        this.job = job;
        this.currentFile = currentFile;
        this.contentHandler = contentHandler;
        this.logger = logger;
    }

    boolean include(final Attributes atts) {
        final URI hrefValue = toURI(atts.getValue(ATTRIBUTE_NAME_HREF));
        final Job.FileInfo fileInfo = job.getFileInfo(stripFragment(currentFile.resolve(hrefValue)));
        final DocumentBuilder builder = getDocumentBuilder();
        builder.setEntityResolver(getCatalogResolver());
        builder.setErrorHandler(new DITAOTXMLErrorHandler(fileInfo.src.toString(), logger));
        try {
            final Document doc = builder.parse(fileInfo.src.toString());
            Node src = null;
            if (hrefValue.getFragment() != null) {
                src = doc.getElementById(hrefValue.getFragment());
            }
            if (src == null) {
                src = doc;
            }

            final Transformer serializer = TransformerFactory.newInstance().newTransformer();
            final DOMSource source = new DOMSource(src);
            final SAXResult result = new SAXResult(new IncludeFilter(contentHandler));
            serializer.transform(source, result);
        } catch (SAXException | IOException | TransformerException e) {
            logger.error("Failed to process include {}", fileInfo.src, e);
            return false;
        }
        return true;
    }
}
