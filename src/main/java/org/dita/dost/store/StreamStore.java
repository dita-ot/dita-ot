/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2020 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.store;

import com.google.common.annotations.VisibleForTesting;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.ReceivingContentHandler;
import net.sf.saxon.s9api.Destination;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.serialize.SerializationProperties;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.util.XMLUtils;
import org.w3c.dom.Document;
import org.xml.sax.*;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;

import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.apache.commons.io.FileUtils.moveFile;
import static org.dita.dost.util.Constants.FILE_EXTENSION_TEMP;

/**
 * Stream based XML I/O
 *
 * @since 3.5
 */
public class StreamStore implements Store {

    private final XMLUtils xmlUtils;

    public StreamStore(XMLUtils xmlUtils) {
        this.xmlUtils = xmlUtils;
    }

    @Override
    public Document getImmutableDocument(final URI path) throws IOException {
//        return (Document) NodeOverNodeInfo.wrap(getImmutableNode(path).getUnderlyingNode());
        return getDocument(path);
    }

    @Override
    public XdmNode getImmutableNode(final URI path) throws IOException {
        try {
            return xmlUtils.getProcessor().newDocumentBuilder().build(new StreamSource(path.toString()));
        } catch (SaxonApiException e) {
            throw new IOException(e);
        }
    }

    @Override
    public Document getDocument(final URI path) throws IOException {
        try {
            return XMLUtils.getDocumentBuilder().parse(path.toString());
        } catch (final IOException | SAXException e) {
            throw new IOException("Failed to read document: " + e.getMessage(), e);
        }
    }

    @Override
    public void writeDocument(final Document doc, final URI dst) throws IOException {
        final XdmNode source = xmlUtils.getProcessor().newDocumentBuilder().wrap(doc);
        writeDocument(source, dst);
    }

    @Override
    public void writeDocument(final XdmNode node, final URI dst) throws IOException {
        try {
            final Serializer serializer = getSerializer(dst);
            serializer.serializeNode(node);
        } catch (SaxonApiException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void transform(final URI input, final ContentHandler contentHandler) throws DITAOTException {
        assert input.isAbsolute();
        if (!input.getScheme().equals("file")) {
            throw new IllegalArgumentException("Only file URI scheme supported: " + input);
        }

        try {
            final XMLReader xmlReader = XMLUtils.getXMLReader();
            xmlReader.setContentHandler(contentHandler);
            xmlReader.parse(input.toString());
        } catch (SAXException | IOException e) {
            throw new DITAOTException(e);
        }
    }

    @Override
    public void transform(final URI input, final List<XMLFilter> filters) throws DITAOTException {
        assert input.isAbsolute();
        if (!input.getScheme().equals("file")) {
            throw new IllegalArgumentException("Only file URI scheme supported: " + input);
        }

        final File inputFile = new File(input);
        final File outputFile = new File(inputFile.getAbsolutePath() + FILE_EXTENSION_TEMP);
        transformURI(inputFile.toURI(), outputFile.toURI(), filters);
        try {
            deleteQuietly(inputFile);
            moveFile(outputFile, inputFile);
        } catch (final IOException e) {
            throw new DITAOTException("Failed to replace " + inputFile + ": " + e.getMessage());
        }
    }

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

    @VisibleForTesting
    Serializer getSerializer(final URI dst) {
        final File outputFile = new File(dst);
        return xmlUtils.getProcessor().newSerializer(outputFile);
    }

    @Override
    public Source getSource(URI path) {
        return new StreamSource(path.toString());
    }

    @Override
    public Destination getDestination(URI path) {
        return getSerializer(path);
    }

    @Override
    public ContentHandler getContentHandler(final URI outputFile) throws SaxonApiException {
        final net.sf.saxon.Configuration configuration = xmlUtils.getProcessor().getUnderlyingConfiguration();
//        final SerializerFactory sf = configuration.getSerializerFactory();
        final PipelineConfiguration pipelineConfiguration = configuration.makePipelineConfiguration();

        final Destination dst = getDestination(outputFile);
        final Receiver receiver = dst.getReceiver(pipelineConfiguration, new SerializationProperties());
//        final Result out = job.getStore().getResult(outputFile);
//        final Receiver receiver = sf.getReceiver(out, new SerializationProperties());

        final ReceivingContentHandler receivingContentHandler = new ReceivingContentHandler();
        receivingContentHandler.setPipelineConfiguration(pipelineConfiguration);
        receivingContentHandler.setReceiver(receiver);

        return receivingContentHandler;
    }
}
