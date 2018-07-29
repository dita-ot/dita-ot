/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2018 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.platform;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SemVerMatch implements Comparable<SemVerMatch> {

    enum Match {
        TILDE,
        CARET,
        LT,
        LE,
        GT,
        GE
    }

    private static final Pattern MATCH_PATTERN = Pattern.compile("(~|\\^|<=?|>=?)?(.+)");

    public final Match match;
    public final Integer major;
    public final Integer minor;
    public final Integer patch;

    public SemVerMatch(String str) {
        final Matcher matcher = MATCH_PATTERN.matcher(str);
        if (!matcher.matches()) {
            throw new IllegalArgumentException();
        }
        String[] tokens = matcher.group(2).split("\\.");
        major = Integer.valueOf(tokens[0]);
        minor = tokens.length >= 2 ? Integer.valueOf(tokens[1]) : null;
        patch = tokens.length >= 3 ? Integer.valueOf(tokens[2]) : null;
        if (matcher.group(1) != null) {
            switch (matcher.group(1)) {
                case "~":
                    match = Match.TILDE;
                    break;
                case "^":
                    match = Match.CARET;
                    break;
                case "<":
                    match = Match.LT;
                    break;
                case "<=":
                    match = Match.LE;
                    break;
                case ">":
                    match = Match.GT;
                    break;
                case ">=":
                    match = Match.GE;
                    break;
                default:
                    throw new IllegalArgumentException();
            }
        } else {
            match = null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SemVerMatch semVer = (SemVerMatch) o;
        return major == semVer.major &&
                minor == semVer.minor &&
                patch == semVer.patch;
    }

    @Override
    public int hashCode() {
        return Objects.hash(major, minor, patch);
    }

    @Override
    public int compareTo(SemVerMatch o) {
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
}
