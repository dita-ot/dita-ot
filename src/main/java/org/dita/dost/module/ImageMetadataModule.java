/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2010 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.module;

import static org.dita.dost.util.Constants.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.util.Job.FileInfo;
import org.dita.dost.util.Pool;
import org.dita.dost.writer.ImageMetadataFilter;
import org.xml.sax.Attributes;

/**
 * Image metadata module.
 *
 */
final class ImageMetadataModule extends AbstractPipelineModuleImpl {

    /**
     * Constructor.
     */
    public ImageMetadataModule() {
        super();
    }

    /**
     * Entry point of image metadata ModuleElem.
     * @param input Input parameters and resources.
     * @return null
     * @throws DITAOTException exception
     */
    @Override
    public AbstractPipelineOutput execute(final AbstractPipelineInput input)
            throws DITAOTException {
        if (logger == null) {
            throw new IllegalStateException("Logger not set");
        }
        final Collection<FileInfo> images = job.getFileInfo(f -> ATTR_FORMAT_VALUE_IMAGE.equals(f.format) || ATTR_FORMAT_VALUE_HTML.equals(f.format));
        if (!images.isEmpty()) {
            final File outputDir = new File(input.getAttribute(ANT_INVOKER_EXT_PARAM_OUTPUTDIR));
            final Predicate<FileInfo> filter = fileInfoFilter != null
                    ? fileInfoFilter
                    : f -> !f.isResourceOnly && ATTR_FORMAT_VALUE_DITA.equals(f.format);
            final Map<URI, Attributes> cache = new ConcurrentHashMap<>();

            if (parallel) {
                final Pool<ImageMetadataFilter> pool = new Pool<>(() -> {
                    final ImageMetadataFilter writer = new ImageMetadataFilter(outputDir, job, cache);
                    writer.setLogger(logger);
                    writer.setJob(job);
                    return writer;
                });
                job.getFileInfo(filter).stream()
                        .parallel()
                        .map(f -> new File(job.tempDir, f.file.getPath()).getAbsoluteFile())
                        .forEach(filename -> {
                            final ImageMetadataFilter writer = pool.borrowObject();
                            try {
                                writer.write(filename);
                            } finally {
                                pool.returnObject(writer);
                            }
                        });
            } else {
                final ImageMetadataFilter writer = new ImageMetadataFilter(outputDir, job, cache);
                writer.setLogger(logger);
                writer.setJob(job);
                for (final FileInfo f : job.getFileInfo(filter)) {
                    writer.write(new File(job.tempDir, f.file.getPath()).getAbsoluteFile());
                }
            }

            storeImageFormat(cache.keySet(), outputDir);

            try {
                job.write();
            } catch (IOException e) {
                throw new DITAOTException("Failed to serialize job configuration: " + e.getMessage(), e);
            }
        }

        return null;
    }

    private void storeImageFormat(final Collection<URI> images, final File outputDir) {
        final URI output = outputDir.toURI();
        final URI temp = job.tempDirURI;
        for (final URI f : images) {
            assert f.isAbsolute();
            URI rel = output.relativize(f);
            if (rel.isAbsolute()) {
                rel = temp.relativize(f);
            }
            final FileInfo fi = job.getFileInfo(rel);
            if (fi != null) {
                logger.debug("Set " + fi.uri + " format to " + ATTR_FORMAT_VALUE_IMAGE);
                job.add(new FileInfo.Builder(fi).format(ATTR_FORMAT_VALUE_IMAGE).build());
            }
        }
    }

}
