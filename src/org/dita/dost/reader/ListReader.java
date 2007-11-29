/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.reader;

import java.io.FileInputStream;
import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import org.dita.dost.log.DITAOTJavaLogger;
import org.dita.dost.module.Content;
import org.dita.dost.module.ContentImpl;
import org.dita.dost.util.Constants;
import org.dita.dost.util.StringUtils;

/**
 * ListReader reads "dita.list" file in temp directory.
 * "dita.list" file contains information of the files that should be processed.
 * 
 * @author Zhang, Yuan Peng
 */
public class ListReader implements AbstractReader {

    private LinkedList refList;
    private ContentImpl content;
    private DITAOTJavaLogger logger;
    private Map copytoMap = new HashMap();

    /**
     * Default constructor of ListReader class.
     */
    public ListReader() {
        super();
        refList = new LinkedList();
        logger = new DITAOTJavaLogger();
        content = new ContentImpl();
        content.setCollection(refList);
    }


    /**
     * @see org.dita.dost.reader.AbstractReader#read(java.lang.String)
     * 
     */
    public void read(String filename) {

		FileInputStream listInput = null;

		try {
			Properties property = new Properties();
			File listFile=new File(filename);
			listInput = new FileInputStream(filename);
			if(listFile.getName().equalsIgnoreCase(Constants.FILE_NAME_DITA_LIST_XML))
				property.loadFromXML(listInput);
			else
				property.load(listInput);
			setList(property);
		} catch (Exception e) {
			logger.logException(e);
		} finally {
			try {
				listInput.close();
			} catch (Exception e) {
				logger.logException(e);
			}
		}
	}
    
    private void setList(Properties property){
        String liststr;
        StringTokenizer tokenizer;
        String copytoMapEntries;
        content.setValue(property.getProperty("user.input.dir"));
        
        /*
         * Parse copy-to target to source map list, 
         * and restore the copy-to map
         */
        copytoMapEntries = property
				.getProperty(Constants.COPYTO_TARGET_TO_SOURCE_MAP_LIST);
        copytoMap = StringUtils.restoreMap(copytoMapEntries);
        
        liststr = property.getProperty(Constants.FULL_DITAMAP_TOPIC_LIST)
				+ Constants.COMMA
				+ property.getProperty(Constants.CONREF_TARGET_LIST) 
				+ Constants.COMMA
				+ property.getProperty(Constants.COPYTO_SOURCE_LIST);
				
        tokenizer = new StringTokenizer(liststr,Constants.COMMA);
                    
        while (tokenizer.hasMoreTokens()) {
        	refList.addFirst(tokenizer.nextToken());
        }            
        
    }

    /**
     * @see org.dita.dost.reader.AbstractReader#getContent()
     * 
     */
    public Content getContent() {
        return content;
    }

    /**
     * Return the copy-to map
	 * @return
	 */
	public Map getCopytoMap() {
    	return copytoMap;
    }
}
