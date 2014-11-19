/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2010 All Rights Reserved.
 */
package org.dita.dost.module;

import java.io.File;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.reader.ConrefPushReader;
import org.dita.dost.reader.ConrefPushReader.MoveKey;
import org.dita.dost.util.Job.FileInfo;
import org.dita.dost.util.Job.FileInfo.Filter;
import org.dita.dost.writer.ConrefPushParser;
import org.w3c.dom.DocumentFragment;

/**
 * Conref push module.
 */
final class ConrefPushModule extends AbstractPipelineModuleImpl {

    @Override
    public AbstractPipelineOutput execute(final AbstractPipelineInput input)
            throws DITAOTException {
        final Collection<FileInfo> fis = job.getFileInfo(new Filter() {
            @Override
            public boolean accept(FileInfo f) {
                return f.isConrefPush;
            }
        });
        if (!fis.isEmpty()) {
            final ConrefPushReader reader = new ConrefPushReader();
            reader.setLogger(logger);
            for(final FileInfo f: fis) {
                final File file = new File(job.tempDir, f.file.getPath());
                logger.info("Reading  " + file.getAbsolutePath());
                //FIXME: this reader calculate parent directory
                reader.read(file.getAbsoluteFile());
            }            
            final Map<File, Hashtable<MoveKey, DocumentFragment>> pushSet = reader.getPushMap();
            for (final Map.Entry<File, Hashtable<MoveKey, DocumentFragment>> entry: pushSet.entrySet()) {
                logger.info("Processing " + entry.getKey().getAbsolutePath());
                final ConrefPushParser parser = new ConrefPushParser();
                parser.setJob(job);
                parser.setLogger(logger);
                parser.setMoveTable(entry.getValue());
                //pass the tempdir to ConrefPushParser
                parser.setTempDir(job.tempDir.getAbsoluteFile());
                //FIXME:This writer creates and renames files, have to
                parser.write(entry.getKey());
            }
        }
        return null;
    }

}
