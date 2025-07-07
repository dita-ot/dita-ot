/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2020 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.writer.include;

import java.io.BufferedReader;
import java.io.IOException;
import org.dita.dost.writer.CoderefResolver;
import org.xml.sax.SAXException;

/**
 * Range based on anchors that define the start and end of a range.
 * Lines between the start and end achors are included.
 * If the start anchor is {@code null}, lines are included from the first line until the line before the end anchor.
 * If the end anchor is {@code null}, lines are included from line after the start anchor until the last line.
 * If both start and end anchor are {@code null}, all lines are included.
 */
public class AnchorRange extends AllRange implements Range {

  private final String start;
  private final String end;
  private int include;

  /**
   * Create a new anchor range based on case-sensitive matching for lines that contain the anchor.
   * @param start start anchor, exclusive, may be {@code null}
   * @param end end line, exclusive, may be {@code null}
   */
  public AnchorRange(final String start, final String end) {
    this.start = start;
    this.end = end;
    include = start != null ? -1 : 1;
  }

  @Override
  public void copyLines(final BufferedReader codeReader) throws IOException, SAXException {
    boolean first = true;
    String line;
    while ((line = codeReader.readLine()) != null) {
      if (include == -1 && start != null) {
        include = line.contains(start) ? 0 : -1;
      } else if (include > -1 && end != null) {
        include = line.contains(end) ? -1 : include;
      }
      if (include > 0) {
        if (first) {
          first = false;
        } else {
          handler.characters(CoderefResolver.XML_NEWLINE, 0, CoderefResolver.XML_NEWLINE.length);
        }
        final char[] ch = line.toCharArray();
        handler.characters(ch, 0, ch.length);
      }
      if (include >= 0) {
        include++;
      }
    }
  }
}
