/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2024 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.util;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ClasspathURIResolverTest {

  private ClasspathURIResolver resolver = new ClasspathURIResolver();

  @ParameterizedTest
  @ValueSource(
    strings = { "classpath:ClasspathURIResolverTest/file.txt", "classpath:/ClasspathURIResolverTest/file.txt" }
  )
  void resolve(String href) throws TransformerException {
    assertSource("classpath:/ClasspathURIResolverTest/file.txt", resolver.resolve(href, "classpath:baz/quz"));
  }

  @Test
  void resolve_relative() throws TransformerException {
    assertSource(
      "classpath:/ClasspathURIResolverTest/file.txt",
      resolver.resolve("file.txt", "classpath:ClasspathURIResolverTest/other.txt")
    );
  }

  private void assertSource(String exp, Source act) {
    assertEquals(exp, act.getSystemId());
    assertInstanceOf(StreamSource.class, act);
    if (act instanceof StreamSource stream) {
      assertNotNull(stream.getInputStream());
    }
    close(act);
  }

  //  @Test
  //  void resolve_other() {
  //    assertEquals(URI.create("file:/foo/bar"), resolver.getAbsolute("file:/foo/bar", "file:/baz/quz"));
  //    assertEquals(URI.create("file:/baz/foo/bar"), resolver.getAbsolute("foo/bar", "file:/baz/quz"));
  //  }
  //
  //  @Test
  //  void resolve_relative() {
  //    assertEquals(URI.create("foo/bar"), resolver.getAbsolute("foo/bar", "classpath:baz/quz"));
  //  }

  private void close(Source act) {
    if (act instanceof StreamSource stream) {
      try {
        if (stream.getInputStream() != null) {
          stream.getInputStream().close();
        }
        if (stream.getReader() != null) {
          stream.getReader().close();
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
