package org.dita.dost.module.filter;

import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.reader.DitaValReader;
import org.dita.dost.reader.SubjectSchemeReader;
import org.dita.dost.util.FilterUtils;
import org.dita.dost.util.Job;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import java.io.File;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.StringUtils.getExtProps;
import static org.dita.dost.util.StringUtils.getExtPropsFromSpecializations;
import static org.dita.dost.util.URLUtils.toURI;
import static org.dita.dost.util.XMLUtils.*;

public class MapBranchFilter {

    private final DITAOTLogger logger;
    private final URI currentFile;
    private final Job job = Job.instance;
    private final Map<URI, FilterUtils> filterCache = new HashMap<>();
    private SubjectScheme subjectSchemes;
    private QName[][] domains;

    private Deque<MapBranch> elements = new ArrayDeque<>();

    private static class MapBranch {
        Element element;
        List<FilterUtils> filter;
        public MapBranch(Element element, List<FilterUtils> filter) {
            this.element = element;
            this.filter = filter;
        }
    }

    public MapBranchFilter(DITAOTLogger logger, URI currentFile) {
        this.logger = logger;
        this.currentFile = currentFile;
    }

    /** Filter map and remove excluded content. */
    public void filterBranches(final Element root) {
        subjectSchemes = getSubjectScheme(root);
        domains = getExtProps(root.getAttribute(ATTRIBUTE_NAME_DOMAINS));
        List<FilterUtils> baseFilter = getBaseFilter(subjectSchemes);
        filterBranches(root, baseFilter);
    }

    private List<FilterUtils> getBaseFilter(final SubjectScheme subjectSchemeMap) {
        URI ditavalFile = Optional.of(new File(job.tempDir, FILE_NAME_MERGED_DITAVAL)).filter(File::exists).map(File::toURI).orElse(null);
        if (ditavalFile != null) {
            final FilterUtils f = getFilterUtils(ditavalFile).refine(subjectSchemeMap);
            return singletonList(f);
        }
        return Collections.emptyList();
    }

    FilterUtils getFilterUtils(final URI ditaval) {
        logger.info("Reading " + ditaval);
        DitaValReader ditaValReader = new DitaValReader();
        ditaValReader.setJob(job);
        ditaValReader.read(ditaval);
        Map<FilterUtils.FilterKey, FilterUtils.Action> filterMap = ditaValReader.getFilterMap();
        FilterUtils f = new FilterUtils(filterMap, ditaValReader.getForegroundConflictColor(), ditaValReader.getBackgroundConflictColor());
        f.setLogger(logger);
        return f;
    }

    /**
     * Read subject scheme definitions.
     */
    SubjectScheme getSubjectScheme(final Element root) {
        logger.debug("Loading subject schemes");

        SubjectSchemeReader subjectSchemeReader = new SubjectSchemeReader();
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

    private void filterBranches(Element documentRoot, List<FilterUtils> baseFilter) {
        elements.push(new MapBranch(documentRoot, baseFilter));

        while (!elements.isEmpty()) {
            MapBranch mapBranch = elements.pop();
            Element element = mapBranch.element;

            List<FilterUtils> filterUtils = combineFilterUtils(element, mapBranch.filter);

            if (exclude(element, domains, filterUtils)) {
                element.getParentNode().removeChild(element);
                continue;
            }

            getChildElements(element).forEach(child ->  elements.push(new MapBranch(child, new ArrayList<>(filterUtils))));

            Set<FilterUtils.Flag> flags = collectFlags(element, filterUtils);
            flags.forEach(flag -> addStartEndFlagElement(element, flag));
        }
    }

    List<FilterUtils> combineFilterUtils(Element element, List<FilterUtils> filters) {
        return getChildElement(element, DITAVAREF_D_DITAVALREF)
                .map(ditavalRef -> getFilterUtils(ditavalRef).refine(subjectSchemes))
                .map(f -> {
                    final List<FilterUtils> fs = new ArrayList<>(filters.size() + 1);
                    fs.addAll(filters);
                    fs.add(f);
                    return fs;
                })
                .orElse(filters);
    }

    private FilterUtils getFilterUtils(final Element ditavalRef) {
        final URI href = toURI(ditavalRef.getAttribute(ATTRIBUTE_NAME_HREF));
        final URI tmp = currentFile.resolve(href);
        final Job.FileInfo fi = job.getFileInfo(tmp);
        final URI ditaval = fi.src;
        return filterCache.computeIfAbsent(ditaval, this::getFilterUtils);
    }

    private boolean exclude(Element element, QName[][] props, List<FilterUtils> filterUtils) {
        for (FilterUtils filterUtil : filterUtils) {
            if (filterUtil.needsExclusion(element, props)) {
                return true;
            }
        }

        return false;
    }

    private Set<FilterUtils.Flag> collectFlags(Element element, List<FilterUtils> filterUtils) {
        return filterUtils.stream()
                .flatMap(f -> f.getFlags(element, domains).stream())
                .map(f -> f.adjustPath(currentFile, job))
                .collect(Collectors.toSet());
    }

    private void addStartEndFlagElement(Element element, FilterUtils.Flag flag) {
        addStartFlag(element, flag);
        addEndFlag(element, flag);
    }

    private void addStartFlag(Element element, FilterUtils.Flag flag) {
        Element startElement = (Element) element.getOwnerDocument().importNode(flag.getStartFlag(), true);
        Node firstChild = element.getFirstChild();
        if (firstChild != null) {
            element.insertBefore(startElement, firstChild);
        } else {
            element.appendChild(startElement);
        }
    }

    private void addEndFlag(Element element, FilterUtils.Flag flag) {
        element.appendChild(element.getOwnerDocument().importNode(flag.getEndFlag(), true));
    }

}
