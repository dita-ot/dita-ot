/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.reader;

import java.io.FileInputStream;
import java.util.LinkedList;
import java.util.Properties;
import java.util.StringTokenizer;

import org.dita.dost.module.Content;
import org.dita.dost.module.ContentImpl;


/**
 * @author Zhang, Yuan Peng
 */
public class ListReader extends AbstractReader {

    private LinkedList refList;
    private ContentImpl content;

    /**
     * 
     */
    public ListReader() {
        super();
        refList = new LinkedList();
        content = new ContentImpl();
        content.setCollection(refList);
    }

    /**
     * 
     */
    public void read(String filename) {
        String liststr;
        try {
            FileInputStream listInput = new FileInputStream(filename);
            Properties property = new Properties();
            property.load(listInput);
            liststr = property.getProperty("dita.list");
            StringTokenizer list = new StringTokenizer(liststr,",");
                        
            while (list.hasMoreTokens()) {
                refList.addFirst(list.nextToken());
            }
            

        } catch (Exception e) {
            e.printStackTrace(System.out);
        }

    }

    /**
     * 
     */
    public Content getContent() {
        return content;
    }

}
