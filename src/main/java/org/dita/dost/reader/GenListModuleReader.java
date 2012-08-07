/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.reader;

import static org.dita.dost.util.Constants.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;
import java.util.Map.Entry;

import org.apache.xerces.xni.grammars.XMLGrammarPool;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.exception.DITAOTXMLErrorHandler;
import org.dita.dost.log.MessageBean;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.module.GenMapAndTopicListModule.KeyDef;
import org.dita.dost.reader.GenListModuleReader.Reference;
import org.dita.dost.util.CatalogUtils;
import org.dita.dost.util.DITAAttrUtils;
import org.dita.dost.util.FileUtils;
import org.dita.dost.util.FilterUtils;
import org.dita.dost.util.OutputUtils;
import org.dita.dost.util.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;


/**
 * This class extends AbstractReader, used to parse relevant dita topics
 * and ditamap files for GenMapAndTopicListModule.
 * 
 * <p><strong>Not thread-safe</strong>. Instances can be reused by calling
 * {@link #reset()} between calls to {@link #parse(File)}.</p>
 * 
 * @version 1.0 2004-11-25
 * 
 * @author Wu, Zhi Qiang
 */
public final class GenListModuleReader extends AbstractXMLReader {
    /** XMLReader instance for parsing dita file */
    private XMLReader reader = null;

    /** Filter utils */
    private FilterUtils filterUtils;
    /** Output utilities */
    private OutputUtils outputUtils;
    /** Basedir of the current parsing file */
    private String currentDir = null;

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
    private final Set<String> conrefTargets;

    /** Set of href nonConrefCopytoTargets refered in current parsing file */
    private final Set<String> hrefTargets;

    /** Set of href targets with anchor appended */
    private final Set<String> hrefTopicSet;

    /** Set of chunk targets */
    private final Set<String> chunkTopicSet;

    /** Set of subject schema files */
    private final Set<String> schemeSet;

    /** Set of subsidiary files */
    private final Set<String> subsidiarySet;

    /** Set of sources of those copy-to that were ignored */
    private final Set<String> ignoredCopytoSourceSet;

    /** Map of copy-to target to souce	*/
    private final Map<String, String> copytoMap;

    /** Map of key definitions */
    private final Map<String, KeyDef> keysDefMap;

    //Added on 20100826 for bug:3052913 start
    /** Map to store multi-level keyrefs */
    private final Map<String, String>keysRefMap;
    //Added on 20100826 for bug:3052913 end

    /** Flag for conrefpush   */
    private boolean hasconaction = false;

    /** Flag used to mark if parsing entered into excluded element */
    private boolean insideExcludedElement = false;

    /** Used to record the excluded level */
    private int excludedLevel = 0;

    /** foreign/unknown nesting level */
    private int foreignLevel = 0;

    /** chunk nesting level */
    private int chunkLevel = 0;

    //Added by William on 2010-06-17 for bug:3016739 start
    /** mark topics in reltables */
    private int relTableLevel = 0;
    //Added by William on 2010-06-17 for bug:3016739 end

    /** chunk to-navigation level */
    private int chunkToNavLevel = 0;

    /** Topic group nesting level */
    private int topicGroupLevel = 0;

    /** Flag used to mark if current file is still valid after filtering */
    private boolean isValidInput = false;

    /** Contains the attribution specialization from props */
    private String[][] props;

    /** Set of outer dita files */
    private final Set<String> outDitaFilesSet;
    /** Absolute system path to input file parent directory */
    private File rootDir = null;
    /** Absolute system path to file being processed */
    private File currentFile=null;

    private File rootFilePath=null;

    //Added on 2010-08-24 for bug:3086552 start
    private boolean setSystemid = true;
    //Added on 2010-08-24 for bug:3086552 end

    /** Stack for @processing-role value */
    private final Stack<String> processRoleStack;
    /** Depth inside a @processing-role parent */
    private int processRoleLevel;
    /** Topics with processing role of "resource-only" */
    private final Set<String> resourceOnlySet;
    /** Topics with processing role of "normal" */
    private final Set<String> crossSet;
    private final Set<String> schemeRefSet;

    /** Subject scheme document root */
    //private Document schemeRoot = null;

    /** Current processing node */
    //private Element currentElement = null;

    /** Relationship graph between subject schema */
    private Map<String, Set<String>> relationGraph = null;

    //Added by William on 2009-06-25 for req #12014 start
    /** StringBuffer to store <exportanchors> elements */
    private StringBuffer result = new StringBuffer();
    /** Flag to show whether a file has <exportanchors> tag */
    private boolean hasExport = false;
    /** For topic/dita files whether a </file> tag should be added */
    private boolean shouldAppendEndTag = false;
    /** Store the href of topicref tag */
    private String topicHref = "";
    /** Topicmeta set for merge multiple exportanchors into one.
     * Each topicmeta/prolog can define many exportanchors */
    private final Set<String> topicMetaSet;
    /** Refered topic id */
    private String topicId = "";
    /** Map to store plugin id */
    private final Map<String, Set<String>> pluginMap = new HashMap<String, Set<String>>();
    /** Transtype */
    private String transtype;
    //Added by William on 2010-03-01 for update onlytopicinmap option start
    /** Map to store referenced branches. */
    private final Map<String, List<String>> vaildBranches;
    /** Int to mark referenced nested elements. */
    private int level;
    /** Topicref stack */
    private final Stack<String> topicrefStack;
    /** Store the primary ditamap file name. */
    private String primaryDitamap = "";
    //Added by William on 2010-03-01 for update onlytopicinmap option end.

    //Added by William on 2010-06-01 for bug:3005748 start
    /** Get DITAAttrUtil */
    private final DITAAttrUtils ditaAttrUtils = DITAAttrUtils.getInstance();
    //Added by William on 2010-06-01 for bug:3005748 end

    //Added by William on 2010-06-09 for bug:3013079 start
    /** Store the external/peer keydefs */
    private final Map<String, String> exKeysDefMap;
    //Added by William on 2010-06-09 for bug:3013079 end

    /**
     * Get transtype.
     * @return the transtype
     */
    public String getTranstype() {
        return transtype;
    }

    /**
     * Set transtype.
     * @param transtype the transtype to set
     */
    public void setTranstype(final String transtype) {
        this.transtype = transtype;
    }

    /**
     * @return the pluginMap
     */
    public Map<String, Set<String>> getPluginMap() {
        return pluginMap;
    }

    /**
     * @return the result
     */
    public StringBuffer getResult() {
        return result;
    }
    //Added by William on 2009-06-25 for req #12014 end

