/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2010 All Rights Reserved.
 */
package org.dita.dost.util;

import static org.junit.Assert.assertEquals;
import java.io.File;
import java.io.IOException;

import org.apache.tools.ant.BuildException;
import org.dita.dost.TestUtils;
import org.dita.dost.util.DITAOTCopy;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestDITAOTCopy {
	
	private final File resourceDir = new File("test-stub");
	private File tempDir;
	
	private File mydestFile;
	private File myFile;
	
	@Before
	public void setUp() throws IOException {
		tempDir = TestUtils.createTempDir(getClass());
		myFile = new File(tempDir, "testbuild.xml");
		FileUtils.copyFile(new File(resourceDir, "testbuild.xml"), myFile);
		mydestFile = new File(tempDir, "testbuildaaa.xml");
	}
	
	@Test
	public void testexecute() throws BuildException, IOException
	{
		DITAOTCopy ditaotcopy= new DITAOTCopy();
		ditaotcopy.setIncludes(myFile.getPath());
		ditaotcopy.setTodir(tempDir.getPath());
	       ditaotcopy.setRelativePaths(mydestFile.getName()); 
	       ditaotcopy.execute();
	       
        assertEquals(TestUtils.readFileToString(myFile),
                     TestUtils.readFileToString(mydestFile));
	}

	@After
	public void tearDown() throws IOException {
		TestUtils.forceDelete(tempDir);
	}
	
}
