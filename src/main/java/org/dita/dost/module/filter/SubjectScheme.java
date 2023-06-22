/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2017 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.module.filter;

import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;
import org.w3c.dom.Element;

/**
 * Subject scheme bindings
 *
 * @param subjectSchemeMap Subject scheme bindings, {@code Map<AttName, Map<ElemName, Set<Element>>>}
 */
public record SubjectScheme(Map<QName, Map<String, Set<Element>>> subjectSchemeMap) {
  public boolean isEmpty() {
    return subjectSchemeMap.isEmpty();
  }
}
