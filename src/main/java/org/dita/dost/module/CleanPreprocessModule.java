/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2016 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.module;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.io.FileUtils;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.util.Job.FileInfo;
import org.dita.dost.util.URLUtils;
import org.dita.dost.writer.LinkFilter;
import org.dita.dost.writer.MapCleanFilter;
import org.xml.sax.XMLFilter;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.XMLUtils.addOrSetAttribute;
import org.dita.dost.util.XMLUtils;

/**
 * Move temporary files not based on output URI to match output URI structure.
 *
 * @since 2.5
 */
public class CleanPreprocessModule extends AbstractPipelineModuleImpl {

    private static final String PARAM_USE_RESULT_FILENAME = "use-result-filename";

    private final LinkFilter filter = new LinkFilter();
    private final MapCleanFilter mapFilter = new MapCleanFilter();
    private final XMLUtils xmlUtils = new XMLUtils();

    @Override
    public void setLogger(final DITAOTLogger logger) {
        super.setLogger(logger);
        xmlUtils.setLogger(logger);
    }

    @Override
    public AbstractPipelineOutput execute(final AbstractPipelineInput input) throws DITAOTException {
        final boolean useResultFilename = Optional.ofNullable(input.getAttribute(PARAM_USE_RESULT_FILENAME))
                .map(Boolean::parseBoolean)
                .orElse(true);

        final URI base = getBaseDir();
        final String uplevels = getUplevels(base);
        job.setProperty("uplevels", uplevels);
        job.setInputDir(base);

        if (useResultFilename) {
            init();
            final Collection<FileInfo> fis = new ArrayList<>(job.getFileInfo());
            final Collection<FileInfo> res = new ArrayList<>(fis.size());
            for (final FileInfo fi : fis) {
                try {
                    final FileInfo.Builder builder = new FileInfo.Builder(fi);
                    final URI rel = base.relativize(fi.result);
                    builder.uri(rel);
                    if (fi.format != null && (fi.format.equals("coderef") || fi.format.equals("image"))) {
                        logger.debug("Skip format " + fi.format);
                    } else {
                        final File srcFile = new File(job.tempDirURI.resolve(fi.uri));
                        if (srcFile.exists()) {
                            final File destFile = new File(job.tempDirURI.resolve(rel));
                            final List<XMLFilter> processingPipe = getProcessingPipe(fi, srcFile, destFile);
                            if (!processingPipe.isEmpty()) {
                                logger.info("Processing " + srcFile.toURI() + " to " + destFile.toURI());
                                xmlUtils.transform(srcFile.toURI(), destFile.toURI(), processingPipe);
                                if (!srcFile.equals(destFile)) {
                                    logger.debug("Deleting " + srcFile.toURI());
                                    FileUtils.deleteQuietly(srcFile);
                                }
                            } else if (!srcFile.equals(destFile)) {
                                logger.info("Moving " + srcFile.toURI() + " to " + destFile.toURI());
                                FileUtils.moveFile(srcFile, destFile);
                            }
                        }
                    }
                    res.add(builder.build());
                } catch (final IOException e) {
                    logger.error("Failed to clean " + job.tempDirURI.resolve(fi.uri) + ": " + e.getMessage(), e);
                }
            }

            fis.forEach(fi -> job.remove(fi));
            res.forEach(fi -> job.add(fi));
        }

        // start map
        final FileInfo start = job.getFileInfo(job.getInputFile());
        if (start != null) {
            job.setInputMap(start.uri);
        }

        try {
            job.write();
        } catch (IOException e) {
            throw new DITAOTException();
        }

        return null;
    }

    String getUplevels(final URI base) {
        final URI rel = base.relativize(job.getInputFile());
        final int count = rel.toString().split("/").length - 1;
        return IntStream.range(0, count).boxed()
                .map(i -> "../")
                .collect(Collectors.joining(""));

    }

    /** Get common base directory for all files */
    @VisibleForTesting
    URI getBaseDir() {
        URI baseDir = job.getInputDir();

        final Collection<FileInfo> fis = job.getFileInfo();
        for (final FileInfo fi : fis) {
            final URI res = fi.result.resolve(".");
            baseDir = getCommonBase(baseDir, res);
        }

        return baseDir;
    }

    @VisibleForTesting
    URI getCommonBase(final URI left, final URI right) {
        assert left.isAbsolute();
        assert right.isAbsolute();
        if (!left.getScheme().equals(right.getScheme())) {
            throw new IllegalArgumentException("Argument schemes do not match");
        }
        final URI l = left.resolve(".");
        final URI r = right.resolve(".");
        final String lp = l.getPath();
        final String rp = r.getPath();
        if (lp.equals(rp)) {
            return l;
        }
        if (lp.startsWith(rp)) {
            return r;
        }
        if (rp.startsWith(lp)) {
            return l;
        }
        final String[] la = left.getPath().split("/");
        final String[] ra = right.getPath().split("/");
        int i = 0;
        final int len = Math.min(la.length, ra.length);
        for (; i < len; i++) {
            if (la[i].equals(ra[i])) {
                //
            } else {
                final int common = Math.max(0, i);
                final String path = Arrays.asList(la)
                        .subList(0, common)
                        .stream()
                        .collect(Collectors.joining("/")) + "/";
                return URLUtils.setPath(left, path);
            }
        }
        return null;
    }

    private void init() {
        filter.setJob(job);
        filter.setLogger(logger);

        mapFilter.setJob(job);
        mapFilter.setLogger(logger);
    }

    private List<XMLFilter> getProcessingPipe(final FileInfo fi, final File srcFile, final File destFile) {
        final List<XMLFilter> res = new ArrayList<>();

        if (fi.format == null || fi.format.equals(ATTR_FORMAT_VALUE_DITA) || fi.format.equals(ATTR_FORMAT_VALUE_DITAMAP)) {
            filter.setCurrentFile(srcFile.toURI());
            filter.setDestFile(destFile.toURI());
            res.add(filter);
        }

        if (fi.format != null && fi.format.equals(ATTR_FORMAT_VALUE_DITAMAP)) {
            res.add(mapFilter);
        }

        return res;
    }

}
