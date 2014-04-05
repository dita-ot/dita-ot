/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */
package org.dita.dost.module;

import static java.util.Arrays.asList;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.Configuration.*;
import static org.dita.dost.util.Job.*;
import static org.dita.dost.util.URLUtils.*;
import static org.dita.dost.writer.GenListModuleFilter.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.xerces.xni.grammars.XMLGrammarPool;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.exception.DITAOTXMLErrorHandler;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.reader.DitaValReader;
import org.dita.dost.reader.GrammarPoolManager;
import org.dita.dost.reader.KeydefFilter;
import org.dita.dost.util.CatalogUtils;
import org.dita.dost.util.Configuration;
import org.dita.dost.util.DelayConrefUtils;
import org.dita.dost.util.FileUtils;
import org.dita.dost.util.FilterUtils;
import org.dita.dost.util.Job;
import org.dita.dost.util.Job.FileInfo;
import org.dita.dost.util.Job.FileInfo.Builder;
import org.dita.dost.util.KeyDef;
import org.dita.dost.util.StringUtils;
import org.dita.dost.util.URLUtils;
import org.dita.dost.writer.DebugFilter;
import org.dita.dost.writer.DitaWriter;
import org.dita.dost.writer.ExportAnchorsFilter;
import org.dita.dost.writer.ExportAnchorsFilter.ExportAnchor;
import org.dita.dost.writer.GenListModuleFilter;
import org.dita.dost.writer.GenListModuleFilter.Reference;
import org.dita.dost.writer.NormalizeFilter;
import org.dita.dost.writer.ProfilingFilter;
import org.dita.dost.writer.ValidationFilter;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * Walk through input files and normalize them into the temporary directory. 
 */
public final class GenMapAndTopicListDebugAndFilterModule extends AbstractPipelineModuleImpl {

    // Constants

    public static final String PARAM_INPUTMAP_URI = "inputmap.uri";
    public static final String PARAM_INPUTDIR_URI = "inputdir.uri";
    
    // Variables

    /** File info map. */
    private final Map<String, FileInfo.Builder> fileInfoMap = new HashMap<String, FileInfo.Builder>();
    /** Set of all dita files */
    private final Set<File> ditaSet = new HashSet<File>();
    /** Set of all topic files */
    private final Set<File> fullTopicSet = new HashSet<File>();
    /** Set of href topic files with anchor ID */
    private final Set<File> hrefWithIDSet = new HashSet<File>();
    /** Set of all images */
    private final Set<File> imageSet = new HashSet<File>();
    /** Set of all images used for flagging */
    private final Set<File> flagImageSet = new HashSet<File>();
    /** Set of all html files */
    private final Set<File> htmlSet = new HashSet<File>();
    /** Set of all the copy-to sources */
    private Set<File> copytoSourceSet = new HashSet<File>();
    /** Set of all the non-conref targets */
    private final Set<File> nonConrefCopytoTargetSet = new HashSet<File>();
    /** Set of sources of those copy-to that were ignored */
    private final Set<File> ignoredCopytoSourceSet = new HashSet<File>();
    /** Set of relative flag image files */
    private final Set<File> relFlagImagesSet = new HashSet<File>();
    /** Map of all copy-to (target,source) */
    private Map<File, File> copytoMap = new HashMap<File, File>();
    /** List of files waiting for parsing. Values are relative system paths. */
    private final Queue<URI> waitList = new ConcurrentLinkedQueue<URI>();
    /** List of parsed files */
    private final Queue<URI> doneList = new ConcurrentLinkedQueue<URI>();
    /** Set of outer dita files */
    private final Set<File> outDitaFilesSet = new HashSet<File>();
    /** Set of files with "@processing-role=resource-only" */
    private final Set<File> resourceOnlySet = new HashSet<File>();
    /** Map of all key definitions */
    private final Map<String, KeyDef> keysDefMap = new HashMap<String, KeyDef>();
    /** Absolute basedir for processing */
    private URI baseInputDir;
    /** Absolute ditadir for processing */
    private File ditaDir;
    /** Relative input file */
    private URI inputFile;
    /** Absolute path for filter file. */
    private File ditavalFile;
    /** Prefix path. Either an empty string or a path which ends in {@link java.io.File#separator File.separator}. */
    private final String prefix = "";
    private GenListModuleFilter listFilter;
    private KeydefFilter keydefFilter;
    private ExportAnchorsFilter exportAnchorsFilter;
    private boolean xmlValidate = true;
    /** Absolute path to input file. */
    private URI rootFile;
    /** File currently being processed */
    private URI currentFile;
    private String transtype;
    /** use grammar pool cache */
    private boolean gramcache = true;
    /** Generate {@code xtrf} and {@code xtrc} attributes */
    private final boolean genDebugInfo = Boolean.parseBoolean(Configuration.configuration.get("generate-debug-attributes"));
    //private boolean setSystemid = true;
    private FilterUtils filterUtils = new FilterUtils();
    /** XMLReader instance for parsing dita file */
    private XMLReader reader;

    // Constructor

