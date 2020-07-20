/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2004, 2005 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.

 */
package org.dita.dost.module;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.exception.DITAOTXMLErrorHandler;
import org.dita.dost.module.reader.TempFileNameScheme;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.reader.DitaValReader;
import org.dita.dost.reader.SubjectSchemeReader;
import org.dita.dost.util.*;
import org.dita.dost.writer.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.*;
import org.xml.sax.ext.LexicalHandler;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.dita.dost.reader.GenListModuleReader.ROOT_URI;
import static org.dita.dost.reader.GenListModuleReader.isFormatDita;
import static org.dita.dost.util.Configuration.Mode;
import static org.dita.dost.util.Configuration.printTranstype;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.FileUtils.getRelativePath;
import static org.dita.dost.util.FileUtils.resolve;
import static org.dita.dost.util.FilterUtils.SUBJECT_SCHEME_EXTENSION;
import static org.dita.dost.util.Job.FileInfo;
import static org.dita.dost.util.URLUtils.exists;
import static org.dita.dost.util.URLUtils.toFile;
import static org.dita.dost.util.XMLUtils.close;


/**
 * DebugAndFilterModule implement the second step in preprocess. It will insert debug
 * information into every dita files and filter out the information that is not
 * necessary.
 *
 * @author Zhang, Yuan Peng
 */
public final class DebugAndFilterModule extends SourceReaderModule {

    private Mode processingMode;
    /** Generate {@code xtrf} and {@code xtrc} attributes */
    private boolean genDebugInfo;
    private boolean setSystemId;
    /** Profiling is enabled. */
    private boolean profilingEnabled;
    private String transtype;
    private File ditavalFile;
    private FilterUtils filterUtils;
    /** Absolute path to current destination file. */
    private File outputFile;
    private Map<QName, Map<String, Set<String>>> validateMap;
    private Map<QName, Map<String, String>> defaultValueMap;
    /** Absolute path to current source file. */
    private URI currentFile;
    private List<URI> resources;
    private Map<URI, Set<URI>> dic;
    private SubjectSchemeReader subjectSchemeReader;
    private FilterUtils baseFilterUtils;
    private DitaWriterFilter ditaWriterFilter;
    private TopicFragmentFilter topicFragmentFilter;
    private TempFileNameScheme tempFileNameScheme;

