/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2017 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.module.filter;

import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.util.Map;
import java.util.Set;

/**
 * Subject scheme bindings
 */
public class SubjectScheme {

    /** Subject scheme bindings, {@code Map<AttName, Map<ElemName, Set<Element>>>} */
    public final Map<QName, Map<String, Set<Element>>> subjectSchemeMap;

    public SubjectScheme(final Map<QName, Map<String, Set<Element>>> subjectSchemeMap) {
        this.subjectSchemeMap = subjectSchemeMap;
    }

    public boolean isEmpty() {
        return subjectSchemeMap.isEmpty();
    }
}
