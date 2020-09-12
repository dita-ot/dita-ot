/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2004, 2005 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.

 */
package org.dita.dost.reader;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.MessageBean;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.util.DitaClass;
import org.dita.dost.util.Job;
import org.dita.dost.writer.AbstractXMLFilter;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.net.URI;
import java.util.*;
import java.util.function.Predicate;

import static org.dita.dost.util.Configuration.ditaFormat;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.URLUtils.*;
import static org.dita.dost.util.XMLUtils.nonDitaContext;

/**
 * Parse relevant DITA files and collect information.
 *
 * <p>
 * <strong>Not thread-safe</strong>. Instances can be reused by calling
 * {@link #reset()} between calls to parse.
 * </p>
 */
public final class GenListModuleReader extends AbstractXMLFilter {

    public static final URI ROOT_URI = toURI("ROOT");

    /**
     * Output utilities
     */
    private Job job;
    /**
     * Absolute basedir of the current parsing file
     */
    private URI currentDir = null;
    /**
     * Flag for conref in parsing file
     */
    private boolean hasConRef = false;
    /**
     * Flag for href in parsing file
     */
    private boolean hasHref = false;
    /**
     * Flag for keyref in parsing file
     */
    private boolean hasKeyRef = false;
    /**
     * Flag for whether parsing file contains coderef
     */
    private boolean hasCodeRef = false;
    /**
     * Set of all targets referred in current parsing file except conref and copy-to
     */
    private final Set<Reference> nonConrefCopytoTargets = new LinkedHashSet<>(64);
    /**
     * Set of conref targets refered in current parsing file
     */
    private final Set<URI> conrefTargets = new HashSet<>(32);
    /**
     * Set of href nonConrefCopytoTargets refered in current parsing file
     */
    private final Set<URI> hrefTargets = new HashSet<>(32);
    /**
     * Set of subject schema files
     */
    private final Set<URI> schemeSet = new HashSet<>(32);
    /**
     * Set of coderef or object target files
     */
    private final Set<URI> coderefTargetSet = new HashSet<>(16);
    /**
     * Set of sources of those copy-to that were ignored
     */
    private final Set<URI> ignoredCopytoSourceSet = new HashSet<>(16);
    /**
     * Map of copy-to target to souce
     */
    private final Map<URI, URI> copytoMap = new HashMap<>(16);
    /**
     * Flag for conrefpush
     */
    private boolean hasconaction = false;
    /**
     * DITA class values for open elements
     **/
    private final Deque<DitaClass> classes = new LinkedList<>();
    /**
     * Flag used to mark if current file is still valid after filtering
     */
    private boolean isValidInput = false;
    /**
     * Set of outer dita files
     */
    private final Set<URI> outDitaFilesSet = new HashSet<>(64);
    /**
     * Absolute system path to input file parent directory
     */
    private URI rootDir = null;
    /**
     * Stack for @processing-role value
     */
    private final Stack<String> processRoleStack = new Stack<>();
    /**
     * Topics with processing role of "resource-only"
     */
    private final Set<URI> resourceOnlySet = new HashSet<>(32);
    /**
     * Topics with processing role of "normal"
     */
    private final Set<URI> normalProcessingRoleSet = new HashSet<>(32);
    /**
     * Topics referenced from something other than <topicref>
     */
    private final Set<URI> nonTopicrefReferenceSet = new HashSet<>(32);
    /**
     * Subject scheme relative file paths.
     */
    private final Set<URI> schemeRefSet = new HashSet<>(32);
    /**
     * Relationship graph between subject schema. Keys are subject scheme map paths and values
     * are subject scheme map paths, both relative to base directory. A key {@link #ROOT_URI} contains all subject scheme maps.
     */
    private final Map<URI, Set<URI>> schemeRelationGraph = new LinkedHashMap<>();
    private boolean isRootElement = true;
    private DitaClass rootClass = null;
    private Predicate<String> formatFilter;

    /**
     * Set output utilities.
     *
     * @param job output utils
     */
    public void setJob(final Job job) {
        this.job = job;
    }

    /**
     * Get out file set.
     *
     * @return out file set
     */
    public Set<URI> getOutDitaFilesSet() {
        return outDitaFilesSet;
    }

