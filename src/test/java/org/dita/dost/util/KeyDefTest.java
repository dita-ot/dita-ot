package org.dita.dost.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class KeyDefTest {

    @Test
    public void testKeyDefStringStringString() {
        final KeyDef k = new KeyDef("foo", "bar", "scope", "baz");
        assertEquals("foo", k.keys);
        assertEquals("bar", k.href);
        assertEquals("scope", k.scope);
        assertEquals("baz", k.source);
        final KeyDef n = new KeyDef("foo", null, null, null);
        assertEquals("foo", n.keys);
        assertNull(n.href);
        assertNull(n.scope);
        assertNull(n.source);
    }
    
    @Test
    public void testKeyDefToString() {
        final KeyDef k = new KeyDef("foo", "bar", "scope", "baz");
        assertEquals("foo=bar(scope)(baz)", k.toString());
        final KeyDef n = new KeyDef("foo", null, null, null);
        assertEquals("foo=", n.toString());
    }

}
