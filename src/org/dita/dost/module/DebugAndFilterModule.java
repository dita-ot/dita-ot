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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.DITAOTJavaLogger;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.pipeline.PipelineHashIO;
import org.dita.dost.reader.DitaValReader;
import org.dita.dost.reader.ListReader;
import org.dita.dost.util.Constants;
import org.dita.dost.util.FileUtils;
import org.dita.dost.util.FilterUtils;
import org.dita.dost.writer.DitaWriter;
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
	 * File extension of source file
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
    			result.append(Constants.COMMA).append(FileUtils.replaceExtName(file));
    		} else {
    			//append one more comma at the beginning of property value
    			result.append(Constants.COMMA);
    			result.append(FileUtils.replaceExtName(file.substring(0,equalIndex)));
    			result.append(Constants.EQUAL);
    			result.append(FileUtils.replaceExtName(file.substring(equalIndex+1)));
    		}

    	}
    	String list = result.substring(Constants.INT_1);
		property.setProperty(listName, list);

		String files[] = list.split(
				Constants.COMMA);
		String filename = "";
		if (listName == "user.input.file") {
			filename = "user.input.file.list";
		} else
			filename = listName.substring(Constants.INT_0, listName
					.lastIndexOf("list"))
					+ ".list";
		try {
			BufferedWriter bufferedWriter = new BufferedWriter(
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private DITAOTJavaLogger javaLogger = new DITAOTJavaLogger();
	
	private boolean xmlValidate=true;
	/**
	 * Default Construtor
	 *
	 */
	public DebugAndFilterModule(){
	}

    /**
     * @see org.dita.dost.module.AbstractPipelineModule#execute(org.dita.dost.pipeline.AbstractPipelineInput)
     * 
     */
    public AbstractPipelineOutput execute(AbstractPipelineInput input) throws DITAOTException {
         String baseDir = ((PipelineHashIO) input).getAttribute(Constants.ANT_INVOKER_PARAM_BASEDIR);
        String ditavalFile = ((PipelineHashIO) input).getAttribute(Constants.ANT_INVOKER_PARAM_DITAVAL);
        tempDir = ((PipelineHashIO) input).getAttribute(Constants.ANT_INVOKER_PARAM_TEMPDIR);
        String ext = ((PipelineHashIO) input).getAttribute(Constants.ANT_INVOKER_PARAM_DITAEXT);
        String ditaDir=((PipelineHashIO) input).getAttribute(Constants.ANT_INVOKER_EXT_PARAM_DITADIR);;
        String inputDir = null;
        String filePathPrefix = null;
        ListReader listReader = new ListReader();
        LinkedList<String> parseList = null;
        Content content;
        DitaWriter fileWriter;
        File ditalist=null;
        File xmlDitalist=null;
		extName = ext.startsWith(Constants.DOT) ? ext : (Constants.DOT + ext);
        if (!new File(tempDir).isAbsolute()) {
        	tempDir = new File(baseDir, tempDir).getAbsolutePath();
        }
        if (ditavalFile != null && !new File(ditavalFile).isAbsolute()) {
			ditavalFile = new File(baseDir, ditavalFile).getAbsolutePath();
		}
        ditalist=new File(tempDir, Constants.FILE_NAME_DITA_LIST);
        xmlDitalist=new File(tempDir, Constants.FILE_NAME_DITA_LIST_XML);
        if(xmlDitalist.exists())
        	listReader.read(xmlDitalist.getAbsolutePath());
        else 
        	listReader.read(ditalist.getAbsolutePath());
        parseList = (LinkedList<String>) listReader.getContent()
                .getCollection();
        inputDir = (String) listReader.getContent().getValue();
        
        if (!new File(inputDir).isAbsolute()) {
        	inputDir = new File(baseDir, inputDir).getAbsolutePath();
        }
        DitaValReader filterReader = new DitaValReader();
        
        Set<String> schemaSet = listReader.getSchemeSet();
        Iterator<String> iter = schemaSet.iterator();
        while (iter.hasNext()) {
        	filterReader.loadSubjectScheme(FileUtils.resolveFile(
        			DebugAndFilterModule.tempDir, iter.next()));
        }
        
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
        	DitaWriter.initXMLReader(ditaDir,xmlValidate);
		} catch (SAXException e) {
			throw new DITAOTException(e.getMessage(), e);
		}

        fileWriter = new DitaWriter();
        content.setValue(tempDir);
        fileWriter.setContent(content);
        fileWriter.setValidateMap(filterReader.getValidValuesMap());
        fileWriter.setDefaultValueMap(filterReader.getDefaultValueMap());
        
        if(inputDir != null){
            filePathPrefix = inputDir + Constants.STICK;
        }
        
        while (!parseList.isEmpty()) {
        	String filename = (String) parseList.removeLast();
        	
        	if (!new File(inputDir, filename).exists()) {
        		// This is an copy-to target file, ignore it
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
        // reload the property for processing of copy-to
        File xmlListFile=new File(tempDir, Constants.FILE_NAME_DITA_LIST_XML);
        if(xmlListFile.exists())
        	listReader.read(xmlListFile.getAbsolutePath());
        else
        	listReader.read(new File(tempDir, Constants.FILE_NAME_DITA_LIST).getAbsolutePath());
        performCopytoTask(tempDir, listReader.getCopytoMap());

        return null;
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
        		javaLogger
						.logWarn(new StringBuffer("Copy-to task [copy-to=\"")
								.append(copytoTarget)
								.append("\"] which points to an existed file was ignored.").toString());
        	}else{
        		FileUtils.copyFile(srcFile, targetFile);
        	}
        }
	}

    private void updateList(String tempDir){
    	Properties property = new Properties();
    	FileOutputStream output = null;
    	FileOutputStream xmlDitalist=null;
    	try{
    		//property.load(new FileInputStream( new File(tempDir, Constants.FILE_NAME_DITA_LIST)));
    		property.loadFromXML(new FileInputStream( new File(tempDir, Constants.FILE_NAME_DITA_LIST_XML)));
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
    		try{
    			output.close();
    			xmlDitalist.close();
    		}catch(IOException e){
				javaLogger.logException(e);
    		}
    	}
    	
    }

}
