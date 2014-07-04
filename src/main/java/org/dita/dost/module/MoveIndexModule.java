/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.module;

import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.URLUtils.*;

import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.reader.MapIndexReader;
import org.dita.dost.util.Job.FileInfo;
import org.dita.dost.util.Job.FileInfo.Filter;
import org.dita.dost.writer.DitaIndexWriter;

/**
 * MoveIndexModule implement the move index step in preprocess. It reads the index
 * information from ditamap file and move these information to different
 * corresponding dita topic file.
 * 
 * @author Zhang, Yuan Peng
 */
final class MoveIndexModule extends AbstractPipelineModuleImpl {

    /**
     * Entry point of MoveIndexModule.
     * 
     * @param input Input parameters and resources.
     * @return null
     * @throws DITAOTException exception
     */
    @Override
    public AbstractPipelineOutput execute(final AbstractPipelineInput input) throws DITAOTException {
        final Collection<FileInfo> fis = job.getFileInfo(new Filter() {
            @Override
            public boolean accept(FileInfo f) {
                return ATTR_FORMAT_VALUE_DITAMAP.equals(f.format);
            }
        });
        if (!fis.isEmpty()) {
            final MapIndexReader indexReader = new MapIndexReader();
            indexReader.setLogger(logger);
            indexReader.setMatch(Arrays.asList(MAP_TOPICREF, MAP_TOPICMETA, TOPIC_KEYWORDS));
    
            for(final FileInfo f: fis){
                //FIXME: this reader needs parent directory for further process
                indexReader.read(new File(job.tempDir, f.file.getPath()).getAbsoluteFile());
            }
    
            final Map<URI, String> mapSet = indexReader.getMapping();
            
            if (!mapSet.isEmpty()) {
                final DitaIndexWriter indexInserter = new DitaIndexWriter();
                indexInserter.setLogger(logger);
                for (final Map.Entry<URI, String> entry: mapSet.entrySet()) {
                    final String targetFileName = entry.getKey().getPath();
                    if (targetFileName.endsWith(FILE_EXTENSION_DITA) || targetFileName.endsWith(FILE_EXTENSION_XML)){
                        indexInserter.setIndexEntries(entry.getValue());
                        if (toFile(entry.getKey()).exists()) {
                            logger.info("Processing " + targetFileName);
                            indexInserter.write(new File(entry.getKey()));
                        } else {
                            logger.error("File " + entry.getKey() + " does not exist");
                        }
        
                    }
                }
            }
        }
        return null;
    }

}
