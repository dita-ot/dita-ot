/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2020 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.writer.include;

import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.util.Job;
import org.dita.dost.util.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;

import java.io.IOException;
import java.net.URI;

import static org.dita.dost.util.Constants.ATTRIBUTE_NAME_HREF;
import static org.dita.dost.util.URLUtils.stripFragment;
import static org.dita.dost.util.URLUtils.toURI;

final class IncludeXml {
    private final Job job;
    private final URI currentFile;
    private final ContentHandler contentHandler;
    private final DITAOTLogger logger;
    private final XMLUtils xmlUtils;

    IncludeXml(Job job, URI currentFile, ContentHandler contentHandler, DITAOTLogger logger) {
        this.job = job;
        this.currentFile = currentFile;
        this.contentHandler = contentHandler;
        this.logger = logger;
        this.xmlUtils = new XMLUtils();
        xmlUtils.setLogger(logger);
    }

    boolean include(final Attributes atts) {
        final URI hrefValue = toURI(atts.getValue(ATTRIBUTE_NAME_HREF));
        final Job.FileInfo fileInfo = job.getFileInfo(stripFragment(currentFile.resolve(hrefValue)));
        try {
            final Document doc = job.getStore().getDocument(fileInfo.src);
            Node src = null;
            if (hrefValue.getFragment() != null) {
                src = doc.getElementById(hrefValue.getFragment());
            }
            if (src == null) {
                src = doc;
            }

            xmlUtils.writeDocument(src, new IncludeFilter(contentHandler));
        } catch (IOException e) {
            logger.error("Failed to process include {}", fileInfo.src, e);
            return false;
        }
        return true;
    }
}
