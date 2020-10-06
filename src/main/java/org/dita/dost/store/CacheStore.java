/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2019 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.store;

import net.sf.saxon.dom.NodeOverNodeInfo;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.ReceivingContentHandler;
import net.sf.saxon.event.Sender;
import net.sf.saxon.lib.ParseOptions;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.TreeInfo;
import net.sf.saxon.s9api.*;
import net.sf.saxon.serialize.SerializationProperties;
import net.sf.saxon.trans.UncheckedXPathException;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.wrapper.RebasedDocument;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.util.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.helpers.XMLFilterImpl;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.net.URI;
import java.util.*;

import static org.dita.dost.util.Constants.FILE_EXTENSION_TEMP;
import static org.dita.dost.util.URLUtils.stripFragment;
import static org.dita.dost.util.URLUtils.toURI;

/**
 * DOM and memory based store, backed up by a disk store.
 */
public class CacheStore extends AbstractStore implements Store {

    private final StreamStore fallback;
    private final Map<URI, Entry> cache;

    public CacheStore(final File tempDir, final XMLUtils xmlUtils) {
        super(tempDir, xmlUtils);
        fallback = new StreamStore(tempDir, xmlUtils);
        this.cache = new HashMap<>();
    }

    @Override
    public void delete(final URI file) throws IOException {
        final URI f = file.normalize();
        if (isTempFile(f)) {
            if (cache.containsKey(f)) {
                remove(f);
                return;
            }
        }
        cacheMiss(f);
        fallback.delete(file);
    }

    @Override
    public void copy(final URI src, final URI dst) throws IOException {
        if (cache.containsKey(src)) {
            final URI s = toAbsolute(src);
            final Entry entry = get(s);
            final URI d = toAbsolute(dst);
            put(d, entry);
            return;
        }
        cacheMiss(src);
        fallback.copy(src, dst);
    }

    @Override
    public void move(final URI src, final URI dst) throws IOException {
        if (LOG) System.err.println("Cache move: " + src + " -> " + dst);
        if (cache.containsKey(src)) {
            final URI s = toAbsolute(src);
            final URI d = toAbsolute(dst);
            final Entry remove = remove(s);
            final Entry wrap = rebase(remove, d);
            put(d, wrap);
            return;
        }
        cacheMiss(src);
        fallback.move(src, dst);
    }

    @Override
    public boolean exists(final URI path) {
        final URI f = stripFragment(toAbsolute(path)).normalize();
        if (cache.containsKey(f)) {
            return true;
        }
        return fallback.exists(f);
    }

    @Override
    public long getLastModified(final URI path) {
        final URI f = stripFragment(toAbsolute(path)).normalize();
        if (cache.containsKey(f)) {
            return cache.get(f).lastModified;
        }
        return fallback.getLastModified(f);
    }

    @Override
    public Source resolve(final String href, final String base) throws TransformerException {
        final URI b = toAbsolute(toURI(base));
        final URI h = toURI(href);
        assert b.isAbsolute();
        assert h.isAbsolute();
        final URI f = b.resolve(h).normalize();
        if (LOG) System.err.println("Cache resolve: " + f);
        if (isTempFile(f)) {
            if (cache.containsKey(f)) {
                final Entry entry = get(f);
                return toSource(entry, f);
            }
        }
        return fallback.resolve(href, base);
    }

    @Override
    public Source getSource(final URI path) {
        assert path.isAbsolute();
        final URI f = getUri(path).normalize();
        if (LOG) System.err.println("Cache getSource: " + f);
        if (isTempFile(f)) {
            if (cache.containsKey(f)) {
                final Entry entry = get(f);
                return toSource(entry, f);
            }
            cacheMiss(f);
        }
        return fallback.getSource(f);
    }

    @Override
    public Document getImmutableDocument(URI path) throws IOException {
        final URI f = toAbsolute(path);
        if (LOG) System.err.println("getImmutableDocument:" + f);
        if (isTempFile(f)) {
            if (cache.containsKey(f)) {
                final Entry entry = cache.get(f);
                if (entry.doc != null) {
                    return entry.doc;
                } else if (entry.node != null) {
                    final NodeInfo nodeInfo = entry.node.getUnderlyingNode();
                    final Document doc = (Document) NodeOverNodeInfo.wrap(nodeInfo);
                    put(f, new Entry(doc, entry.node, null));
                    return doc;
                } else if (entry.bytes != null) {
                    try (InputStream in = new ByteArrayInputStream(entry.bytes)) {
                        final Document doc = XMLUtils.getDocumentBuilder().parse(in, f.toString());
                        put(f, new Entry(doc, null, entry.bytes));
                        return doc;
                    } catch (SAXException e) {
                        throw new IOException(e);
                    }
                } else {
                    throw new IllegalArgumentException();
                }
            }
            cacheMiss(f);
        }
        return fallback.getDocument(path);
    }

