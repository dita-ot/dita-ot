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
import org.dita.dost.util.Job.FileInfo;
import org.dita.dost.util.Job.FileInfo.Filter;
import org.dita.dost.writer.ConrefPushParser;

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
                logger.logInfo("Reading  " + file.getAbsolutePath());
                //FIXME: this reader calculate parent directory
                reader.read(file.getAbsoluteFile());
            }            
            final Map<String, Hashtable<String, String>> pushSet = reader.getPushMap();
            for (final Map.Entry<String, Hashtable<String,String>> entry: pushSet.entrySet()) {
                logger.logInfo("Processing " + new File(entry.getKey()).getAbsolutePath());
                final ConrefPushParser parser = new ConrefPushParser();
                parser.setJob(job);
                parser.setLogger(logger);
                parser.setMoveTable(entry.getValue());
                //pass the tempdir to ConrefPushParser
                parser.setTempDir(job.tempDir.getAbsoluteFile());
                //FIXME:This writer creates and renames files, have to
                parser.write(new File(entry.getKey()));
            }
        }
        return null;
    }

}
