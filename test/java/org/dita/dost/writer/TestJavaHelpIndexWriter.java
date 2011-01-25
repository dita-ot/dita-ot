/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2010 All Rights Reserved.
 */
package org.dita.dost.writer;
import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.index.IndexTerm;
import org.dita.dost.module.Content;
import org.dita.dost.module.ContentImpl;
import org.dita.dost.writer.JavaHelpIndexWriter;
import org.junit.Test;
public class TestJavaHelpIndexWriter {
	JavaHelpIndexWriter javahelpindexwriter =new JavaHelpIndexWriter();
	String filenameout="test-stub" + File.separator + "TestJavaHelpIndexWriter" + File.separator + "javahelpindexwriteroutput.xml";
	FileOutputStream fileoutputstream;
	Collection collection = new ArrayList();
	IndexTerm indexterm1=new IndexTerm();
	IndexTerm indexterm2=new IndexTerm();
	File javahelpindexwriterfile =new File("test-stub" + File.separator + "TestJavaHelpIndexWriter" + File.separator + "javahelpindexwriteroutput.xml");
	File comparejavahelpindexwriterfile=new File("test-stub" + File.separator + "TestJavaHelpIndexWriter" + File.separator + "comparejavahelpindexwriteroutput.xml");
	@Test
	public void testwrite() throws DITAOTException, UnsupportedEncodingException, FileNotFoundException
	{
		
		Content content = new ContentImpl();
		indexterm1.setTermName("name1");
		indexterm1.setTermKey("indexkey1");
		indexterm2.setTermName("name2");
		indexterm2.setTermKey("indexkey2");
		indexterm1.addSubTerm(indexterm2);
	    collection.add(indexterm1);	 
	    content.setCollection(collection);
		javahelpindexwriter.setContent(content);
		javahelpindexwriter.write(filenameout);
		if(!javahelpindexwriterfile.exists())
        { 
            System.err.println("Can't Find " + javahelpindexwriterfile);
        }
       if(!comparejavahelpindexwriterfile.exists())
       {
    	  System.err.println("Can't Find " + comparejavahelpindexwriterfile);
       }
       
       try
       {
    	   BufferedReader javahelpindexwriterbuf = new BufferedReader(new FileReader(javahelpindexwriterfile));
           BufferedReader comparjavahelpindexwriterbuf= new BufferedReader(new FileReader(comparejavahelpindexwriterfile));
           String str;
           String st1="";
           javahelpindexwriterbuf.readLine();
           while ((str = javahelpindexwriterbuf.readLine()) != null) 
           {      
           	st1=st1+str;
           }
         
           javahelpindexwriterbuf.close();
           String ste;
           String st2="";
           comparjavahelpindexwriterbuf.readLine();
           while ((ste = comparjavahelpindexwriterbuf.readLine()) != null) 
           {       
           	st2=st2+ste;
           }
         
           comparjavahelpindexwriterbuf.close();
           assertEquals(st1,st2);
       }
       catch (IOException e) 
       {
           e.getStackTrace();
       }
	}
}
