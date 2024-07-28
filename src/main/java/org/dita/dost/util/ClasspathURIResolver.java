/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2024 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.util;

import static org.dita.dost.util.Constants.URI_SEPARATOR;

import java.io.IOException;
import java.net.URI;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.lib.ResourceRequest;
import net.sf.saxon.lib.ResourceResolver;
import net.sf.saxon.trans.XPathException;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ClasspathURIResolver implements EntityResolver, URIResolver, ResourceResolver {

  static final String SCHEME_PREFIX = "classpath:";

  @Override
  public Source resolve(String href, String base) throws TransformerException {
    if (href.startsWith(SCHEME_PREFIX)) {
      var path = href.substring(SCHEME_PREFIX.length());
      if (!path.startsWith(URI_SEPARATOR)) {
        path = URI_SEPARATOR + path;
      }
      var absPath = getClass().getResource(path).toString();
      //      var absPath = SCHEME_PREFIX + path;
      System.out.println("A:" + absPath);
      return new StreamSource(getClass().getResourceAsStream(path), absPath);
    } else if (base.startsWith(SCHEME_PREFIX)) {
      var path = URI.create(base.substring(SCHEME_PREFIX.length())).resolve(href).toString();
      if (!path.startsWith(URI_SEPARATOR)) {
        path = URI_SEPARATOR + path;
      }
      var absPath = getClass().getResource(path).toString();
      //      var absPath = SCHEME_PREFIX + path;
      System.out.println("B:" + absPath);
      return new StreamSource(getClass().getResourceAsStream(path), absPath);
    }
    System.out.println("C: href=" + href + " base=" + base);
    return null;
  }

  @Override
  public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
    if (systemId.startsWith(SCHEME_PREFIX)) {
      var path = systemId.substring(SCHEME_PREFIX.length());
      if (!path.startsWith(URI_SEPARATOR)) {
        path = URI_SEPARATOR + path;
      }
      var input = getClass().getResourceAsStream(path);
      var absPath = getClass().getResource(path).toString();
      //      var absPath = SCHEME_PREFIX + path;
      var res = new InputSource(input);
      res.setSystemId(absPath);
      System.out.println("D:" + absPath);
      return res;
    }
    System.out.println("E:" + systemId);
    return null;
  }

  @Override
  public Source resolve(ResourceRequest request) throws XPathException {
    var href = request.uri;
    var base = request.baseUri;
    try {
      return resolve(href, base);
    } catch (TransformerException e) {
      throw new XPathException(e);
    }
  }
}
