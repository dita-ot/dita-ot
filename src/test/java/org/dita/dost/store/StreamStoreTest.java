/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2020 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.store;

import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.dita.dost.util.XMLUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

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
    public void getExists() throws IOException, SaxonApiException, URISyntaxException {
    	assertFalse(store.exists(new URI("http://abc/def")));
    }

    @After
    public void tearDown() throws Exception {
//        FileUtils.deleteDirectory(tmpDir);
        System.out.println(tmpDir);
    }
}