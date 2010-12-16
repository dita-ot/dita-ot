package org.dita.dost.junit;
import static org.junit.Assert.assertEquals;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.tools.ant.BuildException;
import org.dita.dost.util.DITAOTCopy;
import org.junit.Test;

public class TestDITAOTCopy {
	
	public static DITAOTCopy ditaotcopy= new DITAOTCopy();
	String relativepath="testbuildaaa.xml";
	
	@Test
	public void testexecute() throws BuildException
	{
		ditaotcopy.setIncludes("test-stub" + File.separator + "testbuild.xml");
		ditaotcopy.setTodir("test-stub");
		ditaotcopy.setRelativePaths(relativepath);
		ditaotcopy.execute();

		
		 String FileName="test-stub" + File.separator + "testbuild.xml";
	      String DestFileName="test-stub" + File.separator + "testbuildaaa.xml";
	       File myFile=new File(FileName);
	       File mydestFile=new File(DestFileName);

	      
	       ditaotcopy.setRelativePaths(relativepath); 
	       ditaotcopy.execute();
	       if(!myFile.exists())
	        { 
	            System.err.println("Can't Find " + FileName);
	        }

	        try 
	        {
	            BufferedReader in = new BufferedReader(new FileReader(myFile));
	            BufferedReader in1= new BufferedReader(new FileReader(mydestFile));
	            String str;
	            String std="";
	            while ((str = in.readLine()) != null) 
	            {       
	            	std=std+str;
	            }
	          
	            in.close();
	            String ste;
	            String stj="";
	            while ((ste = in1.readLine()) != null) 
	            {       
	            	stj=stj+ste;
	            }
	          
	            in.close();
	            assertEquals(stj,std);
	        } 
	        catch (IOException e) 
	        {
	            e.getStackTrace();
	        }
	        
		
	}

	
}
