/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2007 All Rights Reserved.
 */
package org.dita.dost.reader;

import static org.dita.dost.util.Constants.*;
import static org.dita.dost.writer.DitaWriter.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.module.Content;
import org.dita.dost.module.ContentImpl;
import org.dita.dost.util.FileUtils;
import org.dita.dost.util.StringUtils;
import org.dita.dost.writer.ChunkTopicParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

/**
 * ChunkMapReader class, read ditamap file for chunking.
 *
 */
public final class ChunkMapReader implements AbstractReader {

    public static final String FILE_NAME_STUB_DITAMAP = "stub.ditamap";
	public static final String FILE_EXTENSION_CHUNK = ".chunk";
	public static final String ATTR_XTRF_VALUE_GENERATED = "generated_by_chunk";
    
	public static final String CHUNK_BY_DOCUMENT = "by-document";
    public static final String CHUNK_BY_TOPIC = "by-topic";
    public static final String CHUNK_TO_CONTENT = "to-content";
    public static final String CHUNK_TO_NAVIGATION = "to-navigation";

    private DITAOTLogger logger;

    private boolean chunkByTopic = false;

    private String filePath = null;
    private LinkedHashMap<String, String> changeTable = null;

    private Hashtable<String, String> conflictTable = null;
    private final Random random;

    private Set<String> refFileSet = null;

    private String ditaext = null;

    private String transtype = null;

    private ProcessingInstruction workdir = null;
    private ProcessingInstruction workdirUrl = null;
    private ProcessingInstruction path2proj = null;

