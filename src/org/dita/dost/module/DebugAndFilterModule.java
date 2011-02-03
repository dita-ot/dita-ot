/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.module;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.Set;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.DITAOTFileLogger;
import org.dita.dost.log.DITAOTJavaLogger;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.pipeline.PipelineHashIO;
import org.dita.dost.reader.DitaValReader;
import org.dita.dost.reader.ListReader;
import org.dita.dost.util.CatalogUtils;
import org.dita.dost.util.Constants;
import org.dita.dost.util.FileUtils;
import org.dita.dost.util.FilterUtils;
import org.dita.dost.util.StringUtils;
import org.dita.dost.util.TimingUtils;
import org.dita.dost.writer.DitaWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * DebugAndFilterModule implement the second step in preprocess. It will insert debug
 * information into every dita files and filter out the information that is not 
 * necessary.
 * 
 * @author Zhang, Yuan Peng
 */
public class DebugAndFilterModule implements AbstractPipelineModule {
	private static final String [] PROPERTY_UPDATE_LIST = {"user.input.file",Constants.HREF_TARGET_LIST,
			Constants.CONREF_LIST,Constants.HREF_DITA_TOPIC_LIST,Constants.FULL_DITA_TOPIC_LIST,
			Constants.FULL_DITAMAP_TOPIC_LIST,Constants.CONREF_TARGET_LIST,Constants.COPYTO_SOURCE_LIST,
			Constants.COPYTO_TARGET_TO_SOURCE_MAP_LIST,Constants.OUT_DITA_FILES_LIST,Constants.CONREF_PUSH_LIST,
			Constants.KEYREF_LIST,Constants.CODEREF_LIST,Constants.CHUNK_TOPIC_LIST,Constants.HREF_TOPIC_LIST,
			Constants.RESOURCE_ONLY_LIST};
	/**
	 * File extension of source file.
	 */
	public static String extName = null;
    private static String tempDir = "";
	
    private static void updateProperty (String listName, Properties property){
    	StringBuffer result = new StringBuffer(Constants.INT_1024);
    	String propValue = property.getProperty(listName);
		String file;
		int equalIndex;
		int fileExtIndex;
		StringTokenizer tokenizer = null;
		
		
    	if (propValue == null || Constants.STRING_EMPTY.equals(propValue.trim())){
    		//if the propValue is null or empty
    		return;
    	}
    	
    	tokenizer = new StringTokenizer(propValue,Constants.COMMA);
    	
    	while (tokenizer.hasMoreElements()){
    		file = (String)tokenizer.nextElement();
    		equalIndex = file.indexOf(Constants.EQUAL);
    		fileExtIndex = file.lastIndexOf(Constants.DOT);

    		if(fileExtIndex != -1 &&
    				Constants.FILE_EXTENSION_DITAMAP.equalsIgnoreCase(file.substring(fileExtIndex))){
    			result.append(Constants.COMMA).append(file);
    		} else if (equalIndex == -1 ){
    			//append one more comma at the beginning of property value
    			result.append(Constants.COMMA).append(FileUtils.replaceExtName(file,extName));
    		} else {
    			//append one more comma at the beginning of property value
    			result.append(Constants.COMMA);
    			result.append(FileUtils.replaceExtName(file.substring(0,equalIndex),extName));
    			result.append(Constants.EQUAL);
    			result.append(FileUtils.replaceExtName(file.substring(equalIndex+1),extName));
    		}

    	}
    	String list = result.substring(Constants.INT_1);
		property.setProperty(listName, list);

		String files[] = list.split(
				Constants.COMMA);
		String filename = "";
		if (listName.equals("user.input.file")) {
			filename = "user.input.file.list";
		} else
			filename = listName.substring(Constants.INT_0, listName
					.lastIndexOf("list"))
					+ ".list";
		Writer bufferedWriter = null;
		try {
			bufferedWriter = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(new File(
							tempDir, filename))));
			if(files.length>0){
				for (int i = 0; i < files.length; i++) {
					bufferedWriter.write(files[i]);
					if (i < files.length - 1)
						bufferedWriter.write("\n");
					bufferedWriter.flush();
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (bufferedWriter != null) {
				try {
					bufferedWriter.close();
				} catch (IOException e) {
					final DITAOTLogger logger = new DITAOTJavaLogger();
					logger.logException(e);
				}
			}
		}
	}
	private DITAOTJavaLogger javaLogger = new DITAOTJavaLogger();
	
