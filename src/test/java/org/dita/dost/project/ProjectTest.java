/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2019 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.project;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.dita.dost.project.Project.Context;
import org.dita.dost.project.Project.Deliverable.Inputs;
import org.dita.dost.project.Project.Deliverable.Inputs.Input;
import org.dita.dost.project.Project.Deliverable.Profile;
import org.dita.dost.project.Project.Deliverable.Profile.DitaVal;
import org.dita.dost.project.Project.Publication;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;

public class ProjectTest {

    private final ObjectWriter jsonWriter = new ObjectMapper()
            .writerFor(Project.class)
            .with(SerializationFeature.INDENT_OUTPUT);

    @Test
    public void serializeJsonSimple() throws IOException {
        final Project project = getProject();
        jsonWriter.writeValueAsString(project);
    }

    @Test
    public void serializeJsonRoot() throws IOException {
        final Project project = new Project(
                null,
                Arrays.asList(
                        new Project.ProjectRef(URI.create("simple.json"))
                ),
                null,
                null
        );
        jsonWriter.writeValueAsString(project);
    }

    private Project getProject() {
        return new Project(
                Arrays.asList(
                        new Project.Deliverable(
                                "name",
                                "id",
                                new Context("Site", "site", null,
                                        new Inputs(//"inputs-name",
//                        "inputs-ref",
                                                Arrays.asList(new Input(URI.create("site.ditamap")))),
                                        new Profile(//"profile-name",
//                        "profile-ref",
                                                Arrays.asList(new DitaVal(URI.create("site.ditaval"))))
                                ),
                                URI.create("./site"),
                                new Publication("Site", "site", null, "html5", Arrays.asList(
                                        new Publication.Param("args.gen.task.lbl", "YES", null, null),
                                        new Publication.Param("args.rellinks", "noparent", null, null)
                                ))
                        )
                ),
                null,
                null,
                null
        );
    }
}
