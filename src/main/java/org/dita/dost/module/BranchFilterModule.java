/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */
package org.dita.dost.module;

import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.StringUtils.*;
import static org.dita.dost.util.URLUtils.*;
import static org.dita.dost.util.XMLUtils.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.util.XMLUtils;
import org.dita.dost.writer.ProfilingFilter;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.reader.DitaValReader;
import org.dita.dost.util.DitaClass;
import org.dita.dost.util.FilterUtils;
import org.dita.dost.util.Job.FileInfo;
import org.xml.sax.XMLFilter;

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
final class BranchFilterModule extends AbstractPipelineModuleImpl {

    private static final String SKIP_FILTER = "skip-filter";
    private static final String BRANCH_COPY_TO = "filter-copy-to";

    private final DocumentBuilder builder;
    private final DitaValReader ditaValReader;
    private final Map<URI, FilterUtils> filterCache = new HashMap<>();
    /** Current map being processed. */
    private URI map;

    public BranchFilterModule() {
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (final ParserConfigurationException e) {
            throw new RuntimeException("Failed to build parser: " + e.getMessage(), e);
        }
        ditaValReader = new DitaValReader();
        ditaValReader.initXMLReader(true);
    }
    
    @Override
    public void setLogger(final DITAOTLogger logger) {
        super.setLogger(logger);
        ditaValReader.setLogger(logger);
    }

