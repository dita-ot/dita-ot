package org.dita.dost.junit;
import static org.junit.Assert.assertEquals;
import org.dita.dost.module.ContentImpl;
import org.junit.Test;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.writer.CHMIndexWriter;
public class TestCHMIndexWriter {
	public static CHMIndexWriter chmindexwriter = new CHMIndexWriter();
	public static ContentImpl content = new ContentImpl();
	
	@Test
	public void testgetIndexFileName(){
		
		
		assertEquals("test-stub\\a.xml.hhk",(chmindexwriter.getIndexFileName("test-stub\\a.xml")));

	}
	
	@Test(expected = DITAOTException.class)
	public void testwrite() throws DITAOTException
	{
		String filename = "test-stub\\a.xml";
		chmindexwriter.write(filename);
	}
	

}
