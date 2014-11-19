/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */
package org.dita.dost.module;

import static org.dita.dost.util.Constants.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.util.Job.FileInfo;
import org.dita.dost.util.Job.FileInfo.Filter;
import org.dita.dost.util.XMLUtils;
import org.dita.dost.writer.TopicFragmentFilter;
import org.xml.sax.XMLFilter;

final class TopicFragmentModule extends AbstractPipelineModuleImpl {

    /**
     * Process topic files for same topic fragments identifiers.
     * 
     * @param input Input parameters and resources.
     * @return always returns {@code null}
     */
    @Override
    public AbstractPipelineOutput execute(final AbstractPipelineInput input)
            throws DITAOTException {
        final Collection<FileInfo> fis = job.getFileInfo(new Filter() {
            @Override
            public boolean accept(final FileInfo f) {
                return ATTRIBUTE_FORMAT_VALUE_DITA.equals(f.format);
            }
        });
        for (final FileInfo f: fis) {
            final File file = new File(job.tempDir, f.file.getPath());
            logger.info("Processing " + file.getAbsolutePath());
            
            final List<XMLFilter> filters = new ArrayList<XMLFilter>();
            
            final TopicFragmentFilter filter = new TopicFragmentFilter();
            filters.add(filter);
                            
            XMLUtils.transform(file, filters);
        }
        return null;
    }

}
