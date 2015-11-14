/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */
package org.dita.dost.util;

import static java.util.Collections.*;

import java.util.*;

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
        for (final KeyScope scope: childScopes) {
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

}