    private String processingRole = ATTR_PROCESSING_ROLE_VALUE_NORMAL;
    /**
     * Constructor.
     */
    public ChunkMapReader() {
        super();
        chunkByTopic=false;// By default, processor should chunk by document.
        changeTable = new LinkedHashMap<String, String>(INT_128);
        refFileSet = new HashSet<String>(INT_128);
        conflictTable = new Hashtable<String, String>(INT_128);
        random = new Random();
    }
    /**
     * read input file.
     * @param filename filename
     */
    @Override
    public void read(final String filename) {
        final File inputFile = new File(filename);
        filePath = inputFile.getParent();
        try {
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder builder = factory.newDocumentBuilder();
            final Document doc = builder.parse(inputFile);

            // workdir and path2proj processing instructions.
            final NodeList docNodes = doc.getChildNodes();
            for (int i = 0; i < docNodes.getLength(); i++) {
                final Node node = docNodes.item(i);
                if (node.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE) {
                    final ProcessingInstruction pi = (ProcessingInstruction) node;
                    if (pi.getNodeName() == PI_WORKDIR_TARGET) {
                        workdir = pi;
                    } if (pi.getNodeName() == PI_WORKDIR_TARGET_URI) {
                        workdirUrl = pi;
                    } else if (pi.getNodeName().equals(PI_PATH2PROJ_TARGET)) {
                        path2proj = pi;
                    }
                }
            }

            //get the document node
            final Element root = doc.getDocumentElement();
            //get the immediate child nodes
            final NodeList list = root.getChildNodes();
            final String rootChunkValue = root.getAttribute(ATTRIBUTE_NAME_CHUNK);
            if(rootChunkValue != null &&
                    rootChunkValue.contains(CHUNK_BY_TOPIC)){
                chunkByTopic = true;
            }else{
                chunkByTopic = false;
            }
            //chunk value = "to-content"
            //When @chunk="to-content" is specified on "map" element,
            //chunk module will change its @class attribute to "topicref"
            //and process it as if it were a normal topicref wich @chunk="to-content"
            if(rootChunkValue != null &&
                    rootChunkValue.indexOf(CHUNK_TO_CONTENT)!=-1){
                // if to-content is specified on map element

                // create the reference to the new file on root element.
                String newFilename = inputFile.getName().substring(
                        0, inputFile.getName().indexOf(FILE_EXTENSION_DITAMAP)) + ditaext;
                File newFile = new File(inputFile.getParentFile().getAbsolutePath(),newFilename);
                if (newFile.exists()) {
                    newFilename = generateFilename("Chunk", ditaext);
                    final String oldpath = newFile.getAbsolutePath();
                    newFile = new File(FileUtils.resolveFile(inputFile.getParentFile().getAbsolutePath(), newFilename));
                    // Mark up the possible name changing, in case that references might be updated.
                    conflictTable.put(newFile.getAbsolutePath(), FileUtils.normalize(oldpath));
                }
                //change the class attribute to "topicref"
                final String originClassValue = root.getAttribute(ATTRIBUTE_NAME_CLASS);
                root.setAttribute(ATTRIBUTE_NAME_CLASS, originClassValue + MAP_TOPICREF.matcher);
                root.setAttribute(ATTRIBUTE_NAME_HREF, newFilename);

                //create the new file
                OutputStreamWriter newFileWriter = null;
                try{
                    newFileWriter = new OutputStreamWriter(new FileOutputStream(newFile), UTF8);
                    newFileWriter.write(XML_HEAD);
                    newFileWriter.write(LESS_THAN);
                    newFileWriter.write(QUESTION);
                    newFileWriter.write(PI_WORKDIR_TARGET);
                    newFileWriter.write(STRING_BLANK);
                    newFileWriter.write(UNIX_SEPARATOR);
                    newFileWriter.write(newFile.getParentFile().getAbsolutePath());
                    newFileWriter.write(QUESTION);
                    newFileWriter.write(GREATER_THAN);
                    
                    newFileWriter.write(LESS_THAN);
                    newFileWriter.write(QUESTION);
                    newFileWriter.write(PI_WORKDIR_TARGET_URI);
                    newFileWriter.write(STRING_BLANK);
                    newFileWriter.write(UNIX_SEPARATOR);
                    newFileWriter.write(newFile.getParentFile().toURI().toString());
                    newFileWriter.write(QUESTION);
                    newFileWriter.write(GREATER_THAN);
                    
                    newFileWriter.write("<dita></dita>");
                    newFileWriter.flush();
                    newFileWriter.close();
                }catch (final Exception e) {
                    logger.logError(e.getMessage(), e) ;
                }finally{
                    try{
                        if(newFileWriter!=null){
                            newFileWriter.close();
                        }
                    }catch (final Exception e) {
                        logger.logError(e.getMessage(), e) ;
                    }
                }

                //process chunk
                processTopicref(root);

                //add newly created file to changeTable
                changeTable.put(newFile.getAbsolutePath(),newFile.getAbsolutePath());

                // restore original root element
                if(originClassValue != null){
                    root.setAttribute(ATTRIBUTE_NAME_CLASS, originClassValue);
                }
                //remove the href
                root.removeAttribute(ATTRIBUTE_NAME_HREF);

            }else{
                // if to-content is not specified on map element
                //process the map element's immediate child node(s)
                for (int i = 0; i < list.getLength(); i++){
                    final Node node = list.item(i);
                    if (node.getNodeType() == Node.ELEMENT_NODE){
                        final Element currentElem = (Element) node;
                        final Node classAttr = node.getAttributes().getNamedItem(ATTRIBUTE_NAME_CLASS);

                        String classValue = null;
                        if(classAttr != null){
                            classValue = classAttr.getNodeValue();
                        }

                        if(classValue != null && MAP_RELTABLE.matches(classValue)){
                            updateReltable(currentElem);
                        }
                        if(classValue != null && MAP_TOPICREF.matches(classValue)
                                && !MAPGROUP_D_TOPICGROUP.matches(classValue)){
                            processTopicref(currentElem);
                        }

                    }
                }
            }

            //write the edited ditamap file to a temp file
            outputMapFile(inputFile.getAbsolutePath()+FILE_EXTENSION_CHUNK,root);
            if(!inputFile.delete()){
                logger.logError(MessageUtils.getInstance().getMessage("DOTJ009E", inputFile.getPath(), inputFile.getAbsolutePath()+FILE_EXTENSION_CHUNK).toString());
            }
            if(!new File(inputFile.getAbsolutePath()+FILE_EXTENSION_CHUNK).renameTo(inputFile)){
                logger.logError(MessageUtils.getInstance().getMessage("DOTJ009E", inputFile.getPath(), inputFile.getAbsolutePath()+FILE_EXTENSION_CHUNK).toString());
            }

        }catch (final Exception e){
            logger.logError(e.getMessage(), e) ;
        }

    }
    
    /**
     * Generate file name
     * 
     * @param prefix file name prefix
     * @param extension file extension
     * @return generated file name
     */
	private String generateFilename(final String prefix, final String extension) {
		return prefix + random.nextInt(Integer.MAX_VALUE) + extension;
	}

    @Override
    public void setLogger(final DITAOTLogger logger) {
        this.logger = logger;
    }

