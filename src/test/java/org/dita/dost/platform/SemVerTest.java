/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2018 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.platform;

import org.junit.Test;

import static org.junit.Assert.*;

public class SemVerTest {

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