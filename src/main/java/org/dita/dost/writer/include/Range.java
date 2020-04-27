/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2020 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.writer.include;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.IOException;

public interface Range {
    /**
     * Copy lines from reader to target handler
     *
     * @param codeReader line reader
     */
    void copyLines(final BufferedReader codeReader) throws IOException, SAXException;

    /**
     * Set target handler
     */
    Range handler(final ContentHandler contentHandler);
}
