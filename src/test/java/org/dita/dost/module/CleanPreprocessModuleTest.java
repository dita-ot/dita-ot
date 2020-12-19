/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2017 Jarno Elovirta
 *
 *  See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.module;

import org.dita.dost.store.StreamStore;
import org.dita.dost.util.Job;
import org.dita.dost.util.Job.FileInfo.Builder;
import org.dita.dost.util.XMLUtils;
import org.junit.Test;

import java.io.File;
import java.net.URI;

import static java.net.URI.create;
import static org.junit.Assert.assertEquals;

public class CleanPreprocessModuleTest {

    private final CleanPreprocessModule module = new CleanPreprocessModule();

    @Test
    public void getCommonBase() {
        assertEquals(create("file:/foo/bar/"), module.getCommonBase(create("file:/foo/bar/a"), create("file:/foo/bar/b")));
        assertEquals(create("file:/foo/"), module.getCommonBase(create("file:/foo/a"), create("file:/foo/bar/b")));
        assertEquals(create("file:/foo/"), module.getCommonBase(create("file:/foo/bar/a"), create("file:/foo/b")));
        assertEquals(create("file:/foo/"), module.getCommonBase(create("file:/foo/bar/a"), create("file:/foo/baz/b")));
        assertEquals(null, module.getCommonBase(create("file:/foo/bar/a"), create("https://example.com/baz/b")));
    }

    @Test
    public void getBaseDir() throws Exception {
        final File tempDir = new File("").getAbsoluteFile();
        final Job job = new Job(tempDir, new StreamStore(tempDir, new XMLUtils()));
        job.setInputDir(URI.create("file:/foo/bar/"));
        job.add(new Builder()
                .uri(create("map.ditamap"))
                .isInput(true)
                .result(create("file:/foo/bar/map.ditamap"))
                .build());
        job.add(new Builder()
                .uri(create("topics/topic.dita"))
                .result(create("file:/foo/bar/topics/topic.dita"))
                .build());
        job.add(new Builder()
                .uri(create("topics/null.dita"))
                .build());
        job.add(new Builder()
                .uri(create("topics/task.dita"))
                .result(create("file:/foo/bar/topics/task.dita"))
                .build());
        job.add(new Builder()
                .uri(create("common/topic.dita"))
                .result(create("file:/foo/bar/common/topic.dita"))
                .build());
        module.setJob(job);
        assertEquals(create("file:/foo/bar/"), module.getBaseDir());
    }

    @Test
    public void getBaseDirExternal() throws Exception {
        final File tempDir = new File("").getAbsoluteFile();
        final Job job = new Job(tempDir, new StreamStore(tempDir, new XMLUtils()));
        job.setInputDir(URI.create("file:/foo/bar/"));
        job.add(new Builder()
                .uri(create("map.ditamap"))
                .isInput(true)
                .result(create("file:/foo/bar/map.ditamap"))
                .build());
        job.add(new Builder()
                .uri(create("topics/topic.dita"))
                .result(create("https://example.com/topics/bar/topics/topic.dita"))
                .build());
        module.setJob(job);
        assertEquals(create("file:/foo/bar/"), module.getBaseDir());
    }

    @Test
    public void getBaseDirSubdir() throws Exception {
        final File tempDir = new File("").getAbsoluteFile();
        final Job job = new Job(tempDir, new StreamStore(tempDir, new XMLUtils()));
        job.setInputDir(URI.create("file:/foo/bar/maps/"));
        job.add(new Builder()
                .uri(create("maps/map.ditamap"))
                .isInput(true)
                .result(create("file:/foo/bar/maps/map.ditamap"))
                .build());
        job.add(new Builder()
                .uri(create("topics/topic.dita"))
                .result(create("file:/foo/bar/topics/topic.dita"))
                .build());
        module.setJob(job);
        assertEquals(create("file:/foo/bar/"), module.getBaseDir());
    }

    @Test
    public void getBaseDirSupdir() throws Exception {
        final File tempDir = new File("").getAbsoluteFile();
        final Job job = new Job(tempDir, new StreamStore(tempDir, new XMLUtils()));
        job.setInputDir(URI.create("file:/foo/bar/maps/"));
        job.add(new Builder()
                .uri(create("maps/map.ditamap"))
                .isInput(true)
                .result(create("file:/foo/bar/maps/map.ditamap"))
                .build());
        job.add(new Builder()
                .uri(create("topics/topic.dita"))
                .result(create("file:/foo/bar/topic.dita"))
                .build());
        module.setJob(job);
        assertEquals(create("file:/foo/bar/"), module.getBaseDir());
    }

}