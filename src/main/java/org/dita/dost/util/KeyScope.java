/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2015 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.util;

import java.util.*;

import static java.util.Collections.unmodifiableMap;

/**
 * Immutable key store for keys and child key scopes.
 *
 * @since 2.2
 */
public class KeyScope {

    public final String name;
    public final Map<String, KeyDef> keyDefinition;
    public final Map<String, KeyScope> childScopes;

    public KeyScope(final String name, final Map<String, KeyDef> keyDefinition, final List<KeyScope> childScopes) {
        this.name = name;
        this.keyDefinition = unmodifiableMap(keyDefinition);
        final Map<String, KeyScope> cs = new HashMap<>();
        for (final KeyScope scope : childScopes) {
            cs.put(scope.name, scope);
        }
        this.childScopes = unmodifiableMap(cs);
    }

    public KeyScope(final Map<String, KeyDef> keyDefinition) {
        this(null, keyDefinition, Collections.<KeyScope>emptyList());
    }

    public KeyDef get(final String key) {
        return keyDefinition.get(key);
    }

    public Set<String> keySet() {
        return keyDefinition.keySet();
    }

    public KeyScope getChildScope(final String scope) {
        return childScopes.get(scope);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        KeyScope keyScope = (KeyScope) o;

        if (name != null ? !name.equals(keyScope.name) : keyScope.name != null) return false;
        if (!keyDefinition.equals(keyScope.keyDefinition)) return false;
        return childScopes.equals(keyScope.childScopes);
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + keyDefinition.hashCode();
        result = 31 * result + childScopes.hashCode();
        return result;
    }
}
