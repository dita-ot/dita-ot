/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2017 Jarno Elovirta
 *
 *  See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.module;

import org.dita.dost.TestUtils;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.PipelineHashIO;
import org.dita.dost.store.StreamStore;
import org.dita.dost.util.Job;
import org.dita.dost.util.Job.FileInfo.Builder;
import org.dita.dost.util.XMLUtils;
import org.dita.dost.writer.ImageMetadataFilterTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.stream.Collectors;

import static java.net.URI.create;
import static java.util.Arrays.asList;
import static org.apache.commons.io.FileUtils.copyFile;
import static org.dita.dost.TestUtils.assertXMLEqual;
import static org.dita.dost.util.Constants.ANT_INVOKER_EXT_PARAM_OUTPUTDIR;
import static org.junit.Assert.assertEquals;

public class ImageMetadataModuleTest {

    private static final File resourceDir = TestUtils.getResourceDir(ImageMetadataFilterTest.class);
    private static final File srcDir = new File(resourceDir, "src");
    private static final File expDir = new File(resourceDir, "exp");
    private static File tempDir;

    @BeforeClass
    public static void setup() throws IOException {
        tempDir = TestUtils.createTempDir(ImageMetadataModuleTest.class);
    }

    @Test
    public void testWrite() throws DITAOTException, SAXException, IOException {
        final File f = new File(tempDir, "test.dita");
        copyFile(new File(srcDir, "test.dita"), f);

        final Job job = new Job(tempDir, new StreamStore(tempDir, new XMLUtils()));
        job.setProperty("uplevels", "");
        job.setInputDir(srcDir.toURI());
        job.addAll(asList("img.xxx", "img.png", "img.gif", "img.jpg").stream()
                .map(p -> new Builder()
                        .uri(create(p)).src(new File(srcDir, p).toURI()).format("html")
                        .build())
                .collect(Collectors.toList()));
        job.add(new Builder()
                .uri(create("test.dita")).format("dita")
                .build());

        final ImageMetadataModule filter = new ImageMetadataModule();
        filter.setLogger(new TestUtils.TestLogger());
        filter.setJob(job);

        final AbstractPipelineInput input = new PipelineHashIO();
        input.setAttribute(ANT_INVOKER_EXT_PARAM_OUTPUTDIR, new File(tempDir, "out").getAbsolutePath());
        filter.execute(input);

        assertXMLEqual(new InputSource(new File(expDir, "test.dita").toURI().toString()),
                new InputSource(f.toURI().toString()));
        assertEquals("image", job.getFileInfo(create("img.png")).format);
        assertEquals("image", job.getFileInfo(create("img.gif")).format);
        assertEquals("image", job.getFileInfo(create("img.jpg")).format);
        assertEquals("image", job.getFileInfo(create("img.xxx")).format);
    }

    @AfterClass
    public static void teardown() throws IOException {
        TestUtils.forceDelete(tempDir);
    }

}
