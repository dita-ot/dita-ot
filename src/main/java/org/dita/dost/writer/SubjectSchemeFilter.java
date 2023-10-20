/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2023 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.writer;

import static org.dita.dost.reader.SubjectSchemeReader.ANY_ELEMENT;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import javax.xml.namespace.QName;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.util.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class SubjectSchemeFilter extends AbstractXMLFilter {

  private static Pattern WHITESPACE = Pattern.compile("\\s+");

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
    var res = addDefaultAttributeValues(qName, atts);
    validateAttributeValues(qName, res);

    super.startElement(uri, localName, qName, res);
  }

  /**
   * Add default attribute values from subject scheme.
   */
  private Attributes addDefaultAttributeValues(final String elemName, final Attributes atts) {
    AttributesImpl modified = null;
    for (Map.Entry<QName, Map<String, String>> elementDefaultValues : defaultValueMap.entrySet()) {
      var attrName = elementDefaultValues.getKey();
      if (atts.getValue(attrName.getNamespaceURI(), attrName.getLocalPart()) == null) {
        var defaultValue = elementDefaultValues.getValue().get(elemName);
        if (defaultValue != null) {
          if (modified == null) {
            modified = new AttributesImpl(atts);
          }
          modified.addAttribute(attrName.getNamespaceURI(), attrName.getLocalPart(), null, "CDATA", defaultValue);
        }
      }
    }
    return Objects.requireNonNullElse(modified, atts);
  }

  /**
   * Validate attribute values against subject scheme.
   */
  private void validateAttributeValues(final String elemName, final Attributes atts) {
    for (int i = 0; i < atts.getLength(); i++) {
      var attrName = new QName(atts.getURI(i), atts.getLocalName(i));
      var valueMap = validateMap.get(attrName);
      if (valueMap != null) {
        var valueSet = valueMap.get(elemName);
        if (valueSet == null) {
          valueSet = valueMap.get(ANY_ELEMENT);
        }
        if (valueSet != null) {
          var attrValue = atts.getValue(i);
          var tokens = WHITESPACE.split(attrValue.trim());
          for (final String token : tokens) {
            if (!valueSet.contains(token)) {
              logger.warn(
                MessageUtils
                  .getMessage("DOTJ049W", attrName.toString(), elemName, attrValue, StringUtils.join(valueSet, ", "))
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
