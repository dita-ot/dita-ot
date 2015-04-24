/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.module;

import static org.dita.dost.reader.GenListModuleReader.*;
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
import java.util.Queue;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.xerces.xni.grammars.XMLGrammarPool;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.exception.DITAOTXMLErrorHandler;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.reader.*;
import org.dita.dost.reader.GenListModuleReader.Reference;
import org.dita.dost.util.*;
import org.dita.dost.util.Job.FileInfo;
import org.dita.dost.writer.ExportAnchorsFilter;
import org.dita.dost.writer.ExportAnchorsFilter.ExportAnchor;
import org.dita.dost.writer.ProfilingFilter;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

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
    /** FileInfos keyed by src. */
    private final Map<URI, FileInfo> fileinfos = new HashMap<URI, FileInfo>();
    /** Set of all topic files */
    private final Set<URI> fullTopicSet;

    /** Set of all map files */
    private final Set<URI> fullMapSet;

    /** Set of topic files containing href */
    private final Set<URI> hrefTopicSet;

    /** Set of href topic files with anchor ID */
    private final Set<URI> hrefWithIDSet;

    /** Set of chunk topic with anchor ID */
    private final Set<URI> chunkTopicSet;

    /** Set of dita files containing conref */
    private final Set<URI> conrefSet;

    /** Set of topic files containing coderef */
    private final Set<URI> coderefSet;

    /** Set of all images */
    private final Set<URI> imageSet;

    /** Set of all images used for flagging */
    private final Set<URI> flagImageSet;

    /** Set of all html files */
    private final Set<URI> htmlSet;

    /** Set of all the href targets */
    private final Set<URI> hrefTargetSet;

    /** Set of all the conref targets */
    private Set<URI> conrefTargetSet;

    /** Set of all the copy-to sources */
    private Set<URI> copytoSourceSet;

    /** Set of all the non-conref targets */
    private final Set<URI> nonConrefCopytoTargetSet;

    /** Set of sources of those copy-to that were ignored */
    private final Set<URI> ignoredCopytoSourceSet;

    /** Set of subsidiary files */
    private final Set<URI> subsidiarySet;

    /** Set of absolute flag image files */
    private final Set<URI> relFlagImagesSet;

    /** Map of all copy-to (target,source) */
    private Map<URI, URI> copytoMap;

    /** List of files waiting for parsing. Values are absolute URI references. */
    private final Queue<Reference> waitList;

    /** List of parsed files */
    private final List<URI> doneList;
    private final List<URI> failureList;

    /** Set of outer dita files */
    private final Set<URI> outDitaFilesSet;

    /** Set of sources of conacion */
    private final Set<URI> conrefpushSet;

    /** Set of files containing keyref */
    private final Set<URI> keyrefSet;

    /** Set of files with "@processing-role=resource-only" */
    private final Set<URI> resourceOnlySet;

    /** Map of all key definitions */
    private final Map<String, KeyDef> keysDefMap;

    /** Absolute basedir for processing */
    private File baseInputDir;

    /** Absolute ditadir for processing */
    private File ditaDir;
    /** Profiling is enabled. */
    private boolean profilingEnabled;
    /** Absolute path for filter file. */
    private File ditavalFile;
    /** Number of directory levels base directory is adjusted. */
    private int uplevels = 0;

    /** XMLReader instance for parsing dita file */
    private XMLReader reader;
    private GenListModuleReader listFilter;
    private KeydefFilter keydefFilter;
    private ExportAnchorsFilter exportAnchorsFilter;
    private boolean xmlValidate = true;
    private ContentHandler nullHandler;
    private FilterUtils filterUtils;
    private TempFileNameScheme tempFileNameScheme;

    /** Absolute path to input file. */
    private URI rootFile;
    /** File currently being processed */
    private URI currentFile;
    /** Subject scheme key map. Key is key value, value is key definition. */
    private Map<String, KeyDef> schemekeydefMap;
    /** Subject scheme absolute file paths. */
    private final Set<URI> schemeSet;
    /** Subject scheme usage. Key is absolute file path, value is set of applicable subject schemes. */
    private final Map<URI, Set<URI>> schemeDictionary;
    private String transtype;

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
        fullTopicSet = new HashSet<URI>(128);
        fullMapSet = new HashSet<URI>(128);
        hrefTopicSet = new HashSet<URI>(128);
        hrefWithIDSet = new HashSet<URI>(128);
        chunkTopicSet = new HashSet<URI>(128);
        schemeSet = new HashSet<URI>(128);
        conrefSet = new HashSet<URI>(128);
        imageSet = new HashSet<URI>(128);
        flagImageSet = new LinkedHashSet<URI>(128);
        htmlSet = new HashSet<URI>(128);
        hrefTargetSet = new HashSet<URI>(128);
        subsidiarySet = new HashSet<URI>(16);
        waitList = new LinkedList<Reference>();
        doneList = new LinkedList<URI>();
        failureList = new LinkedList<URI>();
        conrefTargetSet = new HashSet<URI>(128);
        nonConrefCopytoTargetSet = new HashSet<URI>(128);
        copytoMap = new HashMap<URI, URI>();
        copytoSourceSet = new HashSet<URI>(128);
        ignoredCopytoSourceSet = new HashSet<URI>(128);
        outDitaFilesSet = new HashSet<URI>(128);
        relFlagImagesSet = new LinkedHashSet<URI>(128);
        conrefpushSet = new HashSet<URI>(128);
        keysDefMap = new HashMap<String, KeyDef>();
        keyrefSet = new HashSet<URI>(128);
        coderefSet = new HashSet<URI>(128);

        schemeDictionary = new HashMap<URI, Set<URI>>();

        // @processing-role
        resourceOnlySet = new HashSet<URI>(128);
    }

    @Override
    public AbstractPipelineOutput execute(final AbstractPipelineInput input) throws DITAOTException {
        if (logger == null) {
            throw new IllegalStateException("Logger not set");
        }

        try {
            parseInputParameters(input);

            initFilters();
            initXMLReader(ditaDir, xmlValidate);
            
            addToWaitList(new Reference(rootFile));
            processWaitList();

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
    
    /**
     * Initialize reusable filters.
     */
    private void initFilters() {
        listFilter = new GenListModuleReader();
        listFilter.setLogger(logger);
        listFilter.setInputDir(rootFile.resolve("."));
        listFilter.setPrimaryDitamap(rootFile);
        listFilter.setJob(job);
        
        if (profilingEnabled) {
            filterUtils = parseFilterFile();
        }

        exportAnchorsFilter = new ExportAnchorsFilter();
        exportAnchorsFilter.setInputFile(rootFile);
        
        keydefFilter = new KeydefFilter();
        keydefFilter.setLogger(logger);
        keydefFilter.setInputFile(rootFile);
        keydefFilter.setJob(job);
        
        nullHandler = new DefaultHandler();
    }

    /**
     * Init xml reader used for pipeline parsing.
     * 
     * @param ditaDir absolute path to DITA-OT directory
     * @param validate whether validate input file
     * @throws SAXException parsing exception
     */
    private void initXMLReader(final File ditaDir, final boolean validate) throws SAXException {
        reader = XMLUtils.getXMLReader();
        // to check whether the current parsing file's href value is out of inputmap.dir
        reader.setFeature(FEATURE_NAMESPACE_PREFIX, true);
        if (validate) {
            reader.setFeature(FEATURE_VALIDATION, true);
            try {
                reader.setFeature(FEATURE_VALIDATION_SCHEMA, true);
            } catch (final SAXNotRecognizedException e) {
                // Not Xerces, ignore exception
            }
        } else {
            final String msg = MessageUtils.getInstance().getMessage("DOTJ037W").toString();
            logger.warn(msg);
        }
        if (gramcache) {
            final XMLGrammarPool grammarPool = GrammarPoolManager.getGrammarPool();
            try {
                reader.setProperty("http://apache.org/xml/properties/internal/grammar-pool", grammarPool);
                logger.info("Using Xerces grammar pool for DTD and schema caching.");
            } catch (final NoClassDefFoundError e) {
                logger.debug("Xerces not available, not using grammar caching");
            } catch (final SAXNotRecognizedException e) {
                logger.warn("Failed to set Xerces grammar pool for parser: " + e.getMessage());
            } catch (final SAXNotSupportedException e) {
                logger.warn("Failed to set Xerces grammar pool for parser: " + e.getMessage());
            }
        }
        CatalogUtils.setDitaDir(ditaDir);
        reader.setEntityResolver(CatalogUtils.getCatalogResolver());
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
        job.setOnlyTopicInMap(Boolean.valueOf(input.getAttribute(ANT_INVOKER_EXT_PARAM_ONLYTOPICINMAP)));

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
        profilingEnabled = true;
        if (input.getAttribute(ANT_INVOKER_PARAM_PROFILING_ENABLED) != null) {
            profilingEnabled = Boolean.parseBoolean(input.getAttribute(ANT_INVOKER_PARAM_PROFILING_ENABLED));
        }
        if (profilingEnabled) {
            if (ditavalFile != null && !ditavalFile.isAbsolute()) {
                // XXX Shouldn't this be resolved to current directory, not Ant script base directory?
                ditavalFile = new File(basedir, ditavalFile.getPath()).getAbsoluteFile();
            }
        }

        rootFile = inFile.getAbsoluteFile().toURI();
        // create the keydef file for scheme files
        schemekeydefMap = new HashMap<String, KeyDef>();

        // Set the mapDir
        job.setInputFile(inFile);
    }

    private void processWaitList() throws DITAOTException {
        while (!waitList.isEmpty()) {
            processFile(waitList.remove());
        }
    }
    
    /**
     * Get pipe line filters
     * 
     * @param fileToParse absolute path to current file being processed
     */
    private List<XMLFilter> getProcessingPipe(final URI fileToParse) {
        assert fileToParse.isAbsolute();
        final List<XMLFilter> pipe = new ArrayList<XMLFilter>();

        if (filterUtils != null) {
            final ProfilingFilter profilingFilter = new ProfilingFilter();
            profilingFilter.setLogger(logger);
            profilingFilter.setJob(job);
            profilingFilter.setFilterUtils(filterUtils);
            pipe.add(profilingFilter);
        }
        
        if (INDEX_TYPE_ECLIPSEHELP.equals(transtype)) {
            exportAnchorsFilter.setCurrentFile(fileToParse);
            exportAnchorsFilter.setErrorHandler(new DITAOTXMLErrorHandler(fileToParse.toString(), logger));
            pipe.add(exportAnchorsFilter);
        }

        keydefFilter.setCurrentDir(toFile(fileToParse).getParentFile().toURI());
        keydefFilter.setErrorHandler(new DITAOTXMLErrorHandler(fileToParse.toString(), logger));
        pipe.add(keydefFilter);

        listFilter.setCurrentFile(fileToParse);
        listFilter.setErrorHandler(new DITAOTXMLErrorHandler(fileToParse.toString(), logger));
        pipe.add(listFilter);

        return pipe;
    }

    /**
     * Read a file and process it for list information.
     * 
     * @param ref system path of the file to process
     * @throws DITAOTException if processing failed
     */
    private void processFile(final Reference ref) throws DITAOTException {
        currentFile = ref.filename;
        assert currentFile.isAbsolute();
        logger.info("Processing " + currentFile);
        final String[] params = { currentFile.toString() };
        
        try {
            XMLReader xmlSource = getXmlReader(ref.format);
            for (final XMLFilter f: getProcessingPipe(currentFile)) {
                f.setParent(xmlSource);
                f.setEntityResolver(CatalogUtils.getCatalogResolver());
                xmlSource = f;
            }
            xmlSource.setContentHandler(nullHandler);            
            
            xmlSource.parse(currentFile.toString());

            if (listFilter.isValidInput()) {
                processParseResult(currentFile);
                categorizeCurrentFile(ref);
            } else if (!currentFile.equals(rootFile)) {
                logger.warn(MessageUtils.getInstance().getMessage("DOTJ021W", params).toString());
                failureList.add(currentFile);
            }
        } catch (final RuntimeException e) {
            throw e;
        } catch (final SAXParseException sax) {
            final Exception inner = sax.getException();
            if (inner != null && inner instanceof DITAOTException) {
                throw (DITAOTException) inner;
            }
            if (currentFile.equals(rootFile)) {
                throw new DITAOTException(MessageUtils.getInstance().getMessage("DOTJ012F", params).toString() + ": " + sax.getMessage(), sax);
            } else {
                logger.error(MessageUtils.getInstance().getMessage("DOTJ013E", params).toString() + ": " + sax.getMessage(), sax);
            }
            failureList.add(currentFile);
        } catch (final FileNotFoundException e) {
            if (currentFile.equals(rootFile)) {
                throw new DITAOTException(MessageUtils.getInstance().getMessage("DOTX008E", params).toString(), e);
            } else {
                logger.error(MessageUtils.getInstance().getMessage("DOTX008E", params).toString());
            }
            failureList.add(currentFile);
        } catch (final Exception e) {
            if (currentFile.equals(rootFile)) {
                throw new DITAOTException(MessageUtils.getInstance().getMessage("DOTJ012F", params).toString() + ": " + e.getMessage(),  e);
            } else {
                logger.error(MessageUtils.getInstance().getMessage("DOTJ013E", params).toString() + ": " + e.getMessage(), e);
            }
            failureList.add(currentFile);
        }

        if (!listFilter.isValidInput() && currentFile.equals(rootFile)) {
            if (xmlValidate) {
                // stop the build if all content in the input file was filtered out.
                throw new DITAOTException(MessageUtils.getInstance().getMessage("DOTJ022F", params).toString());
            } else {
                // stop the build if the content of the file is not valid.
                throw new DITAOTException(MessageUtils.getInstance().getMessage("DOTJ034F", params).toString());
            }
        }

        doneList.add(currentFile);
        listFilter.reset();
        keydefFilter.reset();

    }

    private XMLReader getXmlReader(final String format) throws SAXException {
        for (final Map.Entry<String, String> e: parserMap.entrySet()) {
            if (format != null && format.equals(e.getKey())) {
                try {
                    return (XMLReader) this.getClass().forName(e.getValue()).newInstance();
                } catch (final InstantiationException ex) {
                    throw new SAXException(ex);
                } catch (final IllegalAccessException ex) {
                    throw new SAXException(ex);
                } catch (final ClassNotFoundException ex) {
                    throw new SAXException(ex);
                }
            }
        }
        return reader;
    }

    /**
     * Process results from parsing a single topic
     *
     * @param currentFile absolute URI processes files
     */
    private void processParseResult(final URI currentFile) {
        final Map<URI, URI> cpMap = listFilter.getCopytoMap();
        final Map<String, KeyDef> kdMap = keydefFilter.getKeysDMap();

        // Category non-copyto result and update uplevels accordingly
        for (final Reference file: listFilter.getNonCopytoResult()) {
            categorizeReferenceFile(file);
            updateUplevels(file.filename);
        }

        // Update uplevels for copy-to targets, and store copy-to map.
        // Note: same key(target) copy-to will be ignored.
        for (final Map.Entry<URI, URI> e: cpMap.entrySet()) {
            final URI key = e.getKey();
            final URI value = e.getValue();
            if (copytoMap.containsKey(key)) {
                logger.warn(MessageUtils.getInstance().getMessage("DOTX065W", value.getPath(), key.getPath()).toString());
                ignoredCopytoSourceSet.add(value);
            } else {
                updateUplevels(key);
                copytoMap.put(key, value);
            }
        }
        schemeSet.addAll(listFilter.getSchemeRefSet());

        // collect key definitions
        for (final Map.Entry<String, KeyDef> e: kdMap.entrySet()) {
            // key and value.keys will differ when keydef is a redirect to another keydef
            final String key = e.getKey();
            final KeyDef value = e.getValue();
            if (!keysDefMap.containsKey(key)) {
                keysDefMap.put(key, new KeyDef(key, value.href, value.scope, currentFile));
            }
            // if the current file is also a schema file
            if (schemeSet.contains(currentFile)) {
                schemekeydefMap.put(key, new KeyDef(key, value.href, value.scope, currentFile));
            }
        }

        hrefTargetSet.addAll(listFilter.getHrefTargets());
        hrefWithIDSet.addAll(listFilter.getHrefTopicSet());
        chunkTopicSet.addAll(listFilter.getChunkTopicSet());
        conrefTargetSet.addAll(listFilter.getConrefTargets());
        nonConrefCopytoTargetSet.addAll(listFilter.getNonConrefCopytoTargets());
        ignoredCopytoSourceSet.addAll(listFilter.getIgnoredCopytoSourceSet());
        subsidiarySet.addAll(listFilter.getSubsidiaryTargets());
        outDitaFilesSet.addAll(listFilter.getOutFilesSet());

        // Generate topic-scheme dictionary
        final Set<URI> schemeSet = listFilter.getSchemeSet();
        if (schemeSet != null && !schemeSet.isEmpty()) {
            Set<URI> children = schemeDictionary.get(currentFile);
            if (children == null) {
                children = new HashSet<URI>();
            }
            children.addAll(schemeSet);
            schemeDictionary.put(currentFile, children);
            final Set<URI> hrfSet = listFilter.getHrefTargets();
            for (final URI filename: hrfSet) {
                children = schemeDictionary.get(filename);
                if (children == null) {
                    children = new HashSet<URI>();
                }
                children.addAll(schemeSet);
                schemeDictionary.put(filename, children);
            }
        }
    }

    /**
     * Categorize current file type
     * 
     * @param ref file path
     */
    private void categorizeCurrentFile(final Reference ref) {
        final URI currentFile = ref.filename;
        if (listFilter.hasConaction()) {
            conrefpushSet.add(currentFile);
        }

        if (listFilter.hasConRef()) {
            conrefSet.add(currentFile);
        }

        if (listFilter.hasKeyRef()) {
            keyrefSet.add(currentFile);
        }

        if (listFilter.hasCodeRef()) {
            coderefSet.add(currentFile);
        }

        if (listFilter.isDitaTopic()) {
            if (ref.format != null) {
                assert currentFile.getFragment() == null;
                final URI f = currentFile.normalize();
                if (!fileinfos.containsKey(f)) {
                    final FileInfo i = new FileInfo.Builder()
                            //.uri(tempFileNameScheme.generateTempFileName(currentFile))
                            .src(currentFile)
                            .format(ref.format)
                            .build();
                    fileinfos.put(i.src, i);
                }
            }
            fullTopicSet.add(currentFile);
            hrefTargetSet.add(currentFile);
            if (listFilter.hasHref()) {
                hrefTopicSet.add(currentFile);
            }
        } else if (listFilter.isDitaMap()) {
            fullMapSet.add(currentFile);
        }
    }

    /**
     * Categorize file.
     * 
     * @param file file system path with optional format
     */
    private void categorizeReferenceFile(final Reference file) {
        // avoid files referred by coderef being added into wait list
        if (subsidiarySet.contains(file.filename)) {
            return;
        }
        if (isFormatDita(file.format) || ATTR_FORMAT_VALUE_DITAMAP.equals(file.format)) {
            addToWaitList(file);
        } else if (ATTR_FORMAT_VALUE_IMAGE.equals(file.format)) {
            imageSet.add(file.filename);
            final File image = toFile(file.filename);
            if (!image.exists()){
                logger.warn(MessageUtils.getInstance().getMessage("DOTX008W", image.getAbsolutePath()).toString());
            }
        } else {
            htmlSet.add(file.filename);
        }
    }

    /**
     * Update uplevels if needed. If the parameter contains a {@link org.dita.dost.util.Constants#STICK STICK}, it and
     * anything following it is removed.
     * 
     * @param file file path
     */
    private void updateUplevels(final URI file) {
        assert file.isAbsolute();
        if (file.getPath() != null) {
            final URI f = file.toString().contains(STICK)
                    ? toURI(file.toString().substring(0, file.toString().indexOf(STICK)))
                    : file;
            final URI relative = getRelativePath(rootFile, f).normalize();
            final int lastIndex = relative.getPath().lastIndexOf(".." + URI_SEPARATOR);
            if (lastIndex != -1) {
                final int newUplevels = lastIndex / 3 + 1;
                uplevels = Math.max(newUplevels, uplevels);
            }
        }
    }

    /**
     * Add the given file the wait list if it has not been parsed.
     * 
     * @param ref reference to absolute system path
     */
    private void addToWaitList(final Reference ref) {
        final URI file = ref.filename;
        assert file.isAbsolute() && file.getFragment() == null;
        if (doneList.contains(file) || waitList.contains(ref) || file.equals(currentFile)) {
            return;
        }

        waitList.add(ref);
    }

    /**
     * Update base directory and prefix based on uplevels.
     */
    private void updateBaseDirectory() {
        for (int i = uplevels; i > 0; i--) {
            baseInputDir = baseInputDir.getParentFile();
        }
    }

    /**
     * Get up-levels absolute path.
     *
     * @param rootTemp relative URI for temporary root file
     * @return path to up-level, e.g. {@code ../../}, may be empty string
     */
    private String getLevelsPath(final URI rootTemp) {
        final int u = rootTemp.toString().split(URI_SEPARATOR).length - 1;
        if (u == 0) {
            return "";
        }
        final StringBuilder buff = new StringBuilder();
        for (int current = u; current > 0; current--) {
            buff.append("..").append(File.separator);
        }
        return buff.toString();
    }

    /**
     * Escape regular expression special characters.
     * 
     * @param value input
     * @return input with regular expression special characters escaped
     */
    private static String escapeRegExp(final String value) {
        final StringBuilder buff = new StringBuilder();
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
        Map<FilterUtils.FilterKey, FilterUtils.Action> filterMap;
        if (ditavalFile != null) {
            final DitaValReader ditaValReader = new DitaValReader();
            ditaValReader.setLogger(logger);
            ditaValReader.initXMLReader(setSystemid);

            ditaValReader.read(ditavalFile.getAbsoluteFile());
            // Store filter map for later use
            filterMap = ditaValReader.getFilterMap();
            // Store flagging image used for image copying
            flagImageSet.addAll(ditaValReader.getImageList());
            relFlagImagesSet.addAll(ditaValReader.getRelFlagImageList());
        } else {
            filterMap = Collections.emptyMap();
        }
        final FilterUtils filterUtils = new FilterUtils(printTranstype.contains(transtype), filterMap);
        filterUtils.setLogger(logger);
        return filterUtils;
    }

    private void refactoringResult() {
        resourceOnlySet.addAll(listFilter.getResourceOnlySet());
        handleConref();
        handleCopyto();
    }

    /**
     * Handle copy-to topics.
     */
    private void handleCopyto() {
        final Map<URI, URI> tempMap = new HashMap<URI, URI>();
        final Set<URI> pureCopytoSources = new HashSet<URI>(128);
        final Set<URI> totalCopytoSources = new HashSet<URI>(128);

        // Validate copy-to map, remove those without valid sources
        for (final URI target: copytoMap.keySet()) {
            final URI source = copytoMap.get(target);
            assert source.isAbsolute();
            assert target.isAbsolute();
            if (exists(source)) {
                tempMap.put(target, source);
                // Add the copy-to target to conreflist when its source has conref
                if (conrefSet.contains(source)) {
                    conrefSet.add(target);
                }
                if (keyrefSet.contains(source)) {
                    keyrefSet.add(target);
                }
            }
        }

        copytoMap = tempMap;

        // Add copy-to targets into fullTopicSet
        fullTopicSet.addAll(copytoMap.keySet());

        // Get pure copy-to sources
        totalCopytoSources.addAll(copytoMap.values());
        totalCopytoSources.addAll(ignoredCopytoSourceSet);
        for (final URI src: totalCopytoSources) {
            if (!nonConrefCopytoTargetSet.contains(src) && !copytoMap.keySet().contains(src)) {
                pureCopytoSources.add(src);
            }
        }

        copytoSourceSet = pureCopytoSources;

        // Remove pure copy-to sources from fullTopicSet
        fullTopicSet.removeAll(pureCopytoSources);
    }

    /**
     * Handle topic which are only conref sources from normal processing.
     */
    private void handleConref() {
        // Get pure conref targets
        final Set<URI> pureConrefTargets = new HashSet<URI>(128);
        for (final URI target: conrefTargetSet) {
            if (!nonConrefCopytoTargetSet.contains(target)) {
                pureConrefTargets.add(target);
            }
        }
        conrefTargetSet = pureConrefTargets;

        // Remove pure conref targets from fullTopicSet
        fullTopicSet.removeAll(pureConrefTargets);
    }

    /**
     * Write result files.
     * 
     * @throws DITAOTException if writing result files failed
     */
    private void outputResult() throws DITAOTException {
        tempFileNameScheme = new DefaultTempFileScheme(baseInputDir.toURI());

        if (!job.tempDir.exists() && !job.tempDir.mkdirs()) {
            throw new DITAOTException("Failed to create " + job.tempDir + " directory");
        }
        
        // assume empty Job
        final URI rootTemp = tempFileNameScheme.generateTempFileName(rootFile);
        final File relativeRootFile = toFile(rootTemp);

        job.setProperty(INPUT_DIR, baseInputDir.toString());
        job.setProperty(INPUT_DITAMAP, relativeRootFile.toString());

        job.setProperty(INPUT_DITAMAP_LIST_FILE_LIST, USER_INPUT_FILE_LIST_FILE);
        final File inputfile = new File(job.tempDir, USER_INPUT_FILE_LIST_FILE);
        writeListFile(inputfile, relativeRootFile.toString());

        job.setProperty("tempdirToinputmapdir.relative.value", escapeRegExp(getPrefix(relativeRootFile)));
        job.setProperty("uplevels", getLevelsPath(rootTemp));

        for (final URI file: outDitaFilesSet) {
            getOrCreateFileInfo(fileinfos, file).isOutDita = true;
        }
//        // XXX: This loop is probably redundant
//        for (FileInfo f: prop.getFileInfo().values()) {
//            if (ATTR_FORMAT_VALUE_DITA.equals(f.format) || ATTR_FORMAT_VALUE_DITAMAP.equals(f.format)) {
//                f.isActive = false;
//            }
//        }
        for (final URI file: fullTopicSet) {
            final FileInfo ff = getOrCreateFileInfo(fileinfos, file);
            if (ff.format == null) {
                ff.format = ATTR_FORMAT_VALUE_DITA;
            }
        }
        for (final URI file: fullMapSet) {
            final FileInfo ff = getOrCreateFileInfo(fileinfos, file);
            if (ff.format == null) {
                ff.format = ATTR_FORMAT_VALUE_DITAMAP;
            }
        }
        for (final URI file: hrefTopicSet) {
            getOrCreateFileInfo(fileinfos, file).hasLink = true;
        }
        for (final URI file: conrefSet) {
            getOrCreateFileInfo(fileinfos, file).hasConref = true;
        }
        for (final URI file: imageSet) {
            getOrCreateFileInfo(fileinfos, file).format = ATTR_FORMAT_VALUE_IMAGE;
        }
        for (final URI file: flagImageSet) {
            final FileInfo f = getOrCreateFileInfo(fileinfos, file);
            f.isFlagImage = true;
            f.format = ATTR_FORMAT_VALUE_IMAGE;
        }
        for (final URI file: htmlSet) {
            getOrCreateFileInfo(fileinfos, file).format = ATTR_FORMAT_VALUE_HTML;
        }
        for (final URI file: hrefTargetSet) {
            getOrCreateFileInfo(fileinfos, file).isTarget = true;
        }
        for (final URI file: hrefWithIDSet) {
            getOrCreateFileInfo(fileinfos, file).isNonConrefTarget = true;
        }
        for (final URI file: chunkTopicSet) {
            getOrCreateFileInfo(fileinfos, file).isSkipChunk = true;
        }
        for (final URI file: schemeSet) {
            getOrCreateFileInfo(fileinfos, file).isSubjectScheme = true;
        }
        for (final URI file: conrefTargetSet) {
            getOrCreateFileInfo(fileinfos, file).isConrefTarget = true;
        }
        for (final URI file: copytoSourceSet) {
            getOrCreateFileInfo(fileinfos, file).isCopyToSource = true;
        }
        for (final URI file: subsidiarySet) {
            final FileInfo f = getOrCreateFileInfo(fileinfos, file);
            f.isSubtarget = true;
            if (f.format == null) {
                f.format = PR_D_CODEREF.localName;
            }
        }
        for (final URI file: conrefpushSet) {
            getOrCreateFileInfo(fileinfos, file).isConrefPush = true;
        }
        for (final URI file: keyrefSet) {
            getOrCreateFileInfo(fileinfos, file).hasKeyref = true;
        }
        for (final URI file: coderefSet) {
            getOrCreateFileInfo(fileinfos, file).hasCoderef = true;
        }
        for (final URI file: resourceOnlySet) {
            getOrCreateFileInfo(fileinfos, file).isResourceOnly = true;
        }
        
        addFlagImagesSetToProperties(job, relFlagImagesSet);

        for (final FileInfo fs: fileinfos.values()) {
            if (!failureList.contains(fs.src)) {
                job.add(fs);
            }
        }

        // Convert copyto map into set and output
        job.setCopytoMap(addFilePrefix(copytoMap));
        addKeyDefSetToProperties(job, keysDefMap);

        try {
            logger.info("Serializing job specification");
            job.write();
        } catch (final IOException e) {
            throw new DITAOTException("Failed to serialize job configuration files: " + e.getMessage(), e);
        }

        try {
            // Output relation-graph
            SubjectSchemeReader.writeMapToXML(addMapFilePrefix(listFilter.getRelationshipGrap()), new File(job.tempDir, FILE_NAME_SUBJECT_RELATION));
            // Output topic-scheme dictionary
            SubjectSchemeReader.writeMapToXML(addMapFilePrefix(schemeDictionary), new File(job.tempDir, FILE_NAME_SUBJECT_DICTIONARY));
        } catch (final IOException e) {
            throw new DITAOTException(e);
        }

        writeExportAnchors();

        KeyDef.writeKeydef(new File(job.tempDir, SUBJECT_SCHEME_KEYDEF_LIST_FILE), addFilePrefix(schemekeydefMap.values()));
    }

    /**
     * Write list file.
     * @param inputfile output list file
     * @param relativeRootFile list value
     */
    private void writeListFile(final File inputfile, final String relativeRootFile) {
        Writer bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(inputfile)));
            bufferedWriter.write(relativeRootFile);
            bufferedWriter.flush();
        } catch (final FileNotFoundException e) {
            logger.error(e.getMessage(), e) ;
        } catch (final IOException e) {
            logger.error(e.getMessage(), e) ;
        } finally {
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                } catch (final IOException e) {
                    logger.error(e.getMessage(), e) ;
                }
            }
        }
    }

    /**
     * Prefix path.
     *
     * @param relativeRootFile relative path for root temporary file
     * @return either an empty string or a path which ends in {@link java.io.File#separator File.separator}
     * */
    private String getPrefix(final File relativeRootFile) {
        String res;
        final File p = relativeRootFile.getParentFile();
        if (p != null) {
            res = p.toString() + File.separator;
        } else {
            res = "";
        }
        return res;
    }

    private FileInfo getOrCreateFileInfo(final Map<URI, FileInfo> fileInfos, final URI file) {
        assert file.getFragment() == null;
        final URI f = file.normalize();
        FileInfo.Builder b;
        if (fileInfos.containsKey(f)) {
            b = new FileInfo.Builder(fileInfos.get(f));
        } else {
            b = new FileInfo.Builder().src(file);
        }
        b = b.uri(tempFileNameScheme.generateTempFileName(file));
        final FileInfo i = b.build();
        fileInfos.put(i.src, i);
        return i;
    }

    private void writeExportAnchors() throws DITAOTException {
        if (INDEX_TYPE_ECLIPSEHELP.equals(transtype)) {
            // Output plugin id
            final File pluginIdFile = new File(job.tempDir, FILE_NAME_PLUGIN_XML);
            final DelayConrefUtils delayConrefUtils = new DelayConrefUtils();
            delayConrefUtils.writeMapToXML(exportAnchorsFilter.getPluginMap(), pluginIdFile);
            OutputStream exportStream = null;
            XMLStreamWriter export = null;
            try {
                exportStream = new FileOutputStream(new File(job.tempDir, FILE_NAME_EXPORT_XML));
                export = XMLOutputFactory.newInstance().createXMLStreamWriter(exportStream, "UTF-8");
                export.writeStartDocument();
                export.writeStartElement("stub");
                for (final ExportAnchor e: exportAnchorsFilter.getExportAnchors()) {
                    export.writeStartElement("file");
                    export.writeAttribute("name", tempFileNameScheme.generateTempFileName(toFile(e.file).toURI()).toString());
                    for (final String t: sort(e.topicids)) {
                        export.writeStartElement("topicid");
                        export.writeAttribute("name", t);
                        export.writeEndElement();
                    }
                    for (final String i: sort(e.ids)) {
                        export.writeStartElement("id");
                        export.writeAttribute("name", i);
                        export.writeEndElement();
                    }
                    for (final String k: sort(e.keys)) {
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
                        logger.error("Failed to close export anchor file: " + e.getMessage(), e);
                    }
                }
                if (exportStream != null) {
                    try {
                        exportStream.close();
                    } catch (final IOException e) {
                        logger.error("Failed to close export anchor file: " + e.getMessage(), e);
                    }
                }
            }
        }
    }

    private List<String> sort(final Set<String> set) {
        final List<String> sorted = new ArrayList<String>(set);
        Collections.sort(sorted);
        return sorted;
    }

    /**
     * Convert absolute paths to relative temporary directory paths
     * @return map with relative keys and values
     */
    private Map<URI, Set<URI>> addMapFilePrefix(final Map<URI, Set<URI>> map) {
        final Map<URI, Set<URI>> res = new HashMap<URI, Set<URI>>();
        for (final Map.Entry<URI, Set<URI>> e: map.entrySet()) {
            final URI key = e.getKey();
            final Set<URI> newSet = new HashSet<URI>(e.getValue().size());
            for (final URI file: e.getValue()) {
                newSet.add(tempFileNameScheme.generateTempFileName(file));
            }
            res.put(key.equals(ROOT_URI) ? key : tempFileNameScheme.generateTempFileName(key), newSet);
        }
        return res;
    }


    
    /**
     * Add file prefix. For absolute paths the prefix is not added.
     * 
     * @param set file paths
     * @return file paths with prefix
     */
    private Map<URI, URI> addFilePrefix(final Map<URI, URI> set) {
        final Map<URI, URI> newSet = new HashMap<URI, URI>();
        for (final Map.Entry<URI, URI> file: set.entrySet()) {
            final URI key = tempFileNameScheme.generateTempFileName(file.getKey());
            final URI value = tempFileNameScheme.generateTempFileName(file.getValue());
            newSet.put(key, value);
        }
        return newSet;
    }

    private Collection<KeyDef> addFilePrefix(final Collection<KeyDef> keydefs) {
        final Collection<KeyDef> res = new ArrayList<KeyDef>(keydefs.size());
        for (final KeyDef k: keydefs) {
            final URI source = tempFileNameScheme.generateTempFileName(k.source);
            res.add(new KeyDef(k.keys, k.href, k.scope, source));
        }
        return res;
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
            final URI href = (file.href != null && ATTR_SCOPE_VALUE_LOCAL.equals(file.scope))
                             ? tempFileNameScheme.generateTempFileName(file.href)
                             : file.href;
            final URI source = tempFileNameScheme.generateTempFileName(file.source);
            final KeyDef keyDef = new KeyDef(keys, href, file.scope, source);
            updated.add(keyDef);
        }
        // write key definition
        try {
            KeyDef.writeKeydef(new File(job.tempDir, KEYDEF_LIST_FILE), updated);
        } catch (final DITAOTException e) {
            logger.error("Failed to write key definition file: " + e.getMessage(), e);
        }
    }

    /**
     * add FlagImangesSet to Properties, which needn't to change the dir level,
     * just ouput to the ouput dir.
     *
     * @param prop job configuration
     * @param set absolute flag image files
     */
    private void addFlagImagesSetToProperties(final Job prop, final Set<URI> set) {
        final Set<URI> newSet = new LinkedHashSet<URI>(128);
        for (final URI file: set) {
//            assert file.isAbsolute();
            if (file.isAbsolute()) {
                // no need to append relative path before absolute paths
                newSet.add(file.normalize());
            } else {
                // In ant, all the file separator should be slash, so we need to
                // replace all the back slash with slash.
                newSet.add(file.normalize());
            }
        }

        // write list attribute to file
        final String fileKey = org.dita.dost.util.Constants.REL_FLAGIMAGE_LIST.substring(0, org.dita.dost.util.Constants.REL_FLAGIMAGE_LIST.lastIndexOf("list")) + "file";
        prop.setProperty(fileKey, org.dita.dost.util.Constants.REL_FLAGIMAGE_LIST.substring(0, org.dita.dost.util.Constants.REL_FLAGIMAGE_LIST.lastIndexOf("list")) + ".list");
        final File list = new File(job.tempDir, prop.getProperty(fileKey));
        Writer bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(list)));
            final Iterator<URI> it = newSet.iterator();
            while (it.hasNext()) {
                bufferedWriter.write(it.next().getPath());
                if (it.hasNext()) {
                    bufferedWriter.write("\n");
                }
            }
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (final FileNotFoundException e) {
            logger.error(e.getMessage(), e) ;
        } catch (final IOException e) {
            logger.error(e.getMessage(), e) ;
        } finally {
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                } catch (final IOException e) {
                    logger.error(e.getMessage(), e) ;
                }
            }
        }

        prop.setProperty(org.dita.dost.util.Constants.REL_FLAGIMAGE_LIST, StringUtils.join(newSet, COMMA));
    }

    /**
     * Temporary file name generator.
     */
    public static interface TempFileNameScheme {
        /**
         * Generate temporary file name.
         *
         * @param src absolute source file URI
         * @return relative temporary file URI
         */
        public URI generateTempFileName(final URI src);
    }

    public static class DefaultTempFileScheme implements TempFileNameScheme {
        final URI b;
        public DefaultTempFileScheme(final URI b) {
            this.b = b;
        }
        public URI generateTempFileName(final URI src) {
            assert src.isAbsolute();
            //final URI b = baseInputDir.toURI();
            final URI rel = toURI(b.relativize(src).toString());
            return rel;
        }
    }

}