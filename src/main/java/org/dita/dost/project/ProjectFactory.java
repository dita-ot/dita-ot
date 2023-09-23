/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2019 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.project;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;
import org.dita.dost.project.Project.Context;
import org.dita.dost.project.Project.Deliverable;
import org.dita.dost.project.Project.ProjectRef;
import org.dita.dost.project.Project.Publication;
import org.dita.dost.util.FileUtils;
import org.slf4j.Logger;
import org.xml.sax.SAXParseException;

public class ProjectFactory {

  private static final ObjectReader jsonReader = new ObjectMapper()
    .reader()
    .forType(ProjectBuilder.class)
    .with(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
  private static final ObjectReader yamlReader = new YAMLMapper()
    .reader()
    .forType(ProjectBuilder.class)
    .with(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
  private final XmlReader xmlReader = new XmlReader();

  private Logger logger;
  private boolean lax;

  public static ProjectFactory getInstance() {
    return new ProjectFactory();
  }

  public Project load(final URI file) throws IOException {
    try {
      return resolveReferences(load(file, Collections.emptySet()));
    } catch (SAXParseException e) {
      if (e.getLineNumber() != -1) {
        throw new IOException(
          String.format(
            "Failed to read project file %s:%s:%s: %s",
            file,
            e.getLineNumber(),
            e.getColumnNumber(),
            e.getLocalizedMessage()
          ),
          e
        );
      } else {
        throw new IOException(String.format("Failed to read project file %s: %s", file, e.getLocalizedMessage()), e);
      }
    } catch (IOException e) {
      throw new IOException("Failed to read project file: " + e.getMessage(), e);
    }
  }

  @VisibleForTesting
  static Project resolveReferences(Project src) {
    return new Project(
      src.deliverables() == null
        ? Collections.emptyList()
        : src
          .deliverables()
          .stream()
          .map(deliverable -> {
            final Context context = Optional
              .ofNullable(deliverable.context())
              .flatMap(cntx -> Optional.ofNullable(cntx.idref()))
              .map(idref -> {
                final Context pub = src
                  .contexts()
                  .stream()
                  .filter(cntx -> Objects.equals(cntx.id(), deliverable.context().idref()))
                  .findAny()
                  .orElseThrow(() ->
                    new RuntimeException(String.format("Context not found: %s", deliverable.context().idref()))
                  );
                return pub;
              })
              .orElse(deliverable.context());
            final Publication publication = Optional
              .ofNullable(deliverable.publication())
              .flatMap(publ -> Optional.ofNullable(publ.idref()))
              .map(idref -> {
                final Publication pub = src
                  .publications()
                  .stream()
                  .filter(publ -> Objects.equals(publ.id(), deliverable.publication().idref()))
                  .findAny()
                  .orElseThrow(() ->
                    new RuntimeException(String.format("Publication not found: %s", deliverable.publication().idref()))
                  );
                return merge(pub, deliverable.publication());
              })
              .orElse(deliverable.publication());
            return new Deliverable(deliverable.name(), deliverable.id(), context, deliverable.output(), publication);
          })
          .collect(Collectors.toList()),
      src.includes(),
      src.publications(),
      src.contexts()
    );
  }

  private static Publication merge(Publication base, Publication extend) {
    final Map<String, Publication.Param> params = new HashMap<>();
    if (base.params() != null) {
      for (Publication.Param param : base.params()) {
        params.put(param.name(), param);
      }
    }
    if (extend.params() != null) {
      for (Publication.Param param : extend.params()) {
        params.put(param.name(), param);
      }
    }
    return new Publication(
      base.name(),
      base.id(),
      base.idref(),
      base.transtype(),
      new ArrayList<>(params.values()),
      base.profiles()
    );
  }

  private Project load(final URI file, final Set<URI> processed) throws IOException, SAXParseException {
    if (processed.contains(file)) {
      throw new RuntimeException("Recursive project file import: " + file);
    }
    final ProjectBuilder builder;
    switch (FileUtils.getExtension(file.getPath()).toLowerCase()) {
      case "xml" -> {
        xmlReader.setLogger(logger);
        xmlReader.setLax(lax);
        builder = xmlReader.read(file);
      }
      case "json" -> builder = jsonReader.readValue(file.toURL());
      case "yaml" -> builder = yamlReader.readValue(file.toURL());
      default -> throw new RuntimeException("Unrecognized project file format: " + file);
    }
    final Project project = Project.build(builder, file);

    return resolveIncludes(project, file, ImmutableSet.<URI>builder().addAll(processed).add(file).build());
  }

  private Project resolveIncludes(final Project project, final URI base, final Set<URI> processed)
    throws IOException, SAXParseException {
    if (project.includes() == null || project.includes().isEmpty()) {
      return project;
    }
    final List<Deliverable> deliverables = project.deliverables() != null
      ? new ArrayList<>(project.deliverables())
      : new ArrayList<>();
    final List<Publication> publications = project.publications() != null
      ? new ArrayList<>(project.publications())
      : new ArrayList<>();
    final List<Context> contexts = project.contexts() != null ? new ArrayList<>(project.contexts()) : new ArrayList<>();
    if (project.includes() != null) {
      for (final ProjectRef projectRef : project.includes()) {
        final URI href = projectRef.href().isAbsolute() ? projectRef.href() : base.resolve(projectRef.href());
        final Project ref = load(href, processed);
        if (ref.deliverables() != null) {
          deliverables.addAll(ref.deliverables());
        }
        if (ref.publications() != null) {
          publications.addAll(ref.publications());
        }
        if (ref.contexts() != null) {
          contexts.addAll(ref.contexts());
        }
      }
    }
    return new Project(deliverables, project.includes(), publications, contexts);
  }

  public void setLogger(Logger logger) {
    this.logger = logger;
  }

  public void setLax(boolean lax) {
    this.lax = lax;
  }
}