    /**
     * Constructor.
     */
    public GenListModuleReader() {
        nonConrefCopytoTargets = new HashSet<Reference>(INT_64);
        hrefTargets = new HashSet<String>(INT_32);
        hrefTopicSet = new HashSet<String>(INT_32);
        chunkTopicSet = new HashSet<String>(INT_32);
        schemeSet = new HashSet<String>(INT_32);
        schemeRefSet = new HashSet<String>(INT_32);
        conrefTargets = new HashSet<String>(INT_32);
        copytoMap = new HashMap<String, String>(INT_16);
        subsidiarySet = new HashSet<String>(INT_16);
        ignoredCopytoSourceSet = new HashSet<String>(INT_16);
        outDitaFilesSet=new HashSet<String>(INT_64);
        keysDefMap = new HashMap<String, KeyDef>();
        keysRefMap = new HashMap<String, String>();

        exKeysDefMap = new HashMap<String, String>();

        processRoleLevel = 0;
        processRoleStack = new Stack<String>();
        resourceOnlySet = new HashSet<String>(INT_32);
        crossSet = new HashSet<String>(INT_32);

        //store the topicmeta element
        topicMetaSet = new HashSet<String>(INT_16);

        vaildBranches = new HashMap<String, List<String>>(INT_32);
        level = 0;
        topicrefStack = new Stack<String>();

        //schemeRoot = null;
        //currentElement = null;

        props = null;
        try {
            reader = StringUtils.getXMLReader();
        } catch (final SAXException e) {
            throw new RuntimeException("Unable to create XML parser: " + e.getMessage(), e);
        }
        reader.setContentHandler(this);
        try {
            reader.setProperty(LEXICAL_HANDLER_PROPERTY,this);
        } catch (final SAXNotRecognizedException e1) {
            logger.logException(e1);
        } catch (final SAXNotSupportedException e1) {
            logger.logException(e1);
        }
    }
    
    /**
     * Set content filter.
     * 
     * @param filterUtils filter utils
     */
    public void setFilterUtils(final FilterUtils filterUtils) {
        this.filterUtils = filterUtils;
    }
    
    /**
     * Set output utilities.
     * @param outputUtils output utils
     */
    public void setOutputUtils(final OutputUtils outputUtils) {
        this.outputUtils = outputUtils;
    }


