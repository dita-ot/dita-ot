package org.dita.dost.junit;
import org.dita.dost.writer.CHMIndexWriter;
import org.dita.dost.writer.EclipseIndexWriter;
import org.dita.dost.writer.IDitaTranstypeIndexWriter;
import org.dita.dost.writer.JavaHelpIndexWriter;
import org.junit.Test;
import static org.junit.Assert.assertEquals;


public class TestIDitaTranstypeIndexWriter {
	public static IDitaTranstypeIndexWriter idita1= new CHMIndexWriter(); 
	public static IDitaTranstypeIndexWriter idita2= new EclipseIndexWriter();
	public static IDitaTranstypeIndexWriter idita3= new JavaHelpIndexWriter();
	@Test
	public void testiditatranstypeindexwriter()
	{
		String rootpath=System.getProperty("user.dir");
		String path=rootpath+"\\test-stub\\index.xml";
		String outputfilename="test-stub\\iditatranstypewriter";
		assertEquals("test-stub\\iditatranstypewriter.hhk",idita1.getIndexFileName(outputfilename));
		assertEquals(path,idita2.getIndexFileName(outputfilename));
		assertEquals("test-stub\\iditatranstypewriter_index.xml",idita3.getIndexFileName(outputfilename));
	}

}
