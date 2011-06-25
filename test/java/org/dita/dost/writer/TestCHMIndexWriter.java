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
import java.io.File;
import java.io.IOException;

import org.dita.dost.TestUtils;
import org.dita.dost.module.ContentImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.writer.CHMIndexWriter;
public class TestCHMIndexWriter {
	
	private File tempDir;
	
	public static CHMIndexWriter chmindexwriter = new CHMIndexWriter();
	public static ContentImpl content = new ContentImpl();
	
	@Before
	public void setUp() throws IOException, DITAOTException {
		tempDir = TestUtils.createTempDir(getClass());
	}
	
	@Test
	public void testgetIndexFileName(){
		
		
		assertEquals(new File(tempDir, "a.xml.hhk").getAbsolutePath(),
				     (chmindexwriter.getIndexFileName(new File(tempDir,"a.xml").getAbsolutePath())));

	}
	
	@Test(expected = DITAOTException.class)
	public void testwrite() throws DITAOTException
	{
		String filename = new File(tempDir, "a.xml").getAbsolutePath();
		chmindexwriter.write(filename);
	}
	
	@After
	public void tearDown() throws IOException {
		TestUtils.forceDelete(tempDir);
	}

}
