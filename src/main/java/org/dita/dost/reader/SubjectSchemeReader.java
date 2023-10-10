/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2013 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.reader;

import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.URLUtils.toURI;

import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.function.Predicate;
import javax.xml.namespace.QName;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.module.filter.SubjectScheme;
import org.dita.dost.module.filter.SubjectScheme.SubjectDefinition;
import org.dita.dost.util.Job;
import org.dita.dost.util.StringUtils;
import org.dita.dost.util.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Subject scheme reader.
 *
 * @since 1.8
 */
public class SubjectSchemeReader {

  private DITAOTLogger logger;
  private Job job;
  private final Map<QName, Map<String, Set<SubjectDefinition>>> bindingMap;
  private final Map<QName, Map<String, Set<String>>> validValuesMap;
  private final Map<QName, Map<String, String>> defaultValueMap;

  public SubjectSchemeReader() {
    validValuesMap = new HashMap<>();
    defaultValueMap = new HashMap<>();
    bindingMap = new HashMap<>();
  }

  /**
   * reset.
   */
  public void reset() {
    validValuesMap.clear();
    defaultValueMap.clear();
    bindingMap.clear();
  }

  /**
   * Get map of valid attribute values based on subject scheme. The
   * contents of the map is in pseudo-code
   * {@code Map<AttName, Map<ElemName, <Set<Value>>>}. For default element
   * mapping, the value is {@code *}.
   *
   * @return valid attribute values or empty map
   */
  public Map<QName, Map<String, Set<String>>> getValidValuesMap() {
    return validValuesMap;
  }

  /**
   * Get map of default values based on subject scheme. The
   * contents of the map is in pseudo-code
   * {@code Map<AttName, Map<ElemName, Default>>}. For default element
   * mapping, the value is {@code *}.
   *
   * @return default values or empty map
   */
  public Map<QName, Map<String, String>> getDefaultValueMap() {
    return defaultValueMap;
  }

  /**
   * Get map subject scheme definitions. The
   * contents of the map is in pseudo-code
   * {@code Map<AttName, Map<ElemName, Set<Element>>>}. For default element
   * mapping, the value is {@code *}.
   *
   * @return subject scheme definitions
   */
  public SubjectScheme getSubjectSchemeMap() {
    return new SubjectScheme(bindingMap);
  }

  public void setLogger(final DITAOTLogger logger) {
    this.logger = logger;
  }

  public void setJob(final Job job) {
    this.job = job;
  }

  /**
   * Read a map from XML properties file. Values are split by {@link org.dita.dost.util.Constants#COMMA COMMA} into a set.
   *
   * @param inputFile XML properties file absolute path
   */
  public Map<URI, Set<URI>> readMapFromXML(final File inputFile) throws IOException {
    final Map<URI, Set<URI>> graph = new HashMap<>();
    if (!inputFile.exists()) {
      return Collections.emptyMap();
    }
    final Properties prop = new Properties();
    try (InputStream in = new BufferedInputStream(job.getStore().getInputStream(inputFile.toURI()))) {
      prop.loadFromXML(in);
    } catch (final IOException e) {
      throw new IOException("Failed to read subject scheme graph: " + e.getMessage(), e);
    }

    for (final Map.Entry<Object, Object> entry : prop.entrySet()) {
      final String key = (String) entry.getKey();
      final String value = (String) entry.getValue();
      final Set<URI> r = new HashSet<>();
      for (final String v : StringUtils.restoreSet(value, COMMA)) {
        r.add(toURI(v));
      }
      graph.put(toURI(key), r);
    }

    return Collections.unmodifiableMap(graph);
  }

  /**
   * Write map of sets to a file.
   *
   * <p>The serialization format is XML properties format where values are comma
   * separated lists.</p>
   *
   * @param m          map to serialize
   * @param outputFile output filename, relative to temporary directory
   */
  public void writeMapToXML(final Map<URI, Set<URI>> m, final File outputFile) throws IOException {
    if (m == null) {
      return;
    }
    final Properties prop = new Properties();
    for (final Map.Entry<URI, Set<URI>> entry : m.entrySet()) {
      final URI key = entry.getKey();
      final String value = StringUtils.join(entry.getValue(), COMMA);
      prop.setProperty(key.getPath(), value);
    }
    try (OutputStream os = job.getStore().getOutputStream(outputFile.toURI())) {
      prop.storeToXML(os, null);
    } catch (final IOException e) {
      throw new IOException("Failed to write subject scheme graph: " + e.getMessage(), e);
    }
  }

  /**
   * Load schema file.
   *
   * @param scheme absolute path for subject scheme
   */
  public void loadSubjectScheme(final File scheme) {
    assert scheme.isAbsolute();
    if (!job.getStore().exists(scheme.toURI())) {
      throw new IllegalStateException();
    }
    logger.debug("Load subject scheme " + scheme);

    try {
      final Document doc = job.getStore().getDocument(scheme.toURI());
      final Element schemeRoot = doc.getDocumentElement();
      if (schemeRoot == null) {
        return;
      }
      loadSubjectScheme(schemeRoot);
    } catch (final RuntimeException e) {
      throw e;
    } catch (final Exception e) {
      logger.error(e.getMessage(), e);
    }
  }

  public void loadSubjectScheme(final Element schemeRoot) {
    final Map<String, SubjectDefinition> subjectDefinitionsByKey = getSubjectDefinition(schemeRoot);
    for (Element child : XMLUtils.getChildElements(schemeRoot, SUBJECTSCHEME_ENUMERATIONDEF)) {
      processEnumerationDef(subjectDefinitionsByKey, child);
    }
  }