	private boolean xmlValidate=true;
	
	private String inputMap = null;
	
	private String ditaDir = null;
	
	private String inputDir = null;
	
	//Added on 2010-08-24 for bug:3086552 start
	private boolean setSystemid = true;
	//Added on 2010-08-24 for bug:3086552 end
	
	private DITAOTFileLogger fileLogger = DITAOTFileLogger.getInstance();
	/**
	 * Default Construtor.
	 *
	 */
	public DebugAndFilterModule(){
	}

    /**
     * @see org.dita.dost.module.AbstractPipelineModule#execute(org.dita.dost.pipeline.AbstractPipelineInput)
     * 
     */
    public AbstractPipelineOutput execute(AbstractPipelineInput input) throws DITAOTException {
    	
    	Date executeStartTime = TimingUtils.getNowTime();
    	String msg = "DebugAndFilterModule.execute(): Starting...";
    	fileLogger.logInfo(msg);
    	
        try {
			String baseDir = ((PipelineHashIO) input).getAttribute(Constants.ANT_INVOKER_PARAM_BASEDIR);
			String ditavalFile = ((PipelineHashIO) input).getAttribute(Constants.ANT_INVOKER_PARAM_DITAVAL);
			tempDir = ((PipelineHashIO) input).getAttribute(Constants.ANT_INVOKER_PARAM_TEMPDIR);
			String ext = ((PipelineHashIO) input).getAttribute(Constants.ANT_INVOKER_PARAM_DITAEXT);
			ditaDir=((PipelineHashIO) input).getAttribute(Constants.ANT_INVOKER_EXT_PARAM_DITADIR);
			//Added by William on 2009-07-18 for req #12014 start
			//get transtype
			String transtype = ((PipelineHashIO) input).getAttribute(Constants.ANT_INVOKER_EXT_PARAM_TRANSTYPE);
			//Added by William on 2009-07-18 for req #12014 start
			
			//Added on 2010-08-24 for bug:3086552 start
			String setSystemid_tmp = ((PipelineHashIO) input).getAttribute(Constants.ANT_INVOKER_EXT_PARAN_SETSYSTEMID);
			if(setSystemid_tmp.equals("yes")) {
				setSystemid = true;
			} else {
				setSystemid = false;
			}
			DitaValReader.initXMLReader(setSystemid);
			//Added on 2010-08-24 for bug:3086552 end
			
			inputDir = null;
			String filePathPrefix = null;
			ListReader listReader = new ListReader();
			LinkedList<String> parseList = null;
			Content content;
			DitaWriter fileWriter;


			extName = ext.startsWith(Constants.DOT) ? ext : (Constants.DOT + ext);
			if (!new File(tempDir).isAbsolute()) {
				tempDir = new File(baseDir, tempDir).getAbsolutePath();
			}
			if (ditavalFile != null && !new File(ditavalFile).isAbsolute()) {
				ditavalFile = new File(baseDir, ditavalFile).getAbsolutePath();
			}

			//null means default path: tempdir/dita.xml.properties
			listReader.read(null);

			parseList = (LinkedList<String>) listReader.getContent().getCollection();
			inputDir = (String) listReader.getContent().getValue();
			inputMap = new File(inputDir + File.separator + listReader.getInputMap()).getAbsolutePath();
			
			// Output subject schemas
			this.outputSubjectScheme();
			
			if (!new File(inputDir).isAbsolute()) {
				inputDir = new File(baseDir, inputDir).getAbsolutePath();
			}
			DitaValReader filterReader = new DitaValReader();
			
			if (ditavalFile!=null){
			    filterReader.read(ditavalFile);
			    content = filterReader.getContent();
			    FilterUtils.setFilterMap(filterReader.getFilterMap());
			}else{
			    content = new ContentImpl();
			    //FilterUtils.setFilterMap(null);
			}
			try{
				String valueOfValidate=((PipelineHashIO) input).getAttribute("validate");
				if(valueOfValidate!=null){
					if("false".equalsIgnoreCase(valueOfValidate))
						xmlValidate=false;
					else
						xmlValidate=true;
				}
				DitaWriter.initXMLReader(ditaDir,xmlValidate, setSystemid);
			} catch (SAXException e) {
				throw new DITAOTException(e.getMessage(), e);
			}

			fileWriter = new DitaWriter();
			content.setValue(tempDir);
			fileWriter.setContent(content);
			
			//Added by Alan Date:2009-08-04 --begin
			fileWriter.setExtName(extName);
			
			//added by William on 2009-07-18 for req #12014 start
			//set transtype
			fileWriter.setTranstype(transtype);
			//added by William on 2009-07-18 for req #12014 end
			if(inputDir != null){
			    filePathPrefix = inputDir + Constants.STICK;
			}
			
			Map<String, Set<String>> dic = readMapFromXML(Constants.FILE_NAME_SUBJECT_DICTIONARY);
			
			while (!parseList.isEmpty()) {
				String filename = (String) parseList.removeLast();
				String message = "DebugAndFilterModule.execute(): Handling file " + filename + "...";
				fileLogger.logInfo(message);
				
				Set<String> schemaSet = dic.get(filename);
				filterReader.reset();
				if (schemaSet != null) {
			        Iterator<String> iter = schemaSet.iterator();
			        while (iter.hasNext()) {
			        	filterReader.loadSubjectScheme(FileUtils.resolveFile(
			        			DebugAndFilterModule.tempDir, iter.next())+".subm");
			        }
			        if (ditavalFile!=null){
			        	filterReader.filterReset();
			            filterReader.read(ditavalFile);
			            FilterUtils.setFilterMap(filterReader.getFilterMap());
			        } else {
			        	FilterUtils.setFilterMap(null);
			        }
			        
			        fileWriter.setValidateMap(filterReader.getValidValuesMap());
			        fileWriter.setDefaultValueMap(filterReader.getDefaultValueMap());
				} else {
					if (ditavalFile!=null){
			            FilterUtils.setFilterMap(filterReader.getFilterMap());
			        } else {
			        	FilterUtils.setFilterMap(null);
			        }
				}
				
				if (!new File(inputDir, filename).exists()) {
					// This is an copy-to target file, ignore it
					System.out.println("   Ignoring a copy-to file.");
					continue;
				}
				
			    /*
			     * Usually the writer's argument for write() is used to pass in the
			     * ouput file name. But in this case, the input file name is same as
			     * output file name so we can use this argument to pass in the input
			     * file name. "|" is used to separate the path information that is
			     * not necessary to be kept (baseDir) and the path information that
			     * need to be kept in the temp directory.
			     */        	
				fileWriter.write(
						new StringBuffer().append(filePathPrefix)
							.append(filename).toString());
			}
			
			updateList(tempDir);
			//Added by William on 2010-04-16 for cvf flag support start
			//update dictionary.
			updateDictionary(tempDir);
			//Added by William on 2010-04-16 for cvf flag support end
			
			// reload the property for processing of copy-to
			File xmlListFile=new File(tempDir, Constants.FILE_NAME_DITA_LIST_XML);
			if(xmlListFile.exists())
				listReader.read(xmlListFile.getAbsolutePath());
			else
				listReader.read(new File(tempDir, Constants.FILE_NAME_DITA_LIST).getAbsolutePath());
			performCopytoTask(tempDir, listReader.getCopytoMap());
		} catch (Exception e) {
			System.err.println("Exception doing debug and filter module processing: ");
			e.printStackTrace();
		} finally {
			fileLogger.logInfo("Execution time: " + TimingUtils.reportElapsedTime(executeStartTime));
		}

        return null;
    }
    
