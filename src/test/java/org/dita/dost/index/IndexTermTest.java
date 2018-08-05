/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2011 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.index;

import static org.dita.dost.index.IndexTerm.IndexTermPrefix.*;
import static org.dita.dost.index.IndexTerm.IndexTermPrefix.SEE;
import static org.junit.Assert.*;

import org.dita.dost.util.Constants;
import org.dita.dost.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class IndexTermTest {

    private static final Locale DEFAULT_LOCALE = StringUtils.getLocale("");

    private final IndexTerm unset = new IndexTerm();
    private final IndexTerm empty = new IndexTerm();
    private final IndexTerm whitespace = new IndexTerm();
    private final IndexTerm simple = new IndexTerm();
    private final IndexTerm number = new IndexTerm();
    private final IndexTerm punctuation = new IndexTerm();
    private final IndexTerm multipleWords = new IndexTerm();
    private final IndexTerm whitespaceSeparated = new IndexTerm();
    private final IndexTerm nonAscii = new IndexTerm();
    private final IndexTerm prefixed = new IndexTerm();
    private final IndexTerm nested = new IndexTerm();

    @Before
    public void setUp() {
        empty.setTermName("");
        empty.setTermKey("");
        empty.setTermPrefix(null);
        whitespace.setTermName("  \t  \n  ");
        whitespace.setTermKey("  \t  \n  ");
        number.setTermName("3.14159265");
        number.setTermKey("3.14159265");
        punctuation.setTermName(";");
        punctuation.setTermKey(";");
        simple.setTermName("simple");
        simple.setTermKey("simple");
        multipleWords.setTermName("multiple words");
        multipleWords.setTermKey("multiple words");
        whitespaceSeparated.setTermName("  \t  \n  whitespace  \t  \n  separated  \t  \n  ");
        whitespaceSeparated.setTermKey("  \t  \n  whitespace  \t  \n  separated  \t  \n  ");
        nonAscii.setTermName("\u65e5\u672c\u8a9e");
        nonAscii.setTermKey("\u65e5\u672c\u8a9e");
        prefixed.setTermName("fixed");
        prefixed.setTermKey("fixed");
        prefixed.setTermPrefix(SEE);
        nested.setTermName("root");
        nested.setTermKey("root");
        nested.addSubTerms(construct("sub", 2, 1));
    }

    @Test
    public void testHashCode() {
        assertEquals(simple.hashCode(), simple.hashCode());
        final IndexTerm s = new IndexTerm();
        s.setTermName("simple");
        s.setTermKey("simple");
        assertEquals(simple.hashCode(), s.hashCode());
    }

    @Test
    public void testGetTermLocale() {
        IndexTerm.setTermLocale(null);
        assertNull(IndexTerm.getTermLocale());
        IndexTerm.setTermLocale(Locale.JAPAN);
        assertEquals(Locale.JAPAN, IndexTerm.getTermLocale());

    }

    @Test
    public void testSetTermLocale() {
        IndexTerm.setTermLocale(null);
        IndexTerm.setTermLocale(Locale.JAPAN);
    }

    @Test
    public void testGetTermName() {
        assertEquals(null, unset.getTermName());
        assertEquals("", empty.getTermName());
        assertEquals("  \t  \n  ", whitespace.getTermName());
        assertEquals("3.14159265", number.getTermName());
        assertEquals(";", punctuation.getTermName());
        assertEquals("simple", simple.getTermName());
        assertEquals("multiple words", multipleWords.getTermName());
        assertEquals("  \t  \n  whitespace  \t  \n  separated  \t  \n  ", whitespaceSeparated.getTermName());
        assertEquals("\u65e5\u672c\u8a9e", nonAscii.getTermName());
    }

    @Test
    public void testSetTermName() {
        new IndexTerm().setTermName(null);
        new IndexTerm().setTermName("");
        new IndexTerm().setTermName("  \t  \n  ");
        new IndexTerm().setTermName("simple");
    }

    @Test
    public void testGetTermKey() {
        assertEquals(null, unset.getTermKey());
        assertEquals("", empty.getTermKey());
        assertEquals("  \t  \n  ", whitespace.getTermKey());
        assertEquals("3.14159265", number.getTermKey());
        assertEquals(";", punctuation.getTermKey());
        assertEquals("simple", simple.getTermKey());
        assertEquals("multiple words", multipleWords.getTermKey());
        assertEquals("  \t  \n  whitespace  \t  \n  separated  \t  \n  ", whitespaceSeparated.getTermKey());
        assertEquals("\u65e5\u672c\u8a9e", nonAscii.getTermKey());
    }

    @Test
    public void testSetTermKey() {
        new IndexTerm().setTermKey(null);
        new IndexTerm().setTermKey("");
        new IndexTerm().setTermKey("  \t  \n  ");
        new IndexTerm().setTermKey("simple");
    }

    @Test
    public void testGetSubTerms() {
        assertTrue(simple.getSubTerms().isEmpty());
        assertEquals(2, nested.getSubTerms().size());
        assertEquals(2, nested.getSubTerms().get(0).getSubTerms().size());
    }

    @Test
    public void testGetStartAttribute() {
        final IndexTerm i = new IndexTerm();
        assertNull(i.getStartAttribute());
        i.setStartAttribute("");
        assertEquals("", i.getStartAttribute());
        i.setStartAttribute("start");
        assertEquals("start", i.getStartAttribute());
    }

    @Test
    public void testGetEndAttribute() {
        final IndexTerm i = new IndexTerm();
        assertNull(i.getEndAttribute());
        i.setEndAttribute("");
        assertEquals("", i.getEndAttribute());
        i.setEndAttribute("end");
        assertEquals("end", i.getEndAttribute());
    }

    @Test
    public void testSetStartAttribute() {
        new IndexTerm().setStartAttribute(null);
        new IndexTerm().setStartAttribute("");
    }

    @Test
    public void testSetEndAttribute() {
        new IndexTerm().setEndAttribute(null);
        new IndexTerm().setEndAttribute("");
    }

    @Test(expected = NullPointerException.class)
    public void testAddSubTerm() {
        new IndexTerm().addSubTerm(null);
    }

    @Test
    public void testAddSubTerms() {
        new IndexTerm().addSubTerms(null);
        new IndexTerm().addSubTerms(Collections.<IndexTerm>emptyList());
    }

    @Test
    public void testEqualsObject() {
        assertTrue(simple.equals(simple));
        final IndexTerm s = new IndexTerm();
        s.setTermName("simple");
        s.setTermKey("simple");
        assertTrue(simple.equals(s));
        assertFalse(simple.equals(nested));
        assertFalse(simple.equals(null));
        assertFalse(simple.equals(""));
    }

    @Test
    public void testSortSubTerms() {
        IndexTerm.setTermLocale(DEFAULT_LOCALE);
        final IndexTerm root = new IndexTerm();
        final String[] src = { "B", "b", "a", "A" };
        for (final String key: src) {
            final IndexTerm i = new IndexTerm();
            i.setTermName(key);
            i.setTermKey(key);
            root.addSubTerm(i);
        }
        root.sortSubTerms();

        final List<String> act = new ArrayList<String>();
        for (final IndexTerm i: root.getSubTerms()) {
            act.add(i.getTermKey());
        }

        final String[] exp = { "a", "A", "b", "B" };
        assertArrayEquals(exp, act.toArray(new String[0]));
    }

    @Test
    public void testCompareTo() {
        IndexTerm.setTermLocale(DEFAULT_LOCALE);
        assertEquals(1, simple.compareTo(empty));
        assertEquals(0, simple.compareTo(simple));
        assertEquals(-1, simple.compareTo(nonAscii));
        try {
            IndexTerm.setTermLocale(null);
            new IndexTerm().compareTo(new IndexTerm());
            fail();
        } catch (final NullPointerException e) {}
    }

    @Test
    public void testGetTargetList() {
        assertTrue(simple.getTargetList().isEmpty());
    }

    @Test(expected = NullPointerException.class)
    public void testAddTarget() {
        new IndexTerm().addSubTerm(null);
    }

    @Test
    public void testAddTargets() {
        new IndexTerm().addTargets(null);
        new IndexTerm().addTargets(Collections.<IndexTermTarget>emptyList());
    }

    @Test
    public void testHasSubTerms() {
        assertTrue(nested.hasSubTerms());
        assertFalse(simple.hasSubTerms());
    }

    @Test @Ignore
    public void testToString() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetTermPrefix() {
        final IndexTerm i = new IndexTerm();
        assertNull(i.getTermPrefix());
        i.setTermPrefix(null);
        assertEquals(null, i.getTermPrefix());
        i.setTermPrefix(SEE);
        assertEquals(SEE, i.getTermPrefix());
    }

    @Test
    public void testSetTermPrefix() {
        new IndexTerm().setTermPrefix(null);
    }

    @Test
    public void testGetTermFullName() {
        IndexTerm.setTermLocale(null);
        assertEquals("simple", simple.getTermFullName());
        assertEquals("See fixed", prefixed.getTermFullName());
        final IndexTerm empty = new IndexTerm();
        empty.setTermName("empty");
        empty.setTermPrefix(null);
        assertEquals("empty", empty.getTermFullName());

        IndexTerm.setTermLocale(DEFAULT_LOCALE);
        assertEquals("See fixed", prefixed.getTermFullName());
        IndexTerm.setTermLocale(StringUtils.getLocale("ar_EG"));
        assertEquals("\u0623\u0646\u0638\u0631 fixed", prefixed.getTermFullName());
        IndexTerm.setTermLocale(null);
    }

    @Test
    public void testUpdateSubTerm() {
        final IndexTerm single = new IndexTerm();
        final IndexTerm singleSub = new IndexTerm();
        singleSub.setTermPrefix(SEE);
        single.addSubTerm(singleSub);
        single.updateSubTerm();
        for (final IndexTerm s: single.getSubTerms()) {
            assertEquals(SEE_ALSO, s.getTermPrefix());
        }

        final IndexTerm more = new IndexTerm();
        final IndexTerm moreSub = new IndexTerm();
        moreSub.setTermPrefix(SEE);
        more.addSubTerm(moreSub);
        more.addSubTerm(new IndexTerm());
        more.updateSubTerm();
        for (final IndexTerm m: more.getSubTerms()) {
            assertFalse(SEE_ALSO.equals(m));
        }
    }

    @Test
    public void testIsLeaf() {
        assertTrue(simple.isLeaf());
        assertFalse(nested.isLeaf());
    }

    private List<IndexTerm> construct(final String base, final int count, final int depth) {
        final List<IndexTerm> res = new ArrayList<IndexTerm>();
        for (int i = 0; i < count; i++) {
            final IndexTerm it = new IndexTerm();
            final String n = base + " " + i;
            it.setTermName(n);
            it.setTermKey(n);
            for (int j = 0; j < count; j++) {
                final IndexTermTarget itt = new IndexTermTarget();
                itt.setTargetName("Target " + j + " " + n);
                itt.setTargetURI("base/target " + j + " " + n);
                it.addTarget(itt);
            }
            if (depth > 0) {
                it.addSubTerms(construct(base + " " + i, count, depth - 1));
            }
            res.add(it);
        }
        return res;
    }

}
