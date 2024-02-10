/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2023 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.writer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.Set;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import org.dita.dost.TestUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;

class SubjectSchemeFilterTest {

  @ParameterizedTest
  @MethodSource({ "validateMap", "defaultValueMap" })
  void startElement(
    Map<String, String> src,
    Map<String, String> exp,
    Map<QName, Map<String, Set<String>>> validateMap,
    Map<QName, Map<String, String>> defaultValueMap,
    int expMsgCount
  ) throws SAXException {
    var filter = new SubjectSchemeFilter();
    var logger = new TestUtils.CachingLogger();
    filter.setLogger(logger);
    filter.setValidateMap(validateMap);
    filter.setDefaultValueMap(defaultValueMap);
    filter.setContentHandler(
      new XMLFilterImpl() {
        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
          assertAttributesEquals(attributes(exp), atts);
        }
      }
    );
    filter.startElement(XMLConstants.NULL_NS_URI, "p", "p", attributes(src));
    assertEquals(expMsgCount, logger.getMessages().size());
  }

  private static Arguments[] validateMap() {
    return new Arguments[] {
      Arguments.of(
        Map.of("audience", "novice"),
        Map.of("audience", "novice"),
        Map.of(new QName("audience"), Map.of("p", Set.of("expert"))),
        Map.of(),
        1
      ),
      Arguments.of(
        Map.of("audience", "novice intermediate"),
        Map.of("audience", "novice intermediate"),
        Map.of(new QName("audience"), Map.of("p", Set.of("expert"))),
        Map.of(),
        2
      ),
      Arguments.of(
        Map.of("audience", "novice"),
        Map.of("audience", "novice"),
        Map.of(new QName("audience"), Map.of("div", Set.of("expert"))),
        Map.of(),
        0
      ),
      Arguments.of(
        Map.of("audience", "novice"),
        Map.of("audience", "novice"),
        Map.of(new QName("audience"), Map.of("p", Set.of("novice"))),
        Map.of(),
        0
      ),
      Arguments.of(
        Map.of("audience", "novice intermediate"),
        Map.of("audience", "novice intermediate"),
        Map.of(new QName("audience"), Map.of("p", Set.of("novice", "intermediate"))),
        Map.of(),
        0
      ),
    };
  }

  private static Arguments[] defaultValueMap() {
    return new Arguments[] {
      Arguments.of(
        Map.of("audience", "novice"),
        Map.of("audience", "novice", "platform", "linux"),
        Map.of(),
        Map.of(new QName("platform"), Map.of("p", "linux")),
        0
      ),
      Arguments.of(
        Map.of("audience", "novice"),
        Map.of("audience", "novice"),
        Map.of(),
        Map.of(new QName("platform"), Map.of("div", "linux")),
        0
      ),
      Arguments.of(
        Map.of("audience", "novice"),
        Map.of("audience", "novice"),
        Map.of(),
        Map.of(new QName("audience"), Map.of("p", "expert")),
        0
      ),
    };
  }

  private static Attributes attributes(Map<String, String> atts) {
    var res = new AttributesImpl();
    atts.forEach((name, value) -> {
      res.addAttribute(XMLConstants.NULL_NS_URI, name, name, "CDATA", value);
    });
    return res;
  }

  private void assertAttributesEquals(Attributes exp, Attributes act) {
    assertEquals(exp.getLength(), act.getLength());
    for (int i = 0; i < exp.getLength(); i++) {
      assertEquals(exp.getValue(i), act.getValue(exp.getURI(i), exp.getLocalName(i)));
    }
  }
}
