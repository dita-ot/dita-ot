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
import net.sf.saxon.s9api.XsltTransformer;
import org.dita.dost.exception.DITAOTException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;
import org.xml.sax.XMLFilter;

import javax.xml.transform.Source;
import javax.xml.transform.URIResolver;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;

/**
 * Abstract XML I/O.
 *
 * <p>The following file-system like operations are provided:</p>
 * <ul>
 * <li>read</li>
 * <li>write</li>
 * <li>test for existence</li>
 * </ul>
 * <p>The following convenience operations are provided:</p>
 * <ul>
 * <li>copy</li>
 * <li>move</li>
 * <li>transform</li>
 * </ul>
 *
 * @since 3.5
 */
public interface Store extends URIResolver {

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
     * Transform file with XSLT.
     *
     * @param src file to transform and replace
     * @param transformer XSLT transformer to transform file with
     */
    void transform(final URI src, final XsltTransformer transformer) throws DITAOTException;

    /**
     * Transform file with XSLT.
     *
     * @param src input file
     * @param dst output file
     * @param transformer XSLT transformer to transform file with
     */
    void transform(final URI src, final URI dst, final XsltTransformer transformer) throws DITAOTException;

    /**
     * Get temporary file source
     *
     * @param path temporary file URI, absolute or relative
     * @return source for temporary file
     */
    Source getSource(URI path);

    /**
     * Get immutable DOM document for resource. If mutating methods are called,
     * {@link UnsupportedOperationException} is thrown.
     *
     * @param path temporary file URI, absolute or relative
     * @return document or null if not available
     * @throws java.io.FileNotFoundException if file does not exist or cannot be read
     */
    Document getImmutableDocument(URI path) throws IOException;

    /**
     * Get immutable XsdNode for resource.
     *
     * @param path temporary file URI, absolute or relative
     * @return document or null if not available
     * @throws java.io.FileNotFoundException if file does not exist or cannot be read
     */
    XdmNode getImmutableNode(final URI path) throws IOException;

    /**
     * Get DOM document for resource.
     *
     * @param path temporary file URI, absolute or relative
     * @return document or null if not available
     * @throws java.io.FileNotFoundException if file does not exist or cannot be read
     */
    Document getDocument(URI path) throws IOException;

    /**
     * Write DOM document to store.
     *
     * @param doc document to store
     * @param dst destination file URI, absolute or relative
     * @throws IOException if serializing file fails
     */
    void writeDocument(Document doc, URI dst) throws IOException;

    /**
     * Write DOM document to SAX pipe.
     *
     * @param doc document to save
     * @param dst SAX pipe to write to
     * @throws IOException if serializing file fails
     */
    void writeDocument(Node doc, final ContentHandler dst) throws IOException;

    /**
     * Write XsdNode to store.
     *
     * @param node document to save
     * @param dst destination file URI, absolute or relative
     * @throws IOException if serializing file fails
     */
    void writeDocument(XdmNode node, final ContentHandler dst) throws IOException;

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

    /**
     * Get temporary file absolute URI
     *
     * @param path temporary file relative URI
     * @return absolute URI to temporary file
     */
    URI getUri(URI path);

    /**
     * Delete temporary file
     *
     * @param file file to delete, absolute or relative
     * @throws java.io.IOException if deleting failed
     */
    void delete(URI file) throws IOException;

    /**
     * Copy temporary file
     *
     * @param src source file, absolute or relative
     * @param dst destination file, absolute or relative
     * @throws java.io.IOException if copying failed
     */
    void copy(URI src, URI dst) throws IOException;

    /**
     * Check if file exits
     *
     * @param path file to test, absolute or relative
     * @return {@code true} if file exists, otherwise {@code false}
     */
    boolean exists(URI path);

    /**
     * Returns the time that the resouce was last modified.
     * @param path file to test, absolute or relative
     * @return epoch timestamp or zero
     */
    long getLastModified(URI path);

    /**
     * Move temporary file
     *
     * @param src source file, absolute or relative
     * @param dst destination file, absolute or relative
     * @throws java.io.IOException if moving failed
     */
    void move(URI src, URI dst) throws IOException;

    /**
     * Get input stream. Intended for non-XML sources.
     *
     * @param path temporary file URI, absolute or relative
     * @return input stream for temporary file
     */
    InputStream getInputStream(URI path) throws IOException;

    /**
     * Get output stream. Intended for non-XML sources.
     *
     * @param path temporary file URI, absolute or relative
     * @return output stream for temporary file
     */
    OutputStream getOutputStream(URI path) throws IOException;
}
