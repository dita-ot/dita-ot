/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2016 Jarno Elovirta
 *
 *  See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.module.reader;

import org.apache.xerces.xni.grammars.XMLGrammarPool;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.module.AbstractPipelineModuleImpl;
import org.dita.dost.module.GenMapAndTopicListModule.TempFileNameScheme;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.reader.*;
import org.dita.dost.util.*;
import org.dita.dost.writer.DitaWriterFilter;
import org.dita.dost.writer.ExportAnchorsFilter;
import org.dita.dost.writer.TopicFragmentFilter;
import org.xml.sax.*;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.dita.dost.reader.GenListModuleReader.ROOT_URI;
import static org.dita.dost.reader.GenListModuleReader.Reference;
import static org.dita.dost.util.Configuration.*;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.Job.FileInfo;
import static org.dita.dost.util.Job.USER_INPUT_FILE_LIST_FILE;
import static org.dita.dost.util.URLUtils.*;
import static org.dita.dost.util.XMLUtils.close;

/**
 * Base class for document reader and serializer.
 *
 * @since 2.5
 */
public abstract class AbstractReaderModule extends AbstractPipelineModuleImpl {

    public static final String ELEMENT_STUB = "stub";
    Predicate<String> formatFilter;
    /** FileInfos keyed by src. */
    private final Map<URI, FileInfo> fileinfos = new HashMap<>();
    /** Set of all topic files */
    private final Set<URI> fullTopicSet = new HashSet<>(128);
    /** Set of all map files */
    private final Set<URI> fullMapSet = new HashSet<>(128);
    /** Set of topic files containing href */
    private final Set<URI> hrefTopicSet = new HashSet<>(128);
    /** Set of dita files containing conref */
    private final Set<URI> conrefSet = new HashSet<>(128);
    /** Set of topic files containing coderef */
    private final Set<URI> coderefSet = new HashSet<>(128);
    /** Set of all images */
    final Set<Reference> formatSet = new HashSet<>();
    /** Set of all images used for flagging */
    private final Set<URI> flagImageSet = new LinkedHashSet<>(128);
    /** Set of all HTML and other non-DITA or non-image files */
    final Set<URI> htmlSet = new HashSet<>(128);
    /** Set of all the href targets */
    private final Set<URI> hrefTargetSet = new HashSet<>(128);
    /** Set of all the conref targets */
    private Set<URI> conrefTargetSet = new HashSet<>(128);
    /** Set of all the non-conref targets */
    private final Set<URI> nonConrefCopytoTargetSet = new HashSet<>(128);
    /** Set of subsidiary files */
    private final Set<URI> coderefTargetSet = new HashSet<>(16);
    /** Set of absolute flag image files */
    private final Set<URI> relFlagImagesSet = new LinkedHashSet<>(128);
    /** List of files waiting for parsing. Values are absolute URI references. */
    private final Queue<Reference> waitList = new LinkedList<>();
    /** List of parsed files */
    final List<URI> doneList = new LinkedList<>();
    final List<URI> failureList = new LinkedList<>();
    /** Set of outer dita files */
    private final Set<URI> outDitaFilesSet = new HashSet<>(128);
    /** Set of sources of conacion */
    private final Set<URI> conrefpushSet = new HashSet<>(128);
    /** Set of files containing keyref */
    private final Set<URI> keyrefSet = new HashSet<>(128);
    /** Set of files with "@processing-role=resource-only" */
    private final Set<URI> resourceOnlySet = new HashSet<>(128);
    /** Absolute basedir for processing */
    private URI baseInputDir;
//    /** Number of directory levels base directory is adjusted. */
//    private int uplevels = 0;
    GenListModuleReader listFilter;
    KeydefFilter keydefFilter;
    ExportAnchorsFilter exportAnchorsFilter;
    boolean validate = true;
    ContentHandler nullHandler;
    private TempFileNameScheme tempFileNameScheme;
    /** Absolute path to input file. */
    URI rootFile;
    /** Subject scheme key map. Key is key value, value is key definition. */
    private final Map<String, KeyDef> schemekeydefMap = new HashMap<>();
    /** Subject scheme absolute file paths. */
    private final Set<URI> schemeSet = new HashSet<>(128);
    /** Subject scheme usage. Key is absolute file path, value is set of applicable subject schemes. */
    private final Map<URI, Set<URI>> schemeDictionary = new HashMap<>();
    private final Map<URI, URI> copyTo = new HashMap<>();
    private boolean setSystemid = true;
    Mode processingMode;
    /** Generate {@code xtrf} and {@code xtrc} attributes */
    boolean genDebugInfo;
    /** use grammar pool cache */
    private boolean gramcache = true;
    private boolean setSystemId;
    /** Profiling is enabled. */
    private boolean profilingEnabled;
    String transtype;
    /** Absolute DITA-OT base path. */
    File ditaDir;
    private File ditavalFile;
    FilterUtils filterUtils;
    /** Absolute path to current destination file. */
    File outputFile;
    Map<QName, Map<String, Set<String>>> validateMap;
    Map<QName, Map<String, String>> defaultValueMap;
    /** XMLReader instance for parsing dita file */
    private XMLReader reader;
    /** Absolute path to current source file. */
    URI currentFile;
    private Map<URI, Set<URI>> dic;
    private SubjectSchemeReader subjectSchemeReader;
    private FilterUtils baseFilterUtils;
    DitaWriterFilter ditaWriterFilter;
    TopicFragmentFilter topicFragmentFilter;

