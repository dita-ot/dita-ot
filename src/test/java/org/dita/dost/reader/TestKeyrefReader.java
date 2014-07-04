/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2010 All Rights Reserved.
 */
package org.dita.dost.reader;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.assertEquals;
import static org.dita.dost.util.URLUtils.*;

import java.io.File;
import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.custommonkey.xmlunit.XMLUnit;
import org.xml.sax.InputSource;

import org.dita.dost.TestUtils;
import org.dita.dost.reader.KeyrefReader;
import org.junit.Test;

public class TestKeyrefReader {

    private static final File resourceDir = TestUtils.getResourceDir(TestKeyrefReader.class);
    private static final File srcDir = new File(resourceDir, "src");

    @Test
    public void testKeyrefReader() throws Exception {
        final String path = System.getProperty("user.dir");
        final File filename = new File(srcDir, "keyrefreader.xml");

        final Set <String> set = new HashSet<String> ();
        set.add("blatview");
        set.add("blatfeference");
        set.add("blatintro");
        set.add("keyword");
        set.add("escape");
        set.add("top");
        set.add("nested");
        final KeyrefReader keyrefreader = new KeyrefReader();
        keyrefreader.setKeys(set);
        keyrefreader.read(toURI(filename.getAbsolutePath()));
        final Map<String, Element> act= keyrefreader.getKeyDefinition();

        final Map<String, String> exp = new HashMap<String, String>();
        exp.put("blatfeference", "<topicref keys='blatview blatfeference blatintro' href='blatview.dita' navtitle='blatview' locktitle='yes' class='- map/topicref '/>");
        exp.put("blatview", "<topicref keys='blatview blatfeference blatintro' href='blatview.dita' navtitle='blatview' locktitle='yes' class='- map/topicref '/>");
        exp.put("blatintro", "<topicref keys='blatview blatfeference blatintro' href='blatview.dita' navtitle='blatview' locktitle='yes' class='- map/topicref '/>");
        exp.put("keyword", "<topicref keys='keyword' class='- map/topicref '><topicmeta class='- map/topicmeta '><keywords class='- topic/keywords '><keyword class='- topic/keyword '>keyword value</keyword></keywords></topicmeta></topicref>");
        exp.put("escape", "<topicref keys='escape' class='- map/topicref ' navtitle='&amp;&lt;&gt;&quot;&apos;'><topicmeta class='- map/topicmeta '><keywords class='- topic/keywords '><keyword class='- topic/keyword '>&amp;&lt;&gt;&quot;&apos;</keyword></keywords></topicmeta></topicref>");
        exp.put("top", "<topicref keys='top' class='- map/topicref ' navtitle='top'><topicmeta class='- map/topicmeta '><keywords class='- topic/keywords '><keyword class='- topic/keyword '>top keyword</keyword></keywords></topicmeta><topicref keys='nested' class='- map/topicref ' navtitle='nested'><topicmeta class='- map/topicmeta '><keywords class='- topic/keywords '><keyword class='- topic/keyword '>nested keyword</keyword></keywords></topicmeta></topicref></topicref>");
        exp.put("nested", "<topicref keys='nested' class='- map/topicref ' navtitle='nested'><topicmeta class='- map/topicmeta '><keywords class='- topic/keywords '><keyword class='- topic/keyword '>nested keyword</keyword></keywords></topicmeta></topicref>");
        
        TestUtils.resetXMLUnit();
        XMLUnit.setIgnoreWhitespace(true);
        assertEquals(exp.keySet(), act.keySet());
        for (Map.Entry<String, String> e: exp.entrySet()) {
            final Document ev = keyDefToDoc(e.getValue());
            final Document av = act.get(e.getKey()).getOwnerDocument();
            assertXMLEqual(ev, av);
        }
    }
    
    private static Document keyDefToDoc(final String key) throws Exception {
        final InputSource inputSource = new InputSource(new StringReader(key));
        final DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        return documentBuilder.parse(inputSource);
    }

}
