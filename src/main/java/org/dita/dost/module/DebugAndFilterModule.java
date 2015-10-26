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
import static org.dita.dost.util.FileUtils.getRelativeUnixPath;
import static org.dita.dost.util.FileUtils.resolve;
import static org.dita.dost.util.Job.*;
import static org.dita.dost.util.Configuration.*;
import static org.dita.dost.util.URLUtils.*;
import static org.dita.dost.util.FilterUtils.*;
import static org.dita.dost.util.XMLUtils.*;

import java.io.*;
import java.net.URI;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.apache.xerces.xni.grammars.XMLGrammarPool;
import org.apache.xml.resolver.tools.CatalogResolver;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.exception.DITAOTXMLErrorHandler;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.reader.DitaValReader;
import org.dita.dost.reader.GrammarPoolManager;
import org.dita.dost.reader.SubjectSchemeReader;
import org.dita.dost.util.*;
import org.dita.dost.writer.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.*;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.XMLFilterImpl;


/**
 * DebugAndFilterModule implement the second step in preprocess. It will insert debug
 * information into every dita files and filter out the information that is not
 * necessary.
 * 
 * @author Zhang, Yuan Peng
 */
public final class DebugAndFilterModule extends AbstractPipelineModuleImpl {

    private final Map<File, File> copytoMap = new HashMap<>();
    private Mode processingMode;
    /** Generate {@code xtrf} and {@code xtrc} attributes */
    private boolean genDebugInfo;
    /** Absolute input map path. */
    private URI inputMap;
    /** use grammar pool cache */
    private boolean gramcache = true;
    private boolean setSystemId;
    /** Profiling is enabled. */
    private boolean profilingEnabled;
    private boolean validate;
    private String transtype;
    private boolean forceUnique;
    /** Absolute DITA-OT base path. */
    private File ditaDir;
    private File ditavalFile;
    private FilterUtils filterUtils;
    /** Absolute path to current destination file. */
    private File outputFile;
    private Map<String, Map<String, Set<String>>> validateMap;
    private Map<String, Map<String, String>> defaultValueMap;
    /** XMLReader instance for parsing dita file */
    private XMLReader reader;
    /** Absolute path to current source file. */
    private URI currentFile;
    private Map<URI, Set<URI>> dic;
    private SubjectSchemeReader subjectSchemeReader;
    private FilterUtils baseFilterUtils;
    private ForceUniqueFilter forceUniqueFilter;
    private DitaWriterFilter ditaWriterFilter;
    private TopicFragmentFilter topicFragmentFilter;

    @Override
    public AbstractPipelineOutput execute(final AbstractPipelineInput input) throws DITAOTException {
        if (logger == null) {
            throw new IllegalStateException("Logger not set");
        }
        try {
            readArguments(input);
            init();

            for (final FileInfo f: job.getFileInfo()) {
                if (isFormatDita(f.format) || ATTR_FORMAT_VALUE_DITAMAP.equals(f.format)
                        || f.isConrefTarget || f.isCopyToSource) {
                    processFile(f);
                }
            }

            performCopytoTask();

            job.write();
        } catch (final Exception e) {
            e.printStackTrace();
            throw new DITAOTException("Exception doing debug and filter module processing: " + e.getMessage(), e);
        }

        return null;
    }

