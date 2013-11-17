/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.module;

import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.Configuration.*;
import static org.dita.dost.util.Job.*;
import static org.dita.dost.util.URLUtils.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.reader.DitaValReader;
import org.dita.dost.reader.GenListModuleReader;
import org.dita.dost.reader.GenListModuleReader.ExportAnchor;
import org.dita.dost.reader.GenListModuleReader.Reference;
import org.dita.dost.reader.GrammarPoolManager;
import org.dita.dost.util.DelayConrefUtils;
import org.dita.dost.util.FileUtils;
import org.dita.dost.util.FilterUtils;
import org.dita.dost.util.Job;
import org.dita.dost.util.KeyDef;
import org.dita.dost.util.StringUtils;
import org.dita.dost.util.Job.FileInfo;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * This class extends AbstractPipelineModule, used to generate map and topic
 * list by parsing all the refered dita files.
 * 
 * @version 1.0 2004-11-25
 * 
 * @author Wu, Zhi Qiang
 */
public final class GenMapAndTopicListModule extends AbstractPipelineModuleImpl {

    public static final String ELEMENT_STUB = "stub";

    /** Set of all dita files */
    private final Set<File> ditaSet;

    /** Set of all topic files */
    private final Set<File> fullTopicSet;

    /** Set of all map files */
    private final Set<File> fullMapSet;

    /** Set of topic files containing href */
    private final Set<File> hrefTopicSet;

    /** Set of href topic files with anchor ID */
    private final Set<File> hrefWithIDSet;

    /** Set of chunk topic with anchor ID */
    private final Set<File> chunkTopicSet;

    /** Set of map files containing href */
    private final Set<File> hrefMapSet;

    /** Set of dita files containing conref */
    private final Set<File> conrefSet;

    /** Set of topic files containing coderef */
    private final Set<File> coderefSet;

    /** Set of all images */
    private final Set<File> imageSet;

    /** Set of all images used for flagging */
    private final Set<File> flagImageSet;

    /** Set of all html files */
    private final Set<File> htmlSet;

    /** Set of all the href targets */
    private final Set<File> hrefTargetSet;

    /** Set of all the conref targets */
    private Set<File> conrefTargetSet;

    /** Set of all the copy-to sources */
    private Set<File> copytoSourceSet;

    /** Set of all the non-conref targets */
    private final Set<File> nonConrefCopytoTargetSet;

    /** Set of sources of those copy-to that were ignored */
    private final Set<File> ignoredCopytoSourceSet;

    /** Set of subsidiary files */
    private final Set<File> subsidiarySet;

    /** Set of relative flag image files */
    private final Set<File> relFlagImagesSet;

    /** Map of all copy-to (target,source) */
    private Map<File, File> copytoMap;

    /** List of files waiting for parsing. Values are relative system paths. */
    private final List<File> waitList;

    /** List of parsed files */
    private final List<File> doneList;

    /** Set of outer dita files */
    private final Set<File> outDitaFilesSet;

    /** Set of sources of conacion */
    private final Set<File> conrefpushSet;

    /** Set of files containing keyref */
    private final Set<File> keyrefSet;

    /** Set of files with "@processing-role=resource-only" */
    private final Set<File> resourceOnlySet;

    /** Map of all key definitions */
    private final Map<String, KeyDef> keysDefMap;

    /** Absolute basedir for processing */
    private File baseInputDir;

    /** Absolute ditadir for processing */
    private File ditaDir;
    /** Input file name. */
    private File inputFile;
    /** Absolute path for filter file. */
    private File ditavalFile;

    private int uplevels = 0;
    /** Prefix path. Either an empty string or a path which ends in {@link java.io.File#separator File.separator}. */
    private String prefix = "";

    private GenListModuleReader reader;
    private boolean xmlValidate = true;

    /** Absolute path to input file. */
    private File rootFile;
    /** File currently being processed */
    private File currentFile;
    /** Subject scheme key map. Key is key value, value is key definition. */
    private Map<String, KeyDef> schemekeydefMap;
    /** Subject scheme relative file paths. */
    private final Set<File> schemeSet;
    /** Subject scheme usage. Key is relative file path, value is set of applicable subject schemes. */
    private final Map<File, Set<File>> schemeDictionary;
    private String transtype;

    private final Map<String, URI> exKeyDefMap;

    /** use grammar pool cache */
    private boolean gramcache = true;

    private boolean setSystemid = true;

    /**
     * Create a new instance and do the initialization.
     * 
     * @throws ParserConfigurationException never throw such exception
     * @throws SAXException never throw such exception
     */
    public GenMapAndTopicListModule() throws SAXException, ParserConfigurationException {
        ditaSet = new HashSet<File>(128);
        fullTopicSet = new HashSet<File>(128);
        fullMapSet = new HashSet<File>(128);
        hrefTopicSet = new HashSet<File>(128);
        hrefWithIDSet = new HashSet<File>(128);
        chunkTopicSet = new HashSet<File>(128);
        schemeSet = new HashSet<File>(128);
        hrefMapSet = new HashSet<File>(128);
        conrefSet = new HashSet<File>(128);
        imageSet = new HashSet<File>(128);
        flagImageSet = new LinkedHashSet<File>(128);
        htmlSet = new HashSet<File>(128);
        hrefTargetSet = new HashSet<File>(128);
        subsidiarySet = new HashSet<File>(16);
        waitList = new LinkedList<File>();
        doneList = new LinkedList<File>();
        conrefTargetSet = new HashSet<File>(128);
        nonConrefCopytoTargetSet = new HashSet<File>(128);
        copytoMap = new HashMap<File, File>();
        copytoSourceSet = new HashSet<File>(128);
        ignoredCopytoSourceSet = new HashSet<File>(128);
        outDitaFilesSet = new HashSet<File>(128);
        relFlagImagesSet = new LinkedHashSet<File>(128);
        conrefpushSet = new HashSet<File>(128);
        keysDefMap = new HashMap<String, KeyDef>();
        exKeyDefMap = new HashMap<String, URI>();
        keyrefSet = new HashSet<File>(128);
        coderefSet = new HashSet<File>(128);

        schemeDictionary = new HashMap<File, Set<File>>();

        // @processing-role
        resourceOnlySet = new HashSet<File>(128);
    }

