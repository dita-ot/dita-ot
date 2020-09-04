/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2011 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.util;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.apache.xerces.impl.dtd.DTDGrammar;
import org.apache.xerces.impl.dtd.XMLDTDDescription;
import org.apache.xerces.xni.grammars.Grammar;
import org.junit.Test;


public class XMLGrammarPoolImplUtilsTest {

    @Test
    public void testCompareDescriptorsByPublicID() {
    	//#3568 We have two DITA topics located in different folders. The DOCTYPES are something like:
    	//<!DOCTYPE "publicID" "topic.dita">
    	//The DTD description for each of them has a different expanded system ID because the relative system ID
    	//is made absolute to the XML file location
    	//But the grammar will still be found because we consider the public ID to have more importance when comparing the descriptions.
    	File base = new File("abc.xml");
    	File expanded = new File("abc.dtd");
    	XMLDTDDescription desc1 = new XMLDTDDescription("publicID", "topic.dtd", base.toURI().toASCIIString(), expanded.toURI().toASCIIString(), "topic");
    	DTDGrammar grammar = new DTDGrammar(null, desc1);
    	XMLGrammarPoolImplUtils utils = new XMLGrammarPoolImplUtils();
    	utils.putGrammar(grammar);
    	
    	File base2 = new File("../abc.xml");
    	File expanded2 = new File("../abc.dtd");
    	XMLDTDDescription desc2 = new XMLDTDDescription("publicID", "topic.dtd", base2.toURI().toASCIIString(), expanded2.toURI().toASCIIString(), "topic");
    	Grammar retrieved = utils.getGrammar(desc2);
    	assertEquals("Should have retrieved the grammar, even though the expanded system ID differs", grammar, retrieved); 
    }
}