    private static class InternalEntityResolver implements EntityResolver {

		private HashMap<String, String> catalogMap = null;
		
		public InternalEntityResolver(HashMap<String, String> map) {
			this.catalogMap = map;
		}
		
		public InputSource resolveEntity(String publicId, String systemId)
				throws SAXException, IOException {
			if (catalogMap.get(publicId) != null) {
				File dtdFile = new File((String) catalogMap.get(publicId));
				return new InputSource(dtdFile.getAbsolutePath());
			}else if (catalogMap.get(systemId) != null){
				File schemaFile = new File((String) catalogMap.get(systemId));
				return new InputSource(schemaFile.getAbsolutePath());
			}

			return null;
		}
		
	}
    
    private Map<String, Set<String>> readMapFromXML(String filename) {
    	File inputFile = new File(tempDir, filename);
    	Map<String, Set<String>> graph = new HashMap<String, Set<String>>();
    	if (!inputFile.exists()) return graph;
		Properties prop = new Properties();
		FileInputStream in = null;
		try {
			in = new FileInputStream(inputFile);
			prop.loadFromXML(in);
			in.close();
		} catch (IOException e) {
			this.javaLogger.logException(e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					javaLogger.logException(e);
				}
			}
		}
		
		Iterator<Object> it = prop.keySet().iterator();
		while (it.hasNext()) {
			String key = (String)it.next();
			String value = prop.getProperty(key);
			graph.put(key, StringUtils.restoreSet(value, Constants.COMMA));
		}
		
