/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2017 Jarno Elovirta
 *
 *  See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.module;

import org.dita.dost.TestUtils;
import org.dita.dost.TestUtils.TestLogger;
import org.dita.dost.store.StreamStore;
import org.dita.dost.util.Job;
import org.dita.dost.util.Job.FileInfo.Builder;
import org.dita.dost.util.XMLUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static java.net.URI.create;
import static org.junit.Assert.assertEquals;

public class CleanPreprocessModuleTest {

    private CleanPreprocessModule module;
    private XMLUtils xmlUtils;
    private Job job;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Before
    public void setUp() throws IOException {
        module = new CleanPreprocessModule();
        xmlUtils = new XMLUtils();
        final File tempDir = temporaryFolder.newFolder();
        job = new Job(tempDir, new StreamStore(tempDir, xmlUtils));
        module.setJob(job);
    }

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

        assertEquals(create("file:/foo/bar/"), module.getBaseDir());
    }

    @Test
    public void getBaseDirExternal() throws Exception {
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

        assertEquals(create("file:/foo/bar/"), module.getBaseDir());
    }

    @Test
    public void getBaseDirSubdir() throws Exception {
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

        assertEquals(create("file:/foo/bar/"), module.getBaseDir());
    }

    @Test
    public void getBaseDirSupdir() throws Exception {
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

        assertEquals(create("file:/foo/bar/"), module.getBaseDir());
    }

    @Test(expected = RuntimeException.class)
    public void RewriteRule_WhenStylesheetNotFound_ShouldThrowException() throws Exception {
        module.setJob(job);
        module.setXmlUtils(xmlUtils);
        final TestLogger logger = new TestUtils.TestLogger(false);
        module.setLogger(logger);
        final Map<String, String> input = new HashMap<>();
        input.put("result.rewrite-rule.xsl", "abc.xsl");

        module.execute(input);
    }
}