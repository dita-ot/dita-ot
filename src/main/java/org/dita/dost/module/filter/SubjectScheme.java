/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2017 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.module.filter;

import java.util.*;
import javax.xml.namespace.QName;

/**
 * Subject scheme bindings
 *
 * @param subjectSchemeMap Subject scheme bindings, {@code Map<AttName, Map<ElemName, Set<Element>>>}
 */
public record SubjectScheme(Map<QName, Map<String, Set<SubjectDefinition>>> subjectSchemeMap) {
  public boolean isEmpty() {
    return subjectSchemeMap.isEmpty();
  }

  public record SubjectDefinition(Set<String> keys, String keyref, List<SubjectDefinition> children) {
    public SubjectDefinition(Set<String> keys, String keyref, List<SubjectDefinition> children) {
      this.keys = keys;
      this.keyref = keyref;
      this.children = Objects.requireNonNullElse(children, Collections.emptyList());
    }
    public List<SubjectDefinition> flatten() {
      final List<SubjectDefinition> res = new ArrayList<>();
      flatten(this, res);
      return res;
    }
    private void flatten(SubjectDefinition def, final List<SubjectDefinition> buf) {
      buf.add(def);
      for (SubjectDefinition child : def.children) {
        flatten(child, buf);
      }
    }
  }
}