		return graph;
    }
	
	private void outputSubjectScheme() throws DITAOTException {
		
		Map<String, Set<String>> graph = readMapFromXML(Constants.FILE_NAME_SUBJECT_RELATION);
		
		Queue<String> queue = new LinkedList<String>();
		Set<String> visitedSet = new HashSet<String>();
		Iterator<Map.Entry<String, Set<String>>> graphIter = graph.entrySet().iterator();
		if (graphIter.hasNext()) {
			Map.Entry<String, Set<String>> entry = graphIter.next();
			queue.offer(entry.getKey());
		}
		
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			builder.setEntityResolver(new InternalEntityResolver(
					CatalogUtils.getCatalog(ditaDir)));
			
			while (!queue.isEmpty()) {
				String parent = queue.poll();
				Set<String> children = graph.get(parent);
				
				if (children != null)
					queue.addAll(children);
				if ("ROOT".equals(parent) || visitedSet.contains(parent)) continue;
				visitedSet.add(parent);
				String tmprel = FileUtils.getRelativePathFromMap(inputMap, parent);
				tmprel = FileUtils.resolveFile(DebugAndFilterModule.tempDir, tmprel)+".subm";
				Document parentRoot = null;
				if (!FileUtils.fileExists(tmprel))
					parentRoot = builder.parse(new InputSource(new FileInputStream(parent)));
				else
					parentRoot = builder.parse(new InputSource(new FileInputStream(tmprel)));
				if (children != null) {
					Iterator<String> child = children.iterator();
					while (child.hasNext()) {
						String childpath = child.next();
						Document childRoot = builder.parse(new InputSource(new FileInputStream(childpath)));
						mergeScheme(parentRoot, childRoot);
						String rel = FileUtils.getRelativePathFromMap(inputMap, childpath);
						rel = FileUtils.resolveFile(DebugAndFilterModule.tempDir, rel)+".subm";
						generateScheme(rel, childRoot);
					}
				}
				
				//Output parent scheme
				String rel = FileUtils.getRelativePathFromMap(inputMap, parent);
				rel = FileUtils.resolveFile(DebugAndFilterModule.tempDir, rel)+".subm";
				generateScheme(rel, parentRoot);
			}
		} catch (Exception e) {
			javaLogger.logException(e);
			throw new DITAOTException(e);
		}
		
	}
	
	private void mergeScheme(Document parentRoot, Document childRoot) {
		Queue<Element> pQueue = new LinkedList<Element>();
		pQueue.offer(parentRoot.getDocumentElement());
		
		while (!pQueue.isEmpty()) {
			Element pe = pQueue.poll();
			NodeList pList = pe.getChildNodes();
			for (int i = 0; i < pList.getLength(); i++) {
				Node node = pList.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE)
					pQueue.offer((Element)node);
			}
			
			String value = pe.getAttribute(Constants.ATTRIBUTE_NAME_CLASS);
			if (StringUtils.isEmptyString(value) 
					|| !value.contains(Constants.ATTR_CLASS_VALUE_SUBJECT_DEF))
				continue;
			
			if (!StringUtils.isEmptyString(
					value = pe.getAttribute(Constants.ATTRIBUTE_NAME_KEYREF))) {
				// extend child scheme
				Element target = searchForKey(childRoot.getDocumentElement(), value);
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
					Node tmpnode = childRoot.importNode(pList.item(i), false);
					if (tmpnode.getNodeType() == Node.ELEMENT_NODE
							&& searchForKey(target, 
									((Element)tmpnode).getAttribute(Constants.ATTRIBUTE_NAME_KEYS)) != null)
						continue;
					target.appendChild(tmpnode);
				}
				
			} else if (!StringUtils.isEmptyString(
					value = pe.getAttribute(Constants.ATTRIBUTE_NAME_KEYS))) {
				// merge into parent scheme
				Element target = searchForKey(childRoot.getDocumentElement(), value);
				if (target != null) {
					pList = target.getChildNodes();
					for (int i = 0; i < pList.getLength(); i++) {
						Node tmpnode = parentRoot.importNode(pList.item(i), false);
						if (tmpnode.getNodeType() == Node.ELEMENT_NODE
								&& searchForKey(pe, 
										((Element)tmpnode).getAttribute(Constants.ATTRIBUTE_NAME_KEYS)) != null)
							continue;
						pe.appendChild(tmpnode);
					}
				}
			}
		}
	}
	
	private Element searchForKey(Element root, String key) {
		if (root == null || StringUtils.isEmptyString(key)) return null;
		Queue<Element> queue = new LinkedList<Element>();
		queue.offer(root);
		
		while (!queue.isEmpty()) {
			Element pe = queue.poll();
			NodeList pchildrenList = pe.getChildNodes();
			for (int i = 0; i < pchildrenList.getLength(); i++) {
				Node node = pchildrenList.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE)
					queue.offer((Element)node);
			}
			
			String value = pe.getAttribute(Constants.ATTRIBUTE_NAME_CLASS);
			if (StringUtils.isEmptyString(value) 
					|| !value.contains(Constants.ATTR_CLASS_VALUE_SUBJECT_DEF))
				continue;
			
			value = pe.getAttribute(Constants.ATTRIBUTE_NAME_KEYS);
			if (StringUtils.isEmptyString(value)) continue;
			
			if (value.equals(key)) return pe;
		}
		return null;
	}
	
	private void generateScheme(String filename, Document root) throws DITAOTException {
		try {
			FileOutputStream file = new FileOutputStream(new File(filename));
			StreamResult res = new StreamResult(file);
			DOMSource ds = new DOMSource(root);
			TransformerFactory tff = TransformerFactory.newInstance();
			Transformer tf = tff.newTransformer();
			tf.transform(ds, res);
			if (res.getOutputStream() != null)
				res.getOutputStream().close();
			if (file != null) file.close();
		} catch (Exception e) {
			javaLogger.logException(e);
			throw new DITAOTException(e);
		}
	}
    
    
    /*
     * Execute copy-to task, generate copy-to targets base on sources
     */
	private void performCopytoTask(String tempDir, Map<String, String> copytoMap) {
        Iterator<Map.Entry<String, String>> iter = copytoMap.entrySet().iterator();
        while (iter.hasNext()) {
        	Map.Entry<String, String> entry = iter.next();
        	String copytoTarget = (String) entry.getKey();
        	String copytoSource = (String) entry.getValue();        	
        	File srcFile = new File(tempDir, copytoSource);
        	File targetFile = new File(tempDir, copytoTarget);
        	
        	if (targetFile.exists()) {
        		//edited by Alan on Date:2009-11-02 for Work Item:#1590 start
        		/*javaLogger
						.logWarn(new StringBuffer("Copy-to task [copy-to=\"")
								.append(copytoTarget)
								.append("\"] which points to an existed file was ignored.").toString());*/
        		Properties prop = new Properties();
        		prop.setProperty("%1", copytoTarget);
        		javaLogger.logWarn(MessageUtils.getMessage("DOTX064W", prop).toString());
        		//edited by Alan on Date:2009-11-02 for Work Item:#1590 end
        	}else{
        		FileUtils.copyFile(srcFile, targetFile);
        	}
        }
	}

    private void updateList(String tempDir){
    	Properties property = new Properties();
    	FileInputStream in = null;
    	FileOutputStream output = null;
    	FileOutputStream xmlDitalist=null;
    	try{
    		in = new FileInputStream( new File(tempDir, Constants.FILE_NAME_DITA_LIST_XML));
    		//property.load(new FileInputStream( new File(tempDir, Constants.FILE_NAME_DITA_LIST)));
    		property.loadFromXML(in);
    		for (int i = 0; i < PROPERTY_UPDATE_LIST.length; i ++){
    			updateProperty(PROPERTY_UPDATE_LIST[i], property);
    		}
    		
    		output = new FileOutputStream(new File(tempDir, Constants.FILE_NAME_DITA_LIST));
    		xmlDitalist=new FileOutputStream(new File(tempDir,Constants.FILE_NAME_DITA_LIST_XML));
    		property.store(output, null);
    		property.storeToXML(xmlDitalist, null);
    		output.flush();
    		xmlDitalist.flush();
    	} catch (Exception e){
    		javaLogger.logException(e);
    	} finally{
    		if (in != null) {
        		try{
        			in.close();
        		}catch(IOException e){
    				javaLogger.logException(e);
        		}
    		}
    		if (output != null) {
        		try{
        			output.close();
        		}catch(IOException e){
    				javaLogger.logException(e);
        		}
    		}
    		if (xmlDitalist != null) {
        		try{
        			xmlDitalist.close();
        		}catch(IOException e){
    				javaLogger.logException(e);
        		}
    		}
    	}
    	
    }
    
    //Added by William on 2010-04-16 for cvf flag support start
    private void updateDictionary(String tempDir){
    	//orignal map
    	Map<String, Set<String>> dic = readMapFromXML(Constants.FILE_NAME_SUBJECT_DICTIONARY);
    	//result map
    	Map<String, Set<String>> resultMap = new HashMap<String, Set<String>>();
    	//Iterate the orignal map
    	Iterator<Map.Entry<String, Set<String>>> itr = dic.entrySet().iterator();
    	while (itr.hasNext()) {
			Map.Entry<String, java.util.Set<String>> entry =  itr.next();
			//filename will be checked.
			String filename = entry.getKey();
			if(FileUtils.isTopicFile(filename)){
				//Replace extension name.
				filename = FileUtils.replaceExtName(filename, extName);
			}
			//put the updated value into the result map
			resultMap.put(filename, entry.getValue());
		}
    	
    	//Write the updated map into the dictionary file
    	this.writeMapToXML(resultMap, Constants.FILE_NAME_SUBJECT_DICTIONARY);
    	//File inputFile = new File(tempDir, Constants.FILE_NAME_SUBJECT_DICTIONARY);
    		
    }
    //Method for writing a map into xml file.
    private void writeMapToXML(Map<String, Set<String>> m, String filename) {
		if (m == null) return;
		Properties prop = new Properties();
		Iterator<Map.Entry<String, Set<String>>> iter = m.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String, Set<String>> entry = iter.next();
			String key = entry.getKey();
			String value = StringUtils.assembleString(entry.getValue(), Constants.COMMA);
			prop.setProperty(key, value);
		}
		File outputFile = new File(tempDir, filename);
		FileOutputStream os = null;
		try {
			os = new FileOutputStream(outputFile, false);
			prop.storeToXML(os, null);
			os.close();
		} catch (IOException e) {
			this.javaLogger.logException(e);
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					javaLogger.logException(e);
				}
			}
		}
	}
    //Added by William on 2010-04-16 for cvf flag support end

}
