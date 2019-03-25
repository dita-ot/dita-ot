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

import static org.junit.Assert.assertEquals;

public class ProjectFactoryTest {

    @Test
    public void resolveReferences() {
        final Project src = new Project(
                Collections.singletonList(
                        new Project.Deliverable(null, null, null,
                                new Project.Deliverable.Publication(null, null, "id", null, null)
                        )
                ),
                null,
                Collections.singletonList(
                        new Project.Deliverable.Publication(null, "id", null, null, null)
                )
        );
        ProjectFactory.resolveReferences(src);
    }

    @Test(expected = RuntimeException.class)
    public void resolveReferences_notFound() {
        final Project src = new Project(
                Collections.singletonList(
                        new Project.Deliverable(null, null, null,
                                new Project.Deliverable.Publication(null, null, "missing", null, null)
                        )
                ),
                null,
                Collections.emptyList()
        );
        final Project act = ProjectFactory.resolveReferences(src);
        assertEquals("id", act.deliverables.get(0).publication.id);
    }

}