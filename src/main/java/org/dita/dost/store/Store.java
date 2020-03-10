/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2019 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.store;

import org.dita.dost.exception.DITAOTException;
import org.w3c.dom.Document;
import org.xml.sax.XMLFilter;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.List;

public interface Store {

    /**
     * Transform file with XML filters.
     *
     * @param src file to transform and replace
     * @param filters XML filters to transform file with, may be an empty list
     */
    void transform(final URI src, final List<XMLFilter> filters) throws DITAOTException;

    /**
     * Transform file with XML filters.
     *
     * @param src input file
     * @param dst output file
     * @param filters XML filters to transform file with, may be an empty list
     */
    void transform(final URI src, final URI dst, final List<XMLFilter> filters) throws DITAOTException;

    /**
     * Get temporary file source
     *
     * @param path temporary file URI, absolute or relative
     * @return source for temporary file
     */
    Source getSource(URI path);

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

    /**
     * Get temporary file result
     *
     * @param path temporary file URI, absolute or relative
     * @return result for temporary file
     */
    Result getResult(URI path);

}
