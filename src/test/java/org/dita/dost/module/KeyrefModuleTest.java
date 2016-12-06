/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2016 Jarno Elovirta
 *
 *  See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.module;

import com.google.common.collect.ImmutableMap;
import org.dita.dost.TestUtils.TestLogger;
import org.dita.dost.module.KeyrefModule.ResolveTask;
import org.dita.dost.util.Job;
import org.dita.dost.util.Job.FileInfo.Builder;
import org.dita.dost.util.KeyDef;
import org.dita.dost.util.KeyScope;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static java.net.URI.create;
import static java.util.Collections.emptyList;
import static org.dita.dost.TestUtils.createTempDir;
import static org.junit.Assert.assertEquals;

public class KeyrefModuleTest {

    KeyrefModule module;

    @Before
    public void setUp() throws IOException {
        final File tempDir = createTempDir(KeyrefModuleTest.class);

        module = new KeyrefModule();
        module.setJob(new Job(tempDir));
        module.setLogger(new TestLogger());
    }

    @Test
    public void testAdjustResourceRenames() {
        final List<ResolveTask> src = Collections.singletonList(new ResolveTask(
                new KeyScope("scope",
                        ImmutableMap.<String, KeyDef>builder()
                                .put("key", new KeyDef("key", create("target.dita"), null, null, null, null))
                                .build(),
                        emptyList()),
                new Builder().uri(create("target.dita")).build(),
                new Builder().uri(create("target-1.dita")).build()));
        final List<ResolveTask> act = module.adjustResourceRenames(src);

        final KeyScope exp = new KeyScope("scope",
                ImmutableMap.<String, KeyDef>builder()
                        .put("key", new KeyDef("key", create("target.dita"), null, null, null, null))
                        .build(),
                emptyList());

        assertEquals(exp, act.get(0).scope);
    }

}