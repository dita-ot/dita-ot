package org.dita.dost.writer.include;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

class AnchorRangeTest {

  private final String CONTENT = """
        foo
        // START
        bar
        // END
        baz
        """;

  private static class CacheContentHandler extends DefaultHandler {

    public final List<String> lines = new ArrayList<>();

    @Override
    public void characters(char ch[], int start, int length) throws SAXException {
      lines.add(new String(ch, start, length));
    }
  }

  private AnchorRange range;
  private CacheContentHandler handler;

  @Test
  void copyLines() throws IOException, SAXException {
    range = new AnchorRange(null, null);
    handler = new CacheContentHandler();
    range.handler(handler);

    var reader = new BufferedReader(new StringReader(CONTENT));
    range.copyLines(reader);

    assertEquals(List.of("foo", "\n", "// START", "\n", "bar", "\n", "// END", "\n", "baz"), handler.lines);
  }

  @Test
  void copyLines_start() throws IOException, SAXException {
    range = new AnchorRange(null, "END");
    handler = new CacheContentHandler();
    range.handler(handler);

    var reader = new BufferedReader(new StringReader(CONTENT));
    range.copyLines(reader);

    assertEquals(List.of("foo", "\n", "// START", "\n", "bar"), handler.lines);
  }

  @Test
  void copyLines_middle() throws IOException, SAXException {
    range = new AnchorRange("START", "END");
    handler = new CacheContentHandler();
    range.handler(handler);

    var reader = new BufferedReader(new StringReader(CONTENT));
    range.copyLines(reader);

    assertEquals(List.of("bar"), handler.lines);
  }

  @Test
  void copyLines_end() throws IOException, SAXException {
    range = new AnchorRange("START", null);
    handler = new CacheContentHandler();
    range.handler(handler);

    var reader = new BufferedReader(new StringReader(CONTENT));
    range.copyLines(reader);

    assertEquals(List.of("bar", "\n", "// END", "\n", "baz"), handler.lines);
  }
}
