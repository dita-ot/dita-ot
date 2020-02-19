/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2015 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.module;

import org.apache.commons.io.FilenameUtils;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.module.reader.TempFileNameScheme;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.reader.DitaValReader;
import org.dita.dost.util.DitaClass;
import org.dita.dost.util.FilterUtils;
import org.dita.dost.util.FilterUtils.Flag;
import org.dita.dost.util.Job;
import org.dita.dost.util.Job.FileInfo;
import org.dita.dost.writer.ProfilingFilter;
import org.w3c.dom.*;
import org.xml.sax.XMLFilter;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.StringUtils.getExtProps;
import static org.dita.dost.util.StringUtils.getExtPropsFromSpecializations;
import static org.dita.dost.util.URLUtils.stripFragment;
import static org.dita.dost.util.URLUtils.toURI;
import static org.dita.dost.util.XMLUtils.*;

/**
 * Branch filter module.
 *
 * <p>Branch filtering is done with the following steps:</p>
 * <ol>
 *   <li>Split braches so that each branch will only contain a single ditavalref</li>
 *   <li>Generate copy-to attribute for each brach generated topicref</li>
 *   <li>Filter map based on branch filters</li>
 *   <li>Rewrite duplicate generated copy-to targets</li>
 *   <li>Copy and filter generated copy-to targets</li>
 *   <li>Filter topics that were not branch generated</li>
 * </ol>
 *
 * @since 2.2
 */
public class BranchFilterModule extends AbstractPipelineModuleImpl {

    private static final String SKIP_FILTER = "skip-filter";
    private static final String BRANCH_COPY_TO = "filter-copy-to";

    private final DitaValReader ditaValReader;
    private TempFileNameScheme tempFileNameScheme;
    private final Map<URI, FilterUtils> filterCache = new HashMap<>();
    /** Current map being processed, relative to temporary directory */
    private URI map;
    /** Absolute URI to map being processed. */
    protected URI currentFile;
    private final Set<URI> filtered = new HashSet<>();
    
    /* Sets to track what topics are renamed in map, still in map, filtered from map */ 
    private final Set<URI> renamedTopics = new HashSet<>();
    private final Set<URI> sameNameTopics = new HashSet<>();
    private final Set<URI> filteredTopics = new HashSet<>();

    public BranchFilterModule() {
        ditaValReader = new DitaValReader();
    }

    @Override
    public void setLogger(final DITAOTLogger logger) {
        super.setLogger(logger);
        ditaValReader.setLogger(logger);
    }

