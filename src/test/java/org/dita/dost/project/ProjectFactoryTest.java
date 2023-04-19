/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2019 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.project;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.dita.dost.project.Project.Context;
import org.dita.dost.project.Project.Deliverable;
import org.dita.dost.project.Project.Publication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

public class ProjectFactoryTest {

  private ProjectFactory factory;

  @BeforeEach
  public void setUp() {
    factory = ProjectFactory.getInstance();
  }

  @Test
  public void resolveReferences_deliverable() {
    final Project src = new Project(
      singletonList(new Deliverable(null, null, null, null, new Publication(null, null, "id", null, null, null))),
      null,
      singletonList(new Publication(null, "id", null, null, null, null)),
      null
    );
    ProjectFactory.resolveReferences(src);
  }

  @Test
  public void resolveReferences_publicationRefWithParam() {
    final Project src = new Project(
      singletonList(
        new Deliverable(
          null,
          null,
          null,
          null,
          new Publication(
            null,
            null,
            "id",
            null,
            List.of(new Publication.Param("different", "override", null, null)),
            null
          )
        )
      ),
      null,
      singletonList(
        new Publication(
          null,
          "id",
          null,
          null,
          Arrays.asList(
            new Publication.Param("different", "base", null, null),
            new Publication.Param("same", "base", null, null)
          ),
          null
        )
      ),
      null
    );
    final Project act = ProjectFactory.resolveReferences(src);

    final List<Publication.Param> params = new ArrayList<>(act.deliverables().get(0).publication().params());
    params.sort(Comparator.comparing(Publication.Param::name));
    assertEquals(2, params.size());
    assertEquals("different", params.get(0).name());
    assertEquals("override", params.get(0).value());
    assertEquals("same", params.get(1).name());
    assertEquals("base", params.get(1).value());
  }

  @Test
  public void resolveReferences_context() {
    final Project src = new Project(
      singletonList(new Deliverable(null, null, new Context(null, null, "id", null, null), null, null)),
      null,
      null,
      singletonList(new Context(null, "id", null, null, null))
    );
    ProjectFactory.resolveReferences(src);
  }

  @Test
  public void resolveReferences_notFound() {
    assertThrows(
      RuntimeException.class,
      () -> {
        final Project src = new Project(
          singletonList(
            new Deliverable(null, null, null, null, new Publication(null, null, "missing", null, null, null))
          ),
          null,
          Collections.emptyList(),
          null
        );
        final Project act = ProjectFactory.resolveReferences(src);
        assertEquals("id", act.deliverables().get(0).publication().id());
      }
    );
  }

  @Test
  public void read() throws IOException, URISyntaxException, SAXException {
    final URI file = getClass().getClassLoader().getResource("org/dita/dost/project/simple.json").toURI();
    final Project project = factory.load(file);
    assertEquals(1, project.deliverables().size());
    assertTrue(project.deliverables().get(0).context().inputs().inputs().get(0).href().isAbsolute());
    assertTrue(project.includes().isEmpty());
  }

  @Test
  public void read_product() throws IOException, URISyntaxException {
    for (String extension : new String[] { "xml", "yaml", "json" }) {
      final String path = String.format("org/dita/dost/project/product.%s", extension);
      final URI file = getClass().getClassLoader().getResource(path).toURI();
      final Project project = factory.load(file);
      assertEquals(1, project.deliverables().size());
      assertTrue(project.deliverables().get(0).context().inputs().inputs().get(0).href().isAbsolute());
      assertEquals(3, project.deliverables().get(0).publication().params().size());
      final Map<String, Project.Publication.Param> params = project
        .deliverables()
        .get(0)
        .publication()
        .params()
        .stream()
        .collect(Collectors.toMap(Publication.Param::name, Function.identity()));
      assertEquals("NO", params.get("args.gen.task.lbl").value());
    }
  }

  @Test
  public void readMultiple() throws IOException, URISyntaxException, SAXException {
    final URI file = getClass().getClassLoader().getResource("org/dita/dost/project/multiple.json").toURI();
    final Project project = factory.load(file);
    assertEquals(1, project.deliverables().size());
    assertTrue(project.deliverables().get(0).context().inputs().inputs().get(0).href().isAbsolute());
    assertTrue(project.includes().isEmpty());
  }

  @Test
  public void deserializeJsonRoot() throws IOException, URISyntaxException, SAXException {
    final URI input = getClass().getClassLoader().getResource("org/dita/dost/project/root.json").toURI();
    final Project project = factory.load(input);
    assertEquals(1, project.deliverables().size());
    assertEquals(2, project.includes().size());
  }

  @Test
  public void deserializeJsonProduct() throws IOException, URISyntaxException, SAXException {
    final URI input = getClass().getClassLoader().getResource("org/dita/dost/project/product.json").toURI();
    final Project project = factory.load(input);
    assertEquals(1, project.deliverables().size());
    assertEquals(1, project.publications().size());
    assertEquals("common-sitePub2", project.deliverables().get(0).publication().id());
  }

  @Test
  public void deserializeJsonRecursive() throws IOException, URISyntaxException, SAXException {
    assertThrows(
      RuntimeException.class,
      () -> {
        final URI input = getClass().getClassLoader().getResource("org/dita/dost/project/recursive.json").toURI();
        factory.load(input);
      }
    );
  }
}