    private void outputMapFile(final String file, final Element root) {

        OutputStreamWriter output = null;
        try{
            output = new OutputStreamWriter(
                    new FileOutputStream(file),
                    UTF8);
            // path2proj processing instructions were not being sent to output.
            // The follow few lines corrects that problem.
            output.write(XML_HEAD);
            if (workdir != null) {
                output(workdir, output);
            }
            if (workdirUrl != null) {
                output(workdirUrl, output);
            }
            if (path2proj != null)
            {
                output(path2proj, output);
            }

            output(root,output);
            output.flush();
            output.close();
        }catch (final Exception e) {
            logger.logError(e.getMessage(), e) ;
        }finally{
            try{
                if(output!=null){
                    output.close();
                }
            }catch (final Exception e) {
                logger.logError(e.getMessage(), e) ;
            }
        }
    }

    private void output(final ProcessingInstruction instruction,final Writer outputWriter) throws IOException{
        outputWriter.write(LESS_THAN);
        outputWriter.write(QUESTION);
        outputWriter.write(instruction.getTarget());
        outputWriter.write(STRING_BLANK);
        outputWriter.write(instruction.getData());
        outputWriter.write(QUESTION);
        outputWriter.write(GREATER_THAN);
    }


    private void output(final Text text, final Writer outputWriter) throws IOException{
        outputWriter.write(StringUtils.escapeXML(text.getData()));
    }