    /**
     * Get scheme set.
     *
     * @return scheme set
     */
    public Set<URI> getSchemeSet() {
        return schemeSet;
    }

    /**
     * Get scheme ref set.
     *
     * @return scheme ref set
     */
    public Set<URI> getSchemeRefSet() {
        return schemeRefSet;
    }

    /**
     * Is the processed file a DITA topic.
     *
     * @return {@code true} if DITA topic, otherwise {@code false}
     */
    public boolean isDitaTopic() {
        if (isRootElement) {
            throw new IllegalStateException();
        }
        return rootClass == null || TOPIC_TOPIC.matches(rootClass);
    }

    public String currentFileFormat() {
        if (rootClass == null || TOPIC_TOPIC.matches(rootClass)) {
            return ATTR_FORMAT_VALUE_DITA;
        } else if (MAP_MAP.matches(rootClass)) {
            return ATTR_FORMAT_VALUE_DITAMAP;
        } else {
            return null;
        }
    }

    /**
     * Is the currently processed file a DITA map.
     *
     * @return {@code true} if DITA map, otherwise {@code false}
     */
    public boolean isDitaMap() {
        if (isRootElement) {
            throw new IllegalStateException();
        }
        return rootClass != null && MAP_MAP.matches(rootClass);
    }

    /**
     * Get relationship graph between subject schema. Keys are subject scheme map paths and values
     * are subject scheme map paths, both relative to base directory. A key {@link #ROOT_URI} contains all subject scheme maps.
     *
     * @return relationship graph
     */
    public Map<URI, Set<URI>> getRelationshipGrap() {
        return schemeRelationGraph;
    }

    public void setPrimaryDitamap(final URI primaryDitamap) {
        assert primaryDitamap.isAbsolute();
        this.rootDir = primaryDitamap.resolve(".");
    }

    /**
     * To see if the parsed file has conref inside.
     *
     * @return true if has conref and false otherwise
     */
    public boolean hasConRef() {
        return hasConRef;
    }

    /**
     * To see if the parsed file has keyref inside.
     *
     * @return true if has keyref and false otherwise
     */
    public boolean hasKeyRef() {
        return hasKeyRef;
    }

    /**
     * To see if the parsed file has coderef inside.
     *
     * @return true if has coderef and false otherwise
     */
    public boolean hasCodeRef() {
        return hasCodeRef;
    }

    /**
     * To see if the parsed file has href inside.
     *
     * @return true if has href and false otherwise
     */
    public boolean hasHref() {
        return hasHref;
    }

    /**
     * Get copy-to map.
     *
     * @return map of copy-to target to souce
     */
    public Map<URI, URI> getCopytoMap() {
        return copytoMap;
    }

    /**
     * Get the href target.
     *
     * @return Returns the hrefTargets.
     */
    public Set<URI> getHrefTargets() {
        return hrefTargets;
    }

    /**
     * Get conref targets.
     *
     * @return Returns the conrefTargets.
     */
    public Set<URI> getConrefTargets() {
        return conrefTargets;
    }

    /**
     * Get coderef targets.
     *
     * @return Returns coderef targets.
     */
    public Set<URI> getCoderefTargets() {
        return coderefTargetSet;
    }

    /**
     * Set current file absolute path
     *
     * @param currentFile absolute path to current file
     */
    public void setCurrentFile(final URI currentFile) {
        assert currentFile.isAbsolute();
        super.setCurrentFile(currentFile);
        currentDir = currentFile.resolve(".");
    }

    /**
     * Check if the current file is valid after filtering.
     *
     * @return true if valid and false otherwise
     */
    public boolean isValidInput() {
        return isValidInput;
    }

    /**
     * Check if the current file has conaction.
     *
     * @return true if has conaction and false otherwise
     */
    public boolean hasConaction() {
        return hasconaction;
    }

    public Set<Reference> getNonConrefCopytoTargets() {
        return nonConrefCopytoTargets;
    }

    public Set<URI> getCoderefTargetSet() {
        return coderefTargetSet;
    }

    public Set<URI> getIgnoredCopytoSourceSet() {
        return ignoredCopytoSourceSet;
    }

    public Set<URI> getResourceOnlySet() {
        return resourceOnlySet;
    }

    public Set<URI> getNormalProcessingRoleSet() {
        return normalProcessingRoleSet;
    }

