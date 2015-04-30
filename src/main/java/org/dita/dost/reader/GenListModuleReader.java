/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.reader;

import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.URLUtils.*;
import static org.dita.dost.util.FileUtils.*;
import static org.dita.dost.util.StringUtils.*;
import static org.dita.dost.reader.ChunkMapReader.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.MessageBean;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.util.DitaClass;
import org.dita.dost.util.Job;
import org.dita.dost.writer.AbstractXMLFilter;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * This class extends AbstractReader, used to parse relevant dita topics and
 * ditamap files for GenMapAndTopicListModule.
 * 
 * <p>
 * <strong>Not thread-safe</strong>. Instances can be reused by calling
 * {@link #reset()} between calls to parse.
 * </p>
 */
public final class GenListModuleReader extends AbstractXMLFilter {

    public static final URI ROOT_URI = toURI("ROOT");

    /** Output utilities */
    private Job job;
    /** Absolute basedir of the current parsing file */
    private URI currentDir = null;
    /** Flag for conref in parsing file */
    private boolean hasConRef = false;
    /** Flag for href in parsing file */
    private boolean hasHref = false;
    /** Flag for keyref in parsing file */
    private boolean hasKeyRef = false;
    /** Flag for whether parsing file contains coderef */
    private boolean hasCodeRef = false;
    /** Set of all the non-conref and non-copyto targets refered in current parsing file */
    private final Set<Reference> nonConrefCopytoTargets;
    /** Set of conref targets refered in current parsing file */
    private final Set<URI> conrefTargets;
    /** Set of href nonConrefCopytoTargets refered in current parsing file */
    private final Set<URI> hrefTargets;
    /** Set of href targets with anchor appended */
    private final Set<URI> hrefTopicSet;
    /** Set of chunk targets */
    private final Set<URI> chunkTopicSet;
    /** Set of subject schema files */
    private final Set<URI> schemeSet;
    /** Set of coderef or object target files */
    private final Set<URI> subsidiarySet;
    /** Set of sources of those copy-to that were ignored */
    private final Set<URI> ignoredCopytoSourceSet;
    /** Map of copy-to target to souce */
    private final Map<URI, URI> copytoMap;
    /** Flag for conrefpush */
    private boolean hasconaction = false;
    /** foreign/unknown nesting level */
    private int foreignLevel = 0;
    /** chunk nesting level */
    private int chunkLevel = 0;
    /** mark topics in reltables */
    private int relTableLevel = 0;
    /** chunk to-navigation level */
    private int chunkToNavLevel = 0;
    /** Topic group nesting level */
    private int topicGroupLevel = 0;
    /** Flag used to mark if current file is still valid after filtering */
    private boolean isValidInput = false;
    /** Contains the attribution specialization from props */
    private String[][] props;
    /** Set of outer dita files */
    private final Set<URI> outDitaFilesSet;
    /** Absolute system path to input file parent directory */
    private URI rootDir = null;
    /** Absolute system path to file being processed */
    private URI currentFile = null;
    /** Stack for @processing-role value */
    private final Stack<String> processRoleStack;
    /** Depth inside a @processing-role parent */
    private int processRoleLevel;
    /** Topics with processing role of "resource-only" */
    private final Set<URI> resourceOnlySet;
    /** Topics with processing role of "normal" */
    private final Set<URI> crossSet;
    /** Subject scheme relative file paths. */
    private final Set<URI> schemeRefSet;
    /** Relationship graph between subject schema. Keys are subject scheme map paths and values
     * are subject scheme map paths, both relative to base directory. A key {@link #ROOT_URI} contains all subject scheme maps. */
    private final Map<URI, Set<URI>> schemeRelationGraph;
    /** Map to store referenced branches. */
    private final Map<URI, List<String>> validBranches;
    /** Int to mark referenced nested elements. */
    private int level;
    /** Topicref stack */
    private final Stack<String> topicrefStack;
    /** Store the primary ditamap file name. */
    private URI primaryDitamap;
    private boolean isRootElement = true;
    private DitaClass rootClass = null;

    /**
     * Constructor.
     */
    public GenListModuleReader() {
        nonConrefCopytoTargets = new LinkedHashSet<Reference>(64);
        hrefTargets = new HashSet<URI>(32);
        hrefTopicSet = new HashSet<URI>(32);
        chunkTopicSet = new HashSet<URI>(32);
        schemeSet = new HashSet<URI>(32);
        schemeRefSet = new HashSet<URI>(32);
        conrefTargets = new HashSet<URI>(32);
        copytoMap = new HashMap<URI, URI>(16);
        subsidiarySet = new HashSet<URI>(16);
        ignoredCopytoSourceSet = new HashSet<URI>(16);
        outDitaFilesSet = new HashSet<URI>(64);
        processRoleLevel = 0;
        processRoleStack = new Stack<String>();
        resourceOnlySet = new HashSet<URI>(32);
        crossSet = new HashSet<URI>(32);
        schemeRelationGraph = new LinkedHashMap<URI, Set<URI>>();
        validBranches = new HashMap<URI, List<String>>(32);
        level = 0;
        topicrefStack = new Stack<String>();
        props = null;
    }

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
    public Set<URI> getOutFilesSet() {
        return outDitaFilesSet;
    }

    /**
     * @return the hrefTopicSet
     */
    public Set<URI> getHrefTopicSet() {
        return hrefTopicSet;
    }

    /**
     * @return the chunkTopicSet
     */
    public Set<URI> getChunkTopicSet() {
        return chunkTopicSet;
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
     * List of files with "@processing-role=resource-only".
     * 
     * @return the resource-only set
     */
    public Set<URI> getResourceOnlySet() {
        final Set<URI> res = new HashSet<URI>(resourceOnlySet);
        res.removeAll(crossSet);
        return res;
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
        this.primaryDitamap = primaryDitamap;
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
     * Get all targets except copy-to.
     * 
     * @return set of target file path with option format after
     *         {@link org.dita.dost.util.Constants#STICK STICK}
     */
    public Set<Reference> getNonCopytoResult() {
        final Set<Reference> nonCopytoSet = new LinkedHashSet<Reference>(128);

        nonCopytoSet.addAll(nonConrefCopytoTargets);
        for (final URI f : conrefTargets) {
            nonCopytoSet.add(new Reference(stripFragment(f)));
        }
        for (final URI f : copytoMap.values()) {
            nonCopytoSet.add(new Reference(stripFragment(f)));
        }
        for (final URI f : ignoredCopytoSourceSet) {
            nonCopytoSet.add(new Reference(stripFragment(f)));
        }
        for (final URI filename : subsidiarySet) {
            // only activated on /generateout:3 & is out file.
            if (isOutFile(filename) && job.getGeneratecopyouter() == Job.Generate.OLDSOLUTION) {
                nonCopytoSet.add(new Reference(stripFragment(filename)));
            }
        }
        // nonCopytoSet.addAll(subsidiarySet);
        return nonCopytoSet;
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
     * Get subsidiary targets.
     * 
     * @return Returns the subsidiarySet.
     */
    public Set<URI> getSubsidiaryTargets() {
        return subsidiarySet;
    }

    /**
     * Get outditafileslist.
     * 
     * @return Returns the outditafileslist.
     */
    public Set<URI> getOutDitaFilesSet() {
        return outDitaFilesSet;
    }

    /**
     * Get non-conref and non-copyto targets.
     * 
     * @return Returns the nonConrefCopytoTargets.
     */
    public Set<URI> getNonConrefCopytoTargets() {
        final Set<URI> res = new HashSet<URI>(nonConrefCopytoTargets.size());
        for (final Reference r : nonConrefCopytoTargets) {
            res.add(r.filename);
        }
        return res;
    }

    /**
     * Returns the ignoredCopytoSourceSet.
     * 
     * @return Returns the ignoredCopytoSourceSet.
     */
    public Set<URI> getIgnoredCopytoSourceSet() {
        return ignoredCopytoSourceSet;
    }

    /**
     * Get the copy-to map.
     * 
     * @return copy-to map
     */
    public Map<URI, URI> getCopytoMap() {
        return copytoMap;
    }

    /**
     * Set processing input directory absolute path.
     * 
     * @param inputDir absolute path to base directory
     */
    public void setInputDir(final URI inputDir) {
        this.rootDir = inputDir;
    }

    /**
     * Set current file absolute path
     * 
     * @param currentFile absolute path to current file
     */
    public void setCurrentFile(final URI currentFile) {
        assert currentFile.isAbsolute();
        this.currentFile = currentFile;
        this.currentDir = currentFile.resolve(".");
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

    /**
     * 
     * Reset the internal variables.
     */
    public void reset() {
        hasKeyRef = false;
        hasConRef = false;
        hasHref = false;
        hasCodeRef = false;
        currentDir = null;
        foreignLevel = 0;
        chunkLevel = 0;
        relTableLevel = 0;
        chunkToNavLevel = 0;
        topicGroupLevel = 0;
        isValidInput = false;
        hasconaction = false;
        nonConrefCopytoTargets.clear();
        hrefTargets.clear();
        hrefTopicSet.clear();
        chunkTopicSet.clear();
        conrefTargets.clear();
        copytoMap.clear();
        ignoredCopytoSourceSet.clear();
        outDitaFilesSet.clear();
        schemeSet.clear();
        schemeRefSet.clear();
        level = 0;
        topicrefStack.clear();
        processRoleLevel = 0;
        processRoleStack.clear();
        isRootElement = true;
        rootClass = null;
        // Don't clean resourceOnlySet or crossSet
    }

    @Override
    public void startDocument() throws SAXException {
        if (currentDir == null) {
            throw new IllegalStateException();
        }
        getContentHandler().startDocument();
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes atts)
            throws SAXException {
        handleRootElement(localName, atts);

        final String processingRole = atts.getValue(ATTRIBUTE_NAME_PROCESSING_ROLE);
        final URI href = toURI(atts.getValue(ATTRIBUTE_NAME_HREF));
        final String scope = atts.getValue(ATTRIBUTE_NAME_SCOPE);
        if (processingRole != null) {
            processRoleStack.push(processingRole);
            processRoleLevel++;
            if (ATTR_SCOPE_VALUE_EXTERNAL.equals(scope)) {
            } else if (ATTR_PROCESSING_ROLE_VALUE_RESOURCE_ONLY.equals(processingRole)) {
                if (href != null) {
                    resourceOnlySet.add(currentDir.resolve(href));
                }
            } else if (ATTR_PROCESSING_ROLE_VALUE_NORMAL.equals(processingRole)) {
                if (href != null) {
                    crossSet.add(currentDir.resolve(href));
                }
            }
        } else if (processRoleLevel > 0) {
            processRoleLevel++;
            if (ATTR_SCOPE_VALUE_EXTERNAL.equals(scope)) {
            } else if (ATTR_PROCESSING_ROLE_VALUE_RESOURCE_ONLY.equals(processRoleStack.peek())) {
                if (href != null) {
                    resourceOnlySet.add(currentDir.resolve(href));
                }
            } else if (ATTR_PROCESSING_ROLE_VALUE_NORMAL.equals(processRoleStack.peek())) {
                if (href != null) {
                    crossSet.add(currentDir.resolve(href));
                }
            }
        } else {
            if (href != null) {
                crossSet.add(currentDir.resolve(href));
            }
        }

        final String classValue = atts.getValue(ATTRIBUTE_NAME_CLASS);

        // Generate Scheme relationship graph
        if (SUBJECTSCHEME_SUBJECTSCHEME.matches(classValue)) {
            // Make it easy to do the BFS later.
            final URI key = ROOT_URI;
            final Set<URI> children = schemeRelationGraph.containsKey(key) ? schemeRelationGraph.get(key) : new LinkedHashSet<URI>();
            children.add(currentFile);
            schemeRelationGraph.put(key, children);

            schemeRefSet.add(currentFile);
        } else if (SUBJECTSCHEME_SCHEMEREF.matches(classValue)) {
            if (href != null) {
                final URI key = currentFile;
                final Set<URI> children = schemeRelationGraph.containsKey(key) ? schemeRelationGraph.get(key) : new LinkedHashSet<URI>();
                final URI child = currentFile.resolve(href);
                children.add(child);
                schemeRelationGraph.put(key, children);
            }
        }

        if (foreignLevel > 0) {
            // if it is an element nested in foreign/unknown element
            // do not parse it
            foreignLevel++;
            return;
        } else if (classValue != null && (TOPIC_FOREIGN.matches(classValue) || TOPIC_UNKNOWN.matches(classValue))) {
            foreignLevel++;
        }

        if (chunkLevel > 0) {
            chunkLevel++;
        } else if (atts.getValue(ATTRIBUTE_NAME_CHUNK) != null) {
            chunkLevel++;
        }
        if (relTableLevel > 0) {
            relTableLevel++;
        } else if (classValue != null && MAP_RELTABLE.matches(classValue)) {
            relTableLevel++;
        }

        if (chunkToNavLevel > 0) {
            chunkToNavLevel++;
        } else if (atts.getValue(ATTRIBUTE_NAME_CHUNK) != null
                && atts.getValue(ATTRIBUTE_NAME_CHUNK).contains(CHUNK_TO_NAVIGATION)) {
            chunkToNavLevel++;
        }

        if (topicGroupLevel > 0) {
            topicGroupLevel++;
        } else if (atts.getValue(ATTRIBUTE_NAME_CLASS) != null
                && atts.getValue(ATTRIBUTE_NAME_CLASS).contains(MAPGROUP_D_TOPICGROUP.matcher)) {
            topicGroupLevel++;
        }

        if (classValue == null && !ELEMENT_NAME_DITA.equals(localName)) {
            logger.info(MessageUtils.getInstance().getMessage("DOTJ030I", localName).toString());
        }

        if (classValue != null && TOPIC_TOPIC.matches(classValue)) {
            final String domains = atts.getValue(ATTRIBUTE_NAME_DOMAINS);
            if (domains == null) {
                logger.info(MessageUtils.getInstance().getMessage("DOTJ029I", localName).toString());
            } else {
                props = getExtProps(domains);
            }
        }

        /*
         * For ditamap, set it to valid if element <map> or extended from <map>
         * was found, this kind of element's class attribute must contains
         * 'map/map'; For topic files, set it to valid if element <title> or
         * extended from <title> was found, this kind of element's class
         * attribute must contains 'topic/title'.
         */
        if ((MAP_MAP.matches(classValue)) || (TOPIC_TITLE.matches(classValue))) {
            isValidInput = true;
        }

        handleTopicRef(localName, atts);

        parseConrefAttr(atts);
        parseAttribute(atts, ATTRIBUTE_NAME_HREF);
        parseAttribute(atts, ATTRIBUTE_NAME_COPY_TO);
        parseAttribute(atts, ATTRIBUTE_NAME_DATA);
        parseConactionAttr(atts);
        parseConkeyrefAttr(atts);
        parseKeyrefAttr(atts);

        getContentHandler().startElement(uri, localName, qName, atts);
    }

    private void handleRootElement(final String localName, final Attributes atts) {
        if (isRootElement) {
            isRootElement = false;
            final String classValue = atts.getValue(ATTRIBUTE_NAME_CLASS);
            if (classValue != null) {
                rootClass = new DitaClass(atts.getValue(ATTRIBUTE_NAME_CLASS));
            }

            final URI href = currentFile;
            if (isDitaMap() && resourceOnlySet.contains(href) && !crossSet.contains(href)) {
                processRoleLevel++;
                processRoleStack.push(ATTR_PROCESSING_ROLE_VALUE_RESOURCE_ONLY);
            }
        }
    }

    private void handleTopicRef(String localName, Attributes atts) {
        final String classValue = atts.getValue(ATTRIBUTE_NAME_CLASS);
        // onlyTopicInMap is on.
        if (job.getOnlyTopicInMap() && isDitaMap()) {
            // topicref(only defined in ditamap file.)
            if (MAP_TOPICREF.matches(classValue)) {
                URI hrefValue = toURI(atts.getValue(ATTRIBUTE_NAME_HREF));
                if (hrefValue == null || hrefValue.toString().isEmpty()) {
                    hrefValue = toURI(atts.getValue(ATTRIBUTE_NAME_CONREF));
                }
                if (hrefValue != null && !hrefValue.toString().isEmpty()) {
                    // exclude external resources
                    final String attrScope = atts.getValue(ATTRIBUTE_NAME_SCOPE);
                    if (ATTR_SCOPE_VALUE_EXTERNAL.equals(attrScope) || ATTR_SCOPE_VALUE_PEER.equals(attrScope)
                            || hrefValue.toString().contains(COLON_DOUBLE_SLASH) || hrefValue.toString().startsWith(SHARP)) {
                        return;
                    }
                    // normalize href value.
                    URI fileName = hrefValue;
                    if (!fileName.isAbsolute()) {
                        fileName = currentDir.resolve(fileName);
                    }

                    final boolean canParse = parseBranch(atts, hrefValue, fileName);
                    if (canParse) {
                        topicrefStack.push(localName);
                    }
                }
            }
        }
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName) throws SAXException {
        // @processing-role
        if (processRoleLevel > 0) {
            if (processRoleLevel == processRoleStack.size()) {
                processRoleStack.pop();
            }
            processRoleLevel--;
        }
        if (foreignLevel > 0) {
            foreignLevel--;
            return;
        }
        if (chunkLevel > 0) {
            chunkLevel--;
        }
        if (relTableLevel > 0) {
            relTableLevel--;
        }
        if (chunkToNavLevel > 0) {
            chunkToNavLevel--;
        }
        if (topicGroupLevel > 0) {
            topicGroupLevel--;
        }

        if (!topicrefStack.isEmpty() && localName.equals(topicrefStack.peek())) {
            level--;
            topicrefStack.pop();
        }
        
        getContentHandler().endElement(uri, localName, qName);
    }

    /**
     * Clean up.
     */
    @Override
    public void endDocument() throws SAXException {
        if (processRoleLevel > 0) {
            processRoleLevel--;
            processRoleStack.pop();
        }
        
        getContentHandler().endDocument();
    }

    /**
     * Method for see whether a branch should be parsed.
     * 
     * @param atts {@link Attributes}
     * @param hrefValue {@link String}
     * @param fileName normalized file name(remove '#')
     * @return boolean
     */
    private boolean parseBranch(final Attributes atts, final URI hrefValue, final URI fileName) {
        // current file is primary ditamap file.
        // parse every branch.
        if (currentDir == null && currentFile.equals(primaryDitamap)) {
            // add branches into map
            addReferredBranches(hrefValue, fileName);
            return true;
        } else {
            // current file is a sub-ditamap one.
            // get branch's id
            final String id = atts.getValue(ATTRIBUTE_NAME_ID);
            // this branch is not referenced
            if (level == 0 && isEmptyString(id)) {
                // There is occassion that the whole ditamap should be parsed
                final boolean found = searchBrachesMap(id);
                if (found) {
                    // Add this branch into map for parsing.
                    addReferredBranches(hrefValue, fileName);
                    // update level
                    level++;
                    return true;
                } else {
                    return false;
                }
                // this brach is a decendent of a referenced one
            } else if (level != 0) {
                // Add this branch into map for parsing.
                addReferredBranches(hrefValue, fileName);
                // update level
                level++;
                return true;
                // This branch has an id but is a new one
            } else if (!isEmptyString(id)) {
                // search branches map.
                final boolean found = searchBrachesMap(id);
                // branch is referenced
                if (found) {
                    // Add this branch into map for parsing.
                    addReferredBranches(hrefValue, fileName);
                    // update level
                    level++;
                    return true;
                } else {
                    // this branch is not referenced
                    return false;
                }
            } else {
                return false;
            }
        }
    }

    /**
     * Search braches map with branch id and current file name.
     * 
     * @param id String branch id.
     * @return boolean true if found and false otherwise.
     */
    private boolean searchBrachesMap(final String id) {
        // caculate relative path for current file.
        final URI currentFileAbsolute = currentFile;
        // seach the map with id & current file name.
        if (validBranches.containsKey(currentFileAbsolute)) {
            final List<String> branchIdList = validBranches.get(currentFileAbsolute);
            // the branch is referenced.
            if (branchIdList.contains(id)) {
                return true;
            } else {// the whole map is referenced
                // the branch is not referred
                return branchIdList.size() == 0;
            }
        } else {
            // current file is not refered
            return false;
        }
    }

    /**
     * Add branches into map.
     * 
     * @param hrefValue
     * @param fileName
     */
    private void addReferredBranches(final URI hrefValue, final URI fileName) {
        final String branchId = hrefValue.getFragment();
        // href value has branch id.
        if (branchId != null) {
            // The map contains the file name
            if (validBranches.containsKey(fileName)) {
                final List<String> branchIdList = validBranches.get(fileName);
                branchIdList.add(branchId);
            } else {
                final List<String> branchIdList = new ArrayList<String>();
                branchIdList.add(branchId);
                validBranches.put(fileName, branchIdList);
            }
            // href value has no branch id
        } else {
            validBranches.put(fileName, new ArrayList<String>());
        }
    }
    
    /**
     * Parse the input attributes for needed information.
     * 
     * @param atts all attributes
     * @param attrName attributes to process
     */
    private void parseAttribute(final Attributes atts, final String attrName) throws SAXException {
        String attrValue = atts.getValue(attrName);
        if (attrValue == null) {
            return;
        }
        final String attrClass = atts.getValue(ATTRIBUTE_NAME_CLASS);
        final String attrScope = atts.getValue(ATTRIBUTE_NAME_SCOPE);
        String attrFormat = atts.getValue(ATTRIBUTE_NAME_FORMAT);
        final String attrType = atts.getValue(ATTRIBUTE_NAME_TYPE);
        final URI codebase = toURI(atts.getValue(ATTRIBUTE_NAME_CODEBASE));

        if (ATTRIBUTE_NAME_HREF.equals(attrName)) {
            if (PR_D_CODEREF.matches(attrClass)) {
                // if current element is <coderef> or its specialization
                // set hasCodeRef to true
                hasCodeRef = true;
            } else {
                hasHref = true;
            }
        }

        // external resource is filtered here.
        if (ATTR_SCOPE_VALUE_EXTERNAL.equals(attrScope) || ATTR_SCOPE_VALUE_PEER.equals(attrScope)
                || attrValue.contains(COLON_DOUBLE_SLASH) || attrValue.startsWith(SHARP)) {
            return;
        }

        final URI target = toURI(attrValue);
        URI filename;
        if (target.isAbsolute() && !ATTRIBUTE_NAME_DATA.equals(attrName)) {
            filename = target;
        } else if (ATTRIBUTE_NAME_DATA.equals(attrName)) {
            if (codebase != null) {
                if (codebase.isAbsolute()) {
                    filename = codebase.resolve(attrValue);
                } else {
                    filename = currentDir.resolve(codebase).resolve(attrValue);
                }
            } else {
                filename = currentDir.resolve(attrValue);
            }
        } else {
            filename = currentDir.resolve(attrValue);
        }
        filename = stripFragment(filename);
        assert filename.isAbsolute();

        if (MAP_TOPICREF.matches(attrClass)) {
            if (ATTR_TYPE_VALUE_SUBJECT_SCHEME.equalsIgnoreCase(attrType)) {
                schemeSet.add(filename);
            }
        } else if (TOPIC_IMAGE.matches(attrClass)) {
            attrFormat = ATTR_FORMAT_VALUE_IMAGE;
        } else if (TOPIC_OBJECT.matches(attrClass)) {
            attrFormat = ATTR_FORMAT_VALUE_HTML;
        }
        // files referred by coderef won't effect the uplevels, code has already returned.
        if (PR_D_CODEREF.matches(attrClass)) {
            subsidiarySet.add(filename);
            return;
        }

        // Collect non-conref and non-copyto targets
        if ((ATTRIBUTE_NAME_HREF.equals(attrName) || ATTRIBUTE_NAME_DATA.equals(attrName))
                && (atts.getValue(ATTRIBUTE_NAME_COPY_TO) == null
                    || (atts.getValue(ATTRIBUTE_NAME_CHUNK) != null && atts.getValue(ATTRIBUTE_NAME_CHUNK).contains(CHUNK_TO_CONTENT)))
                && (followLinks()
                    || (TOPIC_IMAGE.matches(attrClass) || TOPIC_OBJECT.matches(attrClass)))) {
            nonConrefCopytoTargets.add(new Reference(filename, attrFormat));
        }

        if (isFormatDita(attrFormat)) {
            if (ATTRIBUTE_NAME_HREF.equals(attrName) && followLinks()) {
                hrefTargets.add(filename);
                toOutFile(filename);
                if (chunkLevel > 0 && chunkToNavLevel == 0 && topicGroupLevel == 0 && relTableLevel == 0) {
                    chunkTopicSet.add(filename);
                } else {
                    hrefTopicSet.add(filename);
                }
            }
            if (ATTRIBUTE_NAME_COPY_TO.equals(attrName)) {
                final URI href = toURI(atts.getValue(ATTRIBUTE_NAME_HREF));
                if (href != null) {
                    if (href.toString().isEmpty()) {
                        logger.warn("Copy-to task [href=\"\" copy-to=\"" + filename + "\"] was ignored.");
                    } else {
                        final URI value = stripFragment(currentDir.resolve(href));
                        if (copytoMap.get(filename) != null) {
                            if (!value.equals(copytoMap.get(filename))) {
                                logger.warn(MessageUtils.getInstance().getMessage("DOTX065W", href.toString(), filename.toString()).toString());
                            }
                            ignoredCopytoSourceSet.add(value);
                        } else if (!(atts.getValue(ATTRIBUTE_NAME_CHUNK) != null && atts.getValue(ATTRIBUTE_NAME_CHUNK).contains(
                                CHUNK_TO_CONTENT))) {
                            copytoMap.put(filename, value);
                        }
                    }
                }

                final URI pathWithoutID = stripFragment(currentDir.resolve(attrValue));
                if (chunkLevel > 0 && chunkToNavLevel == 0 && topicGroupLevel == 0) {
                    chunkTopicSet.add(pathWithoutID);
                } else {
                    hrefTopicSet.add(pathWithoutID);
                }
            }
        }
    }

    private void parseConrefAttr(final Attributes atts) throws SAXException {
        String attrValue = atts.getValue(ATTRIBUTE_NAME_CONREF);
        if (attrValue != null) {
            hasConRef = true;

            URI filename;
            final URI target = toURI(attrValue);
            if (isAbsolute(target)) {
                filename = target;
            } else if (attrValue.startsWith(SHARP)) {
                filename = currentFile;
            } else {
                filename = currentDir.resolve(attrValue);
            }
            filename = stripFragment(filename);

            // Collect only conref target topic files
            conrefTargets.add(filename);
            toOutFile(filename);
        }
    }

    private void parseConkeyrefAttr(final Attributes atts) {
        final String conkeyref = atts.getValue(ATTRIBUTE_NAME_CONKEYREF);
        if (conkeyref != null) {
            hasConRef = true;
        }
    }

    private void parseKeyrefAttr(final Attributes atts) {
        final String keyref = atts.getValue(ATTRIBUTE_NAME_KEYREF);
        if (keyref != null) {
            hasKeyRef = true;
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

    private boolean isFormatDita(final String attrFormat) {
        return attrFormat == null || attrFormat.equals(ATTR_FORMAT_VALUE_DITA);
    }

    /**
     * Check if path walks up in parent directories
     * 
     * @param toCheckPath path to check
     * @return {@code true} if path walks up, otherwise {@code false}
     */
    private boolean isOutFile(final URI toCheckPath) {
        return !toCheckPath.getPath().startsWith(rootDir.getPath());
    }

    /**
     * Should links be followed.
     */
    private boolean followLinks() {
        return !job.getOnlyTopicInMap() || isDitaMap();
    }

    private void addToOutFilesSet(final URI hrefedFile) {
        if (followLinks()) {
            outDitaFilesSet.add(hrefedFile);
        }
    }

    private void toOutFile(final URI filename) throws SAXException {
        assert filename.isAbsolute();
        // the filename is a relative path from the dita input file
        final String[] prop = { currentFile.toString() };
        if (job.getGeneratecopyouter() == Job.Generate.NOT_GENERATEOUTTER) {
            if (isOutFile(filename)) {
                if (job.getOutterControl() == Job.OutterControl.FAIL) {
                    final MessageBean msgBean = MessageUtils.getInstance().getMessage("DOTJ035F", prop);
                    throw new SAXParseException(null, null, new DITAOTException(msgBean, null, msgBean.toString()));
                } else if (job.getOutterControl() == Job.OutterControl.WARN) {
                    final String message = MessageUtils.getInstance().getMessage("DOTJ036W", prop).toString();
                    logger.warn(message);
                }
                addToOutFilesSet(filename);
            }
        }
    }

    /**
     * File reference with path and optional format.
     */
    public static class Reference {
        /** Absolute URI reference */
        public final URI filename;
        /** Format of the reference */
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
            result = prime * result + ((format == null) ? 0 : format.hashCode());
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
            if (format == null) {
                if (other.format != null) {
                    return false;
                }
            } else if (!format.equals(other.format)) {
                return false;
            }
            return true;
        }
    }

}