    @Override
    public AbstractPipelineOutput execute(final AbstractPipelineInput input) throws DITAOTException {
        if (logger == null) {
            throw new IllegalStateException("Logger not set");
        }

        try {
            parseInputParameters(input);

            // set grammar pool flag
            GrammarPoolManager.setGramCache(gramcache);

            reader = new GenListModuleReader();
            reader.setLogger(logger);
            reader.initXMLReader(ditaDir, xmlValidate, rootFile, setSystemid);
            final FilterUtils filterUtils = parseFilterFile();
            reader.setFilterUtils(filterUtils);
            reader.setJob(job);

            addToWaitList(inputFile);
            processWaitList();
            // Depreciated function
            // The base directory does not change according to the referenceing
            // topic files in the new resolution
            updateBaseDirectory();
            refactoringResult();
            outputResult();
        } catch (final DITAOTException e) {
            throw e;
        } catch (final SAXException e) {
            throw new DITAOTException(e.getMessage(), e);
        } catch (final Exception e) {
            throw new DITAOTException(e.getMessage(), e);
        }

        return null;
    }

    private void parseInputParameters(final AbstractPipelineInput input) throws IOException {
        ditaDir = new File(input.getAttribute(ANT_INVOKER_EXT_PARAM_DITADIR));
        if (input.getAttribute(ANT_INVOKER_PARAM_DITAVAL) != null) {
            ditavalFile = new File(input.getAttribute(ANT_INVOKER_PARAM_DITAVAL));
        }
        xmlValidate = Boolean.valueOf(input.getAttribute(ANT_INVOKER_EXT_PARAM_VALIDATE));

        // get transtype
        transtype = input.getAttribute(ANT_INVOKER_EXT_PARAM_TRANSTYPE);

        gramcache = "yes".equalsIgnoreCase(input.getAttribute(ANT_INVOKER_EXT_PARAM_GRAMCACHE));
        setSystemid = "yes".equalsIgnoreCase(input.getAttribute(ANT_INVOKER_EXT_PARAN_SETSYSTEMID));

        // For the output control
        job.setGeneratecopyouter(input.getAttribute(ANT_INVOKER_EXT_PARAM_GENERATECOPYOUTTER));
        job.setOutterControl(input.getAttribute(ANT_INVOKER_EXT_PARAM_OUTTERCONTROL));
        job.setOnlyTopicInMap(input.getAttribute(ANT_INVOKER_EXT_PARAM_ONLYTOPICINMAP));

        // Set the OutputDir
        final File path = new File(input.getAttribute(ANT_INVOKER_EXT_PARAM_OUTPUTDIR));
        if (path.isAbsolute()) {
            job.setOutputDir(path);
        } else {
            throw new IllegalArgumentException("Output directory " + path + " must be absolute");
        }

        final String basedir = input.getAttribute(ANT_INVOKER_PARAM_BASEDIR);
        final String ditaInputDir = input.getAttribute(ANT_INVOKER_EXT_PARAM_INPUTDIR);
        if (ditaInputDir != null) {
        	File inDir = new File(ditaInputDir);
            if (!inDir.isAbsolute()) {
            	// XXX Shouldn't this be resolved to current directory, not Ant script base directory?
                inDir = new File(basedir, ditaInputDir);
            }
            baseInputDir = inDir.getCanonicalFile();
        }
        
        final String ditaInput = input.getAttribute(ANT_INVOKER_PARAM_INPUTMAP);
        File inFile = new File(ditaInput);
        if (!inFile.isAbsolute()) {
        	if (baseInputDir != null) {
        		inFile = new File(baseInputDir, ditaInput);
        	} else {
	            // XXX Shouldn't this be resolved to current directory, not Ant script base directory?
	            inFile = new File(basedir, ditaInput);
        	}
        }
        inFile = inFile.getCanonicalFile();
        if (baseInputDir == null) {
        	baseInputDir = inFile.getParentFile().getCanonicalFile();
        }

        if (!ditaDir.isAbsolute()) {
            throw new IllegalArgumentException("DITA-OT installation directory " + ditaDir + " must be absolute");
        } else {
            ditaDir = ditaDir.getCanonicalFile();
        }
        if (ditavalFile != null && !ditavalFile.isAbsolute()) {
            // XXX Shouldn't this be resolved to current directory, not Ant script base directory?
            ditavalFile = new File(basedir, ditavalFile.getPath()).getAbsoluteFile();
        }

        rootFile = inFile.getCanonicalFile();
       
        inputFile = new File(FileUtils.getRelativeUnixPath(new File(baseInputDir, "x").getAbsolutePath(), inFile.getAbsolutePath()));
        // create the keydef file for scheme files
    	schemekeydefMap = new HashMap<String, KeyDef>();

        // Set the mapDir
        job.setInputMapPathName(inFile);
    }

