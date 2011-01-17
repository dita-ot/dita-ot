package org.dita.dost.writer;
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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.dita.dost.exception.DITAOTException;


import static org.junit.Assert.assertEquals;

public class TestPropertiesWriter {
	
	private final File tempDir = new File(System.getProperty("java.io.tmpdir"));
	private final File resourceDir = new File("test-stub" + File.separator + "TestPropertiesWriter");
	
	private File outputFile;
	private File xmlDitalist;
	
	@Before
	public void setUp() {
		outputFile = new File(tempDir, Constants.FILE_NAME_DITA_LIST);
    	xmlDitalist = new File(tempDir, Constants.FILE_NAME_DITA_LIST_XML);
	}
	
    @Test
    public void testwrite() throws DITAOTException, FileNotFoundException, IOException {
    	final File inputfile = new File(resourceDir,Constants.FILE_NAME_EXPORT_XML);
    	final Properties prop = new Properties();
    	prop.loadFromXML(new FileInputStream (inputfile));
    	final Content content = new ContentImpl();
    	content.setValue(prop);
    	final PropertiesWriter propertieswriter = new PropertiesWriter();
		propertieswriter.setContent(content);		
		propertieswriter.write(outputFile.getAbsolutePath());
		propertieswriter.writeToXML(xmlDitalist.getAbsolutePath());
		
		final File ditalistfile=new File(tempDir, Constants.FILE_NAME_DITA_LIST);		
		final File compareditalistfile=new File(resourceDir, "compareofdita.list");
		final File ditalistpropertiesfile=new File(tempDir, Constants.FILE_NAME_DITA_LIST_XML);
		final File compareditalistpropertiesfile=new File(resourceDir,  "compareofdita.xml.properties");
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
	            final BufferedReader ditalistbuf = new BufferedReader(new FileReader(ditalistfile));
	            final BufferedReader compareditalistbuf= new BufferedReader(new FileReader(compareditalistfile));
	            final BufferedReader ditalistpropertiesfilebuf= new BufferedReader(new FileReader(ditalistpropertiesfile));
	            final BufferedReader compareditalistpropertiesfilebuf= new BufferedReader(new FileReader(compareditalistpropertiesfile));
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
	        catch (final IOException e) 
	        {
	            e.getStackTrace();
	        }
    }
    
    @After
    public void tearDown() {
//        outputFile.delete();
//    	xmlDitalist.delete();
    }
    
}
