/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2017 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.module.filter;

import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.module.AbstractPipelineModuleImpl;
import org.dita.dost.module.reader.TempFileNameScheme;
import org.dita.dost.reader.DitaValReader;
import org.dita.dost.reader.SubjectSchemeReader;
import org.dita.dost.util.*;
import org.dita.dost.util.Job.FileInfo;
import org.w3c.dom.Element;

import java.io.*;
import java.net.URI;
import java.util.*;

import static org.dita.dost.util.Configuration.configuration;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.URLUtils.toURI;
import static org.dita.dost.util.XMLUtils.*;

/**
 * Abstract branch filter module.
 *
 * @since 3.0
 */
public abstract class AbstractBranchFilterModule extends AbstractPipelineModuleImpl {

    private final DitaValReader ditaValReader;
    TempFileNameScheme tempFileNameScheme;
    final SubjectSchemeReader subjectSchemeReader;
    private final Map<URI, FilterUtils> filterCache = new HashMap<>();
    /** Absolute URI to map being processed. */
    URI currentFile;

    AbstractBranchFilterModule() {
        ditaValReader = new DitaValReader();
        subjectSchemeReader = new SubjectSchemeReader();
    }

    @Override
    public void setJob(final Job job) {
        super.setJob(job);
        try {
            final String cls = Optional
                    .ofNullable(job.getProperty("temp-file-name-scheme"))
                    .orElse(configuration.get("temp-file-name-scheme"));
            tempFileNameScheme = (TempFileNameScheme) Class.forName(cls).newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        tempFileNameScheme.setBaseDir(job.getInputDir());
        ditaValReader.setJob(job);
    }

    @Override
    public void setLogger(final DITAOTLogger logger) {
        super.setLogger(logger);
        ditaValReader.setLogger(logger);
        subjectSchemeReader.setLogger(logger);
    }

    /**
     * Read subject scheme definitions.
     */
    SubjectScheme getSubjectScheme(final Element root) {
        subjectSchemeReader.reset();
        logger.debug("Loading subject schemes");
        final List<Element> subjectSchemes = toList(root.getElementsByTagName("*"));
        subjectSchemes.stream()
                .filter(SUBJECTSCHEME_ENUMERATIONDEF::matches)
                .forEach(enumerationDef -> {
                    final Element schemeRoot = ancestors(enumerationDef)
                            .filter(SUBMAP::matches)
                            .findFirst()
                            .orElse(root);
                    subjectSchemeReader.processEnumerationDef(schemeRoot, enumerationDef);
                });
        return subjectSchemeReader.getSubjectSchemeMap();
    }

    /**
     * Combine referenced DITAVAL to existing list and refine with subject scheme.
     */
    List<FilterUtils> combineFilterUtils(final Element topicref, final List<FilterUtils> filters,
                                         final SubjectScheme subjectSchemeMap) {
        return getChildElement(topicref, DITAVAREF_D_DITAVALREF)
                .map(ditavalRef -> getFilterUtils(ditavalRef).refine(subjectSchemeMap))
                .map(f -> {
                    final List<FilterUtils> fs = new ArrayList<>(filters.size() + 1);
                    fs.addAll(filters);
                    fs.add(f);
                    return fs;
                })
                .orElse(filters);
    }

    /**
     * Read referenced DITAVAL and cache filter.
     **/
    private FilterUtils getFilterUtils(final Element ditavalRef) {
        final URI href = toURI(ditavalRef.getAttribute(ATTRIBUTE_NAME_HREF));
        final URI tmp = currentFile.resolve(href);
        final FileInfo fi = job.getFileInfo(tmp);
        final URI ditaval = fi.src;
        return filterCache.computeIfAbsent(ditaval, this::getFilterUtils);
    }

    final Set<URI> flagImageSet = new LinkedHashSet<>(128);
    final Set<URI> relFlagImagesSet = new LinkedHashSet<>(128);

    /**
     * Read DITAVAL file.
     */
    FilterUtils getFilterUtils(final URI ditaval) {
        logger.info("Reading " + ditaval);
        ditaValReader.filterReset();
        ditaValReader.read(ditaval);
        flagImageSet.addAll(ditaValReader.getImageList());
        relFlagImagesSet.addAll(ditaValReader.getRelFlagImageList());
        Map<FilterUtils.FilterKey, FilterUtils.Action> filterMap = ditaValReader.getFilterMap();
        final FilterUtils f = new FilterUtils(filterMap, ditaValReader.getForegroundConflictColor(), ditaValReader.getBackgroundConflictColor());
        f.setLogger(logger);
        return f;
    }

    void addFlagImagesSetToProperties(final Job prop, final Set<URI> set) {
        for (final URI file: flagImageSet) {
            final FileInfo f = getOrCreateFileInfo(file);
            f.isFlagImage = true;
            f.format = ATTR_FORMAT_VALUE_IMAGE;
            job.add(f);
        }

        final Set<URI> newSet = new LinkedHashSet<>(128);
        for (final URI file: set) {
            if (file.isAbsolute()) {
                // no need to append relative path before absolute paths
                newSet.add(file.normalize());
            } else {
                // In ant, all the file separator should be slash, so we need to
                // replace all the back slash with slash.
                newSet.add(file.normalize());
            }
        }

        // write list attribute to file
        final String fileKey = REL_FLAGIMAGE_LIST.substring(0, REL_FLAGIMAGE_LIST.lastIndexOf("list")) + "file";
        prop.setProperty(fileKey, REL_FLAGIMAGE_LIST.substring(0, REL_FLAGIMAGE_LIST.lastIndexOf("list")) + ".list");
        final File list = new File(job.tempDir, prop.getProperty(fileKey));
        try (Writer bufferedWriter = new BufferedWriter(new OutputStreamWriter(job.getStore().getOutputStream(list.toURI())))) {
            for (URI aNewSet : newSet) {
                bufferedWriter.write(aNewSet.getPath());
                bufferedWriter.write('\n');
            }
            bufferedWriter.flush();
        } catch (final IOException e) {
            logger.error(e.getMessage(), e) ;
        }

        prop.setProperty(REL_FLAGIMAGE_LIST, StringUtils.join(newSet, COMMA));
    }

    private FileInfo getOrCreateFileInfo(final URI file) {
        assert file.isAbsolute();
        assert file.getFragment() == null;
        final URI f = file.normalize();
        return Optional.ofNullable(job.getFileInfo(f))
                .map(FileInfo.Builder::new)
                .orElse(new FileInfo.Builder().src(file))
                .uri(tempFileNameScheme.generateTempFileName(file))
                .build();
    }

}