    private void processWaitList() throws DITAOTException {
        reader.setTranstype(transtype);

        if (FileUtils.isDITAMapFile(inputFile.getPath())) {
            reader.setPrimaryDitamap(inputFile.getPath());
        }

        while (!waitList.isEmpty()) {
        	currentFile = waitList.remove(0); 
            processFile(currentFile);
        }
    }

    /**
     * Read a file and process it for list information.
     * 
     * @param currentFile system path of the file to process
     * @throws DITAOTException if processing failed
     */
    private void processFile(final File currentFile) throws DITAOTException {
        File fileToParse;
        File file;
        if (currentFile.isAbsolute()) {
            fileToParse = currentFile;
            file = new File(FileUtils.getRelativeUnixPath(rootFile.getAbsolutePath(), currentFile.getPath()));
        } else {
            fileToParse = new File(baseInputDir, currentFile.getPath());
            file = currentFile;
        }
        try {
        	fileToParse = fileToParse.getCanonicalFile();
		} catch (final IOException e1) {
			logger.logError(e1.toString());
		}
        logger.logInfo("Processing " + fileToParse.getAbsolutePath());
        final String[] params = { fileToParse.getAbsolutePath() };

        if (!fileToParse.exists()) {
            logger.logError(MessageUtils.getInstance().getMessage("DOTX008E", params).toString());
            return;
        }
        if (!FileUtils.isValidTarget(file.getPath().toLowerCase())) {
            final Properties prop = new Properties();
            prop.put("%1", fileToParse);
            logger.logWarn(MessageUtils.getInstance().getMessage("DOTJ053W", params).toString());
        }
        try {
            reader.setTranstype(transtype);
            reader.setCurrentDir(file.getParentFile());
            reader.parse(fileToParse);

            // don't put it into dita.list if it is invalid
            if (reader.isValidInput()) {
                processParseResult(file);
                categorizeCurrentFile(file);
            } else if (!file.equals(inputFile)) {
                logger.logWarn(MessageUtils.getInstance().getMessage("DOTJ021W", params).toString());
            }
        } catch (final SAXParseException sax) {
            // To check whether the inner third level is DITAOTBuildException
            // :FATALERROR
            final Exception inner = sax.getException();
            if (inner != null && inner instanceof DITAOTException) {// second
                // level
                logger.logInfo(inner.getMessage());
                throw (DITAOTException) inner;
            }
            if (file.equals(inputFile)) {
                // stop the build if exception thrown when parsing input file.
                final String msg = MessageUtils.getInstance().getMessage("DOTJ012F", params).toString() + ": " + sax.getMessage();
                throw new DITAOTException(MessageUtils.getInstance().getMessage("DOTJ012F", params), sax, msg);
            }
            final String buff = MessageUtils.getInstance().getMessage("DOTJ013E", params).toString() + LINE_SEPARATOR + sax.getMessage();
            logger.logError(buff.toString(), sax);
        } catch (final Exception e) {
            if (file.equals(inputFile)) {
                // stop the build if exception thrown when parsing input file.
                final String msg = MessageUtils.getInstance().getMessage("DOTJ012F", params).toString() + ": " + e.getMessage();
                throw new DITAOTException(MessageUtils.getInstance().getMessage("DOTJ012F", params), e, msg);
            }
            final String buff = MessageUtils.getInstance().getMessage("DOTJ013E", params).toString() + LINE_SEPARATOR + e.getMessage();
            logger.logError(buff, e);
        }

        if (!reader.isValidInput() && file.equals(inputFile)) {
            if (xmlValidate == true) {
                // stop the build if all content in the input file was filtered out.
                throw new DITAOTException(MessageUtils.getInstance().getMessage("DOTJ022F", params).toString());
            } else {
                // stop the build if the content of the file is not valid.
                throw new DITAOTException(MessageUtils.getInstance().getMessage("DOTJ034F", params).toString());
            }
        }

        doneList.add(file);
        reader.reset();

    }