    public Set<URI> getNonTopicrefReferenceSet() {
        return nonTopicrefReferenceSet;
    }

    /**
     * Reset the internal variables.
     */
    public void reset() {
        hasKeyRef = false;
        hasConRef = false;
        hasHref = false;
        hasCodeRef = false;
        currentDir = null;
        classes.clear();
        isValidInput = false;
        hasconaction = false;
        coderefTargetSet.clear();
        nonConrefCopytoTargets.clear();
        hrefTargets.clear();
        conrefTargets.clear();
        copytoMap.clear();
        ignoredCopytoSourceSet.clear();
        outDitaFilesSet.clear();
        schemeSet.clear();
        schemeRefSet.clear();
        processRoleStack.clear();
        isRootElement = true;
        rootClass = null;
        // Don't clean resourceOnlySet, normalProcessingRoleSet, or nonTopicrefReferenceSet
    }

    @Override
    public void startDocument() throws SAXException {
        if (currentDir == null) {
            throw new IllegalStateException();
        }

        processRoleStack.push(ATTR_PROCESSING_ROLE_VALUE_NORMAL);

        getContentHandler().startDocument();
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes atts)
            throws SAXException {
        handleRootElement(atts);
        handleSubjectScheme(atts);

        String processingRole = atts.getValue(ATTRIBUTE_NAME_PROCESSING_ROLE);
        if (processingRole == null) {
            processingRole = processRoleStack.peek();
        }
        processRoleStack.push(processingRole);

        final DitaClass cls = DitaClass.getInstance(atts);

        final URI href = toURI(atts.getValue(ATTRIBUTE_NAME_HREF));
        final String scope = atts.getValue(ATTRIBUTE_NAME_SCOPE);
        if (href != null && href.getPath() != null && !href.getPath().isEmpty() &&
                !ATTR_SCOPE_VALUE_EXTERNAL.equals(scope) && !ATTR_SCOPE_VALUE_PEER.equals(scope)) {
            if (isFormatDita(atts.getValue(ATTRIBUTE_NAME_FORMAT)) && !isDitaMap() &&
                    !job.crawlTopics()) {
                // Topic link within a topic, ignore if only crawling map
            } else if (!(MAP_TOPICREF.matches(cls))) {
                nonTopicrefReferenceSet.add(stripFragment(currentDir.resolve(href)));
            } else if (ATTR_PROCESSING_ROLE_VALUE_RESOURCE_ONLY.equals(processingRole)) {
                resourceOnlySet.add(stripFragment(currentDir.resolve(href)));
            } else {
                normalProcessingRoleSet.add(stripFragment(currentDir.resolve(href)));
            }
        }

        if (cls != null && cls.isValid()) {
            classes.addFirst(cls);
        } else {
            classes.addFirst(null);
        }

        if (!(cls != null && cls.isValid()) && !ELEMENT_NAME_DITA.equals(localName)) {
            if (nonDitaContext(classes)) {
                // Normal case for bad class: in non DITA context, no message
            } else if (atts.getValue(ATTRIBUTE_NAME_CLASS) == null) { // Missing @class
                logger.info(MessageUtils.getMessage("DOTJ030I", localName).setLocation(atts).toString());
            } else { // Invalid DITA @class
                logger.info(MessageUtils.getMessage("DOTJ070I", atts.getValue(ATTRIBUTE_NAME_CLASS), localName).setLocation(atts).toString());
            }
        } else {
            if ((MAP_MAP.matches(cls)) || (TOPIC_TITLE.matches(cls))) {
                isValidInput = true;
            }

            parseConrefAttr(atts);
            if (PR_D_CODEREF.matches(cls)) {
                parseCoderef(atts);
            } else if (TOPIC_OBJECT.matches(cls)) {
                parseObject(atts);
            } else if (MAP_TOPICREF.matches(cls)) {
                parseAttribute(atts, ATTRIBUTE_NAME_HREF);
                parseAttribute(atts, ATTRIBUTE_NAME_COPY_TO);
            } else {
                parseAttribute(atts, ATTRIBUTE_NAME_HREF);
            }
            parseConactionAttr(atts);
            parseConkeyrefAttr(atts);
            parseKeyrefAttr(atts);
        }

        getContentHandler().startElement(uri, localName, qName, atts);
    }

