/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2023 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.module.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.dita.dost.module.filter.SubjectScheme.SubjectDefinition;
import org.junit.jupiter.api.Test;

class SubjectSchemeTest {

  @Test
  void flatten() {
    var src = createSubjectDefinition(
      "A",
      createSubjectDefinition("A_a", createSubjectDefinition("A_a_i"), createSubjectDefinition("A_a_ii")),
      createSubjectDefinition("A_b", createSubjectDefinition("A_b_i"), createSubjectDefinition("A_b_ii"))
    );
    var exp = List.of(
      createSubjectDefinition(
        "A",
        createSubjectDefinition("A_a", createSubjectDefinition("A_a_i"), createSubjectDefinition("A_a_ii")),
        createSubjectDefinition("A_b", createSubjectDefinition("A_b_i"), createSubjectDefinition("A_b_ii"))
      ),
      createSubjectDefinition("A_a", createSubjectDefinition("A_a_i"), createSubjectDefinition("A_a_ii")),
      createSubjectDefinition("A_a_i"),
      createSubjectDefinition("A_a_ii"),
      createSubjectDefinition("A_b", createSubjectDefinition("A_b_i"), createSubjectDefinition("A_b_ii")),
      createSubjectDefinition("A_b_i"),
      createSubjectDefinition("A_b_ii")
    );
    assertEquals(exp, src.flatten());
  }

  private SubjectDefinition createSubjectDefinition(String key, SubjectDefinition... children) {
    return new SubjectDefinition(Set.of(key), null, Arrays.asList(children));
  }
}
