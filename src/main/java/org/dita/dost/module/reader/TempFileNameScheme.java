/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2019 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.module.reader;

import java.net.URI;

/**
 * Temporary file name generator.
 */
public interface TempFileNameScheme {
    /**
     * Set input base directory.
     * @param b absolute base directory
     */
    default void setBaseDir(final URI b) {}
    /**
     * Generate temporary file name.
     *
     * @param src absolute source file URI
     * @return relative temporary file URI
     */
    URI generateTempFileName(final URI src);
}
