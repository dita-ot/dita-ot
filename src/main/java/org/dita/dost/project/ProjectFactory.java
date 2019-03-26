/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2019 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.project;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import org.dita.dost.util.FileUtils;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

public class ProjectFactory {

    private static final ObjectReader jsonReader = new ObjectMapper().reader().forType(Project.class);
    private static final ObjectReader xmlReader = new XmlMapper().reader().forType(Project.class);

    public static Project load(final URI file) throws IOException {

        try {
            return resolveReferences(load(file, Collections.emptySet()));
        } catch (IOException e) {
            throw new IOException("Failed to read project file: " + e.getMessage(), e);
        }
    }

    @VisibleForTesting
    static Project resolveReferences(Project src) {
        return new Project(
                src.deliverables == null ? Collections.emptyList() :
                src.deliverables.stream()
                        .map(deliverable -> new Project.Deliverable(
                                deliverable.name,
                                Optional.ofNullable(deliverable.context)
                                        .flatMap(context -> Optional.ofNullable(context.idref))
                                        .map(idref -> {
                                            final Project.Deliverable.Context pub = src.contexts.stream()
                                                    .filter(context -> Objects.equals(context.id, deliverable.context.idref))
                                                    .findAny()
                                                    .orElseThrow(() -> new RuntimeException(String.format("Context not found: %s", deliverable.context.idref)));
                                            return pub;
                                        })
                                        .orElse(deliverable.context),
                                deliverable.output,
                                Optional.ofNullable(deliverable.publication)
                                        .flatMap(publication -> Optional.ofNullable(publication.idref))
                                        .map(idref -> {
                                            final Project.Deliverable.Publication pub = src.publications.stream()
                                                    .filter(publication -> Objects.equals(publication.id, deliverable.publication.idref))
                                                    .findAny()
                                                    .orElseThrow(() -> new RuntimeException(String.format("Publication not found: %s", deliverable.publication.idref)));
                                            return pub;
                                        })
                                        .orElse(deliverable.publication)
                        ))
                        .collect(Collectors.toList()),
                src.includes,
                src.publications,
                src.contexts);
    }

    private static Project load(final URI file, final Set<URI> processed) throws IOException {
        if (processed.contains(file)) {
            throw new RuntimeException("Recursive project file import: " + file);
        }
        final ObjectReader reader = getObjectReader(file);
        final Project project = reader.readValue(file.toURL());
        return resolveIncludes(project, file, ImmutableSet.<URI>builder().addAll(processed).add(file).build());
    }

    private static ObjectReader getObjectReader(final URI file) {
        switch (FileUtils.getExtension(file.getPath()).toLowerCase()) {
            case "xml":
                return xmlReader;
            case "json":
                return jsonReader;
            default:
                throw new RuntimeException("Unrecognized project file format: " + file);
        }
    }

    private static Project resolveIncludes(final Project project, final URI base, final Set<URI> processed) throws IOException {
        if (project.includes == null || project.includes.isEmpty()) {
            return project;
        }
        final List<Project.Deliverable> deliverables = project.deliverables != null
                ? new ArrayList(project.deliverables)
                : new ArrayList();
        final List<Project.Deliverable.Publication> publications = project.publications != null
                ? new ArrayList(project.publications)
                : new ArrayList();
        final List<Project.Deliverable.Context> contexts = project.contexts != null
                ? new ArrayList(project.contexts)
                : new ArrayList();
        if (project.includes != null) {
            for (final Project.ProjectRef projectRef : project.includes) {
                final URI href = projectRef.href.isAbsolute() ? projectRef.href : base.resolve(projectRef.href);
                final Project ref = load(href, processed);
                if (ref.deliverables != null) {
                    deliverables.addAll(ref.deliverables);
                }
                if (ref.publications != null) {
                    publications.addAll(ref.publications);
                }
                if (ref.contexts != null) {
                    contexts.addAll(ref.contexts);
                }
            }
        }
        return new Project(deliverables, project.includes, publications, contexts);
    }

}
