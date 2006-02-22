/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.reader;

import java.io.FileInputStream;
import java.util.LinkedList;
import java.util.Properties;
import java.util.StringTokenizer;

import org.dita.dost.log.DITAOTJavaLogger;
import org.dita.dost.module.Content;
import org.dita.dost.module.ContentImpl;
import org.dita.dost.util.Constants;

/**
 * ListReader reads "dita.list" file in temp directory.
 * "dita.list" file contains information of the files that should be processed.
 * 
 * @author Zhang, Yuan Peng
 */
public class ListReader extends AbstractReader {

    private LinkedList refList;
    private ContentImpl content;
    private DITAOTJavaLogger logger;


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
        String liststr;
        StringTokenizer tokenizer;
        FileInputStream listInput = null;
        try {
            listInput = new FileInputStream(filename);
            Properties property = new Properties();
            property.load(listInput);
            liststr = property.getProperty(Constants.FULL_DITAMAP_TOPIC_LIST);
            tokenizer = new StringTokenizer(liststr,Constants.COMMA);
                        
            while (tokenizer.hasMoreTokens()) {
                refList.addFirst(tokenizer.nextToken());
            }
            

        } catch (Exception e) {
        	logger.logException(e);
        }finally{
            try{
                listInput.close();
            }catch (Exception e) {
            	logger.logException(e);              
            }
            
        }

    }


    /**
     * @see org.dita.dost.reader.AbstractReader#getContent()
     * 
     */
    public Content getContent() {
        return content;
    }

}
