/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2020 Radu Coravu
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.util;

import org.apache.xerces.impl.dtd.DTDGrammar;
import org.apache.xerces.impl.dtd.XMLDTDDescription;
import org.apache.xerces.xni.grammars.Grammar;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


public class XMLGrammarPoolImplUtilsTest {

    private XMLGrammarPoolImplUtils utils;

    @Before
    public void setUp() {
        utils = new XMLGrammarPoolImplUtils();
    }

    @Test
    public void testCompareDescriptorsByPublicID_same() {
        final XMLDTDDescription desc1 = getXmldtdDescription("file:/foo/abc.xml", "file:/foo/abc.dtd", "publicID");
        final DTDGrammar exp = new DTDGrammar(null, desc1);
        utils.putGrammar(exp);

        final XMLDTDDescription desc2 = getXmldtdDescription("file:/abc.xml", "file:/abc.dtd", "publicID");
        final Grammar act = utils.getGrammar(desc2);

        assertEquals(exp, act);
    }

    @Test
    public void testCompareDescriptorsByPublicID_different() {
        final XMLDTDDescription desc1 = getXmldtdDescription("file:/foo/abc.xml", "file:/foo/abc.dtd", "publicID");
        final DTDGrammar exp = new DTDGrammar(null, desc1);
        utils.putGrammar(exp);

        final XMLDTDDescription desc2 = getXmldtdDescription("file:/abc.xml", "file:/abc.dtd", "differentId");
        final Grammar act = utils.getGrammar(desc2);

        assertNull(act);
    }

    @Test
    public void testCompareDescriptorsBySystemID_same() {
        final XMLDTDDescription desc1 = getXmldtdDescription("file:/foo/abc.xml", "file:/foo/abc.dtd", null);
        final DTDGrammar exp = new DTDGrammar(null, desc1);
        utils.putGrammar(exp);

        final XMLDTDDescription desc2 = getXmldtdDescription("file:/abc.xml", "file:/foo/abc.dtd", null);
        final Grammar act = utils.getGrammar(desc2);

        assertEquals(exp, act);
    }

    @Test
    public void testCompareDescriptorsBySystemID_different() {
        final XMLDTDDescription desc1 = getXmldtdDescription("file:/foo/abc.xml", "file:/foo/abc.dtd", null);
        final DTDGrammar exp = new DTDGrammar(null, desc1);
        utils.putGrammar(exp);

        final XMLDTDDescription desc2 = getXmldtdDescription("file:/abc.xml", "file:/abc.dtd", null);
        final Grammar act = utils.getGrammar(desc2);

        assertNull(act);
    }

    private XMLDTDDescription getXmldtdDescription(String base, String systemId, String publicId) {
        return new XMLDTDDescription(publicId, "topic.dtd", base, systemId, "topic");
    }

}
