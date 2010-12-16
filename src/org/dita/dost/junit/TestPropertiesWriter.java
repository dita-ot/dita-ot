package org.dita.dost.junit;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import org.dita.dost.module.Content;
import org.dita.dost.module.ContentImpl;
import org.dita.dost.util.Constants;

import org.dita.dost.writer.PropertiesWriter;
import org.junit.Test;
import org.dita.dost.exception.DITAOTException;


import static org.junit.Assert.assertEquals;

public class TestPropertiesWriter {
	
	public  static PropertiesWriter propertieswriter= new PropertiesWriter();
	private static String tempDir = "test-stub" + File.separator + "TestPropertiesWriter" + File.separator;
    @Test
    public void testwrite()throws DITAOTException, FileNotFoundException, IOException
    {
    	File outputFile = new File(tempDir, Constants.FILE_NAME_DITA_LIST);
    	File xmlDitalist=new File(tempDir, Constants.FILE_NAME_DITA_LIST_XML);
    	File inputfile=new File(tempDir,Constants.FILE_NAME_EXPORT_XML);
    	Properties prop = new Properties();
    	prop.load(new FileInputStream (inputfile));
    	Content content = new ContentImpl();
    	content.setValue(prop);
    	
		propertieswriter.setContent(content);		
		propertieswriter.write(outputFile.getAbsolutePath());
		propertieswriter.writeToXML(xmlDitalist.getAbsolutePath());
		String ditalist="test-stub" + File.separator + "TestPropertiesWriter" + File.separator + "dita.list";
		String compareditalist="test-stub" + File.separator + "TestPropertiesWriter" + File.separator + "compareofdita.list";
		String ditalistproperties="test-stub" + File.separator + "TestPropertiesWriter" + File.separator + "dita.xml.properties";
		String compareditalistproperties="test-stub" + File.separator + "TestPropertiesWriter" + File.separator + "compareofdita.xml.properties";
		File ditalistfile=new File (ditalist);
		File compareditalistfile=new File (compareditalist);
		File ditalistpropertiesfile=new File (ditalistproperties);
		File compareditalistpropertiesfile=new File(compareditalistproperties);
		  if(!ditalistfile.exists())
	        { 
	            System.err.println("Can't Find " + ditalistfile);
	        }
          if(!compareditalistfile.exists())
          {
        	  System.err.println("Can't Find " + compareditalistfile);
          }
          if(!ditalistpropertiesfile.exists())
          {
        	  System.err.println("Can't Find " + ditalistpropertiesfile);
          }
          if(!compareditalistpropertiesfile.exists())
          {
        	  System.err.println("Can't Find " + compareditalistpropertiesfile);
          }
	        try 
	        {
	            BufferedReader ditalistbuf = new BufferedReader(new FileReader(ditalistfile));
	            BufferedReader compareditalistbuf= new BufferedReader(new FileReader(compareditalistfile));
	            BufferedReader ditalistpropertiesfilebuf= new BufferedReader(new FileReader(ditalistpropertiesfile));
	            BufferedReader compareditalistpropertiesfilebuf= new BufferedReader(new FileReader(compareditalistpropertiesfile));
	            String str;
	            String st1="";
	            ditalistbuf.readLine();
	            while ((str = ditalistbuf.readLine()) != null) 
	            {      
	            	st1=st1+str;
	            }
	          
	            ditalistbuf.close();
	            String ste;
	            String st2="";
	            compareditalistbuf.readLine();
	            while ((ste = compareditalistbuf.readLine()) != null) 
	            {       
	            	st2=st2+ste;
	            }
	          
	            compareditalistbuf.close();
	            assertEquals(st1,st2);
	            
	            String stj;
	            String st3="";
	            ditalistpropertiesfilebuf.readLine();
	            while ((stj = ditalistpropertiesfilebuf.readLine()) != null) 
	            {       
	            	st3=st3+stj;
	            }
	            ditalistpropertiesfilebuf.close();
	            String stk;
	            String st4="";
	            compareditalistpropertiesfilebuf.readLine();
	            while ((stk = compareditalistpropertiesfilebuf.readLine()) != null) 
	            {       
	            	st4=st4+stk;
	            }
	            compareditalistpropertiesfilebuf.close();
	            assertEquals(st3,st4);
	        } 
	        catch (IOException e) 
	        {
	            e.getStackTrace();
	        }
		
    }
}
