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
import com.google.common.collect.ImmutableSet;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class ProjectFactory {

    private static final ObjectReader reader = new ObjectMapper().reader().forType(Project.class);

    public static Project load(final URI file) throws IOException {
        try {
            return load(file, Collections.emptySet());
        } catch (IOException e) {
            throw new IOException("Failed to read project file: " + e.getMessage(), e);
        }
    }

    private static Project load(final URI file, final Set<URI> processed) throws IOException {
        if (processed.contains(file)) {
            throw new RuntimeException("Recursive project file import: " + file);
        }
        final Project project = reader.readValue(file.toURL());
        return resolve(project, file, ImmutableSet.<URI>builder().addAll(processed).add(file).build());
    }

    private static Project resolve(final Project project, final URI base, final Set<URI> processed) throws IOException {
        if (project.includes == null || project.includes.isEmpty()) {
            return project;
        }
        final List<Project.Deliverable> res = project.deliverables != null
                ? new ArrayList(project.deliverables)
                : new ArrayList();
        if (project.includes != null) {
            for (final Project.ProjectRef projectRef : project.includes) {
                final URI href = projectRef.href.isAbsolute() ? projectRef.href : base.resolve(projectRef.href);
                final Project ref = load(href, processed);
                res.addAll(ref.deliverables);
            }
        }
        return new Project(res, project.includes);
    }

}
