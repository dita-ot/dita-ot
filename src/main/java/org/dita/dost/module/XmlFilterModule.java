/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2015 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.module;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.util.Job.FileInfo;
import org.dita.dost.writer.AbstractXMLFilter;
import org.xml.sax.XMLFilter;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Map processes topics through XML filters. Filters are reused and should reset internal state on
 * {@code startDocument} event.
 */
public final class XmlFilterModule extends AbstractPipelineModuleImpl {

    /**
     * Filter files through XML filters.
     *
     * @param input Input parameters and resources.
     * @return always returns {@code null}
     */
    @Override
    public AbstractPipelineOutput execute(final AbstractPipelineInput input)
            throws DITAOTException {
        final Collection<FileInfo> fis = job.getFileInfo(fileInfoFilter);
        if (parallel) {
            fis.stream().parallel().forEach(f -> {
                final URI file = job.tempDirURI.resolve(f.uri);
                logger.info("Processing " + file);
                try {
                    job.getStore().transform(file, getProcessingPipe(f));
                } catch (final DITAOTException e) {
                    logger.error("Failed to process XML filter: " + e.getMessage(), e);
                }
            });
        } else {
            for (final FileInfo f : fis) {
                final URI file = job.tempDirURI.resolve(f.uri);
                logger.info("Processing " + file);
                try {
                    job.getStore().transform(file, getProcessingPipe(f));
                } catch (final DITAOTException e) {
                    logger.error("Failed to process XML filter: " + e.getMessage(), e);
                }
            }
        }
        return null;
    }

    /**
     * Get pipe line filters
     *
     * @param fi current file being processed
     */
    private List<XMLFilter> getProcessingPipe(final FileInfo fi) {
        final URI fileToParse = job.tempDirURI.resolve(fi.uri);
        assert fileToParse.isAbsolute();
        return filters.stream()
                .filter(p -> p.predicate.test(fi))
                .map(FilterPair::newInstance)
                .map(f -> {
                    logger.debug("Configure filter " + f.getClass().getCanonicalName());
                    f.setCurrentFile(fileToParse);
                    f.setJob(job);
                    f.setLogger(logger);
                    return f;
                })
                .collect(Collectors.toList());
    }

    /**
     * SAX filter with file predicate.
     */
    public static class FilterPair {
        public final Class<? extends AbstractXMLFilter> filterClass;
        public final Predicate<FileInfo> predicate;
        public final Map<String, String> params;

        public FilterPair(final Class<? extends AbstractXMLFilter> filterClass,
                          final Predicate<FileInfo> fileInfoFilter,
                          final Map<String, String> params) {
            this.filterClass = filterClass;
            this.predicate = fileInfoFilter;
            this.params = params;
        }

        public AbstractXMLFilter newInstance() {
            try {
                final AbstractXMLFilter f = filterClass.getDeclaredConstructor().newInstance();
                params.forEach(f::setParam);
                return f;
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
