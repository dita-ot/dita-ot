/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2015 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.util;

import static java.util.Collections.unmodifiableMap;

import java.util.*;

/**
 * Immutable key store for keys and child key scopes.
 *
 * @since 2.2
 */
public record KeyScope(String id, String name, Map<String, KeyDef> keyDefinition, List<KeyScope> childScopes) {
  public static final String ROOT_ID = "#root";
  public static final KeyScope EMPTY = new KeyScope(ROOT_ID, null, Collections.emptyMap(), Collections.emptyList());

  public KeyScope(
    final String id,
    final String name,
    final Map<String, KeyDef> keyDefinition,
    final List<KeyScope> childScopes
  ) {
    this.id = id;
    this.name = name;
    this.keyDefinition = unmodifiableMap(keyDefinition);
    this.childScopes = List.copyOf(childScopes);
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

  public static KeyScope merge(final KeyScope scope1, final KeyScope scope2) {
    if (!Objects.equals(scope1.id, scope2.id)) {
      throw new IllegalArgumentException(
        String.format("Scopes should have the same ID: %s != %s", scope1.id, scope2.id)
      );
    }
    final Map<String, KeyDef> keyDefinition = new HashMap<>();
    scope1.keyDefinition.forEach(keyDefinition::putIfAbsent);
    scope2.keyDefinition.forEach(keyDefinition::putIfAbsent);
    final List<KeyScope> childScopes = new ArrayList<>();
    childScopes.addAll(scope1.childScopes);
    childScopes.addAll(scope2.childScopes);
    return new KeyScope(
      scope1.id,
      scope1.name,
      unmodifiableMap(keyDefinition),
      Collections.unmodifiableList(childScopes)
    );
  }
}
