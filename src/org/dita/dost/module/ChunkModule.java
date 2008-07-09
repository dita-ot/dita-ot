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
		// TODO Auto-generated constructor stub
	}

	public AbstractPipelineOutput execute(AbstractPipelineInput input)
			throws DITAOTException {
		// TODO Auto-generated method stub
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
			updateList((Hashtable)content.getValue(),input);	
			// update references in dita files
			updateRefOfDita(content,input);
		}

		
	        
	        
		return null;
	}
	
	private void updateRefOfDita(Content changeTable,AbstractPipelineInput input){
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
		String baseDir = ((PipelineHashIO) input).getAttribute(Constants.ANT_INVOKER_PARAM_BASEDIR);
		String tempDir = ((PipelineHashIO) input).getAttribute(Constants.ANT_INVOKER_PARAM_TEMPDIR);
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
		
		StringBuffer topicList=new StringBuffer();
		//StringBuffer fullDitamapAndTopicList=new StringBuffer();
		StringBuffer ditamapList=new StringBuffer();
		
		topicList=topicList.append((String)prop.getProperty(Constants.FULL_DITA_TOPIC_LIST));
		//fullDitamapAndTopicList=fullDitamapAndTopicList.append((String)prop.getProperty(Constants.FULL_DITAMAP_LIST));
		ditamapList=ditamapList.append((String)prop.getProperty(Constants.FULL_DITA_TOPIC_LIST));
		 
		if(topicList!=null){
			String newChunkedFile=null;
			Iterator it=changeTable.entrySet().iterator();
			File topic_list=new File(tempDir, Constants.FULL_DITA_TOPIC_LIST.substring(0, Constants.FULL_DITA_TOPIC_LIST.lastIndexOf("list"))+".list");
			File map_list=new File(tempDir, Constants.FULL_DITAMAP_LIST.substring(0, Constants.FULL_DITAMAP_LIST.lastIndexOf("list"))+".list");
			
			while(it.hasNext()){
				Map.Entry entry = (Map.Entry) it.next();
				String oldFile=(String)entry.getKey();
				if(entry.getValue().toString().equals(oldFile)){
					
					newChunkedFile=entry.getValue().toString();
					newChunkedFile=FileUtils.getRelativePathFromMap(xmlDitalist.getAbsolutePath(), newChunkedFile);
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
			String topics[]=((String)prop.getProperty(Constants.FULL_DITA_TOPIC_LIST)).split(Constants.COMMA);
			String maps[]=((String)prop.getProperty(Constants.FULL_DITAMAP_LIST)).split(Constants.COMMA);
			
			try {
			BufferedWriter topicWriter = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(topic_list)));
			BufferedWriter mapWriter = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(map_list)));
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
			} catch (FileNotFoundException e) {
				logger.logException(e);
			} catch (IOException e) {
				logger.logException(e);
			}
		}
		
		/*
		 * write filename in the list to a file, in order to use the includesfile attribute in ant script
		 */
		String[] keys={Constants.CHUNKED_DITAMAP_LIST,Constants.CHUNKED_TOPIC_LIST};
		Set sets[]={chunkedDitamapSet,chunkedTopicSet};
		for(int i=0;i<keys.length;i++){
			String fileKey=keys[i].substring(0,keys[i].lastIndexOf("list"))+"file";
			prop.put(fileKey, keys[i].substring(0, keys[i].lastIndexOf("list"))+".list");
			File list = new File(tempDir, prop.getProperty(fileKey));
			try {
				BufferedWriter bufferedWriter=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(list)));
				Iterator it=sets[i].iterator();
				while(it.hasNext()){
					bufferedWriter.write((String)it.next());
					if(it.hasNext())
						bufferedWriter.write("\n");
				}
				bufferedWriter.flush();
				bufferedWriter.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
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
	private void addSetToProperties(Properties prop, String key, Set set) {
		String value = null;
		value = StringUtils.assembleString(set, Constants.COMMA);
		prop.put(key, value);
		// clear set
		set.clear();
	}
}
