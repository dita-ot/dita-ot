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

public class FlattenTempFileScheme implements TempFileNameScheme {
    @Override
    public URI generateTempFileName(final URI src) {
        assert src.isAbsolute();
        return toURI(src.getPath().substring(1)
                .replaceAll("[/\\\\]", "__")
                .replaceAll("\\s+", "-"));
    }
}
