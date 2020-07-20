/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2017 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.writer;

import org.dita.dost.store.StreamStore;
import org.dita.dost.util.Job;
import org.dita.dost.util.XMLUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import static org.junit.Assert.assertEquals;

public class LinkFilterTest {

    private URI tempDir;
    private URI srcDir;

    final LinkFilter linkFilter = new LinkFilter();

    @Before
    public void setUp() throws Exception {
        tempDir = new File(System.getProperty("java.io.tmpdir")).toURI().resolve("./");
        srcDir = new File("").toURI().resolve("./");
    }

    @Test
    public void getHrefFragment() throws Exception {
        assertEquals(URI.create("#foo"), linkFilter.getHref(URI.create("#foo")));
    }

    @Test
    public void getHref() {
        final Job job = getJob();
        job.setInputDir(srcDir.resolve("maps"));
        linkFilter.setJob(job);
        linkFilter.setDestFile(tempDir.resolve("maps/test.ditamap"));
        linkFilter.setCurrentFile(tempDir.resolve("xyz.ditamap"));
        assertEquals(URI.create("../topics/topic.dita"), linkFilter.getHref(URI.create("abc.dita")));
    }

    private Job getJob() {
        try {
            final Job job = new Job(new File(tempDir), new StreamStore(new File(tempDir), new XMLUtils()));
            job.add(new Job.FileInfo.Builder()
                    .uri(URI.create("abc.dita"))
                    .src(srcDir.resolve("topics/topic.dita"))
                    .result(srcDir.resolve("topics/topic.dita"))
                    .build());
            job.add(new Job.FileInfo.Builder()
                    .uri(URI.create("xyz.ditamap"))
                    .src(srcDir.resolve("maps/test.ditamap"))
                    .result(srcDir.resolve("maps/test.ditamap"))
                    .build());
            return job;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}