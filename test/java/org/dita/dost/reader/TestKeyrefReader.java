/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2010 All Rights Reserved.
 */
package org.dita.dost.reader;
import static org.junit.Assert.assertEquals;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.dita.dost.module.Content;
import org.dita.dost.reader.KeyrefReader;
import org.dita.dost.resolver.DitaURIResolverFactory;
import org.junit.Test;

public class TestKeyrefReader {
	
	File keyrefreadercompare = new File("test-stub" + File.separator + "keyrefreaderCompare.xml");
	
	@Test
	public void testKeyrefReader() throws FileNotFoundException
	{
	    String path=System.getProperty("user.dir");
		DitaURIResolverFactory.setPath(path);
		String filename = "test-stub" + File.separator + "keyrefreader.xml";
		StringBuilder content1 = new StringBuilder();
		BufferedReader reader = null;
		try{
			reader=new BufferedReader(new FileReader(keyrefreadercompare));
			String line;
			while((line=reader.readLine())!=null)
				content1.append(line);
		   }
		catch(IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (reader!=null)
				try{
					reader.close();
				}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}
		final Set <String> set=new HashSet<String> ();
		set.add("blatview");
		set.add("blatfeference");
		set.add("blatintro");
		final KeyrefReader keyrefreader = new KeyrefReader();
		keyrefreader.setKeys(set);
		keyrefreader.read(filename);
		Content content = keyrefreader.getContent();
		//keyrefreader.getContent();
        final String string1 = content.getValue().toString();
		assertEquals(content1.toString(), string1);
	
	}
	


}
