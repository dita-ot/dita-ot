/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2019 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.store;

import net.sf.saxon.s9api.XsltTransformer;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.util.XMLUtils;
import org.xml.sax.XMLFilter;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;

import static org.dita.dost.util.Constants.FILE_EXTENSION_TEMP;
import static org.dita.dost.util.URLUtils.toURI;

/**
 * Common base class for store implementations.
 */
public abstract class AbstractStore implements Store {

    protected final XMLUtils xmlUtils;
    public final File tempDir;
    public final URI tempDirUri;

    public AbstractStore(final File tempDir, final XMLUtils xmlUtils) {
        if (!tempDir.isAbsolute()) {
            throw new IllegalArgumentException("Temporary directory " + tempDir + " must be absolute");
        }
        this.tempDirUri = tempDir.toURI();
        this.tempDir = tempDir;
        this.xmlUtils = xmlUtils;
    }

    @Override
    public URI getUri(final URI path) {
        return tempDirUri.resolve(path).normalize();
    }

    protected boolean isTempFile(final URI f) {
        return f.toString().startsWith(tempDirUri.toString());
    }

    @Override
    public void transform(final URI input, final URI output, final List<XMLFilter> filters) throws DITAOTException {
        final URI src = input.normalize();
        final URI dst = output.normalize();
        if (src.equals(dst)) {
            transform(src, filters);
        } else {
            transformURI(src, dst, filters);
        }
    }

    @Override
    public void transform(final URI src, final List<XMLFilter> filters) throws DITAOTException {
        final URI dst = toURI(src.toString() + FILE_EXTENSION_TEMP).normalize();
        transformURI(src, dst, filters);
        try {
            move(dst, src);
        } catch (final RuntimeException e) {
            throw e;
        } catch (final IOException e) {
            throw new DITAOTException("Failed to replace " + src + ": " + e.getMessage());
        }
    }

    abstract void transformURI(final URI input, final URI output, final List<XMLFilter> filters) throws DITAOTException;

    abstract void transformUri(final URI input, final URI output, final XsltTransformer transformer) throws DITAOTException;

}
