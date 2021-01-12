/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2021 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.chunk;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.module.AbstractPipelineModuleImpl;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.util.Job;
import org.dita.dost.util.Job.FileInfo;
import org.dita.dost.util.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.dita.dost.util.Constants.ATTRIBUTE_NAME_CHUNK;
import static org.dita.dost.util.Constants.MAP_TOPICREF;
import static org.dita.dost.util.XMLUtils.getChildElements;

public class ChunkModule extends AbstractPipelineModuleImpl {
    public enum Action {
        COMBINE("combine"),
        SPLIT("split");
        public final String name;

        Action(final String name) {
            this.name = name;
        }
    }

    @Override
    public AbstractPipelineOutput execute(final AbstractPipelineInput input) throws DITAOTException {
        try {
            // read modifiable map
            final FileInfo in = job.getFileInfo(fi -> fi.isInput).iterator().next();
            final URI mapFile = job.tempDirURI.resolve(in.uri);
            final Document mapDoc = job.getStore().getDocument(mapFile);
            final List<Element> chunks = new ArrayList<>();
            // walk topicref | map
            walk(mapDoc.getDocumentElement(), chunks);
            job.getStore().writeDocument(mapDoc, mapFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // for each chunk
        //   recursively merge chunk topics
        // TODO rewrite links to chunk content
        return null;
    }

    private void walk(Element elem, List<Element> chunks) {
        //   if @chunk = COMBINE
        if (elem.getAttribute(ATTRIBUTE_NAME_CHUNK).equals(Action.COMBINE.name)) {
            //     create chunk
            chunks.add(elem);
            //     remove contents
            for (Element child : getChildElements(elem, MAP_TOPICREF)) {
                System.out.println("remove " + child);
                elem.removeChild(child);
            }
            return;
        } else {
            for (Element child : getChildElements(elem, MAP_TOPICREF)) {
                walk(child, chunks);
            }
        }
    }
}
