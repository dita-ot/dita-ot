/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2020 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.writer.include;

import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.util.Job;
import org.dita.dost.util.XMLUtils;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;

import java.io.IOException;
import java.net.URI;
import java.util.stream.Stream;

import static net.sf.saxon.s9api.streams.Steps.id;
import static org.dita.dost.util.Constants.ATTRIBUTE_NAME_HREF;
import static org.dita.dost.util.URLUtils.stripFragment;
import static org.dita.dost.util.URLUtils.toURI;

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
        try {
            final XdmNode doc = job.getStore().getImmutableNode(fileInfo.src);
            final XdmNode src;
            if (hrefValue.getFragment() != null) {
                final XdmItem id = XdmAtomicValue.makeAtomicValue(hrefValue.getFragment());
                final Stream<XdmNode> idStream = (Stream<XdmNode>) id(doc).apply(id);
                src = idStream.findAny().orElse(doc);
            } else {
                src = doc;
            }

            job.getStore().writeDocument(src, new IncludeFilter(contentHandler));
        } catch (IOException e) {
            logger.error("Failed to process include {}", fileInfo.src, e);
            return false;
        }
        return true;
    }
}
