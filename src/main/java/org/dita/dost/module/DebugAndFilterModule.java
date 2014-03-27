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
import static org.dita.dost.util.Configuration.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
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
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.dita.dost.exception.DITAOTException;
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
import org.dita.dost.util.KeyDef;
import org.dita.dost.util.StringUtils;
import org.dita.dost.util.URLUtils;
import org.dita.dost.util.XMLUtils;
import org.dita.dost.writer.DitaWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.helpers.XMLFilterImpl;


/**
 * DebugAndFilterModule implement the second step in preprocess. It will insert debug
 * information into every dita files and filter out the information that is not
 * necessary.
 * 
 * @author Zhang, Yuan Peng
 */
final class DebugAndFilterModule extends AbstractPipelineModuleImpl {
    
    /** Subject scheme file extension */
    private static final String SUBJECT_SCHEME_EXTENSION = ".subm";
    
    /** Absolute input map path. */
    private File inputMap = null;

    @Override
    public AbstractPipelineOutput execute(final AbstractPipelineInput input) throws DITAOTException {
        if (logger == null) {
            throw new IllegalStateException("Logger not set");
        }
        try {
            final String baseDir = input.getAttribute(ANT_INVOKER_PARAM_BASEDIR);
            /* Absolute DITA-OT base path. */
            File ditaDir = new File(input.getAttribute(ANT_INVOKER_EXT_PARAM_DITADIR));
            final String transtype = input.getAttribute(ANT_INVOKER_EXT_PARAM_TRANSTYPE);
            File ditavalFile = null;
            if (input.getAttribute(ANT_INVOKER_PARAM_DITAVAL) != null ) {
                ditavalFile = new File(input.getAttribute(ANT_INVOKER_PARAM_DITAVAL));
                if (!ditavalFile.isAbsolute()) {
                    ditavalFile = new File(baseDir, ditavalFile.getPath()).getAbsoluteFile();
                }

            }
            
            /* Absolute input directory path. */
            File inputDir = new File(job.getInputDir());
            if (!inputDir.isAbsolute()) {
                inputDir = new File(baseDir, inputDir.getPath()).getAbsoluteFile();
            }
            inputMap = new File(inputDir, job.getInputMap()).getAbsoluteFile();

            // Output subject schemas
            outputSubjectScheme();
            final DitaValReader filterReader = new DitaValReader();
            filterReader.setLogger(logger);
            filterReader.initXMLReader("yes".equals(input.getAttribute(ANT_INVOKER_EXT_PARAN_SETSYSTEMID)));

            FilterUtils filterUtils = new FilterUtils(printTranstype.contains(transtype));
            filterUtils.setLogger(logger);
            if (ditavalFile != null){
                filterReader.read(ditavalFile.getAbsoluteFile());
                filterUtils.setFilterMap(filterReader.getFilterMap());
            }
            final SubjectSchemeReader subjectSchemeReader = new SubjectSchemeReader();
            subjectSchemeReader.setLogger(logger);
            
            final DitaWriter fileWriter = new DitaWriter();
            fileWriter.setLogger(logger);
            try{
                final boolean xmlValidate = Boolean.valueOf(input.getAttribute("validate"));
                boolean setSystemid = true;
                fileWriter.initXMLReader(ditaDir.getAbsoluteFile(),xmlValidate, setSystemid);
            } catch (final SAXException e) {
                throw new DITAOTException(e.getMessage(), e);
            }
            fileWriter.setTempDir(job.tempDir);
            if (filterUtils != null) {
            	fileWriter.setFilterUtils(filterUtils);
            }
            if (transtype.equals(INDEX_TYPE_ECLIPSEHELP)) {
                fileWriter.setDelayConrefUtils(new DelayConrefUtils());
            }
            fileWriter.setKeyDefinitions(KeyDef.readKeydef(new File(job.tempDir, KEYDEF_LIST_FILE)));
           
            job.setGeneratecopyouter(input.getAttribute(ANT_INVOKER_EXT_PARAM_GENERATECOPYOUTTER));
            job.setOutterControl(input.getAttribute(ANT_INVOKER_EXT_PARAM_OUTTERCONTROL));
            job.setOnlyTopicInMap(input.getAttribute(ANT_INVOKER_EXT_PARAM_ONLYTOPICINMAP));
            job.setInputFile(inputMap);
            job.setOutputDir(new File(input.getAttribute(ANT_INVOKER_EXT_PARAM_OUTPUTDIR)));
            fileWriter.setJob(job);

            final Map<String, Set<String>> dic = readMapFromXML(FILE_NAME_SUBJECT_DICTIONARY);

            for (final FileInfo f: job.getFileInfo()) {
                if (ATTR_FORMAT_VALUE_DITA.equals(f.format) || ATTR_FORMAT_VALUE_DITAMAP.equals(f.format)
                        || f.isConrefTarget || f.isCopyToSource) {
                    final File filename = f.file;
                    final File currentFile = new File(inputDir, filename.getPath());
                    if (!currentFile.exists()) {
                        // Assuming this is an copy-to target file, ignore it
                        logger.debug("Ignoring a copy-to file " + filename);
                        continue;
                    }
                    logger.info("Processing " + currentFile.getAbsolutePath());
    
                    final Set<String> schemaSet = dic.get(filename.getPath());
                    filterReader.reset();
                    if (schemaSet != null) {
                        subjectSchemeReader.reset();
                        final FilterUtils fu = new FilterUtils(printTranstype.contains(transtype));
                        fu.setLogger(logger);
                        for (final String schema: schemaSet) {
                            subjectSchemeReader.loadSubjectScheme(FileUtils.resolve(job.tempDir.getAbsolutePath(), schema) + SUBJECT_SCHEME_EXTENSION);
                        }
                        if (ditavalFile != null){
                            filterReader.filterReset();
                            filterReader.setSubjectScheme(subjectSchemeReader.getSubjectSchemeMap());
                            filterReader.read(ditavalFile.getAbsoluteFile());
                            final Map<FilterKey, Action> fm = new HashMap<FilterKey, Action>();
                            fm.putAll(filterReader.getFilterMap());
                            fm.putAll(filterUtils.getFilterMap());
                            fu.setFilterMap(Collections.unmodifiableMap(fm));
                        } else {
                            fu.setFilterMap(Collections.EMPTY_MAP);
                        }
                        fileWriter.setFilterUtils(fu);
    
                        fileWriter.setValidateMap(subjectSchemeReader.getValidValuesMap());
                        fileWriter.setDefaultValueMap(subjectSchemeReader.getDefaultValueMap());
                    } else {
                        fileWriter.setFilterUtils(filterUtils);
                    }
    
                    fileWriter.write(inputDir, filename);
                }
            }

            // reload the property for processing of copy-to
            performCopytoTask();
        } catch (final Exception e) {
            e.printStackTrace();
            throw new DITAOTException("Exception doing debug and filter module processing: " + e.getMessage(), e);
        }

        return null;
    }

