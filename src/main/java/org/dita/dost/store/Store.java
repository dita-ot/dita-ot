/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2019 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.store;

import org.w3c.dom.Document;

import java.io.IOException;
import java.net.URI;

public interface Store {

    /**
     * Get DOM document for file. Convenience method.
     *
     * @param path temporary file URI, absolute or relative
     * @return document or null if not available
     * @throws java.io.FileNotFoundException if file does not exist or cannot be read
     */
    Document getDocument(URI path) throws IOException;

    /**
     * Write DOM document to file.
     *
     * @param doc document to store
     * @param dst destination file URI, absolute or relative
     * @throws IOException if serializing file fails
     */
    void writeDocument(Document doc, URI dst) throws IOException;

}
