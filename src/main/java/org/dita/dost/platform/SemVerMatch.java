/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2018 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.platform;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SemVerMatch {

    enum Match {
        TILDE,
        CARET,
        EQ,
        LT,
        LE,
        GT,
        GE
    }

    public static class Range {
        public final Match match;
        public final SemVer version;

        public Range(Match match, Integer major, Integer minor, Integer patch) {
            if (match == null || major == null || minor == null || patch == null) {
                throw new NullPointerException();
            }
            this.match = match;
            this.version = new SemVer(major, minor, patch);
        }

        @Override
        public String toString() {
            final StringBuilder buf = new StringBuilder();
            if (match != null) {
                switch (match) {
                    case TILDE:
                        buf.append("~");
                        break;
                    case CARET:
                        buf.append("^");
                        break;
                    case LT:
                        buf.append("<");
                        break;
                    case LE:
                        buf.append("<=");
                        break;
                    case GT:
                        buf.append(">");
                        break;
                    case GE:
                        buf.append(">=");
                        break;
                    case EQ:
                        break;
                    default:
                        throw new IllegalArgumentException();
                }
            }
            buf.append(version.toString());
            return buf.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Range range = (Range) o;
            return match == range.match &&
                    Objects.equals(version, range.version);
        }

        @Override
        public int hashCode() {
            return Objects.hash(match, version);
        }
    }

    private static final Pattern MATCH_PATTERN = Pattern.compile("(~|\\^|<=?|>=?)?([0-9.x*]+?)(-.+?(\\+.+?)?)?");

    public final Range start;
    public final Range end;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SemVerMatch that = (SemVerMatch) o;
        return Objects.equals(start, that.start) &&
                Objects.equals(end, that.end);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end);
    }

    public SemVerMatch(Range start, Range end) {
        this.start = start;
        this.end = end;
    }

    public SemVerMatch(String str) {
        if (str.equals("") || str.equals("*")) {
            start = new Range(Match.GE, 0, 0, 0);
            end = null;
            return;
        }
        final Matcher matcher = MATCH_PATTERN.matcher(str);
        if (!matcher.matches()) {
            throw new IllegalArgumentException();
        }
        final Match match;
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
            match = Match.EQ;
        }
        String[] tokens = matcher.group(2).split("\\.");
        final Integer major = tokens.length >= 1 ? parseToken(tokens[0]) : null;
        final Integer minor = tokens.length >= 2 ? parseToken(tokens[1]) : null;
        final Integer patch = tokens.length >= 3 ? parseToken(tokens[2]) : null;

        switch (match) {
            case TILDE:
                start = new Range(Match.GE,
                        major != null ? major : 0,
                        minor != null ? minor : 0,
                        patch != null ? patch : 0);
                end = tilde(Match.LT, major, minor);
                break;
            case CARET:
                start = new Range(Match.GE,
                        major != null ? major : 0,
                        minor != null ? minor : 0,
                        patch != null ? patch : 0);
                end = caret(Match.LT, major, minor, patch);
                break;
            case LT:
                start = new Range(Match.GE, 0, 0, 0);
                end = new Range(Match.LT,
                        major != null ? major : 0,
                        minor != null ? minor : 0,
                        patch != null ? patch : 0);
                break;
            case LE:
                start = new Range(Match.GE, 0, 0, 0);
                end = new Range(Match.LE,
                        major != null ? major : 0,
                        minor != null ? minor : 0,
                        patch != null ? patch : 0);
                break;
            case GT:
                start = new Range(Match.GT,
                        major != null ? major : 0,
                        minor != null ? minor : 0,
                        patch != null ? patch : 0);
                end = null;
                break;
            case GE:
                start = new Range(Match.GE,
                        major != null ? major : 0,
                        minor != null ? minor : 0,
                        patch != null ? patch : 0);
                end = null;
                break;
            case EQ:
                start = new Range(Match.GE,
                        major != null ? major : 0,
                        minor != null ? minor : 0,
                        patch != null ? patch : 0);
                end = inc(Match.LT, major, minor, patch);
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    private static Range inc(Match match, Integer major, Integer minor, Integer patch) {
        final Integer major1 = major != null && minor == null && patch == null ? Integer.valueOf(major + 1) : major;
        final Integer minor1 = null != minor && patch == null ? Integer.valueOf(minor + 1) : minor;
        final Integer patch1 = patch != null ? Integer.valueOf(patch + 1) : patch;
        return new Range(match,
                major1 != null ? major1 : 0,
                minor1 != null ? minor1 : 0,
                patch1 != null ? patch1 : 0);
    }

    private static Range tilde(Match match, Integer major, Integer minor) {
        final Integer major1 = major != null && minor == null ? Integer.valueOf(major + 1) : major;
        final Integer minor1 = minor != null ? minor + 1 : null;
        final Integer patch1 = null;
        return new Range(match,
                major1 != null ? major1 : 0,
                minor1 != null ? minor1 : 0,
                patch1 != null ? patch1 : 0);
    }

    private static Range caret(Match match, Integer major, Integer minor, Integer patch) {
        final List<Integer> tokens = new ArrayList<>();
        if (major != null) {
            tokens.add(major);
            if (minor != null) {
                tokens.add(minor);
                if (patch != null) {
                    tokens.add(patch);
                }
            }
        }
        // Find first non-null
        int i = 0;
        for (; i < tokens.size(); i++) {
            if (tokens.get(i) != 0) {
                break;
            }
        }
        if (i == tokens.size()) {
            i = tokens.size() - 1;
        }

        // increment first non-null
        tokens.set(i, tokens.get(i) + 1);
        // zero after first non-null
        for (i++; i < tokens.size(); i++) {
            tokens.set(i, 0);
        }

        return new Range(match,
                tokens.size() > 0 && tokens.get(0) != null ? tokens.get(0) : 0,
                tokens.size() > 1 && tokens.get(1) != null ? tokens.get(1) : 0,
                tokens.size() > 2 && tokens.get(2) != null ? tokens.get(2) : 0);
    }

    private static Integer parseToken(String token) {
        switch (token) {
            case "x":
            case "X":
            case "*":
                return null;
            default:
                return Integer.valueOf(token);
        }
    }

    public boolean contains(SemVer semver) {
        if (start != null && !compare(start.version, semver, start.match)) {
            return false;
        }
        if (end != null && !compare(end.version, semver, end.match)) {
            return false;
        }
        return true;
    }

    private boolean compare(SemVer self, SemVer other, Match ops) {
        final int res = -1 * self.compareTo(other);
        switch(ops) {
            case LT:
                return res < 0;
            case LE:
                return res <= 0;
            case GT:
                return res > 0;
            case GE:
                return res >= 0;
            case EQ:
                return res == 0;
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();
        if (start != null) {
            buf.append(start);
        }
        if (start != null && end != null) {
            buf.append(" ");
        }
        if (end != null) {
            buf.append(end);
        }
        return buf.toString();
    }

}