    private void output(final Element elem, final Writer outputWriter) throws IOException{
        outputWriter.write(LESS_THAN);
        outputWriter.write(elem.getNodeName());
        final NamedNodeMap attrMap = elem.getAttributes();
        for (int i = 0; i<attrMap.getLength(); i++){
            outputWriter.write(STRING_BLANK);
            outputWriter.write(attrMap.item(i).getNodeName());
            outputWriter.write(EQUAL);
            outputWriter.write(QUOTATION);
            outputWriter.write(StringUtils.escapeXML(attrMap.item(i).getNodeValue()));
            outputWriter.write(QUOTATION);
        }
        outputWriter.write(GREATER_THAN);
        final NodeList children = elem.getChildNodes();
        for (int j = 0; j<children.getLength(); j++){
            final Node child = children.item(j);
            switch (child.getNodeType()){
            case Node.TEXT_NODE:
                output((Text) child, outputWriter); break;
            case Node.PROCESSING_INSTRUCTION_NODE:
                output((ProcessingInstruction) child, outputWriter); break;
            case Node.ELEMENT_NODE:
                output((Element) child, outputWriter); break;
            }
        }
        outputWriter.write(LESS_THAN);
        outputWriter.write(SLASH);
        outputWriter.write(elem.getNodeName());
        outputWriter.write(GREATER_THAN);
    }
    //process chunk
    private void processTopicref(final Element node) {
        String hrefValue = null;
        String chunkValue = null;
        String copytoValue = null;
        String scopeValue = null;
        String classValue = null;
        String xtrfValue = null;
        String processValue = null;
        final String tempRole = processingRole;
        boolean prevChunkByTopic = false;

        final Node hrefAttr = node.getAttributeNode(ATTRIBUTE_NAME_HREF);
        final Node chunkAttr = node.getAttributeNode(ATTRIBUTE_NAME_CHUNK);
        final Node copytoAttr = node.getAttributeNode(ATTRIBUTE_NAME_COPY_TO);
        final Node scopeAttr = node.getAttributeNode(ATTRIBUTE_NAME_SCOPE);
        final Node classAttr = node.getAttributeNode(ATTRIBUTE_NAME_CLASS);
        final Node xtrfAttr = node.getAttributeNode(ATTRIBUTE_NAME_XTRF);
        final Node processAttr = node.getAttributeNode(ATTRIBUTE_NAME_PROCESSING_ROLE);

        if(hrefAttr != null){
            hrefValue = hrefAttr.getNodeValue();
        }
        if(chunkAttr != null){
            chunkValue = chunkAttr.getNodeValue();
        }
        if(copytoAttr != null){
            copytoValue = copytoAttr.getNodeValue();
        }
        if(scopeAttr != null) {
            scopeValue = scopeAttr.getNodeValue();
        }
        if(classAttr != null) {
            classValue = classAttr.getNodeValue();
        }
        if(xtrfAttr != null) {
            xtrfValue = xtrfAttr.getNodeValue();
        }
        if(processAttr != null) {
            processValue = processAttr.getNodeValue();
            processingRole = processValue;
        }
        //This file is chunked(by-topic)
        if (xtrfValue != null && xtrfValue.contains(ATTR_XTRF_VALUE_GENERATED)) {
            return;
        }

        //set chunkByTopic if there is "by-topic" or "by-document" in chunkValue
        if(chunkValue != null &&
                (chunkValue.contains(CHUNK_BY_TOPIC) ||
                        chunkValue.contains(CHUNK_BY_DOCUMENT))){
            //a temp value to store the flag
            prevChunkByTopic = chunkByTopic;
            //if there is "by-topic" then chunkByTopic should be set to true;
            chunkByTopic = chunkValue.contains(CHUNK_BY_TOPIC);
        }

        if(ATTR_SCOPE_VALUE_EXTERNAL.equalsIgnoreCase(scopeValue)
                || (hrefValue != null && !FileUtils.fileExists(FileUtils.resolveFile(filePath, hrefValue)))
                || (MAPGROUP_D_TOPICHEAD.matches(classValue) && chunkValue == null)||
                ////support topicref without href attribute
                (MAP_TOPICREF.matches(classValue) && chunkValue == null && hrefValue == null)
                ) {
            //|| (ATTR_PROCESSING_ROLE_VALUE_RESOURCE_ONLY.equalsIgnoreCase(processValue))) {
            //Skip external links or non-existing href files.
            //Skip topic head entries.
            //Skip @processing-role=resource-only entries.
            if(chunkValue != null &&
                    (chunkValue.contains(CHUNK_BY_TOPIC) ||
                            chunkValue.contains(CHUNK_BY_DOCUMENT))){
                chunkByTopic = prevChunkByTopic;
            }
            processChildTopicref(node);
            //chunk "to-content"
        } else if(chunkValue != null &&
                //edited on 20100818 for bug:3042978
                chunkValue.indexOf(CHUNK_TO_CONTENT) != -1 && (hrefAttr != null || copytoAttr != null || node.hasChildNodes())){
            //if this is the start point of the content chunk
            //TODO very important start point(to-content).
            processChunk(node,false, chunkByTopic);
        }else if(chunkValue != null &&
                chunkValue.indexOf(CHUNK_TO_NAVIGATION)!=-1 &&
                INDEX_TYPE_ECLIPSEHELP.equals(transtype)){
            //if this is the start point of the navigation chunk
            if(chunkValue != null &&
                    (chunkValue.contains(CHUNK_BY_TOPIC) ||
                            chunkValue.contains(CHUNK_BY_DOCUMENT))){
                //restore the chunkByTopic value
                chunkByTopic = prevChunkByTopic;
            }
            processChildTopicref(node);
            //create new map file
            //create new map's root element
            final Node root = node.getOwnerDocument().getDocumentElement().cloneNode(false);
            //create navref element
            final Element navref = node.getOwnerDocument().createElement(MAP_NAVREF.localName);
            final String newMapFile = generateFilename("MAPCHUNK", ".ditamap");
            navref.setAttribute(MAPGROUP_D_MAPREF.localName,newMapFile);
            navref.setAttribute(ATTRIBUTE_NAME_CLASS, MAP_NAVREF.toString());
            //replace node with navref
            node.getParentNode().replaceChild(navref,node);
            root.appendChild(node);
            // generate new file
            final String navmap = FileUtils.resolveFile(filePath,newMapFile);
            changeTable.put(navmap, navmap);
            outputMapFile(navmap,(Element)root);
            //chunk "by-topic"
        }else if(chunkByTopic){
            //TODO very important start point(by-topic).
            processChunk(node,true, chunkByTopic);
            if(chunkValue != null &&
                    (chunkValue.contains(CHUNK_BY_TOPIC) ||
                            chunkValue.contains(CHUNK_BY_DOCUMENT))){
                chunkByTopic = prevChunkByTopic;
            }
            processChildTopicref(node);
        }else{
            String currentPath = null;
            if(copytoValue != null){
                currentPath = FileUtils.resolveFile(filePath, copytoValue);
            }else if(hrefValue != null){
                currentPath = FileUtils.resolveFile(filePath, hrefValue);
            }
            if(currentPath != null){
                if(changeTable.containsKey(currentPath)){
                    changeTable.remove(currentPath);
                }
                if(!refFileSet.contains(currentPath)){
                    refFileSet.add(currentPath);
                }
            }

            // Here, we have a "by-document" chunk, simply
            // send it to the output.
            if ((chunkValue != null || !chunkByTopic) && currentPath != null
                    && !ATTR_PROCESSING_ROLE_VALUE_RESOURCE_ONLY.equals(processingRole)) {
                changeTable.put(currentPath, currentPath);
            }

            if (chunkValue != null &&
                    (chunkValue.contains(CHUNK_BY_TOPIC) ||
                            chunkValue.contains(CHUNK_BY_DOCUMENT))){
                chunkByTopic = prevChunkByTopic;
            }

            processChildTopicref(node);
        }

        //restore chunkByTopic if there is "by-topic" or "by-document" in chunkValue
        if(chunkValue != null &&
                (chunkValue.contains(CHUNK_BY_TOPIC) ||
                        chunkValue.contains(CHUNK_BY_DOCUMENT))){
            chunkByTopic = prevChunkByTopic;
        }

        processingRole = tempRole;

    }


