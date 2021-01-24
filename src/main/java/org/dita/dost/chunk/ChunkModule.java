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
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.sf.saxon.s9api.streams.Steps.attribute;
import static org.dita.dost.chunk.ChunkOperation.Operation.COMBINE;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.URLUtils.setFragment;
import static org.dita.dost.util.XMLUtils.getChildElements;
import static org.dita.dost.util.XMLUtils.rootElement;

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
            chunks = rewrite(rewriteMap, chunks);
            job.getStore().writeDocument(mapDoc, mapFile);
            // for each chunk
            for (ChunkOperation chunk : chunks) {
                //   recursively merge chunk topics
                final Document chunkDoc = job.getStore().getDocument(chunk.src);
                merge(chunk, chunk, chunkDoc);
                rewriteLinks(chunkDoc, chunk, rewriteMap);
                chunkDoc.normalizeDocument();
                job.getStore().writeDocument(chunkDoc, chunk.src);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
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
                final URI rel = chunk.src.resolve(".").relativize(rewrite);
                link.setAttribute(ATTRIBUTE_NAME_HREF, rel.toString());
            } else {
                final URI rel = chunk.src.resolve(".").relativize(abs);
                link.setAttribute(ATTRIBUTE_NAME_HREF, rel.toString());
            }
        }
    }

    /**
     * Rewrite chunks and collect topic moves.
     */
    private List<ChunkOperation> rewrite(final Map<URI, URI> rewriteMap,
                                         final List<ChunkOperation> chunks) {
        final List<ChunkOperation> list = new ArrayList<>();
        for (ChunkOperation chunk : chunks) {
            list.add(rewriteChunk(rewriteMap, chunk).build());
        }
        return list;
    }

    private ChunkBuilder rewriteChunk(final Map<URI, URI> rewriteMap, final ChunkOperation chunk) {
        final ChunkBuilder builder = new ChunkBuilder(chunk.operation)
                .topicref(chunk.topicref)
                .src(chunk.src);
        final URI dst = chunk.src;
        builder.dst(dst);
        rewriteMap.put(chunk.src, dst);
        for (ChunkOperation child : chunk.children) {
            builder.addChild(rewriteChunkChild(rewriteMap, chunk, child));
        }
        return builder;
    }

    private ChunkBuilder rewriteChunkChild(final Map<URI, URI> rewriteMap,
                                           final ChunkOperation rootChunk,
                                           final ChunkOperation chunk) {
        final ChunkBuilder builder = new ChunkBuilder(chunk.operation)
                .topicref(chunk.topicref)
                .src(chunk.src);
        // TODO rewrite dst
        final String id = getRootTopicId(chunk.src);
        final URI dst = setFragment(rootChunk.src, id);
        builder.dst(dst);
        rewriteMap.put(chunk.src, dst);
        for (ChunkOperation child : chunk.children) {
            builder.addChild(rewriteChunkChild(rewriteMap, rootChunk, child));
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
    private void merge(final ChunkOperation rootChunk,
                       final ChunkOperation chunk,
                       final Document doc) throws IOException {
        final Element ditaWrapper = doc.createElement(ELEMENT_NAME_DITA);
        ditaWrapper.setAttributeNS(DITA_NAMESPACE, ATTRIBUTE_PREFIX_DITAARCHVERSION + ":" + ATTRIBUTE_NAME_DITAARCHVERSION, "2.0");
        final Element dstTopic = (Element) doc.replaceChild(ditaWrapper, doc.getDocumentElement());
        ditaWrapper.appendChild(dstTopic);
        mergeTopic(rootChunk, chunk, dstTopic);
    }

    /**
     * Merge chunk tree fragment into a single topic.
     */
    private void mergeTopic(final ChunkOperation rootChunk,
                       final ChunkOperation chunk,
                       final Element dstTopic) throws IOException {
        for (ChunkOperation child : chunk.children) {
            final Document chunkDoc = job.getStore().getDocument(child.src);
            final Element topic = chunkDoc.getDocumentElement();
            final Element imported = (Element) dstTopic.getOwnerDocument().importNode(topic, true);
            relativizeLinks(imported, child.src, rootChunk.dst);
            final Element added = (Element) dstTopic.appendChild(imported);
            mergeTopic(rootChunk, child, added);
        }
    }

    private void relativizeLinks(final Element topic, final URI src, final URI dst) {
        for (Element link : getChildElements(topic, TOPIC_LINK, true)) {
            final URI href = URLUtils.toURI(link.getAttribute(ATTRIBUTE_NAME_HREF));
            final URI abs = src.resolve(href);
            final URI rel = dst.resolve(".").relativize(abs);
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
            final URI href = mapFile.resolve(elem.getAttribute(ATTRIBUTE_NAME_HREF));
            final ChunkBuilder builder = new ChunkBuilder(COMBINE)
                    .src(href)
//                    .dst(href)
                    .topicref(elem);
            //     remove contents
            for (Element child : getChildElements(elem, MAP_TOPICREF)) {
                builder.addChild(collect(mapFile, child));
                elem.removeChild(child);
            }
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

    private ChunkBuilder collect(final URI mapFile, final Element elem) {
        final URI href = mapFile.resolve(elem.getAttribute(ATTRIBUTE_NAME_HREF));
        final ChunkBuilder builder = new ChunkBuilder(COMBINE)
                .src(href)
//                .dst(URI.create("#" + href.toString().replaceAll("/:#", "_")))
                .topicref(elem);
        for (Element child : getChildElements(elem, MAP_TOPICREF)) {
            builder.addChild(collect(mapFile, child));
        }
        return builder;
    }
}
