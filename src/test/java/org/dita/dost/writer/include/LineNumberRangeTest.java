package org.dita.dost.writer.include;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

class LineNumberRangeTest {

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

  private LineNumberRange range;
  private CacheContentHandler handler;

  @ParameterizedTest
  @CsvSource({ "0,2", "0,100" })
  void copyLines(int start, int end) throws IOException, SAXException {
    range = new LineNumberRange(start, end);
    handler = new CacheContentHandler();
    range.handler(handler);

    var reader = new BufferedReader(new StringReader(CONTENT));
    range.copyLines(reader);

    assertEquals(List.of("foo", "\n", "bar", "\n", "baz"), handler.lines);
  }

  @Test
  void copyLines_start() throws IOException, SAXException {
    range = new LineNumberRange(0, 1);
    handler = new CacheContentHandler();
    range.handler(handler);

    var reader = new BufferedReader(new StringReader(CONTENT));
    range.copyLines(reader);

    assertEquals(List.of("foo", "\n", "bar"), handler.lines);
  }

  @Test
  void copyLines_middle() throws IOException, SAXException {
    range = new LineNumberRange(1, 1);
    handler = new CacheContentHandler();
    range.handler(handler);

    var reader = new BufferedReader(new StringReader(CONTENT));
    range.copyLines(reader);

    assertEquals(List.of("bar"), handler.lines);
  }

  @Test
  void copyLines_end() throws IOException, SAXException {
    range = new LineNumberRange(1, 2);
    handler = new CacheContentHandler();
    range.handler(handler);

    var reader = new BufferedReader(new StringReader(CONTENT));
    range.copyLines(reader);

    assertEquals(List.of("bar", "\n", "baz"), handler.lines);
  }

  @Test
  void copyLines_linefeed() throws IOException, SAXException {
    range = new LineNumberRange(0, 0);
    handler = new CacheContentHandler();
    range.handler(handler);

    var reader = new BufferedReader(new StringReader("\n"));
    range.copyLines(reader);

    assertEquals(List.of(""), handler.lines);
  }

  @Test
  void copyLines_empty() throws IOException, SAXException {
    range = new LineNumberRange(0, 0);
    handler = new CacheContentHandler();
    range.handler(handler);

    var reader = new BufferedReader(new StringReader(""));
    range.copyLines(reader);

    assertEquals(List.of(), handler.lines);
  }

  @ParameterizedTest
  @CsvSource(value = { "2,1", "-2,-1" })
  void copyLines_invalid(int start, int end) throws IOException, SAXException {
    range = new LineNumberRange(start, end);
    handler = new CacheContentHandler();
    range.handler(handler);

    var reader = new BufferedReader(new StringReader(CONTENT));
    range.copyLines(reader);

    assertEquals(List.of(), handler.lines);
  }
}
