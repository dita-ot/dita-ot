/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2020 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.store;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmNode;
import org.apache.commons.io.FileUtils;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.util.XMLUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

public class StreamStoreTest {

    private XMLUtils xmlUtils;
    private StreamStore store;
    private File tmpDir;

    @Before
    public void setUp() throws Exception {
        xmlUtils = new XMLUtils();
        tmpDir = Files.createTempDirectory(StreamStoreTest.class.getName()).toFile();
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
    public void transformWithAnchorInURIPath() throws IOException, DITAOTException, URISyntaxException {
    	final Path target = Paths.get(tmpDir.getAbsolutePath(), "source.xml");
        Files.write(target, "<root/>".getBytes(StandardCharsets.UTF_8));
        final URI uri = new URI(target.toUri().toString() + "#abc");
    	store.transform(uri, Collections.emptyList());
    }

    @After
    public void tearDown() throws Exception {
        FileUtils.deleteDirectory(tmpDir);
    }
}