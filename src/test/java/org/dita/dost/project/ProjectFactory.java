/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2019 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.project;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class ProjectFactory {

    public static Project load(final URI file) throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        try {
            return resolve(mapper.readValue(file.toURL(), Project.class), file);
        } catch (IOException e) {
            throw new IOException("Failed to read project file: " + e.getMessage(), e);
        }
    }

    private static Project resolve(final Project project, final URI base) throws IOException {
        if (project.includes == null || project.includes.isEmpty()) {
            return project;
        }
        final List<Project.Deliverable> res = project.deliverables != null
                ? new ArrayList(project.deliverables)
                : new ArrayList();
        if (project.includes != null) {
            for (final Project.ProjectRef projectRef : project.includes) {
                final URI href = projectRef.href.isAbsolute() ? projectRef.href : base.resolve(projectRef.href);
                final Project ref = load(href);
                res.addAll(ref.deliverables);
            }
        }
        return new Project(res, project.includes);
    }

}