    private void processChildTopicref(final Node node) {
        final NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++){
            final Node current = children.item(i);
            if(current.getNodeType()==Node.ELEMENT_NODE){
                final Element currentElem = (Element) current;
                final String classValue  = currentElem.getAttribute(ATTRIBUTE_NAME_CLASS);
                final String hrefValue = currentElem.getAttribute(ATTRIBUTE_NAME_HREF);
                final String xtrfValue = currentElem.getAttribute(ATTRIBUTE_NAME_XTRF);
                if(MAP_TOPICREF.matches(classValue)){
                    if((hrefValue.length() != 0 &&
                            !ATTR_XTRF_VALUE_GENERATED.equals(xtrfValue) &&
                            ! FileUtils.resolveFile(filePath,hrefValue)
                            .equals(changeTable.get(FileUtils.resolveFile(filePath,hrefValue)))) ||
                            MAPGROUP_D_TOPICHEAD.matches(classValue)
                            ){

                        //make sure hrefValue make sense and target file
                        //is not generated file or the element is topichead
                        processTopicref(currentElem);
                        //support topicref without href attribute
                    }else if(hrefValue.length() == 0){
                        processTopicref(currentElem);
                    }
                }
            }
        }

    }

    private void processChunk(final Element elem, final boolean separate, final boolean chunkByTopic) {
        //set up ChunkTopicParser
        try{
            final ChunkTopicParser chunkParser = new ChunkTopicParser();
            chunkParser.setLogger(logger);
            chunkParser.setup(changeTable, conflictTable, refFileSet, elem, separate, chunkByTopic, ditaext);
            chunkParser.write(filePath);
        }catch (final Exception e) {
            logger.logError(e.getMessage(), e) ;
        }
    }

    private void updateReltable(final Element elem) {
        final String hrefValue = elem.getAttribute(ATTRIBUTE_NAME_HREF);
        if (hrefValue.length() != 0){
            if(changeTable.containsKey(FileUtils.resolveFile(filePath,hrefValue))){
                String resulthrefValue = null;
                if (hrefValue.indexOf(SHARP)!=-1){
                    resulthrefValue=FileUtils.getRelativePath(filePath+UNIX_SEPARATOR+FILE_NAME_STUB_DITAMAP
                            ,FileUtils.resolveFile(filePath,hrefValue))
                            + hrefValue.substring(hrefValue.indexOf(SHARP)+1);
                }else{
                    resulthrefValue=FileUtils.getRelativePath(filePath+UNIX_SEPARATOR+FILE_NAME_STUB_DITAMAP
                            ,FileUtils.resolveFile(filePath,hrefValue));
                }
                elem.setAttribute(ATTRIBUTE_NAME_HREF, resulthrefValue);
            }
        }
        final NodeList children = elem.getChildNodes();
        for(int i = 0; i < children.getLength(); i++){
            final Node current = children.item(i);
            if(current.getNodeType() == Node.ELEMENT_NODE){
                final Element currentElem = (Element) current;
                final String classValue = currentElem.getAttribute(ATTRIBUTE_NAME_CLASS);
                if (MAP_TOPICREF.matches(classValue)){

                }
            }
        }
    }
    
    /**
     * get content.
     * @return content value {@code LinkedHashMap<String, String>}
     * @deprecated use {@link #getChangeTable()} instead
     */
    @Override
    @Deprecated
    public Content getContent() {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Get changed files table.
     * @return map of changed files
     */
    public Map<String, String> getChangeTable() {
        return Collections.unmodifiableMap(changeTable); 
    }
    
    /**
     * get conflict table.
     * @return conflict table
     */
    public Hashtable<String, String> getConflicTable() {
        return conflictTable;
    }
    /**
     * Set up environment.
     * @param ditaext ditaext
     * @param transtype transtype
     */
    public void setup(final String ditaext, final String transtype) {
        this.ditaext = ditaext;
        this.transtype = transtype;

    }

}
