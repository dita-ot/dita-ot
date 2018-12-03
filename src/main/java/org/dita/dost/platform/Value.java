/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2018 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.platform;

import java.util.Objects;

public class Value {
    public final String id;
    public final String value;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Value value = (Value) o;
        return Objects.equals(id, value.id) &&
                Objects.equals(this.value, value.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, value);
    }

    public Value(final String id, final String value) {
        this.id = id;
        this.value = value;
    }
}
