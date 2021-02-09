/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2020 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.store;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.util.XMLUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.helpers.XMLFilterImpl;

import com.google.common.io.Files;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmNode;

public class StreamStoreTest {

    private XMLUtils xmlUtils;
    private StreamStore store;
    private File tmpDir;

    @Before
    public void setUp() throws Exception {
        xmlUtils = new XMLUtils();
        tmpDir = Files.createTempDir();
        store = new StreamStore(tmpDir, xmlUtils);
    }

    @Test
    public void getSerializer_subDirectory() throws IOException, SaxonApiException {
        final Document doc = XMLUtils.getDocumentBuilder().newDocument();
        doc.appendChild(doc.createElement("foo"));
        final XdmNode source = xmlUtils.getProcessor().newDocumentBuilder().wrap(doc);

        final Serializer serializer = store.getSerializer(tmpDir.toURI().resolve("foo/bar"));
        serializer.serializeNode(source);
    }
    
    @Test
    public void transformWithAnchorInURIPath() throws IOException, SaxonApiException, DITAOTException, URISyntaxException {
    	File target = new File(tmpDir, "source.xml");
    	FileWriter fw = new FileWriter(target);
    	fw.write("<root/>");
    	fw.close();
    	URI uri = new URI(target.toURI().toString() + "#abc");
    	String[] startAccumulator = new String[1];
    	List<XMLFilter> filters = new ArrayList<XMLFilter>();
    	filters.add(new XMLFilterImpl() {
    		@Override
    		public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
    			super.startElement(uri, localName, qName, atts);
    			startAccumulator[0] = localName;
    		}
    	});
    	store.transform(uri, filters);
    	assertEquals("root", startAccumulator[0]);
    }

    @After
    public void tearDown() throws Exception {
//        FileUtils.deleteDirectory(tmpDir);
        System.out.println(tmpDir);
    }
}