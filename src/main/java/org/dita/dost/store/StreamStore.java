/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2020 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.store;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmNode;
import org.dita.dost.util.XMLUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.net.URI;

public class StreamStore implements Store {

    private final XMLUtils xmlUtils;

    public StreamStore(XMLUtils xmlUtils) {
        this.xmlUtils = xmlUtils;
    }

    /**
     * Get DOM document for file. Convenience method.
     *
     * @param path temporary file URI, absolute or relative
     * @throws java.io.FileNotFoundException if file does not exist or cannot be read
     */
    @Override
    public Document getDocument(final URI path) throws IOException {
        try {
            return XMLUtils.getDocumentBuilder().parse(path.toString());
        } catch (final IOException | SAXException e) {
            throw new IOException("Failed to read document: " + e.getMessage(), e);
        }
    }

    /**
     * Write DOM document to file.
     *
     * @param doc document to store
     * @param dst absolute destination file
     * @throws IOException if serializing file fails
     */
    public void writeDocument(final Document doc, final File dst) throws IOException {
        try {
            final Serializer serializer = xmlUtils.getProcessor().newSerializer(dst);
            final XdmNode source = xmlUtils.getProcessor().newDocumentBuilder().wrap(doc);
            serializer.serializeNode(source);
        } catch (SaxonApiException e) {
            throw new IOException(e);
        }
    }

    /**
     * Write DOM document to file.
     *
     * @param doc document to store
     * @param dst absolute destination file URI
     * @throws IOException if serializing file fails
     */
    @Override
    public void writeDocument(final Document doc, final URI dst) throws IOException {
        writeDocument(doc, new File(dst));
    }
}
