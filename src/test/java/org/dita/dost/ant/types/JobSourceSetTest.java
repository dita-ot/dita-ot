/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2019 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.ant.types;

import com.google.common.io.Files;
import org.apache.tools.ant.types.Resource;
import org.dita.dost.TestUtils;
import org.dita.dost.ant.types.JobSourceSet.SelectorElem;
import org.dita.dost.store.StreamStore;
import org.dita.dost.util.Job;
import org.dita.dost.util.XMLUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class JobSourceSetTest {

    private static File tempDir;
    private static Job job;
    private JobSourceSet jobSourceSet;

    @BeforeClass
    public static void setUpClass() throws Exception {
        tempDir = TestUtils.createTempDir(JobSourceSetTest.class);
        job = new Job(tempDir, new StreamStore(tempDir, new XMLUtils()));
        for (String ext : Arrays.asList("dita", "ditamap", "jpg", "html")) {
            final String file = "a." + ext;
            Files.touch(new File(tempDir, file));
            job.add(new Job.FileInfo.Builder()
                    .src(URI.create("file:///" + file))
                    .uri(URI.create(file))
                    .format(ext)
                    .build());
        }

    }

    @Before
    public void setUp() throws Exception {
        jobSourceSet = new JobSourceSet() {
            @Override
            Job getJob() {
                return job;
            }
        };
    }

    @AfterClass
    public static void tearDown() throws IOException {
        TestUtils.forceDelete(tempDir);
    }

    @Test
    public void testEmpty() {
        final List<Resource> act = jobSourceSet.stream().collect(Collectors.toList());
        assertEquals(4, act.size());
    }

    @Test
    public void testFormatAttribute() {
        jobSourceSet.setFormat("dita");
        final List<Resource> act = jobSourceSet.stream().collect(Collectors.toList());
        assertEquals(1, act.size());
    }

    @Test
    public void testFilterEmpty() {
        assertTrue(jobSourceSet.filter(job.getFileInfo(URI.create("a.dita"))));
    }

    @Test
    public void testFilterIncludesFormat() {
        jobSourceSet.addConfiguredIncludes(
                new SelectorElem(new HashSet<>(Arrays.asList("dita")), null, null, null, null)
        );
        assertTrue(jobSourceSet.filter(job.getFileInfo(URI.create("a.dita"))));
        assertFalse(jobSourceSet.filter(job.getFileInfo(URI.create("a.ditamap"))));
        assertFalse(jobSourceSet.filter(job.getFileInfo(URI.create("a.html"))));
    }

    @Test
    public void testFilterIncludesMultipleFormat() {
        jobSourceSet.addConfiguredIncludes(
                new SelectorElem(new HashSet<>(Arrays.asList("dita")), null, null, null, null)
        );
        jobSourceSet.addConfiguredIncludes(
                new SelectorElem(new HashSet<>(Arrays.asList("ditamap")), null, null, null, null)
        );
        assertTrue(jobSourceSet.filter(job.getFileInfo(URI.create("a.dita"))));
        assertTrue(jobSourceSet.filter(job.getFileInfo(URI.create("a.ditamap"))));
        assertFalse(jobSourceSet.filter(job.getFileInfo(URI.create("a.html"))));
    }

    @Test
    public void testFilterExcludesFormat() {
        jobSourceSet.addConfiguredExcludes(
                new SelectorElem(new HashSet<>(Arrays.asList("dita")), null, null, null, null)
        );
        assertFalse(jobSourceSet.filter(job.getFileInfo(URI.create("a.dita"))));
        assertTrue(jobSourceSet.filter(job.getFileInfo(URI.create("a.ditamap"))));
        assertTrue(jobSourceSet.filter(job.getFileInfo(URI.create("a.html"))));
    }

    @Test
    public void testFilterExcludesMultipleFormat() {
        jobSourceSet.addConfiguredExcludes(
                new SelectorElem(new HashSet<>(Arrays.asList("dita")), null, null, null, null)
        );
        jobSourceSet.addConfiguredExcludes(
                new SelectorElem(new HashSet<>(Arrays.asList("ditamap")), null, null, null, null)
        );
        assertFalse(jobSourceSet.filter(job.getFileInfo(URI.create("a.dita"))));
        assertFalse(jobSourceSet.filter(job.getFileInfo(URI.create("a.ditamap"))));
        assertTrue(jobSourceSet.filter(job.getFileInfo(URI.create("a.html"))));
    }
}