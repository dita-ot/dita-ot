/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2011 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.writer;

import java.io.File;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.util.Job;
import org.dita.dost.util.XMLUtils;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * Base for XML filters.
 *
 * @since 1.5.4
 * @author Jarno Elovirta
 */
public abstract class AbstractXMLFilter extends XMLFilterImpl implements AbstractWriter {

    protected DITAOTLogger logger;
    protected Job job;
    /** Absolute temporary directory URI to file being processed */
    protected URI currentFile;
    protected final Map<String, String> params = new HashMap<>();

    @Override
    public void write(final File filename) throws DITAOTException {
        assert filename.isAbsolute();
        job.getStore().transform(filename.toURI(), Collections.singletonList(this));
    }

    @Override
    public void setLogger(final DITAOTLogger logger) {
        this.logger = logger;
    }

    @Override
    public void setJob(final Job job) {
        this.job = job;
    }

    public void setCurrentFile(final URI currentFile) {
        assert currentFile.isAbsolute();
        this.currentFile = currentFile;
    }

    public void setParam(final String name, final String value) {
        params.put(name, value);
    }
}