    @Override
    public XdmNode getImmutableNode(URI path) throws IOException {
        final URI f = toAbsolute(path);
        if (LOG) System.err.println("getImmutableNode:" + f);
        if (isTempFile(f)) {
            if (cache.containsKey(f)) {
                final Entry entry = cache.get(f);
                if (entry.node != null) {
                    return entry.node;
                } else if (entry.doc != null) {
                    final XdmNode node = xmlUtils.getProcessor().newDocumentBuilder().wrap(entry.doc);
                    put(f, new Entry(entry.doc, node, entry.bytes));
                    return node;
                } else if (entry.bytes != null) {
                    try (InputStream in = new ByteArrayInputStream(entry.bytes)) {
                        final StreamSource source = new StreamSource(in);
                        source.setSystemId(f.toString());
                        final XdmNode node = xmlUtils.getProcessor().newDocumentBuilder().build(source);
                        put(f, new Entry(entry.doc, node, entry.bytes));
                    } catch (SaxonApiException e) {
                        throw new IOException(e);
                    }
                } else {
                    throw new IllegalArgumentException();
                }
            }
            cacheMiss(f);
        }
        return fallback.getImmutableNode(path);
    }

    @Override
    public Document getDocument(final URI path) throws IOException {
        final URI f = toAbsolute(path);
        if (LOG) System.err.println("getDocument:" + f);
        if (isTempFile(f)) {
            if (cache.containsKey(f)) {
                final Entry entry = get(f);
                if (entry.doc != null) {
                    return (Document) entry.doc.cloneNode(true);
                } else if (entry.node != null) {
                    return cloneDocument(entry.node);
                } else if (entry.bytes != null) {
                    try (InputStream in = new ByteArrayInputStream(entry.bytes)) {
                        final InputSource inputSource = new InputSource(in);
                        inputSource.setSystemId(f.toString());
                        final Document doc = XMLUtils.getDocumentBuilder().parse(inputSource);
                        put(f, new Entry(doc, entry.node, entry.bytes));
                        return doc;
                    } catch (SAXException e) {
                        throw new IOException(e);
                    }
                } else {
                    throw new IllegalArgumentException();
                }
            }
            cacheMiss(f);
        }
        return fallback.getDocument(path);
    }

    @Override
    public void writeDocument(final Document doc, final URI path) throws IOException {
        if (isTempFile(path)) {
            if (LOG) System.err.println("writeDocument: " + path);
            doc.setDocumentURI(path.toString());
            final XdmNode node = xmlUtils.getProcessor().newDocumentBuilder().wrap(doc);
            final NodeInfo nodeInfo = node.getUnderlyingNode();
            if (nodeInfo.getBaseURI() == null || nodeInfo.getBaseURI().isEmpty()) {
                nodeInfo.setSystemId(doc.getBaseURI());
            }
            put(path, new Entry(null, node, null));
        } else {
            fallback.writeDocument(doc, path);
        }
    }

    @Override
    public void writeDocument(final XdmNode node, final URI path) throws IOException {
        if (isTempFile(path)) {
            if (LOG) System.err.println("writeDocument: " + path);
//            final NodeInfo nodeInfo = node.getUnderlyingNode();
//            if (nodeInfo.getBaseURI() == null || nodeInfo.getBaseURI().isEmpty()) {
//                nodeInfo.setSystemId(doc.getBaseURI());
//            }
            put(path, new Entry(null, node, null));
        } else {
            fallback.writeDocument(node, path);
        }
    }

    @Override
    public void writeDocument(final Node doc, final ContentHandler dst) throws IOException {
        fallback.writeDocument(doc, dst);
    }

    @Override
    public void writeDocument(final XdmNode node, final ContentHandler dst) throws IOException {
        fallback.writeDocument(node, dst);
    }