    private void processFile(final FileInfo f) {
        currentFile = f.src;
        if (!exists(f.src)) { //copytoMap.containsKey(f.file)
            logger.debug("Ignoring a copy-to file " + f.src);
            return;
        }
        outputFile = new File(job.tempDir, f.file.getPath());
        final File outputDir = outputFile.getParentFile();
        if (!outputDir.exists() && !outputDir.mkdirs()) {
            logger.error("Failed to create output directory " + outputDir.getAbsolutePath());
            return;
        }
        logger.info("Processing " + f.src);

        final Set<URI> schemaSet = dic.get(f.uri);
        if (schemaSet != null && !schemaSet.isEmpty()) {
            logger.debug("Loading subject schemes");
            subjectSchemeReader.reset();
            for (final URI schema : schemaSet) {
                subjectSchemeReader.loadSubjectScheme(new File(job.tempDir.toURI().resolve(schema.getPath() + SUBJECT_SCHEME_EXTENSION)));
            }
            validateMap = subjectSchemeReader.getValidValuesMap();
            defaultValueMap = subjectSchemeReader.getDefaultValueMap();
        } else {
            validateMap = Collections.EMPTY_MAP;
            defaultValueMap = Collections.EMPTY_MAP;
        }
        if (profilingEnabled) {
            filterUtils = baseFilterUtils.refine(subjectSchemeReader.getSubjectSchemeMap());
        }

        InputSource in = null;
        OutputStream out = null;
        try {
            out = new FileOutputStream(outputFile);

            reader.setErrorHandler(new DITAOTXMLErrorHandler(currentFile.toString(), logger));

            final TransformerFactory tf = TransformerFactory.newInstance();
//            final Transformer serializer = tf.newTransformer();
            final SAXTransformerFactory stf = (SAXTransformerFactory) tf;
            final TransformerHandler serializer = stf.newTransformerHandler();

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
//            final Source source = new SAXSource(xmlSource, in);
            final Result result = new StreamResult(out);
//            serializer.transform(source, result);
            serializer.setResult(result);
            xmlSource.setContentHandler(serializer);
            xmlSource.parse(new InputSource(f.src.toString()));
        } catch (final RuntimeException e) {
            throw e;
        } catch (final Exception e) {
            logger.error(e.getMessage(), e) ;
        } finally {
            if (out != null) {
                try {
                    out.close();
                }catch (final Exception e) {
                    logger.error(e.getMessage(), e) ;
                }
            }
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

    private XMLReader getXmlReader(final String format) throws SAXException {
        for (final Map.Entry<String, String> e: parserMap.entrySet()) {
            if (format != null && format.equals(e.getKey())) {
                try {
                    return (XMLReader) this.getClass().forName(e.getValue()).newInstance();
                } catch (final InstantiationException | ClassNotFoundException | IllegalAccessException ex) {
                    throw new SAXException(ex);
                }
            }
        }
        return reader;
    }


    private void init() throws IOException, DITAOTException, SAXException {
        // Output subject schemas
        outputSubjectScheme();
        subjectSchemeReader = new SubjectSchemeReader();
        subjectSchemeReader.setLogger(logger);
        dic = SubjectSchemeReader.readMapFromXML(new File(job.tempDir, FILE_NAME_SUBJECT_DICTIONARY));

        if (profilingEnabled) {
            final DitaValReader filterReader = new DitaValReader();
            filterReader.setLogger(logger);
            filterReader.initXMLReader(setSystemId);
            Map<FilterKey, Action> filterMap;
            if (ditavalFile != null) {
                filterReader.read(ditavalFile.getAbsoluteFile());
                filterMap = filterReader.getFilterMap();
            } else {
                filterMap = Collections.EMPTY_MAP;
            }
            baseFilterUtils = new FilterUtils(printTranstype.contains(transtype), filterMap);
            baseFilterUtils.setLogger(logger);
        }

        initXmlReader();

        initFilters();
    }
    /**
     * Init xml reader used for pipeline parsing.
     */
     private void initXmlReader() throws SAXException {
        CatalogUtils.setDitaDir(ditaDir);
        reader = XMLUtils.getXMLReader();
        if (validate) {
            reader.setFeature(FEATURE_VALIDATION, true);
            try {
                reader.setFeature(FEATURE_VALIDATION_SCHEMA, true);
            } catch (final SAXNotRecognizedException e) {
                // Not Xerces, ignore exception
            }
        }
        reader.setFeature(FEATURE_NAMESPACE, true);
        final CatalogResolver resolver = CatalogUtils.getCatalogResolver();
        reader.setEntityResolver(resolver);
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
    }

    /**
     * Initialize reusable filters.
     */
    private void initFilters() {
        forceUniqueFilter = new ForceUniqueFilter();
        forceUniqueFilter.setLogger(logger);
        forceUniqueFilter.setJob(job);
        forceUniqueFilter.setEntityResolver(reader.getEntityResolver());

        ditaWriterFilter = new DitaWriterFilter();
        ditaWriterFilter.setLogger(logger);
        ditaWriterFilter.setJob(job);
        ditaWriterFilter.setEntityResolver(reader.getEntityResolver());

        topicFragmentFilter = new TopicFragmentFilter(ATTRIBUTE_NAME_CONREF, ATTRIBUTE_NAME_CONREFEND);
    }

    /**
     * Get pipe line filters
     *
     * @param fileToParse absolute URI to current file being processed
     */
    private List<XMLFilter> getProcessingPipe(final URI fileToParse) {
        final List<XMLFilter> pipe = new ArrayList<>();

        if (genDebugInfo) {
            final DebugFilter debugFilter = new DebugFilter();
            debugFilter.setLogger(logger);
            debugFilter.setInputFile(currentFile);
            pipe.add(debugFilter);
        }

        if (filterUtils != null) {
            final ProfilingFilter profilingFilter = new ProfilingFilter();
            profilingFilter.setLogger(logger);
            profilingFilter.setFilterUtils(filterUtils);
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

        if (forceUnique) {
            forceUniqueFilter.setCurrentFile(currentFile);
            pipe.add(forceUniqueFilter);
        }

        pipe.add(topicFragmentFilter);

        ditaWriterFilter.setDefaultValueMap(defaultValueMap);
        ditaWriterFilter.setCurrentFile(currentFile);
        ditaWriterFilter.setOutputFile(outputFile);
        pipe.add(ditaWriterFilter);

        return pipe;
    }

    private void readArguments(AbstractPipelineInput input) {
        final File baseDir = toFile(input.getAttribute(ANT_INVOKER_PARAM_BASEDIR));
        ditaDir = new File(input.getAttribute(ANT_INVOKER_EXT_PARAM_DITADIR));
        transtype = input.getAttribute(ANT_INVOKER_EXT_PARAM_TRANSTYPE);
        profilingEnabled = true;
        if (input.getAttribute(ANT_INVOKER_PARAM_PROFILING_ENABLED) != null) {
            profilingEnabled = Boolean.parseBoolean(input.getAttribute(ANT_INVOKER_PARAM_PROFILING_ENABLED));
        }
        if (profilingEnabled) {
            if (input.getAttribute(ANT_INVOKER_PARAM_DITAVAL) != null) {
                ditavalFile = new File(input.getAttribute(ANT_INVOKER_PARAM_DITAVAL));
                if (!ditavalFile.isAbsolute()) {
                    ditavalFile = new File(baseDir, ditavalFile.getPath()).getAbsoluteFile();
                }
            }
        }
        gramcache = "yes".equalsIgnoreCase(input.getAttribute(ANT_INVOKER_EXT_PARAM_GRAMCACHE));
        validate = Boolean.valueOf(input.getAttribute(ANT_INVOKER_EXT_PARAM_VALIDATE));
        setSystemId = "yes".equals(input.getAttribute(ANT_INVOKER_EXT_PARAN_SETSYSTEMID));
        forceUnique = Boolean.valueOf(input.getAttribute(ANT_INVOKER_EXT_PARAN_FORCE_UNIQUE));
        genDebugInfo = Boolean.valueOf(input.getAttribute(ANT_INVOKER_EXT_PARAM_GENERATE_DEBUG_ATTR));
        final String mode = input.getAttribute(ANT_INVOKER_EXT_PARAM_PROCESSING_MODE);
        processingMode = mode != null ? Mode.valueOf(mode.toUpperCase()) : Mode.LAX;

        // Absolute input directory path
        URI inputDir = job.getInputDir();
        if (!inputDir.isAbsolute()) {
            inputDir = baseDir.toURI().resolve(inputDir);
        }
        inputMap = inputDir.resolve(job.getInputMap());

        for (final Map.Entry<URI, URI> e: job.getCopytoMap().entrySet()) {
            copytoMap.put(toFile(e.getKey()), toFile(e.getValue()));
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

            final DocumentBuilder builder = XMLUtils.getDocumentBuilder();
            builder.setEntityResolver(CatalogUtils.getCatalogResolver());

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
                    final URI src = job.getInputDir().resolve(parent);
                    parentRoot = builder.parse(src.toString());
                } else {
                    parentRoot = builder.parse(tmprel);
                }
                if (children != null) {
                    for (final URI childpath: children) {
                        final Document childRoot = builder.parse(inputMap.resolve(childpath.getPath()).toString());
                        mergeScheme(parentRoot, childRoot);
                        generateScheme(new File(job.tempDir, childpath.getPath() + SUBJECT_SCHEME_EXTENSION), childRoot);
                    }
                }

                //Output parent scheme
                generateScheme(new File(job.tempDir.getAbsoluteFile(), parent.getPath() + SUBJECT_SCHEME_EXTENSION), parentRoot);
            }
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
        final File p = filename.getParentFile();
        if (!p.exists() && !p.mkdirs()) {
            throw new DITAOTException("Failed to make directory " + p.getAbsolutePath());
        }
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(filename);
            final StreamResult res = new StreamResult(out);
            final DOMSource ds = new DOMSource(root);
            final TransformerFactory tff = TransformerFactory.newInstance();
            final Transformer tf = tff.newTransformer();
            tf.transform(ds, res);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e) ;
            throw new DITAOTException(e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    throw new DITAOTException(e);
                }
            }
        }
    }


    /**
     * Execute copy-to task, generate copy-to targets base on sources
     */
    private void performCopytoTask() {
        final Map<File, File> copyTo = new HashMap<>(copytoMap);
        if (forceUniqueFilter != null) {
            for (final Map.Entry<URI, URI> e: forceUniqueFilter.copyToMap.entrySet()) {
                copyTo.put(toFile(e.getKey()), toFile(e.getValue()));
            }
        }
        
        for (final Map.Entry<File, File> entry: copyTo.entrySet()) {
            final File copytoTarget = entry.getKey();
            final File copytoSource = entry.getValue();
            final File srcFile = new File(job.tempDir, copytoSource.getPath());
            final File targetFile = new File(job.tempDir, copytoTarget.getPath());

            if (targetFile.exists()) {
                logger.warn(MessageUtils.getInstance().getMessage("DOTX064W", copytoTarget.getPath()).toString());
            } else {
                final File inputMapInTemp = new File(job.tempDir, job.getInputMap().getPath()).getAbsoluteFile();
                copyFileWithPIReplaced(srcFile, targetFile, copytoTarget, inputMapInTemp);
                // add new file info into job
                final FileInfo src = job.getFileInfo(toURI(copytoSource));
                final FileInfo.Builder b = src != null ? new FileInfo.Builder(src) : new FileInfo.Builder();
                final FileInfo dst = b.uri(toURI(copytoTarget)).isCopyToSource(false).build();
                job.add(dst);
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
    private void copyFileWithPIReplaced(final File src, final File target, final File copytoTargetFilename, final File inputMapInTemp) {
        if (!target.getParentFile().exists() && !target.getParentFile().mkdirs()) {
            logger.error("Failed to create copy-to target directory " + target.getParentFile().getAbsolutePath());
            return;
        }
        final File path2project = DebugAndFilterModule.getPathtoProject(copytoTargetFilename, target, inputMapInTemp, job);
        final File workdir = target.getParentFile();
        XMLFilter filter = new CopyToFilter(workdir, path2project);
        
        logger.info("Processing " + src.getAbsolutePath() + " to " + target.getAbsolutePath());
        try {
            XMLUtils.transform(src, target, Collections.singletonList(filter));
        } catch (final DITAOTException e) {
            logger.error("Failed to write copy-to file: " + e.getMessage(), e);
        }
    }
    
    /**
     * XML filter to rewrite processing instructions to reflect copy-to location. The following processing-instructions are
     * processed: 
     * 
     * <ul>
     * <li>{@link Constants#PI_WORKDIR_TARGET PI_WORKDIR_TARGET}</li>
     * <li>{@link Constants#PI_WORKDIR_TARGET_URI PI_WORKDIR_TARGET_URI}</li>
     * <li>{@link Constants#PI_PATH2PROJ_TARGET PI_PATH2PROJ_TARGET}</li>
     * <li>{@link Constants#PI_PATH2PROJ_TARGET_URI PI_PATH2PROJ_TARGET_URI}</li>
     * </ul>
     */
    private static final class CopyToFilter extends XMLFilterImpl {
        
        private final File workdir;
        private final File path2project;
        
        CopyToFilter(final File workdir, final File path2project) {
            super();
            this.workdir = workdir;
            this.path2project = path2project;
        }
                
        @Override
        public void processingInstruction(final String target, final String data) throws SAXException {
            String d = data;
            switch (target) {
                case PI_WORKDIR_TARGET:
                    if (workdir != null) {
                        try {
                            if (!OS_NAME.toLowerCase().contains(OS_NAME_WINDOWS)) {
                                d = workdir.getCanonicalPath();
                            } else {
                                d = UNIX_SEPARATOR + workdir.getCanonicalPath();
                            }
                        } catch (final IOException e) {
                            throw new RuntimeException("Failed to get canonical path for working directory: " + e.getMessage(), e);
                        }
                    }
                    break;
                case PI_WORKDIR_TARGET_URI:
                    if (workdir != null) {
                        d = workdir.toURI().toString();
                    }
                    break;
                case PI_PATH2PROJ_TARGET:
                    if (path2project != null) {
                        d = path2project.getPath();
                    }
                    break;
                case PI_PATH2PROJ_TARGET_URI:
                    if (path2project != null) {
                        d = toURI(path2project).toString();
                        if (!d.endsWith(URI_SEPARATOR)) {
                            d = d + URI_SEPARATOR;
                        }
                    }
                    break;
            }
            getContentHandler().processingInstruction(target, d);
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
                return new File(getRelativeUnixPath(traceFilename.getAbsolutePath(), inputMap.getAbsolutePath())).getParentFile();
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
        final URI relativePath = getRelativePath(job.getInputFile(), overflowingFile.toURI());
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
    private static boolean isOutFile(final File filePathName, final File inputMap){
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
            parser.getContentHandler().processingInstruction("doctype-public", publicId);
            parser.getContentHandler().processingInstruction("doctype-system", systemId);
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