    /**
     * Read a map from XML properties file. Values are split by {@link org.dita.dost.util.Constants#COMMA COMMA} into a set.
     * 
     * @param filename XML properties file path, relative to temporary directory
     */
    private Map<String, Set<String>> readMapFromXML(final String filename) {
        final File inputFile = new File(job.tempDir, filename);
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
            logger.error(e.getMessage(), e) ;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (final IOException e) {
                    logger.error(e.getMessage(), e) ;
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
                String tmprel = FileUtils.getRelativeUnixPath(inputMap.getAbsolutePath(), parent);
                tmprel = FileUtils.resolve(job.tempDir.getAbsolutePath(), tmprel) + SUBJECT_SCHEME_EXTENSION;
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
                        String rel = FileUtils.getRelativeUnixPath(inputMap.getAbsolutePath(), childpath);
                        rel = FileUtils.resolve(job.tempDir.getAbsolutePath(), rel) + SUBJECT_SCHEME_EXTENSION;
                        generateScheme(rel, childRoot);
                    }
                }

                //Output parent scheme
                String rel = FileUtils.getRelativeUnixPath(inputMap.getAbsolutePath(), parent);
                rel = FileUtils.resolve(job.tempDir.getAbsolutePath(), rel) + SUBJECT_SCHEME_EXTENSION;
                generateScheme(rel, parentRoot);
            }
        } catch (final Exception e) {
            logger.error(e.getMessage(), e) ;
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
        FileOutputStream out = null;
        try {
            final File f = new File(filename);
            final File p = f.getParentFile();
            if (!p.exists() && !p.mkdirs()) {
                throw new IOException("Failed to make directory " + p.getAbsolutePath());
            }
            out = new FileOutputStream(new File(filename));
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
        final Map<File, File> copytoMap = job.getCopytoMap();
        
        for (final Map.Entry<File, File> entry: copytoMap.entrySet()) {
            final File copytoTarget = entry.getKey();
            final File copytoSource = entry.getValue();
            final File srcFile = new File(job.tempDir, copytoSource.getPath());
            final File targetFile = new File(job.tempDir, copytoTarget.getPath());

            if (targetFile.exists()) {
                /*logger
                        .logWarn(new StringBuffer("Copy-to task [copy-to=\"")
                                .append(copytoTarget)
                                .append("\"] which points to an existed file was ignored.").toString());*/
                logger.warn(MessageUtils.getInstance().getMessage("DOTX064W", copytoTarget.getPath()).toString());
            }else{
                final File inputMapInTemp = new File(job.tempDir, job.getInputMap()).getAbsoluteFile();
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
    public void copyFileWithPIReplaced(final File src, final File target, final File copytoTargetFilename, final File inputMapInTemp) {
        if (!target.getParentFile().exists() && !target.getParentFile().mkdirs()) {
            logger.error("Failed to create copy-to target directory " + target.getParentFile().getAbsolutePath());
            return;
        }
        final DitaWriter dw = new DitaWriter();
        dw.setJob(job);
        final String path2project = dw.getPathtoProject(copytoTargetFilename, target, inputMapInTemp);
        final File workdir = target.getParentFile();
        XMLFilter filter = new CopyToFilter(workdir, path2project);
        
        logger.info("Processing " + target.getAbsolutePath());
        try {
            XMLUtils.transform(src, target, Arrays.asList(filter));
        } catch (final DITAOTException e) {
            logger.error("Failed to write copy-to file: " + e.getMessage(), e);
        }
    }
    
    /**
     * XML filter to rewrite processing instructions to reflect copy-to location. The following processing-instructions are
     * processed: 
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
        
        CopyToFilter(final File workdir, final String path2project) {
            super();
            this.workdir = workdir;
            this.path2project = path2project;
        }
                
        @Override
        public void processingInstruction(final String target, final String data) throws SAXException {
            String d = data;
            if(target.equals(PI_WORKDIR_TARGET)) {
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
                    d = URLUtils.correct(FileUtils.separatorsToUnix(path2project), true);
                }
            }            
            getContentHandler().processingInstruction(target, d);
        }
        
    }

}
