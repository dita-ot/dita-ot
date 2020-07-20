/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2004, 2005 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.

 */
package org.dita.dost.module;

import com.google.common.collect.MultimapBuilder.SetMultimapBuilder;
import com.google.common.collect.SetMultimap;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.exception.DITAOTXMLErrorHandler;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.module.reader.TempFileNameScheme;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.reader.DitaValReader;
import org.dita.dost.reader.GenListModuleReader;
import org.dita.dost.reader.KeydefFilter;
import org.dita.dost.reader.SubjectSchemeReader;
import org.dita.dost.util.*;
import org.dita.dost.writer.DebugFilter;
import org.dita.dost.writer.ExportAnchorsFilter;
import org.dita.dost.writer.ProfilingFilter;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.dita.dost.reader.GenListModuleReader.*;
import static org.dita.dost.util.Configuration.Mode;
import static org.dita.dost.util.Configuration.printTranstype;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.Job.*;
import static org.dita.dost.util.URLUtils.*;

/**
 * This class extends AbstractPipelineModule, used to generate map and topic
 * list by parsing all the refered dita files.
 *
 * @version 1.0 2004-11-25
 *
 * @author Wu, Zhi Qiang
 */
public final class GenMapAndTopicListModule extends SourceReaderModule {

    public static final String ELEMENT_STUB = "stub";
    /** Generate {@code xtrf} and {@code xtrc} attributes */
    private boolean genDebugInfo;
    private Mode processingMode;
    /** FileInfos keyed by src. */
    private final Map<URI, FileInfo> fileinfos = new HashMap<>();
    /** Set of all topic files */
    private final Set<URI> fullTopicSet;

    /** Set of all map files */
    private final Set<URI> fullMapSet;

    /** Set of topic files containing href */
    private final Set<URI> hrefTopicSet;

    /** Set of dita files containing conref */
    private final Set<URI> conrefSet;

    /** Set of topic files containing coderef */
    private final Set<URI> coderefSet;

    /** Set of all images */
    private final Set<Reference> formatSet;

    /** Set of all images used for flagging */
    private final Set<URI> flagImageSet;

    /** Set of all HTML and other non-DITA or non-image files */
    private final SetMultimap<String, URI> htmlSet;

    /** Set of all the href targets */
    private final Set<URI> hrefTargetSet;

    /** Set of all the conref targets */
    private Set<URI> conrefTargetSet;

    /** Set of all the non-conref targets */
    private final Set<URI> nonConrefCopytoTargetSet;

    /** Set of subsidiary files */
    private final Set<URI> coderefTargetSet;

    /** Set of absolute flag image files */
    private final Set<URI> relFlagImagesSet;

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

    /** Absolute basedir for processing */
    private URI baseInputDir;

    /** Profiling is enabled. */
    private boolean profilingEnabled;
    /** Absolute path for filter file. */
    private File ditavalFile;
    /** Number of directory levels base directory is adjusted. */
    private int uplevels = 0;

    private GenListModuleReader listFilter;
    private KeydefFilter keydefFilter;
    private ExportAnchorsFilter exportAnchorsFilter;
    private ContentHandler nullHandler;
    private FilterUtils filterUtils;
    private TempFileNameScheme tempFileNameScheme;

    /** Absolute path to input file. */
    private URI rootFile;
    private List<URI> resources;
    /** File currently being processed */
    private URI currentFile;
    /** Subject scheme key map. Key is key value, value is key definition. */
    private Map<String, KeyDef> schemekeydefMap;
    /** Subject scheme absolute file paths. */
    private final Set<URI> schemeSet;
    /** Subject scheme usage. Key is absolute file path, value is set of applicable subject schemes. */
    private final Map<URI, Set<URI>> schemeDictionary;
    private final Map<URI, URI> copyTo = new HashMap<>();
    private String transtype;

    private boolean setSystemid = true;
    /** Formats for source topics */
    // XXX This is a hack to retain format. A better solution would be to keep the format with the source URI
    private final Map<URI, String> sourceFormat = new HashMap<>();

