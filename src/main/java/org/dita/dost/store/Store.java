/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2020 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.store;

import net.sf.saxon.s9api.Destination;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;
import org.dita.dost.exception.DITAOTException;
import org.w3c.dom.Document;
import org.xml.sax.ContentHandler;
import org.xml.sax.XMLFilter;

import javax.xml.transform.Source;
import java.io.IOException;
import java.net.URI;
import java.util.List;

/**
 * Abstract XML I/O.
 *
 * @since 3.5
 */
public interface Store {

    /**
     * Read XML into SAX pipe.
     *
     * @param src file to read
     * @param contentHandler SAX pipe to read to
     */
    void transform(final URI src, final ContentHandler contentHandler) throws DITAOTException;

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
     * Get immutable DOM document for file. If mutating methods are called,
     * {@link UnsupportedOperationException} is thrown.
     *
     * @param path temporary file URI, absolute or relative
     * @return document or null if not available
     * @throws java.io.FileNotFoundException if file does not exist or cannot be read
     */
    Document getImmutableDocument(URI path) throws IOException;

    /**
     * Get immutable XsdNode for file.
     *
     * @param path temporary file URI, absolute or relative
     * @return document or null if not available
     * @throws java.io.FileNotFoundException if file does not exist or cannot be read
     */
    XdmNode getImmutableNode(final URI path) throws IOException;

    /**
     * Get DOM document for file.
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
     * Write XdmNode to file.
     *
     * @param node XdmNode to store
     * @param dst destination file URI, absolute or relative
     * @throws IOException if serializing file fails
     */
    void writeDocument(XdmNode node, URI dst) throws IOException;

    /**
     * Get temporary file destination
     *
     * @param path temporary file URI, absolute or relative
     * @return destination for temporary file
     */
    Destination getDestination(URI path) throws IOException;

    /**
     * Get result content handler.
     *
     * @param path temporary file URI, absolute or relative
     * @return serializer content handler
     * @throws SaxonApiException if creating serializer fails
     */
    ContentHandler getContentHandler(URI path) throws SaxonApiException, IOException;
}
