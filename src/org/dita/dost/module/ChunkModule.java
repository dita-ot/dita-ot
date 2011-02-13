/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2007 All Rights Reserved.
 */
package org.dita.dost.module;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.DITAOTJavaLogger;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.pipeline.PipelineHashIO;
import org.dita.dost.reader.ChunkMapReader;
import org.dita.dost.util.Constants;
import org.dita.dost.util.FileUtils;
import org.dita.dost.util.StringUtils;
import org.dita.dost.writer.PropertiesWriter;
import org.dita.dost.writer.TopicRefWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
/**
 * The chunking module class.
 *
 */
public class ChunkModule implements AbstractPipelineModule {

	/**
	 * Constructor.
	 */
	public ChunkModule() {
		super();
	}

	/**
	 * Entry point of chunk module. Starting from map files, it parses and
	 * processes chunk attribute, writes out the "chunked" results and finally
	 * update references pointing to "chunked" topics in other dita topics.
	 * 
	 * @param input Input parameters and resources.
	 * @return null
	 * @throws DITAOTException exception
	 */
	public AbstractPipelineOutput execute(AbstractPipelineInput input)
			throws DITAOTException {
		String tempDir = ((PipelineHashIO) input).getAttribute(Constants.ANT_INVOKER_PARAM_TEMPDIR);
		String ditaext = ((PipelineHashIO) input).getAttribute(Constants.ANT_INVOKER_PARAM_DITAEXT);
		String transtype = ((PipelineHashIO) input).getAttribute(Constants.ANT_INVOKER_EXT_PARAM_TRANSTYPE);
	    
	    DITAOTJavaLogger javaLogger = new DITAOTJavaLogger();
	    
        if (!new File(tempDir).isAbsolute()) {
    	    String baseDir = ((PipelineHashIO) input).getAttribute(Constants.ANT_INVOKER_PARAM_BASEDIR);
        	tempDir = new File(baseDir, tempDir).getAbsolutePath();
        }
        //change to xml property
	    File ditalist = new File(tempDir, Constants.FILE_NAME_DITA_LIST);
	    File xmlDitalist=new File(tempDir,Constants.FILE_NAME_DITA_LIST_XML);
	    ChunkMapReader mapReader = new ChunkMapReader();
	    mapReader.setup(ditaext, transtype);
		
	    Properties prop = new Properties();
	    InputStream in = null;
	    try{
	    	if(xmlDitalist.exists()) {
	    		in = new FileInputStream(xmlDitalist);
				prop.loadFromXML(in);
			} else {
				in = new FileInputStream(ditalist);
				prop.load(in);
			}
		}catch(IOException ioe){
			throw new DITAOTException(ioe);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					javaLogger.logException(e);
				}
			}
		}
		String mapFile = new File(tempDir, prop.getProperty(Constants.INPUT_DITAMAP)).getAbsolutePath();   
		try{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(mapFile);
			Element root = doc.getDocumentElement();
			if(root.getAttribute(Constants.ATTRIBUTE_NAME_CLASS).contains(" eclipsemap/plugin ") && transtype.equals(Constants.INDEX_TYPE_ECLIPSEHELP)){
				StringTokenizer st = new StringTokenizer(prop.getProperty(Constants.FULL_DITAMAP_LIST), Constants.COMMA);
				while(st.hasMoreTokens()){
					mapFile = new File(tempDir, st.nextToken()).getAbsolutePath();        	        
			        mapReader.read(mapFile);
				}
			}
			else{
				mapReader.read(mapFile);
			}
		}catch (Exception e){
			javaLogger.logException(e);
		}

		Content content = mapReader.getContent();
		if(content.getValue()!=null){
			// update dita.list to include new generated files
			updateList((LinkedHashMap<String,String>)content.getValue(), mapReader.getConflicTable(),input);
			// update references in dita files
			updateRefOfDita(content, mapReader.getConflicTable(),input);
		}

		
	        
	        
		return null;
	}
	//update the href in ditamap and topic files
	private void updateRefOfDita(Content changeTable, Hashtable<String, String> conflictTable, AbstractPipelineInput input){
	    DITAOTJavaLogger logger=new DITAOTJavaLogger();
	    String tempDir = ((PipelineHashIO) input).getAttribute(Constants.ANT_INVOKER_PARAM_TEMPDIR);
        if (!new File(tempDir).isAbsolute()) {
    	    String baseDir = ((PipelineHashIO) input).getAttribute(Constants.ANT_INVOKER_PARAM_BASEDIR);
        	tempDir = new File(baseDir, tempDir).getAbsolutePath();
        }
        File ditalist=new File(tempDir, Constants.FILE_NAME_DITA_LIST);
        File xmlDitalist=new File(tempDir, Constants.FILE_NAME_DITA_LIST_XML);
	    Properties prop = new Properties();
	    InputStream in = null;
	    try{
	    	if(xmlDitalist.exists()) {
	    		in = new FileInputStream(xmlDitalist);
				prop.loadFromXML(in);
			} else {
				in = new FileInputStream(ditalist);
				prop.load(in);
			}
	    }catch(IOException io){
	    	logger.logError(io.getMessage());
	    } finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					logger.logException(e);
				}
			}
		}
	    TopicRefWriter topicRefWriter=new TopicRefWriter();
		topicRefWriter.setContent(changeTable);
		topicRefWriter.setup(conflictTable);
		StringTokenizer fullTopicList=new StringTokenizer(prop.getProperty(Constants.FULL_DITAMAP_TOPIC_LIST), Constants.COMMA);
		try{
			while(fullTopicList.hasMoreTokens()){
				topicRefWriter.write(new File(tempDir,fullTopicList.nextToken()).getAbsolutePath());
			}
		}catch(DITAOTException ex){
			logger.logException(ex);
		}

	}
	
	
	private void updateList(LinkedHashMap<String, String> changeTable, Hashtable<String, String> conflictTable, AbstractPipelineInput input){
	    DITAOTJavaLogger logger=new DITAOTJavaLogger();
		String tempDir = ((PipelineHashIO) input).getAttribute(Constants.ANT_INVOKER_PARAM_TEMPDIR);
        if (!new File(tempDir).isAbsolute()) {
    		String baseDir = ((PipelineHashIO) input).getAttribute(Constants.ANT_INVOKER_PARAM_BASEDIR);
        	tempDir = new File(baseDir, tempDir).getAbsolutePath();
        	
        }
	    File ditalist=new File(tempDir,Constants.FILE_NAME_DITA_LIST);
	    File xmlDitalist=new File(tempDir,Constants.FILE_NAME_DITA_LIST_XML);
	    Properties prop = new Properties();
	    InputStream in = null;
		try{
	    	if(xmlDitalist.exists()) {
	    		in = new FileInputStream(xmlDitalist);
	    		prop.loadFromXML(in);
	    	} else { 
	    		in = new FileInputStream(ditalist);
	    		prop.load(in);
	    	}
		}catch(IOException ex){
			logger.logException(ex);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					logger.logException(e);
				}
			}
		}
		
		Set<String> hrefTopics = StringUtils.restoreSet((String)prop.getProperty(Constants.HREF_TOPIC_LIST));
		Set<String> chunkTopics = StringUtils.restoreSet((String)prop.getProperty(Constants.CHUNK_TOPIC_LIST));
		for (String s : chunkTopics) {
			if (!StringUtils.isEmptyString(s) && !s.contains(Constants.SHARP)) {
				// This entry does not have an anchor, we assume that this topic will
				// be fully chunked. Thus it should not produce any output. 
				Iterator<String> hrefit = hrefTopics.iterator();
				while(hrefit.hasNext()) {
					String ent = hrefit.next();
					if (FileUtils.resolveFile(tempDir, ent).equalsIgnoreCase(
							FileUtils.resolveFile(tempDir, s)))  {
						// The entry in hrefTopics points to the same target
						// as entry in chunkTopics, it should be removed.
						hrefit.remove();
					}
				}
			} else if (!StringUtils.isEmptyString(s) && hrefTopics.contains(s)) {
				hrefTopics.remove(s);
			}
		}
		
		Set<String> topicList = new LinkedHashSet<String>(Constants.INT_128);
		Set<String> oldTopicList = StringUtils.restoreSet((String)prop.getProperty(Constants.FULL_DITA_TOPIC_LIST));
		for (String t : hrefTopics) {
			if (t.lastIndexOf(Constants.SHARP) != -1) {
				t = t.substring(0, t.lastIndexOf(Constants.SHARP));
			}
			if (t.lastIndexOf(Constants.FILE_EXTENSION_DITAMAP) == -1) {
				String ditaext = ((PipelineHashIO) input).getAttribute(Constants.ANT_INVOKER_PARAM_DITAEXT);
				t = changeExtName(t, ditaext, ditaext);
			}
			t = FileUtils.getRelativePathFromMap(xmlDitalist.getAbsolutePath(), FileUtils.resolveFile(tempDir, t));
			topicList.add(t);
			if (oldTopicList.contains(t)) {
				oldTopicList.remove(t);
			}
		}
		
	    HashSet<String> chunkedTopicSet=new LinkedHashSet<String>(Constants.INT_128);
	    HashSet<String> chunkedDitamapSet=new LinkedHashSet<String>(Constants.INT_128);
		Set<String> ditamapList = StringUtils.restoreSet((String)prop.getProperty(Constants.FULL_DITAMAP_LIST));
		for (Map.Entry<String, String> entry: changeTable.entrySet()) {
			String oldFile=entry.getKey();
			if(entry.getValue().equals(oldFile)){
				//newly chunked file
				String newChunkedFile=entry.getValue();
				newChunkedFile=FileUtils.getRelativePathFromMap(xmlDitalist.getAbsolutePath(), newChunkedFile);
				String extName=getExtName(newChunkedFile);
				if(extName!=null && !extName.equalsIgnoreCase("DITAMAP")){
					chunkedTopicSet.add(newChunkedFile);
					if (!topicList.contains(newChunkedFile)) {
						topicList.add(newChunkedFile);
						if (oldTopicList.contains(newChunkedFile)) {
							//newly chunked file shouldn't be deleted
							oldTopicList.remove(newChunkedFile);
						}
					}
				}else{
					if (!ditamapList.contains(newChunkedFile)) {
						ditamapList.add(newChunkedFile);
						if (oldTopicList.contains(newChunkedFile)) {
							oldTopicList.remove(newChunkedFile);
						}
					}
					chunkedDitamapSet.add(newChunkedFile);
				}
			
			}
		}
		//removed extra topic files
		for (String s : oldTopicList) {
			if (!StringUtils.isEmptyString(s)) {
				File f = new File(tempDir, s);
				if(f.exists()) {
					f.delete();
				}
			}
		}
		
		//TODO we have refined topic list and removed extra topic files, next we need to clean up
		// conflictTable and try to resolve file name conflicts.
		for (Map.Entry<String,String> entry: changeTable.entrySet()) {
			String oldFile = entry.getKey();
			if (entry.getValue().equals(oldFile)) {
				// original topic file
				String targetPath = conflictTable.get(entry.getKey());
				if (targetPath != null) {
					File target = new File(targetPath);
					if (!FileUtils.fileExists(target.getAbsolutePath())) {
						// newly chunked file
						File from = new File(entry.getValue());
						String relativePath = FileUtils.getRelativePathFromMap(xmlDitalist.getAbsolutePath(), from.getAbsolutePath());
						//ensure the rename
						target.delete();
						//ensure the newly chunked file to the old one
						from.renameTo(target);
						if (topicList.contains(relativePath)) {
							topicList.remove(relativePath);
						}
						if (chunkedTopicSet.contains(relativePath)){
							chunkedTopicSet.remove(relativePath);
						}
						relativePath = FileUtils.getRelativePathFromMap(xmlDitalist.getAbsolutePath(), target.getAbsolutePath());
						topicList.add(relativePath);
						chunkedTopicSet.add(relativePath);
					} else {
						conflictTable.remove(entry.getKey());
					}
				}
			}
		}
		
		//TODO Remove newly generated files from resource-only list, these new files should not
		//     excluded from the final outputs.
		Set<String> resourceOnlySet = StringUtils.restoreSet(prop.getProperty(Constants.RESOURCE_ONLY_LIST));
		resourceOnlySet.removeAll(chunkedTopicSet);
		resourceOnlySet.removeAll(chunkedDitamapSet);
		
		prop.setProperty(Constants.RESOURCE_ONLY_LIST, StringUtils.assembleString(resourceOnlySet, Constants.COMMA));
		prop.setProperty(Constants.FULL_DITA_TOPIC_LIST,StringUtils.assembleString(topicList, Constants.COMMA));
		prop.setProperty(Constants.FULL_DITAMAP_LIST, StringUtils.assembleString(ditamapList, Constants.COMMA));
		topicList.addAll(ditamapList);
		prop.setProperty(Constants.FULL_DITAMAP_TOPIC_LIST, StringUtils.assembleString(topicList, Constants.COMMA));
	
		try {
			writeList(prop, tempDir, Constants.FULL_DITA_TOPIC_LIST);
			writeList(prop, tempDir, Constants.FULL_DITAMAP_LIST);
			writeList(prop, tempDir, Constants.FULL_DITAMAP_TOPIC_LIST);
		} catch (FileNotFoundException e) {
			logger.logException(e);
		} catch (IOException e) {
			logger.logException(e);
		}
		
		/*
		 * write filename in the list to a file, in order to use the includesfile attribute in ant script
		 */
		String[] keys={Constants.CHUNKED_DITAMAP_LIST, Constants.CHUNKED_TOPIC_LIST, Constants.RESOURCE_ONLY_LIST};
		List<Set<String>> sets = new ArrayList<Set<String>>();
		sets.add(chunkedDitamapSet);
		sets.add(chunkedTopicSet);
		sets.add(resourceOnlySet);
		for(int i=0;i<keys.length;i++){
			String key = keys[i];
			String fileKey=key.substring(0,key.lastIndexOf("list"))+"file";
			prop.put(fileKey, key.substring(0, key.lastIndexOf("list"))+".list");
			File list = new File(tempDir, prop.getProperty(fileKey));
			BufferedWriter bufferedWriter=null;
			try {
				bufferedWriter=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(list)));
				Iterator<String> it= sets.get(i).iterator();
				while(it.hasNext()){
					bufferedWriter.write(it.next());
					if(it.hasNext()) {
						bufferedWriter.write("\n");
					}
				}
				bufferedWriter.flush();
				bufferedWriter.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (bufferedWriter != null) {
					try {
						bufferedWriter.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}


		addSetToProperties(prop,Constants.CHUNKED_DITAMAP_LIST,chunkedDitamapSet);
		addSetToProperties(prop,Constants.CHUNKED_TOPIC_LIST,chunkedTopicSet);
		Content content = new ContentImpl();
		content.setValue(prop);
		PropertiesWriter writer = new PropertiesWriter();
		writer.setContent(content);
		try{
			writer.write(ditalist.getAbsolutePath());
			writer.writeToXML(xmlDitalist.getAbsolutePath());
		}catch(DITAOTException ex){
			logger.logException(ex);
		}

	}

	/**
	 * Write a property value to a list file.
	 * 
	 * @param prop source properties
	 * @param tempDir temporary directory
	 * @param list name of the list
	 */
	private void writeList(Properties prop, String tempDir, String list) throws FileNotFoundException,
			IOException {
		File topic_list=new File(tempDir, list.substring(0, list.lastIndexOf("list"))+".list");
		BufferedWriter topicWriter = null;
		try {
			topicWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(topic_list)));
			String topics[]=((String)prop.getProperty(list)).split(Constants.COMMA);
			for (int i = 0; i < topics.length; i++){
				topicWriter.write(topics[i]);
				if (i < topics.length - 1) {
					topicWriter.write("\n");
				}
				topicWriter.flush();
			}
		} finally {
			if (topicWriter != null) {
				topicWriter.close();
			}
		}
	}
	
	/**
	 * Get file extension
	 * 
	 * @param file filename, may contain a URL fragment
	 * @return file extensions
	 */
	private String getExtName(String file){
		int index = file.indexOf(Constants.SHARP);

		if (file.startsWith(Constants.SHARP)) {
			return null;
		} else if (index != -1) {
			String fileName = file.substring(0, index);
			int fileExtIndex = fileName.lastIndexOf(Constants.DOT);
			return (fileExtIndex != -1) ? fileName.substring(fileExtIndex + 1,
					fileName.length()) : null;
		} else {
			int fileExtIndex = file.lastIndexOf(Constants.DOT);
			return (fileExtIndex != -1) ? file.substring(fileExtIndex + 1,
					file.length()) : null;
		}
	}
	
	/**
	 * Change file extension.
	 * 
	 * @param filename original file name, may be <code>null</code>
	 * @param from source extension, may be <code>null</code>
	 * @param to destination extension, may be <code>null</code>
	 * @return filename with changed file extension, <code>null</code> if empty input
	 */
	private String changeExtName(String filename, String from, String to) {
		if (StringUtils.isEmptyString(filename)) {
			return null;
		}
		if (filename.indexOf(to) != -1) {
			return filename;
		}
		if (from == null) {
			from = "";
		}
		if (to == null) {
			to = "";
		}
		if (filename.lastIndexOf(from) != -1) {
			return filename.substring(0, filename.lastIndexOf(from)) + to; 
		} else {
			return filename + to;
		}
	}

	/**
	 * Add strings to set as a comma delimited list and clear input value set.
	 * 
	 * @param prop properties to add value to
	 * @param key key to add
	 * @param set set of values
	 */
	private void addSetToProperties(Properties prop, String key, Set<String> set) {
		String value = StringUtils.assembleString(set, Constants.COMMA);
		prop.put(key, value);
		// clear set
		set.clear();
	}
}
