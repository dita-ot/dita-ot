/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2010 All Rights Reserved.
 */
package org.dita.dost.util;
/**
 * This class is for get the first xml:lang value set in ditamap/topic files
 * 
 * @version 1.0 2010-09-30
 * 
 * @author Zhang Di Hua
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.dita.dost.log.DITAOTJavaLogger;
import org.dita.dost.log.MessageUtils;

public class CheckLang extends Task {

    private String basedir;
    
    private String tempdir;
    
    private String outputdir;
    
    private String inputmap;
    
    private String message;
    
    private DITAOTJavaLogger logger = new DITAOTJavaLogger();

	/**
     * Executes the Ant task.
     */
    public void execute(){
    	
    	logger.logInfo(message);
    	
    	Properties params = new Properties();
    	//ensure tempdir is absolute 
    	if (!new File(tempdir).isAbsolute()) {
        	tempdir = new File(basedir, tempdir).getAbsolutePath();
        }
    	//ensure outdir is absolute
		if (!new File(outputdir).isAbsolute()) {
			outputdir = new File(basedir, outputdir).getAbsolutePath();
		}
		//ensure inputmap is absolute
		if (!new File(inputmap).isAbsolute()) {
			inputmap = new File(tempdir, inputmap).getAbsolutePath();
		}
		
		
		//File object of dita.list
		File ditalist = new File(tempdir, Constants.FILE_NAME_DITA_LIST);
		//File object of dita.xml.properties
	    File xmlDitalist=new File(tempdir,Constants.FILE_NAME_DITA_LIST_XML);
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
		}catch(IOException e){
			String msg = null;
			params.put("%1", ditalist);
			msg = MessageUtils.getMessage("DOTJ011E", params).toString();
			/*msg = new StringBuffer(msg).append(Constants.LINE_SEPARATOR)
					.append(e.toString()).toString();*/
			logger.logError(msg);
		} finally {
        	if (in != null) {
        		try {
        			in.close();
        		} catch (IOException e) {
        			logger.logException(e);
        		}
        	}
        }
		
		LangParser parser = new LangParser();

        try {

            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            SAXParser saxParser = saxParserFactory.newSAXParser();
            //parse the user input file(usually a map)
            saxParser.parse(inputmap, parser);
            String langCode = parser.getLangCode();
            if(!StringUtils.isEmptyString(langCode)){
            	setActiveProjectProperty("htmlhelp.locale", langCode);
            }else{
            	Set<String> topicList = StringUtils.restoreSet((String)prop.getProperty(Constants.FULL_DITA_TOPIC_LIST));
            	//parse topic files
            	for(String topicFileName : topicList){
            		File topicFile = new File(tempdir, topicFileName);
            		if(topicFile.exists()){
	            		saxParser.parse(topicFile, parser);
	            		langCode = parser.getLangCode();
	            		if(!StringUtils.isEmptyString(langCode)){
	                    	setActiveProjectProperty("htmlhelp.locale", langCode);
	                    	break;
	                    }
            		}
            	}
            	//no lang is set
            	if(StringUtils.isEmptyString(langCode)){
            		//use default lang code
            		setActiveProjectProperty("htmlhelp.locale", "en-us");
            	}
            }
            
            

        } catch (Exception e) {
            /* Since an exception is used to stop parsing when the search
             * is successful, catch the exception.
             */
            if (e.getMessage() != null &&
                e.getMessage().equals("Search finished")) {
                System.out.println("Lang search finished");
            } else {
                e.printStackTrace();
            }
        }
    }

    /**
     * Sets property in active ant project with name specified inpropertyName,
     * and value specified in propertyValue parameter
     */
    private void setActiveProjectProperty(String propertyName, String propertyValue) {
        Project activeProject = getProject();
        if (activeProject != null) {
            activeProject.setProperty(propertyName, propertyValue);
        }
    }
    
    public void setBasedir(String basedir) {
		this.basedir = basedir;
	}

	public void setTempdir(String tempdir) {
		this.tempdir = tempdir;
	}

	public void setInputmap(String inputmap) {
		this.inputmap = inputmap;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setOutputdir(String outputdir) {
		this.outputdir = outputdir;
	}

}