    @Override
    public void setJob(final Job job) {
        super.setJob(job);
        ditaValReader.setJob(job);
        try {
            tempFileNameScheme = (TempFileNameScheme) Class.forName(job.getProperty("temp-file-name-scheme")).newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        tempFileNameScheme.setBaseDir(job.getInputDir());
    }

    @Override
    public AbstractPipelineOutput execute(final AbstractPipelineInput input) throws DITAOTException {
        final Collection<FileInfo> fis = job.getFileInfo(fi -> fi.isInput || fi.isInputResource);
        for (FileInfo fi : fis) {
            processMap(fi.uri);
        }

        try {
            job.write();
        } catch (final IOException e) {
            throw new DITAOTException("Failed to serialize job configuration: " + e.getMessage(), e);
        }

        return null;
    }

    /**
     * Process map for branch replication.
     */
    protected void processMap(final URI map) {
        assert !map.isAbsolute();
        this.map = map;
        currentFile = job.tempDirURI.resolve(map);
        // parse
        logger.info("Processing " + currentFile);
        final Document doc;
        try {
            logger.debug("Reading " + currentFile);
            doc = job.getStore().getDocument(currentFile);
        } catch (final IOException e) {
            logger.error("Failed to parse " + currentFile, e);
            return;
        }

        logger.debug("Split branches and generate copy-to");
        splitBranches(doc.getDocumentElement(), Branch.EMPTY);
        logger.debug("Filter map");
        filterBranches(doc.getDocumentElement());
        logger.debug("Rewrite duplicate topic references");
        rewriteDuplicates(doc.getDocumentElement());
        logger.debug("Filter topics and generate copies");
        generateCopies(doc.getDocumentElement(), Collections.emptyList());
        logger.debug("Remove obsolete references");
        removeObsoleteReferences();
        logger.debug("Filter existing topics");
        filterTopics(doc.getDocumentElement(), Collections.emptyList());

        logger.debug("Writing " + currentFile);

        try {
            doc.setDocumentURI(currentFile.toString());
            job.getStore().writeDocument(doc, currentFile);
        } catch (final IOException e) {
            logger.error("Failed to serialize " + map.toString() + ": " + e.getMessage(), e);
        }
    }

    /** Rewrite href or copy-to if duplicates exist. */
    private void rewriteDuplicates(final Element root) {
        // collect href and copy-to
        final Map<URI, Map<Set<URI>, List<Attr>>> refs = new HashMap<>();
        for (final Element e: getTopicrefs(root)) {
            Attr attr = e.getAttributeNode(BRANCH_COPY_TO);
            if (attr == null) {
                attr = e.getAttributeNode(ATTRIBUTE_NAME_COPY_TO);
                if (attr == null) {
                    attr = e.getAttributeNode(ATTRIBUTE_NAME_HREF);
                }
            }
            if (attr != null) {
                final URI h = stripFragment(map.resolve(attr.getValue()));
                Map<Set<URI>, List<Attr>> attrsMap = refs.computeIfAbsent(h, k -> new HashMap<>());
                final Set<URI> currentFilter = getBranchFilters(e);
                List<Attr> attrs = attrsMap.computeIfAbsent(currentFilter, k -> new ArrayList<>());
                attrs.add(attr);
            }
        }
        // check and rewrite
        for (final Map.Entry<URI, Map<Set<URI>, List<Attr>>> ref: refs.entrySet()) {
            final Map<Set<URI>, List<Attr>> attrsMaps = ref.getValue();
            if (attrsMaps.size() > 1) {
                if (attrsMaps.containsKey(Collections.EMPTY_LIST)) {
                    attrsMaps.remove(Collections.EMPTY_LIST);
                } else {
                    Set<URI> first = attrsMaps.keySet().iterator().next();
                    attrsMaps.remove(first);
                }
                int i = 1;
                for (final Map.Entry<Set<URI>, List<Attr>> attrsMap: attrsMaps.entrySet()) {
                    final String suffix = "-" + i;
                    final List<Attr> attrs = attrsMap.getValue();
                    for (final Attr attr: attrs) {
                        final String gen = addSuffix(attr.getValue(), suffix);
                        logger.info(MessageUtils.getMessage("DOTJ065I", attr.getValue(), gen)
                                .setLocation(attr.getOwnerElement()).toString());
                        if (attr.getName().equals(BRANCH_COPY_TO)) {
                            attr.setValue(gen);
                        } else {
                            attr.getOwnerElement().setAttribute(BRANCH_COPY_TO, gen);
                        }

                        final URI dstUri = map.resolve(gen);
                        if (dstUri != null) {
                            final FileInfo hrefFileInfo = job.getFileInfo(currentFile.resolve(attr.getValue()));
                            if (hrefFileInfo != null) {
                                final URI newResult = addSuffix(hrefFileInfo.result, suffix);
                                final FileInfo.Builder dstBuilder = new FileInfo.Builder(hrefFileInfo)
                                        .uri(dstUri)
                                        .result(newResult);
                                if (hrefFileInfo.format == null) {
                                    dstBuilder.format(ATTR_FORMAT_VALUE_DITA);
                                }
                                final FileInfo dstFileInfo = dstBuilder.build();
                                job.add(dstFileInfo);
                            }
                        }
                    }
                    i++;
                }
            }
        }
    }

    private Set<URI> getBranchFilters(final Element e) {
        final Set<URI> res = new HashSet<>();
        Element current = e;
        while (current != null) {
            final List<Element> ditavalref = getChildElements(current, DITAVAREF_D_DITAVALREF);
            if (!ditavalref.isEmpty()) {
                res.add(toURI(ditavalref.get(0).getAttribute(ATTRIBUTE_NAME_HREF)));
            }
            final Node parent = current.getParentNode();
            if (parent != null && parent.getNodeType() == Node.ELEMENT_NODE) {
                current = (Element) parent;
            } else {
                break;
            }
        }
        return res;
    }

    /** Add suffix to file name */
    private static String addSuffix(final String href, final String suffix) {
        final int idx = href.lastIndexOf(".");
        return idx != -1
                ? (href.substring(0, idx) + suffix + href.substring(idx))
                : (href + suffix);
    }

    /** Add suffix to file name */
    private static URI addSuffix(final URI href, final String suffix) {
        return URI.create(addSuffix(href.toString(), suffix));
    }

    /** Get all topicrefs */
    private List<Element> getTopicrefs(final Element root) {
        final List<Element> res = new ArrayList<>();
        final NodeList all = root.getElementsByTagName("*");
        for (int i = 0; i < all.getLength(); i++) {
            final Element elem = (Element) all.item(i);
            if (MAP_TOPICREF.matches(elem)
                    && isDitaFormat(elem.getAttributeNode(ATTRIBUTE_NAME_FORMAT))
                    && !elem.getAttribute(ATTRIBUTE_NAME_SCOPE).equals(ATTR_SCOPE_VALUE_EXTERNAL)) {
                res.add(elem);
            }
        }
        return res;
    }

    private boolean isDitaFormat(final Attr formatAttr) {
        return formatAttr == null ||
                ATTR_FORMAT_VALUE_DITA.equals(formatAttr.getNodeValue()) ||
                ATTR_FORMAT_VALUE_DITAMAP.equals(formatAttr.getNodeValue());
    }

    /** Filter map and remove excluded content. */
    private void filterBranches(final Element root) {
        final String domains = root.getAttribute(ATTRIBUTE_NAME_DOMAINS);
        final String specializations = root.getAttribute(ATTRIBUTE_NAME_SPECIALIZATIONS);
        final QName[][] props = !domains.isEmpty()
                ? getExtProps(domains)
                : getExtPropsFromSpecializations(specializations);
        filterBranches(root, Collections.emptyList(), props);
    }

    private void filterBranches(final Element elem, final List<FilterUtils> filters, final QName[][] props) {
        final List<FilterUtils> fs = combineFilterUtils(elem, filters);

        boolean exclude = false;
        for (final FilterUtils f: fs) {
            exclude = f.needExclude(elem, props);
            if (exclude) {
                break;
            }
        }

        if (exclude) {
            addToFilteredSet(elem);
            elem.getParentNode().removeChild(elem);
        } else {
            final List<Element> childElements = getChildElements(elem);
            final Set<Flag> flags = fs.stream()
                    .flatMap(f -> f.getFlags(elem, props).stream())
                    .map(f -> f.adjustPath(currentFile, job))
                    .collect(Collectors.toSet());
            for (Flag flag : flags) {
                final Element startElement = (Element) elem.getOwnerDocument().importNode(flag.getStartFlag(), true);
                final Node firstChild = elem.getFirstChild();
                if (firstChild != null) {
                    elem.insertBefore(startElement, firstChild);
                } else {
                    elem.appendChild(startElement);
                }
                final Element endElement = (Element) elem.getOwnerDocument().importNode(flag.getEndFlag(), true);
                elem.appendChild(endElement);
            }
            for (final Element child : childElements) {
                filterBranches(child, fs, props);
            }
        }
    }
    
    /** When a branch is filtered, add references in branch to filtered set **/
    private void addToFilteredSet(final Element elem) {
        final String copyTo = elem.getAttribute(BRANCH_COPY_TO);
        final String href = elem.getAttribute(ATTRIBUTE_NAME_HREF);
        if (!copyTo.isEmpty()) {
            final URI dstUri = map.resolve(copyTo);
            filteredTopics.add(dstUri);
        }
        if (!href.isEmpty()) {
            final URI srcUri = map.resolve(href);
            filteredTopics.add(srcUri);
        }
        for (final Element child : getChildElements(elem)) {
            addToFilteredSet(child);
        }
    }

    private List<FilterUtils> combineFilterUtils(final Element topicref, final List<FilterUtils> filters) {
        final List<Element> ditavalRefs = getChildElements(topicref, DITAVAREF_D_DITAVALREF);
        assert ditavalRefs.size() <= 1;
        if (!ditavalRefs.isEmpty()) {
            List<FilterUtils> fs = new ArrayList<>(filters);
            final FilterUtils f = getFilterUtils(ditavalRefs.get(0));
            if (f != null) {
                fs = new ArrayList<>(fs);
                fs.add(f);
            }
            return fs;
        } else {
            return filters;
        }
    }

    /** Copy and filter topics for branches. These topics have a new name and will be added to job configuration. */
    private void generateCopies(final Element topicref, final List<FilterUtils> filters) {
        final List<FilterUtils> fs = combineFilterUtils(topicref, filters);

        final String copyTo = topicref.getAttribute(BRANCH_COPY_TO);
        final String href = topicref.getAttribute(ATTRIBUTE_NAME_HREF);
        if (!copyTo.isEmpty()) {
            final URI dstUri = map.resolve(copyTo);
            final URI dstAbsUri = job.tempDirURI.resolve(dstUri);
            final URI srcUri = map.resolve(href);
            final URI srcAbsUri = job.tempDirURI.resolve(srcUri);
            final FileInfo srcFileInfo = job.getFileInfo(srcUri);
            if (srcFileInfo != null) {
//                final FileInfo fi = new FileInfo.Builder(srcFileInfo).uri(dstUri).build();
//                 TODO: Maybe Job should be updated earlier?
//                job.add(fi);
                renamedTopics.add(srcUri);
                logger.info("Filtering " + srcAbsUri + " to " + dstAbsUri);
                final ProfilingFilter writer = new ProfilingFilter();
                writer.setLogger(logger);
                writer.setJob(job);
                writer.setFilterUtils(fs);
                writer.setCurrentFile(dstAbsUri);
                final List<XMLFilter> pipe = singletonList(writer);

                try {
                    job.getStore().transform(srcAbsUri, dstAbsUri, pipe);
                } catch (final DITAOTException e) {
                    logger.error("Failed to filter " + srcAbsUri + " to " + dstAbsUri + ": " + e.getMessage(), e);
                }
                topicref.setAttribute(ATTRIBUTE_NAME_HREF, copyTo);
                topicref.removeAttribute(BRANCH_COPY_TO);
                // disable filtering again
                topicref.setAttribute(SKIP_FILTER, Boolean.TRUE.toString());
            }
        } else if (!href.isEmpty()) {
            final URI srcUri = map.resolve(href);
            sameNameTopics.add(srcUri);
        }
        for (final Element child: getChildElements(topicref, MAP_TOPICREF)) {
            if (DITAVAREF_D_DITAVALREF.matches(child)) {
                continue;
            }
            generateCopies(child, fs);
        }
    }
    
    /** Remove files from job if they were renamed and no longer exist with original name **/
    private void removeObsoleteReferences() {
        /** If a file was renamed and no longer exists with original name, remove from job **/
        for (final URI file: renamedTopics) {
            if (!sameNameTopics.contains(file) && job.getFileInfo(file) != null) {
                job.remove(job.getFileInfo(file));
            }
        }
        /** If a file reference was filtered from map and no longer exists, remove from job **/
        for (final URI file: filteredTopics) {
            if (!sameNameTopics.contains(file) && job.getFileInfo(file) != null) {
                job.remove(job.getFileInfo(file));
            }
        }
    }

    /** Modify and filter topics for branches. These files use an existing file name. */
    private void filterTopics(final Element topicref, final List<FilterUtils> filters) {
        final List<FilterUtils> fs = combineFilterUtils(topicref, filters);

        final String href = topicref.getAttribute(ATTRIBUTE_NAME_HREF);
        final Attr skipFilter = topicref.getAttributeNode(SKIP_FILTER);
        final URI srcAbsUri = job.tempDirURI.resolve(map.resolve(href));
        if (!fs.isEmpty() && skipFilter == null
                && !filtered.contains(srcAbsUri)
                && !href.isEmpty()
                && !ATTR_SCOPE_VALUE_EXTERNAL.equals(topicref.getAttribute(ATTRIBUTE_NAME_SCOPE))
                && !ATTR_PROCESSING_ROLE_VALUE_RESOURCE_ONLY.equals(topicref.getAttribute(ATTRIBUTE_NAME_PROCESSING_ROLE))
                && isDitaFormat(topicref.getAttributeNode(ATTRIBUTE_NAME_FORMAT))) {
            final ProfilingFilter writer = new ProfilingFilter();
            writer.setLogger(logger);
            writer.setJob(job);
            writer.setFilterUtils(fs);
            writer.setCurrentFile(srcAbsUri);
            final List<XMLFilter> pipe = singletonList(writer);

            logger.info("Filtering " + srcAbsUri);
            try {
                job.getStore().transform(srcAbsUri, pipe);
            } catch (final DITAOTException e) {
                logger.error("Failed to filter " + srcAbsUri + ": " + e.getMessage(), e);
            }
            filtered.add(srcAbsUri);
        }
        if (skipFilter != null) {
            topicref.removeAttributeNode(skipFilter);
        }

        for (final Element child: getChildElements(topicref, MAP_TOPICREF)) {
            if (DITAVAREF_D_DITAVALREF.matches(child)) {
                continue;
            }
            filterTopics(child, fs);
        }
    }

    /**
     * Read and cache filter.
     **/
    private FilterUtils getFilterUtils(final Element ditavalRef) {
        if (ditavalRef.getAttribute(ATTRIBUTE_NAME_HREF).isEmpty()) {
            return null;
        }
        final URI href = toURI(ditavalRef.getAttribute(ATTRIBUTE_NAME_HREF));
        final URI tmp = currentFile.resolve(href);
        final FileInfo fi = job.getFileInfo(tmp);
        final URI ditaval = fi.src;
        FilterUtils f = filterCache.get(ditaval);
        if (f == null) {
            ditaValReader.filterReset();
            logger.info("Reading " + ditaval);
            ditaValReader.read(ditaval);
            Map<FilterUtils.FilterKey, FilterUtils.Action> filterMap = ditaValReader.getFilterMap();
            f = new FilterUtils(filterMap, ditaValReader.getForegroundConflictColor(), ditaValReader.getBackgroundConflictColor());
            f.setLogger(logger);
            filterCache.put(ditaval, f);
        }
        return f;
    }

    /**
     * Duplicate branches so that each {@code ditavalref} will in a separate branch.
     */
    void splitBranches(final Element elem, final Branch filter) {
        final List<Element> ditavalRefs = getChildElements(elem, DITAVAREF_D_DITAVALREF);
        if (ditavalRefs.size() > 0) {
            // remove ditavalrefs
            for (final Element branch: ditavalRefs) {
                elem.removeChild(branch);
            }
            // create additional branches after current element
            final List<Element> branches = new ArrayList<>(ditavalRefs.size());
            branches.add(elem);
            final Node next = elem.getNextSibling();
            for (int i = 1; i < ditavalRefs.size(); i++) {
                final Element clone = (Element) elem.cloneNode(true);
                if (next != null) {
                    elem.getParentNode().insertBefore(clone, next);
                } else {
                    elem.getParentNode().appendChild(clone);
                }
                branches.add(clone);
            }
            // insert ditavalrefs
            for (int i = 0; i < branches.size(); i++) {
                final Element branch = branches.get(i);
                final Element ditavalref = ditavalRefs.get(i);
                branch.appendChild(ditavalref);
                final Branch currentFilter = filter.merge(ditavalref);
                processAttributes(branch, currentFilter);
                final Branch childFilter = new Branch(currentFilter.resourcePrefix, currentFilter.resourceSuffix, Optional.empty(), Optional.empty());
                // process children of all branches
                for (final Element child: getChildElements(branch, MAP_TOPICREF)) {
                    if (DITAVAREF_D_DITAVALREF.matches(child)) {
                        continue;
                    }
                    splitBranches(child, childFilter);
                }
            }
        } else {
            processAttributes(elem, filter);
            for (final Element child: getChildElements(elem, MAP_TOPICREF)) {
                splitBranches(child, filter);
            }
        }
    }

    /** Immutable branch definition. */
    public static class Branch {
        /** Empty root branch */
        public static final Branch EMPTY = new Branch();
        public final Optional<String> resourcePrefix;
        public final Optional<String> resourceSuffix;
        public final Optional<String> keyscopePrefix;
        public final Optional<String> keyscopeSuffix;
        private Branch() {
            this.resourcePrefix = Optional.empty();
            this.resourceSuffix = Optional.empty();
            this.keyscopePrefix = Optional.empty();
            this.keyscopeSuffix = Optional.empty();
        }
        public Branch(final Optional<String> resourcePrefix, final Optional<String> resourceSuffix,
                      final Optional<String> keyscopePrefix, final Optional<String> keyscopeSuffix) {
//            final URI prefix = toURI(resourcePrefix).normalize();
//            if (prefix.toString().startsWith("..")) {
//                throw new Exception("ERROR: Resource prefix may not start with ..");
//            }
            this.resourcePrefix = resourcePrefix;
            this.resourceSuffix = resourceSuffix;
            this.keyscopePrefix = keyscopePrefix;
            this.keyscopeSuffix = keyscopeSuffix;
        }
        @Override
        public String toString() {
            return "{" + resourcePrefix + "," + resourceSuffix + ";" + keyscopePrefix + ", " + keyscopeSuffix + "}";
        }
        public Branch merge(final Element ditavalref) {
            return new Branch(
                getPrefix(ditavalref, this.resourcePrefix),
                getSuffix(ditavalref, this.resourceSuffix),
                getKeyscopePrefix(ditavalref, this.keyscopePrefix),
                getKeyscopeSuffix(ditavalref, this.keyscopeSuffix)
            );
        }
        private Optional<String> get(final Element ditavalref, final DitaClass cls) {
            for (final Element ditavalmeta: getChildElements(ditavalref, DITAVAREF_D_DITAVALMETA)) {
                final Optional<Element> childElements = getChildElement(ditavalmeta, cls);
                if (childElements.isPresent()) {
                    return Optional.of(getStringValue(childElements.get()));
                }
            }
            return Optional.empty();
        }
        private Optional<String> getPrefix(final Element ditavalref, final Optional<String> oldValue) {
            final Optional<String> v = get(ditavalref, DITAVAREF_D_DVR_RESOURCEPREFIX);
            return v.map(s -> Optional.of(oldValue.orElse("") + s)).orElse(oldValue);
        }
        private Optional<String> getSuffix(final Element ditavalref, final Optional<String> oldValue) {
            final Optional<String> v = get(ditavalref, DITAVAREF_D_DVR_RESOURCESUFFIX);
            return v.map(s -> Optional.of(s + oldValue.orElse(""))).orElse(oldValue);
        }
        private Optional<String> getKeyscopePrefix(final Element ditavalref, final Optional<String> oldValue) {
            final Optional<String> v = get(ditavalref, DITAVAREF_D_DVR_KEYSCOPEPREFIX);
            return v.map(s -> Optional.of(oldValue.orElse("") + s)).orElse(oldValue);
        }
        private Optional<String> getKeyscopeSuffix(final Element ditavalref, final Optional<String> oldValue) {
            final Optional<String> v = get(ditavalref, DITAVAREF_D_DVR_KEYSCOPESUFFIX);
            return v.map(s -> Optional.of(s + oldValue.orElse(""))).orElse(oldValue);
        }
    }

    private void processAttributes(final Element elem, final Branch filter) {
        if (filter.resourcePrefix.isPresent() || filter.resourceSuffix.isPresent()) {
            final String href = elem.getAttribute(ATTRIBUTE_NAME_HREF);
            final String copyTo = elem.getAttribute(ATTRIBUTE_NAME_COPY_TO);
            final String scope = elem.getAttribute(ATTRIBUTE_NAME_SCOPE);
            if ((!href.isEmpty() || !copyTo.isEmpty()) && !scope.equals(ATTR_SCOPE_VALUE_EXTERNAL)) {
                final FileInfo hrefFileInfo = job.getFileInfo(currentFile.resolve(href));
                final FileInfo copyToFileInfo = !copyTo.isEmpty() ? job.getFileInfo(currentFile.resolve(copyTo)) : null;
                final URI dstSource;
                dstSource = generateCopyTo((copyToFileInfo != null ? copyToFileInfo : hrefFileInfo).result, filter);
                final URI dstTemp = tempFileNameScheme.generateTempFileName(dstSource);
                final String dstPathFromMap = !copyTo.isEmpty() ? FilenameUtils.getPath(copyTo) : FilenameUtils.getPath(href);
                final FileInfo.Builder dstBuilder = new FileInfo.Builder(hrefFileInfo)
                        .result(dstSource)
                        .uri(dstTemp);
                if (dstBuilder.build().format == null) {
                    dstBuilder.format(ATTR_FORMAT_VALUE_DITA);
                }
                if (hrefFileInfo.src == null && href != null) {
                    if (copyToFileInfo != null) {
                        dstBuilder.src(copyToFileInfo.src);
                    }
                }
                final FileInfo dstFileInfo = dstBuilder
                        .build();

                elem.setAttribute(BRANCH_COPY_TO, dstPathFromMap + FilenameUtils.getName(dstTemp.toString()));
                if (!copyTo.isEmpty()) {
                    elem.removeAttribute(ATTRIBUTE_NAME_COPY_TO);
                }

                job.add(dstFileInfo);
            }
        }

        if (filter.keyscopePrefix.isPresent() || filter.keyscopeSuffix.isPresent()) {
            final StringBuilder buf = new StringBuilder();
            final String keyscope = elem.getAttribute(ATTRIBUTE_NAME_KEYSCOPE);
            if (!keyscope.isEmpty()) {
                for (final String key : keyscope.trim().split("\\s+")) {
                    filter.keyscopePrefix.ifPresent(buf::append);
                    buf.append(key);
                    filter.keyscopeSuffix.ifPresent(buf::append);
                    buf.append(' ');
                }
            } else {
                filter.keyscopePrefix.ifPresent(buf::append);
                filter.keyscopeSuffix.ifPresent(buf::append);
            }
            elem.setAttribute(ATTRIBUTE_NAME_KEYSCOPE, buf.toString().trim());
        }
    }

    static URI generateCopyTo(final URI href, final Branch filter) {
        final StringBuilder buf = new StringBuilder(href.toString());
        final Optional<String> suffix = filter.resourceSuffix;
        suffix.ifPresent(s -> {
            final int sep = buf.lastIndexOf(URI_SEPARATOR);
            final int i = buf.lastIndexOf(".");
            if (i != -1 && (sep == -1 || i > sep)) {
                buf.insert(i, s);
            } else {
                buf.append(s);
            }
        });
        final Optional<String> prefix = filter.resourcePrefix;
        prefix.ifPresent(s -> {
            final int i = buf.lastIndexOf(URI_SEPARATOR);
            if (i != -1) {
                buf.insert(i + 1, s);
            } else {
                buf.insert(0, s);
            }
        });
        return toURI(buf.toString());
    }

}