    @Override
    public AbstractPipelineOutput execute(final AbstractPipelineInput input) throws DITAOTException {
        processMap(job.getInputMap());

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
        this.map = map;
        // parse
        final URI mapFile = job.tempDir.toURI().resolve(map);
        logger.info("Processing " + mapFile);
        final Document doc;
        try {
            logger.debug("Reading " + mapFile);
            doc = builder.parse(new InputSource(mapFile.toString()));
        } catch (final SAXException | IOException e) {
            logger.error("Failed to parse " + mapFile, e);
            return;
        }

        logger.debug("Split branches and generate copy-to");
        splitBranches(doc.getDocumentElement(), Branch.EMPTY);
        logger.debug("Filter map");
        filterBranches(doc.getDocumentElement());
        logger.debug("Rewrite duplicate topic references");
        rewriteDuplicates(doc.getDocumentElement());
        logger.debug("Filter topics and generate copies");
        generateCopies(doc.getDocumentElement(), Collections.<FilterUtils>emptyList());
        logger.debug("Filter existing topics");
        filterTopics(doc.getDocumentElement(), Collections.<FilterUtils>emptyList());

        logger.debug("Writing " + mapFile);
        StreamResult result = null;
        try {
            Transformer serializer = TransformerFactory.newInstance().newTransformer();
            result = new StreamResult(mapFile.toString());
            serializer.transform(new DOMSource(doc), result);
        } catch (final TransformerConfigurationException | TransformerFactoryConfigurationError e) {
            throw new RuntimeException(e);
        } catch (final TransformerException e) {
            logger.error("Failed to serialize " + map.toString() + ": " + e.getMessage(), e);
        } finally {
            try {
                close(result);
            } catch (final IOException e) {
                logger.error("Failed to close result stream for " + map.toString() + ": " + e.getMessage(), e);
            }
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
                Map<Set<URI>, List<Attr>> attrsMap = refs.get(h);
                if (attrsMap == null) {
                    attrsMap = new HashMap<>();
                    refs.put(h, attrsMap);
                }
                final Set<URI> currentFilter = getBranchFilters(e);
                List<Attr> attrs = attrsMap.get(currentFilter);
                if (attrs == null) {
                    attrs = new ArrayList<>();
                    attrsMap.put(currentFilter, attrs);
                }
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
                        logger.info(MessageUtils.getInstance().getMessage("DOTJ065I", attr.getValue(), gen)
                                .setLocation((Element) attr.getOwnerElement()).toString());
                        if (attr.getName().equals(BRANCH_COPY_TO)) {
                            attr.setValue(gen);
                        } else {
                            attr.getOwnerElement().setAttribute(BRANCH_COPY_TO, gen);
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

//    /** Get topicrefs that are part of a branch */
//    private List<Element> getBranchTopicrefs(final Element root) {
//        final List<Element> res = new ArrayList<>();
//        walkBranchTopicref(root, false, res);
//        return res;
//    }
//
//    /** Walker to collect topicrefs that are part of a branch. */
//    private void walkBranchTopicref(final Element elem, final boolean inBranch, final List<Element> res) {
//        final boolean b = inBranch || !getChildElements(elem, DITAVAREF_D_DITAVALREF).isEmpty();
//        if (b && MAP_TOPICREF.matches(elem)
//                && isDitaFormat(elem.getAttributeNode(ATTRIBUTE_NAME_FORMAT))
//                && !elem.getAttribute(ATTRIBUTE_NAME_SCOPE).equals(ATTR_SCOPE_VALUE_EXTERNAL)) {
//            res.add(elem);
//        }
//        for (final Element child: getChildElements(elem)) {
//            walkBranchTopicref(child, b, res);
//        }
//    }

    private boolean isDitaFormat(final Attr formatAttr) {
        return formatAttr == null ||
                ATTR_FORMAT_VALUE_DITA.equals(formatAttr.getNodeValue()) ||
                ATTR_FORMAT_VALUE_DITAMAP.equals(formatAttr.getNodeValue());
    }

    /** Filter map and remove excluded content. */
    private void filterBranches(final Element root) {
        final String[][] props = getExtProps(root.getAttribute(ATTRIBUTE_NAME_DOMAINS));
        filterBranches(root, Collections.<FilterUtils>emptyList(), props);
    }

    private void filterBranches(final Element elem, final List<FilterUtils> filters, final String[][] props) {
        final List<FilterUtils> fs = combineFilterUtils(elem, filters);

        boolean exclude = false;
        for (final FilterUtils f: fs) {
            exclude = exclude(elem, f, props);
            if (exclude) {
                break;
            }
        }

        if (exclude) {
            elem.getParentNode().removeChild(elem);
        } else {
            for (final Element child : getChildElements(elem)) {
                filterBranches(child, fs, props);
            }
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

    /** Test if element should be excluded based on fiter. */
    private boolean exclude(final Element element, final FilterUtils filterUtils, final String[][] props) {
        final AttributesBuilder buf = new AttributesBuilder();
        final NamedNodeMap attrs = element.getAttributes();
        for (int i = 0; i < attrs.getLength() ; i++) {
            final Node attr = attrs.item(i);
            if (attr.getNodeType() == Node.ATTRIBUTE_NODE) {
                buf.add((Attr) attr);
            }
        }
        return filterUtils.needExclude(buf.build(), props);
    }

    /** Copy and filter topics for branches. These topics have a new name and will be added to job configuration. */
    private void generateCopies(final Element topicref, final List<FilterUtils> filters) {
        final List<FilterUtils> fs = combineFilterUtils(topicref, filters);

        final String copyTo = topicref.getAttribute(BRANCH_COPY_TO);
        if (!copyTo.isEmpty()) {
            final URI dstUri = map.resolve(copyTo);
            final URI dstAbsUri = job.tempDir.toURI().resolve(dstUri);
            final String href = topicref.getAttribute(ATTRIBUTE_NAME_HREF);
            final URI srcUri = map.resolve(href);
            final URI srcAbsUri = job.tempDir.toURI().resolve(srcUri);
            final FileInfo srcFileInfo = job.getFileInfo(srcUri);
            if (srcFileInfo != null) {
                final FileInfo fi = new FileInfo.Builder(srcFileInfo).uri(dstUri).build();
                // TODO: Maybe Job should be updated earlier?
                job.add(fi);
                logger.info("Filtering " + srcAbsUri + " to " + dstAbsUri);
                final List<XMLFilter> pipe = new ArrayList<>();
                // TODO: replace multiple profiling filters with a merged filter utils
                for (final FilterUtils f : fs) {
                    final ProfilingFilter writer = new ProfilingFilter();
                    writer.setLogger(logger);
                    writer.setJob(job);
                    writer.setFilterUtils(f);
                    pipe.add(writer);
                }
                final File dstDirUri = new File(dstAbsUri.resolve("."));
                if (!dstDirUri.exists() && !dstDirUri.mkdirs()) {
                    logger.error("Failed to create directory " + dstDirUri);
                }
                try {
                    XMLUtils.transform(srcAbsUri,
                                       dstAbsUri,
                                       pipe);
                } catch (final DITAOTException e) {
                    logger.error("Failed to filter " + srcAbsUri + " to " + dstAbsUri + ": " + e.getMessage(), e);
                }
                topicref.setAttribute(ATTRIBUTE_NAME_HREF, copyTo);
                topicref.removeAttribute(BRANCH_COPY_TO);
                // disable filtering again
                topicref.setAttribute(SKIP_FILTER, Boolean.TRUE.toString());
            }
        }
        for (final Element child: getChildElements(topicref, MAP_TOPICREF)) {
            if (DITAVAREF_D_DITAVALREF.matches(child)) {
                continue;
            }
            generateCopies(child, fs);
        }
    }

    /** Modify and filter topics for branches. These files use an existing file name. */
    private void filterTopics(final Element topicref, final List<FilterUtils> filters) {
        final List<FilterUtils> fs = combineFilterUtils(topicref, filters);

        final String href = topicref.getAttribute(ATTRIBUTE_NAME_HREF);
        final Attr skipFilter = topicref.getAttributeNode(SKIP_FILTER);
        if (!fs.isEmpty() && skipFilter == null
                && !href.isEmpty()
                && !ATTR_SCOPE_VALUE_EXTERNAL.equals(topicref.getAttribute(ATTRIBUTE_NAME_SCOPE))) {
            final List<XMLFilter> pipe = new ArrayList<>();
            // TODO: replace multiple profiling filters with a merged filter utils
            for (final FilterUtils f : fs) {
                final ProfilingFilter writer = new ProfilingFilter();
                writer.setLogger(logger);
                writer.setJob(job);
                writer.setFilterUtils(f);
                pipe.add(writer);
            }

            final URI srcAbsUri = job.tempDir.toURI().resolve(map.resolve(href));
            logger.info("Filtering " + srcAbsUri);
            try {
                XMLUtils.transform(toFile(srcAbsUri),
                        pipe);
            } catch (final DITAOTException e) {
                logger.error("Failed to filter " + srcAbsUri + ": " + e.getMessage(), e);
            }
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
        final URI ditaval = job.getFileInfo(map).src.resolve(ditavalRef.getAttribute(ATTRIBUTE_NAME_HREF));
        FilterUtils f = filterCache.get(ditaval);
        if (f == null) {
            ditaValReader.filterReset();
            ditaValReader.read(ditaval);
            Map<FilterUtils.FilterKey, FilterUtils.Action> filterMap = ditaValReader.getFilterMap();
            f = new FilterUtils(filterMap);
            f.setLogger(logger);
            filterCache.put(ditaval, f);
        }
        return f;
    }

    /**
     * Duplicate branches so that each {@code ditavalref} will in a separate branch.
     */
    private void splitBranches(final Element elem, final Branch filter) {
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
                branch.insertBefore(ditavalref, branch.getFirstChild());
                final Branch currentFilter = filter.merge(ditavalref);
                processAttributes(branch, currentFilter);
                // process children of all branches
                for (final Element child: getChildElements(branch, MAP_TOPICREF)) {
                    if (DITAVAREF_D_DITAVALREF.matches(child)) {
                        continue;
                    }
                    splitBranches(child, currentFilter);
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
        static final Branch EMPTY = new Branch();
        final String resourcePrefix;
        final String resourceSuffix;
        final String keyscopePrefix;
        final String keyscopeSuffix;
        private Branch() {
            this.resourcePrefix = null;
            this.resourceSuffix = null;
            this.keyscopePrefix = null;
            this.keyscopeSuffix = null;
        }
        Branch(String resourcePrefix, String resourceSuffix, String keyscopePrefix, String keyscopeSuffix) {
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
        private Branch merge(final Element ditavalref) {
            return new Branch(
                getPrefix(ditavalref, this.resourcePrefix),
                getSuffix(ditavalref, this.resourceSuffix),
                getKeyscopePrefix(ditavalref, this.keyscopePrefix),
                getKeyscopeSuffix(ditavalref, this.keyscopeSuffix)
            );
        }
        private String get(final Element ditavalref, final DitaClass cls) {
            for (final Element ditavalmeta: getChildElements(ditavalref, DITAVAREF_D_DITAVALMETA)) {
                for (final Element resoucePrefix: getChildElements(ditavalmeta, cls)) {
                    return getStringValue(resoucePrefix);
                }
            }
            return null;
        }
        private String getPrefix(final Element ditavalref, final String oldValue) {
            final String v = get(ditavalref, DITAVAREF_D_DVR_RESOURCEPREFIX);
            if (v != null) {
                return (oldValue != null ? oldValue : "") + v;
            } 
            return oldValue;
        }
        private String getSuffix(final Element ditavalref, final String oldValue) {
            final String v = get(ditavalref, DITAVAREF_D_DVR_RESOURCESUFFIX);
            if (v != null) {
                return v + (oldValue != null ? oldValue : "");
            }
            return oldValue;
        }
        private String getKeyscopePrefix(final Element ditavalref, final String oldValue) {
            final String v = get(ditavalref, DITAVAREF_D_DVR_KEYSCOPEPREFIX);
            if (v != null) {
                return (oldValue != null ? oldValue : "") + v;
            }
            return oldValue;
        }
        private String getKeyscopeSuffix(final Element ditavalref, final String oldValue) {
            final String v = get(ditavalref, DITAVAREF_D_DVR_KEYSCOPESUFFIX);
            if (v != null) {
                return v + (oldValue != null ? oldValue : "");
            }
            return oldValue;
        }
    }
    
    private void processAttributes(final Element elem, final Branch filter) {
        if (filter.resourcePrefix != null || filter.resourceSuffix != null) {
            final String href = elem.getAttribute(ATTRIBUTE_NAME_HREF);
            final String copyTo = elem.getAttribute(ATTRIBUTE_NAME_COPY_TO);
            final String scope = elem.getAttribute(ATTRIBUTE_NAME_SCOPE);
            if ((!href.isEmpty() || !copyTo.isEmpty()) && !scope.equals(ATTR_SCOPE_VALUE_EXTERNAL)) {
                elem.setAttribute(BRANCH_COPY_TO,
                        generateCopyTo(copyTo.isEmpty() ? href : copyTo, filter).toString());
                if (!copyTo.isEmpty()) {
                    elem.removeAttribute(ATTRIBUTE_NAME_COPY_TO);
                }
            }
        }

        if ((filter.keyscopePrefix != null || filter.keyscopeSuffix != null)) {
            final String keyscope = elem.getAttribute(ATTRIBUTE_NAME_KEYSCOPE);
            if (!keyscope.isEmpty()) {
                final StringBuilder buf = new StringBuilder();
                for (final String key : keyscope.trim().split("\\s+")) {
                    if (filter.keyscopePrefix != null) {
                        buf.append(filter.keyscopePrefix);
                    }
                    buf.append(key);
                    if (filter.keyscopeSuffix != null) {
                        buf.append(filter.keyscopeSuffix);
                    }
                    buf.append(' ');
                }
                elem.setAttribute(ATTRIBUTE_NAME_KEYSCOPE, buf.toString().trim());
            }
        }
    }
    
    static URI generateCopyTo(final String href, final Branch filter) {
        final StringBuilder buf = new StringBuilder(href);
        final String suffix = filter.resourceSuffix;
        if (suffix != null) {
            final int sep = buf.lastIndexOf(URI_SEPARATOR);
            final int i = buf.lastIndexOf(".");
            if (i != -1 && (sep == -1 || i > sep)) {
                buf.insert(i, suffix);
            } else {
                buf.append(suffix);
            }
        }
        final String prefix = filter.resourcePrefix;
        if (prefix != null) {
            final int i = buf.lastIndexOf(URI_SEPARATOR);
            if (i != -1) {
                buf.insert(i + 1, prefix);
            } else {
                buf.insert(0, prefix);
            }
        }
        return toURI(buf.toString());
    }
    
}
