/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2021 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.chunk;

import net.sf.saxon.s9api.XdmNode;
import org.dita.dost.chunk.ChunkOperation.ChunkBuilder;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.module.AbstractPipelineModuleImpl;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.util.Job.FileInfo;
import org.dita.dost.util.URLUtils;
import org.dita.dost.util.XMLUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

import static net.sf.saxon.s9api.streams.Steps.attribute;
import static org.dita.dost.chunk.ChunkOperation.Operation.COMBINE;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.URLUtils.getRelativePath;
import static org.dita.dost.util.URLUtils.setFragment;
import static org.dita.dost.util.XMLUtils.*;

public class ChunkModule extends AbstractPipelineModuleImpl {
    @Override
    public AbstractPipelineOutput execute(final AbstractPipelineInput input) throws DITAOTException {
        try {
            // read modifiable map
            final FileInfo in = job.getFileInfo(fi -> fi.isInput).iterator().next();
            final URI mapFile = job.tempDirURI.resolve(in.uri);
            final Document mapDoc = job.getStore().getDocument(mapFile);
            List<ChunkOperation> chunks = new ArrayList<>();
            final Map<URI, URI> rewriteMap = new HashMap<>();
            // walk topicref | map
            walk(mapFile, mapDoc.getDocumentElement(), chunks);
            chunks = rewrite(mapFile, rewriteMap, chunks);
            rewriteTopicrefs(mapFile, chunks);
            job.getStore().writeDocument(mapDoc, mapFile);
            // for each chunk
            for (ChunkOperation chunk : chunks) {
                //   recursively merge chunk topics
                final Document chunkDoc = merge(chunk);
                rewriteLinks(chunkDoc, chunk, rewriteMap);
                chunkDoc.normalizeDocument();
                job.getStore().writeDocument(chunkDoc, setFragment(chunk.dst, null));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Rewrite topicrefs.
     */
    private void rewriteTopicrefs(final URI mapFile, final List<ChunkOperation> chunks) {
        for (ChunkOperation chunk : chunks) {
            final URI dst = getRelativePath(mapFile.resolve("."), chunk.dst);
            chunk.topicref.setAttribute(ATTRIBUTE_NAME_HREF, dst.toString());
            rewriteTopicrefs(mapFile, chunk.children);
        }
    }

    /**
     * Based on topic moves in chunk, rewrite link in a topic.
     */
    private void rewriteLinks(final Document chunkDoc,
                              final ChunkOperation chunk,
                              final Map<URI, URI> rewriteMap) {
        for (Element link : getChildElements(chunkDoc.getDocumentElement(), TOPIC_LINK, true)) {
            final URI href = URLUtils.toURI(link.getAttribute(ATTRIBUTE_NAME_HREF));
            final URI abs = chunk.src.resolve(href);
            final URI rewrite = rewriteMap.get(abs);
            if (rewrite != null) {
                final URI rel = getRelativePath(chunk.src.resolve("."), rewrite);
                link.setAttribute(ATTRIBUTE_NAME_HREF, rel.toString());
            } else {
                final URI rel = getRelativePath(chunk.src.resolve("."), abs);
                link.setAttribute(ATTRIBUTE_NAME_HREF, rel.toString());
            }
        }
    }

    /**
     * Rewrite chunks and collect topic moves.
     */
    private List<ChunkOperation> rewrite(final URI mapFile,
                                         final Map<URI, URI> rewriteMap,
                                         final List<ChunkOperation> chunks) {
        final List<ChunkOperation> list = new ArrayList<>();
        for (ChunkOperation rootChunk : chunks) {
            list.add(rewriteChunk(mapFile, rewriteMap, rootChunk).build());
        }
        return list;
    }

    private ChunkBuilder rewriteChunk(final URI mapFile,
                                      final Map<URI, URI> rewriteMap,
                                      final ChunkOperation rootChunk) {
        String id = rootChunk.src != null ? getRootTopicId(rootChunk.src) : null;
        URI dst = rootChunk.src != null ? setFragment(rootChunk.src, id) : mapFile.resolve("Chunk1.dita");
        final Collection<URI> values = rewriteMap.values();
        for (int i = 1; id == null || values.contains(dst); i++) {
            id = "Chunk" + i;
            dst = setFragment(rootChunk.src != null
                    ? setFragment(rootChunk.src, id)
                    : mapFile.resolve(id + ".dita"), id);
        }
        final ChunkBuilder builder = new ChunkBuilder(rootChunk.operation)
                .topicref(rootChunk.topicref)
                .src(rootChunk.src)
                .dst(dst)
                .id(id);
        rewriteMap.put(rootChunk.src, dst);
        for (ChunkOperation child : rootChunk.children) {
            final ChunkBuilder childBuilder = rewriteChunkChild(
                    rewriteMap,
                    rootChunk.src != null ? rootChunk.src : dst,
                    child);
            builder.addChild(childBuilder);
        }
        return builder;
    }

    private ChunkBuilder rewriteChunkChild(final Map<URI, URI> rewriteMap,
                                           final URI rootChunkSrc,
                                           final ChunkOperation chunk) {
        String id = chunk.src != null ? getRootTopicId(chunk.src) : null;
        URI dst = setFragment(rootChunkSrc, id);
        final Collection<URI> values = rewriteMap.values();
        for (int i = 1; id == null || values.contains(dst); i++) {
            id = "unique_" + i;
            dst = setFragment(rootChunkSrc, id);
        }
        final ChunkBuilder builder = new ChunkBuilder(chunk.operation)
                .topicref(chunk.topicref)
                .src(chunk.src)
                .dst(dst)
                .id(id);
        rewriteMap.put(chunk.src, dst);
        for (ChunkOperation child : chunk.children) {
            builder.addChild(rewriteChunkChild(rewriteMap, rootChunkSrc, child));
        }
        return builder;
    }

    /**
     * Get root topic ID.
     */
    private String getRootTopicId(final URI src) {
        try {
            final XdmNode node = job.getStore().getImmutableNode(src);
            return node.select(rootElement().then(attribute(ATTRIBUTE_NAME_ID))).asString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Merge chunk tree into a single topic.
     */
    private Document merge(final ChunkOperation rootChunk) throws IOException {
        if (rootChunk.src != null) {
            final Document doc = job.getStore().getDocument(rootChunk.src);
            final Element ditaWrapper = createDitaElement(doc);
            final Element dstTopic = (Element) doc.replaceChild(ditaWrapper, doc.getDocumentElement());
            ditaWrapper.appendChild(dstTopic);
            mergeTopic(rootChunk, rootChunk, dstTopic);
            return doc;
        } else {
//            final Document doc = emptyDocument(rootChunk.id);
            final Document doc = XMLUtils.getDocumentBuilder().newDocument();
            final Element ditaWrapper = createDitaElement(doc);
            doc.appendChild(ditaWrapper);
//            final Element dstTopic = (Element) doc.replaceChild(ditaWrapper, doc.getDocumentElement());
//            ditaWrapper.appendChild(dstTopic);
            mergeTopic(rootChunk, rootChunk, ditaWrapper);
            return doc;
        }
    }

    private Element createDitaElement(final Document doc) {
        final Element ditaWrapper = doc.createElement(ELEMENT_NAME_DITA);
        ditaWrapper.setAttributeNS(DITA_NAMESPACE, ATTRIBUTE_PREFIX_DITAARCHVERSION + ":" + ATTRIBUTE_NAME_DITAARCHVERSION, "2.0");
        return ditaWrapper;
    }

    /**
     * Merge chunk tree fragment into a single topic.
     */
    private void mergeTopic(final ChunkOperation rootChunk,
                            final ChunkOperation chunk,
                            final Element dstTopic) throws IOException {
        for (ChunkOperation child : chunk.children) {
            final Element imported;
            if (child.src != null) {
                final Document chunkDoc = job.getStore().getDocument(child.src);
                final Element topic = chunkDoc.getDocumentElement();
                imported = (Element) dstTopic.getOwnerDocument().importNode(topic, true);
                rewriteTopicId(imported, child.id);
                relativizeLinks(imported, child.src, rootChunk.dst);
//            } else if (MAPGROUP_D_TOPICHEAD.matches(child.topicref)) {
//                imported = emptyTopic(dstTopic.getOwnerDocument(), child.id);
            } else {
                imported = emptyTopic(dstTopic.getOwnerDocument(), child.id);
//                imported = null;
            }
//            if (imported != null) {
            final Element added = (Element) dstTopic.appendChild(imported);
            mergeTopic(rootChunk, child, added);
//            } else {
//                mergeTopic(rootChunk, child, dstTopic);
//            }
        }
    }

    private Document emptyDocument(final String id) {
        final Document doc = XMLUtils.getDocumentBuilder().newDocument();
        doc.appendChild(emptyTopic(doc, id));
        return doc;
    }

    private Element emptyTopic(final Document doc, final String id) {
        final Element imported = doc.createElement(TOPIC_TOPIC.localName);
        imported.setAttribute(ATTRIBUTE_NAME_CLASS, TOPIC_TOPIC.toString());
        imported.setAttribute(ATTRIBUTE_NAME_ID, id);
        return imported;
    }

    private void rewriteTopicId(final Element topic, final String id) {
        topic.setAttribute(ATTRIBUTE_NAME_ID, id);
    }

    private void relativizeLinks(final Element topic, final URI src, final URI dst) {
        for (Element link : getChildElements(topic, TOPIC_LINK, true)) {
            final URI href = URLUtils.toURI(link.getAttribute(ATTRIBUTE_NAME_HREF));
            final URI abs = src.resolve(href);
            final URI rel = getRelativePath(dst.resolve("."), abs);
            link.setAttribute(ATTRIBUTE_NAME_HREF, rel.toString());
        }
    }

    /**
     * Walk map and collect chunks.
     */
    private void walk(final URI mapFile, final Element elem, final List<ChunkOperation> chunks) {
        //   if @chunk = COMBINE
        if (elem.getAttribute(ATTRIBUTE_NAME_CHUNK).equals(COMBINE.name)) {
            //     create chunk
            final Attr hrefNode = elem.getAttributeNode(ATTRIBUTE_NAME_HREF);
            final URI href = hrefNode != null ? mapFile.resolve(hrefNode.getValue()) : null;
            final ChunkBuilder builder = new ChunkBuilder(COMBINE)
                    .src(href)
//                    .dst(href)
                    .topicref(elem);
            //     remove contents
            //                elem.removeChild(child);
            getChildElements(elem, MAP_TOPICREF).stream()
                    .flatMap(child -> collect(mapFile, child).stream())
                    .forEachOrdered(builder::addChild);
            // remove @chunk
            elem.removeAttribute(ATTRIBUTE_NAME_CHUNK);
            chunks.add(builder.build());
            return;
        } else {
            for (Element child : getChildElements(elem, MAP_TOPICREF)) {
                walk(mapFile, child, chunks);
            }
        }
    }

    private List<ChunkBuilder> collect(final URI mapFile, final Element elem) {
        final Attr hrefNode = elem.getAttributeNode(ATTRIBUTE_NAME_HREF);
        if (hrefNode != null && isDitaFormat(elem)) {
            final URI href = mapFile.resolve(hrefNode.getValue());
            final ChunkBuilder builder = new ChunkBuilder(COMBINE)
                    .src(href)
//                .dst(URI.create("#" + href.toString().replaceAll("/:#", "_")))
                    .topicref(elem);
            for (Element child : getChildElements(elem, MAP_TOPICREF)) {
                for (ChunkBuilder chunkBuilder : collect(mapFile, child)) {
                    builder.addChild(chunkBuilder);
                }
            }
            return Collections.singletonList(builder);
        } else {
            return getChildElements(elem, MAP_TOPICREF).stream()
                    .flatMap(child -> collect(mapFile, child).stream())
                    .collect(Collectors.toList());
        }
    }
}
