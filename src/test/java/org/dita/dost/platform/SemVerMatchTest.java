/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2018 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.platform;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SemVerMatchTest {

    @Test
    public void testParse() {
        new SemVerMatch("1.2.3-4.z.5");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseInvalid() {
        new SemVerMatch("1.2.3+meta");
    }

    @Test
    public void testPlainConstructor() {
        assertEquals(
                new SemVerMatch(
                        new SemVerMatch.Range(SemVerMatch.Match.GE, 1, 2, 3),
                        new SemVerMatch.Range(SemVerMatch.Match.LT, 1, 2, 4)
                ),
                new SemVerMatch("1.2.3"));
    }

    @Test
    public void testXRangeConstructor() {
        //    * := >=0.0.0 (Any version satisfies)
        assertEquals(
                new SemVerMatch(
                        new SemVerMatch.Range(SemVerMatch.Match.GE, 0, 0, 0),
                        null
                ),
                new SemVerMatch("*"));
        //        1.x := >=1.0.0 <2.0.0 (Matching major version)
        assertEquals(
                new SemVerMatch(
                        new SemVerMatch.Range(SemVerMatch.Match.GE, 1, 0, 0),
                        new SemVerMatch.Range(SemVerMatch.Match.LT, 2, 0, 0)
                ),
                new SemVerMatch("1.x"));
        //        1.2.x := >=1.2.0 <1.3.0 (Matching major and minor versions)
        assertEquals(
                new SemVerMatch(
                        new SemVerMatch.Range(SemVerMatch.Match.GE, 1, 2, 0),
                        new SemVerMatch.Range(SemVerMatch.Match.LT, 1, 3, 0)
                ),
                new SemVerMatch("1.2.x"));
        //        "" (empty string) := * := >=0.0.0
        assertEquals(
                new SemVerMatch(
                        new SemVerMatch.Range(SemVerMatch.Match.GE, 0, 0, 0),
                        null
                ),
                new SemVerMatch("*"));
        //        1 := 1.x.x := >=1.0.0 <2.0.0
        assertEquals(
                new SemVerMatch(
                        new SemVerMatch.Range(SemVerMatch.Match.GE, 1, 0, 0),
                        new SemVerMatch.Range(SemVerMatch.Match.LT, 2, 0, 0)
                ),
                new SemVerMatch("1"));
        //        1.2 := 1.2.x := >=1.2.0 <1.3.0
        assertEquals(
                new SemVerMatch(
                        new SemVerMatch.Range(SemVerMatch.Match.GE, 1, 2, 0),
                        new SemVerMatch.Range(SemVerMatch.Match.LT, 1, 3, 0)
                ),
                new SemVerMatch("1.2"));
    }

    @Test
    public void testTildeConstructor() {
        //        ~1.2.3 := >=1.2.3 <1.(2+1).0 := >=1.2.3 <1.3.0
        assertEquals(
                new SemVerMatch(
                        new SemVerMatch.Range(SemVerMatch.Match.GE, 1, 2, 3),
                        new SemVerMatch.Range(SemVerMatch.Match.LT, 1, 3, 0)
                ),
                new SemVerMatch("~1.2.3"));
        //        ~1.2 := >=1.2.0 <1.(2+1).0 := >=1.2.0 <1.3.0 (Same as 1.2.x)
        assertEquals(
                new SemVerMatch(
                        new SemVerMatch.Range(SemVerMatch.Match.GE, 1, 2, 0),
                        new SemVerMatch.Range(SemVerMatch.Match.LT, 1, 3, 0)
                ),
                new SemVerMatch("~1.2"));
        //        ~1 := >=1.0.0 <(1+1).0.0 := >=1.0.0 <2.0.0 (Same as 1.x)
        assertEquals(
                new SemVerMatch(
                        new SemVerMatch.Range(SemVerMatch.Match.GE, 1, 0, 0),
                        new SemVerMatch.Range(SemVerMatch.Match.LT, 2, 0, 0)
                ),
                new SemVerMatch("~1"));
        //        ~0.2.3 := >=0.2.3 <0.(2+1).0 := >=0.2.3 <0.3.0
        assertEquals(
                new SemVerMatch(
                        new SemVerMatch.Range(SemVerMatch.Match.GE, 0, 2, 3),
                        new SemVerMatch.Range(SemVerMatch.Match.LT, 0, 3, 0)
                ),
                new SemVerMatch("~0.2.3"));
        //        ~0.2 := >=0.2.0 <0.(2+1).0 := >=0.2.0 <0.3.0 (Same as 0.2.x)
        assertEquals(
                new SemVerMatch(
                        new SemVerMatch.Range(SemVerMatch.Match.GE, 0, 2, 0),
                        new SemVerMatch.Range(SemVerMatch.Match.LT, 0, 3, 0)
                ),
                new SemVerMatch("~0.2"));
        //        ~0 := >=0.0.0 <(0+1).0.0 := >=0.0.0 <1.0.0 (Same as 0.x)
        assertEquals(
                new SemVerMatch(
                        new SemVerMatch.Range(SemVerMatch.Match.GE, 0, 0, 0),
                        new SemVerMatch.Range(SemVerMatch.Match.LT, 1, 0, 0)
                ),
                new SemVerMatch("~0"));
        //        ~1.2.3-beta.2 := >=1.2.3-beta.2 <1.3.0
//        assertEquals(
//                new SemVerMatch(
//                        new SemVerMatch.Range(SemVerMatch.Match.GE, 1, 2, 3),
//                        new SemVerMatch.Range(SemVerMatch.Match.LT, 1, 3, 0)
//                ),
//                new SemVerMatch("~1.2.3-beta.2"));
    }

    @Test
    public void testCaretConstructor() {
        //        ^1.2.3 := >=1.2.3 <2.0.0
        final SemVerMatch actual1 = new SemVerMatch("^1.2.3");
        assertEquals(
                new SemVerMatch(
                        new SemVerMatch.Range(SemVerMatch.Match.GE, 1, 2, 3),
                        new SemVerMatch.Range(SemVerMatch.Match.LT, 2, 0, 0)
                ),
                actual1);
        //        ^0.2.3 := >=0.2.3 <0.3.0
        assertEquals(
                new SemVerMatch(
                        new SemVerMatch.Range(SemVerMatch.Match.GE, 0, 2, 3),
                        new SemVerMatch.Range(SemVerMatch.Match.LT, 0, 3, 0)
                ),
                new SemVerMatch("^0.2.3"));
        //        ^0.0.3 := >=0.0.3 <0.0.4
        assertEquals(
                new SemVerMatch(
                        new SemVerMatch.Range(SemVerMatch.Match.GE, 0, 0, 3),
                        new SemVerMatch.Range(SemVerMatch.Match.LT, 0, 0, 4)
                ),
                new SemVerMatch("^0.0.3"));
        //        ^1.2.3-beta.2 := >=1.2.3-beta.2 <2.0.0
//        assertEquals(
//                new SemVerMatch(
//                        new SemVerMatch.Range(SemVerMatch.Match.GE, 1, 2, 3),
//                        new SemVerMatch.Range(SemVerMatch.Match.LT, 2, 0, 0)
//                ),
//                new SemVerMatch("1.2.3-beta.2"));
        //        ^0.0.3-beta := >=0.0.3-beta <0.0.4
//        assertEquals(
//                new SemVerMatch(
//                        new SemVerMatch.Range(SemVerMatch.Match.GE, 0, 0, 3),
//                        new SemVerMatch.Range(SemVerMatch.Match.LT, 0, 0, 4)
//                ),
//                new SemVerMatch("^0.0.3-beta"));
        //        ^1.2.x := >=1.2.0 <2.0.0
        assertEquals(
                new SemVerMatch(
                        new SemVerMatch.Range(SemVerMatch.Match.GE, 1, 2, 0),
                        new SemVerMatch.Range(SemVerMatch.Match.LT, 2, 0, 0)
                ),
                new SemVerMatch("^1.2.x"));
        //        ^0.0.x := >=0.0.0 <0.1.0
        final SemVerMatch actual = new SemVerMatch("^0.0.x");
        assertEquals(
                new SemVerMatch(
                        new SemVerMatch.Range(SemVerMatch.Match.GE, 0, 0, 0),
                        new SemVerMatch.Range(SemVerMatch.Match.LT, 0, 1, 0)
                ),
                actual);
        //        ^0.0 := >=0.0.0 <0.1.0
        assertEquals(
                new SemVerMatch(
                        new SemVerMatch.Range(SemVerMatch.Match.GE, 0, 0, 0),
                        new SemVerMatch.Range(SemVerMatch.Match.LT, 0, 1, 0)
                ),
                new SemVerMatch("^0.0"));
        //        ^1.x := >=1.0.0 <2.0.0
        assertEquals(
                new SemVerMatch(
                        new SemVerMatch.Range(SemVerMatch.Match.GE, 1, 0, 0),
                        new SemVerMatch.Range(SemVerMatch.Match.LT, 2, 0, 0)
                ),
                new SemVerMatch("^1.x"));
        //        ^0.x := >=0.0.0 <1.0.0
        assertEquals(
                new SemVerMatch(
                        new SemVerMatch.Range(SemVerMatch.Match.GE, 0, 0, 0),
                        new SemVerMatch.Range(SemVerMatch.Match.LT, 1, 0, 0)
                ),
                new SemVerMatch("^0.x"));
    }

    @Test
    public void testHyphenRangeParse() {
        //        1.2.3 - 2.3.4 := >=1.2.3 <=2.3.4
        //        1.2 - 2.3.4 := >=1.2.0 <=2.3.4
        //        1.2.3 - 2.3 := >=1.2.3 <2.4.0
        //        1.2.3 - 2 := >=1.2.3 <3.0.0
    }

    @Test
    public void testContains() {
        assertTrue(
                new SemVerMatch(
                    new SemVerMatch.Range(SemVerMatch.Match.GE, 0, 0, 0),
                    new SemVerMatch.Range(SemVerMatch.Match.LT, 1, 0, 0)
                )
                .contains(new SemVer(0, 1, 0)));
        assertFalse(
                new SemVerMatch(
                        new SemVerMatch.Range(SemVerMatch.Match.GE, 0, 0, 0),
                        new SemVerMatch.Range(SemVerMatch.Match.LT, 1, 0, 0)
                )
                        .contains(new SemVer(2, 0, 0)));
    }

}