    /**
     * Create a new instance and do the initialization.
     */
    public GenMapAndTopicListModule() {
        super();
        fullTopicSet = new HashSet<>(128);
        fullMapSet = new HashSet<>(128);
        hrefTopicSet = new HashSet<>(128);
        schemeSet = new HashSet<>(128);
        conrefSet = new HashSet<>(128);
        formatSet = new HashSet<>();
        flagImageSet = new LinkedHashSet<>(128);
        htmlSet = SetMultimapBuilder.hashKeys().hashSetValues().build();
        hrefTargetSet = new HashSet<>(128);
        coderefTargetSet = new HashSet<>(16);
        waitList = new LinkedList<>();
        doneList = new LinkedList<>();
        failureList = new LinkedList<>();
        conrefTargetSet = new HashSet<>(128);
        nonConrefCopytoTargetSet = new HashSet<>(128);
        outDitaFilesSet = new HashSet<>(128);
        relFlagImagesSet = new LinkedHashSet<>(128);
        conrefpushSet = new HashSet<>(128);
        keyrefSet = new HashSet<>(128);
        coderefSet = new HashSet<>(128);

        schemeDictionary = new HashMap<>();

        // @processing-role
        resourceOnlySet = new HashSet<>(128);
    }

    @Override
    public AbstractPipelineOutput execute(final AbstractPipelineInput input) throws DITAOTException {
        if (logger == null) {
            throw new IllegalStateException("Logger not set");
        }

        try {
            parseInputParameters(input);

            initFilters();
            initXmlReader();

            readResourceFiles();
            readStartFile();
            processWaitList();

            updateBaseDirectory();
            handleConref();
            outputResult();
        } catch (final DITAOTException e) {
            throw e;
        } catch (final Exception e) {
            throw new DITAOTException(e.getMessage(), e);
        }

        return null;
    }

    private void readResourceFiles() throws DITAOTException {
        if (!resources.isEmpty()) {
            for (URI resource : resources) {
                addToWaitList(new Reference(resource));
            }
            processWaitList();

            resourceOnlySet.addAll(hrefTargetSet);
            resourceOnlySet.addAll(conrefTargetSet);
            resourceOnlySet.addAll(nonConrefCopytoTargetSet);
            resourceOnlySet.addAll(outDitaFilesSet);
            resourceOnlySet.addAll(conrefpushSet);
            resourceOnlySet.addAll(keyrefSet);
            resourceOnlySet.addAll(resourceOnlySet);
            resourceOnlySet.addAll(fullTopicSet);
            resourceOnlySet.addAll(fullMapSet);
            resourceOnlySet.addAll(conrefSet);
        }
    }

    private void readStartFile() throws DITAOTException {
        addToWaitList(new Reference(rootFile));
    }

    /**
     * Initialize reusable filters.
     */
    private void initFilters() {
        listFilter = new GenListModuleReader();
        listFilter.setLogger(logger);
        listFilter.setPrimaryDitamap(rootFile);
        listFilter.setJob(job);

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
    }

