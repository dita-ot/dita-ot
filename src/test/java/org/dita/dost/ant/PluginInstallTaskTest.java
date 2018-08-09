/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2018 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.ant;

import org.dita.dost.platform.Registry;
import org.dita.dost.platform.Registry.Dependency;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PluginInstallTaskTest {

    final PluginInstallTask registryTask = new PluginInstallTask();

    @Test
    public void matchingPlatformVersion() {
        assertFalse(registryTask.matchingPlatformVersion(createRegistry(">=2.5")));
        assertTrue(registryTask.matchingPlatformVersion(createRegistry(">=1.2")));
        assertTrue(registryTask.matchingPlatformVersion(createRegistry("1.2.3")));
    }

    private Registry createRegistry(String version) {
        return new Registry(null, "1.0.0",
                new Dependency[]{
                        new Dependency("org.dita.base", version)
                },
                null, null);
    }
}