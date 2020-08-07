/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2011 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.writer;

import static org.apache.commons.io.FileUtils.*;
import static org.dita.dost.TestUtils.assertXMLEqual;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Collectors;

import org.dita.dost.store.StreamStore;
import org.dita.dost.util.XMLUtils;
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

        final Job job = new Job(tempDir, new StreamStore(tempDir, new XMLUtils()));
        job.setProperty("uplevels", "");
        final ImageMetadataFilter filter = new ImageMetadataFilter(srcDir, job);
        filter.setLogger(new TestUtils.TestLogger());
        filter.setJob(job);
        filter.write(f.getAbsoluteFile());

        assertXMLEqual(new InputSource(new File(expDir, "test.dita").toURI().toString()),
                new InputSource(f.toURI().toString()));
        assertEquals(Arrays.asList("img.png", "img.gif", "img.jpg", "img.xxx").stream()
                        .map(img -> new File(srcDir, img).toURI())
                        .collect(Collectors.toSet()),
                new HashSet(filter.getImages()));
    }

    @Test
    public void testUplevelsWrite() throws DITAOTException, SAXException, IOException {
        final File f = new File(tempDir, "sub" + File.separator + "test.dita");
        f.getParentFile().mkdirs();
        copyFile(new File(srcDir, "test.dita"), f);

        final Job job = new Job(tempDir, new StreamStore(tempDir, new XMLUtils()));
        job.setProperty("uplevels", ".." + File.separator);
        final ImageMetadataFilter filter = new ImageMetadataFilter(srcDir, job);
        filter.setLogger(new TestUtils.TestLogger());
        filter.setJob(job);
        filter.write(f.getAbsoluteFile());

        assertXMLEqual(new InputSource(new File(expDir, "test.dita").toURI().toString()),
                new InputSource(f.toURI().toString()));
        assertEquals(Arrays.asList("img.png", "img.gif", "img.jpg", "img.xxx").stream()
                        .map(img -> new File(srcDir, img).toURI())
                        .collect(Collectors.toSet()),
                new HashSet(filter.getImages()));
    }

    @AfterClass
    public static void teardown() throws IOException {
        TestUtils.forceDelete(tempDir);
    }

}
