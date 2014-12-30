/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */
package org.dita.dost.module;

import static org.dita.dost.util.Configuration.printTranstype;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.FilterUtils.*;
import static org.dita.dost.util.FileUtils.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.apache.tools.ant.util.FileUtils;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.reader.DitaValReader;
import org.dita.dost.reader.SubjectSchemeReader;
import org.dita.dost.util.FilterUtils;
import org.dita.dost.util.Job.FileInfo;
import org.dita.dost.writer.ProfilingFilter;

/**
 * Filter module class.
 */
final class FilterModule extends AbstractPipelineModuleImpl {

    @Override
    public AbstractPipelineOutput execute(final AbstractPipelineInput input) throws DITAOTException {
        if (logger == null) {
            throw new IllegalStateException("Logger not set");
        }
        final String transtype = input.getAttribute(ANT_INVOKER_EXT_PARAM_TRANSTYPE);
        final File ditavalFile = input.getAttribute(ANT_INVOKER_PARAM_DITAVAL) != null ? new File(input.getAttribute(ANT_INVOKER_PARAM_DITAVAL)) : null;

        final DitaValReader ditaValReader = new DitaValReader();
        ditaValReader.setLogger(logger);
        ditaValReader.initXMLReader(true);
        Map<FilterKey, Action> filterMap;
        if (ditavalFile != null) {
            ditaValReader.read(ditavalFile.getAbsoluteFile());
            filterMap = ditaValReader.getFilterMap();
        } else {
            filterMap = Collections.EMPTY_MAP;
        }
        final FilterUtils filterUtils = new FilterUtils(printTranstype.contains(transtype), filterMap);
        filterUtils.setLogger(logger);

        final ProfilingFilter writer = new ProfilingFilter();
        writer.setLogger(logger);
        writer.setJob(job);
        writer.setFilterUtils(filterUtils);

        final SubjectSchemeReader subjectSchemeReader = new SubjectSchemeReader();
        subjectSchemeReader.setLogger(logger);
        Map<URI, Set<URI>> dic;
        try {
            dic = SubjectSchemeReader.readMapFromXML(new File(job.tempDir, FILE_NAME_SUBJECT_DICTIONARY));
        } catch (final IOException e) {
            throw new DITAOTException(e);
        }

        for (final FileInfo f: job.getFileInfo()) {
            if (ATTR_FORMAT_VALUE_DITA.equals(f.format) || ATTR_FORMAT_VALUE_DITAMAP.equals(f.format)) {
                final File file = new File(job.tempDir, f.file.getPath());
                logger.info("Processing " + file.getAbsolutePath());

                subjectSchemeReader.reset();
                final Set<URI> schemaSet = dic.get(f.uri);
                if (schemaSet != null && !schemaSet.isEmpty()) {
                    logger.info("Loading subject schemes");
                    for (final URI schema : schemaSet) {
                        subjectSchemeReader.loadSubjectScheme(new File(job.tempDir.toURI().resolve(schema.getPath() + SUBJECT_SCHEME_EXTENSION)));
                    }
                }

                writer.setFilterUtils(filterUtils.refine(subjectSchemeReader.getSubjectSchemeMap()));

                try {
                    writer.write(file.getAbsoluteFile());
                    if (!writer.hasElementOutput()) {
                        logger.info("All content in " + file.getAbsolutePath() + " was filtered out");
                        job.remove(f);
                        FileUtils.delete(file);
                    }
                } catch (final Exception e) {
                    logger.error("Failed to profile " + file.getAbsolutePath() + ": " + e.getMessage());
                }
            }
        }

        try {
            job.write();
        } catch (final IOException e) {
            throw new DITAOTException(e);
        }
        
        return null;
    }

}
