/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2016 Jarno Elovirta
 *
 *  See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.module;

import org.dita.dost.TestUtils.TestLogger;
import org.dita.dost.module.KeyrefModule.ResolveTask;
import org.dita.dost.util.Job;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.dita.dost.TestUtils.createTempDir;
import static org.junit.Assert.*;

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
        final List<ResolveTask> src = new ArrayList<>();
        final List<ResolveTask> act = module.adjustResourceRenames(src);

        final List<ResolveTask> exp = new ArrayList<>();

        assertEquals(exp, act);
    }

}