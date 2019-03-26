/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2019 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.project;

import org.junit.Test;

import java.util.Collections;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

public class ProjectFactoryTest {

    @Test
    public void resolveReferences_deliverable() {
        final Project src = new Project(
                singletonList(
                        new Project.Deliverable(
                                null,
                                null,
                                null,
                                new Project.Deliverable.Publication(
                                        null,
                                        null,
                                        "id",
                                        null,
                                        null)
                        )
                ),
                null,
                singletonList(
                        new Project.Deliverable.Publication(
                                null,
                                "id",
                                null,
                                null,
                                null)
                ),
                null
        );
        ProjectFactory.resolveReferences(src);
    }

    @Test
    public void resolveReferences_context() {
        final Project src = new Project(
                singletonList(
                        new Project.Deliverable(
                                null,
                                new Project.Deliverable.Context(
                                        null,
                                        null,
                                        "id",
                                        null,
                                        null),
                                null,
                                null
                        )
                ),
                null,
                null,
                singletonList(
                        new Project.Deliverable.Context(
                                null,
                                "id",
                                null,
                                null,
                                null)
                )
        );
        ProjectFactory.resolveReferences(src);
    }

    @Test(expected = RuntimeException.class)
    public void resolveReferences_notFound() {
        final Project src = new Project(
                singletonList(
                        new Project.Deliverable(
                                null,
                                null,
                                null,
                                new Project.Deliverable.Publication(
                                        null,
                                        null,
                                        "missing",
                                        null,
                                        null)
                        )
                ),
                null,
                Collections.emptyList(),
                null
        );
        final Project act = ProjectFactory.resolveReferences(src);
        assertEquals("id", act.deliverables.get(0).publication.id);
    }

}