    @Override
    public Destination getDestination(final URI path) throws IOException {
        if (isTempFile(path)) {
            final XdmDestination dst = new XdmDestination();
            dst.setBaseURI(path);
            dst.onClose(() -> {
                final XdmNode node = dst.getXdmNode();
                put(path, new Entry(null, node, null));
            });
            return dst;
        }
        return fallback.getDestination(path);
    }

    @Override
    public ContentHandler getContentHandler(final URI outputFile) throws SaxonApiException, IOException {
        final net.sf.saxon.Configuration configuration = xmlUtils.getProcessor().getUnderlyingConfiguration();
        final PipelineConfiguration pipelineConfiguration = configuration.makePipelineConfiguration();

        final Destination dst = getDestination(outputFile);
        final Receiver receiver = dst.getReceiver(pipelineConfiguration, new SerializationProperties());

        final ReceivingContentHandler receivingContentHandler = new ReceivingContentHandler();
        receivingContentHandler.setPipelineConfiguration(pipelineConfiguration);
        receivingContentHandler.setReceiver(receiver);

        return receivingContentHandler;
    }

    @Override
    public void transform(final URI src, final ContentHandler dst) throws DITAOTException {
        final URI f = src.normalize();
        if (isTempFile(f)) {
            if (cache.containsKey(f)) {
                try {
                    final Source source = getSource(src);
                    final Receiver receiver = getReceiver(dst);
                    Sender.send(source, receiver, new ParseOptions());
                } catch (final RuntimeException e) {
                    throw e;
                } catch (final Exception e) {
                    throw new DITAOTException("Failed to transform " + src + ": " + e.getMessage(), e);
                }
                return;
            }
        }
        fallback.transform(src, dst);
    }

    @Override
    public void transform(final URI input, final List<XMLFilter> filters) throws DITAOTException {
        final URI src = input.normalize();
        if (isTempFile(src)) {
            try {
                final Source source = getSource(src);
                final ContentHandler serializer = getContentHandler(src);
                final ContentHandler pipe = getPipe(filters, serializer);
                final Receiver receiver = getReceiver(pipe);
                Sender.send(source, receiver, new ParseOptions());
                // getDestination will handle save to cache
            } catch (IOException | XPathException | SaxonApiException e) {
                throw new DITAOTException("Failed to transform " + src + ": " + e.getMessage(), e);
            }
        } else {
            fallback.transform(src, filters);
        }
    }

    @Override
    void transformURI(final URI input, final URI output, final List<XMLFilter> filters) throws DITAOTException {
        if (isTempFile(input)) {
            try {
                ContentHandler dst = getContentHandler(output);
                ContentHandler pipe = getPipe(filters, dst);
                transform(input, pipe);
            } catch (IOException | SaxonApiException e) {
                throw new DITAOTException("Failed to transform " + input + ": " + e.getMessage(), e);
            }
            return;
        }
        fallback.transformURI(input, output, filters);
    }

    @Override
    public void transform(final URI input, final URI output, final XsltTransformer transformer) throws DITAOTException {
        final URI src = input.normalize();
        final URI dst = output.normalize();
        if (src.equals(dst)) {
            transform(src, transformer);
        } else {
            transformUri(src, dst, transformer);
        }
    }

    @Override
    public void transform(final URI src, final XsltTransformer transformer) throws DITAOTException {
        final boolean useTmpBuf = !isTempFile(src.normalize());
        final URI dst = useTmpBuf
                ? toURI(src + FILE_EXTENSION_TEMP).normalize()
                : src;
        try {
            final Source source = getSource(src);
            transformer.setSource(source);
            final Destination result = getDestination(dst);
            result.setDestinationBaseURI(src);
            transformer.setDestination(result);
            transformer.transform();
            if (useTmpBuf) {
                move(dst, src);
            }
        } catch (final UncheckedXPathException e) {
            throw new DITAOTException("Failed to transform document", e);
        } catch (final SaxonApiException e) {
            throw new DITAOTException("Failed to transform document: " + e.getMessage(), e);
        } catch (final IOException e) {
            throw new DITAOTException("Failed to replace " + src + ": " + e.getMessage());
        }
    }

