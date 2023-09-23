/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2013 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.writer;

import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.URLUtils.*;

import java.net.URI;
import java.util.Optional;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.util.*;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Filter for processing content key reference elements in DITA files. Instances are
 * reusable but not thread-safe.
 */
public final class ConkeyrefFilter extends AbstractXMLFilter {

  private KeyScope keys;

  public void setKeyDefinitions(final KeyScope keys) {
    this.keys = keys;
  }

  // XML filter methods ------------------------------------------------------

  @Override
  public void startElement(final String uri, final String localName, final String name, final Attributes atts)
    throws SAXException {
    AttributesImpl resAtts = null;
    final String conkeyref = atts.getValue(ATTRIBUTE_NAME_CONKEYREF);
    conkeyref:if (conkeyref != null) {
      final int keyIndex = conkeyref.indexOf(SLASH);
      final String key = keyIndex != -1 ? conkeyref.substring(0, keyIndex) : conkeyref;
      final KeyDef keyDef = keys.get(key);
      if (keyDef != null) {
        final String id = keyIndex != -1 ? conkeyref.substring(keyIndex + 1) : null;
        resAtts = new AttributesImpl(atts);
        XMLUtils.removeAttribute(resAtts, ATTRIBUTE_NAME_CONKEYREF);
        final KeyDef k = keys.get(key);
        if (k.href != null && (k.scope.equals(ATTR_SCOPE_VALUE_LOCAL))) {
          URI target = getRelativePath(keyDef.source, k.href);
          final String keyFragment = k.href.getFragment();
          if (id != null && keyFragment != null) {
            target = setFragment(target, keyFragment + SLASH + id);
          } else if (id != null) {
            target = setFragment(target, id);
          } else if (keyFragment != null) {
            target = setFragment(target, keyFragment);
          }
          XMLUtils.addOrSetAttribute(resAtts, ATTRIBUTE_NAME_CONREF, target.toString());
        } else {
          logger.warn(MessageUtils.getMessage("DOTJ060W", key, conkeyref).setLocation(atts).toString());
        }
      } else {
        logger.error(MessageUtils.getMessage("DOTJ046E", conkeyref).setLocation(atts).toString());
      }
    }
    getContentHandler().startElement(uri, localName, name, resAtts != null ? resAtts : atts);
  }

  /**
   * Update href URI.
   *
   * @param keyMap key definition map URI
   * @param href href URI
   * @return updated href URI
   */
  private URI getRelativePath(final URI keyMap, final URI href) {
    final URI inputMap = Optional.ofNullable(job.getFileInfo(keyMap)).map(fi -> fi.uri).orElse(null);
    final URI keyValue;
    if (inputMap != null) {
      final URI tmpMap = job.tempDirURI.resolve(inputMap);
      keyValue = tmpMap.resolve(stripFragment(href));
    } else {
      keyValue = job.tempDirURI.resolve(stripFragment(href));
    }
    return URLUtils.getRelativePath(currentFile, keyValue);
  }
}