  public Map<String, SubjectDefinition> getSubjectDefinition(final Element schemeRoot) {
    final List<SubjectDefinition> subjectDefinitions = readSubjectDefinitions(schemeRoot);
    final Map<String, SubjectDefinition> buf = new HashMap<>();
    getSubjectDefinition(subjectDefinitions, buf);
    return Collections.unmodifiableMap(buf);
  }

  private void getSubjectDefinition(List<SubjectDefinition> subjectDefinitions, Map<String, SubjectDefinition> buf) {
    for (SubjectDefinition subjectDefinition : subjectDefinitions) {
      for (String key : subjectDefinition.keys()) {
        buf.putIfAbsent(key, subjectDefinition);
      }
      getSubjectDefinition(subjectDefinition.children(), buf);
    }
  }

  private List<SubjectDefinition> readSubjectDefinitions(final Element elem) {
    final List<SubjectDefinition> res = new ArrayList<>();
    readSubjectDefinitions(elem, res);
    return Collections.unmodifiableList(res);
  }

  private void readSubjectDefinitions(final Element elem, List<SubjectDefinition> buf) {
    for (Element child : XMLUtils.getChildElements(elem)) {
      if (SUBJECTSCHEME_SUBJECTDEF.matches(child)) {
        String keyref = child.getAttribute(ATTRIBUTE_NAME_KEYREF);
        if (keyref.isEmpty()) {
          keyref = null;
        }
        final List<SubjectDefinition> childBuf = new ArrayList<>();
        readSubjectDefinitions(child, childBuf);
        final SubjectDefinition res = new SubjectDefinition(Set.copyOf(getKeyValues(child)), keyref, childBuf);
        buf.add(res);
      } else {
        readSubjectDefinitions(child, buf);
      }
    }
  }

  public void processEnumerationDef(
    final Map<String, SubjectDefinition> subjectDefinitions,
    final Element enumerationDef
  ) {
    final String elementName = XMLUtils
      .getChildElement(enumerationDef, SUBJECTSCHEME_ELEMENTDEF)
      .map(child -> child.getAttribute(ATTRIBUTE_NAME_NAME))
      .filter(Predicate.not(String::isEmpty))
      .orElse("*");

    final Optional<Element> attributeDefElement = XMLUtils.getChildElement(enumerationDef, SUBJECTSCHEME_ATTRIBUTEDEF);
    final QName attributeName = attributeDefElement
      .map(child -> child.getAttribute(ATTRIBUTE_NAME_NAME))
      .filter(Predicate.not(String::isEmpty))
      .map(QName::valueOf)
      .orElse(null);
    if (attributeDefElement.isPresent()) {
      bindingMap.computeIfAbsent(attributeName, k -> new HashMap<>());
    }

    XMLUtils
      .getChildElement(enumerationDef, SUBJECTSCHEME_DEFAULTSUBJECT)
      .map(child -> child.getAttribute(ATTRIBUTE_NAME_KEYREF))
      .filter(Predicate.not(String::isEmpty))
      .ifPresent(keyValue -> {
        final Map<String, String> S = defaultValueMap.getOrDefault(attributeName, new HashMap<>());
        S.put(elementName, keyValue);
        defaultValueMap.put(attributeName, S);
      });

    for (Element child : XMLUtils.getChildElements(enumerationDef, SUBJECTSCHEME_SUBJECTDEF)) {
      final List<String> keyValues = Optional
        .of(child.getAttribute(ATTRIBUTE_NAME_KEYREF))
        .filter(Predicate.not(String::isBlank))
        .or(() -> Optional.of(child.getAttribute(ATTRIBUTE_NAME_KEYS)))
        .map(String::trim)
        .filter(Predicate.not(String::isEmpty))
        .map(value -> Arrays.asList(value.split("\\s+")))
        .orElse(List.of());
      if (!subjectDefinitions.isEmpty() && !keyValues.isEmpty()) {
        for (String keyValue : keyValues) {
          //          final Element subTree = searchForKey(subjectDefinitions, keyValue);
          final SubjectDefinition subTree = subjectDefinitions.get(keyValue);
          if (subTree != null) {
            final Map<String, Set<SubjectDefinition>> S = bindingMap.getOrDefault(attributeName, new HashMap<>());
            final Set<SubjectDefinition> A = S.getOrDefault(elementName, new HashSet<>());
            if (!A.contains(subTree)) {
              if (attributeName != null) {
                putValuePairsIntoMap(subTree, elementName, attributeName, keyValue);
              }
            }
            A.add(subTree);
            S.put(elementName, A);
            bindingMap.put(attributeName, S);
          }
        }
      }
    }
  }

  private static List<String> getKeyValues(Element child) {
    var value = child.getAttribute(ATTRIBUTE_NAME_KEYS).trim();
    if (value.isEmpty()) {
      return List.of();
    }
    return Arrays.asList(value.split("\\s+"));
  }

  /**
   * Populate valid values map
   *
   * @param subtree     subject scheme definition element
   * @param elementName element name
   * @param attName     attribute name
   * @param category    enumeration category name
   */
  private void putValuePairsIntoMap(
    final SubjectDefinition subtree,
    final String elementName,
    final QName attName,
    final String category
  ) {
    final Map<String, Set<String>> valueMap = validValuesMap.getOrDefault(attName, new HashMap<>());
    final Set<String> valueSet = valueMap.getOrDefault(elementName, new HashSet<>());
    subtree
      .flatten()
      .stream()
      .flatMap(child -> child.keys().stream())
      .filter(key -> !key.equals(category))
      .forEach(valueSet::add);
    valueMap.put(elementName, valueSet);
    validValuesMap.put(attName, valueMap);
  }
}
