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
import org.dita.dost.reader.DitaValReader;
import org.dita.dost.reader.SubjectSchemeReader;
import org.dita.dost.util.FilterUtils;
import org.dita.dost.util.Job.FileInfo;
import org.w3c.dom.Element;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    final SubjectSchemeReader subjectSchemeReader;
    private final Map<URI, FilterUtils> filterCache = new HashMap<>();
    /** Absolute URI to map being processed. */
    URI currentFile;

    AbstractBranchFilterModule() {
        ditaValReader = new DitaValReader();
        subjectSchemeReader = new SubjectSchemeReader();
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
                .filter(e -> SUBJECTSCHEME_ENUMERATIONDEF.matches(e))
                .forEach(enumerationDef -> {
                    final Element schemeRoot = ancestors(enumerationDef)
                            .filter(e -> SUBMAP.matches(e))
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

    /**
     * Read DITAVAL file.
     */
    FilterUtils getFilterUtils(final URI ditaval) {
        logger.info("Reading " + ditaval);
        ditaValReader.filterReset();
        ditaValReader.read(ditaval);
        Map<FilterUtils.FilterKey, FilterUtils.Action> filterMap = ditaValReader.getFilterMap();
        final FilterUtils f = new FilterUtils(filterMap, ditaValReader.getForegroundConflictColor(), ditaValReader.getBackgroundConflictColor());
        f.setLogger(logger);
        return f;
    }

}
