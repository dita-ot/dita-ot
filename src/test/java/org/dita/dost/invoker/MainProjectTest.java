/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2022 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.invoker;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.dita.dost.project.Project;
import org.dita.dost.project.ProjectFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MainProjectTest {

  private ProjectFactory factory;
  private Project project;
  private URI projectFile;
  private Main main;

  @BeforeEach
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

    final Map<String, Object> exp = Map.of(
      "project.deliverable",
      "site",
      "output.dir",
      Paths.get("out", "site").toAbsolutePath().toString(),
      "args.input",
      baseDir.resolve("site.ditamap").toString(),
      "transtype",
      "html5",
      "args.empty",
      "",
      "args.rellinks",
      "noparent",
      "args.path",
      Paths.get(baseDir).resolve("baz qux").toAbsolutePath().toString(),
      "args.filter",
      Paths.get(baseDir).resolve("site.ditaval").toAbsolutePath().toString(),
      "args.gen.task.lbl",
      "YES",
      "args.uri",
      baseDir.resolve("foo%20bar").toString()
    );
    assertEquals(singletonList(exp), act);
  }

  @Test
  public void collectProperties_profiles() throws IOException, URISyntaxException {
    projectFile = getClass().getClassLoader().getResource("org/dita/dost/project/profiles.xml").toURI();
    project = factory.load(projectFile);
    final URI baseDir = projectFile.resolve("");

    final List<Map<String, Object>> act = main.collectProperties(project, projectFile, emptyMap());

    final Map<String, Object> exp = Map.of(
      "project.deliverable",
      "site",
      "output.dir",
      Paths.get("out", "site").toAbsolutePath().toString(),
      "args.input",
      baseDir.resolve("site.ditamap").toString(),
      "transtype",
      "html5",
      "args.filter",
      Stream
        .of("site-html5.ditaval", "site.ditaval")
        .map(ditaval -> Paths.get(baseDir).resolve(ditaval).toAbsolutePath().toString())
        .collect(Collectors.joining(File.pathSeparator))
    );
    assertEquals(singletonList(exp), act);
  }
}
