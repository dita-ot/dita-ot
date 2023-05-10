/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2023 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.util;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

/**
 * URI resolver that first tries one supplied URI resolver, and if that returns null, falls back to another.
 */
public class ChainedURIResolver implements URIResolver {

  private final URIResolver first;
  private final URIResolver second;

  public ChainedURIResolver(URIResolver first, URIResolver second) {
    this.first = first;
    this.second = second;
  }

  @Override
  public Source resolve(String href, String base) throws TransformerException {
    final Source res = first.resolve(href, base);
    if (res != null) {
      return res;
    }
    return second.resolve(href, base);
  }
}
