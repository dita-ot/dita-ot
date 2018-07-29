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

public class SemVerMatchTest {

    @Test
    public void testPlainConstructor() {
        final SemVerMatch m = new SemVerMatch("1.2.3");
        assertEquals(null, m.match);
        assertEquals(Integer.valueOf(1), m.major);
        assertEquals(Integer.valueOf(2), m.minor);
        assertEquals(Integer.valueOf(3), m.patch);
    }

    @Test
    public void testTildeConstructor() {
        final SemVerMatch m = new SemVerMatch("~1.2");
        assertEquals(SemVerMatch.Match.TILDE, m.match);
        assertEquals(Integer.valueOf(1), m.major);
        assertEquals(Integer.valueOf(2), m.minor);
        assertEquals(null, m.patch);
    }

}