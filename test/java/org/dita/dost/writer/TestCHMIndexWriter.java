package org.dita.dost.writer;
import static org.junit.Assert.assertEquals;
import java.io.File;
import org.dita.dost.module.ContentImpl;
import org.junit.Test;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.writer.CHMIndexWriter;
public class TestCHMIndexWriter {
	public static CHMIndexWriter chmindexwriter = new CHMIndexWriter();
	public static ContentImpl content = new ContentImpl();
	
	@Test
	public void testgetIndexFileName(){
		
		
		assertEquals("test-stub" + File.separator + "a.xml.hhk",(chmindexwriter.getIndexFileName("test-stub" + File.separator + "a.xml")));

	}
	
	@Test(expected = DITAOTException.class)
	public void testwrite() throws DITAOTException
	{
		String filename = "test-stub" + File.separator + "a.xml";
		chmindexwriter.write(filename);
	}
	

}
