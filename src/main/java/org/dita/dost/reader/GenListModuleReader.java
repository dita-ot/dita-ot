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

import java.io.File;
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
    
    /** Output utilities */
    private Job job;
    /** Relative basedir of the current parsing file */
    private File currentDir = null;
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
    private final Set<File> conrefTargets;
    /** Set of href nonConrefCopytoTargets refered in current parsing file */
    private final Set<File> hrefTargets;
    /** Set of href targets with anchor appended */
    private final Set<File> hrefTopicSet;
    /** Set of chunk targets */
    private final Set<File> chunkTopicSet;
    /** Set of subject schema files */
    private final Set<File> schemeSet;
    /** Set of subsidiary files */
    private final Set<File> subsidiarySet;
    /** Set of sources of those copy-to that were ignored */
    private final Set<File> ignoredCopytoSourceSet;
    /** Map of copy-to target to souce */
    private final Map<File, File> copytoMap;
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
    private final Set<File> outDitaFilesSet;
    /** Absolute system path to input file parent directory */
    private File rootDir = null;
    /** Absolute system path to file being processed */
    private File currentFile = null;
    /** Absolute system path to input file */
    private File rootFilePath = null;
    /** Stack for @processing-role value */
    private final Stack<String> processRoleStack;
    /** Depth inside a @processing-role parent */
    private int processRoleLevel;
    /** Topics with processing role of "resource-only" */
    private final Set<File> resourceOnlySet;
    /** Topics with processing role of "normal" */
    private final Set<File> crossSet;
    /** Subject scheme relative file paths. */
    private final Set<File> schemeRefSet;
    /** Relationship graph between subject schema. Keys are subject scheme map paths and values
     * are subject scheme map paths, both relative to base directory. A key {@code File("ROOT")} contains all subject scheme maps. */
    private final Map<File, Set<File>> schemeRelationGraph;
    /** Map to store referenced branches. */
    private final Map<URI, List<String>> validBranches;
    /** Int to mark referenced nested elements. */
    private int level;
    /** Topicref stack */
    private final Stack<String> topicrefStack;
    /** Store the primary ditamap file name. */
    private String primaryDitamap = "";
    private boolean isRootElement = true;
    private DitaClass rootClass = null;

    /**
     * Constructor.
     */
    public GenListModuleReader() {
        nonConrefCopytoTargets = new HashSet<Reference>(64);
        hrefTargets = new HashSet<File>(32);
        hrefTopicSet = new HashSet<File>(32);
        chunkTopicSet = new HashSet<File>(32);
        schemeSet = new HashSet<File>(32);
        schemeRefSet = new HashSet<File>(32);
        conrefTargets = new HashSet<File>(32);
        copytoMap = new HashMap<File, File>(16);
        subsidiarySet = new HashSet<File>(16);
        ignoredCopytoSourceSet = new HashSet<File>(16);
        outDitaFilesSet = new HashSet<File>(64);
        processRoleLevel = 0;
        processRoleStack = new Stack<String>();
        resourceOnlySet = new HashSet<File>(32);
        crossSet = new HashSet<File>(32);
        schemeRelationGraph = new LinkedHashMap<File, Set<File>>();
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
    public Set<File> getOutFilesSet() {
        return outDitaFilesSet;
    }

    /**
     * @return the hrefTopicSet
     */
    public Set<File> getHrefTopicSet() {
        return hrefTopicSet;
    }

    /**
     * @return the chunkTopicSet
     */
    public Set<File> getChunkTopicSet() {
        return chunkTopicSet;
    }

    /**
     * Get scheme set.
     * 
     * @return scheme set
     */
    public Set<File> getSchemeSet() {
        return schemeSet;
    }

    /**
     * Get scheme ref set.
     * 
     * @return scheme ref set
     */
    public Set<File> getSchemeRefSet() {
        return schemeRefSet;
    }

    /**
     * List of files with "@processing-role=resource-only".
     * 
     * @return the resource-only set
     */
    public Set<File> getResourceOnlySet() {
        final Set<File> res = new HashSet<File>(resourceOnlySet);
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
     * Is the processed file a DITA map.
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
     * are subject scheme map paths, both relative to base directory. A key {@code "ROOT"} contains all subject scheme maps.
     *
     * @return relationship graph
     */
    public Map<File, Set<File>> getRelationshipGrap() {
        return schemeRelationGraph;
    }

    public void setPrimaryDitamap(final String primaryDitamap) {
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
        final Set<Reference> nonCopytoSet = new HashSet<Reference>(128);

        nonCopytoSet.addAll(nonConrefCopytoTargets);
        for (final File f : conrefTargets) {
            nonCopytoSet.add(new Reference(f.getPath()));
        }
        for (final File f : copytoMap.values()) {
            nonCopytoSet.add(new Reference(f.getPath()));
        }
        for (final File f : ignoredCopytoSourceSet) {
            nonCopytoSet.add(new Reference(f.getPath()));
        }
        for (final File filename : subsidiarySet) {
            // only activated on /generateout:3 & is out file.
            if (isOutFile(filename) && job.getGeneratecopyouter() == Job.Generate.OLDSOLUTION) {
                nonCopytoSet.add(new Reference(filename.getPath()));
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
    public Set<File> getHrefTargets() {
        return hrefTargets;
    }

    /**
     * Get conref targets.
     * 
     * @return Returns the conrefTargets.
     */
    public Set<File> getConrefTargets() {
        return conrefTargets;
    }

    /**
     * Get subsidiary targets.
     * 
     * @return Returns the subsidiarySet.
     */
    public Set<File> getSubsidiaryTargets() {
        return subsidiarySet;
    }

    /**
     * Get outditafileslist.
     * 
     * @return Returns the outditafileslist.
     */
    public Set<File> getOutDitaFilesSet() {
        return outDitaFilesSet;
    }

    /**
     * Get non-conref and non-copyto targets.
     * 
     * @return Returns the nonConrefCopytoTargets.
     */
    public Set<File> getNonConrefCopytoTargets() {
        final Set<File> res = new HashSet<File>(nonConrefCopytoTargets.size());
        for (final Reference r : nonConrefCopytoTargets) {
            res.add(new File(r.filename));
        }
        return res;
    }

    /**
     * Returns the ignoredCopytoSourceSet.
     * 
     * @return Returns the ignoredCopytoSourceSet.
     */
    public Set<File> getIgnoredCopytoSourceSet() {
        return ignoredCopytoSourceSet;
    }

    /**
     * Get the copy-to map.
     * 
     * @return copy-to map
     */
    public Map<File, File> getCopytoMap() {
        return copytoMap;
    }

    /**
     * Set processing input directory absolute path.
     * 
     * @param inputDir absolute path to base directory
     */
    public void setInputDir(final File inputDir) {
        this.rootDir = inputDir;
    }

    
    /**
     * Set processing input file absolute path.
     * 
     * @param inputFile absolute path to root file
     */
    public void setInputFile(final File inputFile) {
        this.rootFilePath = inputFile;
    }
    
    /**
     * Set current file absolute path
     * 
     * @param currentFile absolute path to current file
     */
    public void setCurrentFile(final File currentFile) {
        this.currentFile = currentFile;
    }
    
    /**
     * Set the relative directory of current file.
     * 
     * @param dir dir
     */
    public void setCurrentDir(final File dir) {
        currentDir = dir;
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
        // Don't clean resourceOnlySet por crossSet
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
                    resourceOnlySet.add(resolve(currentDir, toFile(href).getPath()));
                }
            } else if (ATTR_PROCESSING_ROLE_VALUE_NORMAL.equals(processingRole)) {
                if (href != null) {
                    crossSet.add(resolve(currentDir, toFile(href).getPath()));
                }
            }
        } else if (processRoleLevel > 0) {
            processRoleLevel++;
            if (ATTR_SCOPE_VALUE_EXTERNAL.equals(scope)) {
            } else if (ATTR_PROCESSING_ROLE_VALUE_RESOURCE_ONLY.equals(processRoleStack.peek())) {
                if (href != null) {
                    resourceOnlySet.add(resolve(currentDir, toFile(href).getPath()));
                }
            } else if (ATTR_PROCESSING_ROLE_VALUE_NORMAL.equals(processRoleStack.peek())) {
                if (href != null) {
                    crossSet.add(resolve(currentDir, toFile(href).getPath()));
                }
            }
        } else {
            if (href != null) {
                crossSet.add(resolve(currentDir, toFile(href).getPath()));
            }
        }

        final String classValue = atts.getValue(ATTRIBUTE_NAME_CLASS);

        // Generate Scheme relationship graph
        if (SUBJECTSCHEME_SUBJECTSCHEME.matches(classValue)) {
            // Make it easy to do the BFS later.
            final File key = new File("ROOT");
            final Set<File> children = schemeRelationGraph.containsKey(key) ? schemeRelationGraph.get(key) : new LinkedHashSet<File>();
            final File child = getRelativePath(rootFilePath.getAbsoluteFile(), currentFile.getAbsoluteFile());
            children.add(child);
            schemeRelationGraph.put(key, children);

            schemeRefSet.add(getRelativePath(rootFilePath.getAbsoluteFile(), currentFile.getAbsoluteFile()));
        } else if (SUBJECTSCHEME_SCHEMEREF.matches(classValue)) {
            if (href != null) {
                final File key = getRelativePath(rootFilePath.getAbsoluteFile(), currentFile.getAbsoluteFile());
                final Set<File> children = schemeRelationGraph.containsKey(key) ? schemeRelationGraph.get(key) : new LinkedHashSet<File>();
                final File child = getRelativePath(rootFilePath.getAbsoluteFile(), resolve(rootDir.getAbsoluteFile(), toFile(href).getPath()));
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
                && atts.getValue(ATTRIBUTE_NAME_CHUNK).contains("to-navigation")) {
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

        if (classValue != null) {
            if ((MAP_MAP.matches(classValue)) || (TOPIC_TITLE.matches(classValue))) {
                isValidInput = true;
            } else if (TOPIC_OBJECT.matches(classValue)) {
                parseAttribute(atts, ATTRIBUTE_NAME_DATA);
            }
        }

        handleTopicRef(localName, atts);

        parseAttribute(atts, ATTRIBUTE_NAME_CONREF);
        parseAttribute(atts, ATTRIBUTE_NAME_HREF);
        parseAttribute(atts, ATTRIBUTE_NAME_COPY_TO);
        parseAttribute(atts, ATTRIBUTE_NAME_IMG);
        parseAttribute(atts, ATTRIBUTE_NAME_CONACTION);
        parseAttribute(atts, ATTRIBUTE_NAME_CONKEYREF);
        parseAttribute(atts, ATTRIBUTE_NAME_KEYREF);

        getContentHandler().startElement(uri, localName, qName, atts);
    }

    private void handleRootElement(final String localName, final Attributes atts) {
        if (isRootElement) {
            isRootElement = false;
            final String classValue = atts.getValue(ATTRIBUTE_NAME_CLASS);
            if (classValue != null) {
                rootClass = new DitaClass(atts.getValue(ATTRIBUTE_NAME_CLASS));
            }

            final File href = getRelativePath(rootFilePath.getAbsoluteFile(), currentFile.getAbsoluteFile());
            if (isDitaMap() && resourceOnlySet.contains(href) && !crossSet.contains(href)) {
                processRoleLevel++;
                processRoleStack.push(ATTR_PROCESSING_ROLE_VALUE_RESOURCE_ONLY);
            }
        }
    }

    private void handleTopicRef(String localName, Attributes atts) {
        final String classValue = atts.getValue(ATTRIBUTE_NAME_CLASS);
        // onlyTopicInMap is on.
        if (job.getOnlyTopicInMap() && canResolved()) {
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
                    URI target = hrefValue;
                    if (target.isAbsolute()) {
                        target = getRelativePath(rootFilePath.getAbsoluteFile().toURI(), target);
                    }
                    // caculate relative path for href value.
                    final URI fileName = toURI(resolve(currentDir, toFile(target)));

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
        final String currentFileRelative = getRelativeUnixPath(rootFilePath.getAbsolutePath(),
                currentFile.getAbsolutePath());
        if (currentDir == null && currentFileRelative.equals(primaryDitamap)) {
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
        final URI currentFileRelative = toURI(getRelativePath(rootFilePath.getAbsoluteFile(),
                currentFile.getAbsoluteFile()));
        // seach the map with id & current file name.
        if (validBranches.containsKey(currentFileRelative)) {
            final List<String> branchIdList = validBranches.get(currentFileRelative);
            // the branch is referenced.
            if (branchIdList.contains(id)) {

                return true;
            } else // the whole map is referenced
// the branch is not referred
                return branchIdList.size() == 0;
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
        final String attrClass = atts.getValue(ATTRIBUTE_NAME_CLASS);
        final String attrScope = atts.getValue(ATTRIBUTE_NAME_SCOPE);
        String attrFormat = atts.getValue(ATTRIBUTE_NAME_FORMAT);
        final String attrType = atts.getValue(ATTRIBUTE_NAME_TYPE);

        final String codebase = atts.getValue(ATTRIBUTE_NAME_CODEBASE);

        if (attrValue == null) {
            return;
        }

        // @conkeyref will be resolved to @conref in Debug&Fileter step
        if (ATTRIBUTE_NAME_CONREF.equals(attrName) || ATTRIBUTE_NAME_CONKEYREF.equals(attrName)) {
            hasConRef = true;
        } else if (ATTRIBUTE_NAME_HREF.equals(attrName)) {
            if (attrClass != null && PR_D_CODEREF.matches(attrClass)) {
                // if current element is <coderef> or its specialization
                // set hasCodeRef to true
                hasCodeRef = true;
            } else {
                hasHref = true;
            }
        } else if (ATTRIBUTE_NAME_KEYREF.equals(attrName)) {
            hasKeyRef = true;
        }

        // external resource is filtered here.
        if (ATTR_SCOPE_VALUE_EXTERNAL.equals(attrScope) || ATTR_SCOPE_VALUE_PEER.equals(attrScope)
                || attrValue.contains(COLON_DOUBLE_SLASH) || attrValue.startsWith(SHARP)) {
            return;
        }

        final URI target = toURI(attrValue);
        String filename = null;
        if (isAbsolute(target) && !ATTRIBUTE_NAME_DATA.equals(attrName)) {
            filename = getRelativeUnixPath(rootFilePath.getAbsoluteFile().toURI().getPath(), target.getPath());
            // for object tag bug:3052156
        } else if (ATTRIBUTE_NAME_DATA.equals(attrName)) {
            if (!isEmptyString(codebase)) {
                filename = resolve(codebase, attrValue).getPath();
            } else {
                filename = resolve(currentDir, attrValue).getPath();
            }
        } else {
            // noraml process.
            filename = resolve(currentDir, attrValue).getPath();
        }

        filename = toFile(filename).getPath();
        // XXX: At this point, filename should be a system path

        if (MAP_TOPICREF.matches(attrClass)) {
            if (ATTR_TYPE_VALUE_SUBJECT_SCHEME.equalsIgnoreCase(attrType)) {
                schemeSet.add(new File(filename));
            }
        } else if (TOPIC_IMAGE.matches(attrClass)) {
            if (attrFormat == null) {
                attrFormat = "image";
            }
        }
        // files referred by coderef won't effect the uplevels, code has already returned.
        if (("DITA-foreign".equals(attrType) && ATTRIBUTE_NAME_DATA.equals(attrName)) || attrClass != null && PR_D_CODEREF.matches(attrClass)) {
            subsidiarySet.add(new File(filename));
            return;
        }

        // Collect non-conref and non-copyto targets
        if ((ATTRIBUTE_NAME_HREF.equals(attrName) || ATTRIBUTE_NAME_DATA.equals(attrName))
                && (atts.getValue(ATTRIBUTE_NAME_COPY_TO) == null
                    || (atts.getValue(ATTRIBUTE_NAME_CHUNK) != null && atts.getValue(ATTRIBUTE_NAME_CHUNK).contains("to-content")))
                && (canResolved() || isSupportedImageFile(filename.toLowerCase()))) {
            nonConrefCopytoTargets.add(new Reference(filename, attrFormat));
        }
        // outside ditamap files couldn't cause warning messages, it is stopped here
        if (attrFormat != null && !ATTR_FORMAT_VALUE_DITA.equals(attrFormat)) {
            // The format of the href is not dita topic
            // The logic after this "if" clause is not related to files other than dita topic.
            // Therefore, we need to return here to filter out those files in other format.
            return;
        }

        /*
         * Collect only href target topic files for index extracting.
         */
        if (ATTRIBUTE_NAME_HREF.equals(attrName) && isFormatDita(attrFormat) && canResolved()) {
            hrefTargets.add(new File(filename));
            toOutFile(new File(filename));
            if (chunkLevel > 0 && chunkToNavLevel == 0 && topicGroupLevel == 0 && relTableLevel == 0) {
                chunkTopicSet.add(new File(filename));
            } else {
                hrefTopicSet.add(new File(filename));
            }
        }

        // Collect only conref target topic files
        if (ATTRIBUTE_NAME_CONREF.equals(attrName)) {
            conrefTargets.add(new File(filename));
            toOutFile(new File(filename));
        }

        // Collect copy-to (target,source) into hash map
        if (ATTRIBUTE_NAME_COPY_TO.equals(attrName) && isFormatDita(attrFormat)) {
            final URI href = toURI(atts.getValue(ATTRIBUTE_NAME_HREF));
            if (href != null) {
                final File value = resolve(currentDir, toFile(href).getPath());
    
                if (href.toString().isEmpty()) {
                    logger.warn("Copy-to task [href=\"\" copy-to=\"" + filename + "\"] was ignored.");
                } else if (copytoMap.get(new File(filename)) != null) {
                    if (!value.equals(copytoMap.get(new File(filename)))) {
                        logger.warn(MessageUtils.getInstance().getMessage("DOTX065W", href.toString(), filename).toString());
                    }
                    ignoredCopytoSourceSet.add(toFile(href));
                } else if (!(atts.getValue(ATTRIBUTE_NAME_CHUNK) != null && atts.getValue(ATTRIBUTE_NAME_CHUNK).contains(
                        "to-content"))) {
                    copytoMap.put(new File(filename), value);
                }
            }
            
            final File pathWithoutID = resolve(currentDir, toFile(attrValue).getPath());
            if (chunkLevel > 0 && chunkToNavLevel == 0 && topicGroupLevel == 0) {
                chunkTopicSet.add(pathWithoutID);
            } else {
                hrefTopicSet.add(pathWithoutID);
            }

        }
        // Collect the conaction source topic file
        if (ATTRIBUTE_NAME_CONACTION.equals(attrName)) {
            if (attrValue.equals("mark") || attrValue.equals("pushreplace")) {
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
    private boolean isOutFile(final File toCheckPath) {
        return toCheckPath.getPath().startsWith("..");
    }

    /**
     * Check if {@link #currentFile} is a map
     * 
     * @return {@code} true if file is map, otherwise {@code false}
     */
    private boolean isMapFile() {
        return isDitaMap();
    }

    private boolean canResolved() {
        return (!job.getOnlyTopicInMap()) || isMapFile();
    }

    private void addToOutFilesSet(final File hrefedFile) {
        if (canResolved()) {
            outDitaFilesSet.add(hrefedFile);
        }
    }

    private void toOutFile(final File filename) throws SAXException {
        // the filename is a relative path from the dita input file
        final String[] prop = { resolve(rootDir.getAbsolutePath(), filename.getPath()).getPath(), normalize(currentFile.getAbsolutePath()).getPath() };
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
        public final String filename;
        public final String format;

        public Reference(final String filename, final String format) {
            this.filename = filename;
            this.format = format;
        }

        public Reference(final String filename) {
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