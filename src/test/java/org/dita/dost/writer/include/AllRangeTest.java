package org.dita.dost.writer.include;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

class AllRangeTest {

  private final String CONTENT = """
        foo
        bar
        baz
        """;

  private static class CacheContentHandler extends DefaultHandler {

    public final List<String> lines = new ArrayList<>();

    @Override
    public void characters(char ch[], int start, int length) throws SAXException {
      lines.add(new String(ch, start, length));
    }
  }

  private AllRange range;
  private CacheContentHandler handler;

  @BeforeEach
  void setUp() {
    range = new AllRange();
    handler = new CacheContentHandler();
    range.handler(handler);
  }

  @Test
  void copyLines() throws IOException, SAXException {
    var reader = new BufferedReader(new StringReader(CONTENT));
    range.copyLines(reader);

    assertEquals(List.of("foo", "\n", "bar", "\n", "baz"), handler.lines);
  }

  @Test
  void copyLines_linefeed() throws IOException, SAXException {
    var reader = new BufferedReader(new StringReader("\n"));
    range.copyLines(reader);

    assertEquals(List.of(""), handler.lines);
  }

  @Test
  void copyLines_empty() throws IOException, SAXException {
    var reader = new BufferedReader(new StringReader(""));
    range.copyLines(reader);

    assertEquals(List.of(), handler.lines);
  }
}
