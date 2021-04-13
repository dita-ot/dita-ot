package org.dita.dost.module.filter;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.util.FilterUtils;
import org.dita.dost.util.Job;
import org.dita.dost.util.URLUtils;
import org.dita.dost.writer.ProfilingFilter;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

import java.net.URI;
import java.util.*;

import static java.util.Collections.EMPTY_LIST;
import static java.util.Collections.singletonList;
import static org.dita.dost.module.filter.TopicBranchFilterModule.SKIP_FILTER;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.FilterUtils.newFilterUtils;
import static org.dita.dost.util.URLUtils.toURI;
import static org.dita.dost.util.XMLUtils.getChildElement;
import static org.dita.dost.util.XMLUtils.getChildElements;

public class TopicBranchFilter {

    /**
     * contains already filtered files
     */
    private final Set<URI> filtered = new HashSet<>();

    private final Map<URI, FilterUtils> filterCache = new HashMap<>();
    private DITAOTLogger logger;
    private URI map;
    private Job job = Job.instance;
    private SubjectScheme subjectSchemeMap;

    private Deque<TopicBranch> elements = new ArrayDeque<>();

    private static class TopicBranch {
        Element element;
        List<FilterUtils> filter;

        public TopicBranch(Element element, List<FilterUtils> filter) {
            this.element = element;
            this.filter = filter;
        }
    }

    public TopicBranchFilter(DITAOTLogger logger, URI map) {
        this.logger = logger;
        this.map = map;
    }

    /** Modify and filter topics for branches. These files use an existing file name. */
    public void filterTopics(Element document, SubjectScheme subjectSchemeMap) {
        this.subjectSchemeMap = subjectSchemeMap;
        job = Job.instance;

        filter(document, EMPTY_LIST);
    }

    public void filter(Element root, List<FilterUtils> filter) {
        elements.push(new TopicBranch(root, filter));

        while (!elements.isEmpty()) {
            TopicBranch current = elements.pop();
            Element element = current.element;

            List<FilterUtils> filterUtils = combineFilterUtils(element, current.filter, subjectSchemeMap);
            URI srcAbsUri = job.tempDirURI.resolve(map.resolve(element.getAttribute(ATTRIBUTE_NAME_HREF)));
            if (shouldFilter(element, filterUtils, srcAbsUri)) {
                ProfilingFilter writer = newProfilingFilter(filterUtils, srcAbsUri);
                transformDocument(srcAbsUri, writer);
                filtered.add(srcAbsUri);
            }

            if (element.getAttributeNode(SKIP_FILTER) != null) {
                element.removeAttributeNode(element.getAttributeNode(SKIP_FILTER));
            }

            for (final Element child: getChildElements(element, MAP_TOPICREF)) {
                if (DITAVAREF_D_DITAVALREF.matches(child)) {
                    continue;
                }
                elements.push(new TopicBranch(child, filterUtils));
            }
        }
    }

    private boolean shouldFilter(Element topicref, List<FilterUtils> filterUtils, URI srcAbsUri) {
        final String href = topicref.getAttribute(ATTRIBUTE_NAME_HREF);
        final Attr skipFilter = topicref.getAttributeNode(SKIP_FILTER);

        return !filterUtils.isEmpty() && skipFilter == null
                && !filtered.contains(srcAbsUri)
                && !href.isEmpty()
                && !ATTR_SCOPE_VALUE_EXTERNAL.equals(topicref.getAttribute(ATTRIBUTE_NAME_SCOPE));
    }

    private void transformDocument(URI srcAbsUri, ProfilingFilter writer) {
        try {
            logger.debug("Filtering " + srcAbsUri);
            job.getStore().transform(URLUtils.stripFragment(srcAbsUri), singletonList(writer));
        } catch (final DITAOTException e) {
            logger.error("Failed to filter " + srcAbsUri + ": " + e.getMessage(), e);
        }
    }

    private ProfilingFilter newProfilingFilter(List<FilterUtils> filterUtils, URI srcAbsUri) {
        ProfilingFilter writer = new ProfilingFilter();
        writer.setLogger(logger);
        writer.setJob(Job.instance);
        writer.setFilterUtils(filterUtils);
        writer.setCurrentFile(srcAbsUri);
        return writer;
    }

    /**
     * Combine referenced DITAVAL to existing list and refine with subject scheme.
     */
    List<FilterUtils> combineFilterUtils(Element topicref, List<FilterUtils> filters, SubjectScheme subjectSchemeMap) {
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
        final URI tmp = Job.instance.tempDirURI.resolve(map).resolve(href);
        final Job.FileInfo fi = job.getFileInfo(tmp);
        final URI ditaval = fi.src;
        return filterCache.computeIfAbsent(ditaval, key -> newFilterUtils(key, logger, job));
    }

}
