/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2020 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.invoker;

import org.dita.dost.project.Project.Deliverable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.dita.dost.invoker.Main.ANT_OUTPUT_DIR;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class MainTest {

    @Parameters(name = "{1}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"", "Without trailing slash"},
                {"/", "With trailing slash"},
        });
    }

    private final String suffix;

    private Main main;
    private Path current;

    public MainTest(final String suffix, final String _desc) {
        this.suffix = suffix;
    }

    @Before
    public void setUp() throws Exception {
        this.main = new Main();
        this.current = new File("").getAbsoluteFile().toPath();
    }

    @Test
    public void withoutEither() {
        assertEquals(current.resolve("out"), getOutputDir(null, null));
    }

    @Test
    public void relativeArgument() {
        assertEquals(current.resolve("baz/qux"), getOutputDir(null, "baz/qux"));
    }

    @Test
    public void absoluteArgument() {
        assertEquals(Paths.get("/baz/qux"), getOutputDir(null, "/baz/qux"));
    }

    @Test
    public void relativeProject() {
        assertEquals(current.resolve("out/foo/bar"), getOutputDir("foo/bar", null));
    }

    @Test
    public void absoluteProject() {
        assertEquals(Paths.get("/foo/bar"), getOutputDir("/foo/bar", null));
    }

    @Test
    public void relativeArgument_relativeProject() {
        assertEquals(current.resolve("baz/qux/foo/bar"), getOutputDir("foo/bar", "baz/qux"));
    }

    @Test
    public void absoluteArgument_relativeProject() {
        assertEquals(Paths.get("/baz/qux/foo/bar"), getOutputDir("foo/bar", "/baz/qux"));
    }

    @Test
    public void relativeArgument_absoluteProject() {
        assertEquals(Paths.get("/foo/bar"), getOutputDir("/foo/bar", "baz/qux"));
    }

    @Test
    public void absoluteArgument_absoluteProject() {
        assertEquals(Paths.get("/foo/bar"), getOutputDir("/foo/bar", "/baz/qux"));
    }

    private Path getOutputDir(final String output, final String arg) {
        final Deliverable deliverable = new Deliverable(
                null, null, null,
                output != null ? URI.create(output + suffix) : null,
                null
        );
        final Map<String, Object> props = new HashMap<>();
        if (arg != null) {
            props.put(ANT_OUTPUT_DIR, arg + suffix);
        }
        return main.getOutputDir(deliverable, props);
    }
}