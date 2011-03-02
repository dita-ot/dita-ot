/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2011 All Rights Reserved.
 */
package org.dita.dost.platform;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.dita.dost.TestUtils;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.util.Constants;
import org.dita.dost.util.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class IntegratorTest {

	private final File resourceDir = new File("test-stub", IntegratorTest.class.getSimpleName());
	private final File expDir = new File(resourceDir, "exp");
	private File tempDir;

	@Before
	public void setUp() throws IOException, DITAOTException {
		tempDir = TestUtils.createTempDir(getClass());
		TestUtils.copy(new File(resourceDir, "src"), tempDir);
	}
	
	@Test
	public void testIntegrator() {
		new Integrator();
	}

	@Test
	public void testGetBasedir() {
		final Integrator i = new Integrator();
		assertNull(i.getBasedir());
		i.setBasedir(new File("foo"));
		assertEquals(new File("foo"), i.getBasedir());
	}

	@Test
	public void testSetBasedir() {
		final Integrator i = new Integrator();
		i.setBasedir(new File("foo"));
		assertEquals(new File("foo"), i.getBasedir());
		i.setBasedir(null);
		assertNull(i.getBasedir());
	}

	@Test
	public void testGetDitaDir() {
		final Integrator i = new Integrator();
		assertNull(i.getDitaDir());
		i.setDitaDir(new File("foo"));
		assertEquals(new File("foo"), i.getDitaDir());
	}

	@Test
	public void testSetDitaDir() {
		final Integrator i = new Integrator();
		i.setDitaDir(new File("foo"));
		assertEquals(new File("foo"), i.getDitaDir());
		i.setDitaDir(null);
		assertNull(i.getDitaDir());
	}

	@Test
	public void testGetProperties() {
		final Integrator i = new Integrator();
		assertNull(i.getProperties());
		i.setProperties(new File("foo"));
		assertEquals(new File("foo").getAbsolutePath(), i.getProperties().getAbsolutePath());
	}

	@Test
	public void testSetProperties() {
		final Integrator i = new Integrator();
		i.setProperties(new File("foo"));
		assertEquals(new File("foo").getAbsolutePath(), i.getProperties().getAbsolutePath());
		i.setProperties(null);
		assertNull(i.getProperties());
	}

	@Test
	public void testExecute() throws Exception {
		final File libDir = new File(tempDir, "lib");
		if (!libDir.exists() && !libDir.mkdirs()) {
			throw new IOException("Failed to create directory " + libDir);
		}
		
		final Integrator i = new Integrator();
		i.setBasedir(tempDir);
		i.setDitaDir(tempDir);
		i.setProperties(new File(tempDir, "integrator.properties"));
		i.execute();
		
		
		assertEquals(getProperties(new File(expDir, "lib" + File.separator + Constants.CONF_PROPERTIES)),
					 getProperties(new File(tempDir, "lib" + File.separator + Constants.CONF_PROPERTIES)));
		assertEquals(TestUtils.readXmlToString(new File(expDir, "build.xml"), true, false),
					 TestUtils.readXmlToString(new File(tempDir, "build.xml"), true, false));
		assertEquals(TestUtils.readXmlToString(new File(expDir, "catalog.xml"), true, false),
				     TestUtils.readXmlToString(new File(tempDir, "catalog.xml"), true, false));
		assertEquals(TestUtils.readXmlToString(new File(expDir, "xsl" + File.separator + "shell.xsl"), true, false),
			         TestUtils.readXmlToString(new File(tempDir, "xsl" + File.separator + "shell.xsl"), true, false));
		assertEquals(TestUtils.readXmlToString(new File(expDir, "xsl" + File.separator + "common" + File.separator + "allstrings.xml"), true, false),
		         	 TestUtils.readXmlToString(new File(tempDir, "xsl" + File.separator + "common" + File.separator + "allstrings.xml"), true, false));
		assertEquals(TestUtils.readXmlToString(new File(expDir, "plugins" + File.separator + "dummy" + File.separator + "xsl" + File.separator + "shell.xsl"), true, false),
					 TestUtils.readXmlToString(new File(tempDir, "plugins" + File.separator + "dummy" + File.separator + "xsl" + File.separator + "shell.xsl"), true, false));

	}
	
	private Properties getProperties(final File f) throws IOException {
		final Properties p = new Properties();
		InputStream in = null;
		try {
			in = new FileInputStream(f);
			p.load(in);
		} finally {
			if (in != null) {
				in.close();
			}
		}
		return p;
	}
	
	@After
	public void tearDown() throws IOException {
		TestUtils.forceDelete(tempDir);
	}
	
}