    public abstract void readStartFile() throws DITAOTException;

    /**
     * Initialize reusable filters.
     */
    void initFilters() {
        tempFileNameScheme.setBaseDir(job.getInputDir());

        listFilter = new GenListModuleReader();
        listFilter.setLogger(logger);
        listFilter.setPrimaryDitamap(rootFile);
        listFilter.setJob(job);
        listFilter.setFormatFilter(formatFilter);

        if (profilingEnabled) {
            filterUtils = parseFilterFile();
        }

        if (INDEX_TYPE_ECLIPSEHELP.equals(transtype)) {
            exportAnchorsFilter = new ExportAnchorsFilter();
            exportAnchorsFilter.setInputFile(rootFile);
        }

        keydefFilter = new KeydefFilter();
        keydefFilter.setLogger(logger);
        keydefFilter.setCurrentFile(rootFile);
        keydefFilter.setJob(job);

        nullHandler = new DefaultHandler();

        ditaWriterFilter = new DitaWriterFilter();
        ditaWriterFilter.setTempFileNameScheme(tempFileNameScheme);
        ditaWriterFilter.setLogger(logger);
        ditaWriterFilter.setJob(job);
        ditaWriterFilter.setEntityResolver(reader.getEntityResolver());

        topicFragmentFilter = new TopicFragmentFilter(ATTRIBUTE_NAME_CONREF, ATTRIBUTE_NAME_CONREFEND);

    }

    /**
     * Init xml reader used for pipeline parsing.
     *
     * @param ditaDir absolute path to DITA-OT directory
     * @param validate whether validate input file
     * @throws SAXException parsing exception
     */
    void initXMLReader(final File ditaDir, final boolean validate) throws SAXException {
        reader = XMLUtils.getXMLReader();
        reader.setFeature(FEATURE_NAMESPACE, true);
        reader.setFeature(FEATURE_NAMESPACE_PREFIX, true);
        if (validate) {
            reader.setFeature(FEATURE_VALIDATION, true);
            try {
                reader.setFeature(FEATURE_VALIDATION_SCHEMA, true);
            } catch (final SAXNotRecognizedException e) {
                // Not Xerces, ignore exception
            }
        } else {
            logger.warn(MessageUtils.getMessage("DOTJ037W").toString());
        }
        if (gramcache) {
            final XMLGrammarPool grammarPool = GrammarPoolManager.getGrammarPool();
            try {
                reader.setProperty("http://apache.org/xml/properties/internal/grammar-pool", grammarPool);
                logger.info("Using Xerces grammar pool for DTD and schema caching.");
            } catch (final NoClassDefFoundError e) {
                logger.debug("Xerces not available, not using grammar caching");
            } catch (final SAXNotRecognizedException | SAXNotSupportedException e) {
                logger.warn("Failed to set Xerces grammar pool for parser: " + e.getMessage());
            }
        }
        CatalogUtils.setDitaDir(ditaDir);
        reader.setEntityResolver(CatalogUtils.getCatalogResolver());
    }

    void parseInputParameters(final AbstractPipelineInput input) {
        ditaDir = toFile(input.getAttribute(ANT_INVOKER_EXT_PARAM_DITADIR));
        if (!ditaDir.isAbsolute()) {
            throw new IllegalArgumentException("DITA-OT installation directory " + ditaDir + " must be absolute");
        }
        validate = Boolean.valueOf(input.getAttribute(ANT_INVOKER_EXT_PARAM_VALIDATE));
        transtype = input.getAttribute(ANT_INVOKER_EXT_PARAM_TRANSTYPE);
        gramcache = "yes".equalsIgnoreCase(input.getAttribute(ANT_INVOKER_EXT_PARAM_GRAMCACHE));
        setSystemid = "yes".equalsIgnoreCase(input.getAttribute(ANT_INVOKER_EXT_PARAN_SETSYSTEMID));
        processingMode = Optional.ofNullable(input.getAttribute(ANT_INVOKER_EXT_PARAM_PROCESSING_MODE))
                .map(String::toUpperCase)
                .map(Mode::valueOf)
                .orElse(Mode.LAX);
        genDebugInfo = Boolean.valueOf(input.getAttribute(ANT_INVOKER_EXT_PARAM_GENERATE_DEBUG_ATTR));

        // For the output control
        job.setGeneratecopyouter(input.getAttribute(ANT_INVOKER_EXT_PARAM_GENERATECOPYOUTTER));
        job.setOutterControl(input.getAttribute(ANT_INVOKER_EXT_PARAM_OUTTERCONTROL));
        job.setOnlyTopicInMap(Boolean.valueOf(input.getAttribute(ANT_INVOKER_EXT_PARAM_ONLYTOPICINMAP)));

        // Set the OutputDir
        final File path = toFile(input.getAttribute(ANT_INVOKER_EXT_PARAM_OUTPUTDIR));
        if (path.isAbsolute()) {
            job.setOutputDir(path);
        } else {
            throw new IllegalArgumentException("Output directory " + path + " must be absolute");
        }

        final File basedir = toFile(input.getAttribute(ANT_INVOKER_PARAM_BASEDIR));

        final URI ditaInputDir = toURI(input.getAttribute(ANT_INVOKER_EXT_PARAM_INPUTDIR));
        if (ditaInputDir != null) {
            if (ditaInputDir.isAbsolute()) {
                baseInputDir = ditaInputDir;
            } else if (ditaInputDir.getPath() != null && ditaInputDir.getPath().startsWith(URI_SEPARATOR)) {
                baseInputDir = setScheme(ditaInputDir, "file");
            } else {
                // XXX Shouldn't this be resolved to current directory, not Ant script base directory?
                baseInputDir = basedir.toURI().resolve(ditaInputDir);
            }
            assert baseInputDir.isAbsolute();
        }

        URI ditaInput = toURI(input.getAttribute(ANT_INVOKER_PARAM_INPUTMAP));
        ditaInput = ditaInput != null ? ditaInput : job.getInputFile();
        if (ditaInput.isAbsolute()) {
            rootFile = ditaInput;
        } else if (ditaInput.getPath() != null && ditaInput.getPath().startsWith(URI_SEPARATOR)) {
            rootFile = setScheme(ditaInput, "file");
        } else if (baseInputDir != null) {
            rootFile = baseInputDir.resolve(ditaInput);
        } else {
            rootFile = basedir.toURI().resolve(ditaInput);
        }
        job.setInputFile(rootFile);

        if (baseInputDir == null) {
            baseInputDir = rootFile.resolve(".");
        }
        job.setInputDir(baseInputDir);

        profilingEnabled = Optional.ofNullable(input.getAttribute(ANT_INVOKER_PARAM_PROFILING_ENABLED))
                .map(Boolean::parseBoolean)
                .orElse(true);
        if (profilingEnabled) {
            ditavalFile = Optional.of(new File(job.tempDir, FILE_NAME_MERGED_DITAVAL))
                    .filter(f -> f.exists())
                    .orElse(null);
        }
    }