    private void processParseResult(final File currentFile) {
        final Map<File, File> cpMap = reader.getCopytoMap();
        final Map<String, KeyDef> kdMap = reader.getKeysDMap();
        // the reader's reset method will clear the map.
        final Map<String, URI> exKdMap = reader.getExKeysDefMap();
        exKeyDefMap.putAll(exKdMap);

        // Category non-copyto result and update uplevels accordingly
        for (final Reference file: reader.getNonCopytoResult()) {
            categorizeResultFile(file);
            updateUplevels(new File(file.filename));
        }

        // Update uplevels for copy-to targets, and store copy-to map.
        // Note: same key(target) copy-to will be ignored.
        for (final File key: cpMap.keySet()) {
            final File value = cpMap.get(key);

            if (copytoMap.containsKey(key)) {
                /*
                 * StringBuffer buff = new StringBuffer();
                 * buff.append("Copy-to task [href=\""); buff.append(value);
                 * buff.append("\" copy-to=\""); buff.append(key);
                 * buff.append("\"] which points to another copy-to target");
                 * buff.append(" was ignored.");
                 * logger.logWarn(buff.toString());
                 */
                logger.logWarn(MessageUtils.getInstance().getMessage("DOTX065W", value.getPath(), key.getPath()).toString());
                ignoredCopytoSourceSet.add(value);
            } else {
                updateUplevels(key);
                copytoMap.put(key, value);
            }
        }
        schemeSet.addAll(reader.getSchemeRefSet());

        // collect key definitions
        for (final String key: kdMap.keySet()) {
            // key and value.keys will differ when keydef is a redirect to another keydef
            final KeyDef value = kdMap.get(key);
            if (keysDefMap.containsKey(key)) {
                // if there already exists duplicated key definition in
                // different map files.
                // Should only emit this if in a debug mode; comment out for now
                /*
                 * Properties prop = new Properties(); prop.put("%1", key);
                 * prop.put("%2", value); prop.put("%3", currentFile); logger
                 * .logInfo(MessageUtils.getInstance().getMessage("DOTJ048I",
                 * prop).toString());
                 */
            } else {
                updateUplevels(new File(key));
                // add the ditamap where it is defined.
                /*
                 * try { keydef.write("<keydef ");
                 * keydef.write("keys=\""+key+"\" ");
                 * keydef.write("href=\""+value+"\" ");
                 * keydef.write("source=\""+currentFile+"\"/>");
                 * keydef.write("\n"); keydef.flush(); } catch (IOException e) {
                 * 
                 * logger.logError(e.getMessage(), e) ; }
                 */
                keysDefMap.put(key, new KeyDef(key, value.href, value.scope, toURI(currentFile)));
            }
            // if the current file is also a schema file
            if (schemeSet.contains(currentFile)) {
            	schemekeydefMap.put(key, new KeyDef(key, value.href, value.scope, toURI(currentFile)));
            }

        }

        hrefTargetSet.addAll(reader.getHrefTargets());
        hrefWithIDSet.addAll(reader.getHrefTopicSet());
        chunkTopicSet.addAll(reader.getChunkTopicSet());
        // schemeSet.addAll(reader.getSchemeRefSet());
        conrefTargetSet.addAll(reader.getConrefTargets());
        nonConrefCopytoTargetSet.addAll(reader.getNonConrefCopytoTargets());
        ignoredCopytoSourceSet.addAll(reader.getIgnoredCopytoSourceSet());
        subsidiarySet.addAll(reader.getSubsidiaryTargets());
        outDitaFilesSet.addAll(reader.getOutFilesSet());

        // Generate topic-scheme dictionary
        if (reader.getSchemeSet() != null && !reader.getSchemeSet().isEmpty()) {
            Set<File> children = schemeDictionary.get(currentFile);
            if (children == null) {
                children = new HashSet<File>();
            }
            children.addAll(reader.getSchemeSet());
            schemeDictionary.put(currentFile, children);
            final Set<File> hrfSet = reader.getHrefTargets();
            for (final File filename: hrfSet) {
                children = schemeDictionary.get(filename);
                if (children == null) {
                    children = new HashSet<File>();
                }
                children.addAll(reader.getSchemeSet());
                schemeDictionary.put(filename, children);
            }
        }
    }

    /**
     * Categorize current file type
     * 
     * @param currentFile file path
     */
    private void categorizeCurrentFile(final File currentFile) {
        final String lcasefn = currentFile.getPath().toLowerCase();

        ditaSet.add(currentFile);

        if (FileUtils.isDITATopicFile(currentFile.getPath())) {
            hrefTargetSet.add(currentFile);
        }

        if (reader.hasConaction()) {
            conrefpushSet.add(currentFile);
        }

        if (reader.hasConRef()) {
            conrefSet.add(currentFile);
        }

        if (reader.hasKeyRef()) {
            keyrefSet.add(currentFile);
        }

        if (reader.hasCodeRef()) {
            coderefSet.add(currentFile);
        }

        if (FileUtils.isDITATopicFile(lcasefn)) {
            fullTopicSet.add(currentFile);
            if (reader.hasHref()) {
                hrefTopicSet.add(currentFile);
            }
        }

        if (FileUtils.isDITAMapFile(lcasefn)) {
            fullMapSet.add(currentFile);
            if (reader.hasHref()) {
                hrefMapSet.add(currentFile);
            }
        }
    }

    /**
     * Categorize file.
     * 
     * If {@code file} parameter contains a pipe character, the pipe character is followed
     * by the format of the file.
     * 
     * TODO: Pass format as separate DITA class parameter.
     * 
     * @param file file system path with optional format
     */
    private void categorizeResultFile(final Reference file) {
        final String lcasefn = file.filename.toLowerCase();

        // avoid files referred by coderef being added into wait list
        if (subsidiarySet.contains(toFile(file.filename))) {
            return;
        }

        if (FileUtils.isDITAFile(lcasefn)
                && (file.format == null || ATTR_FORMAT_VALUE_DITA.equalsIgnoreCase(file.format) || ATTR_FORMAT_VALUE_DITAMAP
                .equalsIgnoreCase(file.format))) {

            addToWaitList(new File(file.filename));
        } else if (!FileUtils.isSupportedImageFile(lcasefn)) {
            // FIXME: Treating all non-image extensions as HTML/resource files is not correct if HTML/resource files
            //        are defined by the file extension. Correct behaviour would be to remove this else block.
            htmlSet.add(new File(file.filename));
        }
        if (FileUtils.isSupportedImageFile(lcasefn)) {
        	imageSet.add(new File(file.filename));        	      	
			try {
				final File image = new File (baseInputDir, file.filename).getCanonicalFile(); 
				if (!image.exists()){
					logger.logWarn(MessageUtils.getInstance().getMessage("DOTX008W", image.getAbsolutePath()).toString());
	            }
			} catch (final IOException e) {
				logger.logError(e.getMessage());
			}
        }

        if (FileUtils.isHTMLFile(lcasefn) || FileUtils.isResourceFile(lcasefn)) {
            htmlSet.add(new File(file.filename));
        }
    }

