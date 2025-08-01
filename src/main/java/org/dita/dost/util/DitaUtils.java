/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2022 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.util;

import static net.sf.saxon.s9api.streams.Predicates.attributeEq;
import static net.sf.saxon.s9api.streams.Steps.ancestorOrSelf;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.XMLUtils.getCascadeValue;

import java.util.Objects;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.streams.Predicates;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

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
    return isLocalScope(getCascadeValue(elem, ATTRIBUTE_NAME_SCOPE));
  }

  public static boolean isLocalScope(final String scope) {
    return scope == null || scope.isEmpty() || scope.equals(ATTR_SCOPE_VALUE_LOCAL);
  }

  public static boolean isExternalScope(final String scope) {
    return Objects.equals(scope, ATTR_SCOPE_VALUE_EXTERNAL);
  }

  public static boolean isExternalScope(final Element elem) {
    return isExternalScope(getCascadeValue(elem, ATTRIBUTE_NAME_SCOPE));
  }

  public static boolean isNormalProcessRole(final Element elem) {
    return isNormalProcessRole(getCascadeValue(elem, ATTRIBUTE_NAME_PROCESSING_ROLE));
  }

  public static boolean isNormalProcessRole(final String processingRole) {
    return processingRole == null || processingRole.equals(ATTR_PROCESSING_ROLE_VALUE_NORMAL);
  }

  public static boolean isResourceOnly(final String processingRole) {
    return Objects.equals(processingRole, ATTR_PROCESSING_ROLE_VALUE_RESOURCE_ONLY);
  }

  public static boolean isResourceOnly(final XdmNode elem) {
    return elem
      .select(
        ancestorOrSelf(Predicates.hasAttribute(ATTRIBUTE_NAME_PROCESSING_ROLE))
          .first()
          .where(attributeEq(ATTRIBUTE_NAME_PROCESSING_ROLE, ATTR_PROCESSING_ROLE_VALUE_RESOURCE_ONLY))
      )
      .exists();
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
