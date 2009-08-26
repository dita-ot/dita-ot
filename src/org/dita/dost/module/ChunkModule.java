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
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashSet;
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

public class ChunkModule implements AbstractPipelineModule {

	public ChunkModule() {
		super();
	}

	/**
	 * Entry point of chunk module. Starting from map files, it parses and
	 * processes chunk attribute, writes out the "chunked" results and finally
	 * update references pointing to "chunked" topics in other dita topics.
	 * 
	 * @throws DITAOTException
	 * @param input Input parameters and resources.
	 * @return null
	 */
	@SuppressWarnings("unchecked")
	public AbstractPipelineOutput execute(AbstractPipelineInput input)
			throws DITAOTException {
	    String baseDir = ((PipelineHashIO) input).getAttribute(Constants.ANT_INVOKER_PARAM_BASEDIR);
		String tempDir = ((PipelineHashIO) input).getAttribute(Constants.ANT_INVOKER_PARAM_TEMPDIR);
		String ditaext = ((PipelineHashIO) input).getAttribute(Constants.ANT_INVOKER_PARAM_DITAEXT);
		String transtype = ((PipelineHashIO) input).getAttribute(Constants.ANT_INVOKER_EXT_PARAM_TRANSTYPE);
	    
	    File ditalist = null;
	    File xmlDitalist=null;
	    Properties prop = new Properties();
	    StringTokenizer st = null;
	    ChunkMapReader mapReader = new ChunkMapReader();
	    Content content;

	    DITAOTJavaLogger javaLogger = new DITAOTJavaLogger();
	    
        if (!new File(tempDir).isAbsolute()) {
        	tempDir = new File(baseDir, tempDir).getAbsolutePath();
        }
        //change to xml property
	    ditalist = new File(tempDir, Constants.FILE_NAME_DITA_LIST);
	    xmlDitalist=new File(tempDir,Constants.FILE_NAME_DITA_LIST_XML);
	    mapReader.setup(ditaext, transtype);
		     
	    try{
	    	if(xmlDitalist.exists())
	    		prop.loadFromXML(new FileInputStream(xmlDitalist));
	    	else 
	    		prop.load(new FileInputStream(ditalist));
		}catch(IOException ioe){
			throw new DITAOTException(ioe);
		}
		String mapFile = new File(tempDir, prop.getProperty(Constants.INPUT_DITAMAP)).getAbsolutePath();   
		try{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(mapFile);
			Element root = doc.getDocumentElement();
			if(root.getAttribute(Constants.ATTRIBUTE_NAME_CLASS).contains(" eclipsemap/plugin ") && transtype.equals(Constants.INDEX_TYPE_ECLIPSEHELP)){
				st = new StringTokenizer(prop.getProperty(Constants.FULL_DITAMAP_LIST), Constants.COMMA);
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

		content = mapReader.getContent();
		if(content.getValue()!=null){
			// update dita.list to include new generated files
			updateList((Hashtable<String,String>)content.getValue(), mapReader.getConflicTable(),input);
			// update references in dita files
			updateRefOfDita(content, mapReader.getConflicTable(),input);
		}

		
	        
	        
		return null;
	}
	
	private void updateRefOfDita(Content changeTable, Hashtable<String, String> conflictTable, AbstractPipelineInput input){
	    Properties prop = new Properties();	 
	    org.dita.dost.log.DITAOTJavaLogger logger=new org.dita.dost.log.DITAOTJavaLogger();
	    String baseDir = ((PipelineHashIO) input).getAttribute(Constants.ANT_INVOKER_PARAM_BASEDIR);
	    String tempDir = ((PipelineHashIO) input).getAttribute(Constants.ANT_INVOKER_PARAM_TEMPDIR);
	    File xmlDitalist=null;
	    File ditalist=null;
        if (!new File(tempDir).isAbsolute()) {
        	tempDir = new File(baseDir, tempDir).getAbsolutePath();
        }
        ditalist=new File(tempDir, Constants.FILE_NAME_DITA_LIST);
        xmlDitalist=new File(tempDir, Constants.FILE_NAME_DITA_LIST_XML);
	    try{
	    	if(xmlDitalist.exists())
	    		prop.loadFromXML(new FileInputStream(xmlDitalist));
	    	else 
	    		prop.load(new FileInputStream(ditalist));
	    }catch(IOException io){
	    	logger.logError(io.getMessage());
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
	
	
	private void updateList(Hashtable<String, String> changeTable, Hashtable<String, String> conflictTable, AbstractPipelineInput input){
		
	    Properties prop = new Properties();	 
	    HashSet<String> chunkedTopicSet=new LinkedHashSet<String>(Constants.INT_128);
	    HashSet<String> chunkedDitamapSet=new LinkedHashSet<String>(Constants.INT_128);
	    org.dita.dost.log.DITAOTJavaLogger logger=new org.dita.dost.log.DITAOTJavaLogger();
		PropertiesWriter writer = new PropertiesWriter();
		Content content = new ContentImpl();
		String baseDir = ((PipelineHashIO) input).getAttribute(Constants.ANT_INVOKER_PARAM_BASEDIR);
		String tempDir = ((PipelineHashIO) input).getAttribute(Constants.ANT_INVOKER_PARAM_TEMPDIR);
		String ditaext = ((PipelineHashIO) input).getAttribute(Constants.ANT_INVOKER_PARAM_DITAEXT);
        if (!new File(tempDir).isAbsolute()) {
        	tempDir = new File(baseDir, tempDir).getAbsolutePath();
        	
        }
        File ditalist=null;
	    File xmlDitalist=null;
	    ditalist=new File(tempDir,Constants.FILE_NAME_DITA_LIST);
	    xmlDitalist=new File(tempDir,Constants.FILE_NAME_DITA_LIST_XML);
		try{
	    	if(xmlDitalist.exists())
	    		prop.loadFromXML(new FileInputStream(xmlDitalist));
	    	else 
	    		prop.load(new FileInputStream(ditalist));
		}catch(IOException ex){
			logger.logException(ex);
		}
		
		Set<String> topicList = new LinkedHashSet<String>(Constants.INT_128);
		Set<String> oldTopicList = null;
		Set<String> ditamapList = null;
		
		Set<String> hrefTopics = null;
		Set<String> chunkTopics = null;
		
		oldTopicList = StringUtils.restoreSet((String)prop.getProperty(Constants.FULL_DITA_TOPIC_LIST));
		ditamapList = StringUtils.restoreSet((String)prop.getProperty(Constants.FULL_DITAMAP_LIST));
		
		hrefTopics = StringUtils.restoreSet((String)prop.getProperty(Constants.HREF_TOPIC_LIST));
		chunkTopics = StringUtils.restoreSet((String)prop.getProperty(Constants.CHUNK_TOPIC_LIST));
		
		Set<String> resourceOnlySet = StringUtils.restoreSet(prop.getProperty(Constants.RESOURCE_ONLY_LIST));
		
		if (hrefTopics != null && chunkTopics != null) {
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
		}
		
		if (hrefTopics != null && hrefTopics.size() > 0) {
			for (String t : hrefTopics) {
				if (t.lastIndexOf(Constants.SHARP) != -1) {
					t = t.substring(0, t.lastIndexOf(Constants.SHARP));
				}
				if (t.lastIndexOf(Constants.FILE_EXTENSION_DITAMAP) == -1) {
					t = changeExtName(t, ditaext, ditaext);
				}
				t = FileUtils.getRelativePathFromMap(xmlDitalist.getAbsolutePath(), FileUtils.resolveFile(tempDir, t));
				topicList.add(t);
				if (oldTopicList.contains(t)) {
					oldTopicList.remove(t);
				}
			}
		}
		 
		if(topicList!=null){
			String newChunkedFile=null;
			Iterator<Map.Entry<String, String>> it=changeTable.entrySet().iterator();
			File topic_list=new File(tempDir, Constants.FULL_DITA_TOPIC_LIST.substring(0, Constants.FULL_DITA_TOPIC_LIST.lastIndexOf("list"))+".list");
			File map_list=new File(tempDir, Constants.FULL_DITAMAP_LIST.substring(0, Constants.FULL_DITAMAP_LIST.lastIndexOf("list"))+".list");
			File all_list=new File(tempDir, Constants.FULL_DITAMAP_TOPIC_LIST.substring(0, Constants.FULL_DITAMAP_TOPIC_LIST.lastIndexOf("list"))+".list");
			while(it.hasNext()){
				Map.Entry<String, String> entry = it.next();
				String oldFile=(String)entry.getKey();
				if(entry.getValue().toString().equals(oldFile)){
					newChunkedFile=entry.getValue().toString();
					newChunkedFile=FileUtils.getRelativePathFromMap(xmlDitalist.getAbsolutePath(), newChunkedFile);
					String extName=getExtName(newChunkedFile);
					if(extName!=null && !extName.equalsIgnoreCase("DITAMAP")){
						chunkedTopicSet.add(newChunkedFile);
						if (!topicList.contains(newChunkedFile)) {
							topicList.add(newChunkedFile);
							if (oldTopicList.contains(newChunkedFile)) {
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
			
			for (String s : oldTopicList) {
				if (!StringUtils.isEmptyString(s)) {
					File f = new File(tempDir, s);
					if(f.exists())
						f.delete();
				}
			}
			
			//TODO we have refined topic list and removed extra topic files, next we need to clean up
			// conflictTable and try to resolve file name conflicts.
			Iterator<Map.Entry<String,String>> iter = changeTable.entrySet().iterator();
			while(iter.hasNext()) {
				Map.Entry<String,String> entry = iter.next();
				String oldFile = (String)entry.getKey();
				if (entry.getValue().toString().equals(oldFile)) {
					// newly chunked file
					String targetPath = ((String)conflictTable.get((String)entry.getKey()));
					if (targetPath != null) {
						File target = new File(targetPath);
						if (!FileUtils.fileExists(target.getAbsolutePath())) {
							File from = new File(entry.getValue().toString());
							String relativePath = FileUtils.getRelativePathFromMap(xmlDitalist.getAbsolutePath(), from.getAbsolutePath());
							target.delete();
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
			resourceOnlySet.removeAll(chunkedTopicSet);
			resourceOnlySet.removeAll(chunkedDitamapSet);
			
			prop.setProperty(Constants.RESOURCE_ONLY_LIST, StringUtils.assembleString(resourceOnlySet, Constants.COMMA));
			prop.setProperty(Constants.FULL_DITA_TOPIC_LIST,StringUtils.assembleString(topicList, Constants.COMMA));
			prop.setProperty(Constants.FULL_DITAMAP_LIST, StringUtils.assembleString(ditamapList, Constants.COMMA));
			topicList.addAll(ditamapList);
			prop.setProperty(Constants.FULL_DITAMAP_TOPIC_LIST, StringUtils.assembleString(topicList, Constants.COMMA));
			
			String topics[]=((String)prop.getProperty(Constants.FULL_DITA_TOPIC_LIST)).split(Constants.COMMA);
			String maps[]=((String)prop.getProperty(Constants.FULL_DITAMAP_LIST)).split(Constants.COMMA);
			String all[]=((String)prop.getProperty(Constants.FULL_DITAMAP_TOPIC_LIST)).split(Constants.COMMA);
			
			try {
			BufferedWriter topicWriter = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(topic_list)));
			BufferedWriter mapWriter = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(map_list)));
			BufferedWriter allWriter = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(all_list)));
			for (int i = 0; i < topics.length; i++){
				topicWriter.write(topics[i]);
				if (i < topics.length - 1) {
					topicWriter.write("\n");
				}
				topicWriter.flush();
			}
			for (int i = 0; i < maps.length; i++){
				mapWriter.write(maps[i]);
				if (i < maps.length - 1) {
					mapWriter.write("\n");
				}
				mapWriter.flush();
			}
			for (int i = 0; i < all.length; i++){
				allWriter.write(all[i]);
				if (i < all.length -1){
					allWriter.write("\n");
				}
				allWriter.flush();
			}
			} catch (FileNotFoundException e) {
				logger.logException(e);
			} catch (IOException e) {
				logger.logException(e);
			}
		}
		
		/*
		 * write filename in the list to a file, in order to use the includesfile attribute in ant script
		 */
		String[] keys={Constants.CHUNKED_DITAMAP_LIST, Constants.CHUNKED_TOPIC_LIST, Constants.RESOURCE_ONLY_LIST};
		Set sets[] = {chunkedDitamapSet, chunkedTopicSet, resourceOnlySet};
		for(int i=0;i<keys.length;i++){
			String fileKey=keys[i].substring(0,keys[i].lastIndexOf("list"))+"file";
			prop.put(fileKey, keys[i].substring(0, keys[i].lastIndexOf("list"))+".list");
			File list = new File(tempDir, prop.getProperty(fileKey));
			try {
				BufferedWriter bufferedWriter=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(list)));
				Iterator<String> it= sets[i].iterator();
				while(it.hasNext()){
					bufferedWriter.write((String)it.next());
					if(it.hasNext())
						bufferedWriter.write("\n");
				}
				bufferedWriter.flush();
				bufferedWriter.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}


		addSetToProperties(prop,Constants.CHUNKED_DITAMAP_LIST,chunkedDitamapSet);
		addSetToProperties(prop,Constants.CHUNKED_TOPIC_LIST,chunkedTopicSet);
		content.setValue(prop);
		writer.setContent(content);
		try{
			writer.write(ditalist.getAbsolutePath());
			writer.writeToXML(xmlDitalist.getAbsolutePath());
		}catch(DITAOTException ex){
			logger.logException(ex);
		}

	}
	
	private String getExtName(String file){
		String fileName;
		int fileExtIndex;
		int index;

		index = file.indexOf(Constants.SHARP);

		if (file.startsWith(Constants.SHARP)) {
			return null;
		} else if (index != -1) {
			fileName = file.substring(0, index);
			fileExtIndex = fileName.lastIndexOf(Constants.DOT);
			return (fileExtIndex != -1) ? fileName.substring(fileExtIndex + 1,
					fileName.length()) : null;
		} else {
			fileExtIndex = file.lastIndexOf(Constants.DOT);
			return (fileExtIndex != -1) ? file.substring(fileExtIndex + 1,
					file.length()) : null;
		}
	}
	
	private String changeExtName(String filename, String from, String to) {
		if (StringUtils.isEmptyString(filename)) {
			return null;
		}
		if (filename.indexOf(to) != -1) return filename;
		if (from == null) from = "";
		if (to == null) to = "";
		if (filename.lastIndexOf(from) != -1) {
			return filename.substring(0, filename.lastIndexOf(from)) + to; 
		} else {
			return filename + to;
		}
	}
	
	private void addSetToProperties(Properties prop, String key, Set<String> set) {
		String value = null;
		value = StringUtils.assembleString(set, Constants.COMMA);
		prop.put(key, value);
		// clear set
		set.clear();
	}
}