    void processWaitList() throws DITAOTException {
        while (!waitList.isEmpty()) {
            readFile(waitList.remove(), null);
        }
    }

    /**
     * Get pipe line filters
     *
     * @param fileToParse absolute path to current file being processed
     */
    abstract List<XMLFilter> getProcessingPipe(final URI fileToParse);

    /**
     * Read a file and process it for list information.
     *
     * @param ref system path of the file to process
     * @param parseFile file to parse, may be {@code null}
     * @throws DITAOTException if processing failed
     */
    void readFile(final Reference ref, final URI parseFile) throws DITAOTException {
        currentFile = ref.filename;
        assert currentFile.isAbsolute();
        final URI src = parseFile != null ? parseFile : currentFile;
        assert src.isAbsolute();
        final URI rel = tempFileNameScheme.generateTempFileName(currentFile);
        outputFile = new File(job.tempDirURI.resolve(rel));
        final File outputDir = outputFile.getParentFile();
        if (!outputDir.exists() && !outputDir.mkdirs()) {
            logger.error("Failed to create output directory " + outputDir.getAbsolutePath());
            return;
        }
        validateMap = Collections.emptyMap();
        defaultValueMap = Collections.emptyMap();
        logger.info("Processing " + currentFile + " to " + outputFile.toURI());
        final String[] params = { currentFile.toString() };

        // Verify stub for current file is in Job
        final FileInfo fi = job.getFileInfo(currentFile);
        if (fi == null) {
            final FileInfo stub = new FileInfo.Builder()
                    .src(currentFile)
                    .uri(rel)
                    .result(currentFile)
                    .build();
            job.add(stub);
        }

//        InputSource in = null;
        Result out = null;
        try {
            final TransformerFactory tf = TransformerFactory.newInstance();
            final SAXTransformerFactory stf = (SAXTransformerFactory) tf;
            final TransformerHandler serializer = stf.newTransformerHandler();

            XMLReader parser = getXmlReader(ref.format);
            XMLReader xmlSource = parser;
            for (final XMLFilter f: getProcessingPipe(currentFile)) {
                f.setParent(xmlSource);
                f.setEntityResolver(CatalogUtils.getCatalogResolver());
                xmlSource = f;
            }

            try {
                final LexicalHandler lexicalHandler = new DTDForwardHandler(xmlSource);
                parser.setProperty("http://xml.org/sax/properties/lexical-handler", lexicalHandler);
                parser.setFeature("http://xml.org/sax/features/lexical-handler", true);
            } catch (final SAXNotRecognizedException e) {}

//            in = new InputSource(src.toString());
            out = new StreamResult(new FileOutputStream(outputFile));
            serializer.setResult(out);
            xmlSource.setContentHandler(serializer);
            xmlSource.parse(src.toString());

            if (listFilter.isValidInput()) {
                processParseResult(currentFile);
                categorizeCurrentFile(ref);
            } else if (!currentFile.equals(rootFile)) {
                logger.warn(MessageUtils.getMessage("DOTJ021W", params).toString());
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
                throw new DITAOTException(MessageUtils.getMessage("DOTJ012F", params).toString() + ": " + sax.getMessage(), sax);
            } else if (processingMode == Mode.STRICT) {
                throw new DITAOTException(MessageUtils.getMessage("DOTJ013E", params).toString() + ": " + sax.getMessage(), sax);
            } else {
                logger.error(MessageUtils.getMessage("DOTJ013E", params).toString() + ": " + sax.getMessage(), sax);
            }
            failureList.add(currentFile);
        } catch (final FileNotFoundException e) {
            if (currentFile.equals(rootFile)) {
                throw new DITAOTException(MessageUtils.getMessage("DOTA069F", params).toString(), e);
            } else if (processingMode == Mode.STRICT) {
                throw new DITAOTException(MessageUtils.getMessage("DOTX008E", params).toString() + ": " + e.getMessage(), e);
            } else {
                logger.error(MessageUtils.getMessage("DOTX008E", params).toString());
            }
            failureList.add(currentFile);
        } catch (final Exception e) {
            if (currentFile.equals(rootFile)) {
                throw new DITAOTException(MessageUtils.getMessage("DOTJ012F", params).toString() + ": " + e.getMessage(),  e);
            } else if (processingMode == Mode.STRICT) {
                throw new DITAOTException(MessageUtils.getMessage("DOTJ013E", params).toString() + ": " + e.getMessage(), e);
            } else {
                logger.error(MessageUtils.getMessage("DOTJ013E", params).toString() + ": " + e.getMessage(), e);
            }
            failureList.add(currentFile);
        } finally {
            if (out != null) {
                try {
                    close(out);
                } catch (final IOException e) {
                    logger.error(e.getMessage(), e) ;
                }
            }
        }

        if (!listFilter.isValidInput() && currentFile.equals(rootFile)) {
            if (validate) {
                // stop the build if all content in the input file was filtered out.
                throw new DITAOTException(MessageUtils.getMessage("DOTJ022F", params).toString());
            } else {
                // stop the build if the content of the file is not valid.
                throw new DITAOTException(MessageUtils.getMessage("DOTJ034F", params).toString());
            }
        }

        doneList.add(currentFile);
        listFilter.reset();
        keydefFilter.reset();

    }