    @Override
    public AbstractPipelineOutput execute(final AbstractPipelineInput input) throws DITAOTException {
        if (logger == null) {
            throw new IllegalStateException("Logger not set");
        }
        try {
            parseInputParameters(input);
            
            initFilters();
            initXMLReader(ditaDir, xmlValidate, rootFile);
            
            addToWaitList(inputFile);
            processWaitList();
            
            handleConref();
            //handleCopyto();
            outputResult();

//            if (true) return null;
//
//            // debug and filter
//            final String baseDir = input.getAttribute(ANT_INVOKER_PARAM_BASEDIR);
//            tempDir = new File(input.getAttribute(ANT_INVOKER_PARAM_TEMPDIR));
//            if (!tempDir.isAbsolute()) {
//                throw new IllegalArgumentException("Temporary directory " + tempDir + " must be absolute");
//            }
//            ditaDir=new File(input.getAttribute(ANT_INVOKER_EXT_PARAM_DITADIR));
//            final String transtype = input.getAttribute(ANT_INVOKER_EXT_PARAM_TRANSTYPE);
//            File ditavalFile = null;
//            if (input.getAttribute(ANT_INVOKER_PARAM_DITAVAL) != null ) {
//                ditavalFile = new File(input.getAttribute(ANT_INVOKER_PARAM_DITAVAL));
//                if (!ditavalFile.isAbsolute()) {
//                    ditavalFile = new File(baseDir, ditavalFile.getPath()).getAbsoluteFile();
//                }
//            }
//
//            final Job job = new Job(tempDir);
//
//            final Set<String> parseList = new HashSet<String>();
//            parseList.addAll(job.getSet(FULL_DITAMAP_TOPIC_LIST));
//            parseList.addAll(job.getSet(CONREF_TARGET_LIST));
//            parseList.addAll(job.getSet(COPYTO_SOURCE_LIST));
//            inputDir = new File(job.getInputDir());
//            if (!inputDir.isAbsolute()) {
//                inputDir = new File(baseDir, inputDir.getPath()).getAbsoluteFile();
//            }
//            inputMap = new File(inputDir, job.getInputMap()).getAbsoluteFile();
//
//            // Output subject schemas
//            outputSubjectScheme();
//
//            final DitaWriter fileWriter = new DitaWriter();
//            fileWriter.setLogger(logger);
//            try{
//                final boolean xmlValidate = Boolean.valueOf(input.getAttribute("validate"));
//                fileWriter.initXMLReader(ditaDir.getAbsoluteFile(),xmlValidate, setSystemid);
//            } catch (final SAXException e) {
//                throw new DITAOTException(e.getMessage(), e);
//            }
//            fileWriter.setTempDir(tempDir);
//            fileWriter.setExtName(extName);
//            fileWriter.setTranstype(transtype);
//            if (filterUtils != null) {
//                fileWriter.setFilterUtils(filterUtils);
//            }
//            fileWriter.setDelayConrefUtils(new DelayConrefUtils());
//            fileWriter.setKeyDefinitions(GenMapAndTopicListModule.readKeydef(new File(tempDir, KEYDEF_LIST_FILE)));
//
//            job.setGeneratecopyouter(input.getAttribute(ANT_INVOKER_EXT_PARAM_GENERATECOPYOUTTER));
//            job.setOutterControl(input.getAttribute(ANT_INVOKER_EXT_PARAM_OUTTERCONTROL));
//            job.setOnlyTopicInMap(input.getAttribute(ANT_INVOKER_EXT_PARAM_ONLYTOPICINMAP));
//            job.setInputMapPathName(inputMap);
//            job.setOutputDir(new File(input.getAttribute(ANT_INVOKER_EXT_PARAM_OUTPUTDIR)));
//            fileWriter.setOutputUtils(outputUtils);
//
//            for (final String filename: parseList) {
//                final File currentFile = new File(inputDir, filename);
//                logger.logInfo("Processing " + currentFile.getAbsolutePath());
//
//                fileWriter.setFilterUtils(filterUtils);
//                if (!new File(inputDir, filename).exists()) {
//                    // This is an copy-to target file, ignore it
//                    logger.logInfo("Ignoring a copy-to file " + filename);
//                    continue;
//                }
//
//                fileWriter.write(inputDir, filename);
//            }
//
//            if (extName != null) {
//                updateList(tempDir);
//            }
//            //update dictionary.
//            updateDictionary(tempDir);
            // reload the property for processing of copy-to
//            try {
//                logger.logInfo("Serializing job specification");
//                prop.write();
//                prop = new Job(tempDir);
//            } catch (final IOException e) {
//                throw new DITAOTException("Failed to serialize job configuration files: " + e.getMessage(), e);
//            }
            performCopytoTask();
            
            try {
                logger.info("Serializing job specification");
                job.write();
            } catch (final IOException e) {
                throw new DITAOTException("Failed to serialize job configuration files: " + e.getMessage(), e);
            }
        } catch (final Exception e) {
            e.printStackTrace();
            throw new DITAOTException("Exception during debug and filter module processing: " + e.getMessage(), e);
        }

        return null;
    }