    @Override
    public void setJob(final Job job) {
        super.setJob(job);
        try {
            tempFileNameScheme = (TempFileNameScheme) Class.forName(job.getProperty("temp-file-name-scheme")).newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        tempFileNameScheme.setBaseDir(job.getInputDir());
    }

    @Override
    public AbstractPipelineOutput execute(final AbstractPipelineInput input) throws DITAOTException {
        if (logger == null) {
            throw new IllegalStateException("Logger not set");
        }
        try {
            readArguments(input);
            init();

            job.getFileInfo().stream()
                    .filter(f -> isFormatDita(f.format) || ATTR_FORMAT_VALUE_DITAMAP.equals(f.format))
                    .forEach(this::processFile);

            job.write();
        } catch (final RuntimeException e) {
            throw e;
        } catch (final Exception e) {
            e.printStackTrace();
            throw new DITAOTException("Exception doing debug and filter module processing: " + e.getMessage(), e);
        }

        return null;
    }

    private void processFile(final FileInfo f) {
        currentFile = f.src;
        if (f.src == null || !exists(f.src) || !f.src.equals(f.result)) {
            logger.warn("Ignoring a copy-to file " + f.result);
            return;
        }
        outputFile = new File(job.tempDir, f.file.getPath());
        logger.info("Processing " + f.src + " to " + outputFile.toURI());

        final Set<URI> schemaSet = dic.get(f.uri);
        if (schemaSet != null && !schemaSet.isEmpty()) {
            logger.debug("Loading subject schemes");
            subjectSchemeReader.reset();
            for (final URI schema : schemaSet) {
                subjectSchemeReader.loadSubjectScheme(new File(job.tempDirURI.resolve(schema.getPath() + SUBJECT_SCHEME_EXTENSION)));
            }
            validateMap = subjectSchemeReader.getValidValuesMap();
            defaultValueMap = subjectSchemeReader.getDefaultValueMap();
        } else {
            validateMap = Collections.emptyMap();
            defaultValueMap = Collections.emptyMap();
        }
        if (profilingEnabled) {
            filterUtils = baseFilterUtils.refine(subjectSchemeReader.getSubjectSchemeMap());
        }

        InputSource in = null;
        try {
            reader.setErrorHandler(new DITAOTXMLErrorHandler(currentFile.toString(), logger));

            XMLReader parser = getXmlReader(f.format);
            XMLReader xmlSource = parser;
            for (final XMLFilter filter: getProcessingPipe(currentFile)) {
                filter.setParent(xmlSource);
                xmlSource = filter;
            }
            // ContentHandler must be reset so e.g. Saxon 9.1 will reassign ContentHandler
            // when reusing filter with multiple Transformers.
            xmlSource.setContentHandler(null);

            try {
                final LexicalHandler lexicalHandler = new DTDForwardHandler(xmlSource);
                parser.setProperty("http://xml.org/sax/properties/lexical-handler", lexicalHandler);
                parser.setFeature("http://xml.org/sax/features/lexical-handler", true);
            } catch (final SAXNotRecognizedException e) {}

            in = new InputSource(f.src.toString());

            final ContentHandler result = job.getStore().getContentHandler(outputFile.toURI());

            xmlSource.setContentHandler(result);
            xmlSource.parse(in);
        } catch (final RuntimeException e) {
            throw e;
        } catch (final Exception e) {
            logger.error(e.getMessage(), e) ;
        } finally {
            try {
                close(in);
            } catch (final IOException e) {
                logger.error(e.getMessage(), e) ;
            }
        }

        if (isFormatDita(f.format)) {
            f.format = ATTR_FORMAT_VALUE_DITA;
        }
    }

    private void init() throws IOException, DITAOTException, SAXException {
        initXmlReader();

        // Output subject schemas
        outputSubjectScheme();
        subjectSchemeReader = new SubjectSchemeReader();
        subjectSchemeReader.setLogger(logger);
        subjectSchemeReader.setJob(job);
        dic = SubjectSchemeReader.readMapFromXML(new File(job.tempDir, FILE_NAME_SUBJECT_DICTIONARY));

        if (profilingEnabled) {
            final DitaValReader filterReader = new DitaValReader();
            filterReader.setLogger(logger);
            filterReader.setJob(job);
            if (ditavalFile != null && ditavalFile.exists()) {
                filterReader.read(ditavalFile.getAbsoluteFile());
                baseFilterUtils = new FilterUtils(printTranstype.contains(transtype), filterReader.getFilterMap(),
                        filterReader.getForegroundConflictColor(), filterReader.getBackgroundConflictColor());
            } else {
                baseFilterUtils = new FilterUtils(printTranstype.contains(transtype));
            }
            baseFilterUtils.setLogger(logger);
        }

        initFilters();
    }

    /**
     * Initialize reusable filters.
     */
    private void initFilters() {
        ditaWriterFilter = new DitaWriterFilter();
        ditaWriterFilter.setLogger(logger);
        ditaWriterFilter.setJob(job);
        ditaWriterFilter.setEntityResolver(reader.getEntityResolver());

        topicFragmentFilter = new TopicFragmentFilter(ATTRIBUTE_NAME_CONREF, ATTRIBUTE_NAME_CONREFEND);

        tempFileNameScheme.setBaseDir(job.getInputDir());
    }

    /**
     * Get pipe line filters
     *
     * @param fileToParse absolute URI to current file being processed
     */
    @Override
    List<XMLFilter> getProcessingPipe(final URI fileToParse) {
        final List<XMLFilter> pipe = new ArrayList<>();

        if (genDebugInfo) {
            final DebugFilter debugFilter = new DebugFilter();
            debugFilter.setLogger(logger);
            debugFilter.setCurrentFile(currentFile);
            pipe.add(debugFilter);
        }

//        if (currentFile.equals(rootFile)) {
//            final ResourceInsertFilter filter = new ResourceInsertFilter();
//            filter.setLogger(logger);
//            filter.setResources(resources);
//            filter.setCurrentFile(currentFile);
//            pipe.add(filter);
//        }

        if (filterUtils != null) {
            final ProfilingFilter profilingFilter = new ProfilingFilter();
            profilingFilter.setLogger(logger);
            profilingFilter.setJob(job);
            profilingFilter.setFilterUtils(filterUtils);
            profilingFilter.setCurrentFile(fileToParse);
            pipe.add(profilingFilter);
        }

        final ValidationFilter validationFilter = new ValidationFilter();
        validationFilter.setLogger(logger);
        validationFilter.setValidateMap(validateMap);
        validationFilter.setCurrentFile(fileToParse);
        validationFilter.setJob(job);
        validationFilter.setProcessingMode(processingMode);
        pipe.add(validationFilter);

        final NormalizeFilter normalizeFilter = new NormalizeFilter();
        normalizeFilter.setLogger(logger);
        pipe.add(normalizeFilter);

        pipe.add(topicFragmentFilter);

        pipe.addAll(super.getProcessingPipe(fileToParse));
//        linkRewriteFilter.setCurrentFile(currentFile);
//        pipe.add(linkRewriteFilter);

        ditaWriterFilter.setDefaultValueMap(defaultValueMap);
        ditaWriterFilter.setCurrentFile(currentFile);
        ditaWriterFilter.setOutputFile(outputFile);
        pipe.add(ditaWriterFilter);

        return pipe;
    }

    private void readArguments(AbstractPipelineInput input) {
        transtype = input.getAttribute(ANT_INVOKER_EXT_PARAM_TRANSTYPE);
        profilingEnabled = true;
        if (input.getAttribute(ANT_INVOKER_PARAM_PROFILING_ENABLED) != null) {
            profilingEnabled = Boolean.parseBoolean(input.getAttribute(ANT_INVOKER_PARAM_PROFILING_ENABLED));
        }
        if (profilingEnabled) {
            ditavalFile = new File(job.tempDir, FILE_NAME_MERGED_DITAVAL);
        }
        gramcache = "yes".equalsIgnoreCase(input.getAttribute(ANT_INVOKER_EXT_PARAM_GRAMCACHE));
        validate = Boolean.valueOf(input.getAttribute(ANT_INVOKER_EXT_PARAM_VALIDATE));
        setSystemId = "yes".equals(input.getAttribute(ANT_INVOKER_EXT_PARAN_SETSYSTEMID));
        genDebugInfo = Boolean.valueOf(input.getAttribute(ANT_INVOKER_EXT_PARAM_GENERATE_DEBUG_ATTR));
        final String mode = input.getAttribute(ANT_INVOKER_EXT_PARAM_PROCESSING_MODE);
        processingMode = mode != null ? Mode.valueOf(mode.toUpperCase()) : Mode.LAX;

        if (input.getAttribute(ANT_INVOKER_PARAM_RESOURCES) != null) {
            resources = Stream.of(input.getAttribute(ANT_INVOKER_PARAM_RESOURCES).split(File.pathSeparator))
                    .map(resource -> new File(resource).toURI())
                    .collect(Collectors.toList());
        } else {
            resources = Collections.emptyList();
        }
    }


    /**
     * Output subject schema file.
     *
     * @throws DITAOTException if generation files
     */
    private void outputSubjectScheme() throws DITAOTException {
        try {
            final Map<URI, Set<URI>> graph = SubjectSchemeReader.readMapFromXML(new File(job.tempDir, FILE_NAME_SUBJECT_RELATION));

            final Queue<URI> queue = new LinkedList<>(graph.keySet());
            final Set<URI> visitedSet = new HashSet<>();

            while (!queue.isEmpty()) {
                final URI parent = queue.poll();
                final Set<URI> children = graph.get(parent);

                if (children != null) {
                    queue.addAll(children);
                }
                if (ROOT_URI.equals(parent) || visitedSet.contains(parent)) {
                    continue;
                }
                visitedSet.add(parent);
                final File tmprel = new File(FileUtils.resolve(job.tempDir, parent) + SUBJECT_SCHEME_EXTENSION);
                final Document parentRoot;
                if (!tmprel.exists()) {
                    final URI src = job.getFileInfo(parent).src;
                    parentRoot = job.getStore().getDocument(src);
                } else {
                    parentRoot = job.getStore().getDocument(tmprel.toURI());
                }
                if (children != null) {
                    for (final URI childpath: children) {
                        final Document childRoot = job.getStore().getImmutableDocument(job.getInputFile().resolve(childpath.getPath()));
                        mergeScheme(parentRoot, childRoot);
                        generateScheme(new File(job.tempDir, childpath.getPath() + SUBJECT_SCHEME_EXTENSION), childRoot);
                    }
                }

                //Output parent scheme
                generateScheme(new File(job.tempDir, parent.getPath() + SUBJECT_SCHEME_EXTENSION), parentRoot);
            }
        } catch (final RuntimeException e) {
            throw e;
        } catch (final Exception e) {
            logger.error(e.getMessage(), e) ;
            throw new DITAOTException(e);
        }

    }

    private void mergeScheme(final Document parentRoot, final Document childRoot) {
        final Queue<Element> pQueue = new LinkedList<>();
        pQueue.offer(parentRoot.getDocumentElement());

        while (!pQueue.isEmpty()) {
            final Element pe = pQueue.poll();
            NodeList pList = pe.getChildNodes();
            for (int i = 0; i < pList.getLength(); i++) {
                final Node node = pList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    pQueue.offer((Element)node);
                }
            }

            String value = pe.getAttribute(ATTRIBUTE_NAME_CLASS);
            if (StringUtils.isEmptyString(value)
                    || !SUBJECTSCHEME_SUBJECTDEF.matches(value)) {
                continue;
            }

            if (!StringUtils.isEmptyString(
                    value = pe.getAttribute(ATTRIBUTE_NAME_KEYREF))) {
                // extend child scheme
                final Element target = searchForKey(childRoot.getDocumentElement(), value);
                if (target == null) {
                    /*
                     * TODO: we have a keyref here to extend into child scheme, but can't
                     * find any matching <subjectdef> in child scheme. Shall we throw out
                     * a warning?
                     *
                     * Not for now, just bypass it.
                     */
                    continue;
                }

                // target found
                pList = pe.getChildNodes();
                for (int i = 0; i < pList.getLength(); i++) {
                    final Node tmpnode = childRoot.importNode(pList.item(i), false);
                    if (tmpnode.getNodeType() == Node.ELEMENT_NODE
                            && searchForKey(target,
                                    ((Element)tmpnode).getAttribute(ATTRIBUTE_NAME_KEYS)) != null) {
                        continue;
                    }
                    target.appendChild(tmpnode);
                }

            } else if (!StringUtils.isEmptyString(
                    value = pe.getAttribute(ATTRIBUTE_NAME_KEYS))) {
                // merge into parent scheme
                final Element target = searchForKey(childRoot.getDocumentElement(), value);
                if (target != null) {
                    pList = target.getChildNodes();
                    for (int i = 0; i < pList.getLength(); i++) {
                        final Node tmpnode = parentRoot.importNode(pList.item(i), false);
                        if (tmpnode.getNodeType() == Node.ELEMENT_NODE
                                && searchForKey(pe,
                                        ((Element)tmpnode).getAttribute(ATTRIBUTE_NAME_KEYS)) != null) {
                            continue;
                        }
                        pe.appendChild(tmpnode);
                    }
                }
            }
        }
    }