    /**
     * Update uplevels if needed. If the parameter contains a {@link org.dita.dost.util.Constants#STICK STICK}, it and
     * anything following it is removed.
     * 
     * @param file file path
     */
    private void updateUplevels(final File file) {
        File f = file;
        if (f.getPath().contains(STICK)) {
            f = new File(f.getPath().substring(0, f.getPath().indexOf(STICK)));
        }

        // for uplevels (../../)
        // ".."-->"../"
        final int lastIndex = FileUtils.separatorsToUnix(FileUtils.normalize(f).getPath())
                .lastIndexOf("../");
        if (lastIndex != -1) {
            final int newUplevels = lastIndex / 3 + 1;
            uplevels = newUplevels > uplevels ? newUplevels : uplevels;
        }
    }

    /**
     * Add the given file the wait list if it has not been parsed.
     * 
     * @param file relative system path
     */
    private void addToWaitList(final File file) {
        if (doneList.contains(file) || waitList.contains(file) || file.equals(currentFile)) {
            return;
        }

        waitList.add(file);
    }

    /**
     * Update base directory based on uplevels.
     */
    private void updateBaseDirectory() {
        for (int i = uplevels; i > 0; i--) {
            final File file = baseInputDir;
            baseInputDir = baseInputDir.getParentFile();
            prefix = new StringBuffer(file.getName()).append(File.separator).append(prefix).toString();
        }
    }

    /**
     * Get up-levels relative path.
     * 
     * @return path to up-level, e.g. {@code ../../}
     */
    private String getUpdateLevels() {
        int current = uplevels;
        final StringBuffer buff = new StringBuffer();
        while (current > 0) {
            buff.append(".." + File.separator);
            current--;
        }
        return buff.toString();
    }

    /**
     * Escape regular expression special characters.
     * 
     * @param value input
     * @return input with regular expression special characters escaped
     */
    private String formatRelativeValue(final String value) {
        final StringBuffer buff = new StringBuffer();
        if (value == null || value.length() == 0) {
            return "";
        }
        int index = 0;
        // $( )+.[^{\
        while (index < value.length()) {
            final char current = value.charAt(index);
            switch (current) {
            case '.':
                buff.append("\\.");
                break;
                // case '/':
                // case '|':
            case '\\':
                buff.append("[\\\\|/]");
                break;
            case '(':
                buff.append("\\(");
                break;
            case ')':
                buff.append("\\)");
                break;
            case '[':
                buff.append("\\[");
                break;
            case ']':
                buff.append("\\]");
                break;
            case '{':
                buff.append("\\{");
                break;
            case '}':
                buff.append("\\}");
                break;
            case '^':
                buff.append("\\^");
                break;
            case '+':
                buff.append("\\+");
                break;
            case '$':
                buff.append("\\$");
                break;
            default:
                buff.append(current);
            }
            index++;
        }
        return buff.toString();
    }

    /**
     * Parse filter file
     * 
     * @return configured filter utility
     */
    private FilterUtils parseFilterFile() {
        final FilterUtils filterUtils = new FilterUtils(printTranstype.contains(transtype));
        filterUtils.setLogger(logger);
        if (ditavalFile != null) {
            final DitaValReader ditaValReader = new DitaValReader();
            ditaValReader.setLogger(logger);
            ditaValReader.initXMLReader(setSystemid);

            ditaValReader.read(ditavalFile.getAbsoluteFile());
            // Store filter map for later use
            filterUtils.setFilterMap(ditaValReader.getFilterMap());
            // Store flagging image used for image copying
            flagImageSet.addAll(ditaValReader.getImageList());
            relFlagImagesSet.addAll(ditaValReader.getRelFlagImageList());
        } else {
            filterUtils.setFilterMap(Collections.EMPTY_MAP);
        }
        return filterUtils;
    }

    private void refactoringResult() {
        resourceOnlySet.addAll(reader.getResourceOnlySet());
        handleConref();
        handleCopyto();
    }

