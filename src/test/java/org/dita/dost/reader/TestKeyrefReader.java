/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2010 All Rights Reserved.
 */
package org.dita.dost.reader;
import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.assertEquals;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.xml.sax.SAXException;

import org.custommonkey.xmlunit.XMLUnit;
import org.xml.sax.InputSource;

import org.dita.dost.TestUtils;
import org.dita.dost.module.Content;
import org.dita.dost.reader.KeyrefReader;
import org.dita.dost.resolver.DitaURIResolverFactory;
import org.junit.Test;

public class TestKeyrefReader {

    private static final File resourceDir = TestUtils.getResourceDir(TestKeyrefReader.class);
    private static final File srcDir = new File(resourceDir, "src");

    @Test
    public void testKeyrefReader() throws IOException, SAXException {
        final String path = System.getProperty("user.dir");
        DitaURIResolverFactory.setPath(path);
        final File filename = new File(srcDir, "keyrefreader.xml");

        final Set <String> set = new HashSet<String> ();
        set.add("blatview");
        set.add("blatfeference");
        set.add("blatintro");
        set.add("keyword");
        set.add("escape");
        final KeyrefReader keyrefreader = new KeyrefReader();
        keyrefreader.setKeys(set);
        keyrefreader.read(filename.getAbsolutePath());
        final Content content = keyrefreader.getContent();
        @SuppressWarnings("unchecked")
        final Hashtable<String, String> act = (Hashtable<String, String>) content.getValue();

        final Hashtable<String, String> exp = new Hashtable<String, String>();
        exp.put("blatfeference", "<topicref keys='blatview blatfeference blatintro' href='blatview.dita' navtitle='blatview' locktitle='yes' class='- map/topicref '/>");
        exp.put("blatview", "<topicref keys='blatview blatfeference blatintro' href='blatview.dita' navtitle='blatview' locktitle='yes' class='- map/topicref '/>");
        exp.put("blatintro", "<topicref keys='blatview blatfeference blatintro' href='blatview.dita' navtitle='blatview' locktitle='yes' class='- map/topicref '/>");
        exp.put("keyword", "<topicref keys='keyword' class='- map/topicref '><topicmeta class='- map/topicmeta '><keywords class='- topic/keywords '><keyword class='- topic/keyword '>keyword value</keyword></keywords></topicmeta></topicref>");
        exp.put("escape", "<topicref keys='escape' class='- map/topicref ' navtitle='&amp;&lt;&gt;&quot;&apos;'><topicmeta class='- map/topicmeta '><keywords class='- topic/keywords '><keyword class='- topic/keyword '>&amp;&lt;&gt;&quot;&apos;</keyword></keywords></topicmeta></topicref>");
        
        TestUtils.resetXMLUnit();
        XMLUnit.setIgnoreWhitespace(true);
        assertEquals(exp.keySet(), act.keySet());
        for (Map.Entry<String, String> e: exp.entrySet()) {
            String ev = e.getValue();
            String av = act.get(e.getKey());
            assertXMLEqual(new InputSource(new StringReader(ev)),
                           new InputSource(new StringReader(av)));
        }
    }

}
