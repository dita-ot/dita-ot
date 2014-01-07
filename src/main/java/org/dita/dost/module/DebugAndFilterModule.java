/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.module;

import static org.dita.dost.util.Constants.*;
import static org.dita.dost.writer.DitaWriter.*;
import static org.dita.dost.util.Job.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.reader.DitaValReader;
import org.dita.dost.reader.SubjectSchemeReader;
import org.dita.dost.util.DelayConrefUtils;
import org.dita.dost.util.FilterUtils.Action;
import org.dita.dost.util.FilterUtils.FilterKey;
import org.dita.dost.util.CatalogUtils;
import org.dita.dost.util.FileUtils;
import org.dita.dost.util.FilterUtils;
import org.dita.dost.util.Job;
import org.dita.dost.util.KeyDef;
import org.dita.dost.util.OutputUtils;
import org.dita.dost.util.StringUtils;
import org.dita.dost.util.TimingUtils;
import org.dita.dost.util.URLUtils;
import org.dita.dost.writer.DitaWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;


/**
 * DebugAndFilterModule implement the second step in preprocess. It will insert debug
 * information into every dita files and filter out the information that is not
 * necessary.
 * 
 * @author Zhang, Yuan Peng
 */
final class DebugAndFilterModule implements AbstractPipelineModule {
    
    /** Subject scheme file extension */
    private static final String SUBJECT_SCHEME_EXTENSION = ".subm";
    
    /**
     * File extension of source file.
     */
    private String extName = null;
    private File tempDir = null;

    private final OutputUtils outputUtils = new OutputUtils();

    /**
     * Update property map.
     *
     * @param listName name of map to update
     * @param property property to update
     */
    private void updatePropertyMap(final String listName, final Job property){
        final Map<String, String> propValues = property.getMap(listName);
        if (propValues == null || propValues.isEmpty()){
            return;
        }
        final Map<String, String> result = new HashMap<String, String>();
        for (final Map.Entry<String, String> e: propValues.entrySet()) {
            String key = e.getKey();
            String value = e.getValue();
            if (!FILE_EXTENSION_DITAMAP.equals("." + FileUtils.getExtension(value))) {
                if (extName != null) {
                    key = FileUtils.replaceExtension(key, extName);
                    value = FileUtils.replaceExtension(value, extName);
                }
            }
            result.put(key, value);
        }
        property.setMap(listName, result);
    }
    
    /**
     * Update property set.
     *
     * @param listName name of set to update
     * @param property property to update
     */
    private void updatePropertySet(final String listName, final Job property){
        final Set<String> propValues = property.getSet(listName);
        if (propValues == null || propValues.isEmpty()){
            return;
        }
        final Set<String> result = new HashSet<String>(propValues.size());
        for (final String file: propValues) {
            String f = file;
            if (!FILE_EXTENSION_DITAMAP.equals("." + FileUtils.getExtension(file))) {
                if (extName != null) {
                    f = FileUtils.replaceExtension(f, extName);
                }
            }
            result.add(f);
        }
        property.setSet(listName, result);
    }
    
    /**
     * Update property value.
     * 
     * @param listName name of value to update
     * @param property property to update
     */
    private void updatePropertyString(final String listName, final Job property){
        String propValue = property.getProperty(listName);
        if (propValue == null || propValue.trim().length() == 0){
            return;
        }
        if (!FILE_EXTENSION_DITAMAP.equals("." + FileUtils.getExtension(propValue))) {
            if (extName != null) {
            	propValue = FileUtils.replaceExtension(propValue, extName);
            }
        }
        property.setProperty(listName, propValue);
    }
    
    private DITAOTLogger logger;

    /** Absolute input map path. */
    private File inputMap = null;
    /** Absolute DITA-OT base path. */
    private File ditaDir = null;
    /** Absolute input directory path. */
    private File inputDir = null;

    private final boolean setSystemid = true;

    /**
     * Default Construtor.
     */
    public DebugAndFilterModule(){
    }

    @Override
    public void setLogger(final DITAOTLogger logger) {
        this.logger = logger;
    }

