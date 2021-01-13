/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2021 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.chunk;

import org.dita.dost.chunk.ChunkOperation.ChunkBuilder;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.module.AbstractPipelineModuleImpl;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.util.Job.FileInfo;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.dita.dost.chunk.ChunkOperation.Operation.COMBINE;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.XMLUtils.getChildElements;

public class ChunkModule extends AbstractPipelineModuleImpl {
    @Override
    public AbstractPipelineOutput execute(final AbstractPipelineInput input) throws DITAOTException {
        try {
            // read modifiable map
            final FileInfo in = job.getFileInfo(fi -> fi.isInput).iterator().next();
            final URI mapFile = job.tempDirURI.resolve(in.uri);
            final Document mapDoc = job.getStore().getDocument(mapFile);
            final List<ChunkOperation> chunks = new ArrayList<>();
            // walk topicref | map
            walk(mapFile, mapDoc.getDocumentElement(), chunks);
            job.getStore().writeDocument(mapDoc, mapFile);
            // for each chunk
            for (ChunkOperation chunk : chunks) {
                //   recursively merge chunk topics
                final Document chunkDoc = job.getStore().getDocument(chunk.src);
                merge(chunk, chunkDoc.getDocumentElement());
                job.getStore().writeDocument(chunkDoc, chunk.src);
            }
            // TODO rewrite links to chunk content
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void merge(final ChunkOperation chunk, final Element root) throws IOException {
        for (ChunkOperation child : chunk.children) {
            final Document chunkDoc = job.getStore().getDocument(child.src);
            final Element topic = chunkDoc.getDocumentElement();
            final Element imported = (Element) root.getOwnerDocument().importNode(topic, true);
            final Element added = (Element) root.appendChild(imported);
            merge(child, added);
        }
    }

    private void walk(final URI mapFile, final Element elem, final List<ChunkOperation> chunks) {
        //   if @chunk = COMBINE
        if (elem.getAttribute(ATTRIBUTE_NAME_CHUNK).equals(COMBINE.name)) {
            //     create chunk
            final URI href = mapFile.resolve(elem.getAttribute(ATTRIBUTE_NAME_HREF));
            final ChunkBuilder builder = new ChunkBuilder(COMBINE).src(href).topicref(elem);
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
        final ChunkBuilder builder = new ChunkBuilder(COMBINE).src(href).topicref(elem);
        for (Element child : getChildElements(elem, MAP_TOPICREF)) {
            builder.addChild(collect(mapFile, child));
        }
        return builder;
    }
}
