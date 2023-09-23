/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2020 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.store;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmNode;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.util.XMLUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.w3c.dom.Document;

public class StreamStoreTest {

  private XMLUtils xmlUtils;
  private StreamStore store;

  @TempDir
  private File tmpDir;

  @BeforeEach
  public void setUp() throws Exception {
    xmlUtils = new XMLUtils();
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
    Files.writeString(tmpDir.toPath().resolve("dummy.xml"), "<dummy/>");
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
    Files.writeString(tmpDir.toPath().resolve("src.xml"), "<dummy/>");

    store.copy(tmpDir.toPath().resolve("src.xml").toUri(), tmpDir.toPath().resolve("dst.xml").toUri());

    assertTrue(Files.exists(tmpDir.toPath().resolve("src.xml")));
    assertTrue(Files.exists(tmpDir.toPath().resolve("dst.xml")));
  }

  @Test
  public void copy_WhenFileIsMissing_ShouldThrowException() throws IOException {
    assertThrows(
      IOException.class,
      () -> store.copy(tmpDir.toPath().resolve("src.xml").toUri(), tmpDir.toPath().resolve("dst.xml").toUri())
    );
  }

  @Test
  public void copy_WhenSrcIsHttp_ShouldThrowException() throws IOException {
    assertThrows(
      IOException.class,
      () -> store.copy(URI.create("http://src.xml"), tmpDir.toPath().resolve("dst.xml").toUri())
    );
  }

  @Test
  public void copy_WhenDstIsHttp_ShouldThrowException() throws IOException {
    assertThrows(
      IOException.class,
      () -> store.copy(tmpDir.toPath().resolve("src.xml").toUri(), URI.create("http://dst.xml"))
    );
  }

  @Test
  public void move_WhenFileExists_ShouldCreateCopy() throws IOException {
    Files.writeString(tmpDir.toPath().resolve("src.xml"), "<dummy/>");

    store.move(tmpDir.toPath().resolve("src.xml").toUri(), tmpDir.toPath().resolve("dst.xml").toUri());

    assertFalse(Files.exists(tmpDir.toPath().resolve("src.xml")));
    assertTrue(Files.exists(tmpDir.toPath().resolve("dst.xml")));
  }

  @Test
  public void move_WhenFileIsMissing_ShouldThrowException() throws IOException {
    assertThrows(
      IOException.class,
      () -> store.move(tmpDir.toPath().resolve("src.xml").toUri(), tmpDir.toPath().resolve("dst.xml").toUri())
    );
  }

  @Test
  public void move_WhenSrcIsHttp_ShouldThrowException() throws IOException {
    assertThrows(
      IOException.class,
      () -> store.move(URI.create("http://src.xml"), tmpDir.toPath().resolve("dst.xml").toUri())
    );
  }

  @Test
  public void move_WhenDstIsHttp_ShouldThrowException() throws IOException {
    assertThrows(
      IOException.class,
      () -> store.move(tmpDir.toPath().resolve("src.xml").toUri(), URI.create("http://dst.xml"))
    );
  }

  @Test
  public void transformWithAnchorInURIPath() throws IOException, DITAOTException, URISyntaxException {
    final Path target = Paths.get(tmpDir.getAbsolutePath(), "source.xml");
    Files.writeString(target, "<root/>");
    final URI uri = new URI(target.toUri() + "#abc");
    store.transform(uri, Collections.emptyList());
  }
}
