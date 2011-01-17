package org.dita.dost.junit;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.dita.dost.util.GenUtils;
import java.io.File;
import java.io.BufferedReader; 
import java.io.FileReader; 
import java.io.IOException; 
public class TestGenutils {
	@Test
	public void testflush()
	{
		GenUtils.clear();
		GenUtils.setOutput("test-stub" + File.separator + "genutils.xml");
		GenUtils.startElement("topic");
		
		GenUtils.addAttr("id", "this is a id");
		GenUtils.addText("this is a text");
		GenUtils.endElement("topic");
		GenUtils.flush();
		
		 String FileName="test-stub" + File.separator + "genutils.xml";
	      
	       File myFile=new File(FileName);
	       
	        if(!myFile.exists())
	        { 
	            System.err.println("Can't Find " + FileName);
	        }

	        try 
	        {
	            BufferedReader in = new BufferedReader(new FileReader(myFile));
	            String str;
	            String std="";
	            while ((str = in.readLine()) != null) 
	            {       
	            	std=std+str;
	            }
	        
	            in.close();
	            assertEquals("<?xml version=\"1.0\" encoding=\"utf-8\"?><topic id=\"this is a id\">this is a text</topic>",std);
	        } 
	        catch (IOException e) 
	        {
	            e.getStackTrace();
	        }
	      
       // assertEquals("<?xml version=\"1.0\" encoding=\"utf-8\"?><topic id=\"this is a id\">this is a text</topic>",str);
	       
	}

}
