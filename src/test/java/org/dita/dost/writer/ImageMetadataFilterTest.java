/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */
package org.dita.dost.writer;

import static org.apache.commons.io.FileUtils.*;
import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;

import java.io.File;
import java.io.IOException;

import org.custommonkey.xmlunit.XMLUnit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.dita.dost.TestUtils;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.util.Job;

public class ImageMetadataFilterTest {

    private static final File resourceDir = TestUtils.getResourceDir(ImageMetadataFilterTest.class);
    private static final File srcDir = new File(resourceDir, "src");
    private static final File expDir = new File(resourceDir, "exp");
    private static File tempDir;

    @BeforeClass
    public static void setup() throws IOException {
        tempDir = TestUtils.createTempDir(ImageMetadataFilterTest.class);
    }

    @Test
    public void testWrite() throws DITAOTException, SAXException, IOException {
        final File f = new File(tempDir, "test.dita");
        copyFile(new File(srcDir, "test.dita"), f);

        final Job job = new Job(tempDir);
        job.setProperty("uplevels", "");
        final ImageMetadataFilter filter = new ImageMetadataFilter(srcDir, job);
        filter.setLogger(new TestUtils.TestLogger());
        filter.setJob(job);
        filter.write(f.getAbsoluteFile());

        TestUtils.resetXMLUnit();
        XMLUnit.setIgnoreWhitespace(true);
        assertXMLEqual(new InputSource(new File(expDir, "test.dita").toURI().toString()),
                new InputSource(f.toURI().toString()));
    }
    
    @Test
    public void testUplevelsWrite() throws DITAOTException, SAXException, IOException {
        final File f = new File(tempDir, "sub" + File.separator + "test.dita");
        f.getParentFile().mkdirs();
        copyFile(new File(srcDir, "test.dita"), f);

        final Job job = new Job(tempDir);
        job.setProperty("uplevels", ".." + File.separator);
        final ImageMetadataFilter filter = new ImageMetadataFilter(srcDir, job);
        filter.setLogger(new TestUtils.TestLogger());
        filter.setJob(job);
        filter.write(f.getAbsoluteFile());

        TestUtils.resetXMLUnit();
        XMLUnit.setIgnoreWhitespace(true);
        assertXMLEqual(new InputSource(new File(expDir, "test.dita").toURI().toString()),
                new InputSource(f.toURI().toString()));
    }

    @AfterClass
    public static void teardown() throws IOException {
        TestUtils.forceDelete(tempDir);
    }

}
