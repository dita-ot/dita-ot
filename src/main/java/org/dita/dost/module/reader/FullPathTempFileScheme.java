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

public class FullPathTempFileScheme implements TempFileNameScheme {
    @Override
    public URI generateTempFileName(final URI src) {
        assert src.isAbsolute();
        final URI rel = toURI(src.getPath().substring(1));
        return rel;
    }
}
