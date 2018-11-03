/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2018 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.platform;

import java.util.Objects;

public class SemVer implements Comparable<SemVer> {

    public final int major;
    public final int minor;
    public final int patch;

    public SemVer(int major, int minor, int patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    public SemVer(String version) {
        String[] tokens = version.split("\\.");
        major = Integer.valueOf(tokens[0]);
        minor = tokens.length >= 2 ? Integer.valueOf(tokens[1]) : 0;
        patch = tokens.length >= 3 ? Integer.valueOf(tokens[2]) : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SemVer semVer = (SemVer) o;
        return major == semVer.major &&
                minor == semVer.minor &&
                patch == semVer.patch;
    }

    @Override
    public int hashCode() {
        return Objects.hash(major, minor, patch);
    }

    @Override
    public int compareTo(SemVer o) {
        if (o.major == this.major && o.minor == this.minor && o.patch == this.patch) {
            return 0;
        }
        if (o.major > this.major) {
            return -1;
        }
        if (o.major < this.major) {
            return 1;
        }
        if (o.minor > this.minor) {
            return -1;
        }
        if (o.minor < this.minor) {
            return 1;
        }
        if (o.patch > this.patch) {
            return -1;
        }
        if (o.patch < this.patch) {
            return 1;
        }
        throw new IllegalStateException();
    }

    @Override
    public String toString() {
        return String.format("%d.%d.%d", major, minor, patch);
    }
}
