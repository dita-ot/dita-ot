/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2013 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.writer;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.reader.AbstractReader;
import org.dita.dost.util.Job;
import org.dita.dost.util.XMLUtils;
import org.w3c.dom.Document;

import java.io.File;
import java.io.IOException;

/**
 * Reads XML into DOM, modifies it, and serializes back into XML.
 */
public abstract class AbstractDomFilter implements AbstractReader {

    protected DITAOTLogger logger;
    protected Job job;

    @Override
    public void read(final File filename) throws DITAOTException {
        assert filename.isAbsolute();
        logger.info("Processing " + filename.toURI());
        Document doc;
        try {
            doc = job.getStore().getDocument(filename.toURI());
        } catch (final RuntimeException e) {
            throw e;
        } catch (final Exception e) {
            throw new DITAOTException("Failed to parse " + filename.getAbsolutePath() + ":" + e.getMessage(), e);
        }

        final Document resDoc = process(doc);

        if (resDoc != null) {
            try {
                logger.debug("Writing " + filename.toURI());
                resDoc.setDocumentURI(filename.toURI().toString());
                job.getStore().writeDocument(resDoc, filename.toURI());
            } catch (final IOException e) {
                throw new DITAOTException("Failed to serialize " + filename.getAbsolutePath() + ": " + e.getMessage(), e);
            }
        }
    }

    @Override
    public void setLogger(final DITAOTLogger logger) {
        this.logger = logger;
    }

    @Override
    public void setJob(final Job job) {
        this.job = job;
    }

    /**
     * Modify document.
     *
     * @param doc document to modify
     * @return modified document, may be argument document; if {@code null}, document is not serialized
     */
    protected abstract Document process(final Document doc);

}