    /**
     * Handle copy-to topics.
     */
    private void handleCopyto() {
        final Map<File, File> tempMap = new HashMap<File, File>();
        final Set<File> pureCopytoSources = new HashSet<File>(128);
        final Set<File> totalCopytoSources = new HashSet<File>(128);

        // Validate copy-to map, remove those without valid sources
        for (final File key: copytoMap.keySet()) {
            final File value = copytoMap.get(key);
            if (new File(baseInputDir + File.separator + prefix, value.getPath()).exists()) {
                tempMap.put(key, value);
                // Add the copy-to target to conreflist when its source has
                // conref
                if (conrefSet.contains(value)) {
                    conrefSet.add(key);
                }
            }
        }

        copytoMap = tempMap;

        // Add copy-to targets into ditaSet, fullTopicSet
        ditaSet.addAll(copytoMap.keySet());
        fullTopicSet.addAll(copytoMap.keySet());

        // Get pure copy-to sources
        totalCopytoSources.addAll(copytoMap.values());
        totalCopytoSources.addAll(ignoredCopytoSourceSet);
        for (final File src: totalCopytoSources) {
            if (!nonConrefCopytoTargetSet.contains(src) && !copytoMap.keySet().contains(src)) {
                pureCopytoSources.add(src);
            }
        }

        copytoSourceSet = pureCopytoSources;

        // Remove pure copy-to sources from ditaSet, fullTopicSet
        ditaSet.removeAll(pureCopytoSources);
        fullTopicSet.removeAll(pureCopytoSources);
    }

    /**
     * Handle topic which are only conref sources from normal processing.
     */
    private void handleConref() {
        // Get pure conref targets
        final Set<File> pureConrefTargets = new HashSet<File>(128);
        for (final File target: conrefTargetSet) {
            if (!nonConrefCopytoTargetSet.contains(target)) {
                pureConrefTargets.add(target);
            }
        }
        conrefTargetSet = pureConrefTargets;

        // Remove pure conref targets from ditaSet, fullTopicSet
        ditaSet.removeAll(pureConrefTargets);
        fullTopicSet.removeAll(pureConrefTargets);
    }

    /**
     * Write result files.
     * 
     * @throws DITAOTException if writing result files failed
     */
    private void outputResult() throws DITAOTException {
        if (!job.tempDir.exists()) {
            job.tempDir.mkdirs();
        }
        
        // assume empty Job
        
        job.setProperty(INPUT_DIR, baseInputDir.getAbsolutePath());
        job.setProperty(INPUT_DITAMAP, prefix + inputFile);

        job.setProperty(INPUT_DITAMAP_LIST_FILE_LIST, USER_INPUT_FILE_LIST_FILE);
        final File inputfile = new File(job.tempDir, USER_INPUT_FILE_LIST_FILE);
        Writer bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(inputfile)));
            bufferedWriter.write(prefix + inputFile);
            bufferedWriter.flush();
        } catch (final FileNotFoundException e) {
            logger.logError(e.getMessage(), e) ;
        } catch (final IOException e) {
            logger.logError(e.getMessage(), e) ;
        } finally {
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                } catch (final IOException e) {
                    logger.logError(e.getMessage(), e) ;
                }
            }
        }

        // add out.dita.files,tempdirToinputmapdir.relative.value to solve the
        // output problem
        job.setProperty("tempdirToinputmapdir.relative.value", formatRelativeValue(prefix));
        job.setProperty("uplevels", getUpdateLevels());
        for (final File file: addFilePrefix(outDitaFilesSet)) {
            job.getOrCreateFileInfo(file).isOutDita = true;
        }
