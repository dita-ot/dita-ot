/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2022 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.util;

import static org.dita.dost.util.Constants.*;

import org.w3c.dom.Element;

public class DitaUtils {
  public static boolean isDitaFormat(final Element elem) {
    final String format = elem.getAttribute(ATTRIBUTE_NAME_FORMAT);
    return isDitaFormat(format);
  }

  public static boolean isDitaFormat(final Job.FileInfo fi) {
    return isDitaFormat(fi.format);
  }

  public static boolean isDitaFormat(final String format) {
    return format == null || format.isEmpty() || format.equals(ATTR_FORMAT_VALUE_DITA);
  }

  public static boolean isLocalScope(final Element elem) {
    final String scope = elem.getAttribute(ATTRIBUTE_NAME_SCOPE);
    return isLocalScope(scope);
  }

  public static boolean isLocalScope(final String scope) {
    return scope == null || scope.isEmpty() || scope.equals(ATTR_SCOPE_VALUE_LOCAL);
  }

  public static boolean isNormalProcessRole(final Element elem) {
    final String processingRole = elem.getAttribute(ATTRIBUTE_NAME_PROCESSING_ROLE);
    return !processingRole.equals(ATTR_PROCESSING_ROLE_VALUE_RESOURCE_ONLY);
  }

  public static Float getDitaVersion(Element topic) {
    final String ditaVersion = topic.getAttributeNS(DITA_NAMESPACE, ATTRIBUTE_NAME_DITAARCHVERSION);
    if (!ditaVersion.isEmpty()) {
      try {
        return Float.valueOf(ditaVersion);
      } catch (IllegalArgumentException e) {
        // Ignore
      }
    }
    return null;
  }
}
