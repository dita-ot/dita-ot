/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2020 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.ant;

import org.apache.tools.ant.Project;
import org.dita.dost.store.StreamStore;
import org.dita.dost.util.Job;
import org.dita.dost.util.XMLUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ExtensibleAntInvokerTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private Project project;
    private File tempDir;

    @Before
    public void setUp() throws IOException {
        tempDir = folder.newFile();
        project = new Project();
        project.setUserProperty("dita.temp.dir", tempDir.getAbsolutePath());
    }

    @Test
    public void getJob_witJobReference() throws IOException {
        final Job job = new Job(tempDir, new StreamStore(tempDir, new XMLUtils()));
        project.addReference("job", job);
        final Job act = ExtensibleAntInvoker.getJob(project);
        assertNotNull(act);
        assertEquals(job, act);
    }

    @Test
    public void getJob_withoutJobReference() {
        final Job act = ExtensibleAntInvoker.getJob(project);
        assertNotNull(act);
    }
}