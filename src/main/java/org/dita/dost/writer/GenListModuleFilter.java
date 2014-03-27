/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */
package org.dita.dost.writer;

import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.URLUtils.*;

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
 * {@link #reset()} between calls to {@link #parse(File, File)}.
 * </p>
 */
public final class GenListModuleFilter extends AbstractXMLFilter {
    
    public static final String PI_PATH2PROJ_TARGET = "path2project";
    public static final String PI_PATH2PROJ_TARGET_URI = "path2project-uri";
//    public static final String PI_WORKDIR_TARGET = "workdir";
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
//    private boolean setSystemid = true;
    /** Stack of inherited attributes. */
    private final Deque<AttributesImpl> inheritedAttsStack;
    /** Topics with processing role of "resource-only" */
    private final Set<File> resourceOnlySet;
    /** Topics with processing role of "normal" */
    private final Set<File> normalProcessingSet;
//    private final List<ExportAnchor> resultList = new ArrayList<ExportAnchor>();
//    private ExportAnchor currentExportAnchor;
//    /** Flag to show whether a file has <exportanchors> tag */
//    private boolean hasExport = false;
//    /** For topic/dita files whether a </file> tag should be added */
//    private boolean shouldAppendEndTag = false;
//    /** Store the href of topicref tag */
//    private String topicHref = "";
//    /** Topicmeta set for merge multiple exportanchors into one. Each topicmeta/prolog can define many exportanchors */
//    private final Set<String> topicMetaSet;
//    /** Refered topic id */
//    private String topicId = "";
//    /** Map to store plugin id */
//    private final Map<String, Set<String>> pluginMap = new HashMap<String, Set<String>>();
//    /** Transtype */
//    private String transtype;
    /** Map to store referenced branches. */
    private final Map<String, List<String>> validBranches;
    /** Int to mark referenced nested elements. */
    private int level;
    /** Topicref stack */
    private final Stack<String> topicrefStack;
    private String path2Project;
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
//        processRoleLevel = 0;
//        processRoleStack = new Stack<String>();
        inheritedAttsStack = new ArrayDeque<AttributesImpl>();
        resourceOnlySet = new HashSet<File>(32);
        normalProcessingSet = new HashSet<File>(32);
//        topicMetaSet = new HashSet<String>(INT_16);
        validBranches = new HashMap<String, List<String>>(32);
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
//        processRoleLevel = 0;
//        processRoleStack.clear();
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

//    /**
//     * Set transtype.
//     * 
//     * @param transtype the transtype to set
//     */
//    public void setTranstype(final String transtype) {
//        this.transtype = transtype;
//    }
//
//    /**
//     * @return the pluginMap
//     */
//    public Map<String, Set<String>> getPluginMap() {
//        return pluginMap;
//    }
//
//    /**
//     * Get export anchors.
//     * 
//     * @return list of export anchors
//     */
//    public List<ExportAnchor> getExportAnchors() {
//        return resultList;
//    }

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
//        for (final String filename : subsidiarySet) {
//            // only activated on /generateout:3 & is out file.
//            if (isOutFile(filename) && OutputUtils.getGeneratecopyouter() == OutputUtils.Generate.OLDSOLUTION) {
//                nonCopytoSet.add(new Reference(filename));
//            }
//        }
        // nonCopytoSet.addAll(subsidiarySet);
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
     * @param inputFile absolute path to base directory
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
     *   <!--dt>{@link #PI_WORKDIR_TARGET}<dt>
     *   <dd>Absolute system path of the file parent directory. On Windows, a {@code /}
     *     is added to beginning of the path.</dd-->
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
//        if (OS_NAME.toLowerCase().indexOf(OS_NAME_WINDOWS) == -1) {
//            getContentHandler().processingInstruction(PI_WORKDIR_TARGET, new File(workDir).getAbsolutePath());
//        } else {
//            getContentHandler().processingInstruction(PI_WORKDIR_TARGET, UNIX_SEPARATOR + new File(workDir).getAbsolutePath());
//        }
        getContentHandler().ignorableWhitespace(new char[] { '\n' }, 0, 1);
        getContentHandler().processingInstruction(PI_WORKDIR_TARGET_URI, workDir.toString());
        getContentHandler().ignorableWhitespace(new char[] { '\n' }, 0, 1);
        if (path2Project != null) {
            getContentHandler().processingInstruction(PI_PATH2PROJ_TARGET, path2Project);
            getContentHandler().processingInstruction(PI_PATH2PROJ_TARGET_URI, toURI(path2Project).toString());
        } else {
            getContentHandler().processingInstruction(PI_PATH2PROJ_TARGET, "");
            getContentHandler().processingInstruction(PI_PATH2PROJ_TARGET_URI, "." + UNIX_SEPARATOR);
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

//        // when meets topic tag
//        if (TOPIC_TOPIC.matches(classValue)) {
//            topicId = atts.getValue(ATTRIBUTE_NAME_ID);
//            // relpace place holder with first topic id
//            // Get relative file name
//            for (final ExportAnchor e : resultList) {
//                if (e.topicids.contains(currentFileRelative + QUESTION)) {
//                    e.topicids.add(topicId);
//                    e.topicids.remove(currentFileRelative + QUESTION);
//                }
//            }
//        }

//        // merge multiple exportanchors into one
//        // Each <topicref> can only have one <topicmeta>.
//        // Each <topic> can only have one <prolog>
//        // and <metadata> can have more than one exportanchors
//        // XXX: This should be moved to a separate filter as it's transtype specific
//        if (INDEX_TYPE_ECLIPSEHELP.equals(transtype)) {
//            if (MAP_MAP.matches(classValue) 
//                    && FileUtils.isDITAMapFile(currentFile.getName()) && inputFile.equals(currentFile)) {
//                String pluginId = atts.getValue(ATTRIBUTE_NAME_ID);
//                if (pluginId == null) {
//                    pluginId = "org.sample.help.doc";
//                }
//                final Set<String> set = StringUtils.restoreSet(pluginId);
//                pluginMap.put("pluginId", set);
//            } else if (MAP_TOPICMETA.matches(classValue) || TOPIC_PROLOG.matches(classValue)) {
//                topicMetaSet.add(qName);
//            } else if (DELAY_D_EXPORTANCHORS.matches(classValue)) {
//                hasExport = true;
//                // If current file is a ditamap file
//                if (FileUtils.isDITAMapFile(currentFile.getName())) {
//                    // if dita file's extension name is ".xml"
//                    String editedHref = "";
//                    if (topicHref.endsWith(FILE_EXTENSION_XML)) {
//                        // change the extension to ".dita" for latter compare
//                        editedHref = topicHref.replace(FILE_EXTENSION_XML, FILE_EXTENSION_DITA);
//                    } else {
//                        editedHref = topicHref;
//                    }
//                    // editedHref = editedHref.replace(File.separator, "/");
//                    currentExportAnchor = new ExportAnchor(editedHref);
//                    // if <exportanchors> is defined in topicmeta(topicref), there is only one topic id
//                    currentExportAnchor.topicids.add(topicId);
//                    // If current file is topic file
//                } else if (FileUtils.isDITATopicFile(currentFile.getName())) {
//                    // if dita file's extension name is ".xml"
//                    if (currentFileRelative.endsWith(FILE_EXTENSION_XML)) {
//                        // change the extension to ".dita" for latter compare
//                        currentFileRelative = currentFileRelative.replace(FILE_EXTENSION_XML, FILE_EXTENSION_DITA);
//                    }
//                    currentFileRelative = FileUtils.separatorsToUnix(currentFileRelative);
//                    currentExportAnchor = new ExportAnchor(currentFileRelative);
//                    // if <exportanchors> is defined in metadata(topic), there can be many topic ids
//                    currentExportAnchor.topicids.add(topicId);
//                    shouldAppendEndTag = true;
//                }
//            } else if (DELAY_D_ANCHORKEY.matches(classValue)) {
//                // create keyref element in the StringBuffer
//                // TODO in topic file is no keys
//                final String keyref = atts.getValue(ATTRIBUTE_NAME_KEYREF);
//                currentExportAnchor.keys.add(keyref);
//            } else if (DELAY_D_ANCHORID.matches(classValue)) {
//                // create keyref element in the StringBuffer
//                final String id = atts.getValue(ATTRIBUTE_NAME_ID);
//                // If current file is a ditamap file
//                // The id can only be element id within a topic
//                if (FileUtils.isDITAMapFile(currentFile.getName())) {
//                    // id shouldn't be same as topic id in the case of duplicate insert
//                    if (!topicId.equals(id)) {
//                        currentExportAnchor.ids.add(id);
//                    }
//                } else if (FileUtils.isDITATopicFile(currentFile.getName())) {
//                    // id shouldn't be same as topic id in the case of duplicate insert
//                    if (!topicId.equals(id)) {
//                        // topic id found
//                        currentExportAnchor.ids.add(id);
//                    }
//                }
//            }
//        }

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
                rootClass = new DitaClass(atts.getValue(ATTRIBUTE_NAME_CLASS));
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
                final File target = toFile(hrefValue);
                // caculate relative path for href value.
                String fileName = null;
                if (target.isAbsolute()) {
                    fileName = FileUtils.getRelativeUnixPath(inputFile.toString(), hrefValue.toString());
                }
                fileName = FileUtils.separatorsToUnix(FileUtils.resolve(currentDir.toString(), hrefValue.toString()).getPath());

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

//    @Override
//    public void ignorableWhitespace(final char[] ch, final int start, final int length) throws SAXException {
//        getContentHandler().characters(ch, start, length);
//    }
    
    @Override
    public void endElement(final String uri, final String localName, final String qName) throws SAXException {
        // processing role
//        if (processRoleLevel > 0) {
//            if (processRoleLevel == processRoleStack.size()) {
//                processRoleStack.pop();
//            }
//            processRoleLevel--;
//        }
        
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
//        // <exportanchors> over should write </file> tag
//
//        if (topicMetaSet.contains(qName) && hasExport) {
//            // If current file is a ditamap file
//            if (FileUtils.isDITAMapFile(currentFile.getName())) {
//                resultList.add(currentExportAnchor);
//                currentExportAnchor = null;
//                // If current file is topic file
//            }
//            hasExport = false;
//            topicMetaSet.clear();
//        }

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
        // processing role
//        if (processRoleLevel > 0) {
//            processRoleLevel--;
//            processRoleStack.pop();
//        }
        
//        if (FileUtils.isDITATopicFile(currentFile.getName()) && shouldAppendEndTag) {
//            resultList.add(currentExportAnchor);
//            currentExportAnchor = null;
//            // should reset
//            shouldAppendEndTag = false;
//        }
        
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
    private boolean parseBranch(final Attributes atts, final URI hrefValue, final String fileName) {
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
    private void addReferredBranches(final URI hrefValue, final String fileName) {
        String branchId = null;
        // href value has branch id.
        if (hrefValue.getFragment() != null) {
            branchId = hrefValue.getFragment();
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

        // Ignore absolute paths for now
//        if (new File(attrValue).isAbsolute() && // FIXME: cannot test for absolute here as the value is not a system path yet
//                !ATTRIBUTE_NAME_DATA.equals(attrName)) {
//            attrValue = FileUtils.getRelativePath(inputFile.getAbsolutePath(), attrValue);
//        // for object tag bug:3052156
//        } else
        final File file = FileUtils.resolve(baseDir.toString(), attValue.getPath());

        final String attrClass = atts.getValue(ATTRIBUTE_NAME_CLASS);
        final String attrFormat = atts.getValue(ATTRIBUTE_NAME_FORMAT);
//        if (MAP_TOPICREF.matches(attrClass)) {            
//            // only transtype = eclipsehelp
//            if (INDEX_TYPE_ECLIPSEHELP.equals(transtype)) {
//                // For only format of the href is dita topic
//                if (attrFormat == null || ATTR_FORMAT_VALUE_DITA.equals(attrFormat)) {
//                    topicHref = FileUtils.separatorsToUnix(file.getPath());
//                    // attrValue has topicId
//                    if (linkUri.getFragment() != null) {
//                        topicId = linkUri.getFragment();
//                    } else {
//                        // get the first topicId(vaild href file)
//                        if (FileUtils.isDITAFile(topicHref)) {
//                            // topicId =
//                            // MergeUtils.getInstance().getFirstTopicId(topicHref,
//                            // (new File(rootFilePath)).getParent(), true);
//                            // to be unique
//                            topicId = topicHref + QUESTION;
//                        }
//                    }
//                } else {
//                    topicHref = "";
//                    topicId = "";
//                }
//            }
//        }

        // Collect non-conref and non-copyto targets
        if (file != null
                && FileUtils.isValidTarget(file.getPath().toLowerCase())
                && (atts.getValue(ATTRIBUTE_NAME_COPY_TO) == null
                        || !FileUtils.isDITATopicFile(atts.getValue(ATTRIBUTE_NAME_COPY_TO).toLowerCase())
                        || (atts.getValue(ATTRIBUTE_NAME_CHUNK) != null
                            && atts.getValue(ATTRIBUTE_NAME_CHUNK).contains("to-content")))
                && !ATTRIBUTE_NAME_CONREF.equals(attrName)
                && !ATTRIBUTE_NAME_COPY_TO.equals(attrName)
                && (canResolved() || FileUtils.isSupportedImageFile(file.getPath().toLowerCase()))) {
            String format = attrFormat;
            if (format == null) {
                if (TOPIC_IMAGE.matches(attrClass)) {
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

//    /**
//     * Convert URI references to file paths.
//     * 
//     * @param filename file reference
//     * @return file path
//     */
//    private File toFile(final String filename) {
//        if (filename == null) {
//            return null;
//        }
//        String f = filename;
//        try {
//            f = URLDecoder.decode(filename, UTF8);
//        } catch (final UnsupportedEncodingException e) {
//            throw new RuntimeException(e);
//        }
//        if (processingMode == Mode.LAX) {
//            f = f.replace(WINDOWS_SEPARATOR, File.separator);
//        }
//        f = f.replace(URI_SEPARATOR, File.separator);
//        return new File(f);
//    }

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

//    /**
//     * Check if path walks up in parent directories
//     * 
//     * @param toCheckPath path to check
//     * @return {@code true} if path walks up, otherwise {@code false}
//     */
//    private boolean isOutFile(final String toCheckPath) {
//        if (!toCheckPath.startsWith("..")) {
//            return false;
//        } else {
//            return true;
//        }
//    }

    /**
     * Check if current file is a map or if not only topics in main map are processed 
     */
    private boolean canResolved() {
        return !job.getOnlyTopicInMap() || rootClass != null && MAP_MAP.matches(rootClass);
    }

//    /**
//     * Add file to out files set
//     * 
//     * @param filename a relative path from the dita input file
//     */
//    private void toOutFile(final String filename) throws DITAOTException {
//        final String[] prop = { FileUtils.normalizeDirectory(inputDir.getAbsolutePath(), filename), FileUtils.normalize(currentFile.getAbsolutePath()) };
//        if ((OutputUtils.getGeneratecopyouter() == OutputUtils.Generate.NOT_GENERATEOUTTER)
//                || (OutputUtils.getGeneratecopyouter() == OutputUtils.Generate.GENERATEOUTTER)) {
//            if (isOutFile(filename)) {
//                if (outputUtils.getOutterControl() == OutputUtils.OutterControl.FAIL) {
//                    final MessageBean msgBean = MessageUtils.getInstance().getMessage("DOTJ035F", prop);
//                    throw new DITAOTException(msgBean, null, msgBean.toString());
//                } else if (outputUtils.getOutterControl() == OutputUtils.OutterControl.WARN) {
//                    final String message = MessageUtils.getInstance().getMessage("DOTJ036W", prop).toString();
//                    logger.logWarn(message);
//                }
//                if (canResolved()) {
//                    outDitaFilesSet.add(filename);
//                }
//            }
//        }
//    }

    /**
     * Get path to base directory
     * 
     * @param filename relative input file path from base directory
     * @param traceFilename absolute input file
     * @param inputMap absolute path to start file
     * @return path to base directory, {@code null} if not available
     */
    public String getPathtoProject(final File filename, final File traceFilename, final String inputMap) {
        String path2Project = null;
//        if (OutputUtils.getGeneratecopyouter() != OutputUtils.Generate.OLDSOLUTION) {
//            if (isOutFile(traceFilename)) {
//                path2Project = getRelativePathFromOut(traceFilename.getAbsolutePath());
//            } else {
//                path2Project = FileUtils.getRelativePath(traceFilename.getAbsolutePath(),inputMap);
//                path2Project = new File(path2Project).getParent();
//                if (path2Project != null && path2Project.length() > 0) {
//                    path2Project = path2Project+File.separator;
//                }
//            }
//        } else {
            final File p = FileUtils.getRelativePath(filename); 
            path2Project = p != null ? p.getPath() : null;
            if (path2Project != null && !path2Project.endsWith(File.separator)) {
                path2Project = path2Project + File.separator;
            }
//        }
         return path2Project;
    }
    
//    /**
//     * Check if path falls outside start document directory
//     * 
//     * @param filePathName path to test
//     * @return {@code true} if outside start directory, otherwise {@code false}
//     */
//    private boolean isOutFile(final File filePathName) {
//        final String relativePath = FileUtils.getRelativePath(outputUtils.getInputMapPathName().getAbsolutePath(), filePathName.getPath());
//        if (relativePath == null || relativePath.length() == 0 || !relativePath.startsWith("..")) {
//            return false;
//        }
//        return true;
//    }
    
//    /**
//     * Just for the overflowing files.
//     * @param overflowingFile overflowingFile
//     * @return relative path to out
//     */
//    public String getRelativePathFromOut(final String overflowingFile) {
//        final File mapPathName = outputUtils.getInputMapPathName();
//        final File currFilePathName = new File(overflowingFile);
//        final String relativePath = FileUtils.getRelativePath( mapPathName.toString(),currFilePathName.toString());
//        final String outputDir = OutputUtils.getOutputDir().getAbsolutePath();
//        final String outputPathName = outputDir + File.separator + "index.html";
//        final String finalOutFilePathName = FileUtils.resolve(outputDir,relativePath);
//        final String finalRelativePathName = FileUtils.getRelativePath(finalOutFilePathName,outputPathName.toString());
//        final String parentDir = new File(finalRelativePathName).getParent();
//        final StringBuffer finalRelativePath = new StringBuffer(parentDir);
//        if (finalRelativePath.length() > 0) {
//            finalRelativePath.append(File.separator);
//        } else {
//            finalRelativePath.append(".").append(File.separator);
//        }
//        return finalRelativePath.toString();
//    }
    
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

//    public static class ExportAnchor {
//        public final String file;
//        public final Set<String> topicids = new HashSet<String>();
//        public final Set<String> keys = new HashSet<String>();
//        public final Set<String> ids = new HashSet<String>();
//
//        ExportAnchor(final String file) {
//            this.file = file;
//        }
//    }

}