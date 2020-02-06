/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2016 Jarno Elovirta
 *
 *  See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.module.reader;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.MultimapBuilder.SetMultimapBuilder;
import com.google.common.collect.SetMultimap;
import org.apache.commons.io.FileUtils;
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
    final SetMultimap<String, URI> htmlSet = SetMultimapBuilder.hashKeys().hashSetValues().build();
    /** Set of all the href targets */
    private final Set<URI> hrefTargetSet = new HashSet<>(128);
    /** Set of all the conref targets */
    private Set<URI> conrefTargetSet = new HashSet<>(128);
    /** Set of all targets except conref and copy-to */
    final Set<URI> nonConrefCopytoTargetSet = new HashSet<>(128);
    /** Set of subsidiary files */
    private final Set<URI> coderefTargetSet = new HashSet<>(16);
    /** Set of absolute flag image files */
    private final Set<URI> relFlagImagesSet = new LinkedHashSet<>(128);
    /** List of files waiting for parsing. Values are absolute URI references. */
    @VisibleForTesting
    final Queue<Reference> waitList = new LinkedList<>();
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
    GenListModuleReader listFilter;
    KeydefFilter keydefFilter;
    ExportAnchorsFilter exportAnchorsFilter;
    boolean validate = true;
    ContentHandler nullHandler;
    private TempFileNameScheme tempFileNameScheme;
    /** Absolute path to input file. */
    URI rootFile;
    /** Subject scheme absolute file paths. */
    private final Set<URI> schemeSet = new HashSet<>(128);
    /** Subject scheme usage. Key is absolute file path, value is set of applicable subject schemes. */
    private final Map<URI, Set<URI>> schemeDictionary = new HashMap<>();
    private final Map<URI, URI> copyTo = new HashMap<>();
    Mode processingMode;
    /** Generate {@code xtrf} and {@code xtrc} attributes */
    boolean genDebugInfo;
    /** use grammar pool cache */
    private boolean gramcache = true;
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
        processingMode = Optional.ofNullable(input.getAttribute(ANT_INVOKER_EXT_PARAM_PROCESSING_MODE))
                .map(String::toUpperCase)
                .map(Mode::valueOf)
                .orElse(Mode.LAX);
        genDebugInfo = Boolean.valueOf(input.getAttribute(ANT_INVOKER_EXT_PARAM_GENERATE_DEBUG_ATTR));

        // For the output control
        job.setGeneratecopyouter(input.getAttribute(ANT_INVOKER_EXT_PARAM_GENERATECOPYOUTTER));
        job.setOutterControl(input.getAttribute(ANT_INVOKER_EXT_PARAM_OUTTERCONTROL));
        job.setOnlyTopicInMap(Boolean.valueOf(input.getAttribute(ANT_INVOKER_EXT_PARAM_ONLYTOPICINMAP)));
        job.setCrawl(Optional.ofNullable(input.getAttribute(ANT_INVOKER_EXT_PARAM_CRAWL))
                .orElse(ANT_INVOKER_EXT_PARAM_CRAWL_VALUE_TOPIC));

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
                    .filter(File::exists)
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
                    .isInput(currentFile.equals(rootFile))
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
            if (!exists(currentFile)) {
                if (currentFile.equals(rootFile)) {
                    throw new DITAOTException(MessageUtils.getMessage("DOTA069F", params).toString(), e);
                } else if (processingMode == Mode.STRICT) {
                    throw new DITAOTException(MessageUtils.getMessage("DOTX008E", params).toString(), e);
                } else {
                    logger.error(MessageUtils.getMessage("DOTX008E", params).toString());
                }
            } else if (currentFile.equals(rootFile)) {
                throw new DITAOTException(MessageUtils.getMessage("DOTJ078F", params).toString() + " Cannot load file: " + e.getMessage(), e);
            } else if (processingMode == Mode.STRICT) {
                throw new DITAOTException(MessageUtils.getMessage("DOTJ079E", params).toString() + " Cannot load file: " + e.getMessage(), e);
            } else {
                logger.error(MessageUtils.getMessage("DOTJ079E", params).toString() + " Cannot load file: " + e.getMessage());
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
            if (failureList.contains(currentFile)) {
                FileUtils.deleteQuietly(outputFile);
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
        // Category non-copyto result
        for (final Reference file: listFilter.getNonCopytoResult()) {
            categorizeReferenceFile(file);
        }
        for (final Map.Entry<URI, URI> e : listFilter.getCopytoMap().entrySet()) {
            final URI source = e.getValue();
            final URI target = e.getKey();
            copyTo.put(target, source);
        }
        schemeSet.addAll(listFilter.getSchemeRefSet());

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
                            .uri(tempFileNameScheme.generateTempFileName(currentFile))
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
    abstract void categorizeReferenceFile(final Reference file);

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
        filterUtils.setJob(job);
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
        for (final String format: htmlSet.keySet()) {
            for (final URI file : htmlSet.get(format)) {
                getOrCreateFileInfo(fileinfos, file).format = format;
            }
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
                final URI src = filteredCopyTo.get(fs.src);
                // correct copy-to
                if (src != null) {
                    final FileInfo corr = new FileInfo.Builder(fs).src(src).build();
                    job.add(corr);
                } else {
                    job.add(fs);
                }
            }
        }
        for (final URI target : filteredCopyTo.keySet()) {
            final URI tmp = tempFileNameScheme.generateTempFileName(target);
            final URI src = filteredCopyTo.get(target);
            final FileInfo fi = new FileInfo.Builder()
                    .result(target)
                    .uri(tmp).build();
            // FIXME: what's the correct value for this? Accept all?
            if (formatFilter.test(fi.format)
                    || fi.format == null || fi.format.equals(ATTR_FORMAT_VALUE_DITA)) {
                job.add(fi);
            }
        }

        final FileInfo root = job.getFileInfo(rootFile);
        if (root == null) {
            throw new RuntimeException("Unable to set input file to job configuration");
        }
        job.add(new FileInfo.Builder(root)
                .isInput(true)
                .build());

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

        initXMLReader(ditaDir, validate);
        initFilters();
        if (filterUtils != null) {
        	filterUtils.setJob(job);
        }
    }

}