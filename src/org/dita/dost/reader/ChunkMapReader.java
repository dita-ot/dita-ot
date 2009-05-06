/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2007 All Rights Reserved.
 */
package org.dita.dost.reader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.dita.dost.log.DITAOTJavaLogger;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.module.Content;
import org.dita.dost.module.ContentImpl;
import org.dita.dost.util.Constants;
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


public class ChunkMapReader implements AbstractReader {
	
	private DITAOTJavaLogger javaLogger = null;
	
	private boolean chunkByTopic = false;
	
	private String filePath = null;
	
	private Hashtable<String, String> changeTable = null;
	
	private Hashtable<String, String> conflictTable = null;
	
	private HashSet<String> refFileSet = null;
	
	private String ditaext = null;
	
	private String transtype = null;
	
	private ProcessingInstruction workdir = null; // Tagsmiths modification

	private ProcessingInstruction path2proj = null; // Tagsmiths modification

	public ChunkMapReader() {
		super();
		javaLogger = new DITAOTJavaLogger();
		chunkByTopic=false;// By default, processor should chunk by document.
		changeTable = new Hashtable<String, String>(Constants.INT_128);
		refFileSet = new HashSet<String>(Constants.INT_128);
		conflictTable = new Hashtable<String, String>(Constants.INT_128);
	}