//        // XXX: This loop is probably redundant
//        for (FileInfo f: prop.getFileInfo().values()) {
//            if ("dita".equals(f.format) || "ditamap".equals(f.format)) {
//                f.isActive = false;
//            }
//        }
        for (final File file: addFilePrefix(fullTopicSet)) {
            final FileInfo ff = job.getOrCreateFileInfo(file);
            ff.format = "dita";
            ff.isActive = true;
        }
        for (final File file: addFilePrefix(fullMapSet)) {
            final FileInfo ff = job.getOrCreateFileInfo(file);
            ff.format = "ditamap";
            ff.isActive = true;
        }        
        for (final File file: addFilePrefix(hrefTopicSet)) {
            job.getOrCreateFileInfo(file).hasLink = true;
        }
        for (final File file: addFilePrefix(conrefSet)) {
            job.getOrCreateFileInfo(file).hasConref = true;
        }
        for (final File file: addFilePrefix(imageSet)) {
            job.getOrCreateFileInfo(file).format = "image";
        }
        for (final File file: addFilePrefix(flagImageSet)) {
            final FileInfo f = job.getOrCreateFileInfo(file);
            f.isFlagImage = true;
            f.format = "image";
        }
        for (final File file: addFilePrefix(htmlSet)) {
            job.getOrCreateFileInfo(file).format = "html";
        }
        for (final File file: addFilePrefix(hrefTargetSet)) {
            job.getOrCreateFileInfo(file).isTarget = true;
        }
        for (final File file: addFilePrefix(hrefWithIDSet)) {
            job.getOrCreateFileInfo(file).isNonConrefTarget = true;
        }
        for (final File file: addFilePrefix(chunkTopicSet)) {
            job.getOrCreateFileInfo(file).isSkipChunk = true;
        }
        for (final File file: addFilePrefix(schemeSet)) {
            job.getOrCreateFileInfo(file).isSubjectScheme = true;
        }
        for (final File file: addFilePrefix(conrefTargetSet)) {
            job.getOrCreateFileInfo(file).isConrefTarget = true;
        }
        for (final File file: addFilePrefix(copytoSourceSet)) {
            job.getOrCreateFileInfo(file).isCopyToSource = true;
        }
        for (final File file: addFilePrefix(subsidiarySet)) {
            job.getOrCreateFileInfo(file).isSubtarget = true;
        }
        for (final File file: addFilePrefix(conrefpushSet)) {
            job.getOrCreateFileInfo(file).isConrefPush = true;
        }
        for (final File file: addFilePrefix(keyrefSet)) {
            job.getOrCreateFileInfo(file).hasKeyref = true;
        }
        for (final File file: addFilePrefix(coderefSet)) {
            job.getOrCreateFileInfo(file).hasCoderef = true;
        }
        for (final File file: addFilePrefix(resourceOnlySet)) {
            job.getOrCreateFileInfo(file).isResourceOnly = true;
        }
        
        addFlagImagesSetToProperties(job, REL_FLAGIMAGE_LIST, relFlagImagesSet);

        // Convert copyto map into set and output
        job.setCopytoMap(addFilePrefix(copytoMap));
        addKeyDefSetToProperties(job, keysDefMap);

        try {
            logger.logInfo("Serializing job specification");
            job.write();
        } catch (final IOException e) {
            throw new DITAOTException("Failed to serialize job configuration files: " + e.getMessage(), e);
        }
        
        // Output relation-graph
        writeMapToXML(reader.getRelationshipGrap(), FILE_NAME_SUBJECT_RELATION);
        // Output topic-scheme dictionary
        writeMapToXML(schemeDictionary, FILE_NAME_SUBJECT_DICTIONARY);

        if (INDEX_TYPE_ECLIPSEHELP.equals(transtype)) {
            // Output plugin id
            final File pluginIdFile = new File(job.tempDir, FILE_NAME_PLUGIN_XML);
            final DelayConrefUtils delayConrefUtils = new DelayConrefUtils();
            delayConrefUtils.writeMapToXML(reader.getPluginMap(), pluginIdFile);
            OutputStream exportStream = null;
            XMLStreamWriter export = null;
            try {
            	exportStream = new FileOutputStream(new File(job.tempDir, FILE_NAME_EXPORT_XML));
            	export = XMLOutputFactory.newInstance().createXMLStreamWriter(exportStream);
            	export.writeStartDocument();
            	export.writeStartElement("stub");
            	for (final ExportAnchor e: reader.getExportAnchors()) {
            		export.writeStartElement("file");
            		export.writeAttribute("name", e.file.toString());
            		for (final String t: e.topicids) {
            			export.writeStartElement("topicid");
                		export.writeAttribute("name", t);
                		export.writeEndElement();
            		}
            		for (final String i: e.ids) {
            			export.writeStartElement("id");
                		export.writeAttribute("name", i);
                		export.writeEndElement();
            		}
            		for (final String k: e.keys) {
            			export.writeStartElement("keyref");
                		export.writeAttribute("name", k);
                		export.writeEndElement();
            		}
            		export.writeEndElement();
            	}
            	export.writeEndElement();
            	export.writeEndDocument();
            } catch (final FileNotFoundException e) {
				throw new DITAOTException("Failed to write export anchor file: " + e.getMessage(), e);
			} catch (final XMLStreamException e) {
				throw new DITAOTException("Failed to serialize export anchor file: " + e.getMessage(), e);
			} finally {
            	if (export != null) {
            		try {
						export.close();
					} catch (final XMLStreamException e) {
						logger.logError("Failed to close export anchor file: " + e.getMessage(), e);
					}
            	}
            	if (exportStream != null) {
            		try {
						exportStream.close();
					} catch (final IOException e) {
						logger.logError("Failed to close export anchor file: " + e.getMessage(), e);
					}
            	}
            }
        }

        KeyDef.writeKeydef(new File(job.tempDir, SUBJECT_SCHEME_KEYDEF_LIST_FILE), schemekeydefMap.values());
    }
    
    /**
     * Write map of sets to a file.
     * 
     * <p>The serialization format is XML properties format where values are comma
     * separated lists.</p>
     * 
     * @param m map to serialize
     * @param filename output filename
     */
    private void writeMapToXML(final Map<File, Set<File>> m, final String filename) {
        if (m == null) {
            return;
        }
        final Properties prop = new Properties();
        for (final Map.Entry<File, Set<File>> entry: m.entrySet()) {
            final File key = entry.getKey();
            final String value = StringUtils.assembleString(entry.getValue(), COMMA);
            prop.setProperty(key.getPath(), value);
        }
        final File outputFile = new File(job.tempDir, filename);
        OutputStream os = null;
        try {
            os = new FileOutputStream(outputFile);
            prop.storeToXML(os, null);
            os.close();
        } catch (final IOException e) {
            logger.logError(e.getMessage(), e) ;
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (final Exception e) {
                    logger.logError(e.getMessage(), e) ;
                }
            }
        }
    }

    /**
     * Add file prefix. For absolute paths the prefix is not added.
     * 
     * @param set file paths
     * @return file paths with prefix
     */
    private Set<String> addPrefix(final Set<String> set) {
        final Set<String> newSet = new HashSet<String>(set.size());
        for (final String file: set) {
            if (new File(file).isAbsolute()) {
                newSet.add(FileUtils.normalize(file).getPath());
            } else {
                // In ant, all the file separator should be slash, so we need to
                // replace all the back slash with slash.
                final int index = file.indexOf(EQUAL);
                if (index != -1) {
                    // keyname
                    final String to = file.substring(0, index);
                    final String source = file.substring(index + 1);
                    
                    newSet.add(FileUtils.normalize(prefix + to).getPath()
                            + EQUAL
                            + FileUtils.normalize(prefix + source).getPath());
                } else {
                    newSet.add(FileUtils.normalize(prefix + file).getPath());
                }
            }
        }
        return newSet;
    }
    
    /**
     * Add file prefix. For absolute paths the prefix is not added.
     * 
     * @param set file paths
     * @return file paths with prefix
     */
    private Set<File> addFilePrefix(final Set<File> set) {
        final Set<File> newSet = new HashSet<File>(set.size());
        for (final File file: set) {
            if (file.isAbsolute()) {
                newSet.add(FileUtils.normalize(file));
            } else {
                newSet.add(FileUtils.normalize(prefix + file));
            }
        }
        return newSet;
    }
    
    /**
     * Add file prefix. For absolute paths the prefix is not added.
     * 
     * @param set file paths
     * @return file paths with prefix
     */
    private Map<File, File> addFilePrefix(final Map<File, File> set) {
        final Map<File, File> newSet = new HashMap<File, File>();
        for (final Map.Entry<File, File> file: set.entrySet()) {
            File key = null;
            if (file.getKey().isAbsolute()) {
                key = FileUtils.normalize(file.getKey());
            } else {
                key = FileUtils.normalize(prefix + file.getKey());
            }
            File value = null; 
            if (file.getValue().isAbsolute()) {
                value = FileUtils.normalize(file.getValue());
            } else {
                value = FileUtils.normalize(prefix + file.getValue());
            }
            newSet.put(key, value);
        }
        return newSet;
    }
    
    /**
     * Add file prefix. For absolute paths the prefix is no added.
     * 
     * @param map map of file paths
     * @return file path map with prefix
     */
    private Map<String, String> addPrefix(final Map<String, String> map) {
        final Map<String, String> newMap = new HashMap<String, String>(map.size());
        for (final Map.Entry<String, String> e: map.entrySet()) {
            String to = e.getKey();
            if (new File(to).isAbsolute()) {
                to = FileUtils.normalize(to).getPath();
            } else {
                to = FileUtils.normalize(prefix + to).getPath();
            }
            String source = e.getValue();
            if (new File(source).isAbsolute()) {
                source = FileUtils.normalize(source).getPath();
            } else {
                source = FileUtils.normalize(prefix + source).getPath();
            }
            newMap.put(to, source);
        }
        return newMap;
    }
    
    /**
     * Add key definition to job configuration
     * 
     * @param prop job configuration
     * @param keydefs key defintions to add
     */
    private void addKeyDefSetToProperties(final Job prop, final Map<String, KeyDef> keydefs) {
        // update value
        final Collection<KeyDef> updated = new ArrayList<KeyDef>(keydefs.size());
        for (final KeyDef file: keydefs.values()) {
            final String keys = file.keys;
            URI href = file.href;
            URI source = file.source;
            if (prefix.length() != 0) {
                // cases where keymap is in map ancestor folder
                if (href == null) {
                    //href = FileUtils.separatorsToUnix(FileUtils.normalize(prefix));
                    source = toURI(FileUtils.normalize(prefix + source.toString()));
                } else {
                    if (!exKeyDefMap.containsKey(file.keys)) {
                        href = toURI(FileUtils.normalize(prefix + href.toString()));
                    }
                    source = toURI(FileUtils.normalize(prefix + source.toString()));
                }
            }
            final KeyDef keyDef = new KeyDef(keys, href, file.scope, source);
            updated.add(keyDef);
        }
        // write key definition
        try {
            KeyDef.writeKeydef(new File(job.tempDir, KEYDEF_LIST_FILE), updated);
        } catch (final DITAOTException e) {
            logger.logError("Failed to write key definition file: " + e.getMessage(), e);
        }
    }

    /**
     * add FlagImangesSet to Properties, which needn't to change the dir level,
     * just ouput to the ouput dir.
     * 
     * @param prop job configuration
     * @param key list name
     * @param set relative flag image files
     */
    private void addFlagImagesSetToProperties(final Job prop, final String key, final Set<File> set) {
        final Set<File> newSet = new LinkedHashSet<File>(128);
        for (final File file: set) {
            if (file.isAbsolute()) {
                // no need to append relative path before absolute paths
                newSet.add(FileUtils.normalize(file));
            } else {
                // In ant, all the file separator should be slash, so we need to
                // replace all the back slash with slash.
                newSet.add(FileUtils.normalize(file));
            }
        }

        // write list attribute to file
        final String fileKey = key.substring(0, key.lastIndexOf("list")) + "file";
        prop.setProperty(fileKey, key.substring(0, key.lastIndexOf("list")) + ".list");
        final File list = new File(job.tempDir, prop.getProperty(fileKey));
        Writer bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(list)));
            final Iterator<File> it = newSet.iterator();
            while (it.hasNext()) {
                bufferedWriter.write(it.next().getPath());
                if (it.hasNext()) {
                    bufferedWriter.write("\n");
                }
            }
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (final FileNotFoundException e) {
            logger.logError(e.getMessage(), e) ;
        } catch (final IOException e) {
            logger.logError(e.getMessage(), e) ;
        } finally {
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                } catch (final IOException e) {
                    logger.logError(e.getMessage(), e) ;
                }
            }
        }

        prop.setProperty(key, StringUtils.assembleString(newSet, COMMA));
    }
    
}