    @Override
    public AbstractPipelineOutput execute(final AbstractPipelineInput input) throws DITAOTException {
        if (logger == null) {
            throw new IllegalStateException("Logger not set");
        }
        final Date executeStartTime = TimingUtils.getNowTime();
        logger.logInfo("DebugAndFilterModule.execute(): Starting...");

        try {
            final String baseDir = input.getAttribute(ANT_INVOKER_PARAM_BASEDIR);
            tempDir = new File(input.getAttribute(ANT_INVOKER_PARAM_TEMPDIR));
            if (!tempDir.isAbsolute()) {
                throw new IllegalArgumentException("Temporary directory " + tempDir + " must be absolute");
            }
            ditaDir=new File(input.getAttribute(ANT_INVOKER_EXT_PARAM_DITADIR));
            final String transtype = input.getAttribute(ANT_INVOKER_EXT_PARAM_TRANSTYPE);
            final String ext = input.getAttribute(ANT_INVOKER_PARAM_DITAEXT);
            if (ext != null) {
                extName = ext.startsWith(DOT) ? ext : (DOT + ext);
            }
            File ditavalFile = null;
            if (input.getAttribute(ANT_INVOKER_PARAM_DITAVAL) != null ) {
                ditavalFile = new File(input.getAttribute(ANT_INVOKER_PARAM_DITAVAL));
                if (!ditavalFile.isAbsolute()) {
                    ditavalFile = new File(baseDir, ditavalFile.getPath()).getAbsoluteFile();
                }

            }

            final Job job = new Job(tempDir);
            
            final Set<String> parseList = new HashSet<String>();
            parseList.addAll(job.getSet(FULL_DITAMAP_TOPIC_LIST));
            parseList.addAll(job.getSet(CONREF_TARGET_LIST));
            parseList.addAll(job.getSet(COPYTO_SOURCE_LIST));
            inputDir = new File(job.getInputDir());
            if (!inputDir.isAbsolute()) {
                inputDir = new File(baseDir, inputDir.getPath()).getAbsoluteFile();
            }
            inputMap = new File(inputDir, job.getInputMap()).getAbsoluteFile();

            // Output subject schemas
            outputSubjectScheme();
            final DitaValReader filterReader = new DitaValReader();
            filterReader.setLogger(logger);
            filterReader.initXMLReader("yes".equals(input.getAttribute(ANT_INVOKER_EXT_PARAN_SETSYSTEMID)));

            FilterUtils filterUtils = null;
            if (ditavalFile!=null){
            	filterUtils = new FilterUtils();
            	filterUtils.setLogger(logger);
                filterReader.read(ditavalFile.getAbsolutePath());
                filterUtils.setFilterMap(filterReader.getFilterMap());
            }
            final SubjectSchemeReader subjectSchemeReader = new SubjectSchemeReader();
            subjectSchemeReader.setLogger(logger);
            
            final DitaWriter fileWriter = new DitaWriter();
            fileWriter.setLogger(logger);
            try{
                final boolean xmlValidate = Boolean.valueOf(input.getAttribute("validate"));
                fileWriter.initXMLReader(ditaDir.getAbsoluteFile(),xmlValidate, setSystemid);
            } catch (final SAXException e) {
                throw new DITAOTException(e.getMessage(), e);
            }
            fileWriter.setTempDir(tempDir);
            fileWriter.setExtName(extName);
            fileWriter.setTranstype(transtype);
            if (filterUtils != null) {
            	fileWriter.setFilterUtils(filterUtils);
            }
            fileWriter.setDelayConrefUtils(new DelayConrefUtils());
            fileWriter.setKeyDefinitions(KeyDef.readKeydef(new File(tempDir, KEYDEF_LIST_FILE)));
           
            outputUtils.setGeneratecopyouter(input.getAttribute(ANT_INVOKER_EXT_PARAM_GENERATECOPYOUTTER));
            outputUtils.setOutterControl(input.getAttribute(ANT_INVOKER_EXT_PARAM_OUTTERCONTROL));
            outputUtils.setOnlyTopicInMap(input.getAttribute(ANT_INVOKER_EXT_PARAM_ONLYTOPICINMAP));
            outputUtils.setInputMapPathName(inputMap);
            outputUtils.setOutputDir(new File(input.getAttribute(ANT_INVOKER_EXT_PARAM_OUTPUTDIR)));
            fileWriter.setOutputUtils(outputUtils);

            final Map<String, Set<String>> dic = readMapFromXML(FILE_NAME_SUBJECT_DICTIONARY);

            for (final String filename: parseList) {
                final File currentFile = new File(inputDir, filename);
                logger.logInfo("Processing " + currentFile.getAbsolutePath());

                final Set<String> schemaSet = dic.get(filename);
                filterReader.reset();
                if (schemaSet != null) {
                    subjectSchemeReader.reset();
                    final FilterUtils fu = new FilterUtils();
                    fu.setLogger(logger);
                    for (final String schema: schemaSet) {
                        subjectSchemeReader.loadSubjectScheme(FileUtils.resolveFile(tempDir.getAbsolutePath(), schema) + SUBJECT_SCHEME_EXTENSION);
                    }
                    if (ditavalFile!=null){
                        filterReader.filterReset();
                        filterReader.setSubjectScheme(subjectSchemeReader.getSubjectSchemeMap());
                        filterReader.read(ditavalFile.getAbsolutePath());
                        final Map<FilterKey, Action> fm = new HashMap<FilterKey, Action>();
                        fm.putAll(filterReader.getFilterMap());
                        fm.putAll(filterUtils.getFilterMap());
                        fu.setFilterMap(Collections.unmodifiableMap(fm));
                    } else {
                        fu.setFilterMap(null);
                    }
                    fileWriter.setFilterUtils(fu);

                    fileWriter.setValidateMap(subjectSchemeReader.getValidValuesMap());
                    fileWriter.setDefaultValueMap(subjectSchemeReader.getDefaultValueMap());
                } else {
                    fileWriter.setFilterUtils(filterUtils);
                }

                if (!new File(inputDir, filename).exists()) {
                    // This is an copy-to target file, ignore it
                    logger.logInfo("Ignoring a copy-to file " + filename);
                    continue;
                }

                fileWriter.write(inputDir, filename);
            }

            if (extName != null) {
                updateList(tempDir);
            }
            //update dictionary.
            updateDictionary(tempDir);

            // reload the property for processing of copy-to
            performCopytoTask(tempDir, new Job(tempDir));
        } catch (final Exception e) {
            e.printStackTrace();
            throw new DITAOTException("Exception doing debug and filter module processing: " + e.getMessage(), e);
        } finally {
            logger.logInfo("Execution time: " + TimingUtils.reportElapsedTime(executeStartTime));
        }

        return null;
    }

