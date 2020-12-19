/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2020 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.store;

import com.google.common.io.Files;
import net.sf.saxon.s9api.Destination;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmNode;
import org.apache.commons.io.FileUtils;
import org.dita.dost.util.XMLUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import java.io.File;
import java.io.IOException;

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

    @After
    public void tearDown() throws Exception {
//        FileUtils.deleteDirectory(tmpDir);
        System.out.println(tmpDir);
    }
}