    private void parseCoderef(final Attributes atts) {
        final URI href = toURI(atts.getValue(ATTRIBUTE_NAME_HREF));
        if (href == null) {
            return;
        }
        final String attrScope = atts.getValue(ATTRIBUTE_NAME_SCOPE);
        if (ATTR_SCOPE_VALUE_EXTERNAL.equals(attrScope) || ATTR_SCOPE_VALUE_PEER.equals(attrScope)
                || href.toString().startsWith(SHARP)) {
            return;
        }

        hasCodeRef = true;
        final URI filename = stripFragment(href.isAbsolute() ? href : currentDir.resolve(href));
        assert filename.isAbsolute();
        coderefTargetSet.add(filename);
    }

    private void parseObject(final Attributes atts) {
        URI attrValue = toURI(atts.getValue(ATTRIBUTE_NAME_DATA));
        if (attrValue == null || attrValue.isAbsolute()) {
            return;
        }

        URI filename;
        final URI codebase = toURI(atts.getValue(ATTRIBUTE_NAME_CODEBASE));
        if (codebase != null) {
            if (codebase.isAbsolute()) {
                filename = codebase.resolve(attrValue);
            } else {
                filename = currentDir.resolve(codebase).resolve(attrValue);
            }
        } else {
            filename = currentDir.resolve(attrValue);
        }
        filename = stripFragment(filename);
        assert filename.isAbsolute();

        nonConrefCopytoTargets.add(new Reference(filename, ATTR_FORMAT_VALUE_NONDITA));
    }

    private void handleSubjectScheme(final Attributes atts) {
        final URI href = toURI(atts.getValue(ATTRIBUTE_NAME_HREF));
        final String classValue = atts.getValue(ATTRIBUTE_NAME_CLASS);
        // Generate Scheme relationship graph
        if (SUBJECTSCHEME_SUBJECTSCHEME.matches(classValue)) {
            // Make it easy to do the BFS later.
            final URI key = ROOT_URI;
            final Set<URI> children = schemeRelationGraph.containsKey(key) ? schemeRelationGraph.get(key) : new LinkedHashSet<>();
            children.add(currentFile);
            schemeRelationGraph.put(key, children);
            schemeRefSet.add(currentFile);
        } else if (SUBJECTSCHEME_SCHEMEREF.matches(classValue)) {
            if (href != null) {
                final URI key = currentFile;
                final Set<URI> children = schemeRelationGraph.containsKey(key) ? schemeRelationGraph.get(key) : new LinkedHashSet<>();
                final URI child = currentFile.resolve(href);
                children.add(child);
                schemeRelationGraph.put(key, children);
            }
        }
    }

    private void handleRootElement(final Attributes atts) {
        if (isRootElement) {
            isRootElement = false;
            final String classValue = atts.getValue(ATTRIBUTE_NAME_CLASS);
            if (classValue != null) {
                rootClass = DitaClass.getInstance(atts.getValue(ATTRIBUTE_NAME_CLASS));
            }
        }
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName) throws SAXException {
        // @processing-role
        processRoleStack.pop();
        classes.pop();

        getContentHandler().endElement(uri, localName, qName);
    }

    /**
     * Clean up.
     */
    @Override
    public void endDocument() throws SAXException {
        processRoleStack.pop();

        getContentHandler().endDocument();
    }

