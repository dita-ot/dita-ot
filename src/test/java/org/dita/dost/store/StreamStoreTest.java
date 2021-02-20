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
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.util.XMLUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class StreamStoreTest {

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    private XMLUtils xmlUtils;
    private StreamStore store;
    private File tmpDir;

    @Before
    public void setUp() throws Exception {
        xmlUtils = new XMLUtils();
        tmpDir = temporaryFolder.newFolder();
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
    public void exists_WhenFileExists_ShouldReturnTrue() throws IOException {
        Files.write(tmpDir.toPath().resolve("dummy.xml"), "<dummy/>".getBytes(UTF_8));
        assertTrue(store.exists(tmpDir.toPath().resolve("dummy.xml").toUri()));
    }

    @Test
    public void exists_WhenFileIsMissing_ShouldReturnFalse() {
        assertFalse(store.exists(tmpDir.toPath().resolve("missing.xml").toUri()));
    }

    @Test
    public void exists_WhenInputIsHttp_ShouldReturnFalse() {
        assertFalse(store.exists(URI.create("http://abc/def")));
    }

    @Test
    public void copy_WhenFileExists_ShouldCreateCopy() throws IOException {
        Files.write(tmpDir.toPath().resolve("src.xml"), "<dummy/>".getBytes(UTF_8));

        store.copy(tmpDir.toPath().resolve("src.xml").toUri(), tmpDir.toPath().resolve("dst.xml").toUri());

        assertTrue(Files.exists(tmpDir.toPath().resolve("src.xml")));
        assertTrue(Files.exists(tmpDir.toPath().resolve("dst.xml")));
    }

    @Test(expected = IOException.class)
    public void copy_WhenFileIsMissing_ShouldThrowException() throws IOException {
        store.copy(tmpDir.toPath().resolve("src.xml").toUri(), tmpDir.toPath().resolve("dst.xml").toUri());
    }

    @Test(expected = IOException.class)
    public void copy_WhenSrcIsHttp_ShouldThrowException() throws IOException {
        store.copy(URI.create("http://src.xml"), tmpDir.toPath().resolve("dst.xml").toUri());
    }

    @Test(expected = IOException.class)
    public void copy_WhenDstIsHttp_ShouldThrowException() throws IOException {
        store.copy(tmpDir.toPath().resolve("src.xml").toUri(), URI.create("http://dst.xml"));
    }

    @Test
    public void move_WhenFileExists_ShouldCreateCopy() throws IOException {
        Files.write(tmpDir.toPath().resolve("src.xml"), "<dummy/>".getBytes(UTF_8));

        store.move(tmpDir.toPath().resolve("src.xml").toUri(), tmpDir.toPath().resolve("dst.xml").toUri());

        assertFalse(Files.exists(tmpDir.toPath().resolve("src.xml")));
        assertTrue(Files.exists(tmpDir.toPath().resolve("dst.xml")));
    }

    @Test(expected = IOException.class)
    public void move_WhenFileIsMissing_ShouldThrowException() throws IOException {
        store.move(tmpDir.toPath().resolve("src.xml").toUri(), tmpDir.toPath().resolve("dst.xml").toUri());
    }

    @Test(expected = IOException.class)
    public void move_WhenSrcIsHttp_ShouldThrowException() throws IOException {
        store.move(URI.create("http://src.xml"), tmpDir.toPath().resolve("dst.xml").toUri());
    }

    @Test(expected = IOException.class)
    public void move_WhenDstIsHttp_ShouldThrowException() throws IOException {
        store.move(tmpDir.toPath().resolve("src.xml").toUri(), URI.create("http://dst.xml"));
    }

    @Test
    public void transformWithAnchorInURIPath() throws IOException, DITAOTException, URISyntaxException {
    	final Path target = Paths.get(tmpDir.getAbsolutePath(), "source.xml");
        Files.write(target, "<root/>".getBytes(StandardCharsets.UTF_8));
        final URI uri = new URI(target.toUri().toString() + "#abc");
    	store.transform(uri, Collections.emptyList());
    }
}