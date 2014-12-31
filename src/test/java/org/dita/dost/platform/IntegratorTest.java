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
import java.util.Arrays;
import java.util.HashSet;
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
    public void testVersionPattern() {
        assertTrue(Integrator.VERSION_PATTERN.matcher("0").matches());
        assertTrue(Integrator.VERSION_PATTERN.matcher("1").matches());
        assertTrue(Integrator.VERSION_PATTERN.matcher("1.0").matches());
        assertTrue(Integrator.VERSION_PATTERN.matcher("1.0.0").matches());
        assertTrue(Integrator.VERSION_PATTERN.matcher("1.0.0.abc123").matches());
        assertTrue(Integrator.VERSION_PATTERN.matcher("012.012.012.ABCabc123-_").matches());
        assertFalse(Integrator.VERSION_PATTERN.matcher("").matches());
        assertFalse(Integrator.VERSION_PATTERN.matcher(" 1").matches());
        assertFalse(Integrator.VERSION_PATTERN.matcher("A").matches());
    }

    @Test
    public void testIdPattern() {
        assertTrue(Integrator.ID_PATTERN.matcher("foo").matches());
        assertTrue(Integrator.ID_PATTERN.matcher("1foo.2-_.bar").matches());
        assertFalse(Integrator.ID_PATTERN.matcher("").matches());
        assertFalse(Integrator.ID_PATTERN.matcher(" foo ").matches());
        assertFalse(Integrator.ID_PATTERN.matcher(".foo").matches());
    }

    @Test
    public void testExecute() throws Exception {
        final File libDir = new File(tempDir, "lib");
        if (!libDir.exists() && !libDir.mkdirs()) {
            throw new IOException("Failed to create directory " + libDir);
        }
        final File resourcesDir = new File(tempDir, "resources");
        if (!resourcesDir.exists() && !resourcesDir.mkdirs()) {
            throw new IOException("Failed to create directory " + resourcesDir);
        }

        final Integrator i = new Integrator(tempDir);
        i.setProperties(new File(tempDir, "integrator.properties"));
        i.execute();

        final Properties expProperties = getProperties(new File(expDir, "lib" + File.separator + Integrator.class.getPackage().getName() + File.separator + Constants.GEN_CONF_PROPERTIES));
        expProperties.setProperty("plugin.base.dir", new File("plugins" + File.separator + "base").getPath());
        expProperties.setProperty("plugin.dummy.dir", new File("plugins" + File.separator + "dummy").getPath());
        final Properties actProperties = getProperties(new File(tempDir, "lib" + File.separator + Integrator.class.getPackage().getName() + File.separator + Constants.GEN_CONF_PROPERTIES));
        // supported_image_extensions needs to be tested separately
        assertEquals(new HashSet(Arrays.asList(expProperties.getProperty("supported_image_extensions").split(";"))),
                     new HashSet(Arrays.asList(expProperties.getProperty("supported_image_extensions").split(";"))));
        expProperties.remove("supported_image_extensions");
        actProperties.remove("supported_image_extensions");
        assertEquals(expProperties, actProperties);
        TestUtils.resetXMLUnit();
        XMLUnit.setNormalizeWhitespace(true);
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreDiffBetweenTextAndCDATA(true);
        XMLUnit.setIgnoreComments(true);
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
