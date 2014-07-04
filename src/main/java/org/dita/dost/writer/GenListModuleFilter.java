/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */
package org.dita.dost.writer;

import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.URLUtils.*;
import static org.dita.dost.util.FileUtils.*;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.util.DitaClass;
import org.dita.dost.util.FileUtils;
import org.dita.dost.util.Job;
import org.dita.dost.util.Job.FileInfo;
import org.dita.dost.util.Job.FileInfo.Builder;
import org.dita.dost.util.KeyDef;
import org.dita.dost.util.StringUtils;
import org.dita.dost.util.XMLUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * This class extends AbstractReader, used to parse relevant dita topics and
 * ditamap files for GenMapAndTopicListModule.
 * 
 * <p>
 * <strong>Not thread-safe</strong>. Instances can be reused by calling
 * {@link #reset()} between calls to parse.
 * </p>
 */
public final class GenListModuleFilter extends AbstractXMLFilter {
    
    public static final String PI_PATH2PROJ_TARGET = "path2project";
    public static final String PI_PATH2PROJ_TARGET_URI = "path2project-uri";
    public static final String PI_WORKDIR_TARGET_URI = "workdir-uri";
    
    /** Inherited attributes and their default values. */
    private static final Map<String, String> inheritedAtts = new HashMap<String, String>();
    static {
        inheritedAtts.put(ATTRIBUTE_NAME_SCOPE, ATTR_SCOPE_VALUE_LOCAL);
        inheritedAtts.put(ATTRIBUTE_NAME_PROCESSING_ROLE, ATTR_PROCESSING_ROLE_VALUE_NORMAL);
    }
    
    /** Output utilities */
    private Job job;
    /** Basedir of the current parsing file */
    private URI currentDir = null;
    /** Set of all the non-conref and non-copyto targets refered in current parsing file */
    private final Set<Reference> nonConrefCopytoTargets;
    /** Set of sources of those copy-to that were ignored */
    private final Set<File> ignoredCopytoSourceSet;
    /** Map of copy-to target to souce */
    private final Map<File, File> copytoMap;
    /** chunk nesting level */
    private int chunkLevel = 0;
    /** mark topics in reltables */
    private int relTableLevel = 0;
    /** chunk to-navigation level */
    private int chunkToNavLevel = 0;
    /** Topic group nesting level */
    private int topicGroupLevel = 0;
    /** Flag used to mark if current file is still valid after filtering */
    /** Set of outer dita files */
    private final Set<File> outDitaFilesSet;
    /** Absolute system path to file being processed */
    private URI currentFile = null;
    /** System path to file being processed, relative to base directory. */
    private URI currentFileRelative;
    /** Stack of inherited attributes. */
    private final Deque<AttributesImpl> inheritedAttsStack;
    /** Topics with processing role of "resource-only" */
    private final Set<File> resourceOnlySet;
    /** Topics with processing role of "normal" */
    private final Set<File> normalProcessingSet;
    /** Map to store referenced branches. */
    private final Map<URI, List<String>> validBranches;
    /** Int to mark referenced nested elements. */
    private int level;
    /** Topicref stack */
    private final Stack<String> topicrefStack;
    private File path2Project;
    /** Absolute system path to input file parent directory */
    private URI inputDir;
    private URI inputFile;
    private File tempDir;
    private final Map<String, Integer> counterMap;
    /** File info map. */
    private final Map<String, FileInfo.Builder> fileInfoMap;
    /** File info for the current document. */
    private FileInfo.Builder fileInfo;
    private boolean isRootElement = true;
    private DitaClass rootClass = null;
    private boolean isStartDocument = false;

    /**
     * Constructor.
     */
    public GenListModuleFilter() {
        nonConrefCopytoTargets = new HashSet<Reference>(64);
        copytoMap = new HashMap<File, File>(16);
        ignoredCopytoSourceSet = new HashSet<File>(16);
        outDitaFilesSet = new HashSet<File>(64);
        inheritedAttsStack = new ArrayDeque<AttributesImpl>();
        resourceOnlySet = new HashSet<File>(32);
        normalProcessingSet = new HashSet<File>(32);
        validBranches = new HashMap<URI, List<String>>(32);
        counterMap = new HashMap<String, Integer>();
        level = 0;
        topicrefStack = new Stack<String>();
        fileInfoMap = new HashMap<String, FileInfo.Builder>();
    }
    
    /**
     * 
     * Reset the internal variables.
     */
    public void reset() {
        currentDir = null;
        chunkLevel = 0;
        relTableLevel = 0;
        chunkToNavLevel = 0;
        topicGroupLevel = 0;
        nonConrefCopytoTargets.clear();
        copytoMap.clear();
        ignoredCopytoSourceSet.clear();
        outDitaFilesSet.clear();
        level = 0;
        topicrefStack.clear();
        inheritedAttsStack.clear();
        currentFileRelative = null;
        path2Project = null;
        counterMap.clear();
        fileInfo = null;
        fileInfoMap.clear();
        isRootElement = true;
        rootClass = null;
        isStartDocument = false;
        // Don't clean:
        // resourceOnlySet
        // normalProcessingSet
    }

    /**
     * Current document is processing start document.
     */
    public void isStartDocument(final boolean isStartDocument) {
        this.isStartDocument = isStartDocument;
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
     * List of files with "@processing-role=resource-only".
     * 
     * @return the resource-only set
     */
    public Set<File> getResourceOnlySet() {
        final Set<File> res = new HashSet<File>(resourceOnlySet);
        res.removeAll(normalProcessingSet);
        return res;
    }

    /**
     * Get all targets except copy-to.
     * 
     * @return set of target file path references
     */
    public Set<Reference> getNonCopytoResult() {
        final Set<Reference> nonCopytoSet = new HashSet<Reference>(128);

        nonCopytoSet.addAll(nonConrefCopytoTargets);
        for (final Builder b: fileInfoMap.values()) {
            final FileInfo f = b.build();
            if (f.isConrefTarget) {
                nonCopytoSet.add(new Reference(f.file.getPath(), f.format));
            }
        }
        for (final File f : copytoMap.values()) {
            nonCopytoSet.add(new Reference(f.getPath(), fileInfoMap.get(f.getPath()).build().format));
        }
        for (final File f : ignoredCopytoSourceSet) {
            nonCopytoSet.add(new Reference(f.getPath(), fileInfoMap.get(f.getPath()).build().format));
        }
        return nonCopytoSet;
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
     * Set the relative directory of current file.
     * 
     * @param dir dir
     */
    public void setCurrentDir(final URI dir) {
        currentDir = dir;
    }
    
    /**
     * Set processing input directory absolute path.
     * 
     * @param inputDir absolute path to base directory
     */
    public void setInputDir(final URI inputDir) {
        this.inputDir = inputDir;
    }

    
    /**
     * Set processing input file absolute path.
     * 
     * @param inputFile absolute path to root file
     */
    public void setInputFile(final URI inputFile) {
        this.inputFile = inputFile;
    }
    
    /**
     * Set current file absolute path
     * 
     * @param currentFile absolute path to current file
     */
    public void setCurrentFile(final URI currentFile) {
        this.currentFile = currentFile;
    }
    
    /**
     * Set temporary directory
     * @param tempDir absolute path to temporary directory
     */
    public void setTempDir(final File tempDir) {
        this.tempDir = tempDir;
    }

    /**
     * Get file info for the current file.
     */
    public List<FileInfo> getFileInfo() {
        final List<FileInfo> res = new ArrayList<FileInfo>();
        //res.add(fileInfo.build());
        for (final Builder b: fileInfoMap.values()) {
            res.add(b.build());
        }
        return res;
    }
    
    // Content handler methods
    
    /**
     * Check if the current file is a ditamap with
     * "@processing-role=resource-only".
     */
    @Override
    public void startDocument() throws SAXException {
        currentFileRelative = inputDir.relativize(currentFile);
        path2Project = getPathtoProject(toFile(currentFileRelative), toFile(currentFile.toString()), job.getInputFile().getAbsolutePath());
        fileInfo = getOrCreateBuilder(currentFileRelative);
        
        super.startDocument();
        outputProcessingInstructions();
    }

    /**
     * Generate processing instructions to the beginning of the document.
     * 
     * <p>The following processing instructions are added before the root element:</p>
     * <dl>
     *   <dt>{@link #PI_WORKDIR_TARGET_URI}<dt>
     *   <dd>Absolute URI of the file parent directory.</dd>
     *   <dt>{@link #PI_PATH2PROJ_TARGET}<dt>
     *   <dd>Relative system path to the output directory, with a trailing directory separator.
     *     When the source file is in the project root directory, processing instruction has no value.</dd>
     *   <dt>{@link #PI_PATH2PROJ_TARGET_URI}<dt>
     *   <dd>Relative URI to the output directory, with a trailing path separator.
     *     When the source file is in the project root directory, processing instruction has value {@code ./}.</dd>
     * </dl> 
     */
    private void outputProcessingInstructions() throws SAXException {
        final URI workDir = tempDir.toURI().resolve(currentFileRelative).resolve(".");
        getContentHandler().ignorableWhitespace(new char[] { '\n' }, 0, 1);
        getContentHandler().processingInstruction(PI_WORKDIR_TARGET_URI, workDir.toString());
        getContentHandler().ignorableWhitespace(new char[] { '\n' }, 0, 1);
        if (path2Project != null) {
            getContentHandler().processingInstruction(PI_PATH2PROJ_TARGET, path2Project.getPath() + File.separator);
            getContentHandler().processingInstruction(PI_PATH2PROJ_TARGET_URI, toURI(path2Project).toString() + URI_SEPARATOR);
        } else {
            getContentHandler().processingInstruction(PI_PATH2PROJ_TARGET, "");
            getContentHandler().processingInstruction(PI_PATH2PROJ_TARGET_URI, "." + URI_SEPARATOR);
        }
        getContentHandler().ignorableWhitespace(new char[] { '\n' }, 0, 1);
    }

    /**
     * Push inherited attributes to the stack.
     */
    private void pushInheritedAttributes(final Attributes atts) {
        final AttributesImpl res = new AttributesImpl();
        for (final Entry<String, String> e: inheritedAtts.entrySet()) {
            final String current = atts.getValue(e.getKey());
            if (current != null) {
                XMLUtils.addOrSetAttribute(res, e.getKey(), current);
            } else if (!inheritedAttsStack.isEmpty()) {
                XMLUtils.addOrSetAttribute(res, e.getKey(), getInherited(e.getKey()));
            } else {
                XMLUtils.addOrSetAttribute(res, e.getKey(), e.getValue());
            }
        }
        inheritedAttsStack.addFirst(res);
    }
    /**
     * Get inherited attribute value.
     * @param name attribute name
     * @return attribute value or {@code null} if not available
     */
    private String getInherited(final String name) {
        return inheritedAttsStack.peekFirst().getValue(name);
    }
    
    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes atts)
            throws SAXException {
        pushInheritedAttributes(atts);
        
        handleProcessingRole(atts);
        
        final String classValue = atts.getValue(ATTRIBUTE_NAME_CLASS);

        handleRootElement(localName, atts);

        if (chunkLevel > 0 || atts.getValue(ATTRIBUTE_NAME_CHUNK) != null) {
            chunkLevel++;
        }
        if (relTableLevel > 0 || MAP_RELTABLE.matches(classValue)) {
            relTableLevel++;
        }

        if (chunkToNavLevel > 0) {
            chunkToNavLevel++;
        } else if (atts.getValue(ATTRIBUTE_NAME_CHUNK) != null
                && atts.getValue(ATTRIBUTE_NAME_CHUNK).contains("to-navigation")) {
            chunkToNavLevel++;
        }

        if (topicGroupLevel > 0 || MAPGROUP_D_TOPICGROUP.matches(classValue)) {
            topicGroupLevel++;
        }

        handleTopicRef(localName, atts);

        try {
            parseLinkAttribute(atts, ATTRIBUTE_NAME_CONREF, currentDir);
            handleConrefAttr(atts, currentDir);
            parseLinkAttribute(atts, ATTRIBUTE_NAME_HREF, currentDir);
            handleHrefAttr(atts, currentDir);
            parseLinkAttribute(atts, ATTRIBUTE_NAME_DATA, atts.getValue(ATTRIBUTE_NAME_CODEBASE) != null ? new URI(atts.getValue(ATTRIBUTE_NAME_CODEBASE)) : currentDir);
            parseLinkAttribute(atts, ATTRIBUTE_NAME_COPY_TO, currentDir);
            handleCopyToAttr(atts, currentDir);
        } catch (final URISyntaxException e) {
            logger.error("Failed to parse URI: " + e.getMessage(), e);
        } catch (DITAOTException e) {
            logger.error("Failed to process link: " + e.getMessage(), e);
        }
        handleConactionAttr(atts);
        handleKeyrefAttr(atts);
        
        super.startElement(uri, localName, qName, atts);
    }

    private void handleRootElement(final String localName, final Attributes atts) {
        if (isRootElement) {
            isRootElement = false;
            final String classValue = atts.getValue(ATTRIBUTE_NAME_CLASS);
            if (classValue != null) {
                rootClass = new DitaClass(classValue);
            }
            if (TOPIC_TOPIC.matches(rootClass)) {
                fileInfo.format(ATTR_FORMAT_VALUE_DITA);
            } else if (ELEMENT_NAME_DITA.equals(localName) && classValue == null) {
                fileInfo.format(ATTR_FORMAT_VALUE_DITA);
            } else if (MAP_MAP.matches(rootClass)) {
                fileInfo.format(ATTR_FORMAT_VALUE_DITAMAP);
            }
            if (SUBJECTSCHEME_SUBJECTSCHEME.matches(rootClass)) {
                fileInfo.isSubjectScheme(true);
            }
        }
    }

    private void handleTopicRef(final String localName, final Attributes atts) {
        final String classValue = atts.getValue(ATTRIBUTE_NAME_CLASS);
        if (MAP_TOPICREF.matches(classValue) && job.getOnlyTopicInMap() && isStartDocument) {
            URI hrefValue = toURI(atts.getValue(ATTRIBUTE_NAME_HREF));
            if (hrefValue == null) {
                hrefValue = toURI(atts.getValue(ATTRIBUTE_NAME_CONREF));
            }
            if (hrefValue != null) {
                if (isExternal(hrefValue, getInherited(ATTRIBUTE_NAME_SCOPE))) {
                    return;
                }
                // normalize href value.
                URI target = hrefValue;
                if (target.isAbsolute()) {
                    target = getRelativePath(inputFile, hrefValue);
                }
                // caculate relative path for href value.
                final URI fileName = toURI(resolve(toFile(currentDir), toFile(hrefValue)));

                final boolean canParse = parseBranch(atts, hrefValue, fileName);
                if (canParse) {
                    topicrefStack.push(localName);
                }
            }
        }
    }

    private void handleProcessingRole(final Attributes atts) {
        final String href = atts.getValue(ATTRIBUTE_NAME_HREF);
        final String scope = inheritedAttsStack.peekFirst().getValue(ATTRIBUTE_NAME_SCOPE);
        if (href != null && !ATTR_SCOPE_VALUE_EXTERNAL.equals(scope)) {
            final String processingRole = getInherited(ATTRIBUTE_NAME_PROCESSING_ROLE);
            final File target = FileUtils.resolve(currentDir.toString(), href);
            if (ATTR_PROCESSING_ROLE_VALUE_RESOURCE_ONLY.equals(processingRole)) {
                resourceOnlySet.add(target);
            } else if (ATTR_PROCESSING_ROLE_VALUE_NORMAL.equals(processingRole)) {
                normalProcessingSet.add(target);
            }
        }
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName) throws SAXException {
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

        inheritedAttsStack.removeFirst();
        super.endElement(uri, localName, qName);
    }

    /**
     * Clean up.
     */
    @Override
    public void endDocument() throws SAXException {
        super.endDocument();
    }
    
    /**
     * Get file info builder from file info map or create new file info into the map. 
     * 
     * @param file file info path
     * @return existing or new file info
     */
    private Builder getOrCreateBuilder(final String file) {
        final String f = file.isEmpty() ? currentFileRelative.toString() : file;
        FileInfo.Builder b = fileInfoMap.get(f);
        if (b == null) {
            b = new FileInfo.Builder().file(new File(f));
            fileInfoMap.put(f, b);
        }
        return b;
    }
    private Builder getOrCreateBuilder(final URI file) {
        return getOrCreateBuilder(file.toString());
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
        if (currentDir == null && isStartDocument) {
            // add branches into map
            addReferredBranches(hrefValue, fileName);
            return true;
        } else {
            // current file is a sub-ditamap one.
            // get branch's id
            final String id = atts.getValue(ATTRIBUTE_NAME_ID);
            // this branch is not referenced
            if (level == 0 && StringUtils.isEmptyString(id)) {
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
            } else if (!StringUtils.isEmptyString(id)) {
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
        // seach the map with id & current file name.
        if (validBranches.containsKey(currentFileRelative.getPath())) {
            final List<String> branchIdList = validBranches.get(currentFileRelative.getPath());
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
    private void parseLinkAttribute(final Attributes atts, final String attrName, final URI baseDir) {
        URI attValue = toURI(atts.getValue(attrName));
        if (attValue == null) {
            return;
        }
        if (isExternal(attValue, getInherited(ATTRIBUTE_NAME_SCOPE))) {
            return;
        }

        final File file = FileUtils.resolve(baseDir.toString(), attValue.getPath());

        // Collect non-conref and non-copyto targets
        if (file != null
                && (atts.getValue(ATTRIBUTE_NAME_COPY_TO) == null
                        || (atts.getValue(ATTRIBUTE_NAME_CHUNK) != null
                            && atts.getValue(ATTRIBUTE_NAME_CHUNK).contains("to-content")))
                && !ATTRIBUTE_NAME_CONREF.equals(attrName)
                && !ATTRIBUTE_NAME_COPY_TO.equals(attrName)
                && (canResolved() || FileUtils.isSupportedImageFile(file.getPath().toLowerCase()))) {
            String format = atts.getValue(ATTRIBUTE_NAME_FORMAT);
            if (format == null) {
                if (TOPIC_IMAGE.matches(atts.getValue(ATTRIBUTE_NAME_CLASS))) {
                    format = "image";
                } else {
                    format = ATTR_FORMAT_VALUE_DITA;
                }
            }
            nonConrefCopytoTargets.add(new Reference(file.getPath(), format));
        }
    }
    
    private void handleHrefAttr(final Attributes atts, final URI baseDir) throws URISyntaxException, DITAOTException {
        final URI href = toURI(atts.getValue(ATTRIBUTE_NAME_HREF));
        if (href != null) {
            final File file = FileUtils.resolve(baseDir.toString(), href.getPath());
            if (PR_D_CODEREF.matches(atts)) {
                fileInfo.hasCoderef(true);
                if (isExternal(href, getInherited(ATTRIBUTE_NAME_SCOPE))) {
                    return;
                }
                getOrCreateBuilder(file.getPath()).isSubtarget(true).format("code");
            } else if (TOPIC_IMAGE.matches(atts)) {
                // noop
            } else {
                if (!MAP_TOPICREF.matches(atts)) {
                    fileInfo.hasLink(true);
                }
                if (isExternal(href, getInherited(ATTRIBUTE_NAME_SCOPE))) {
                    return;
                }
                final String attrFormat = atts.getValue(ATTRIBUTE_NAME_FORMAT);
                if (attrFormat == null || ATTR_FORMAT_VALUE_DITA.equals(attrFormat)) {
                    // Collect only href target topic files for index extracting
                    if (canResolved()) {
                        final Builder b = getOrCreateBuilder(file.getPath()).format(ATTR_FORMAT_VALUE_DITA);
                        // Do not read format 
                        b.isTarget(true);
//                        toOutFile(file.getPath());
                        if (chunkLevel > 0 && chunkToNavLevel == 0 && topicGroupLevel == 0 && relTableLevel == 0) {
                            b.isSkipChunk(true);
                        } else {
                            b.isNonConrefTarget(true);
                        }
                    }
                }
            }
        }
    }

    // TODO: replace with reading from inheritedAttributes
    private boolean isExternal(final URI href, final String scope) {
        return ATTR_SCOPE_VALUE_EXTERNAL.equals(scope)
                || ATTR_SCOPE_VALUE_PEER.equals(scope)
                || href.toString().contains(COLON_DOUBLE_SLASH)
                || href.toString().startsWith(SHARP);
    }
    
    private void handleCopyToAttr(final Attributes atts, final URI baseDir) throws URISyntaxException, DITAOTException {
        final URI copyTo = toURI(atts.getValue(ATTRIBUTE_NAME_COPY_TO));
        if (copyTo != null && !isExternal(copyTo, getInherited(ATTRIBUTE_NAME_SCOPE))) {
            final String attrFormat = atts.getValue(ATTRIBUTE_NAME_FORMAT);
            if (attrFormat == null || ATTR_FORMAT_VALUE_DITA.equals(attrFormat)) {
                final File file = FileUtils.resolve(baseDir.toString(), copyTo.getPath());
                final URI href = toURI(atts.getValue(ATTRIBUTE_NAME_HREF));
                final File value = FileUtils.resolve(toFile(currentDir), toFile(href));
    
                if (copytoMap.containsKey(file)) {
                    if (!value.equals(copytoMap.get(file))) {
                        logger.warn(MessageUtils.getInstance().getMessage("DOTX065W", href.getPath(), file.getPath()).toString());
                    }
                    ignoredCopytoSourceSet.add(toFile(href));
                } else if (!(atts.getValue(ATTRIBUTE_NAME_CHUNK) != null && atts.getValue(ATTRIBUTE_NAME_CHUNK).contains("to-content"))) {
                    copytoMap.put(file, value);
                }
    
                final String pathWithoutID = FileUtils.resolve(currentDir.toString(), toFile(copyTo.getPath()).getPath()).getPath();
                final Builder b = getOrCreateBuilder(pathWithoutID);
                if (chunkLevel > 0 && chunkToNavLevel == 0 && topicGroupLevel == 0) {
                    b.isSkipChunk(true);
                } else {
                    b.isNonConrefTarget(true);
                }
            }
        }
    }
    
    private void handleConrefAttr(final Attributes atts, final URI baseDir) throws DITAOTException, URISyntaxException {
        final URI conref = toURI(atts.getValue(ATTRIBUTE_NAME_CONREF));
        final String conkeyref = atts.getValue(ATTRIBUTE_NAME_CONKEYREF);
        if (conref != null || conkeyref != null) {
            fileInfo.hasConref(true);
        }
        if (conref != null) {
            final File file = FileUtils.resolve(baseDir.toString(), conref.getPath());
            getOrCreateBuilder(file.getPath()).isConrefTarget(true);
        }
    }
    
    private void handleKeyrefAttr(final Attributes atts) {
        final String keyref = atts.getValue(ATTRIBUTE_NAME_KEYREF);
        final String conkeyref = atts.getValue(ATTRIBUTE_NAME_CONKEYREF);
        if (keyref != null || conkeyref != null) {
            fileInfo.hasKeyref(true);
        }
    }   
    
    /**
     * Collect the conaction source topic file
     */
    private void handleConactionAttr(final Attributes atts) {
        final String attrValue = atts.getValue(ATTRIBUTE_NAME_CONACTION);
        if (attrValue != null) {
            if (attrValue.equals("mark") || attrValue.equals("pushreplace")) {
                fileInfo.isConrefPush(true);
            }
        } 
    }

    /**
     * Get multi-level keys list
     */
    private List<String> getKeysList(final String key, final Map<String, String> keysRefMap) {
        final List<String> list = new ArrayList<String>();
        // Iterate the map to look for multi-level keys
        for (Entry<String, String> entry : keysRefMap.entrySet()) {
            // Multi-level key found
            if (entry.getValue().equals(key)) {
                // add key into the list
                final String entryKey = entry.getKey();
                list.add(entryKey);
                // still have multi-level keys
                if (keysRefMap.containsValue(entryKey)) {
                    // rescuive point
                    final List<String> tempList = getKeysList(entryKey, keysRefMap);
                    list.addAll(tempList);
                }
            }
        }
        return list;
    }

    /**
     * Update keysDefMap for multi-level keys
     */
    private void checkMultiLevelKeys(final Map<String, KeyDef> keysDefMap, final Map<String, String> keysRefMap) {
        String key = null;
        KeyDef value = null;
        // tempMap storing values to avoid ConcurrentModificationException
        final Map<String, KeyDef> tempMap = new HashMap<String, KeyDef>();
        for (Entry<String, KeyDef> entry : keysDefMap.entrySet()) {
            key = entry.getKey();
            value = entry.getValue();
            // there is multi-level keys exist.
            if (keysRefMap.containsValue(key)) {
                // get multi-level keys
                final List<String> keysList = getKeysList(key, keysRefMap);
                for (final String multikey : keysList) {
                    // update tempMap
                    tempMap.put(multikey, value);
                }
            }
        }
        // update keysDefMap.
        keysDefMap.putAll(tempMap);
    }

    /**
     * Check if current file is a map or if not only topics in main map are processed 
     */
    private boolean canResolved() {
        return !job.getOnlyTopicInMap() || rootClass != null && MAP_MAP.matches(rootClass);
    }

    /**
     * Get path to base directory
     * 
     * @param filename relative input file path from base directory
     * @param traceFilename absolute input file
     * @param inputMap absolute path to start file
     * @return path to base directory, {@code null} if not available
     */
    public File getPathtoProject(final File filename, final File traceFilename, final String inputMap) {
        final File p = FileUtils.getRelativePath(filename);
        return p != null ? p : null;
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