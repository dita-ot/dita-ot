package org.dita.dost.junit;
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
	public static KeyrefReader keyrefreader = new KeyrefReader();
	Set <String> set=new HashSet<String> ();
	File keyrefreadercompare=new File("test-stub/keyrefreaderCompare.xml");
	BufferedReader reader=null;
	String content1;
	@Test
	public void TestKeyrefReader() throws FileNotFoundException
	{
	    String path=System.getProperty("user.dir");
		DitaURIResolverFactory.setPath(path);
		String filename = "test-stub/keyrefreader.xml";
		try{
			reader=new BufferedReader(new FileReader(keyrefreadercompare));
			String line;
			content1="";
			while((line=reader.readLine())!=null)
				content1+=line+"";
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
		set.add("blatview");
		set.add("blatfeference");
		set.add("blatintro");
		keyrefreader.setKeys(set);
		keyrefreader.read(filename);
		Content content = keyrefreader.getContent();
		keyrefreader.getContent();
        String string1;
        string1=content.getValue().toString();
		assertEquals(content1,string1);
	
	}
	


}
