/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2007 All Rights Reserved.
 */
package org.dita.dost.module;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import org.dita.dost.writer.PropertiesWriter;
import org.dita.dost.writer.TopicRefWriter;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.pipeline.PipelineHashIO;
import org.dita.dost.reader.ChunkMapReader;
import org.dita.dost.reader.ListReader;
import org.dita.dost.util.Constants;
import org.dita.dost.util.FileUtils;
import org.dita.dost.util.StringUtils;
import org.dita.dost.writer.DitaWriter;
import org.xml.sax.SAXException;

public class ChunkModule implements AbstractPipelineModule {

	public ChunkModule() {
		super();
		// TODO Auto-generated constructor stub
	}

	public AbstractPipelineOutput execute(AbstractPipelineInput input)
			throws DITAOTException {
		// TODO Auto-generated method stub
		String tempDir = ((PipelineHashIO) input).getAttribute(Constants.ANT_INVOKER_PARAM_TEMPDIR);
		String ditaext = ((PipelineHashIO) input).getAttribute(Constants.ANT_INVOKER_PARAM_DITAEXT);
		String transtype = ((PipelineHashIO) input).getAttribute(Constants.ANT_INVOKER_EXT_PARAM_TRANSTYPE);
	    
	    File ditalist = null;
	    Properties prop = new Properties();
	    StringTokenizer st = null;
	    ChunkMapReader mapReader = new ChunkMapReader();
	    Content content;
	    
	    ditalist = new File(tempDir, Constants.FILE_NAME_DITA_LIST);
	    
	    mapReader.setup(ditaext, transtype);
		     
	    try{
			prop.load(new FileInputStream(ditalist));
		}catch(IOException ioe){
			throw new DITAOTException(ioe);
		}
		
		st = new StringTokenizer(prop.getProperty(Constants.FULL_DITAMAP_LIST), Constants.COMMA);
		while(st.hasMoreTokens()){
			String mapFile = new File(tempDir, st.nextToken()).getAbsolutePath();        	        
	        mapReader.read(mapFile);
		}
		
		content = mapReader.getContent();
		if(content.getValue()!=null){
			// update dita.list to include new generated files
			updateList((Hashtable)content.getValue(),input);	
			// update references in dita files
			updateRefOfDita(content,input);
		}

		
	        
	        
		return null;
	}
	
	private void updateRefOfDita(Content changeTable,AbstractPipelineInput input){
	    Properties prop = new Properties();	 
	    org.dita.dost.log.DITAOTJavaLogger logger=new org.dita.dost.log.DITAOTJavaLogger();
	    String tempDir = ((PipelineHashIO) input).getAttribute(Constants.ANT_INVOKER_PARAM_TEMPDIR);
	    try{
	    	prop.load(new FileInputStream( new File(tempDir, Constants.FILE_NAME_DITA_LIST)));
	    }catch(IOException io){
	    	logger.logError(io.getMessage());
	    }
	    TopicRefWriter topicRefWriter=new TopicRefWriter();
		topicRefWriter.setContent(changeTable);
		StringTokenizer fullTopicList=new StringTokenizer(prop.getProperty(Constants.FULL_DITA_TOPIC_LIST), Constants.COMMA);
		try{
			while(fullTopicList.hasMoreTokens()){
				topicRefWriter.write(new File(tempDir,fullTopicList.nextToken()).getAbsolutePath());
			}
		}catch(DITAOTException ex){
			logger.logException(ex);
		}

	}
	
	
	private void updateList(Hashtable changeTable,AbstractPipelineInput input){
		
	    Properties prop = new Properties();	 
	    HashSet chunkedTopicSet=new HashSet(Constants.INT_128);
	    HashSet chunkedDitamapSet=new HashSet(Constants.INT_128);
	    org.dita.dost.log.DITAOTJavaLogger logger=new org.dita.dost.log.DITAOTJavaLogger();
		PropertiesWriter writer = new PropertiesWriter();
		Content content = new ContentImpl();
		String tempDir = ((PipelineHashIO) input).getAttribute(Constants.ANT_INVOKER_PARAM_TEMPDIR);
		File ditalist=new File(tempDir, Constants.FILE_NAME_DITA_LIST);
		try{
			prop.load(new FileInputStream(ditalist));
		}catch(IOException ex){
			logger.logException(ex);
		}
		
		StringBuffer topicList=new StringBuffer();
		//StringBuffer fullDitamapAndTopicList=new StringBuffer();
		StringBuffer ditamapList=new StringBuffer();
		
		topicList=topicList.append((String)prop.getProperty(Constants.FULL_DITA_TOPIC_LIST));
		//fullDitamapAndTopicList=fullDitamapAndTopicList.append((String)prop.getProperty(Constants.FULL_DITAMAP_LIST));
		ditamapList=ditamapList.append((String)prop.getProperty(Constants.FULL_DITA_TOPIC_LIST));
		 
		if(topicList!=null){
			String newChunkedFile=null;
			Iterator it=changeTable.entrySet().iterator();
			
			while(it.hasNext()){
				Map.Entry entry = (Map.Entry) it.next();
				String oldFile=(String)entry.getKey();
				if(entry.getValue().toString().equals(oldFile)){
					
					newChunkedFile=entry.getValue().toString();
					newChunkedFile=FileUtils.getRelativePathFromMap(ditalist.getAbsolutePath(), newChunkedFile);
					String extName=getExtName(newChunkedFile);
					//prop.setProperty(Constants.FULL_DITAMAP_TOPIC_LIST,fullDitamapAndTopicList.append(Constants.COMMA).append(newChunkedFile).toString());
					if(extName!=null && !extName.equalsIgnoreCase("DITAMAP")){
						chunkedTopicSet.add(newChunkedFile);
						prop.setProperty(Constants.FULL_DITA_TOPIC_LIST,topicList.append(Constants.COMMA).append(newChunkedFile).toString());
					}else{
						prop.setProperty(Constants.FULL_DITAMAP_LIST,ditamapList.append(Constants.COMMA).append(newChunkedFile).toString());
						chunkedDitamapSet.add(newChunkedFile);
					}
				
				}
			}
		}
		addSetToProperties(prop,Constants.CHUNKED_DITAMAP_LIST,chunkedDitamapSet);
		addSetToProperties(prop,Constants.CHUNKED_TOPIC_LIST,chunkedTopicSet);
		content.setValue(prop);
		writer.setContent(content);
		try{
			writer.write(ditalist.getAbsolutePath());
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
	private void addSetToProperties(Properties prop, String key, Set set) {
		String value = null;
		value = StringUtils.assembleString(set, Constants.COMMA);
		prop.put(key, value);
		// clear set
		set.clear();
	}
}
