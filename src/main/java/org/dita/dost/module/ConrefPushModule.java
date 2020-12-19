/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2010 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.

 */
package org.dita.dost.module;

import java.io.File;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;
import java.util.stream.Collectors;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.reader.ConrefPushReader;
import org.dita.dost.reader.ConrefPushReader.MoveKey;
import org.dita.dost.util.Job.FileInfo;
import org.dita.dost.writer.ConrefPushParser;
import org.w3c.dom.DocumentFragment;

/**
 * Conref push module.
 */
final class ConrefPushModule extends AbstractPipelineModuleImpl {

    @Override
    public AbstractPipelineOutput execute(final AbstractPipelineInput input) {
        final Collection<FileInfo> fis = job.getFileInfo(fileInfoFilter).stream()
                .filter(f -> f.isConrefPush)
                .collect(Collectors.toList());
        if (!fis.isEmpty()) {
            final ConrefPushReader reader = new ConrefPushReader();
            reader.setLogger(logger);
            reader.setJob(job);
            for (final FileInfo f: fis) {
                final File file = new File(job.tempDir, f.file.getPath());
                logger.info("Reading " + file.toURI());
                //FIXME: this reader calculate parent directory
                reader.read(file.getAbsoluteFile());
            }
            final Map<File, Hashtable<MoveKey, DocumentFragment>> pushSet = reader.getPushMap();
            for (final Map.Entry<File, Hashtable<MoveKey, DocumentFragment>> entry: pushSet.entrySet()) {
//                logger.info("Processing " + entry.getKey().toURI());
                final ConrefPushParser parser = new ConrefPushParser();
                parser.setJob(job);
                parser.setLogger(logger);
                parser.setMoveTable(entry.getValue());
                //pass the tempdir to ConrefPushParser
                parser.setTempDir(job.tempDir);
                //FIXME:This writer creates and renames files, have to
                try {
                    parser.read(entry.getKey());
                } catch (final DITAOTException e) {
                    logger.error("Failed to process push conref: " + e.getMessage(), e);
                }
            }
        }
        return null;
    }

}