    private void parseInputParameters(final AbstractPipelineInput input) {
        ditavalFile = new File(job.tempDir, FILE_NAME_MERGED_DITAVAL);
        validate = Boolean.valueOf(input.getAttribute(ANT_INVOKER_EXT_PARAM_VALIDATE));
        if (!validate) {
            final String msg = MessageUtils.getMessage("DOTJ037W").toString();
            logger.warn(msg);
        }
        transtype = input.getAttribute(ANT_INVOKER_EXT_PARAM_TRANSTYPE);
        gramcache = "yes".equalsIgnoreCase(input.getAttribute(ANT_INVOKER_EXT_PARAM_GRAMCACHE));
        setSystemid = "yes".equalsIgnoreCase(input.getAttribute(ANT_INVOKER_EXT_PARAN_SETSYSTEMID));
        final String mode = input.getAttribute(ANT_INVOKER_EXT_PARAM_PROCESSING_MODE);
        processingMode = mode != null ? Mode.valueOf(mode.toUpperCase()) : Mode.LAX;
        genDebugInfo = Boolean.valueOf(input.getAttribute(ANT_INVOKER_EXT_PARAM_GENERATE_DEBUG_ATTR));

        // For the output control
        job.setGeneratecopyouter(input.getAttribute(ANT_INVOKER_EXT_PARAM_GENERATECOPYOUTTER));
        job.setOutterControl(input.getAttribute(ANT_INVOKER_EXT_PARAM_OUTTERCONTROL));
        job.setOnlyTopicInMap(Boolean.valueOf(input.getAttribute(ANT_INVOKER_EXT_PARAM_ONLYTOPICINMAP)));
        job.setCrawl(input.getAttribute(ANT_INVOKER_EXT_PARAM_CRAWL));

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

        if (input.getAttribute(ANT_INVOKER_PARAM_RESOURCES) != null) {
            resources = Stream.of(input.getAttribute(ANT_INVOKER_PARAM_RESOURCES).split(File.pathSeparator))
                    .map(resource -> new File(resource).toURI())
                    .collect(Collectors.toList());
        } else {
            resources = Collections.emptyList();
        }

        final URI ditaInput = toURI(input.getAttribute(ANT_INVOKER_PARAM_INPUTMAP));
        if (ditaInput.isAbsolute()) {
            rootFile = ditaInput;
        } else if (ditaInput.getPath() != null && ditaInput.getPath().startsWith(URI_SEPARATOR)) {
            rootFile = setScheme(ditaInput, "file");
        } else if (baseInputDir != null) {
            rootFile = baseInputDir.resolve(ditaInput);
        } else {
            rootFile = basedir.toURI().resolve(ditaInput);
        }
        assert rootFile.isAbsolute();

        if (baseInputDir == null) {
            baseInputDir = rootFile.resolve(".");
        }
        assert baseInputDir.isAbsolute();

        profilingEnabled = true;
        if (input.getAttribute(ANT_INVOKER_PARAM_PROFILING_ENABLED) != null) {
            profilingEnabled = Boolean.parseBoolean(input.getAttribute(ANT_INVOKER_PARAM_PROFILING_ENABLED));
        }

        // create the keydef file for scheme files
        schemekeydefMap = new HashMap<>();

        // Set the mapDir
        job.setInputFile(rootFile);
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
    @Override
    List<XMLFilter> getProcessingPipe(final URI fileToParse) {
        assert fileToParse.isAbsolute();
        final List<XMLFilter> pipe = new ArrayList<>();

        if (genDebugInfo) {
            final DebugFilter debugFilter = new DebugFilter();
            debugFilter.setLogger(logger);
            debugFilter.setCurrentFile(currentFile);
            pipe.add(debugFilter);
        }

        if (filterUtils != null) {
            final ProfilingFilter profilingFilter = new ProfilingFilter(false);
            profilingFilter.setLogger(logger);
            profilingFilter.setJob(job);
            profilingFilter.setFilterUtils(filterUtils);
            profilingFilter.setCurrentFile(fileToParse);
            pipe.add(profilingFilter);
        }

        if (INDEX_TYPE_ECLIPSEHELP.equals(transtype)) {
            exportAnchorsFilter.setCurrentFile(fileToParse);
            exportAnchorsFilter.setErrorHandler(new DITAOTXMLErrorHandler(fileToParse.toString(), logger));
            pipe.add(exportAnchorsFilter);
        }

        keydefFilter.setCurrentDir(fileToParse.resolve("."));
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
                logger.error(MessageUtils.getMessage("DOTJ021E", params).toString());
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
     * Process results from parsing a single topic or map
     *
     * @param currentFile absolute URI processes files
     */
    private void processParseResult(final URI currentFile) {
        // Category non-copyto result and update uplevels accordingly
        final Set<Reference> nonCopytoResult = new LinkedHashSet<>(128);
        nonCopytoResult.addAll(listFilter.getNonConrefCopytoTargets());
        for (final URI f : listFilter.getConrefTargets()) {
            nonCopytoResult.add(new Reference(stripFragment(f), listFilter.currentFileFormat()));
        }
        for (final URI f : listFilter.getCopytoMap().values()) {
            nonCopytoResult.add(new Reference(stripFragment(f)));
        }
        for (final URI f : listFilter.getIgnoredCopytoSourceSet()) {
            nonCopytoResult.add(new Reference(stripFragment(f)));
        }
        for (final URI filename1 : listFilter.getCoderefTargetSet()) {
            nonCopytoResult.add(new Reference(stripFragment(filename1)));
        }
        for (final Reference file: nonCopytoResult) {
            categorizeReferenceFile(file);
            updateUplevels(file.filename);
        }
        for (final Map.Entry<URI, URI> e : listFilter.getCopytoMap().entrySet()) {
            final URI source = e.getValue();
            final URI target = e.getKey();
            copyTo.put(target, source);
            updateUplevels(target);

        }
        final Set<URI> nonTopicrefReferenceSet = new HashSet<>();
        nonTopicrefReferenceSet.addAll(listFilter.getNonTopicrefReferenceSet());
        nonTopicrefReferenceSet.removeAll(listFilter.getNormalProcessingRoleSet());
        nonTopicrefReferenceSet.removeAll(listFilter.getResourceOnlySet());
        for (final URI file: nonTopicrefReferenceSet) {
            updateUplevels(file);
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
        final Set<URI> nonConrefCopytoTargets = listFilter.getNonConrefCopytoTargets().stream()
                .map(r -> r.filename)
                .collect(Collectors.toSet());
        nonConrefCopytoTargetSet.addAll(nonConrefCopytoTargets);
        coderefTargetSet.addAll(listFilter.getCoderefTargets());
        outDitaFilesSet.addAll(listFilter.getOutDitaFilesSet());

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
            if (ref.format != null && !ref.format.equals(ATTR_FORMAT_VALUE_DITA)) {
                assert currentFile.getFragment() == null;
                if (!sourceFormat.containsKey(currentFile)) {
                    sourceFormat.put(currentFile, ref.format);
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
        if (listFilter.getCoderefTargets().contains(file.filename)) {
            return;
        }
        if (isFormatDita(file.format) && listFilter.isDitaTopic() &&
                !job.crawlTopics() &&
                !listFilter.getConrefTargets().contains(file.filename)) {
            return;  // Do not process topics linked from within topics
        } else if ((isFormatDita(file.format) || ATTR_FORMAT_VALUE_DITAMAP.equals(file.format))) {
            addToWaitList(file);
        } else if (ATTR_FORMAT_VALUE_IMAGE.equals(file.format)) {
            formatSet.add(file);
            if (!exists(file.filename)) {
                logger.warn(MessageUtils.getMessage("DOTX008E", file.filename.toString()).toString());
            }
        } else if (ATTR_FORMAT_VALUE_DITAVAL.equals(file.format)) {
            formatSet.add(file);
        } else {
            htmlSet.put(file.format, file.filename);
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
            baseInputDir = baseInputDir.resolve("..");
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
     * Parse filter file
     *
     * @return configured filter utility
     */
    private FilterUtils parseFilterFile() {
        final FilterUtils filterUtils;
        if (ditavalFile.exists()) {
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
    private void handleConref() {
        // Get pure conref targets
        final Set<URI> pureConrefTargets = new HashSet<>();
        for (final URI target: conrefTargetSet) {
            if (!nonConrefCopytoTargetSet.contains(target)) {
                pureConrefTargets.add(target);
            }
        }
        conrefTargetSet = pureConrefTargets;

        // Remove pure conref targets from fullTopicSet
        fullTopicSet.removeAll(pureConrefTargets);
        // Treat pure conref targets same as resource-only
        resourceOnlySet.addAll(pureConrefTargets);
    }

    /**
     * Write result files.
     *
     * @throws DITAOTException if writing result files failed
     */
    private void outputResult() throws DITAOTException {
        try {
            tempFileNameScheme = (TempFileNameScheme) Class.forName(job.getProperty("temp-file-name-scheme")).newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        tempFileNameScheme.setBaseDir(baseInputDir);

        // assume empty Job
        final URI rootTemp = tempFileNameScheme.generateTempFileName(rootFile);
        final File relativeRootFile = toFile(rootTemp);

        job.setInputDir(baseInputDir);
        job.setInputMap(rootTemp);

        //If root input file is marked resource only due to conref or other feature, remove that designation
        if (resourceOnlySet.contains(rootFile)) {
            resourceOnlySet.remove(rootFile);
        }

        job.setProperty(INPUT_DITAMAP_LIST_FILE_LIST, USER_INPUT_FILE_LIST_FILE);
        final File inputfile = new File(job.tempDir, USER_INPUT_FILE_LIST_FILE);
        writeListFile(inputfile, relativeRootFile.toString());

        job.setProperty("tempdirToinputmapdir.relative.value", StringUtils.escapeRegExp(getPrefix(relativeRootFile)));
        job.setProperty("uplevels", getLevelsPath(rootTemp));

        resourceOnlySet.addAll(resources);

        final Set<URI> res = new HashSet<>();
        res.addAll(listFilter.getResourceOnlySet());
        res.removeAll(listFilter.getNormalProcessingRoleSet());
        resourceOnlySet.addAll(res);

        if (job.getOnlyTopicInMap() || !job.crawlTopics()) {
            final Set<URI> res1 = new HashSet<>();
            res1.addAll(listFilter.getNonTopicrefReferenceSet());
            res1.removeAll(listFilter.getNormalProcessingRoleSet());
            res1.removeAll(listFilter.getResourceOnlySet());
            resourceOnlySet.addAll(res1);
        }

        for (final URI file: outDitaFilesSet) {
            getOrCreateFileInfo(fileinfos, file).isOutDita = true;
        }
        for (final URI file: fullTopicSet) {
            final FileInfo ff = getOrCreateFileInfo(fileinfos, file);
            if (ff.format == null) {
                ff.format = sourceFormat.getOrDefault(ff.src, ATTR_FORMAT_VALUE_DITA);
            }
        }
        for (final URI file: fullMapSet) {
            final FileInfo ff = getOrCreateFileInfo(fileinfos, file);
            if (ff.format == null) {
                ff.format = ATTR_FORMAT_VALUE_DITAMAP;
            }
        }
        for (final URI file: hrefTopicSet) {
            final FileInfo f = getOrCreateFileInfo(fileinfos, file);
            f.hasLink = true;
            if (f.format == null && sourceFormat.containsKey(f.src)) {
                f.format = sourceFormat.get(f.src);
            }
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
            final FileInfo f = getOrCreateFileInfo(fileinfos, file);
            f.isTarget = true;
            if (f.format == null && sourceFormat.containsKey(f.src)) {
                f.format = sourceFormat.get(f.src);
            }
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
        for (final URI resource : resources) {
            getOrCreateFileInfo(fileinfos, resource).isInputResource = true;
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
            final FileInfo fi = new FileInfo.Builder().result(target).uri(tmp).build();
            job.add(fi);
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

        KeyDef.writeKeydef(new File(job.tempDir, SUBJECT_SCHEME_KEYDEF_LIST_FILE), addFilePrefix(schemekeydefMap.values()));
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
            logger.error(e.getMessage(), e) ;
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

    /**
     * Convert absolute paths to relative temporary directory paths
     * @return map with relative keys and values
     */
    private Map<URI, Set<URI>> addMapFilePrefix(final Map<URI, Set<URI>> map) {
        final Map<URI, Set<URI>> res = new HashMap<>();
        for (final Map.Entry<URI, Set<URI>> e: map.entrySet()) {
            final URI key = e.getKey();
            final Set<URI> newSet = new HashSet<>();
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
        final Map<URI, URI> newSet = new HashMap<>();
        for (final Map.Entry<URI, URI> file: set.entrySet()) {
            final URI key = tempFileNameScheme.generateTempFileName(file.getKey());
            final URI value = tempFileNameScheme.generateTempFileName(file.getValue());
            newSet.put(key, value);
        }
        return newSet;
    }

    private Collection<KeyDef> addFilePrefix(final Collection<KeyDef> keydefs) {
        final Collection<KeyDef> res = new ArrayList<>(keydefs.size());
        for (final KeyDef k: keydefs) {
            final URI source = tempFileNameScheme.generateTempFileName(k.source);
            res.add(new KeyDef(k.keys, k.href, k.scope, k.format, source, null));
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
        final String fileKey = org.dita.dost.util.Constants.REL_FLAGIMAGE_LIST.substring(0, org.dita.dost.util.Constants.REL_FLAGIMAGE_LIST.lastIndexOf("list")) + "file";
        prop.setProperty(fileKey, org.dita.dost.util.Constants.REL_FLAGIMAGE_LIST.substring(0, org.dita.dost.util.Constants.REL_FLAGIMAGE_LIST.lastIndexOf("list")) + ".list");
        final File list = new File(job.tempDir, prop.getProperty(fileKey));
        try (Writer bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(list)))) {
            final Iterator<URI> it = newSet.iterator();
            while (it.hasNext()) {
                bufferedWriter.write(it.next().getPath());
                if (it.hasNext()) {
                    bufferedWriter.write("\n");
                }
            }
            bufferedWriter.flush();
        } catch (final IOException e) {
            logger.error(e.getMessage(), e) ;
        }

        prop.setProperty(org.dita.dost.util.Constants.REL_FLAGIMAGE_LIST, StringUtils.join(newSet, COMMA));
    }

}