    private Element searchForKey(final Element root, final String key) {
        if (root == null || StringUtils.isEmptyString(key)) {
            return null;
        }
        final Queue<Element> queue = new LinkedList<>();
        queue.offer(root);

        while (!queue.isEmpty()) {
            final Element pe = queue.poll();
            final NodeList pchildrenList = pe.getChildNodes();
            for (int i = 0; i < pchildrenList.getLength(); i++) {
                final Node node = pchildrenList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    queue.offer((Element)node);
                }
            }

            String value = pe.getAttribute(ATTRIBUTE_NAME_CLASS);
            if (StringUtils.isEmptyString(value)
                    || !SUBJECTSCHEME_SUBJECTDEF.matches(value)) {
                continue;
            }

            value = pe.getAttribute(ATTRIBUTE_NAME_KEYS);
            if (StringUtils.isEmptyString(value)) {
                continue;
            }

            if (value.equals(key)) {
                return pe;
            }
        }
        return null;
    }

    /**
     * Serialize subject scheme file.
     *
     * @param filename output filepath
     * @param root subject scheme document
     *
     * @throws DITAOTException if generation fails
     */
    private void generateScheme(final File filename, final Document root) throws DITAOTException {
        try {
            job.getStore().writeDocument(root, filename.toURI());
        } catch (final IOException e) {
            logger.error(e.getMessage(), e) ;
            throw new DITAOTException(e);
        }
    }

    /**
     * Get path to base directory
     *
     * @param filename relative input file path from base directory
     * @param traceFilename absolute input file
     * @param inputMap absolute path to start file
     * @return path to base directory, {@code null} if not available
     */
    public static File getPathtoProject(final File filename, final File traceFilename, final File inputMap, final Job job) {
        if (job.getGeneratecopyouter() != Job.Generate.OLDSOLUTION) {
            if (isOutFile(traceFilename, inputMap)) {
                return toFile(getRelativePathFromOut(traceFilename.getAbsoluteFile(), job));
            } else {
                return getRelativePath(traceFilename.getAbsoluteFile(), inputMap.getAbsoluteFile()).getParentFile();
            }
        } else {
            return FileUtils.getRelativePath(filename);
        }
    }
    /**
     * Just for the overflowing files.
     * @param overflowingFile overflowingFile
     * @return relative system path to out which ends in {@link java.io.File#separator File.separator}
     */
    private static String getRelativePathFromOut(final File overflowingFile, final Job job) {
        final URI relativePath = URLUtils.getRelativePath(job.getInputFile(), overflowingFile.toURI());
        final File outputDir = job.getOutputDir().getAbsoluteFile();
        final File outputPathName = new File(outputDir, "index.html");
        final File finalOutFilePathName = resolve(outputDir, relativePath.getPath());
        final File finalRelativePathName = FileUtils.getRelativePath(finalOutFilePathName, outputPathName);
        File parentDir = finalRelativePathName.getParentFile();
        if (parentDir == null || parentDir.getPath().isEmpty()) {
            parentDir = new File(".");
        }
        return parentDir.getPath() + File.separator;
    }

    /**
     * Check if path falls outside start document directory
     *
     * @param filePathName absolute path to test
     * @param inputMap absolute input map path
     * @return {@code true} if outside start directory, otherwise {@code false}
     */
    private static boolean isOutFile(final File filePathName, final File inputMap) {
        final File relativePath = FileUtils.getRelativePath(inputMap.getAbsoluteFile(), filePathName.getAbsoluteFile());
        return !(relativePath.getPath().length() == 0 || !relativePath.getPath().startsWith(".."));
    }

    /**
     * Lexical handler to forward DTD declaration into processing instructions.
     */
    private final class DTDForwardHandler implements LexicalHandler {

        private final XMLReader parser;

        public DTDForwardHandler(XMLReader parser) {
            this.parser = parser;
        }

        @Override
        public void startDTD(final String name, final String publicId, final String systemId) throws SAXException {
            if (publicId != null && !publicId.isEmpty()) {
                parser.getContentHandler().processingInstruction("doctype-public", publicId);
            }
            if (systemId != null && !systemId.isEmpty()) {
                parser.getContentHandler().processingInstruction("doctype-system", systemId);
            }
        }

        @Override
        public void endDTD() throws SAXException {}

        @Override
        public void startEntity(String name) throws SAXException {}

        @Override
        public void endEntity(String name) throws SAXException {}

        @Override
        public void startCDATA() throws SAXException {}

        @Override
        public void endCDATA() throws SAXException {}

        @Override
        public void comment(char[] ch, int start, int length) throws SAXException {}
    }
}