    /**
     * Parse the input attributes for needed information.
     *
     * @param atts     all attributes
     * @param attrName attributes to process
     */
    private void parseAttribute(final Attributes atts, final String attrName) throws SAXException {
        URI attrValue = toURI(atts.getValue(attrName));
        if (attrValue == null) {
            return;
        }

        //Check if this attribute will be ignored due to conref
        final String attrConref = atts.getValue(ATTRIBUTE_NAME_CONREF);
        if (attrConref != null && !attrConref.isEmpty() && ATTR_VALUE_DITA_USE_CONREF_TARGET.equals(attrValue.toString())) {
            return;
        }

        final String attrClass = atts.getValue(ATTRIBUTE_NAME_CLASS);
        final String attrScope = atts.getValue(ATTRIBUTE_NAME_SCOPE);

        // external resource is filtered here.
        if (ATTR_SCOPE_VALUE_EXTERNAL.equals(attrScope) || ATTR_SCOPE_VALUE_PEER.equals(attrScope)
                || attrValue.toString().startsWith(SHARP)) {
            return;
        }

        final URI filename = stripFragment(attrValue.isAbsolute() ? attrValue : currentDir.resolve(attrValue));
        assert filename.isAbsolute();

        final String attrType = atts.getValue(ATTRIBUTE_NAME_TYPE);
        if (MAP_TOPICREF.matches(attrClass) && ATTR_TYPE_VALUE_SUBJECT_SCHEME.equalsIgnoreCase(attrType)) {
            schemeSet.add(filename);
        }

        final String attrFormat = getFormat(atts);

        if (ATTRIBUTE_NAME_HREF.equals(attrName)) {
            hasHref = true;
            // Collect non-conref and non-copyto targets
            if (isFormatDita(attrFormat) && !isDitaMap() &&
                    !job.crawlTopics()) {
                // DITA link in a topic, but not crawling topics
            } else if ((followLinks() && canFollow(attrValue))
                    || TOPIC_IMAGE.matches(attrClass)
                    || SVG_D_SVGREF.matches(attrClass)
                    || DITAVAREF_D_DITAVALREF.matches(attrClass)) {
                nonConrefCopytoTargets.add(new Reference(filename, attrFormat));
            }
        }

        if (isFormatDita(attrFormat)) { // && formatFilter.test(attrFormat)
            if (ATTRIBUTE_NAME_HREF.equals(attrName) && canFollow(attrValue)) {
                if (followLinks()) {
                    hrefTargets.add(filename);
                    toOutFile(filename, atts);
                }
            } else if (ATTRIBUTE_NAME_COPY_TO.equals(attrName)) {
                final URI copyTo = toURI(atts.getValue(ATTRIBUTE_NAME_HREF));
                if (copyTo != null) {
                    if (copyTo.toString().isEmpty()) {
                        logger.warn("Copy-to task [href=\"\" copy-to=\"" + filename + "\"] was ignored.");
                    } else {
                        final URI value = stripFragment(currentDir.resolve(copyTo));
                        if (copytoMap.get(filename) != null) {
                            if (!value.equals(copytoMap.get(filename))) {
                                logger.warn(MessageUtils.getMessage("DOTX065W", copyTo.toString(), filename.toString())
                                        .setLocation(atts).toString());
                            }
                            ignoredCopytoSourceSet.add(value);
                        } else {
                            copytoMap.put(filename, value);
                        }
                    }
                }
            }
        }
    }

    /**
     * Make educated guess in advance whether URI can be resolved to a file.
     */
    public static boolean canFollow(URI href) {
        if (href != null && href.getScheme() != null && href.getScheme().equals("mailto")) {
            return false;
        }
        return true;
    }

    private String getFormat(Attributes atts) {
        final String attrClass = atts.getValue(ATTRIBUTE_NAME_CLASS);
        if (TOPIC_IMAGE.matches(attrClass)) {
            return ATTR_FORMAT_VALUE_IMAGE;
        } else if (TOPIC_OBJECT.matches(attrClass)) {
            throw new IllegalArgumentException();
            //return ATTR_FORMAT_VALUE_HTML;
        } else {
            return atts.getValue(ATTRIBUTE_NAME_FORMAT);
        }
    }

    private void parseConrefAttr(final Attributes atts) throws SAXException {
        String attrValue = atts.getValue(ATTRIBUTE_NAME_CONREF);
        if (attrValue != null) {
            if (attrValue.isEmpty()) {
                logger.warn(MessageUtils.getMessage("DOTJ081W").setLocation(atts).toString());
            } else {
                hasConRef = true;

                URI filename;
                final URI target = toURI(attrValue);
                if (isAbsolute(target)) {
                    filename = target;
                } else if (attrValue.startsWith(SHARP)) {
                    filename = currentFile;
                } else {
                    filename = currentDir.resolve(target);
                }
                filename = stripFragment(filename);

                // Collect only conref target topic files
                conrefTargets.add(filename);
                toOutFile(filename, atts);
            }
        }
    }

    private void parseConkeyrefAttr(final Attributes atts) {
        final String conkeyref = atts.getValue(ATTRIBUTE_NAME_CONKEYREF);
        if (conkeyref != null) {
            hasConRef = true;
        }
    }

