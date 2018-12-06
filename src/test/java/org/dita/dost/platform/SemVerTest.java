/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2018 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.platform;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class SemVerTest {

    @Test
    public void testParse() {
        final SemVer act = new SemVer("1.2.3");
        assertEquals(1, act.major);
        assertEquals(2, act.minor);
        assertEquals(3, act.patch);
    }

    @Test
    public void testParsePreRelease() {
        final SemVer act = new SemVer("1.0.0-x.7.z.92");
        assertEquals(1, act.major);
        assertEquals(0, act.minor);
        assertEquals(0, act.patch);
        assertEquals(Arrays.asList("x", 7, "z", 92), act.preRelease);
    }

    @Test
    public void testParseBuildMetadata() {
        final SemVer act = new SemVer("1.0.0-beta+exp.sha.5114f85");
        assertEquals(1, act.major);
        assertEquals(0, act.minor);
        assertEquals(0, act.patch);
        assertEquals(Arrays.asList("beta"), act.preRelease);
    }

    @Test
    public void testCompareEqual() {
        assertEquals(0, new SemVer("1.2.3").compareTo(new SemVer("1.2.3")));
    }

    @Test
    public void testCompareLessThan() {
        assertEquals(-1, new SemVer("1.2.3").compareTo(new SemVer("1.2.4")));
        assertEquals(-1, new SemVer("1.2.3").compareTo(new SemVer("1.3.0")));
        assertEquals(-1, new SemVer("1.2.3").compareTo(new SemVer("2.0.0")));
    }

    @Test
    public void testCompareGreaterThan() {
        assertEquals(1, new SemVer("1.2.4").compareTo(new SemVer("1.2.3")));
        assertEquals(1, new SemVer("1.3.0").compareTo(new SemVer("1.2.3")));
        assertEquals(1, new SemVer("2.0.0").compareTo(new SemVer("1.2.3")));
    }

}