    /**
     * Init xml reader used for pipeline parsing.
     *
     * @param ditaDir absolute path to DITA-OT directory
     * @param validate whether validate input file
     * @param rootFile absolute path to input file
     * @throws SAXException parsing exception
     * @throws IOException if getting canonical file path fails 
     */
    public void initXMLReader(final File ditaDir,final boolean validate,final File rootFile, final boolean arg_setSystemid) throws SAXException, IOException {
        //final DITAOTJavaLogger javaLogger=new DITAOTJavaLogger();

        //to check whether the current parsing file's href value is out of inputmap.dir
        rootDir=rootFile.getParentFile().getCanonicalFile();
        rootFilePath=rootFile.getCanonicalFile();
        reader.setFeature(FEATURE_NAMESPACE_PREFIX, true);
        if(validate==true){
            reader.setFeature(FEATURE_VALIDATION, true);
            reader.setFeature(FEATURE_VALIDATION_SCHEMA, true);
        }else{
            final String msg=MessageUtils.getMessage("DOTJ037W").toString();
            logger.logWarn(msg);
        }
        final XMLGrammarPool grammarPool = GrammarPoolManager.getGrammarPool();
        setGrammarPool(reader, grammarPool);

        CatalogUtils.setDitaDir(ditaDir);
        //Added on 2010-08-24 for bug:3086552 start
        setSystemid= arg_setSystemid;
        //Added on 2010-08-24 for bug:3086552 end

        reader.setEntityResolver(CatalogUtils.getCatalogResolver());
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
        insideExcludedElement = false;
        excludedLevel = 0;
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
        keysDefMap.clear();
        keysRefMap.clear();
        exKeysDefMap.clear();
        schemeSet.clear();
        schemeRefSet.clear();

        //clear level
        level = 0;
        //clear stack
        topicrefStack.clear();


        //@processing-role
        processRoleLevel = 0;
        processRoleStack.clear();

        //reset utils
        ditaAttrUtils.reset();
        /*
         * Don't clean up these sets, we need them through
         * the whole phase to determine a topic's processing-role.
         */
        //resourceOnlySet.clear();
        //crossSet.clear();
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
    public boolean hasKeyRef(){
        return hasKeyRef;
    }

    /**
     * To see if the parsed file has coderef inside.
     * 
     * @return true if has coderef and false otherwise
     */
    public boolean hasCodeRef(){
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
     * @return set of target file path with option format after {@link org.dita.dost.util.Constants#STICK STICK}
     */
    public Set<Reference> getNonCopytoResult() {
        final Set<Reference> nonCopytoSet = new HashSet<Reference>(INT_128);

        nonCopytoSet.addAll(nonConrefCopytoTargets);
        for (final String f: conrefTargets) {
            nonCopytoSet.add(new Reference(f));
        }
        for (final String f: copytoMap.values()) {
            nonCopytoSet.add(new Reference(f));
        }
        for (final String f: ignoredCopytoSourceSet) {
            nonCopytoSet.add(new Reference(f));
        }
        //Added by William on 2010-03-04 for bug:2957938 start
        for(final String filename : subsidiarySet){
            //only activated on /generateout:3 & is out file.
            if(isOutFile(filename) && OutputUtils.getGeneratecopyouter()
                    == OutputUtils.Generate.OLDSOLUTION){
                nonCopytoSet.add(new Reference(filename));
            }
        }
        //nonCopytoSet.addAll(subsidiarySet);
        //Added by William on 2010-03-04 for bug:2957938 end
        return nonCopytoSet;
    }

    /**
     * Get the href target.
     * 
     * @return Returns the hrefTargets.
     */
    public Set<String> getHrefTargets() {
        return hrefTargets;
    }

    /**
     * Get conref targets.
     * 
     * @return Returns the conrefTargets.
     */
    public Set<String> getConrefTargets() {
        return conrefTargets;
    }

    /**
     * Get subsidiary targets.
     * 
     * @return Returns the subsidiarySet.
     */
    public Set<String> getSubsidiaryTargets() {
        return subsidiarySet;
    }

    /**
     * Get outditafileslist.
     * 
     * @return Returns the outditafileslist.
     */
    public Set<String> getOutDitaFilesSet(){
        return outDitaFilesSet;
    }

    /**
     * Get non-conref and non-copyto targets.
     * 
     * @return Returns the nonConrefCopytoTargets.
     */
    public Set<String> getNonConrefCopytoTargets() {
        final Set<String> res = new HashSet<String>(nonConrefCopytoTargets.size());
        for (final Reference r: nonConrefCopytoTargets) {
            res.add(r.filename);
        }
        return res;
    }

    /**
     * Returns the ignoredCopytoSourceSet.
     *
     * @return Returns the ignoredCopytoSourceSet.
     */
    public Set<String> getIgnoredCopytoSourceSet() {
        return ignoredCopytoSourceSet;
    }

    /**
     * Get the copy-to map.
     * 
     * @return copy-to map
     */
    public Map<String, String> getCopytoMap() {
        return copytoMap;
    }

    /**
     * Get the Key definitions.
     * 
     * @return Key definitions map
     */
    public Map<String, KeyDef> getKeysDMap(){
        return keysDefMap;
    }

    //Added by William on 2010-06-09 for bug:3013079 start
    public Map<String, String> getExKeysDefMap() {
        return exKeysDefMap;
    }
    //Added by William on 2010-06-09 for bug:3013079 end

    /**
     * Set the relative directory of current file.
     * 
     * @param dir dir
     */
    public void setCurrentDir(final String dir) {
        this.currentDir = dir;
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
     * @return true if has conaction and false otherwise
     */
    public boolean hasConaction(){
        return hasconaction;
    }
    /**
     * Parse input xml file.
     * 
     * @param file file
     * @throws SAXException SAXException
     * @throws IOException IOException
     * @throws FileNotFoundException FileNotFoundException
     */
    public void parse(final File file) throws FileNotFoundException, IOException, SAXException {

        currentFile=file.getAbsoluteFile();

        reader.setErrorHandler(new DITAOTXMLErrorHandler(file.getName(), logger));
        //Added on 2010-08-24 for bug:3086552 start
        final InputSource is = new InputSource(new FileInputStream(file));
        //Set the system ID
        if(setSystemid) {
            //is.setSystemId(URLUtil.correct(file).toString());
            is.setSystemId(file.toURI().toURL().toString());
        }
        //Added on 2010-08-24 for bug:3086552 end
        reader.parse(is);
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName,
            final Attributes atts) throws SAXException {
        String domains = null;
        final Properties params = new Properties();

        //Added by William on 2010-06-01 for bug:3005748 start
        final String printValue = atts.getValue(ATTRIBUTE_NAME_PRINT);
        //increase element level for nested tags.
        ditaAttrUtils.increasePrintLevel(printValue);
        //Exclude the topic if it is needed.
        if(ditaAttrUtils.needExcludeForPrintAttri(transtype)){
            return;
        }
        //Added by William on 2010-06-01 for bug:3005748 end

        final String processingRole = atts.getValue(ATTRIBUTE_NAME_PROCESSING_ROLE);
        final String href = atts.getValue(ATTRIBUTE_NAME_HREF);
        final String scope = atts.getValue(ATTRIBUTE_NAME_SCOPE);
        if (processingRole != null) {
            processRoleStack.push(processingRole);
            processRoleLevel++;
            if (ATTR_SCOPE_VALUE_EXTERNAL.equals(scope)) {
            } else if (ATTR_PROCESSING_ROLE_VALUE_RESOURCE_ONLY.equals(processingRole)) {
                if (href != null) {
                    resourceOnlySet.add(FileUtils.resolveFile(currentDir, href));
                }
            } else if (ATTR_PROCESSING_ROLE_VALUE_NORMAL.equalsIgnoreCase(processingRole)) {
                if (href != null) {
                    crossSet.add(FileUtils.resolveFile(currentDir, href));
                }
            }
        } else if (processRoleLevel > 0) {
            processRoleLevel++;
            if (ATTR_SCOPE_VALUE_EXTERNAL.equals(scope)) {
            } else if (ATTR_PROCESSING_ROLE_VALUE_RESOURCE_ONLY.equalsIgnoreCase(
                    processRoleStack.peek())) {
                if (href != null) {
                    resourceOnlySet.add(FileUtils.resolveFile(currentDir, href));
                }
            } else if (ATTR_PROCESSING_ROLE_VALUE_NORMAL.equalsIgnoreCase(
                    processRoleStack.peek())) {
                if (href != null) {
                    crossSet.add(FileUtils.resolveFile(currentDir, href));
                }
            }
        } else  {
            if (href != null) {
                crossSet.add(FileUtils.resolveFile(currentDir, href));
            }
        }

        final String classValue = atts.getValue(ATTRIBUTE_NAME_CLASS);

        //Added by William on 2009-06-24 for req #12014 start
        //has class attribute
        if(classValue!=null){

            //when meets topic tag
            if(TOPIC_TOPIC.matches(classValue)){
                topicId = atts.getValue(ATTRIBUTE_NAME_ID);
                //relpace place holder with first topic id
                //Get relative file name
                final String filename = FileUtils.getRelativePath(
                        rootFilePath.getAbsolutePath(), currentFile.getAbsolutePath());
                if(result.indexOf(filename + QUESTION) != -1){
                    result = new StringBuffer(result.toString().replace(filename + QUESTION, topicId));
                }

            }
            // WEK: As of 14 Dec 2009, transtype is sometimes null, not sure under what conditions.
            //			System.out.println(" + [DEBUG] transtype=" + transtype);
            //get plugin id only transtype = eclipsehelp
            if(FileUtils.isDITAMapFile(currentFile.getName())&&
                    rootFilePath.equals(currentFile)&&
                    MAP_MAP.matches(classValue)&&
                    INDEX_TYPE_ECLIPSEHELP.equals(transtype)){
                String pluginId = atts.getValue(ATTRIBUTE_NAME_ID);
                if(pluginId == null){
                    pluginId = "org.sample.help.doc";
                }
                final Set<String> set = StringUtils.restoreSet(pluginId);
                pluginMap.put("pluginId", set);
            }

            //merge multiple exportanchors into one
            //Each <topicref> can only have one <topicmeta>.
            //Each <topic> can only have one <prolog>
            //and <metadata> can have more than one exportanchors
            if (INDEX_TYPE_ECLIPSEHELP.equals(transtype)) {
                if (MAP_TOPICMETA.matches(classValue)
                        || TOPIC_PROLOG.matches(classValue)) {
                    topicMetaSet.add(qName);
                }
                // If the file has <exportanchors> tags only transtype =
                // eclipsehelp
                if (DELAY_D_EXPORTANCHORS.matches(classValue)) {
                    hasExport = true;
                    // If current file is a ditamap file
                    if (FileUtils.isDITAMapFile(currentFile.getName())) {
                        // if dita file's extension name is ".xml"
                        String editedHref = "";
                        if (topicHref.endsWith(FILE_EXTENSION_XML)) {
                            // change the extension to ".dita" for latter
                            // compare
                            editedHref = topicHref.replace(
                                    FILE_EXTENSION_XML,
                                    FILE_EXTENSION_DITA);
                        } else {
                            editedHref = topicHref;
                        }
                        // editedHref = editedHref.replace(File.separator, "/");
                        // create file element in the StringBuffer
                        result.append("<file name=\"" + editedHref + "\">");
                        // if <exportanchors> is defined in topicmeta(topicref),
                        // there is only one topic id
                        result.append("<topicid name=\"" + topicId + "\"/>");

                        // If current file is topic file
                    } else if (FileUtils.isDITATopicFile(currentFile.getName())) {
                        String filename = FileUtils.getRelativePath(
                                rootFilePath.getAbsolutePath(), currentFile.getAbsolutePath());
                        // if dita file's extension name is ".xml"
                        if (filename.endsWith(FILE_EXTENSION_XML)) {
                            // change the extension to ".dita" for latter
                            // compare
                            filename = filename.replace(
                                    FILE_EXTENSION_XML,
                                    FILE_EXTENSION_DITA);
                        }
                        // filename = FileUtils.normalizeDirectory(currentDir,
                        // filename);
                        filename = FileUtils.separatorsToUnix(filename);
                        // create file element in the StringBuffer
                        result.append("<file name=\"" + filename + "\">");
                        // if <exportanchors> is defined in metadata(topic),
                        // there can be many topic ids
                        result.append("<topicid name=\"" + topicId + "\">");

                        shouldAppendEndTag = true;
                    }
                    // meet <anchorkey> tag
                } else if (DELAY_D_ANCHORKEY.matches(classValue)) {
                    // create keyref element in the StringBuffer
                    // TODO in topic file is no keys
                    final String keyref = atts
                            .getValue(ATTRIBUTE_NAME_KEYREF);
                    result.append("<keyref name=\"" + keyref + "\"/>");
                    // meet <anchorid> tag
                } else if (DELAY_D_ANCHORID.matches(classValue)) {
                    // create keyref element in the StringBuffer
                    final String id = atts.getValue(ATTRIBUTE_NAME_ID);
                    // If current file is a ditamap file
                    // The id can only be element id within a topic
                    if (FileUtils.isDITAMapFile(currentFile.getName())) {
                        // only for dita format
                        /*
                         * if(!"".equals(topicHref)){ String absolutePathToFile
                         * = FileUtils.resolveFile((new
                         * File(rootFilePath)).getParent(),topicHref); //whether
                         * the id is a topic id
                         * if(FileUtils.isDITAFile(absolutePathToFile)){ found =
                         * DelayConrefUtils
                         * .getInstance().findTopicId(absolutePathToFile, id); }
                         * //other format file }else{ found = false; }
                         */
                        // id shouldn't be same as topic id in the case of duplicate insert
                        if (!topicId.equals(id)) {
                            result.append("<id name=\"" + id + "\"/>");
                        }
                    } else if (FileUtils.isDITATopicFile(currentFile.getName())) {
                        // id shouldn't be same as topic id in the case of duplicate insert
                        if (!topicId.equals(id)) {
                            // topic id found
                            result.append("<id name=\"" + id + "\"/>");
                        }
                    }
                }
            }
        }
        //Added by William on 2009-06-24 for req #12014 end

        // Generate Scheme relationship graph
        if (classValue != null) {
            if (SUBJECTSCHEME_SUBJECTSCHEME.matches(classValue)) {
                if (this.relationGraph == null) {
                    this.relationGraph = new LinkedHashMap<String, Set<String>>();
                }
                //Make it easy to do the BFS later.
                Set<String> children = this.relationGraph.get("ROOT");
                if (children == null || children.isEmpty()) {
                    children = new LinkedHashSet<String>();
                }
                children.add(this.currentFile.getAbsolutePath());
                this.relationGraph.put("ROOT", children);
                schemeRefSet.add(FileUtils.getRelativePath(rootFilePath.getAbsolutePath(), currentFile.getAbsolutePath()));
            } else if (SUBJECTSCHEME_SCHEMEREF.matches(classValue)) {
                Set<String> children = this.relationGraph.get(this.currentFile);
                if (children == null) {
                    children = new LinkedHashSet<String>();
                    this.relationGraph.put(currentFile.getAbsolutePath(), children);
                }
                if (href != null) {
                    children.add(FileUtils.resolveFile(rootDir.getAbsolutePath(), href));
                }
            }
        }

        if(foreignLevel > 0){
            //if it is an element nested in foreign/unknown element
            //do not parse it
            foreignLevel ++;
            return;
        } else if(classValue != null &&
                (TOPIC_FOREIGN.matches(classValue) ||
                        TOPIC_UNKNOWN.matches(classValue))){
            foreignLevel ++;
        }

        if(chunkLevel > 0) {
            chunkLevel++;
        } else if(atts.getValue(ATTRIBUTE_NAME_CHUNK) != null) {
            chunkLevel++;
        }
        //Added by William on 2010-6-17 for bug:3016739 start
        if(relTableLevel > 0) {
            relTableLevel ++;
        } else if(classValue != null &&
                MAP_RELTABLE.matches(classValue)){
            relTableLevel++;
        }
        //Added by William on 2010-6-17 for bug:3016739 end


        if(chunkToNavLevel > 0) {
            chunkToNavLevel++;
        } else if(atts.getValue(ATTRIBUTE_NAME_CHUNK) != null
                && atts.getValue(ATTRIBUTE_NAME_CHUNK).indexOf("to-navigation") != -1){
            chunkToNavLevel++;
        }

        if(topicGroupLevel > 0) {
            topicGroupLevel++;
        } else if (atts.getValue(ATTRIBUTE_NAME_CLASS) != null
                && atts.getValue(ATTRIBUTE_NAME_CLASS).contains(MAPGROUP_D_TOPICGROUP.matcher)) {
            topicGroupLevel++;
        }

        if(classValue==null && !ELEMENT_NAME_DITA.equals(localName)){
            params.clear();
            params.put("%1", localName);
            logger.logInfo(MessageUtils.getMessage("DOTJ030I", params).toString());
        }

        if (classValue != null && TOPIC_TOPIC.matches(classValue)){
            domains = atts.getValue(ATTRIBUTE_NAME_DOMAINS);
            if(domains==null){
                params.clear();
                params.put("%1", localName);
                logger.logInfo(MessageUtils.getMessage("DOTJ029I", params).toString());
            } else {
                props = StringUtils.getExtProps(domains);
            }
        }

        if (insideExcludedElement) {
            ++excludedLevel;
            return;
        }

        // Ignore element that has been filtered out.
        if (filterUtils.needExclude(atts, props)) {
            insideExcludedElement = true;
            ++excludedLevel;
            return;
        }

        /*
         * For ditamap, set it to valid if element <map> or extended from
         * <map> was found, this kind of element's class attribute must
         * contains 'map/map';
         * For topic files, set it to valid if element <title> or extended
         * from <title> was found, this kind of element's class attribute
         * must contains 'topic/title'.
         */

        if (classValue != null) {
            if ((MAP_MAP.matches(classValue))
                    || (TOPIC_TITLE.matches(classValue))) {
                isValidInput = true;
            }else if (TOPIC_OBJECT.matches(classValue)){
                parseAttribute(atts, ATTRIBUTE_NAME_DATA);
            }
        }


        //Added by William on 2010-03-02 for /onlytopicinmap update start.
        //onlyTopicInMap is on.
        if(outputUtils.getOnlyTopicInMap() && this.canResolved()){
            //topicref(only defined in ditamap file.)
            if(MAP_TOPICREF.matches(classValue)){

                //get href attribute value.
                final String hrefValue = atts.getValue(ATTRIBUTE_NAME_HREF);

                //get conref attribute value.
                final String conrefValue = atts.getValue(ATTRIBUTE_NAME_CONREF);

                //has href attribute and refer to ditamap file.
                if(!StringUtils.isEmptyString(hrefValue)){
                    //exclude external resources
                    final String attrScope = atts.getValue(ATTRIBUTE_NAME_SCOPE);
                    if ("external".equalsIgnoreCase(attrScope)
                            || "peer".equalsIgnoreCase(attrScope)
                            || hrefValue.indexOf(COLON_DOUBLE_SLASH) != -1
                            || hrefValue.startsWith(SHARP)) {
                        return;
                    }
                    //normalize href value.
                    final File target=new File(hrefValue);
                    //caculate relative path for href value.
                    String fileName = null;
                    if(target.isAbsolute()){
                        fileName = FileUtils.getRelativePath(rootFilePath.getAbsolutePath(),hrefValue);
                    }
                    fileName = FileUtils.normalizeDirectory(currentDir, hrefValue);
                    //change '\' to '/' for comparsion.
                    fileName = FileUtils.separatorsToUnix(fileName);

                    final boolean canParse = parseBranch(atts, hrefValue, fileName);
                    if(!canParse){
                        return;
                    }else{
                        topicrefStack.push(localName);
                    }

                }else if(!StringUtils.isEmptyString(conrefValue)){

                    //exclude external resources
                    final String attrScope = atts.getValue(ATTRIBUTE_NAME_SCOPE);
                    if ("external".equalsIgnoreCase(attrScope)
                            || "peer".equalsIgnoreCase(attrScope)
                            || conrefValue.indexOf(COLON_DOUBLE_SLASH) != -1
                            || conrefValue.startsWith(SHARP)) {
                        return;
                    }
                    //normalize href value.
                    final File target=new File(conrefValue);
                    //caculate relative path for href value.
                    String fileName = null;
                    if(target.isAbsolute()){
                        fileName = FileUtils.getRelativePath(rootFilePath.getAbsolutePath(),conrefValue);
                    }
                    fileName = FileUtils.normalizeDirectory(currentDir, conrefValue);

                    //change '\' to '/' for comparsion.
                    fileName = FileUtils.separatorsToUnix(fileName);

                    final boolean canParse = parseBranch(atts, conrefValue, fileName);
                    if(!canParse){
                        return;
                    }else{
                        topicrefStack.push(localName);
                    }
                }
            }
        }
        //Added by William on 2010-03-02 for /onlytopicinmap update end.

        parseAttribute(atts, ATTRIBUTE_NAME_CONREF);
        parseAttribute(atts, ATTRIBUTE_NAME_HREF);
        parseAttribute(atts, ATTRIBUTE_NAME_COPY_TO);
        parseAttribute(atts, ATTRIBUTE_NAME_IMG);
        parseAttribute(atts, ATTRIBUTE_NAME_CONACTION);
        parseAttribute(atts, ATTRIBUTE_NAME_KEYS);
        parseAttribute(atts, ATTRIBUTE_NAME_CONKEYREF);
        parseAttribute(atts, ATTRIBUTE_NAME_KEYREF);

    }

    /**
     * Method for see whether a branch should be parsed.
     * @param atts {@link Attributes}
     * @param hrefValue {@link String}
     * @param fileName normalized file name(remove '#')
     * @return boolean
     */
    private boolean parseBranch(final Attributes atts, final String hrefValue, final String fileName) {
        //current file is primary ditamap file.
        //parse every branch.
        final String currentFileRelative = FileUtils.getRelativePath(
                rootFilePath.getAbsolutePath(), currentFile.getAbsolutePath());
        if(currentDir == null && currentFileRelative.equals(primaryDitamap)){
            //add branches into map
            addReferredBranches(hrefValue, fileName);
            return true;
        }else{
            //current file is a sub-ditamap one.
            //get branch's id
            final String id = atts.getValue(ATTRIBUTE_NAME_ID);
            //this branch is not referenced
            if(level == 0 && StringUtils.isEmptyString(id)){
                //There is occassion that the whole ditamap should be parsed
                final boolean found = searchBrachesMap(id);
                if(found){
                    //Add this branch into map for parsing.
                    addReferredBranches(hrefValue, fileName);
                    //update level
                    level++;
                    return true;
                }else{
                    return false;
                }
                //this brach is a decendent of a referenced one
            }else if(level != 0){
                //Add this branch into map for parsing.
                addReferredBranches(hrefValue, fileName);
                //update level
                level++;
                return true;
                //This branch has an id but is a new one
            }else if(!StringUtils.isEmptyString(id)){
                //search branches map.
                final boolean found = searchBrachesMap(id);
                //branch is referenced
                if(found){
                    //Add this branch into map for parsing.
                    addReferredBranches(hrefValue, fileName);
                    //update level
                    level ++;
                    return true;
                }else{
                    //this branch is not referenced
                    return false;
                }
            }else{
                return false;
            }
        }
    }

    /**
     * Search braches map with branch id and current file name.
     * @param id String branch id.
     * @return boolean true if found and false otherwise.
     */
    private boolean searchBrachesMap(final String id) {
        //caculate relative path for current file.
        final String currentFileRelative = FileUtils.getRelativePath(
                rootFilePath.getAbsolutePath(), currentFile.getAbsolutePath());
        //seach the map with id & current file name.
        if(vaildBranches.containsKey(currentFileRelative)){
            final List<String> branchIdList = vaildBranches.get(currentFileRelative);
            //the branch is referenced.
            if(branchIdList.contains(id)){

                return true;
            }else if(branchIdList.size() == 0){
                //the whole map is referenced

                return true;
            }else{
                //the branch is not referred
                return false;
            }
        }else{
            //current file is not refered
            return false;
        }
    }

    /**
     * Add branches into map.
     * @param hrefValue
     * @param fileName
     */
    private void addReferredBranches(final String hrefValue, final String fileName) {
        String branchId = null;

        //href value has branch id.
        if(hrefValue.contains(SHARP)){
            branchId = hrefValue.substring(hrefValue.lastIndexOf(SHARP) + 1);
            //The map contains the file name
            if(vaildBranches.containsKey(fileName)){
                final List<String> branchIdList = vaildBranches.get(fileName);
                branchIdList.add(branchId);
            }else{
                final List<String> branchIdList = new ArrayList<String>();
                branchIdList.add(branchId);
                vaildBranches.put(fileName, branchIdList);
            }
            //href value has no branch id
        }else{
            vaildBranches.put(fileName, new ArrayList<String>());
        }
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
        //Added by William on 2009-07-15 for req #12014 start
        if(FileUtils.isDITATopicFile(currentFile.getName()) && shouldAppendEndTag){
            result.append("</file>");
            //should reset
            shouldAppendEndTag = false;
        }
        //Added by William on 2009-07-15 for req #12014 end
        //update keysDefMap for multi-level keys for bug:3052913
        checkMultiLevelKeys(keysDefMap, keysRefMap);
    }

    /**
     * Check if the current file is a ditamap with "@processing-role=resource-only".
     */
    @Override
    public void startDocument() throws SAXException {
        final String href = FileUtils.getRelativePath(rootFilePath.getAbsolutePath(), currentFile.getAbsolutePath());
        if (FileUtils.isDITAMapFile(currentFile.getName())
                && resourceOnlySet.contains(href)
                && !crossSet.contains(href)) {
            processRoleLevel++;
            processRoleStack.push(ATTR_PROCESSING_ROLE_VALUE_RESOURCE_ONLY);
        }
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName) throws SAXException {

        //subject scheme
        //if (currentElement != null && currentElement != schemeRoot.getDocumentElement()) {
        //	currentElement = (Element)currentElement.getParentNode();
        //}

        //@processing-role
        if (processRoleLevel > 0) {
            if (processRoleLevel == processRoleStack.size()) {
                processRoleStack.pop();
            }
            processRoleLevel--;
        }

        if (foreignLevel > 0){
            foreignLevel --;
            return;
        }

        if (chunkLevel > 0) {
            chunkLevel--;
        }

        //Added by William on 2010-06-17 for bug:3016739 start
        if (relTableLevel > 0) {
            relTableLevel--;
        }
        //Added by William on 2010-06-17 for bug:3016739 end

        if (chunkToNavLevel > 0) {
            chunkToNavLevel--;
        }

        if (topicGroupLevel > 0) {
            topicGroupLevel--;
        }

        if (insideExcludedElement) {
            // end of the excluded element, mark the flag as false
            if (excludedLevel == 1) {
                insideExcludedElement = false;
            }
            --excludedLevel;
        }
        //Added by William on 2009-06-24 for req #12014 start
        //<exportanchors> over should write </file> tag

        if(topicMetaSet.contains(qName) && hasExport){
            //If current file is a ditamap file
            if(FileUtils.isDITAMapFile(currentFile.getName())){
                result.append("</file>");
                //If current file is topic file
            }else if(FileUtils.isDITATopicFile(currentFile.getName())){
                result.append("</topicid>");
            }
            hasExport = false;
            topicMetaSet.clear();
        }
        //Added by William on 2009-06-24 for req #12014 start

        //Added by William on 2010-03-02 for /onlytopicinmap update start
        if(!topicrefStack.isEmpty() && localName.equals(topicrefStack.peek())){
            level--;
            topicrefStack.pop();
        }
        //Added by William on 2010-03-02 for /onlytopicinmap update end

        //Added by William on 2010-06-01 for bug:3005748 start
        //decrease element level.
        ditaAttrUtils.decreasePrintLevel();
        //Added by William on 2010-06-01 for bug:3005748 end

    }

    /**
     * Parse the input attributes for needed information.
     * 
     * @param atts all attributes
     * @param attrName attributes to process
     */
    private void parseAttribute(final Attributes atts, final String attrName) throws SAXException {
        String attrValue = atts.getValue(attrName);
        String filename = null;
        final String attrClass = atts.getValue(ATTRIBUTE_NAME_CLASS);
        final String attrScope = atts.getValue(ATTRIBUTE_NAME_SCOPE);
        final String attrFormat = atts.getValue(ATTRIBUTE_NAME_FORMAT);
        final String attrType = atts.getValue(ATTRIBUTE_NAME_TYPE);

        //Added on 20100830 for bug:3052156 start
        final String codebase = atts.getValue(ATTRIBUTE_NAME_CODEBASE);
        //Added on 20100830 for bug:3052156 end

        if (attrValue == null) {
            return;
        }

        // @conkeyref will be resolved to @conref in Debug&Fileter step
        if (ATTRIBUTE_NAME_CONREF.equals(attrName) || ATTRIBUTE_NAME_CONKEYREF.equals(attrName)) {
            hasConRef = true;
        } else if (ATTRIBUTE_NAME_HREF.equals(attrName)) {
            if(attrClass != null &&
                    PR_D_CODEREF.matches(attrClass) ){
                //if current element is <coderef> or its specialization
                //set hasCodeRef to true
                hasCodeRef = true;
            }else{
                hasHref = true;
            }
        } else if(ATTRIBUTE_NAME_KEYREF.equals(attrName)){
            hasKeyRef = true;
        }

        // collect the key definitions
        if(ATTRIBUTE_NAME_KEYS.equals(attrName) && attrValue.length() != 0){

            String target = atts.getValue(ATTRIBUTE_NAME_HREF);

            final String keyRef = atts.getValue(ATTRIBUTE_NAME_KEYREF);


            //Added by Alan for bug ID: 2870935 on Date: 2009-10-10 begin
            final String copy_to = atts.getValue(ATTRIBUTE_NAME_COPY_TO);
            if (!StringUtils.isEmptyString(copy_to)) {
                target = copy_to;
            }
            //Added by Alan for bug ID: 2870935 on Date: 2009-10-10 end
            //Added on 20100825 for bug:3052904 start
            //avoid NullPointException
            if(target == null){
                target = "";
            }
            //Added on 20100825 for bug:3052904 end
            //store the target
            final String temp = target;

            // Many keys can be defined in a single definition, like keys="a b c", a, b and c are seperated by blank.
            for(final String key: attrValue.split(" ")){
                if(!keysDefMap.containsKey(key) && !key.equals("")){
                    if(target != null && target.length() != 0){
                        if(attrScope!=null && (attrScope.equals("external") || attrScope.equals("peer"))){
                            //Added by William on 2010-06-09 for bug:3013079 start
                            //store external or peer resources.
                            exKeysDefMap.put(key, target);
                            //Added by William on 2010-06-09 for bug:3013079 end
                            keysDefMap.put(key, new KeyDef(key, target, null));
                        }else{
                            String tail = "";
                            if(target.indexOf(SHARP) != -1){
                                tail = target.substring(target.indexOf(SHARP));
                                target = target.substring(0, target.indexOf(SHARP));
                            }
                            if(new File(target).isAbsolute()) {
                                target = FileUtils.getRelativePath(rootFilePath.getAbsolutePath(), target);
                            }
                            target = FileUtils.normalizeDirectory(currentDir, target);
                            keysDefMap.put(key, new KeyDef(key, target + tail, null));
                        }
                    }else if(!StringUtils.isEmptyString(keyRef)){
                        //store multi-level keys.
                        keysRefMap.put(key, keyRef);
                    }else{
                        // target is null or empty, it is useful in the future when consider the content of key definition
                        keysDefMap.put(key, new KeyDef(key, null, null));
                    }
                }else{
                    final Properties prop = new Properties();
                    prop.setProperty("%1", key);
                    prop.setProperty("%2", target);
                    // DOTJ045W also exists, but is commented out of the messages file
                    logger.logInfo(MessageUtils.getMessage("DOTJ045I", prop).toString());
                }
                //restore target
                target = temp;
            }
        }


        /*
		if (attrValue.startsWith(SHARP)
				|| attrValue.indexOf(COLON_DOUBLE_SLASH) != -1){
			return;
		}
         */
        /*
         * SF Bug 2724090, broken links in conref'ed footnotes.
         * 
         * NOTE: Need verification.

		if (attrValue.startsWith(SHARP)) {
			attrValue = currentFile;
		}
         */
        //external resource is filtered here.
        if ("external".equalsIgnoreCase(attrScope)
                || "peer".equalsIgnoreCase(attrScope)
                || attrValue.indexOf(COLON_DOUBLE_SLASH) != -1
                || attrValue.startsWith(SHARP)) {
            return;
        }
        //Added by William on 2010-01-05 for bug:2926417 start
        if(attrValue.startsWith("file:/") && attrValue.indexOf("file://") == -1){
            attrValue = attrValue.substring("file:/".length());
            //Unix like OS
            if(UNIX_SEPARATOR.equals(File.separator)){
                attrValue = UNIX_SEPARATOR + attrValue;
            }
        }
        //Added by William on 2010-01-05 for bug:2926417 end
        final File target=new File(attrValue);
        if(target.isAbsolute() &&
                !ATTRIBUTE_NAME_DATA.equals(attrName)){
            attrValue=FileUtils.getRelativePath(rootFilePath.getAbsolutePath(),attrValue);
            //for object tag bug:3052156
        }else if(ATTRIBUTE_NAME_DATA.equals(attrName)){
            if(!StringUtils.isEmptyString(codebase)){
                filename = FileUtils.normalizeDirectory(codebase, attrValue);
            }else{
                filename = FileUtils.normalizeDirectory(currentDir, attrValue);
            }
        }else{
            //noraml process.
            filename = FileUtils.normalizeDirectory(currentDir, attrValue);
        }

        if (filename != null) {
            try{
                filename = URLDecoder.decode(filename, UTF8);
            }catch(final UnsupportedEncodingException e){
                logger.logError("Unable to decode URI '" + filename + "': " + e.getMessage());
            }
        }
        // XXX: At this point, filename should be a system path 

        if (MAP_TOPICREF.matches(attrClass)) {
            if (ATTR_TYPE_VALUE_SUBJECT_SCHEME.equalsIgnoreCase(attrType)) {
                schemeSet.add(filename);
            }

            //Added by William on 2009-06-24 for req #12014 start
            //only transtype = eclipsehelp
            if(INDEX_TYPE_ECLIPSEHELP.equals(transtype)){
                //For only format of the href is dita topic
                if (attrFormat == null ||
                        ATTR_FORMAT_VALUE_DITA.equalsIgnoreCase(attrFormat)){
                    if(attrName.equals(ATTRIBUTE_NAME_HREF)){
                        topicHref = filename;

                        topicHref = FileUtils.separatorsToUnix(topicHref);
                        //attrValue has topicId
                        if(attrValue.lastIndexOf(SHARP) != -1){
                            //get the topicId position
                            final int position = attrValue.lastIndexOf(SHARP);
                            topicId = attrValue.substring(position + 1);
                        }else{
                            //get the first topicId(vaild href file)
                            if(FileUtils.isDITAFile(topicHref)){
                                //topicId = MergeUtils.getInstance().getFirstTopicId(topicHref, (new File(rootFilePath)).getParent(), true);
                                //to be unique
                                topicId = topicHref + QUESTION;
                            }
                        }
                    }
                }else{
                    topicHref = "";
                    topicId = "";
                }
            }
            //Added by William on 2009-06-24 for req #12014 end
        }
        //files referred by coderef won't effect the uplevels, code has already returned.
        if (("DITA-foreign".equals(attrType) &&
                ATTRIBUTE_NAME_DATA.equals(attrName))
                || attrClass!=null && PR_D_CODEREF.matches(attrClass)){

            subsidiarySet.add(filename);
            return;
        }

        /*
         * Collect non-conref and non-copyto targets
         */
        if (filename != null && FileUtils.isValidTarget(filename.toLowerCase()) &&
                (StringUtils.isEmptyString(atts.getValue(ATTRIBUTE_NAME_COPY_TO)) ||
                        !FileUtils.isDITATopicFile(atts.getValue(ATTRIBUTE_NAME_COPY_TO).toLowerCase()) ||
                        (atts.getValue(ATTRIBUTE_NAME_CHUNK)!=null && atts.getValue(ATTRIBUTE_NAME_CHUNK).contains("to-content")) )
                        && !ATTRIBUTE_NAME_CONREF.equals(attrName)
                        && !ATTRIBUTE_NAME_COPY_TO.equals(attrName) &&
                        (canResolved() || FileUtils.isSupportedImageFile(filename.toLowerCase()))) {
            //edited by william on 2009-08-06 for bug:2832696 start
            nonConrefCopytoTargets.add(new Reference(filename, attrFormat));
            //nonConrefCopytoTargets.add(filename);
            //edited by william on 2009-08-06 for bug:2832696 end
        }
        //outside ditamap files couldn't cause warning messages, it is stopped here
        if (attrFormat != null &&
                !ATTR_FORMAT_VALUE_DITA.equalsIgnoreCase(attrFormat)){
            //The format of the href is not dita topic
            //The logic after this "if" clause is not related to files other than dita topic.
            //Therefore, we need to return here to filter out those files in other format.
            return;
        }

        /*
         * Collect only href target topic files for index extracting.
         */
        if (ATTRIBUTE_NAME_HREF.equals(attrName)
                && FileUtils.isDITATopicFile(filename) && canResolved()) {
            hrefTargets.add(new File(filename).getPath());
            toOutFile(new File(filename).getPath());
            //use filename instead(It has already been resolved before-hand) bug:3058124
            //String pathWithoutID = FileUtils.resolveFile(currentDir, attrValue);
            if (chunkLevel > 0 && chunkToNavLevel == 0 && topicGroupLevel == 0 && relTableLevel == 0) {
                chunkTopicSet.add(filename);
            } else {
                hrefTopicSet.add(filename);
            }
        }

        //Added on 20100827 for bug:3052156 start
        //add a warning message for outer files refered by @data
        /*if(ATTRIBUTE_NAME_DATA.equals(attrName)){
			toOutFile(new File(filename).getPath());
		}*/
        //Added on 20100827 for bug:3052156 end

        /*
         * Collect only conref target topic files.
         */
        if (ATTRIBUTE_NAME_CONREF.equals(attrName)
                && FileUtils.isDITAFile(filename)) {
            conrefTargets.add(filename);
            toOutFile(new File(filename).getPath());
        }

        // Collect copy-to (target,source) into hash map
        if (ATTRIBUTE_NAME_COPY_TO.equals(attrName)
                && FileUtils.isDITATopicFile(filename)) {
            final String href = atts.getValue(ATTRIBUTE_NAME_HREF);
            final String value = FileUtils.normalizeDirectory(currentDir, href);

            if (StringUtils.isEmptyString(href)) {
                final StringBuffer buff = new StringBuffer();
                buff.append("[WARN]: Copy-to task [href=\"\" copy-to=\"");
                buff.append(filename);
                buff.append("\"] was ignored.");
                logger.logWarn(buff.toString());
            } else if (copytoMap.get(filename) != null){
                //edited by Alan on Date:2009-11-02 for Work Item:#1590 start
                /*StringBuffer buff = new StringBuffer();
				buff.append("Copy-to task [href=\"");
				buff.append(href);
				buff.append("\" copy-to=\"");
				buff.append(filename);
				buff.append("\"] which points to another copy-to target");
				buff.append(" was ignored.");
				javaLogger.logWarn(buff.toString());*/
                final Properties prop = new Properties();
                prop.setProperty("%1", href);
                prop.setProperty("%2", filename);
                if(!value.equals(copytoMap.get(filename))) {
                	logger.logWarn(MessageUtils.getMessage("DOTX065W", prop).toString());
                }
                //edited by Alan on Date:2009-11-02 for Work Item:#1590 end
                ignoredCopytoSourceSet.add(href);
            } else if (!(atts.getValue(ATTRIBUTE_NAME_CHUNK) != null && atts.getValue(ATTRIBUTE_NAME_CHUNK).contains("to-content"))){
                copytoMap.put(filename, value);
            }

            final String pathWithoutID = FileUtils.resolveFile(currentDir, attrValue);
            if (chunkLevel > 0 && chunkToNavLevel == 0 && topicGroupLevel == 0) {
                chunkTopicSet.add(pathWithoutID);
            } else {
                hrefTopicSet.add(pathWithoutID);
            }

        }

        /*
         * Collect the conaction source topic file
         */
        if(ATTRIBUTE_NAME_CONACTION.equals(attrName)){
            if(attrValue.equals("mark")||attrValue.equals("pushreplace")){
                hasconaction = true;
            }

        }
    }

    //Added on 20100826 for bug:3052913 start
    /**
     * Get multi-level keys list
     */
    private List<String> getKeysList(final String key, final Map<String, String> keysRefMap) {

        final List<String> list = new ArrayList<String>();

        //Iterate the map to look for multi-level keys
        final Iterator<Entry<String, String>> iter = keysRefMap.entrySet().iterator();
        while (iter.hasNext()) {
            final Map.Entry<String, String> entry = iter.next();
            //Multi-level key found
            if(entry.getValue().equals(key)){
                //add key into the list
                final String entryKey = entry.getKey();
                list.add(entryKey);
                //still have multi-level keys
                if(keysRefMap.containsValue(entryKey)){
                    //rescuive point
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
    private void checkMultiLevelKeys(final Map<String, KeyDef> keysDefMap,
            final Map<String, String> keysRefMap) {

        String key = null;
        KeyDef value = null;
        //tempMap storing values to avoid ConcurrentModificationException
        final Map<String, KeyDef> tempMap = new HashMap<String, KeyDef>();
        final Iterator<Entry<String, KeyDef>> iter = keysDefMap.entrySet().iterator();

        while (iter.hasNext()) {
            final Map.Entry<String, KeyDef> entry = iter.next();
            key = entry.getKey();
            value = entry.getValue();
            //there is multi-level keys exist.
            if(keysRefMap.containsValue(key)){
                //get multi-level keys
                final List<String> keysList = getKeysList(key, keysRefMap);
                for (final String multikey : keysList) {
                    //update tempMap
                    tempMap.put(multikey, value);
                }
            }
        }
        //update keysDefMap.
        keysDefMap.putAll(tempMap);
    }
    //Added on 20100826 for bug:3052913 end

    /**
     * Check if path walks up in parent directories
     * 
     * @param toCheckPath path to check
     * @return {@code true} if path walks up, otherwise {@code false}
     */
    private boolean isOutFile(final String toCheckPath) {
        if (!toCheckPath.startsWith("..")) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Check if {@link #currentFile} is a map
     * 
     * @return {@code} true if file is map, otherwise {@code false}
     */
    private boolean isMapFile() {
        final String current=FileUtils.normalize(currentFile.getAbsolutePath());
        if(FileUtils.isDITAMapFile(current)) {
            return true;
        } else {
            return false;
        }
    }
    private boolean canResolved(){
        if ((outputUtils.getOnlyTopicInMap() == false) || isMapFile() ) {
            return true;
        } else {
            return false;
        }
    }
    private void addToOutFilesSet(final String hrefedFile) {
        if (canResolved()) {
            outDitaFilesSet.add(hrefedFile);
        }

    }
    /*
	private Element createElement(String uri, String qName,
			Attributes atts) {
		if (schemeRoot != null) {
			Element element = schemeRoot.createElementNS(uri, qName);
			for (int i = 0; i < atts.getLength(); i++) {
				element.setAttribute(atts.getQName(i), atts.getValue(i));
			}
			return element;
		}
		return null;
	}
     */
    private void toOutFile(final String filename) throws SAXException {
        //the filename is a relative path from the dita input file
        final Properties prop=new Properties();
        prop.put("%1", FileUtils.normalizeDirectory(rootDir.getAbsolutePath(), filename));
        prop.put("%2", FileUtils.normalize(currentFile.getAbsolutePath()));
        if ((OutputUtils.getGeneratecopyouter() == OutputUtils.Generate.NOT_GENERATEOUTTER)
                || (OutputUtils.getGeneratecopyouter() == OutputUtils.Generate.GENERATEOUTTER)) {
            if (isOutFile(filename)) {
                if (outputUtils.getOutterControl() == OutputUtils.OutterControl.FAIL){
                    final MessageBean msgBean=MessageUtils.getMessage("DOTJ035F", prop);
                    throw new SAXParseException(null,null,new DITAOTException(msgBean,null,msgBean.toString()));
                } else if (outputUtils.getOutterControl() == OutputUtils.OutterControl.WARN){
                    final String message=MessageUtils.getMessage("DOTJ036W",prop).toString();
                    logger.logWarn(message);
                }
                addToOutFilesSet(filename);
            }

        }

    }
    /**
     * Get out file set.
     * @return out file set
     */
    public Set<String> getOutFilesSet(){
        return outDitaFilesSet;
    }

    /**
     * @return the hrefTopicSet
     */
    public Set<String> getHrefTopicSet() {
        return hrefTopicSet;
    }

    /**
     * @return the chunkTopicSet
     */
    public Set<String> getChunkTopicSet() {
        return chunkTopicSet;
    }
    /**
     * Get scheme set.
     * @return scheme set
     */
    public Set<String> getSchemeSet() {
        return this.schemeSet;
    }
    /**
     * Get scheme ref set.
     * @return scheme ref set
     */
    public Set<String> getSchemeRefSet() {
        return this.schemeRefSet;
    }

    /**
     * List of files with "@processing-role=resource-only".
     * @return the resource-only set
     */
    public Set<String> getResourceOnlySet() {
        resourceOnlySet.removeAll(crossSet);
        return resourceOnlySet;
    }

    /**
     * Get document root of the merged subject schema.
     * @return
     */
    //public Document getSchemeRoot() {
    //	return schemeRoot;
    //}
    /**
     * Get getRelationshipGrap.
     * @return relationship grap
     */
    public Map<String, Set<String>> getRelationshipGrap() {
        return this.relationGraph;
    }

    public String getPrimaryDitamap() {
        return primaryDitamap;
    }

    public void setPrimaryDitamap(final String primaryDitamap) {
        this.primaryDitamap = primaryDitamap;
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
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof Reference)) {
                return false;
            }
            Reference other = (Reference) obj;
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