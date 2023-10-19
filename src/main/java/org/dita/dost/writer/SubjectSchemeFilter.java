/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2023 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.writer;

import static org.dita.dost.util.Constants.COMMA;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.xml.namespace.QName;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.util.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class SubjectSchemeFilter extends AbstractXMLFilter {

  private Map<QName, Map<String, String>> defaultValueMap = null;
  private Map<QName, Map<String, Set<String>>> validateMap = null;

  /**
   * Set valid attribute values.
   *
   * <p>The contents of the map is in pseudo-code
   * {@code Map<AttName, Map<ElemName, <Set<Value>>>}.
   * For default element mapping, the value is {@code *}.
   */
  public void setValidateMap(final Map<QName, Map<String, Set<String>>> validateMap) {
    this.validateMap = validateMap;
  }

  /**
   * Set default value map.
   * @param defaultMap default value map
   */
  public void setDefaultValueMap(final Map<QName, Map<String, String>> defaultMap) {
    defaultValueMap = defaultMap;
  }

  @Override
  public void startElement(final String uri, final String localName, final String qName, final Attributes atts)
    throws SAXException {
    Attributes res = atts;
    validateAttributeValues(qName, res);
    res = addDefaultAttributeValues(qName, res);

    super.startElement(uri, localName, qName, res);
  }

  private Attributes addDefaultAttributeValues(final String qName, final Attributes atts) {
    AttributesImpl modified = null;
    for (Map.Entry<QName, Map<String, String>> elementDefaultValues : defaultValueMap.entrySet()) {
      final QName attrName = elementDefaultValues.getKey();
      final int index = atts.getIndex(attrName.getNamespaceURI(), attrName.getLocalPart());
      if (index == -1) {
        final String defaultValue = elementDefaultValues.getValue().get(qName);
        if (defaultValue != null) {
          if (modified == null) {
            modified = new AttributesImpl(atts);
          }
          modified.addAttribute(attrName.getNamespaceURI(), attrName.getLocalPart(), null, "CDATA", defaultValue);
        }
      }
    }
    return Objects.requireNonNullElse(atts, modified);
  }

  /**
   * Validate attribute values
   *
   * @param qName element name
   * @param atts attributes
   */
  private void validateAttributeValues(final String qName, final Attributes atts) {
    for (int i = 0; i < atts.getLength(); i++) {
      final QName attrName = new QName(atts.getURI(i), atts.getLocalName(i));
      final Map<String, Set<String>> valueMap = validateMap.get(attrName);
      if (valueMap != null) {
        Set<String> valueSet = valueMap.get(qName);
        if (valueSet == null) {
          valueSet = valueMap.get("*");
        }
        if (valueSet != null) {
          final String attrValue = atts.getValue(i);
          final String[] keylist = attrValue.trim().split("\\s+");
          for (final String s : keylist) {
            if (!StringUtils.isEmptyString(s) && !valueSet.contains(s)) {
              logger.warn(
                MessageUtils
                  .getMessage("DOTJ049W", attrName.toString(), qName, attrValue, StringUtils.join(valueSet, COMMA))
                  .setLocation(atts)
                  .toString()
              );
            }
          }
        }
      }
    }
  }
}
