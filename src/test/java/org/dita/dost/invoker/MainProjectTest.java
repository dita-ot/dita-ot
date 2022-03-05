/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2022 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.invoker;

import com.google.common.collect.ImmutableMap;
import org.dita.dost.project.Project;
import org.dita.dost.project.ProjectFactory;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

public class MainProjectTest {

    private ProjectFactory factory;
    private Project project;
    private URI projectFile;
    private Main main;

    @Before
    public void setUp() {
        main = new Main();
        factory = ProjectFactory.getInstance();
        factory.setLax(true);
    }

    @Test
    public void collectProperties_simple() throws IOException, URISyntaxException {
        projectFile = getClass().getClassLoader().getResource("org/dita/dost/project/simple.xml").toURI();
        project = factory.load(projectFile);
        final URI baseDir = projectFile.resolve("");

        final List<Map<String, Object>> act = main.collectProperties(project, projectFile, emptyMap());

        final Map<String, Object> exp = ImmutableMap.<String, Object>builder()
                .put("project.deliverable", "site")
                .put("output.dir", Paths.get("out", "site").toAbsolutePath().toString())
                .put("args.input", baseDir.resolve("site.ditamap").toString())
                .put("transtype", "html5")
                .put("args.empty", "")
                .put("args.rellinks", "noparent")
                .put("args.path", Paths.get(baseDir).resolve("baz qux").toAbsolutePath().toString())
                .put("args.filter", Paths.get(baseDir).resolve("site.ditaval").toAbsolutePath().toString())
                .put("args.gen.task.lbl", "YES")
                .put("args.uri", baseDir.resolve("foo%20bar").toString())
                .build();
        assertEquals(singletonList(exp), act);
    }

    @Test
    public void collectProperties_profiles() throws IOException, URISyntaxException {
        projectFile = getClass().getClassLoader().getResource("org/dita/dost/project/profiles.xml").toURI();
        project = factory.load(projectFile);
        final URI baseDir = projectFile.resolve("");

        final List<Map<String, Object>> act = main.collectProperties(project, projectFile, emptyMap());

        final Map<String, Object> exp = ImmutableMap.<String, Object>builder()
                .put("project.deliverable", "site")
                .put("output.dir", Paths.get("out", "site").toAbsolutePath().toString())
                .put("args.input", baseDir.resolve("site.ditamap").toString())
                .put("transtype", "html5")
                .put("args.filter", Stream.of("site-html5.ditaval", "site.ditaval")
                        .map(ditaval -> Paths.get(baseDir).resolve(ditaval).toAbsolutePath().toString())
                        .collect(Collectors.joining(File.pathSeparator)))
                .build();
        assertEquals(singletonList(exp), act);
    }
}