	public void read(String filename) {
		File inputFile = new File(filename);
        filePath = inputFile.getParent();
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(filename);
			
			// Start Tagsmiths modification: Added logic to collect the 
			// workdir and path2proj processing instructions.
			NodeList docNodes = doc.getChildNodes();
			for (int i = 0; i < docNodes.getLength(); i++) {
				Node node = docNodes.item(i);
				if (node.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE) {
					ProcessingInstruction pi = (ProcessingInstruction) node;
					if (pi.getNodeName() == "workdir") {
						workdir = pi;
					} else if (pi.getNodeName() == "path2project") {
						path2proj = pi;
					}
				}
			}
			// End Tagsmiths modification

			
			Element root = doc.getDocumentElement();
			NodeList list = root.getChildNodes();
			String rootChunkValue = root.getAttribute(Constants.ATTRIBUTE_NAME_CHUNK);
			if(rootChunkValue != null &&
					rootChunkValue.contains("by-topic")){
				chunkByTopic = true;
			}else{
				chunkByTopic = false;
			}
			
			if(root.getAttribute(Constants.ATTRIBUTE_NAME_CHUNK) != null &&
					root.getAttribute(Constants.ATTRIBUTE_NAME_CHUNK).indexOf("to-content")!=-1){
				// if to-content is specified on map element
				
				// create the reference to the new file on root element.
				Random random = new Random();
				String newFilename = inputFile.getName().substring(
						0, inputFile.getName().indexOf(Constants.FILE_EXTENSION_DITAMAP)) + ditaext;
				File newFile = new File(inputFile.getParentFile().getAbsolutePath(),newFilename);
				if (newFile.exists()) {
					newFilename = "Chunk"
							+new Integer(Math.abs(random.nextInt())).toString()+ditaext;
					String oldpath = newFile.getAbsolutePath();
					newFile = new File(FileUtils.resolveFile(inputFile.getParentFile().getAbsolutePath(), newFilename));
					// Mark up the possible name changing, in case that references might be updated.
					conflictTable.put(newFile.getAbsolutePath(), FileUtils.removeRedundantNames(oldpath));
				} 

				String originClassValue = root.getAttribute(Constants.ATTRIBUTE_NAME_CLASS);
				root.setAttribute(Constants.ATTRIBUTE_NAME_CLASS, Constants.ATTR_CLASS_VALUE_TOPICREF);
				root.setAttribute(Constants.ATTRIBUTE_NAME_HREF, newFilename);
				
				//create the new file
				OutputStreamWriter newFileWriter = null;
				try{
					newFileWriter = new OutputStreamWriter(new FileOutputStream(newFile), Constants.UTF8);
					newFileWriter.write(Constants.XML_HEAD);
					newFileWriter.write("<?workdir /"+newFile.getParentFile().getAbsolutePath()+"?>");
					newFileWriter.write("<dita></dita>");
					newFileWriter.flush();
					newFileWriter.close();
				}catch (Exception e) {
					javaLogger.logException(e);
				}finally{
					try{
						if(newFileWriter!=null){
							newFileWriter.close();
						}
					}catch (Exception e) {
						javaLogger.logException(e);
					}					
				}
				
				//process chunk
				processTopicref(root);
				
				//add newly created file to changeTable
				changeTable.put(newFile.getAbsolutePath(),newFile.getAbsolutePath());
				
				// restore original root element
				if(originClassValue != null){
					root.setAttribute(Constants.ATTRIBUTE_NAME_CLASS, originClassValue);
				}
				root.removeAttribute(Constants.ATTRIBUTE_NAME_HREF);
				
			}else{
				// if to-content is not specified on map element
				for (int i = 0; i < list.getLength(); i++){
					Node node = list.item(i);
					Node classAttr = null;
					String classValue = null;
					if (node.getNodeType() == Node.ELEMENT_NODE){
						classAttr = node.getAttributes().getNamedItem("class");
						
						if(classAttr != null){
							classValue = classAttr.getNodeValue();
						}
						
						if(classValue != null && classValue.indexOf(Constants.ATTR_CLASS_VALUE_RELTABLE)!=-1){
							updateReltable((Element)node);
						}
						if(classValue != null && classValue.indexOf(Constants.ATTR_CLASS_VALUE_TOPICREF)!=-1
								&& !classValue.contains(Constants.ATTR_CLASS_VALUE_TOPIC_GROUP)){
							processTopicref(node);
						}
						
					}
				}
			}
			
			
			outputMapFile(inputFile.getAbsolutePath()+".chunk",root);
			if(!inputFile.delete()){
            	Properties prop = new Properties();
            	prop.put("%1", inputFile.getPath());
            	prop.put("%2", inputFile.getAbsolutePath()+".chunk");
            	javaLogger.logError(MessageUtils.getMessage("DOTJ009E", prop).toString());
            }
            if(!new File(inputFile.getAbsolutePath()+".chunk").renameTo(inputFile)){
            	Properties prop = new Properties();
            	prop.put("%1", inputFile.getPath());
            	prop.put("%2", inputFile.getAbsolutePath()+".chunk");
            	javaLogger.logError(MessageUtils.getMessage("DOTJ009E", prop).toString());
            }
			
		}catch (Exception e){
			javaLogger.logException(e);
		}

	}

	private void outputMapFile(String file, Element root) {
		
		OutputStreamWriter output = null;
		try{
		output = new OutputStreamWriter(
				new FileOutputStream(file),
				Constants.UTF8);
		// Start Tagsmiths modification: XML_HEAD and the workdir and
		// path2proj processing instructions were not being sent to output.
		// The follow few lines corrects that problem.
		output.write(Constants.XML_HEAD);
		if (workdir != null)
			output(workdir, output);
		if (path2proj != null)
			output(path2proj, output);
		// End Tagsmiths modification

		output(root,output);
		output.flush();
		output.close();
		}catch (Exception e) {
			javaLogger.logException(e);
		}finally{
			try{
				if(output!=null){
					output.close();
				}
			}catch (Exception e) {
				javaLogger.logException(e);
			}
		}
	}
		
	private void output(ProcessingInstruction instruction,Writer outputWriter) throws IOException{
		outputWriter.write("<?"+instruction.getTarget()+" "+instruction.getData()+"?>");		
	}


	private void output(Text text, Writer outputWriter) throws IOException{
		outputWriter.write(StringUtils.escapeXML(text.getData()));
	}


	private void output(Element elem, Writer outputWriter) throws IOException{
		outputWriter.write("<"+elem.getNodeName());
		NamedNodeMap attrMap = elem.getAttributes();
		for (int i = 0; i<attrMap.getLength(); i++){
			outputWriter.write(" "+attrMap.item(i).getNodeName()
					+"=\""+StringUtils.escapeXML(attrMap.item(i).getNodeValue())
					+"\"");
		}
		outputWriter.write(">");
		NodeList children = elem.getChildNodes();
		Node child;
		for (int j = 0; j<children.getLength(); j++){
			child = children.item(j);
			switch (child.getNodeType()){
			case Node.TEXT_NODE:
				output((Text) child, outputWriter); break;
			case Node.PROCESSING_INSTRUCTION_NODE:
				output((ProcessingInstruction) child, outputWriter); break;
			case Node.ELEMENT_NODE:
				output((Element) child, outputWriter);
			}
		}
		
		outputWriter.write("</"+elem.getNodeName()+">");
	}
	
	private void processTopicref(Node node) {
		NamedNodeMap attr = null;
		Node hrefAttr = null;
		Node chunkAttr = null;
		Node copytoAttr = null;
		Node scopeAttr = null;
		Node classAttr = null;
		Node xtrfAttr = null;
		String hrefValue = null;
		String chunkValue = null;
		String copytoValue = null;
		String scopeValue = null;
		String classValue = null;
		String xtrfValue = null;
		boolean prevChunkByTopic = false;
		
		attr = node.getAttributes();
		
		hrefAttr = attr.getNamedItem(Constants.ATTRIBUTE_NAME_HREF);
		chunkAttr = attr.getNamedItem(Constants.ATTRIBUTE_NAME_CHUNK);
		copytoAttr = attr.getNamedItem(Constants.ATTRIBUTE_NAME_COPY_TO);
		scopeAttr = attr.getNamedItem(Constants.ATTRIBUTE_NAME_SCOPE);
		classAttr = attr.getNamedItem(Constants.ATTRIBUTE_NAME_CLASS);
		xtrfAttr = attr.getNamedItem(Constants.ATTRIBUTE_NAME_XTRF);
		
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
		
		if (xtrfValue != null && xtrfValue.contains("generated_by_chunk")) return;
		
		//set chunkByTopic if there is "by-topic" or "by-document" in chunkValue
		if(chunkValue != null && 
				(chunkValue.contains("by-topic") || 
						chunkValue.contains("by-document"))){
			prevChunkByTopic = chunkByTopic;
			//if there is "by-topic" then chunkByTopic should be set to true;
			chunkByTopic = chunkValue.contains("by-topic");
		}
		
		if("external".equalsIgnoreCase(scopeValue) 
				|| (hrefValue != null && !FileUtils.fileExists(FileUtils.resolveFile(filePath, hrefValue)))
				|| (classValue.contains(Constants.ATTR_CLASS_VALUE_TOPIC_HEAD))) {
			//Skip external links or non-existing files.
			//Skip topic head entries.
			//Skip @processing-role=resource-only entries.
			if(chunkValue != null && 
					(chunkValue.contains("by-topic") || 
							chunkValue.contains("by-document"))){
				chunkByTopic = prevChunkByTopic;
			}
			processChildTopicref(node);
		} else if(chunkValue != null &&
				chunkValue.indexOf("to-content") != -1){
			//if this is the start point of the content chunk
			processChunk((Element)node,false, chunkByTopic);
		}else if(chunkValue != null &&
				chunkValue.indexOf("to-navigation")!=-1 &&
				Constants.INDEX_TYPE_ECLIPSEHELP.equals(transtype)){
			//if this is the start point of the navigation chunk
			if(chunkValue != null && 
					(chunkValue.contains("by-topic") || 
							chunkValue.contains("by-document"))){
				chunkByTopic = prevChunkByTopic;
			}
			processChildTopicref(node);
			//create new map file
			//create new map's root element			
			Node root = node.getOwnerDocument().getDocumentElement().cloneNode(false);
			//create navref element
			Element navref = node.getOwnerDocument().createElement("navref");
			Random random = new Random();
			String newMapFile = "MAPCHUNK"+Math.abs(random.nextInt())+".ditamap";
			navref.setAttribute("mapref",newMapFile);
			//replace node with navref
			node.getParentNode().replaceChild(navref,node);
			root.appendChild(node);			
			// generate new file
			String navmap = FileUtils.resolveFile(filePath,newMapFile);
			changeTable.put(navmap, navmap);
			outputMapFile(navmap,(Element)root);
			
		}else if(chunkByTopic){
			processChunk((Element)node,true, chunkByTopic);
			if(chunkValue != null && 
					(chunkValue.contains("by-topic") || 
							chunkValue.contains("by-document"))){
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
			if ((chunkValue != null || !chunkByTopic) && currentPath != null)
				changeTable.put(currentPath, currentPath);
			
			if (chunkValue != null && 
					(chunkValue.contains("by-topic") || 
							chunkValue.contains("by-document"))){
				chunkByTopic = prevChunkByTopic;
			}
			
			processChildTopicref(node);
		}	
		
		//restore chunkByTopic if there is "by-topic" or "by-document" in chunkValue
		if(chunkValue != null && 
				(chunkValue.contains("by-topic") || 
						chunkValue.contains("by-document"))){
			chunkByTopic = prevChunkByTopic;
		}
		
	}	
	

	private void processChildTopicref(Node node) {
		NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); i++){
			Node current = children.item(i);
			if(current.getNodeType()==Node.ELEMENT_NODE){
				String classValue  = ((Element)current).getAttribute(Constants.ATTRIBUTE_NAME_CLASS);
				String hrefValue = ((Element)current).getAttribute(Constants.ATTRIBUTE_NAME_HREF);
				String xtrfValue = ((Element)current).getAttribute(Constants.ATTRIBUTE_NAME_XTRF);
				if(classValue.indexOf(Constants.ATTR_CLASS_VALUE_TOPICREF)!=-1){
					if((!hrefValue.equals(Constants.STRING_EMPTY) &&
							!"generated_by_chunk".equals(xtrfValue) &&
							! FileUtils.resolveFile(filePath,hrefValue)
							.equals(changeTable.get(FileUtils.resolveFile(filePath,hrefValue)))) || 
							classValue.contains(Constants.ATTR_CLASS_VALUE_TOPICHEAD)){
						//make sure hrefValue make sense and target file 
						//is not generated file or the element is topichead
						processTopicref(current);
					}					
				}
			}
		}
		
	}

	private void processChunk(Element elem, boolean separate, boolean chunkByTopic) {
		//set up ChunkTopicParser
		try{
			ChunkTopicParser chunkParser = new ChunkTopicParser();
			chunkParser.setup(changeTable, conflictTable, refFileSet, elem, separate, chunkByTopic, ditaext);
			chunkParser.write(filePath);
		}catch (Exception e) {
			javaLogger.logException(e);
		}
	}

	private void updateReltable(Element elem) {
		String hrefValue = elem.getAttribute(Constants.ATTRIBUTE_NAME_HREF);
		String resulthrefValue = null;
		if (!hrefValue.equals(Constants.STRING_EMPTY)){
			if(changeTable.containsKey(FileUtils.resolveFile(filePath,hrefValue))){
				if (hrefValue.indexOf(Constants.SHARP)!=-1){
					resulthrefValue=FileUtils.getRelativePathFromMap(filePath+Constants.SLASH+"stub.ditamap"
							,FileUtils.resolveFile(filePath,hrefValue))
					+ hrefValue.substring(hrefValue.indexOf(Constants.SHARP)+1);
				}else{
					resulthrefValue=FileUtils.getRelativePathFromMap(filePath+Constants.SLASH+"stub.ditamap"
							,FileUtils.resolveFile(filePath,hrefValue));
				}
				elem.setAttribute(Constants.ATTRIBUTE_NAME_HREF, resulthrefValue);
			}
		}
		NodeList children = elem.getChildNodes();
		for(int i = 0; i < children.getLength(); i++){
			Node current = children.item(i);
			if(current.getNodeType() == Node.ELEMENT_NODE){
				String classValue = ((Element)current).getAttribute(Constants.ATTRIBUTE_NAME_CLASS);
				if (classValue.indexOf(Constants.ATTR_CLASS_VALUE_TOPICREF)!=-1){
					
				}
			}
		}
	}

	public Content getContent() {
		Content content = new ContentImpl();
		content.setValue(changeTable);
		return content;
	}
	
	public Hashtable<String, String> getConflicTable() {
		return this.conflictTable;
	}

	public void setup(String ditaext, String transtype) {
		this.ditaext = ditaext;
		this.transtype = transtype;
		
	}

}