    /**
     * Initialize reusable filters.
     */
    private void initFilters() {
        filterUtils = new FilterUtils(printTranstype.contains(transtype));
        filterUtils.setLogger(logger);
        if (ditavalFile != null) {
            final DitaValReader ditaValReader = new DitaValReader();
            ditaValReader.setLogger(logger);
            ditaValReader.initXMLReader(true/*setSystemid*/);
            ditaValReader.read(ditavalFile.getAbsoluteFile());
            // Store filter map for later use
            filterUtils.setFilterMap(ditaValReader.getFilterMap());
            // Store flagging image used for image copying
            flagImageSet.addAll(ditaValReader.getImageList());
            relFlagImagesSet.addAll(ditaValReader.getRelFlagImageList());
        } else {
            filterUtils.setFilterMap(Collections.EMPTY_MAP);
        }
        
        listFilter = new GenListModuleFilter();
        listFilter.setLogger(logger);
        listFilter.setInputFile(rootFile);
        listFilter.setInputDir(baseInputDir);//rootFile.getParentFile()
        listFilter.setJob(job);
        listFilter.setTempDir(job.tempDir);
        listFilter.isStartDocument(true);
        
        keydefFilter = new KeydefFilter();
        keydefFilter.setLogger(logger);
        keydefFilter.setInputFile(rootFile);
        keydefFilter.setJob(job);
        
        exportAnchorsFilter = new ExportAnchorsFilter();
        exportAnchorsFilter.setInputFile(rootFile);
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
    private void initXMLReader(final File ditaDir, final boolean validate, final URI rootFile) throws SAXException {
        reader = StringUtils.getXMLReader();
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
        // set grammar pool flag
        if (gramcache) {
            GrammarPoolManager.setGramCache(gramcache);
            final XMLGrammarPool grammarPool = GrammarPoolManager.getGrammarPool();
            try {
                reader.setProperty("http://apache.org/xml/properties/internal/grammar-pool", grammarPool);
                logger.info("Using Xerces grammar pool for DTD and schema caching.");
            } catch (final Exception e) {
                logger.warn("Failed to set Xerces grammar pool for parser: " + e.getMessage());
            }
        }
        CatalogUtils.setDitaDir(ditaDir);
        reader.setEntityResolver(CatalogUtils.getCatalogResolver());
    }
    
    private void parseInputParameters(final AbstractPipelineInput input) throws IOException, URISyntaxException {
        ditaDir = new File(input.getAttribute(ANT_INVOKER_EXT_PARAM_DITADIR));
        if (!ditaDir.isAbsolute()) {
            throw new IllegalArgumentException("DITA-OT installation directory " + ditaDir + " must be absolute");
        }
        ditaDir = ditaDir.getCanonicalFile();
        if (input.getAttribute(ANT_INVOKER_PARAM_DITAVAL) != null) {
            ditavalFile = new File(input.getAttribute(ANT_INVOKER_PARAM_DITAVAL));
            if (!ditavalFile.isAbsolute()) {
                throw new IllegalArgumentException(ANT_INVOKER_PARAM_DITAVAL + " ditavalFile " + ditavalFile + " must be absolute");
            }
        }
        
        xmlValidate = Boolean.valueOf(input.getAttribute(ANT_INVOKER_EXT_PARAM_VALIDATE));

        // get transtype
        transtype = input.getAttribute(ANT_INVOKER_EXT_PARAM_TRANSTYPE);

        gramcache = "yes".equalsIgnoreCase(input.getAttribute(ANT_INVOKER_EXT_PARAM_GRAMCACHE));
        //setSystemid = "yes".equalsIgnoreCase(input.getAttribute(ANT_INVOKER_EXT_PARAN_SETSYSTEMID));

        // For the output control
        job.setGeneratecopyouter(input.getAttribute(ANT_INVOKER_EXT_PARAM_GENERATECOPYOUTTER));
        job.setOutterControl(input.getAttribute(ANT_INVOKER_EXT_PARAM_OUTTERCONTROL));
        job.setOnlyTopicInMap(input.getAttribute(ANT_INVOKER_EXT_PARAM_ONLYTOPICINMAP));

        // Set the OutputDir
        final File path = new File(input.getAttribute(ANT_INVOKER_EXT_PARAM_OUTPUTDIR));
        if (!path.isAbsolute()) {
            throw new IllegalArgumentException("Output directory " + path + " must be absolute");
        }
        job.setOutputDir(path);

        final String ditaInputDirUri = input.getAttribute(PARAM_INPUTDIR_URI);
        if (ditaInputDirUri != null) {
            final URI inDir = new URI(ditaInputDirUri + URI_SEPARATOR);
            if (!isAbsolute(inDir)) {
                throw new IllegalArgumentException(PARAM_INPUTDIR_URI + " must be absolute: " + ditaInputDirUri);
            }
            baseInputDir = inDir.normalize();
        } else {
            final String ditaInputDir = input.getAttribute(ANT_INVOKER_EXT_PARAM_INPUTDIR);
            if (ditaInputDir != null) {
                final File inDir = new File(ditaInputDir);
                if (!inDir.isAbsolute()) {
                    throw new IllegalArgumentException(ANT_INVOKER_EXT_PARAM_INPUTDIR + " must be absolute: " + ditaInputDir);
                }
                baseInputDir = new URI(inDir.toURI() + URI_SEPARATOR).normalize();
            }
        }

        URI inUri = null;
        final String ditaInputUri = input.getAttribute(PARAM_INPUTMAP_URI);
        if (ditaInputUri != null) {
            inUri = new URI(ditaInputUri);
            if (!isAbsolute(inUri)) {
                if (baseInputDir != null) {
                    inUri = baseInputDir.resolve(ditaInputUri);
                } else {
                    throw new IllegalArgumentException("Relative " + PARAM_INPUTMAP_URI + " must be defined with " + PARAM_INPUTDIR_URI + ": " + ditaInputUri);
                }
            }
        } else {
            final String ditaInput = input.getAttribute(ANT_INVOKER_PARAM_INPUTMAP);
            File inFile = new File(ditaInput);
            if (!inFile.isAbsolute()) {
                if (baseInputDir != null) {
                    inFile = new File(new File(baseInputDir), inFile.getPath());
                } else {
                    throw new IllegalArgumentException("Relative " + ANT_INVOKER_PARAM_INPUTMAP + " must be defined with " + ANT_INVOKER_EXT_PARAM_INPUTDIR + ": " + ditaInput);
                }
            }
            inUri = inFile.toURI();
        }
        inUri = inUri.normalize();
        if (baseInputDir == null) {
            baseInputDir = inUri.resolve(".").normalize();
        }

        rootFile = inUri;

        inputFile = baseInputDir.relativize(inUri);

        // Set the mapDir
        job.setInputFile(toFile(inUri));
    }

    /**
     * Process input file in processing queue.
     * 
     * @throws DITAOTException if processing file failed
     */
    private void processWaitList() throws DITAOTException {
        while ((currentFile = waitList.poll()) != null) {
            processFile(currentFile);
        }
    }

    /**
     * Read a file and process it for list information.
     * 
     * @param currentFile relative URI of the file to process
     * @throws DITAOTException if processing failed
     */
    private void processFile(final URI currentFile) throws DITAOTException {
        final URI fileToParse = baseInputDir.resolve(currentFile.getPath()).normalize();
        logger.info("Processing " + fileToParse);
        final String[] params = { fileToParse.toString() };

        if ("file".equals(fileToParse.getScheme()) &&  !new File(fileToParse).exists()) {
            logger.error(MessageUtils.getInstance().getMessage("DOTX008E", params).toString());
            return;
        }
        boolean isValidInput = false;
        try {
            XMLReader xmlSource = reader;
            for (final XMLFilter f: getProcessingPipe(fileToParse)) {
                f.setParent(xmlSource);
                xmlSource = f;
            }
            // ContentHandler must be reset so e.g. Saxon 9.1 will reassign ContentHandler
            // when reusing filter with multiple Transformers.
            xmlSource.setContentHandler(null);
            xmlSource.setEntityResolver(CatalogUtils.getCatalogResolver());
            
            final Source source = new SAXSource(xmlSource, new InputSource(fileToParse.toString()));
            final File outFile = new File(job.tempDir, toFile(currentFile).getPath());
            if (!outFile.getParentFile().exists() && !outFile.getParentFile().mkdirs()) {
                throw new IOException("Failed to create temporary directory " + outFile.getParentFile().getAbsolutePath());
            }
            logger.debug("Writing " + outFile.getAbsolutePath());
            final Result result = new StreamResult(outFile);
            final TransformerFactory tf = TransformerFactory.newInstance();
            tf.setURIResolver(CatalogUtils.getCatalogResolver());
            final Transformer serializer = tf.newTransformer();
            serializer.transform(source, result);

            processParseResult();

            isValidInput = true;
        } catch (final TransformerException sax) {
            final Throwable inner = sax.getException();
            if (inner != null && inner instanceof DITAOTException) {
                logger.info(inner.getMessage());
                throw (DITAOTException) inner;
            }
            if (currentFile.equals(inputFile)) {
                final String msg = MessageUtils.getInstance().getMessage("DOTJ012F", params).toString() + ": " + sax.getMessage();
                throw new DITAOTException(MessageUtils.getInstance().getMessage("DOTJ012F", params), sax, msg);
            }
            final String buff = MessageUtils.getInstance().getMessage("DOTJ013E", params).toString() + LINE_SEPARATOR + sax.getMessage();
            logger.error(buff);
        } catch (final Throwable e) {
            if (currentFile.equals(inputFile)) {
                final String msg = MessageUtils.getInstance().getMessage("DOTJ012F", params).toString() + ": " + e.getMessage();
                throw new DITAOTException(MessageUtils.getInstance().getMessage("DOTJ012F", params), e, msg);
            }
            final String buff = MessageUtils.getInstance().getMessage("DOTJ013E", params).toString() + LINE_SEPARATOR + e.getMessage();
            e.printStackTrace();
            logger.error(buff);
        }

        if (!isValidInput && currentFile.equals(inputFile)) {
            if (xmlValidate) {
                throw new DITAOTException(MessageUtils.getInstance().getMessage("DOTJ022F", params).toString());
            } else {
                throw new DITAOTException(MessageUtils.getInstance().getMessage("DOTJ034F", params).toString());
            }
        }

        doneList.add(currentFile);
        listFilter.reset();
        keydefFilter.reset();
    }
    
    /**
     * Get pipe line filters
     * 
     * @param fileToParse absolute URI to current file being processed
     */
    private List<XMLFilter> getProcessingPipe(final URI fileToParse) {
        final List<XMLFilter> pipe = new ArrayList<XMLFilter>();
        if (genDebugInfo) {
            final DebugFilter debugFilter = new DebugFilter();
            debugFilter.setLogger(logger);
            debugFilter.setInputFile(fileToParse);
            pipe.add(debugFilter);
        }
        if (filterUtils != null) {
            final ProfilingFilter profilingFilter = new ProfilingFilter();
            profilingFilter.setLogger(logger);
            profilingFilter.setFilterUtils(filterUtils);
            pipe.add(profilingFilter);
        }
        {
            final ValidationFilter validationFilter = new ValidationFilter();
            validationFilter.setLogger(logger);
            // SS not supported
            //validationFilter.setValidateMap(validateMap);
            pipe.add(validationFilter);
        }
        {
            final NormalizeFilter normalizeFilter = new NormalizeFilter();
            normalizeFilter.setLogger(logger);
            pipe.add(normalizeFilter);
        }
        if (INDEX_TYPE_ECLIPSEHELP.equals(transtype)) {
            exportAnchorsFilter.setCurrentDir(toFile(currentFile.resolve(".")));
            exportAnchorsFilter.setCurrentFile(fileToParse);
            exportAnchorsFilter.setErrorHandler(new DITAOTXMLErrorHandler(fileToParse.toString(), logger));
            pipe.add(exportAnchorsFilter);
        }
        {
            keydefFilter.setCurrentDir(currentFile.resolve("."));
            keydefFilter.setErrorHandler(new DITAOTXMLErrorHandler(fileToParse.toString(), logger));
            pipe.add(keydefFilter);
        }
        {
//            listFilter.setTranstype(transtype);
            listFilter.setCurrentDir(currentFile.resolve("."));
            listFilter.setCurrentFile(fileToParse);
            listFilter.setErrorHandler(new DITAOTXMLErrorHandler(fileToParse.toString(), logger));
            pipe.add(listFilter);
        }
        return pipe;
    }

    private void processParseResult() {
        for (final FileInfo i: listFilter.getFileInfo()) {
            getOrCreateBuilder(i.file.getPath()).add(i);
        }

        final Map<File, File> cpMap = listFilter.getCopytoMap();
        final Map<String, KeyDef> kdMap = keydefFilter.getKeysDMap();
        // the reader's reset method will clear the map.

        // Category non-copyto result and update uplevels accordingly
        for (final Reference file: listFilter.getNonCopytoResult()) {
            if (isAccessible(new File(file.filename))) {
                categorizeResultFile(file);
//                updateUplevels(file.filename);
            }
        }

        // Update uplevels for copy-to targets, and store copy-to map.
        // Note: same key(target) copy-to will be ignored.
        for (final File key: cpMap.keySet()) {
            final File value = cpMap.get(key);

            if (copytoMap.containsKey(key)) {
                logger.warn(MessageUtils.getInstance().getMessage("DOTX065W", value.getPath(), key.getPath()).toString());
                ignoredCopytoSourceSet.add(value);
            } else {
                if (isAccessible(key)) {
//                    updateUplevels(key);
                    copytoMap.put(key, value);
                }
            }
        }

        // collect key definitions
        for (final String key: kdMap.keySet()) {
            // key and value.keys will differ when keydef is a redirect to another keydef
            final KeyDef value = kdMap.get(key);
            if (!keysDefMap.containsKey(key)) {
//                updateUplevels(key);
                // add the ditamap where it is defined.
                keysDefMap.put(key, new KeyDef(key, value.href, value.scope, currentFile));                
            }
        }

        nonConrefCopytoTargetSet.addAll(listFilter.getNonConrefCopytoTargets());
        ignoredCopytoSourceSet.addAll(listFilter.getIgnoredCopytoSourceSet());
        outDitaFilesSet.addAll(listFilter.getOutFilesSet());
        resourceOnlySet.addAll(listFilter.getResourceOnlySet());
        
        ditaSet.add(toFile(currentFile));
    }

    /**
     * Test whether file reference may be accessed. Files outside the input base directory are
     * not accessible.
     * 
     * @param file path relative to base directory
     * @return
     */
    private boolean isAccessible(final File file) {
        final URI f = baseInputDir.resolve(file.getPath());
        final boolean res = directoryContains(baseInputDir, f);
        if (!res) {
            logger.warn(MessageUtils.getInstance().getMessage("DOTJ036W",
                                                                 f.toString(),
                                                                 currentFile.toString()).toString());
        }
        return res;
    }

    /**
     * Get file info builder from file info map or create new file info into the map. 
     * 
     * @param uri file info path
     * @return existing or new file info
     */
    private Builder getOrCreateBuilder(final URI uri) {
        FileInfo.Builder b = fileInfoMap.get(toFile(uri).getPath());
        if (b == null) {
            b = new FileInfo.Builder().uri(uri);
            // FIXME: use URI as map key 
            fileInfoMap.put(toFile(uri).getPath(), b);
        }
        return b;
    }
    
    /**
     * Get file info builder from file info map or create new file info into the map. 
     * 
     * @param file file info path
     * @return existing or new file info
     */
    private Builder getOrCreateBuilder(final String file) {
        FileInfo.Builder b = fileInfoMap.get(file);
        if (b == null) {
            b = new FileInfo.Builder().file(new File(file));
            fileInfoMap.put(file, b);
        }
        return b;
    }

    /**
     * Categorize file.
     * 
     * @param file file system path with optional format
     */
    private void categorizeResultFile(final Reference file) {
        final String lcasefn = file.filename.toLowerCase();

        if (file.format == null || ATTR_FORMAT_VALUE_DITA.equals(file.format) || ATTR_FORMAT_VALUE_DITAMAP.equals(file.format)) {
            try {
                addToWaitList(new URI(file.filename));
            } catch (final URISyntaxException e) {
                logger.error(e.getMessage());
            }
        } else if (!FileUtils.isSupportedImageFile(lcasefn)) {
            // FIXME: Treating all non-image extensions as HTML/resource files is not correct if HTML/resource files
            //        are defined by the file extension. Correct behaviour would be to remove this else block.
            htmlSet.add(new File(file.filename));
        }
        if (FileUtils.isSupportedImageFile(lcasefn)) {
            imageSet.add(new File(file.filename));        	      	
            final URI image = baseInputDir.resolve(file.filename).normalize(); 
            if ("file".equals(image.getScheme()) && !new File(image).exists()){
                logger.warn(MessageUtils.getInstance().getMessage("DOTX008W", image.toString()).toString());
            }
        }

        if (FileUtils.isHTMLFile(lcasefn) || FileUtils.isResourceFile(lcasefn)) {
            htmlSet.add(new File(file.filename));
        }
    }

//    /**
//     * Update uplevels if needed. If the parameter contains a {@link org.dita.dost.util.Constants#STICK STICK}, it and
//     * anything following it is removed.
//     * 
//     * @param file file path
//     */
//    private void updateUplevels(final String file) {
//        String f = file;
//        if (f.contains(STICK)) {
//            f = f.substring(0, f.indexOf(STICK));
//        }
//
//        // for uplevels (../../)
//        // ".."-->"../"
//        final int lastIndex = FileUtils.separatorsToUnix(FileUtils.normalize(f))
//                .lastIndexOf("../");
//        if (lastIndex != -1) {
//            final int newUplevels = lastIndex / 3 + 1;
//            uplevels = newUplevels > uplevels ? newUplevels : uplevels;
//        }
//    }

    /**
     * Add the given file the wait list if it has not been parsed.
     * 
     * @param file relative URI
     */
    private void addToWaitList(final URI file) {
        if (doneList.contains(file) || waitList.contains(file) || file.equals(currentFile)) {
            return;
        }

        waitList.add(file);
    }

//    /**
//     * Update base directory based on uplevels.
//     */
//    private void updateBaseDirectory() {
//        for (int i = uplevels; i > 0; i--) {
//            final File file = baseInputDir;
//            baseInputDir = baseInputDir.getParentFile();
//            prefix = new StringBuffer(file.getName()).append(File.separator).append(prefix).toString();
//        }
//    }

    /**
     * Get up-levels relative path.
     * 
     * @return path to up-level, e.g. {@code ../../}
     */
    private String getUpdateLevels() {
//        int current = uplevels;
//        final StringBuffer buff = new StringBuffer();
//        while (current > 0) {
//            buff.append(".." + File.separator);
//            current--;
//        }
//        return buff.toString();
        return "";
    }

    /**
     * Escape regular expression special characters.
     * 
     * @param value input
     * @return input with regular expression special characters escaped
     */
    private String formatRelativeValue(final String value) {
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
     * Handle copy-to topics.
     */
    private void handleCopyto() {
        final Map<File, File> tempMap = new HashMap<File, File>();
        // Validate copy-to map, remove those without valid sources
        for (final File dst: copytoMap.keySet()) {
            final File src = copytoMap.get(dst);
            //if (new File(baseInputDir + File.separator + prefix, src).exists()) {
            if (job.getFileInfoMap().containsKey(dst.getPath())) {
                tempMap.put(dst, src);
                // Add the copy-to target to conreflist when its source has
                // conref
//                if (conrefSet.contains(src)) {
//                    conrefSet.add(dst);
//                }
                final FileInfo orig = job.getFileInfoMap().get(src.getPath());
                final FileInfo.Builder b = new FileInfo.Builder(orig);
                b.uri(toURI(dst));
                final FileInfo f = b.build();
                job.add(f);
            }
        }
        copytoMap = tempMap;

        // Add copy-to targets into ditaSet, fullTopicSet
        ditaSet.addAll(copytoMap.keySet());
        fullTopicSet.addAll(copytoMap.keySet());

        // Get pure copy-to sources
        final Set<File> totalCopytoSources = new HashSet<File>(128);
        totalCopytoSources.addAll(copytoMap.values());
        totalCopytoSources.addAll(ignoredCopytoSourceSet);
        final Set<File> pureCopytoSources = new HashSet<File>(128);
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
//        final Set<String> pureConrefTargets = new HashSet<String>(INT_128);
//        for (final String target: conrefTargetSet) {
//            if (!nonConrefCopytoTargetSet.contains(target)) {
//                pureConrefTargets.add(target);
//            }
//        }
//        conrefTargetSet = pureConrefTargets;
//
//        // Remove pure conref targets from ditaSet, fullTopicSet
//        ditaSet.removeAll(pureConrefTargets);
//        fullTopicSet.removeAll(pureConrefTargets);
    }

    /**
     * Write result files.
     * 
     * @throws DITAOTException if writing result files failed
     */
    private void outputResult() throws DITAOTException {
        final File dir = job.tempDir;
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // assume empty Job
        job.setProperty(INPUT_DIR, toFile(baseInputDir).getAbsolutePath());
        job.setProperty(INPUT_DITAMAP, prefix + inputFile);

        job.setProperty(INPUT_DITAMAP_LIST_FILE_LIST, USER_INPUT_FILE_LIST_FILE);
        writeListToFile(new File(job.tempDir, USER_INPUT_FILE_LIST_FILE), asList(prefix + inputFile));

        for (final FileInfo.Builder b: fileInfoMap.values()) {
            job.add(b.build());
        }
        
        handleCopyto();
        
        // add out.dita.files,tempdirToinputmapdir.relative.value to solve the
        // output problem
        job.setProperty("tempdirToinputmapdir.relative.value", formatRelativeValue(prefix));
        job.setProperty("uplevels", getUpdateLevels());
        for (final File file: addFilePrefix(outDitaFilesSet)) {
            job.getOrCreateFileInfo(file).isOutDita = true;
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
        for (final File file: addFilePrefix(hrefWithIDSet)) {
            job.getOrCreateFileInfo(file).isNonConrefTarget = true;
        }
        for (final File file: addFilePrefix(copytoSourceSet)) {
            job.getOrCreateFileInfo(file).isCopyToSource = true;
        }
        for (final File file: addFilePrefix(resourceOnlySet)) {
            job.getOrCreateFileInfo(file).isResourceOnly = true;
        }

        addFlagImagesSetToProperties(job, relFlagImagesSet);

        // Convert copyto map into set and output
        job.setCopytoMap(addPrefix(copytoMap));
        addKeyDefSetToProperties(job, keysDefMap);
        
        try {
            logger.info("Serializing job specification");
            job.write();
        } catch (final IOException e) {
            throw new DITAOTException("Failed to serialize job configuration files: " + e.getMessage(), e);
        }

        if (INDEX_TYPE_ECLIPSEHELP.equals(transtype)) {
            // Output plugin id
            final File pluginIdFile = new File(job.tempDir, FILE_NAME_PLUGIN_XML);
            final DelayConrefUtils delayConrefUtils = new DelayConrefUtils();
            delayConrefUtils.writeMapToXML(exportAnchorsFilter.getPluginMap(), pluginIdFile);
            OutputStream exportStream = null;
            XMLStreamWriter export = null;
            try {
                exportStream = new FileOutputStream(new File(job.tempDir, FILE_NAME_EXPORT_XML));
                export = XMLOutputFactory.newInstance().createXMLStreamWriter(exportStream);
                export.writeStartDocument();
                export.writeStartElement("stub");
                for (final ExportAnchor e: exportAnchorsFilter.getExportAnchors()) {
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

    /**
     * Write a list of strings to a file, each string per line.
     */
    private void writeListToFile(final File output, final Collection<? extends Object> list) {
        Writer bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output)));
            for(final Iterator<? extends Object> i = list.iterator(); i.hasNext();) {
                bufferedWriter.write(i.next().toString());
                if (i.hasNext()) {
                    bufferedWriter.write('\n');
                }
            }
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
                newSet.add(FileUtils.normalize(new File(prefix + file)));
            }
        }
        return newSet;
    }
    
    /**
     * Add file prefix. For absolute paths the prefix is no added.
     * 
     * @param map map of file paths
     * @return file path map with prefix
     */

    private Map<File, File> addPrefix(final Map<File, File> map) {
        final Map<File, File> newMap = new HashMap<File, File>(map.size());
        for (final Map.Entry<File, File> e: map.entrySet()) {
            File to = e.getKey();
            if (to.isAbsolute()) {
                to = FileUtils.normalize(to);
            } else {
                to = FileUtils.normalize(new File(prefix + to));
            }
            File source = e.getValue();
            if (source.isAbsolute()) {
                source = FileUtils.normalize(source);
            } else {
                source = FileUtils.normalize(new File(prefix + source));
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
                    if (ATTR_SCOPE_VALUE_LOCAL.equals(file.scope)) {
                        href = toURI(FileUtils.normalize(prefix + href.toString()));
                    }
                    source = toURI(FileUtils.normalize(prefix + source));
                }
            }
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
     * @param set relative flag image files
     */
    private void addFlagImagesSetToProperties(final Job prop, final Set<File> set) {
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
        final String fileKey = org.dita.dost.util.Constants.REL_FLAGIMAGE_LIST.substring(0, org.dita.dost.util.Constants.REL_FLAGIMAGE_LIST.lastIndexOf("list")) + "file";
        prop.setProperty(fileKey, org.dita.dost.util.Constants.REL_FLAGIMAGE_LIST.substring(0, org.dita.dost.util.Constants.REL_FLAGIMAGE_LIST.lastIndexOf("list")) + ".list");
        writeListToFile(new File(job.tempDir, prop.getProperty(fileKey)), newSet);

        prop.setProperty(org.dita.dost.util.Constants.REL_FLAGIMAGE_LIST, StringUtils.join(newSet, COMMA));
    }

//    /**
//     * Update property map.
//     *
//     * @param listName name of map to update
//     * @param property property to update
//     */
//    private void updatePropertyMap(final String listName, final Job property){
//        final Map<String, String> propValues = property.getMap(listName);
//        if (propValues == null || propValues.isEmpty()){
//            return;
//        }
//        final Map<String, String> result = new HashMap<String, String>();
//        for (final Map.Entry<String, String> e: propValues.entrySet()) {
//            String key = e.getKey();
//            String value = e.getValue();
//            result.put(key, value);
//        }
//        property.setMap(listName, result);
//    }

//    /**
//     * Update property set.
//     *
//     * @param listName name of set to update
//     * @param property property to update
//     */
//    private void updatePropertySet(final String listName, final Job property){
//        final Set<String> propValues = property.getSet(listName);
//        if (propValues == null || propValues.isEmpty()){
//            return;
//        }
//        final Set<String> result = new HashSet<String>(propValues.size());
//        for (final String file: propValues) {
//            String f = file;
//            result.add(f);
//        }
//        property.setSet(listName, result);
//    }

//    /**
//     * Update property value.
//     * 
//     * @param listName name of value to update
//     * @param property property to update
//     */
//    private void updatePropertyString(final String listName, final Job property){
//        String propValue = property.getProperty(listName);
//        if (propValue == null || propValue.trim().length() == 0){
//            return;
//        }
//        property.setProperty(listName, propValue);
//    }

//    /**
//     * Read a map from XML properties file. Values are split by {@link org.dita.dost.util.Constants#COMMA COMMA} into a set.
//     * 
//     * @param filename XML properties file path, relative to temporary directory
//     */
//    private Map<String, Set<String>> readMapFromXML(final String filename) {
//        final File inputFile = new File(job.tempDir, filename);
//        final Map<String, Set<String>> graph = new HashMap<String, Set<String>>();
//        if (!inputFile.exists()) {
//            return graph;
//        }
//        final Properties prop = new Properties();
//        FileInputStream in = null;
//        try {
//            in = new FileInputStream(inputFile);
//            prop.loadFromXML(in);
//            in.close();
//        } catch (final IOException e) {
//            logger.logError(e.getMessage(), e) ;
//        } finally {
//            if (in != null) {
//                try {
//                    in.close();
//                } catch (final IOException e) {
//                    logger.logError(e.getMessage(), e) ;
//                }
//            }
//        }
//
//        for (final Map.Entry<Object, Object> entry: prop.entrySet()) {
//            final String key = (String) entry.getKey();
//            final String value = (String) entry.getValue();
//            graph.put(key, StringUtils.restoreSet(value, COMMA));
//        }
//
//        return Collections.unmodifiableMap(graph);
//    }

//    /**
//     * Output subject schema file.
//     * 
//     * @throws DITAOTException if generation files
//     */
//    private void outputSubjectScheme() throws DITAOTException {
//
//        final Map<String, Set<String>> graph = readMapFromXML(FILE_NAME_SUBJECT_RELATION);
//
//        final Queue<String> queue = new LinkedList<String>();
//        final Set<String> visitedSet = new HashSet<String>();
//
//        for (final Map.Entry<String, Set<String>> entry: graph.entrySet()) {
//            queue.offer(entry.getKey());
//        }
//
//        try {
//            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//            final DocumentBuilder builder = factory.newDocumentBuilder();
//            builder.setEntityResolver(CatalogUtils.getCatalogResolver());
//
//            while (!queue.isEmpty()) {
//                final String parent = queue.poll();
//                final Set<String> children = graph.get(parent);
//
//                if (children != null) {
//                    queue.addAll(children);
//                }
//                if ("ROOT".equals(parent) || visitedSet.contains(parent)) {
//                    continue;
//                }
//                visitedSet.add(parent);
//                String tmprel = FileUtils.getRelativePath(inputMap.getAbsolutePath(), parent);
//                tmprel = FileUtils.resolve(job.tempDir.getAbsolutePath(), tmprel) + SUBJECT_SCHEME_EXTENSION;
//                Document parentRoot = null;
//                if (!FileUtils.fileExists(tmprel)) {
//                    parentRoot = builder.parse(new InputSource(new FileInputStream(parent)));
//                } else {
//                    parentRoot = builder.parse(new InputSource(new FileInputStream(tmprel)));
//                }
//                if (children != null) {
//                    for (final String childpath: children) {
//                        final Document childRoot = builder.parse(new InputSource(new FileInputStream(childpath)));
//                        mergeScheme(parentRoot, childRoot);
//                        String rel = FileUtils.getRelativePath(inputMap.getAbsolutePath(), childpath);
//                        rel = FileUtils.resolve(job.tempDir.getAbsolutePath(), rel) + SUBJECT_SCHEME_EXTENSION;
//                        generateScheme(rel, childRoot);
//                    }
//                }
//
//                //Output parent scheme
//                String rel = FileUtils.getRelativePath(inputMap.getAbsolutePath(), parent);
//                rel = FileUtils.resolve(job.tempDir.getAbsolutePath(), rel) + SUBJECT_SCHEME_EXTENSION;
//                generateScheme(rel, parentRoot);
//            }
//        } catch (final Exception e) {
//            logger.logError(e.getMessage(), e) ;
//            throw new DITAOTException(e);
//        }
//
//    }

//    private void mergeScheme(final Document parentRoot, final Document childRoot) {
//        final Queue<Element> pQueue = new LinkedList<Element>();
//        pQueue.offer(parentRoot.getDocumentElement());
//
//        while (!pQueue.isEmpty()) {
//            final Element pe = pQueue.poll();
//            NodeList pList = pe.getChildNodes();
//            for (int i = 0; i < pList.getLength(); i++) {
//                final Node node = pList.item(i);
//                if (node.getNodeType() == Node.ELEMENT_NODE) {
//                    pQueue.offer((Element)node);
//                }
//            }
//
//            String value = pe.getAttribute(ATTRIBUTE_NAME_CLASS);
//            if (StringUtils.isEmptyString(value)
//                    || !SUBJECTSCHEME_SUBJECTDEF.matches(value)) {
//                continue;
//            }
//
//            if (!StringUtils.isEmptyString(
//                    value = pe.getAttribute(ATTRIBUTE_NAME_KEYREF))) {
//                // extend child scheme
//                final Element target = searchForKey(childRoot.getDocumentElement(), value);
//                if (target == null) {
//                    /*
//                     * TODO: we have a keyref here to extend into child scheme, but can't
//                     * find any matching <subjectdef> in child scheme. Shall we throw out
//                     * a warning?
//                     * 
//                     * Not for now, just bypass it.
//                     */
//                    continue;
//                }
//
//                // target found
//                pList = pe.getChildNodes();
//                for (int i = 0; i < pList.getLength(); i++) {
//                    final Node tmpnode = childRoot.importNode(pList.item(i), false);
//                    if (tmpnode.getNodeType() == Node.ELEMENT_NODE
//                            && searchForKey(target,
//                                    ((Element)tmpnode).getAttribute(ATTRIBUTE_NAME_KEYS)) != null) {
//                        continue;
//                    }
//                    target.appendChild(tmpnode);
//                }
//
//            } else if (!StringUtils.isEmptyString(
//                    value = pe.getAttribute(ATTRIBUTE_NAME_KEYS))) {
//                // merge into parent scheme
//                final Element target = searchForKey(childRoot.getDocumentElement(), value);
//                if (target != null) {
//                    pList = target.getChildNodes();
//                    for (int i = 0; i < pList.getLength(); i++) {
//                        final Node tmpnode = parentRoot.importNode(pList.item(i), false);
//                        if (tmpnode.getNodeType() == Node.ELEMENT_NODE
//                                && searchForKey(pe,
//                                        ((Element)tmpnode).getAttribute(ATTRIBUTE_NAME_KEYS)) != null) {
//                            continue;
//                        }
//                        pe.appendChild(tmpnode);
//                    }
//                }
//            }
//        }
//    }

//    private Element searchForKey(final Element root, final String key) {
//        if (root == null || StringUtils.isEmptyString(key)) {
//            return null;
//        }
//        final Queue<Element> queue = new LinkedList<Element>();
//        queue.offer(root);
//
//        while (!queue.isEmpty()) {
//            final Element pe = queue.poll();
//            final NodeList pchildrenList = pe.getChildNodes();
//            for (int i = 0; i < pchildrenList.getLength(); i++) {
//                final Node node = pchildrenList.item(i);
//                if (node.getNodeType() == Node.ELEMENT_NODE) {
//                    queue.offer((Element)node);
//                }
//            }
//
//            String value = pe.getAttribute(ATTRIBUTE_NAME_CLASS);
//            if (StringUtils.isEmptyString(value)
//                    || !SUBJECTSCHEME_SUBJECTDEF.matches(value)) {
//                continue;
//            }
//
//            value = pe.getAttribute(ATTRIBUTE_NAME_KEYS);
//            if (StringUtils.isEmptyString(value)) {
//                continue;
//            }
//
//            if (value.equals(key)) {
//                return pe;
//            }
//        }
//        return null;
//    }

//    /**
//     * Serialize subject scheme file.
//     * 
//     * @param filename output filepath
//     * @param root subject scheme document
//     * 
//     * @throws DITAOTException if generation fails
//     */
//    private void generateScheme(final String filename, final Document root) throws DITAOTException {
//        try {
//            final File f = new File(filename);
//            final File p = f.getParentFile();
//            if (!p.exists() && !p.mkdirs()) {
//                throw new IOException("Failed to make directory " + p.getAbsolutePath());
//            }
//            final FileOutputStream file = new FileOutputStream(new File(filename));
//            final StreamResult res = new StreamResult(file);
//            final DOMSource ds = new DOMSource(root);
//            final TransformerFactory tff = TransformerFactory.newInstance();
//            final Transformer tf = tff.newTransformer();
//            tf.transform(ds, res);
//            if (res.getOutputStream() != null) {
//                res.getOutputStream().close();
//            }
//            if (file != null) {
//                file.close();
//            }
//        } catch (final Exception e) {
//            logger.logError(e.getMessage(), e) ;
//            throw new DITAOTException(e);
//        }
//    }


    /**
     * Execute copy-to task, generate copy-to targets base on sources
     */
    private void performCopytoTask() {
        final Map<File, File> copytoMap  = job.getCopytoMap();

        for (final Map.Entry<File, File> entry: copytoMap.entrySet()) {
            final File copytoTarget = entry.getKey();
            final File copytoSource = entry.getValue();
            final File srcFile = new File(job.tempDir, copytoSource.getPath());
            final File targetFile = new File(job.tempDir, copytoTarget.getPath());
            if (targetFile.exists()) {
                logger.warn(MessageUtils.getInstance().getMessage("DOTX064W", copytoTarget.getPath()).toString());
            }else{
                final String inputMapInTemp = new File(job.tempDir, job.getInputMap()).getAbsolutePath();
                logger.info("copy-to: " + copytoSource + " -> " + copytoTarget);
                copyFileWithPIReplaced(srcFile, targetFile, copytoTarget, inputMapInTemp);
            }
        }
    }

    /**
     * Copy files and replace workdir PI contents.
     * 
     * @param src
     * @param target
     * @param copytoTargetFilename
     * @param inputMapInTemp
     */
    public void copyFileWithPIReplaced(final File src, final File target, final File copytoTargetFilename, final String inputMapInTemp ) {
        if (!target.getParentFile().exists() && !target.getParentFile().mkdirs()) {
            logger.error("Failed to create copy-to target directory " + target.getParentFile().getAbsolutePath());
        }
        final String path2project = listFilter.getPathtoProject(copytoTargetFilename, target, inputMapInTemp);
        final File workdir = target.getParentFile();
        try {
            final Transformer serializer = TransformerFactory.newInstance().newTransformer();
            final XMLFilter filter = new CopyToFilter(StringUtils.getXMLReader(), workdir, path2project);
            serializer.transform(
                    new SAXSource(filter, new InputSource(src.toURI().toString())),
                    new StreamResult(target));
        } catch (final Exception e) {
            logger.error("Failed to rewrite copy-to file: " + e.getMessage(), e);
        }
    }

    /**
     * XML filter to rewrite processing instructions to reflect copy-to location. The following processing-instructions are 
     * 
     * <ul>
     * <!--li>{@link DitaWriter#PI_WORKDIR_TARGET PI_WORKDIR_TARGET}</li-->
     * <li>{@link DitaWriter#PI_WORKDIR_TARGET_URI PI_WORKDIR_TARGET_URI}</li>
     * <li>{@link DitaWriter#PI_PATH2PROJ_TARGET PI_PATH2PROJ_TARGET}</li>
     * <li>{@link DitaWriter#PI_PATH2PROJ_TARGET_URI PI_PATH2PROJ_TARGET_URI}</li>
     * </ul>
     */
    private static final class CopyToFilter extends XMLFilterImpl {

        private final File workdir;
        private final String path2project;  

        CopyToFilter(final XMLReader parent, final File workdir, final String path2project) {
            super(parent);
            this.workdir = workdir;
            this.path2project = path2project;
        }

        @Override
        public void processingInstruction(final String target, final String data) throws SAXException {
            String d = data;
//            if(target.equals(PI_WORKDIR_TARGET)) {
//                if (workdir != null) {
//                    try {
//                        if (OS_NAME.toLowerCase().indexOf(OS_NAME_WINDOWS) == -1) {
//                            d = workdir.getCanonicalPath();
//                        } else {
//                            d = UNIX_SEPARATOR + workdir.getCanonicalPath();
//                        }
//                    } catch (final IOException e) {
//                        throw new RuntimeException("Failed to get canonical path for working directory: " + e.getMessage(), e);
//                    }
//                }
//            } else
            if (target.equals(PI_WORKDIR_TARGET_URI)) {
                if (workdir != null) {
                    d = workdir.toURI().toString();
                }
            } else if (target.equals(PI_PATH2PROJ_TARGET)) {
                if (path2project != null) {
                    d = path2project;
                }
            } else if (target.equals(PI_PATH2PROJ_TARGET_URI)) {
                if (path2project != null) {
                    d = URLUtils.correct(path2project, true);
                }
            }            
            getContentHandler().processingInstruction(target, d);
        }
        
    }

//    /**
//     * Read job configuration, update properties, and serialise.
//     * 
//     * @param tempDir temporary directory path
//     * @throws IOException 
//     */
//    private void updateList(final File tempDir) throws IOException{
//        final Job job = new Job(tempDir);
//        updatePropertyString(INPUT_DITAMAP, job);
//        updatePropertySet(HREF_TARGET_LIST, job);
//        updatePropertySet(CONREF_LIST, job);
//        updatePropertySet(HREF_DITA_TOPIC_LIST, job);
//        updatePropertySet(FULL_DITA_TOPIC_LIST, job);
//        updatePropertySet(FULL_DITAMAP_TOPIC_LIST, job);
//        updatePropertySet(CONREF_TARGET_LIST, job);
//        updatePropertySet(COPYTO_SOURCE_LIST, job);
//        updatePropertyMap(COPYTO_TARGET_TO_SOURCE_MAP_LIST, job);
//        updatePropertySet(OUT_DITA_FILES_LIST, job);
//        updatePropertySet(CONREF_PUSH_LIST, job);
//        updatePropertySet(KEYREF_LIST, job);
//        updatePropertySet(CODEREF_LIST, job);
//        updatePropertySet(CHUNK_TOPIC_LIST, job);
//        updatePropertySet(HREF_TOPIC_LIST, job);
//        updatePropertySet(RESOURCE_ONLY_LIST, job);
//        job.write();
//    }

//    /**
//     * Update dictionary
//     * 
//     * @param tempDir temporary directory
//     */
//    private void updateDictionary(final File tempDir){
//        //orignal map
//        final Map<String, Set<String>> dic = readMapFromXML(FILE_NAME_SUBJECT_DICTIONARY);
//        //result map
//        final Map<String, Set<String>> resultMap = new HashMap<String, Set<String>>();
//        //Iterate the orignal map
//        for (final Map.Entry<String, java.util.Set<String>> entry: dic.entrySet()) {
//            //filename will be checked.
//            String filename = entry.getKey();
////            if (extName != null) {
////                if(FileUtils.isDITATopicFile(filename)){
////                    //Replace extension name.
////                    filename = FileUtils.replaceExtension(filename, extName);
////                }
////            }
//            //put the updated value into the result map
//            resultMap.put(filename, entry.getValue());
//        }
//
//        //Write the updated map into the dictionary file
//        this.writeMapToXML(resultMap, FILE_NAME_SUBJECT_DICTIONARY);
//        //File inputFile = new File(tempDir, FILE_NAME_SUBJECT_DICTIONARY);
//    }

}