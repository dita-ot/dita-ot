/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2019 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.module.reader;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import org.apache.commons.io.FilenameUtils;

import java.net.URI;

import static org.dita.dost.util.URLUtils.stripFragment;
import static org.dita.dost.util.URLUtils.toURI;

public class HashTempFileScheme implements TempFileNameScheme {
    @Override
    public URI generateTempFileName(final URI src) {
        assert src.isAbsolute();
        final String ext = FilenameUtils.getExtension(src.getPath());
        final String path = stripFragment(src.normalize()).toString();
        final String hash = Hashing.sha1()
                .hashString(path, Charsets.UTF_8)
                .toString();
        return toURI(ext.isEmpty() ? hash : (hash + "." + ext));
    }
}