    public final static String[] KEYREF_ATTRS = new String[]{
            ATTRIBUTE_NAME_KEYREF,
            ATTRIBUTE_NAME_CONKEYREF,
            ATTRIBUTE_NAME_ARCHIVEKEYREFS,
            ATTRIBUTE_NAME_CLASSIDKEYREF,
            ATTRIBUTE_NAME_CODEBASEKEYREF,
            ATTRIBUTE_NAME_DATAKEYREF
    };

    private void parseKeyrefAttr(final Attributes atts) {
        for (final String attr : KEYREF_ATTRS) {
            if (atts.getValue(attr) != null) {
                hasKeyRef = true;
                break;
            }
        }
    }

    private void parseConactionAttr(final Attributes atts) {
        final String conaction = atts.getValue(ATTRIBUTE_NAME_CONACTION);
        if (conaction != null) {
            if (conaction.equals(ATTR_CONACTION_VALUE_MARK) || conaction.equals(ATTR_CONACTION_VALUE_PUSHREPLACE)) {
                hasconaction = true;
            }
        }
    }

    /**
     * Check if format is DITA topic.
     *
     * @param attrFormat format attribute value, may be {@code null}
     * @return {@code true} if DITA topic, otherwise {@code false}
     */
    public static boolean isFormatDita(final String attrFormat) {
        if (attrFormat == null || attrFormat.equals(ATTR_FORMAT_VALUE_DITA)) {
            return true;
        }
        for (final String f : ditaFormat) {
            if (f.equals(attrFormat)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if path walks up in parent directories
     *
     * @param toCheckPath path to check
     * @return {@code true} if path walks up, otherwise {@code false}
     */
    private boolean isOutFile(final URI toCheckPath) {
        final String path = toCheckPath.getPath();
        return !(path != null && path.startsWith(rootDir.getPath()));
    }

    /**
     * Should links be followed.
     */
    private boolean followLinks() {
        if (!job.crawlTopics() && !isDitaMap()) {
            return false;
        }
        return !job.getOnlyTopicInMap() || isDitaMap();
    }

    private void addToOutFilesSet(final URI hrefedFile) {
        if (followLinks()) {
            outDitaFilesSet.add(hrefedFile);
        }
    }

    private void toOutFile(final URI filename, final Attributes atts) throws SAXException {
        assert filename.isAbsolute();
        final String[] prop = {filename.toString(), currentFile.toString()};
        if (job.getGeneratecopyouter() == Job.Generate.NOT_GENERATEOUTTER) {
            if (isOutFile(filename)) {
                if (job.getOutterControl() == Job.OutterControl.FAIL) {
                    final MessageBean msgBean = MessageUtils.getMessage("DOTJ035F", prop).setLocation(atts);
                    throw new SAXParseException(null, null, new DITAOTException(msgBean, null, msgBean.toString()));
                } else if (job.getOutterControl() == Job.OutterControl.WARN) {
                    final MessageBean msgBean = MessageUtils.getMessage("DOTJ036W", prop).setLocation(atts);
                    logger.warn(msgBean.toString());
                }
                addToOutFilesSet(filename);
            }
        }
    }

    public void setFormatFilter(Predicate<String> formatFilter) {
        this.formatFilter = formatFilter;
    }

    /**
     * File reference with path and optional format.
     */
    public static class Reference {
        /**
         * Absolute URI reference
         */
        public final URI filename;
        /**
         * Format of the reference
         */
        public final String format;

        public Reference(final URI filename, final String format) {
            assert filename.isAbsolute() && filename.getFragment() == null;
            this.filename = filename;
            this.format = format;
        }

        public Reference(final URI filename) {
            this(filename, null);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((filename == null) ? 0 : filename.hashCode());
//            result = prime * result + ((format == null) ? 0 : format.hashCode());
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof Reference)) {
                return false;
            }
            final Reference other = (Reference) obj;
            if (filename == null) {
                if (other.filename != null) {
                    return false;
                }
            } else if (!filename.equals(other.filename)) {
                return false;
            }
//            if (format == null) {
//                if (other.format != null) {
//                    return false;
//                }
//            } else if (!format.equals(other.format)) {
//                return false;
//            }
            return true;
        }
    }

}