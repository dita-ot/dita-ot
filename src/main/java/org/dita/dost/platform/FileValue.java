/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2018 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.platform;

import java.util.Objects;

public class FileValue {
    public final String id;
    public final String value;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileValue fileValue = (FileValue) o;
        return Objects.equals(id, fileValue.id) &&
                Objects.equals(value, fileValue.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, value);
    }

    public FileValue(final String id, final String value) {
        this.id = id;
        this.value = value;
    }
}
