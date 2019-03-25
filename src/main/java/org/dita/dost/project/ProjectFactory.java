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
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

public class ProjectFactory {

    private static final ObjectReader reader = new ObjectMapper().reader().forType(Project.class);

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
                src.deliverables.stream()
                        .map(deliverable -> new Project.Deliverable(
                                deliverable.name,
                                deliverable.context,
                                deliverable.output,
                                Optional.ofNullable(deliverable.publication.idref)
                                        .map(idref -> {
                                            final Project.Deliverable.Publication pub = src.publications.stream()
                                                    .filter(publication -> Objects.equals(publication.id, deliverable.publication.idref))
                                                    .findAny()
                                                    .orElseThrow(() -> new RuntimeException(String.format("Publication not %s found", deliverable.publication.idref)));
                                            return pub;
                                        })
                                        .orElse(deliverable.publication)
                        ))
                        .collect(Collectors.toList()),
                src.includes,
                src.publications);
    }

    private static Project load(final URI file, final Set<URI> processed) throws IOException {
        if (processed.contains(file)) {
            throw new RuntimeException("Recursive project file import: " + file);
        }
        final Project project = reader.readValue(file.toURL());
        return resolveIncludes(project, file, ImmutableSet.<URI>builder().addAll(processed).add(file).build());
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
            }
        }
        return new Project(deliverables, project.includes, publications);
    }

}