    /**
     * Read a map from XML properties file. Values are split by {@link org.dita.dost.util.Constants#COMMA COMMA} into a set.
     * 
     * @param filename XML properties file path, relative to temporary directory
     */
    private Map<String, Set<String>> readMapFromXML(final String filename) {
        final File inputFile = new File(tempDir, filename);
        final Map<String, Set<String>> graph = new HashMap<String, Set<String>>();
        if (!inputFile.exists()) {
            return graph;
        }
        final Properties prop = new Properties();
        FileInputStream in = null;
        try {
            in = new FileInputStream(inputFile);
            prop.loadFromXML(in);
            in.close();
        } catch (final IOException e) {
            logger.logError(e.getMessage(), e) ;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (final IOException e) {
                    logger.logError(e.getMessage(), e) ;
                }
            }
        }

        for (final Map.Entry<Object, Object> entry: prop.entrySet()) {
            final String key = (String) entry.getKey();
            final String value = (String) entry.getValue();
            graph.put(key, StringUtils.restoreSet(value, COMMA));
        }

        return Collections.unmodifiableMap(graph);
    }

    /**
     * Output subject schema file.
     * 
     * @throws DITAOTException if generation files
     */
    private void outputSubjectScheme() throws DITAOTException {

        final Map<String, Set<String>> graph = readMapFromXML(FILE_NAME_SUBJECT_RELATION);

        final Queue<String> queue = new LinkedList<String>();
        final Set<String> visitedSet = new HashSet<String>();
        
        for (final Map.Entry<String, Set<String>> entry: graph.entrySet()) {
            queue.offer(entry.getKey());
        }

        try {
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setEntityResolver(CatalogUtils.getCatalogResolver());

            while (!queue.isEmpty()) {
                final String parent = queue.poll();
                final Set<String> children = graph.get(parent);

                if (children != null) {
                    queue.addAll(children);
                }
                if ("ROOT".equals(parent) || visitedSet.contains(parent)) {
                    continue;
                }
                visitedSet.add(parent);
                String tmprel = FileUtils.getRelativePath(inputMap.getAbsolutePath(), parent);
                tmprel = FileUtils.resolveFile(tempDir.getAbsolutePath(), tmprel) + SUBJECT_SCHEME_EXTENSION;
                Document parentRoot = null;
                if (!FileUtils.fileExists(tmprel)) {
                    parentRoot = builder.parse(new InputSource(new FileInputStream(parent)));
                } else {
                    parentRoot = builder.parse(new InputSource(new FileInputStream(tmprel)));
                }
                if (children != null) {
                    for (final String childpath: children) {
                        final Document childRoot = builder.parse(new InputSource(new FileInputStream(childpath)));
                        mergeScheme(parentRoot, childRoot);
                        String rel = FileUtils.getRelativePath(inputMap.getAbsolutePath(), childpath);
                        rel = FileUtils.resolveFile(tempDir.getAbsolutePath(), rel) + SUBJECT_SCHEME_EXTENSION;
                        generateScheme(rel, childRoot);
                    }
                }

                //Output parent scheme
                String rel = FileUtils.getRelativePath(inputMap.getAbsolutePath(), parent);
                rel = FileUtils.resolveFile(tempDir.getAbsolutePath(), rel) + SUBJECT_SCHEME_EXTENSION;
                generateScheme(rel, parentRoot);
            }
        } catch (final Exception e) {
            logger.logError(e.getMessage(), e) ;
            throw new DITAOTException(e);
        }

    }

    private void mergeScheme(final Document parentRoot, final Document childRoot) {
        final Queue<Element> pQueue = new LinkedList<Element>();
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
        final Queue<Element> queue = new LinkedList<Element>();
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
    private void generateScheme(final String filename, final Document root) throws DITAOTException {
        try {
            final File f = new File(filename);
            final File p = f.getParentFile();
            if (!p.exists() && !p.mkdirs()) {
                throw new IOException("Failed to make directory " + p.getAbsolutePath());
            }
            final FileOutputStream file = new FileOutputStream(new File(filename));
            final StreamResult res = new StreamResult(file);
            final DOMSource ds = new DOMSource(root);
            final TransformerFactory tff = TransformerFactory.newInstance();
            final Transformer tf = tff.newTransformer();
            tf.transform(ds, res);
            if (res.getOutputStream() != null) {
                res.getOutputStream().close();
            }
            if (file != null) {
                file.close();
            }
        } catch (final Exception e) {
            logger.logError(e.getMessage(), e) ;
            throw new DITAOTException(e);
        }
    }


    /**
     * Execute copy-to task, generate copy-to targets base on sources
     */
    private void performCopytoTask(final File tempDir, final Job job) {
        final Map<String, String> copytoMap  = job.getCopytoMap();
        
        for (final Map.Entry<String, String> entry: copytoMap.entrySet()) {
            final String copytoTarget = entry.getKey();
            final String copytoSource = entry.getValue();
            final File srcFile = new File(tempDir, copytoSource);
            final File targetFile = new File(tempDir, copytoTarget);

            if (targetFile.exists()) {
                /*logger
                        .logWarn(new StringBuffer("Copy-to task [copy-to=\"")
                                .append(copytoTarget)
                                .append("\"] which points to an existed file was ignored.").toString());*/
                logger.logWarn(MessageUtils.getInstance().getMessage("DOTX064W", copytoTarget).toString());
            }else{
                final String inputMapInTemp = new File(tempDir + File.separator + job.getInputMap()).getAbsolutePath();
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
    public void copyFileWithPIReplaced(final File src, final File target, final String copytoTargetFilename, final String inputMapInTemp ) {
        if (!target.getParentFile().exists() && !target.getParentFile().mkdirs()) {
            logger.logError("Failed to create copy-to target directory " + target.getParentFile().getAbsolutePath());
        }
        final DitaWriter dw = new DitaWriter();
        dw.setOutputUtils(outputUtils);
        final String path2project = dw.getPathtoProject(copytoTargetFilename, target, inputMapInTemp);
        final File workdir = target.getParentFile();
        try {
            final Transformer serializer = TransformerFactory.newInstance().newTransformer();
            final XMLFilter filter = new CopyToFilter(StringUtils.getXMLReader(), workdir, path2project);
            serializer.transform(new SAXSource(filter, new InputSource(src.toURI().toString())),
                                 new StreamResult(target));
        } catch (final TransformerConfigurationException e) {
            logger.logError("Failed to configure serializer: " + e.getMessage(), e);
        } catch (final TransformerFactoryConfigurationError e) {
            logger.logError("Failed to configure serializer: " + e.getMessage(), e);
        } catch (final SAXException e) {
            logger.logError("Failed to create XML parser: " + e.getMessage(), e);
        } catch (final TransformerException e) {
            logger.logError("Failed to rewrite copy-to file: " + e.getMessage(), e);
        }
    }
    
    /**
     * XML filter to rewrite processing instructions to reflect copy-to location. The following processing-instructions are 
     * 
     * <ul>
     * <li>{@link DitaWriter#PI_WORKDIR_TARGET PI_WORKDIR_TARGET}</li>
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
            if(target.equals(PI_WORKDIR_TARGET)) {
                if (workdir != null) {
                    try {
                        if (OS_NAME.toLowerCase().indexOf(OS_NAME_WINDOWS) == -1) {
                            d = workdir.getCanonicalPath();
                        } else {
                            d = UNIX_SEPARATOR + workdir.getCanonicalPath();
                        }
                    } catch (final IOException e) {
                        throw new RuntimeException("Failed to get canonical path for working directory: " + e.getMessage(), e);
                    }
                }
            } else if(target.equals(PI_WORKDIR_TARGET_URI)) {
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

    /**
     * Read job configuration, update properties, and serialise.
     * 
     * @param tempDir temporary directory path
     * @throws IOException 
     */
    private void updateList(final File tempDir) throws IOException{
        final Job job = new Job(tempDir);
        updatePropertyString(INPUT_DITAMAP, job);
        updatePropertySet(HREF_TARGET_LIST, job);
        updatePropertySet(CONREF_LIST, job);
        updatePropertySet(HREF_DITA_TOPIC_LIST, job);
        updatePropertySet(FULL_DITA_TOPIC_LIST, job);
        updatePropertySet(FULL_DITAMAP_TOPIC_LIST, job);
        updatePropertySet(CONREF_TARGET_LIST, job);
        updatePropertySet(COPYTO_SOURCE_LIST, job);
        updatePropertyMap(COPYTO_TARGET_TO_SOURCE_MAP_LIST, job);
        updatePropertySet(OUT_DITA_FILES_LIST, job);
        updatePropertySet(CONREF_PUSH_LIST, job);
        updatePropertySet(KEYREF_LIST, job);
        updatePropertySet(CODEREF_LIST, job);
        updatePropertySet(CHUNK_TOPIC_LIST, job);
        updatePropertySet(HREF_TOPIC_LIST, job);
        updatePropertySet(RESOURCE_ONLY_LIST, job);
        job.write();
    }

    /**
     * Update dictionary
     * 
     * @param tempDir temporary directory
     */
    private void updateDictionary(final File tempDir){
        //orignal map
        final Map<String, Set<String>> dic = readMapFromXML(FILE_NAME_SUBJECT_DICTIONARY);
        //result map
        final Map<String, Set<String>> resultMap = new HashMap<String, Set<String>>();
        //Iterate the orignal map
        for (final Map.Entry<String, java.util.Set<String>> entry: dic.entrySet()) {
            //filename will be checked.
            String filename = entry.getKey();
            if (extName != null) {
                if(FileUtils.isDITATopicFile(filename)){
                    //Replace extension name.
                    filename = FileUtils.replaceExtension(filename, extName);
                }
            }
            //put the updated value into the result map
            resultMap.put(filename, entry.getValue());
        }

        //Write the updated map into the dictionary file
        this.writeMapToXML(resultMap, FILE_NAME_SUBJECT_DICTIONARY);
        //File inputFile = new File(tempDir, FILE_NAME_SUBJECT_DICTIONARY);

    }
    /**
     * Method for writing a map into xml file.
     */
    private void writeMapToXML(final Map<String, Set<String>> m, final String filename) {
        if (m == null) {
            return;
        }
        final Properties prop = new Properties();
        for (final Map.Entry<String, Set<String>> entry: m.entrySet()) {
            final String key = entry.getKey();
            final String value = StringUtils.assembleString(entry.getValue(), COMMA);
            prop.setProperty(key, value);
        }
        final File outputFile = new File(tempDir, filename);
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(outputFile, false);
            prop.storeToXML(os, null);
            os.close();
        } catch (final IOException e) {
            logger.logError(e.getMessage(), e) ;
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (final IOException e) {
                    logger.logError(e.getMessage(), e) ;
                }
            }
        }
    }

}
