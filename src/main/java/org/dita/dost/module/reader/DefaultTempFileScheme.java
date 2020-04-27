/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2019 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.module.reader;

import java.net.URI;

import static org.dita.dost.util.URLUtils.toURI;

public class DefaultTempFileScheme implements TempFileNameScheme {
    URI b;
    @Override
    public void setBaseDir(final URI b) {
        this.b = b;
    }
    @Override
    public URI generateTempFileName(final URI src) {
        assert src.isAbsolute();
        //final URI b = baseInputDir.toURI();
        final URI rel = toURI(b.relativize(src).toString());
        return rel;
    }
}