    @Override
    void transformUri(final URI src, final URI dst, final XsltTransformer transformer) throws DITAOTException {
        try {
            final Source source = getSource(src);
            transformer.setSource(source);
            final Destination result = getDestination(dst);
            transformer.setDestination(result);
            transformer.transform();
        } catch (final UncheckedXPathException e) {
            throw new DITAOTException("Failed to transform document", e);
        } catch (final RuntimeException e) {
            throw e;
        } catch (final SaxonApiException e) {
            throw new DITAOTException("Failed to transform document: " + e.getMessage(), e);
        } catch (final Exception e) {
            throw new DITAOTException("Failed to transform document: " + e.getMessage(), e);
        }
    }

    @Override
    public InputStream getInputStream(final URI path) throws IOException {
        final URI f = path.normalize();
        if (isTempFile(f)) {
            if (cache.containsKey(f)) {
                final Entry entry = cache.get(f);
                if (entry.bytes != null) {
                    return new ByteArrayInputStream(entry.bytes);
                } else if (entry.node != null) {
                    try (ByteArrayOutputStream buf = new ByteArrayOutputStream()) {
                        final XdmNode source = entry.node;
                        final Serializer serializer = xmlUtils.getProcessor().newSerializer(buf);
                        serializer.serializeNode(source);
                        final byte[] bytes = buf.toByteArray();
                        cache.put(f, new Entry(entry.doc, entry.node, bytes));
                        return new ByteArrayInputStream(bytes);
                    } catch (SaxonApiException e) {
                        throw new IOException(e);
                    }
                } else if (entry.doc != null) {
                    try (ByteArrayOutputStream buf = new ByteArrayOutputStream()) {
                        final XdmNode source = xmlUtils.getProcessor().newDocumentBuilder().wrap(entry.doc);
                        final Serializer serializer = xmlUtils.getProcessor().newSerializer(buf);
                        serializer.serializeNode(source);
                        final byte[] bytes = buf.toByteArray();
                        cache.put(f, new Entry(entry.doc, entry.node, bytes));
                        return new ByteArrayInputStream(bytes);
                    } catch (SaxonApiException e) {
                        throw new IOException(e);
                    }
                }
            }
        }
        return fallback.getInputStream(path);
    }

    @Override
    public OutputStream getOutputStream(final URI path) throws IOException {
        final URI f = getUri(path);
        if (LOG) System.err.println("  getOutputStream:" + f);
        if (isTempFile(f)) {
            return new OutputStreamBuffer(f);
        }
        return fallback.getOutputStream(f);
    }

    private class OutputStreamBuffer extends ByteArrayOutputStream {
        private final URI path;

        private OutputStreamBuffer(final URI path) {
            this.path = path;
        }

        @Override
        public void close() throws IOException {
            super.close();
            put(path, new Entry(null, null, toByteArray()));
        }
    }

    private ContentHandler getPipe(List<XMLFilter> filters, ContentHandler dst1) {
        final List<XMLFilter> rev = new ArrayList<>(filters);
        Collections.reverse(rev);
        for (final XMLFilter filter : rev) {
            final XMLFilterImpl filterImpl = (XMLFilterImpl) filter;
            filterImpl.setContentHandler(dst1);
            dst1 = filterImpl;
        }
        return dst1;
    }

    private Receiver getReceiver(final ContentHandler dst) {
        final SAXDestination result = new SAXDestination(dst);
        final PipelineConfiguration pipe = xmlUtils.getProcessor().getUnderlyingConfiguration().makePipelineConfiguration();
        return result.getReceiver(pipe, new SerializationProperties());
    }

    private URI toAbsolute(final URI path) {
        return (path.isAbsolute() ? path : tempDirUri.resolve(path)).normalize();
    }

    private void cacheMiss(final URI f) {
//        System.err.println("Cache miss: " + f);
//        throw new IllegalStateException("Cache miss: " + f);
    }

    private Entry put(URI path, Entry entry) {
        if (entry.node != null) {
            final XdmNode node = entry.node;
            assert node.getBaseURI() != null && !node.getBaseURI().toString().isEmpty();
            assert node.getUnderlyingNode().getBaseURI() != null && !node.getUnderlyingNode().getBaseURI().isEmpty();
        }
        if (entry.doc != null) {
            final Document doc = entry.doc;
            assert doc.getBaseURI() != null && !doc.getBaseURI().isEmpty();
        }
        return cache.put(path, entry);
    }