    /**
     * Process results from parsing a single topic
     *
     * @param currentFile absolute URI processes files
     */
    void processParseResult(final URI currentFile) {
        // Category non-copyto result and update uplevels accordingly
        for (final Reference file: listFilter.getNonCopytoResult()) {
            categorizeReferenceFile(file);
//            updateUplevels(file.filename);
        }
        for (final Map.Entry<URI, URI> e : listFilter.getCopytoMap().entrySet()) {
            final URI source = e.getValue();
            final URI target = e.getKey();
            copyTo.put(target, source);
//            updateUplevels(target);

        }
        schemeSet.addAll(listFilter.getSchemeRefSet());

        // collect key definitions
        for (final Map.Entry<String, KeyDef> e: keydefFilter.getKeysDMap().entrySet()) {
            // key and value.keys will differ when keydef is a redirect to another keydef
            final String key = e.getKey();
            final KeyDef value = e.getValue();
            if (schemeSet.contains(currentFile)) {
                schemekeydefMap.put(key, new KeyDef(key, value.href, value.scope, value.format, currentFile, null));
            }
        }

        hrefTargetSet.addAll(listFilter.getHrefTargets());
        conrefTargetSet.addAll(listFilter.getConrefTargets());
        nonConrefCopytoTargetSet.addAll(listFilter.getNonConrefCopytoTargets());
        coderefTargetSet.addAll(listFilter.getCoderefTargets());
        outDitaFilesSet.addAll(listFilter.getOutFilesSet());

        // Generate topic-scheme dictionary
        final Set<URI> schemeSet = listFilter.getSchemeSet();
        if (schemeSet != null && !schemeSet.isEmpty()) {
            Set<URI> children = schemeDictionary.get(currentFile);
            if (children == null) {
                children = new HashSet<>();
            }
            children.addAll(schemeSet);
            schemeDictionary.put(currentFile, children);
            final Set<URI> hrfSet = listFilter.getHrefTargets();
            for (final URI filename: hrfSet) {
                children = schemeDictionary.get(filename);
                if (children == null) {
                    children = new HashSet<>();
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
    void categorizeCurrentFile(final Reference ref) {
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
            if (ref.format != null && !ref.format.equals(ATTR_FORMAT_VALUE_DITA)) {
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
    abstract void categorizeReferenceFile(final Reference file);/* {
        // avoid files referred by coderef being added into wait list
        if (listFilter.getCoderefTargets().contains(file.filename)) {
            return;
        }
        if (formatFilter.test(file.format)) {
            if (isFormatDita(file.format)) {
                addToWaitList(file);
            } else if (ATTR_FORMAT_VALUE_DITAMAP.equals(file.format)) {
                addToWaitList(file);
            } else if (ATTR_FORMAT_VALUE_IMAGE.equals(file.format)) {
                formatSet.add(file);
                if (!exists(file.filename)) {
                    logger.warn(MessageUtils.getMessage("DOTX008W", file.filename.toString()).toString());
                }
            } else if (ATTR_FORMAT_VALUE_DITAVAL.equals(file.format)) {
                formatSet.add(file);
            } else {
                htmlSet.add(file.filename);
            }
        }
    }*/

//    /**
//     * Update uplevels if needed. If the parameter contains a {@link Constants#STICK STICK}, it and
//     * anything following it is removed.
//     *
//     * @param file file path
//     */
//    private void updateUplevels(final URI file) {
//        assert file.isAbsolute();
//        if (file.getPath() != null) {
//            final URI f = file.toString().contains(STICK)
//                    ? toURI(file.toString().substring(0, file.toString().indexOf(STICK)))
//                    : file;
//            final URI relative = getRelativePath(rootFile, f).normalize();
//            final int lastIndex = relative.getPath().lastIndexOf(".." + URI_SEPARATOR);
//            if (lastIndex != -1) {
//                final int newUplevels = lastIndex / 3 + 1;
//                uplevels = Math.max(newUplevels, uplevels);
//            }
//        }
//    }

    /**
     * Add the given file the wait list if it has not been parsed.
     *
     * @param ref reference to absolute system path
     */
    void addToWaitList(final Reference ref) {
        final URI file = ref.filename;
        assert file.isAbsolute() && file.getFragment() == null;
        if (doneList.contains(file) || waitList.contains(ref) || file.equals(currentFile)) {
            return;
        }

        waitList.add(ref);
    }

//    /**
//     * Update base directory and prefix based on uplevels.
//     */
//    void updateBaseDirectory() {
//        for (int i = uplevels; i > 0; i--) {
//            baseInputDir = baseInputDir.resolve("..");
//        }
//    }

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
     * Parse filter file
     *
     * @return configured filter utility
     */
    private FilterUtils parseFilterFile() {
        final FilterUtils filterUtils;
        if (ditavalFile != null) {
            final DitaValReader ditaValReader = new DitaValReader();
            ditaValReader.setLogger(logger);
            ditaValReader.setJob(job);
            ditaValReader.read(ditavalFile.toURI());
            flagImageSet.addAll(ditaValReader.getImageList());
            relFlagImagesSet.addAll(ditaValReader.getRelFlagImageList());
            filterUtils = new FilterUtils(printTranstype.contains(transtype), ditaValReader.getFilterMap(),
                    ditaValReader.getForegroundConflictColor(), ditaValReader.getBackgroundConflictColor());
        } else {
            filterUtils = new FilterUtils(printTranstype.contains(transtype));
        }
        filterUtils.setLogger(logger);
        return filterUtils;
    }

    /**
     * Handle topic which are only conref sources from normal processing.
     */
    void handleConref() {
        // Get pure conref targets
        final Set<URI> pureConrefTargets = new HashSet<>(conrefTargetSet.size());
        for (final URI target: conrefTargetSet) {
            if (!nonConrefCopytoTargetSet.contains(target)) {
                pureConrefTargets.add(target);
            }
        }
        pureConrefTargets.remove(rootFile);
        conrefTargetSet = pureConrefTargets;

        // Remove pure conref targets from fullTopicSet
        fullTopicSet.removeAll(pureConrefTargets);
    }

    /**
     * Write result files.
     *
     * @throws DITAOTException if writing result files failed
     */
    void outputResult() throws DITAOTException {
        tempFileNameScheme.setBaseDir(baseInputDir);

        // assume empty Job
        final URI rootTemp = tempFileNameScheme.generateTempFileName(rootFile);
        final File relativeRootFile = toFile(rootTemp);

        job.setInputMap(rootTemp);

        job.setProperty(INPUT_DITAMAP_LIST_FILE_LIST, USER_INPUT_FILE_LIST_FILE);
        final File inputfile = new File(job.tempDir, USER_INPUT_FILE_LIST_FILE);
        writeListFile(inputfile, relativeRootFile.toString());

        job.setProperty("tempdirToinputmapdir.relative.value", StringUtils.escapeRegExp(getPrefix(relativeRootFile)));
//        job.setProperty("uplevels", getLevelsPath(rootTemp));

        resourceOnlySet.addAll(listFilter.getResourceOnlySet());

        for (final URI file: outDitaFilesSet) {
            getOrCreateFileInfo(fileinfos, file).isOutDita = true;
        }
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
        for (final Reference file: formatSet) {
            getOrCreateFileInfo(fileinfos, file.filename).format = file.format;
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
        for (final URI file: schemeSet) {
            getOrCreateFileInfo(fileinfos, file).isSubjectScheme = true;
        }
        for (final URI file: coderefTargetSet) {
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

        final Map<URI, URI> filteredCopyTo = filterConflictingCopyTo(copyTo, fileinfos.values());

        for (final FileInfo fs: fileinfos.values()) {
            if (!failureList.contains(fs.src)) {
//                if (job.getFileInfo(fs.uri) != null) {
//                    logger.info("Already in job:" + fs.uri);
//                }
//                if (formatFilter.test(fs.format)) {
                    final URI src = filteredCopyTo.get(fs.src);
                    // correct copy-to
                    if (src != null) {
                        final FileInfo corr = new FileInfo.Builder(fs).src(src).build();
                        job.add(corr);
                    } else {
                        job.add(fs);
                    }
//                } else {
//                    logger.info("skip " + fs.src + " -> " + fs.uri);
//                }
            }
        }
        for (final URI target : filteredCopyTo.keySet()) {
            final URI tmp = tempFileNameScheme.generateTempFileName(target);
            final FileInfo fi = new FileInfo.Builder().result(target).uri(tmp).build();
            // FIXME: what's the correct value for this? Accept all?
            if (formatFilter.test(fi.format)
                    || fi.format == null || fi.format.equals(ATTR_FORMAT_VALUE_DITA)) {
                job.add(fi);
            }
        }

        try {
            logger.info("Serializing job specification");
            if (!job.tempDir.exists() && !job.tempDir.mkdirs()) {
                throw new DITAOTException("Failed to create " + job.tempDir + " directory");
            }
            job.write();
        } catch (final IOException e) {
            throw new DITAOTException("Failed to serialize job configuration files: " + e.getMessage(), e);
        }

        try {
            SubjectSchemeReader.writeMapToXML(addMapFilePrefix(listFilter.getRelationshipGrap()), new File(job.tempDir, FILE_NAME_SUBJECT_RELATION));
            SubjectSchemeReader.writeMapToXML(addMapFilePrefix(schemeDictionary), new File(job.tempDir, FILE_NAME_SUBJECT_DICTIONARY));
        } catch (final IOException e) {
            throw new DITAOTException("Failed to serialize subject scheme files: " + e.getMessage(), e);
        }

        if (INDEX_TYPE_ECLIPSEHELP.equals(transtype)) {
            final DelayConrefUtils delayConrefUtils = new DelayConrefUtils();
            delayConrefUtils.setLogger(logger);
            delayConrefUtils.setJob(job);
            delayConrefUtils.writeMapToXML(exportAnchorsFilter.getPluginMap());
            delayConrefUtils.writeExportAnchors(exportAnchorsFilter, tempFileNameScheme);
        }

//        KeyDef.writeKeydef(new File(job.tempDir, SUBJECT_SCHEME_KEYDEF_LIST_FILE), addFilePrefix(schemekeydefMap.values()));
    }

    /** Filter copy-to where target is used directly. */
    private Map<URI, URI> filterConflictingCopyTo( final Map<URI, URI> copyTo, final Collection<FileInfo> fileInfos) {
        final Set<URI> fileinfoTargets = fileInfos.stream()
                .filter(fi -> fi.src.equals(fi.result))
                .map(fi -> fi.result)
                .collect(Collectors.toSet());
        return copyTo.entrySet().stream()
                .filter(e -> !fileinfoTargets.contains(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Write list file.
     * @param inputfile output list file
     * @param relativeRootFile list value
     */
    private void writeListFile(final File inputfile, final String relativeRootFile) {
        try (Writer bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(inputfile)))) {
            bufferedWriter.write(relativeRootFile);
            bufferedWriter.flush();
        } catch (final IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * Prefix path.
     *
     * @param relativeRootFile relative path for root temporary file
     * @return either an empty string or a path which ends in {@link File#separator File.separator}
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

    /**
     * Convert absolute paths to relative temporary directory paths
     * @return map with relative keys and values
     */
    private Map<URI, Set<URI>> addMapFilePrefix(final Map<URI, Set<URI>> map) {
        final Map<URI, Set<URI>> res = new HashMap<>();
        for (final Map.Entry<URI, Set<URI>> e: map.entrySet()) {
            final URI key = e.getKey();
            final Set<URI> newSet = new HashSet<>(e.getValue().size());
            for (final URI file: e.getValue()) {
                newSet.add(tempFileNameScheme.generateTempFileName(file));
            }
            res.put(key.equals(ROOT_URI) ? key : tempFileNameScheme.generateTempFileName(key), newSet);
        }
        return res;
    }

//    /**
//     * Add file prefix. For absolute paths the prefix is not added.
//     *
//     * @param set file paths
//     * @return file paths with prefix
//     */
//    private Map<URI, URI> addFilePrefix(final Map<URI, URI> set) {
//        final Map<URI, URI> newSet = new HashMap<>();
//        for (final Map.Entry<URI, URI> file: set.entrySet()) {
//            final URI key = tempFileNameScheme.generateTempFileName(file.getKey());
//            final URI value = tempFileNameScheme.generateTempFileName(file.getValue());
//            newSet.put(key, value);
//        }
//        return newSet;
//    }
//
//    private Collection<KeyDef> addFilePrefix(final Collection<KeyDef> keydefs) {
//        final Collection<KeyDef> res = new ArrayList<>(keydefs.size());
//        for (final KeyDef k: keydefs) {
//            final URI source = tempFileNameScheme.generateTempFileName(k.source);
//            res.add(new KeyDef(k.keys, k.href, k.scope, source, null));
//        }
//        return res;
//    }

    /**
     * add FlagImangesSet to Properties, which needn't to change the dir level,
     * just ouput to the ouput dir.
     *
     * @param prop job configuration
     * @param set absolute flag image files
     */
    private void addFlagImagesSetToProperties(final Job prop, final Set<URI> set) {
        final Set<URI> newSet = new LinkedHashSet<>(128);
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
        final String fileKey = Constants.REL_FLAGIMAGE_LIST.substring(0, Constants.REL_FLAGIMAGE_LIST.lastIndexOf("list")) + "file";
        prop.setProperty(fileKey, Constants.REL_FLAGIMAGE_LIST.substring(0, Constants.REL_FLAGIMAGE_LIST.lastIndexOf("list")) + ".list");
        final File list = new File(job.tempDir, prop.getProperty(fileKey));
        try (Writer bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(list)))) {
            for (URI aNewSet : newSet) {
                bufferedWriter.write(aNewSet.getPath());
                bufferedWriter.write('\n');
            }
            bufferedWriter.flush();
        } catch (final IOException e) {
            logger.error(e.getMessage(), e) ;
        }

        prop.setProperty(Constants.REL_FLAGIMAGE_LIST, StringUtils.join(newSet, COMMA));
    }






    /////////////////////////////////////////








//    void writeFile(final FileInfo f) {
//        currentFile = f.src;
//        if (f.src == null || !exists(f.src) || !f.src.equals(f.result)) {
//            logger.warn("Ignoring a copy-to file " + f.result);
//            return;
//        }
//        outputFile = new File(job.tempDir, f.file.getPath());
//        final File outputDir = outputFile.getParentFile();
//        if (!outputDir.exists() && !outputDir.mkdirs()) {
//            logger.error("Failed to create output directory " + outputDir.getAbsolutePath());
//            return;
//        }
//        logger.info("Processing " + f.src + " to " + outputFile.toURI());
//
////        final Set<URI> schemaSet = dic.get(f.uri);
////        if (schemaSet != null && !schemaSet.isEmpty()) {
////            logger.debug("Loading subject schemes");
////            subjectSchemeReader.reset();
////            for (final URI schema : schemaSet) {
////                subjectSchemeReader.loadSubjectScheme(new File(job.tempDirURI.resolve(schema.getPath() + SUBJECT_SCHEME_EXTENSION)));
////            }
////            validateMap = subjectSchemeReader.getValidValuesMap();
////            defaultValueMap = subjectSchemeReader.getDefaultValueMap();
////        } else {
////            validateMap = emptyMap();
////            defaultValueMap = emptyMap();
////        }
////        if (profilingEnabled) {
////            filterUtils = baseFilterUtils.refine(subjectSchemeReader.getSubjectSchemeMap());
////        }
//
//        InputSource in = null;
//        Result out = null;
//        try {
//            reader.setErrorHandler(new DITAOTXMLErrorHandler(currentFile.toString(), logger));
//
//            final TransformerFactory tf = TransformerFactory.newInstance();
//            final SAXTransformerFactory stf = (SAXTransformerFactory) tf;
//            final TransformerHandler serializer = stf.newTransformerHandler();
//
//            XMLReader parser = getXmlReader(f.format);
//            XMLReader xmlSource = parser;
//            for (final XMLFilter filter: getProcessingPipe(currentFile)) {
//                filter.setParent(xmlSource);
//                xmlSource = filter;
//            }
//            // ContentHandler must be reset so e.g. Saxon 9.1 will reassign ContentHandler
//            // when reusing filter with multiple Transformers.
//            xmlSource.setContentHandler(null);
//
//            try {
//                final LexicalHandler lexicalHandler = new DTDForwardHandler(xmlSource);
//                parser.setProperty("http://xml.org/sax/properties/lexical-handler", lexicalHandler);
//                parser.setFeature("http://xml.org/sax/features/lexical-handler", true);
//            } catch (final SAXNotRecognizedException e) {}
//
//            in = new InputSource(f.src.toString());
//            out = new StreamResult(new FileOutputStream(outputFile));
//            serializer.setResult(out);
//            xmlSource.setContentHandler(serializer);
//            xmlSource.parse(new InputSource(f.src.toString()));
//        } catch (final RuntimeException e) {
//            throw e;
//        } catch (final Exception e) {
//            logger.error(e.getMessage(), e) ;
//        } finally {
//            try {
//                close(out);
//            } catch (final Exception e) {
//                logger.error(e.getMessage(), e) ;
//            }
//            try {
//                close(in);
//            } catch (final IOException e) {
//                logger.error(e.getMessage(), e) ;
//            }
//        }
//
//        if (isFormatDita(f.format)) {
//            f.format = ATTR_FORMAT_VALUE_DITA;
//        }
//    }

    private XMLReader getXmlReader(final String format) throws SAXException {
        for (final Map.Entry<String, String> e: parserMap.entrySet()) {
            if (format != null && format.equals(e.getKey())) {
                try {
                    return (XMLReader) Class.forName(e.getValue()).newInstance();
                } catch (final InstantiationException | ClassNotFoundException | IllegalAccessException ex) {
                    throw new SAXException(ex);
                }
            }
        }
        return reader;
    }

    void init() throws SAXException {
        try {
            final String cls = Optional
                    .ofNullable(job.getProperty("temp-file-name-scheme"))
                    .orElse(configuration.get("temp-file-name-scheme"));
            tempFileNameScheme = (TempFileNameScheme) Class.forName(cls).newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        tempFileNameScheme.setBaseDir(job.getInputDir());

//        // Output subject schemas
//        outputSubjectScheme();
//        subjectSchemeReader = new SubjectSchemeReader();
//        subjectSchemeReader.setLogger(logger);
//        subjectSchemeReader.setJob(job);
//        dic = SubjectSchemeReader.readMapFromXML(new File(job.tempDir, FILE_NAME_SUBJECT_DICTIONARY));
//
//        if (profilingEnabled) {
//            final DitaValReader filterReader = new DitaValReader();
//            filterReader.setLogger(logger);
//            filterReader.setJob(job);
//            filterReader.initXMLReader(setSystemId);
//            Map<FilterUtils.FilterKey, FilterUtils.Action> filterMap;
//            if (ditavalFile != null) {
//                filterReader.read(ditavalFile.getAbsoluteFile());
//                filterMap = filterReader.getFilterMap();
//            } else {
//                filterMap = Collections.emptyMap();
//            }
//            baseFilterUtils = new FilterUtils(printTranstype.contains(transtype), filterMap);
//            baseFilterUtils.setLogger(logger);
//        }
        initXMLReader(ditaDir, validate);
        initFilters();
    }

//    /**
//     * Output subject schema file.
//     *
//     * @throws DITAOTException if generation files
//     */
//    private void outputSubjectScheme() throws DITAOTException {
//        try {
//            final Map<URI, Set<URI>> graph = SubjectSchemeReader.readMapFromXML(new File(job.tempDir, FILE_NAME_SUBJECT_RELATION));
//
//            final Queue<URI> queue = new LinkedList<>(graph.keySet());
//            final Set<URI> visitedSet = new HashSet<>();
//
//            final DocumentBuilder builder = XMLUtils.getDocumentBuilder();
//            builder.setEntityResolver(CatalogUtils.getCatalogResolver());
//
//            while (!queue.isEmpty()) {
//                final URI parent = queue.poll();
//                final Set<URI> children = graph.get(parent);
//
//                if (children != null) {
//                    queue.addAll(children);
//                }
//                if (ROOT_URI.equals(parent) || visitedSet.contains(parent)) {
//                    continue;
//                }
//                visitedSet.add(parent);
//                final File tmprel = new File(FileUtils.resolve(job.tempDir, parent) + SUBJECT_SCHEME_EXTENSION);
//                final Document parentRoot;
//                if (!tmprel.exists()) {
//                    final URI src = job.getFileInfo(parent).src;
//                    parentRoot = builder.parse(src.toString());
//                } else {
//                    parentRoot = builder.parse(tmprel);
//                }
//                if (children != null) {
//                    for (final URI childpath: children) {
//                        final Document childRoot = builder.parse(rootFile.resolve(childpath.getPath()).toString());
//                        mergeScheme(parentRoot, childRoot);
//                        generateScheme(new File(job.tempDir, childpath.getPath() + SUBJECT_SCHEME_EXTENSION), childRoot);
//                    }
//                }
//
//                //Output parent scheme
//                generateScheme(new File(job.tempDir, parent.getPath() + SUBJECT_SCHEME_EXTENSION), parentRoot);
//            }
//        } catch (final RuntimeException e) {
//            throw e;
//        } catch (final Exception e) {
//            logger.error(e.getMessage(), e) ;
//            throw new DITAOTException(e);
//        }
//
//    }

//    private void mergeScheme(final Document parentRoot, final Document childRoot) {
//        final Queue<Element> pQueue = new LinkedList<>();
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
//                            ((Element)tmpnode).getAttribute(ATTRIBUTE_NAME_KEYS)) != null) {
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
//                                ((Element)tmpnode).getAttribute(ATTRIBUTE_NAME_KEYS)) != null) {
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
//        final Queue<Element> queue = new LinkedList<>();
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
//    private void generateScheme(final File filename, final Document root) throws DITAOTException {
//        final File p = filename.getParentFile();
//        if (!p.exists() && !p.mkdirs()) {
//            throw new DITAOTException("Failed to make directory " + p.getAbsolutePath());
//        }
//        Result res = null;
//        try {
//            res = new StreamResult(new FileOutputStream(filename));
//            final DOMSource ds = new DOMSource(root);
//            final TransformerFactory tff = TransformerFactory.newInstance();
//            final Transformer tf = tff.newTransformer();
//            tf.transform(ds, res);
//        } catch (final RuntimeException e) {
//            throw e;
//        } catch (final Exception e) {
//            logger.error(e.getMessage(), e) ;
//            throw new DITAOTException(e);
//        } finally {
//            try {
//                close(res);
//            } catch (IOException e) {
//                throw new DITAOTException(e);
//            }
//        }
//    }

//    /**
//     * Get path to base directory
//     *
//     * @param filename relative input file path from base directory
//     * @param traceFilename absolute input file
//     * @param inputMap absolute path to start file
//     * @return path to base directory, {@code null} if not available
//     */
//    public static File getPathtoProject(final File filename, final File traceFilename, final File inputMap, final Job job) {
//        if (job.getGeneratecopyouter() != Job.Generate.OLDSOLUTION) {
//            if (isOutFile(traceFilename, inputMap)) {
//                return toFile(getRelativePathFromOut(traceFilename.getAbsoluteFile(), job));
//            } else {
//                return new File(getRelativeUnixPath(traceFilename.getAbsolutePath(), inputMap.getAbsolutePath())).getParentFile();
//            }
//        } else {
//            return FileUtils.getRelativePath(filename);
//        }
//    }
//    /**
//     * Just for the overflowing files.
//     * @param overflowingFile overflowingFile
//     * @return relative system path to out which ends in {@link java.io.File#separator File.separator}
//     */
//    private static String getRelativePathFromOut(final File overflowingFile, final Job job) {
//        final URI relativePath = getRelativePath(job.getInputFile(), overflowingFile.toURI());
//        final File outputDir = job.getOutputDir().getAbsoluteFile();
//        final File outputPathName = new File(outputDir, "index.html");
//        final File finalOutFilePathName = resolve(outputDir, relativePath.getPath());
//        final File finalRelativePathName = FileUtils.getRelativePath(finalOutFilePathName, outputPathName);
//        File parentDir = finalRelativePathName.getParentFile();
//        if (parentDir == null || parentDir.getPath().isEmpty()) {
//            parentDir = new File(".");
//        }
//        return parentDir.getPath() + File.separator;
//    }

//    /**
//     * Check if path falls outside start document directory
//     *
//     * @param filePathName absolute path to test
//     * @param inputMap absolute input map path
//     * @return {@code true} if outside start directory, otherwise {@code false}
//     */
//    private static boolean isOutFile(final File filePathName, final File inputMap){
//        final File relativePath = FileUtils.getRelativePath(inputMap.getAbsoluteFile(), filePathName.getAbsoluteFile());
//        return !(relativePath.getPath().length() == 0 || !relativePath.getPath().startsWith(".."));
//    }

}