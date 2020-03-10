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
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.util.XMLUtils;
import org.w3c.dom.Document;
import org.xml.sax.*;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.net.URI;
import java.util.List;

import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.apache.commons.io.FileUtils.moveFile;
import static org.dita.dost.util.Constants.FILE_EXTENSION_TEMP;

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
            final Serializer serializer = getSerializer(dst.toURI());
            final XdmNode source = xmlUtils.getProcessor().newDocumentBuilder().wrap(doc);
            serializer.serializeNode(source);
        } catch (DITAOTException | SaxonApiException e) {
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

    /**
     * Transform file with XML filters. Only file URIs are supported.
     *
     * @param input absolute URI to transform and replace
     * @param filters XML filters to transform file with, may be an empty list
     */
    @Override
    public void transform(final URI input, final List<XMLFilter> filters) throws DITAOTException {
        assert input.isAbsolute();
        if (!input.getScheme().equals("file")) {
            throw new IllegalArgumentException("Only file URI scheme supported: " + input);
        }

        transform(new File(input), filters);
    }

    /**
     * Transform file with XML filters.
     *
     * @param inputFile file to transform and replace
     * @param filters XML filters to transform file with, may be an empty list
     */
    private void transform(final File inputFile, final List<XMLFilter> filters) throws DITAOTException {
        final File outputFile = new File(inputFile.getAbsolutePath() + FILE_EXTENSION_TEMP);
        transformFile(inputFile, outputFile, filters);
        try {
            deleteQuietly(inputFile);
            moveFile(outputFile, inputFile);
        } catch (final RuntimeException e) {
            throw e;
        } catch (final Exception e) {
            throw new DITAOTException("Failed to replace " + inputFile + ": " + e.getMessage());
        }
    }

//    /**
//     * Transform file with XML filters.
//     *
//     * @param inputFile input file
//     * @param outputFile output file
//     * @param filters XML filters to transform file with, may be an empty list
//     */
//    public void transform(final File inputFile, final File outputFile, final List<XMLFilter> filters) throws DITAOTException {
//        if (inputFile.equals(outputFile)) {
//            transform(inputFile, filters);
//        } else {
//            transformFile(inputFile, outputFile, filters);
//        }
//    }

    private void transformFile(final File inputFile, final File outputFile, final List<XMLFilter> filters) throws DITAOTException {
        if (!outputFile.getParentFile().exists() && !outputFile.getParentFile().mkdirs()) {
            throw new DITAOTException("Failed to create output directory " + outputFile.getParentFile().getAbsolutePath());
        }

        try {
            XMLReader reader = xmlUtils.getXMLReader();
            for (final XMLFilter filter : filters) {
                // ContentHandler must be reset so e.g. Saxon 9.1 will reassign ContentHandler
                // when reusing filter with multiple Transformers.
                filter.setContentHandler(null);
                filter.setParent(reader);
                reader = filter;
            }

            final Serializer result = getSerializer(outputFile.toURI());
            final ContentHandler serializer = result.getContentHandler();
            reader.setContentHandler(serializer);

            final InputSource inputSource = new InputSource(inputFile.toURI().toString());

            reader.parse(inputSource);
        } catch (final RuntimeException e) {
            throw e;
        } catch (final Exception e) {
            throw new DITAOTException("Failed to transform " + inputFile + ": " + e.getMessage(), e);
        }
    }

    /**
     * Transform file with XML filters.
     *
     * @param input input file
     * @param output output file
     * @param filters XML filters to transform file with, may be an empty list
     */
    @Override
    public void transform(final URI input, final URI output, final List<XMLFilter> filters) throws DITAOTException {
        if (input.equals(output)) {
            transform(input, filters);
        } else {
            transformURI(input, output, filters);
        }
    }

    private void transformURI(final URI input, final URI output, final List<XMLFilter> filters) throws DITAOTException {
        final File outputFile = new File(output);
        if (!outputFile.getParentFile().exists() && !outputFile.getParentFile().mkdirs()) {
            throw new DITAOTException("Failed to create output directory " + outputFile.getParentFile().getAbsolutePath());
        }

        try {
            XMLReader reader = xmlUtils.getXMLReader();
            for (final XMLFilter filter : filters) {
                // ContentHandler must be reset so e.g. Saxon 9.1 will reassign ContentHandler
                // when reusing filter with multiple Transformers.
                filter.setContentHandler(null);
                filter.setParent(reader);
                reader = filter;
            }

            final Serializer result = getSerializer(output);
            final ContentHandler serializer = result.getContentHandler();
            reader.setContentHandler(serializer);

            final InputSource inputSource = new InputSource(input.toString());

            reader.parse(inputSource);
        } catch (final RuntimeException e) {
            throw e;
        } catch (final Exception e) {
            throw new DITAOTException("Failed to transform " + input + ": " + e.getMessage(), e);
        }
    }

    private Serializer getSerializer(final URI dst) throws DITAOTException {
        final File outputFile = new File(dst);
        if (!outputFile.getParentFile().exists() && !outputFile.getParentFile().mkdirs()) {
            throw new DITAOTException("Failed to create output directory " + outputFile.getParentFile().getAbsolutePath());
        }
        return xmlUtils.getProcessor().newSerializer(outputFile);
    }

    @Override
    public Source getSource(URI path) {
        return new StreamSource(path.toString());
    }

    @Override
    public Result getResult(URI path) {
        return new StreamResult(path.toString());
    }
}
