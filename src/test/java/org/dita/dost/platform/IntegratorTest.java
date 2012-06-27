/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */
package org.dita.dost.platform;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.dita.dost.util.Constants.GEN_CONF_PROPERTIES;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.custommonkey.xmlunit.XMLUnit;

import org.xml.sax.InputSource;

import org.dita.dost.TestUtils;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.util.Constants;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class IntegratorTest {

    final File resourceDir = TestUtils.getResourceDir(IntegratorTest.class);
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

        assertEquals(getProperties(new File(expDir, "lib" + File.separator + Integrator.class.getPackage().getName() + File.separator + Constants.GEN_CONF_PROPERTIES)),
                getProperties(new File(tempDir, "lib" + File.separator + Integrator.class.getPackage().getName() + File.separator + Constants.GEN_CONF_PROPERTIES)));
        TestUtils.resetXMLUnit();
        XMLUnit.setNormalizeWhitespace(true);
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreDiffBetweenTextAndCDATA(true);
        assertXMLEqual(new InputSource(new File(expDir, "build.xml").toURI().toString()),
                new InputSource(new File(tempDir, "build.xml").toURI().toString()));
        assertXMLEqual(new InputSource(new File(expDir, "catalog.xml").toURI().toString()),
                new InputSource(new File(tempDir, "catalog.xml").toURI().toString()));
        assertXMLEqual(new InputSource(new File(expDir, "xsl" + File.separator + "shell.xsl").toURI().toString()),
                new InputSource(new File(tempDir, "xsl" + File.separator + "shell.xsl").toURI().toString()));
        assertXMLEqual(new InputSource(new File(expDir, "xsl" + File.separator + "common" + File.separator + "allstrings.xml").toURI().toString()),
                new InputSource(new File(tempDir, "xsl" + File.separator + "common" + File.separator + "allstrings.xml").toURI().toString()));
        assertXMLEqual(new InputSource(new File(expDir, "plugins" + File.separator + "dummy" + File.separator + "xsl" + File.separator + "shell.xsl").toURI().toString()),
                new InputSource(new File(tempDir, "plugins" + File.separator + "dummy" + File.separator + "xsl" + File.separator + "shell.xsl").toURI().toString()));

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
