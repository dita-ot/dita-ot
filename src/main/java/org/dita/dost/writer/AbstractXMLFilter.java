/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */
package org.dita.dost.writer;

import static java.util.Arrays.asList;

import java.io.File;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.util.Job;
import org.dita.dost.util.XMLUtils;
import org.xml.sax.XMLFilter;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * Base for XML filters.
 *
 * @since 1.5.4
 * @author Jarno Elovirta
 */
public abstract class AbstractXMLFilter extends XMLFilterImpl implements AbstractWriter {

    protected DITAOTLogger logger;
    Job job;
    /** Absolute system path to file being processed */
    protected URI currentFile;
    protected Map<String, String> params = new HashMap<>();

    @Override
    public void write(final File filename) throws DITAOTException {
        assert filename.isAbsolute();
        XMLUtils.transform(filename, Collections.singletonList((XMLFilter) this));
    }

    @Override
    public final void setLogger(final DITAOTLogger logger) {
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
