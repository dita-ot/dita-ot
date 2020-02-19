/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2013 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.module;

import static org.dita.dost.util.Configuration.printTranstype;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.FilterUtils.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Optional;
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
        final File ditavalFile = Optional.of(new File(job.tempDir, FILE_NAME_MERGED_DITAVAL))
                .filter(File::exists)
                .orElse(null);

        final DitaValReader ditaValReader = new DitaValReader();
        ditaValReader.setLogger(logger);
        ditaValReader.setJob(job);
        final FilterUtils filterUtils;
        if (ditavalFile != null) {
            ditaValReader.read(ditavalFile.toURI());
            filterUtils = new FilterUtils(printTranstype.contains(transtype), ditaValReader.getFilterMap(),
                    ditaValReader.getForegroundConflictColor(), ditaValReader.getBackgroundConflictColor());
        } else {
            filterUtils = new FilterUtils(printTranstype.contains(transtype));
        }
        filterUtils.setLogger(logger);

        final ProfilingFilter writer = new ProfilingFilter();
        writer.setLogger(logger);
        writer.setJob(job);
        writer.setFilterUtils(filterUtils);

        final SubjectSchemeReader subjectSchemeReader = new SubjectSchemeReader();
        subjectSchemeReader.setLogger(logger);
        subjectSchemeReader.setJob(job);
        Map<URI, Set<URI>> dic;
        try {
            dic = subjectSchemeReader.readMapFromXML(new File(job.tempDir, FILE_NAME_SUBJECT_DICTIONARY));
        } catch (final IOException e) {
            throw new DITAOTException(e);
        }

        for (final FileInfo f: job.getFileInfo(fileInfoFilter)) {
            final File file = new File(job.tempDir, f.file.getPath());
            logger.info("Processing " + file.getAbsolutePath());

            subjectSchemeReader.reset();
            final Set<URI> schemaSet = dic.get(f.uri);
            if (schemaSet != null && !schemaSet.isEmpty()) {
                logger.info("Loading subject schemes");
                for (final URI schema : schemaSet) {
                    final File scheme = new File(job.tempDirURI.resolve(schema.getPath() + SUBJECT_SCHEME_EXTENSION));
                    if (scheme.exists()) {
                        subjectSchemeReader.loadSubjectScheme(scheme);
                    }
                }
            }

            writer.setFilterUtils(filterUtils.refine(subjectSchemeReader.getSubjectSchemeMap()));
            writer.setCurrentFile(file.toURI());

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

        try {
            job.write();
        } catch (final IOException e) {
            throw new DITAOTException(e);
        }

        return null;
    }

}
