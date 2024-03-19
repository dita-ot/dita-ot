/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2011 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.platform;

import java.util.stream.Collectors;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * List transtypes integration action.
 *
 * @since 1.5.4
 * @author Jarno Elovirta
 */
final class ListTranstypeAction extends ImportAction {

  /**
   * Get result.
   */
  @Override
  public void getResult(final ContentHandler buf) throws SAXException {
    final String separator = paramTable.getOrDefault("separator", "|");
    final char[] ret = valueSet
      .stream()
      .map(Value::value)
      .distinct()
      .sorted()
      .collect(Collectors.joining(separator))
      .toCharArray();
    buf.characters(ret, 0, ret.length);
  }

  @Override
  public String getResult() {
    throw new UnsupportedOperationException();
  }
}
