/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2015 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.util;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;

/**
 * Immutable key store for keys and child key scopes.
 *
 * @since 2.2
 */
public class KeyScope {

    public final String id;
    public final String name;
    public final Map<String, KeyDef> keyDefinition;
    public final List<KeyScope> childScopes;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public KeyScope(@JsonProperty("id")final String id, @JsonProperty("name")final String name,
                    @JsonProperty("keyDefinition")final Map<String, KeyDef> keyDefinition,
                    @JsonProperty("childScopes")final List<KeyScope> childScopes) {
        this.id = id;
        this.name = name;
        this.keyDefinition = unmodifiableMap(keyDefinition);
        this.childScopes = unmodifiableList(new ArrayList<>(childScopes));
    }

    public KeyDef get(final String key) {
        return keyDefinition.get(key);
    }

    public Set<String> keySet() {
        return Collections.unmodifiableSet(keyDefinition.keySet());
    }

    public KeyScope getChildScope(final String scope) {
        return childScopes.stream().filter(s -> s.name.equals(scope)).findFirst().orElse(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        KeyScope keyScope = (KeyScope) o;

        if (!Objects.equals(name, keyScope.name)) return false;
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
