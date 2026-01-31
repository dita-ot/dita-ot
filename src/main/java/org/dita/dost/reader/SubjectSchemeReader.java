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
import java.util.stream.Collectors;
import javax.xml.namespace.QName;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.streams.Steps;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.module.filter.SubjectScheme;
import org.dita.dost.module.filter.SubjectScheme.SubjectDefinition;
import org.dita.dost.util.Job;
import org.dita.dost.util.StringUtils;

/**
 * Subject scheme reader.
 *
 * @since 1.8
 */
public class SubjectSchemeReader {

  public static final String ANY_ELEMENT = "*";
  private static final net.sf.saxon.s9api.QName ATTRIBUTE_QNAME_KEYREF = net.sf.saxon.s9api.QName.fromClarkName(
    ATTRIBUTE_NAME_KEYREF
  );
  private static final net.sf.saxon.s9api.QName ATTRIBUTE_QNAME_KEYS = net.sf.saxon.s9api.QName.fromClarkName(
    ATTRIBUTE_NAME_KEYS
  );
  private static final net.sf.saxon.s9api.QName ATTRIBUTE_QNAME_NAME = net.sf.saxon.s9api.QName.fromClarkName(
    ATTRIBUTE_NAME_NAME
  );

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
      final String value = entry.getValue().stream().map(URI::toString).collect(Collectors.joining(COMMA));
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
      final XdmNode doc = job.getStore().getImmutableNode(scheme.toURI());
      final XdmNode schemeRoot = doc.getOutermostElement();
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

  public void loadSubjectScheme(final XdmNode schemeRoot) {
    final Map<String, SubjectDefinition> subjectDefinitionsByKey = getSubjectDefinition(schemeRoot);
    for (XdmNode child : schemeRoot.children(SUBJECTSCHEME_ENUMERATIONDEF::matches)) {
      processEnumerationDef(subjectDefinitionsByKey, child);
    }
  }

  public Map<String, SubjectDefinition> getSubjectDefinition(final XdmNode schemeRoot) {
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

  private List<SubjectDefinition> readSubjectDefinitions(final XdmNode elem) {
    final List<SubjectDefinition> res = new ArrayList<>();
    readSubjectDefinitions(elem, res);
    return Collections.unmodifiableList(res);
  }

  private void readSubjectDefinitions(final XdmNode elem, List<SubjectDefinition> buf) {
    for (XdmNode child : elem.children()) {
      if (SUBJECTSCHEME_SUBJECTDEF.matches(child)) {
        String keyref = child.getAttributeValue(ATTRIBUTE_QNAME_KEYREF);
        if (keyref == null || keyref.isEmpty()) {
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
    final XdmNode enumerationDef
  ) {
    final String elementName = enumerationDef
      .select(
        Steps
          .child(SUBJECTSCHEME_ELEMENTDEF::matches)
          .then(Steps.attribute(ATTRIBUTE_NAME_NAME).where(Predicate.not(isEmptyAttribute())))
      )
      .findFirst()
      .map(XdmItem::getStringValue)
      .orElse(ANY_ELEMENT);

    final Optional<XdmNode> attributeDefElement = enumerationDef
      .select(Steps.child(SUBJECTSCHEME_ATTRIBUTEDEF::matches).first())
      .findFirst();
    final QName attributeName = attributeDefElement
      .map(child -> child.getAttributeValue(ATTRIBUTE_QNAME_NAME))
      .filter(name -> name != null && !name.isEmpty())
      .map(QName::valueOf)
      .orElse(null);
    if (attributeDefElement.isPresent()) {
      bindingMap.computeIfAbsent(attributeName, k -> new HashMap<>());
    }

    enumerationDef
      .select(Steps.child(SUBJECTSCHEME_DEFAULTSUBJECT::matches))
      .map(child -> child.getAttributeValue(ATTRIBUTE_QNAME_KEYREF))
      .filter(keyref -> keyref != null && !keyref.isEmpty())
      .findFirst()
      .ifPresent(keyValue -> {
        final Map<String, String> S = defaultValueMap.getOrDefault(attributeName, new HashMap<>());
        S.put(elementName, keyValue);
        defaultValueMap.put(attributeName, S);
      });

    for (XdmNode child : enumerationDef.children(SUBJECTSCHEME_SUBJECTDEF::matches)) {
      final List<String> keyValues = Optional
        .ofNullable(child.getAttributeValue(ATTRIBUTE_QNAME_KEYREF))
        .filter(Predicate.not(String::isBlank))
        .or(() -> Optional.ofNullable(child.getAttributeValue(ATTRIBUTE_QNAME_KEYS)))
        .map(String::trim)
        .filter(Predicate.not(String::isEmpty))
        .map(value -> Arrays.asList(value.split("\\s+")))
        .orElse(List.of());
      if (!subjectDefinitions.isEmpty() && !keyValues.isEmpty()) {
        for (String keyValue : keyValues) {
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

  private static Predicate<XdmNode> isEmptyAttribute() {
    return attr -> attr.getStringValue().isEmpty();
  }

  private static List<String> getKeyValues(XdmNode child) {
    var value = child.getAttributeValue(ATTRIBUTE_QNAME_KEYS);
    if (value == null) {
      return List.of();
    }
    value = value.trim();
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