    private Entry get(URI s) {
        final Entry entry = cache.get(s);
        if (entry.node != null) {
            final XdmNode node = entry.node;
            assert node.getBaseURI() != null && !node.getBaseURI().toString().isEmpty();
            assert node.getUnderlyingNode().getBaseURI() != null && !node.getUnderlyingNode().getBaseURI().isEmpty();
        } else if (entry.doc != null) {
            final Document node = entry.doc;
            assert node.getBaseURI() != null && !node.getBaseURI().isEmpty();
        }
        return entry;
    }

    private Entry remove(URI f) {
        final Entry entry = cache.remove(f);
        if (entry.node != null) {
            final XdmNode node = entry.node;
            assert node.getBaseURI() != null && !node.getBaseURI().toString().isEmpty();
            assert node.getUnderlyingNode().getBaseURI() != null && !node.getUnderlyingNode().getBaseURI().isEmpty();
        } else if (entry.doc != null) {
            final Document node = entry.doc;
            assert node.getBaseURI() != null && !node.getBaseURI().isEmpty();
        }
        return entry;
    }

    private Entry rebase(final Entry remove, final URI d) {
        XdmNode node = null;
        Document doc = null;
        if (remove.node != null) {
            final TreeInfo treeInfo = remove.node.getUnderlyingNode().getTreeInfo();
//            if (treeInfo instanceof DOMNodeWrapper) {
//                Node n = ((DOMNodeWrapper) treeInfo).getUnderlyingNode();
//                n.getOwnerDocument().setDocumentURI(d.toString());
//                doc = (Document) n;
//            } else if (treeInfo instanceof DocumentWrapper) {
//                doc = (Document) ((DocumentWrapper) treeInfo).docNode;
//                doc.setDocumentURI(d.toString());
//            } else {
                final TreeInfo rebasedDocument = new RebasedDocument(treeInfo,
                        nodeInfo -> d.toString(),
                        nodeInfo -> d.toString());
                rebasedDocument.setSystemId(d.toString());
                final DocumentBuilder builder = xmlUtils.getProcessor().newDocumentBuilder();
                builder.setBaseURI(d);
                node = builder.wrap(rebasedDocument.getRootNode());
//            }
        }
        if (remove.doc != null) {
            remove.doc.setDocumentURI(d.toString());
            doc = remove.doc;
        }
        return new Entry(doc, node, remove.bytes);
    }

    private Source toSource(final Entry entry, final URI path) {
        if (entry.doc != null) {
            return new DOMSource(entry.doc);
        } else if (entry.node != null) {
            final NodeInfo underlyingNode = entry.node.getUnderlyingNode();
            if (underlyingNode.getSystemId().equals(path)) {
                return underlyingNode;
            } else {
                final Entry rebase = rebase(entry, path);
                return rebase.node.asSource();
//                final TreeInfo rebasedDocument = new RebasedDocument(underlyingNode.getTreeInfo(),
//                        nodeInfo -> path.toString(),
//                        nodeInfo -> path.toString());
//                rebasedDocument.setSystemId(path.toString());
//                return rebasedDocument;
            }
        } else if (entry.bytes != null) {
            final StreamSource source = new StreamSource(new ByteArrayInputStream(entry.bytes));
            source.setSystemId(path.toString());
            return source;
        } else {
            throw new IllegalArgumentException();
        }
    }

    private Document cloneDocument(final XdmNode node) throws IOException {
        try {
            final Document doc = XMLUtils.getDocumentBuilder().newDocument();
            final DOMDestination destination = new DOMDestination(doc);
            final Receiver receiver = destination.getReceiver(
                    xmlUtils.getProcessor().getUnderlyingConfiguration().makePipelineConfiguration(),
                    new SerializationProperties());
            Sender.send(node.asSource(), receiver, new ParseOptions());
            // Don't save mutable doc into cache
            return doc;
        } catch (XPathException e) {
            throw new IOException(e);
        }
    }

    private static class Entry {
        private final Document doc;
        private final XdmNode node;
        private final byte[] bytes;
        private long lastModified;

        private Entry(final Document doc, final XdmNode node, final byte[] bytes) {
            this(doc, node, bytes, System.currentTimeMillis());
        }

        private Entry(final Document doc, final XdmNode node, final byte[] bytes, final long lastModified) {
            this.doc = doc;
            this.node = node;
            this.bytes = bytes;
            this.lastModified = lastModified;
        }
    }
}
