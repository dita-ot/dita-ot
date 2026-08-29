/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2007 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.

 */
package org.dita.dost.reader;

import static org.dita.dost.module.GenMapAndTopicListModule.ELEMENT_STUB;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.DitaUtils.isLocalScope;
import static org.dita.dost.util.XMLUtils.getCascadeValue;

import java.io.File;
import java.net.URI;
import java.util.*;
import java.util.Map.Entry;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.util.URLUtils;
import org.dita.dost.util.XMLUtils;
import org.dita.dost.writer.AbstractDomFilter;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Cascade map metadata to child topic references and collect metadata for topics.
 */
public final class MapMetaReader extends AbstractDomFilter {

  /**
   * Cascaded metadata. Contents <topic relative URI, <class matcher, cascading metadata elements>>.
   */
  private final Map<URI, Map<String, Element>> resultTable = new HashMap<>();

  private static final Set<String> uniqueSet = Set.of(
    TOPIC_CRITDATES.matcher,
    TOPIC_PERMISSIONS.matcher,
    TOPIC_PUBLISHER.matcher,
    TOPIC_SOURCE.matcher,
    MAP_SEARCHTITLE.matcher,
    TOPIC_SEARCHTITLE.matcher
  );
  private static final Set<String> cascadeSet = Set.of(
    TOPIC_AUDIENCE.matcher,
    TOPIC_AUTHOR.matcher,
    TOPIC_CATEGORY.matcher,
    TOPIC_COPYRIGHT.matcher,
    TOPIC_CRITDATES.matcher,
    TOPIC_METADATA.matcher,
    TOPIC_PERMISSIONS.matcher,
    TOPIC_PRODINFO.matcher,
    TOPIC_PUBLISHER.matcher
  );
  private static final Set<String> metaSet = Set.of(
    MAP_SEARCHTITLE.matcher,
    TOPIC_SEARCHTITLE.matcher,
    TOPIC_AUTHOR.matcher,
    TOPIC_SOURCE.matcher,
    TOPIC_PUBLISHER.matcher,
    TOPIC_COPYRIGHT.matcher,
    TOPIC_CRITDATES.matcher,
    TOPIC_PERMISSIONS.matcher,
    TOPIC_AUDIENCE.matcher,
    TOPIC_CATEGORY.matcher,
    TOPIC_KEYWORDS.matcher,
    TOPIC_PRODINFO.matcher,
    TOPIC_OTHERMETA.matcher,
    TOPIC_RESOURCEID.matcher,
    TOPIC_DATA.matcher,
    TOPIC_DATA_ABOUT.matcher,
    TOPIC_FOREIGN.matcher,
    TOPIC_UNKNOWN.matcher
  );
  private static final List<String> metaPos = List.of(
    MAP_SEARCHTITLE.matcher,
    TOPIC_SEARCHTITLE.matcher,
    TOPIC_AUTHOR.matcher,
    TOPIC_SOURCE.matcher,
    TOPIC_PUBLISHER.matcher,
    TOPIC_COPYRIGHT.matcher,
    TOPIC_CRITDATES.matcher,
    TOPIC_PERMISSIONS.matcher,
    TOPIC_AUDIENCE.matcher,
    TOPIC_CATEGORY.matcher,
    TOPIC_KEYWORDS.matcher,
    TOPIC_PRODINFO.matcher,
    TOPIC_OTHERMETA.matcher,
    TOPIC_RESOURCEID.matcher,
    TOPIC_DATA.matcher,
    TOPIC_DATA_ABOUT.matcher,
    TOPIC_FOREIGN.matcher,
    TOPIC_UNKNOWN.matcher,
    MAP_LINKTEXT.matcher,
    TOPIC_LINKTEXT.matcher,
    TOPIC_TITLEALT.matcher,
    MAP_SHORTDESC.matcher,
    TOPIC_SHORTDESC.matcher,
    TOPIC_NAVTITLE.matcher,
    TOPIC_METADATA.matcher
  );

  private final Map<String, Element> globalMeta;
  /** Current document. */
  private Document doc = null;
  /** Result metadata document. */
  private Document resultDoc;
  /** Current file. */
  private URI filePath;

  /**
   * Constructor.
   */
  public MapMetaReader() {
    super();
    globalMeta = new HashMap<>(16);
  }

  public void setXmlUtils(XMLUtils xmlUtils) {
    resultDoc = xmlUtils.newDocument();
  }

  /**
   * read map files.
   * @param filename filename
   */
  @Override
  public void read(final File filename) throws DITAOTException {
    filePath = filename.toURI();

    //clear the history on global metadata table
    globalMeta.clear();
    super.read(filename);
  }

  @Override
  public Document process(final Document doc) {
    this.doc = doc;

    for (var elem : XMLUtils.getChildElements(doc.getDocumentElement(), MAP_TOPICMETA)) {
      handleGlobalMeta(elem);
    }
    for (var elem : XMLUtils.getChildElements(doc.getDocumentElement(), MAP_TOPICREF)) {
      handleTopicref(elem, globalMeta);
    }
    for (Element keyword : XMLUtils.getChildElements(doc.getDocumentElement(), TOPIC_KEYWORDS, true)) {
      removeIndexTermRecursive(keyword);
    }
    for (var elem : XMLUtils.getChildElements(doc.getDocumentElement(), MAP_TOPICREF)) {
      collectTopicrefs(elem, elem.getAttributeNode(ATTRIBUTE_NAME_CHUNK));
    }

    return doc;
  }

  private void collectTopicrefs(final Element topicref, Attr chunkAttr) {
    final URI hrefAttr = Optional
      .ofNullable(topicref.getAttributeNode(ATTRIBUTE_NAME_HREF))
      .map(Node::getNodeValue)
      .map(URLUtils::toURI)
      .orElse(null);
    final String scopeAttr = getCascadeValue(topicref, ATTRIBUTE_NAME_SCOPE);
    final Attr formatAttr = topicref.getAttributeNode(ATTRIBUTE_NAME_FORMAT);
    Map<String, Element> current = Collections.emptyMap();
    final boolean hasDitaTopicTarget = hrefAttr != null && isLocalScope(scopeAttr) && isDitaFormat(formatAttr);

    final Attr copyToAttr = topicref.getAttributeNode(ATTRIBUTE_NAME_COPY_TO);
    if (chunkAttr == null || !chunkAttr.getNodeValue().equals("to-content")) {
      chunkAttr = topicref.getAttributeNode(ATTRIBUTE_NAME_CHUNK);
    }
    final boolean isCopiedInChunk =
      copyToAttr != null && (chunkAttr != null && chunkAttr.getNodeValue().equals("to-content"));

    for (Element elem : XMLUtils.getChildElements(topicref, MAP_TOPICREF)) {
      collectTopicrefs(elem, chunkAttr);
    }

    if (hasDitaTopicTarget && !isCopiedInChunk) {
      for (Element elem : XMLUtils.getChildElements(topicref, MAP_TOPICMETA)) {
        current = handleMeta(elem, Collections.emptyMap());
      }
    }

    if (!current.isEmpty() && hasDitaTopicTarget && !isCopiedInChunk) {
      final URI copytoAttr = Optional
        .ofNullable(topicref.getAttributeNode(ATTRIBUTE_NAME_COPY_TO))
        .map(Attr::getNodeValue)
        .map(URLUtils::toURI)
        .map(URLUtils::stripFragment)
        .orElse(null);
      final URI rel = Objects.requireNonNullElse(copytoAttr, hrefAttr);
      final URI topicPath = job.tempDirURI.relativize(filePath.resolve(rel));
      if (resultTable.containsKey(topicPath)) {
        //if the result table already contains some result
        //metadata for current topic path.
        final Map<String, Element> previous = resultTable.get(topicPath);
        resultTable.put(topicPath, mergeMeta(previous, current, metaSet));
      } else {
        resultTable.put(topicPath, cloneElementMap(current));
      }
    }
  }

  /**
   * traverse the node tree and remove all indexterm elements with either start or
   * end attribute.
   * @param parent root element
   */
  private void removeIndexTermRecursive(final Element parent) {
    if (parent == null) {
      return;
    }
    for (var child : XMLUtils.getChildElements(parent)) {
      final boolean isIndexTerm = TOPIC_INDEXTERM.matches(child);
      final boolean hasStart = !child.getAttribute(ATTRIBUTE_NAME_START).isEmpty();
      final boolean hasEnd = !child.getAttribute(ATTRIBUTE_NAME_END).isEmpty();
      if (isIndexTerm && (hasStart || hasEnd)) {
        parent.removeChild(child);
      } else {
        removeIndexTermRecursive(child);
      }
    }
  }

  private void handleTopicref(final Element topicref, final Map<String, Element> inheritance) {
    final URI hrefAttr = Optional
      .ofNullable(topicref.getAttributeNode(ATTRIBUTE_NAME_HREF))
      .map(Node::getNodeValue)
      .map(URLUtils::toURI)
      .orElse(null);
    final String scopeAttr = getCascadeValue(topicref, ATTRIBUTE_NAME_SCOPE);
    final Attr formatAttr = topicref.getAttributeNode(ATTRIBUTE_NAME_FORMAT);
    Map<String, Element> current = mergeMeta(Collections.emptyMap(), inheritance, cascadeSet);
    final boolean hasDitaTopicTarget = hrefAttr != null && isLocalScope(scopeAttr) && isDitaFormat(formatAttr);

    if (hasDitaTopicTarget) {
      for (Element elem : XMLUtils.getChildElements(topicref, MAP_TOPICMETA)) {
        current = handleMeta(elem, inheritance);
      }
    }

    for (Element elem : XMLUtils.getChildElements(topicref, MAP_TOPICREF)) {
      handleTopicref(elem, current);
    }

    if (!current.isEmpty() && hasDitaTopicTarget) {
      final Map<String, Element> metas = cloneElementMap(current);
      if (!metas.isEmpty()) {
        XMLUtils.getChildElement(topicref, MAP_TOPICMETA).ifPresent(topicref::removeChild);
        final Element newMeta = doc.createElement(MAP_TOPICMETA.localName);
        newMeta.setAttribute(ATTRIBUTE_NAME_CLASS, "-" + MAP_TOPICMETA.matcher);
        for (String metaPo : metaPos) {
          final Node stub = metas.get(metaPo);
          if (stub != null) {
            for (var child : XMLUtils.toList(stub.getChildNodes())) {
              final Node copy = topicref.getOwnerDocument().importNode(child, true);
              newMeta.appendChild(copy);
            }
          }
        }
        topicref.insertBefore(newMeta, topicref.getFirstChild());
      }
    }
  }

  private boolean isDitaFormat(final Attr formatAttr) {
    return (
      formatAttr == null ||
      ATTR_FORMAT_VALUE_DITA.equals(formatAttr.getNodeValue()) ||
      ATTR_FORMAT_VALUE_DITAMAP.equals(formatAttr.getNodeValue())
    );
  }

  /**
   * Clone metadata map.
   *
   * @param current metadata map to clone
   * @return a clone of the original map
   */
  private Map<String, Element> cloneElementMap(final Map<String, Element> current) {
    final Map<String, Element> topicMetaTable = new HashMap<>(16);
    for (final Entry<String, Element> topicMetaItem : current.entrySet()) {
      final Element copy = (Element) resultDoc.importNode(topicMetaItem.getValue(), true);
      topicMetaTable.put(topicMetaItem.getKey(), copy);
    }
    return topicMetaTable;
  }

  private Map<String, Element> handleMeta(final Element meta, final Map<String, Element> inheritance) {
    final Map<String, Element> topicMetaTable = new HashMap<>(16);
    getMeta(meta, topicMetaTable);
    return mergeMeta(topicMetaTable, inheritance, cascadeSet);
  }

  private void getMeta(final Element meta, final Map<String, Element> topicMetaTable) {
    for (var elem : XMLUtils.getChildElements(meta)) {
      final String classValue = elem.getAttribute(ATTRIBUTE_NAME_CLASS);
      // int number 1 is used to remove the first "-" or "+" character in class attribute
      final String metaKey = classValue.substring(1, classValue.indexOf(STRING_BLANK, classValue.indexOf(SLASH)) + 1);
      if (TOPIC_METADATA.matches(classValue)) {
        getMeta(elem, topicMetaTable);
      } else if (topicMetaTable.containsKey(metaKey)) {
        //append node to the list if it exists in topic meta table
        final Node copy = resultDoc.importNode(elem, true);
        topicMetaTable.get(metaKey).appendChild(copy);
      } else {
        if (TOPIC_NAVTITLE.matches(classValue)) {
          //Add locktitle value to navtitle so we know whether it should be pushed to topics
          final String locktitleAttr = Optional
            .ofNullable(((Element) meta.getParentNode()).getAttributeNode(ATTRIBUTE_NAME_LOCKTITLE))
            .map(Attr::getNodeValue)
            .orElse("no");
          elem.setAttributeNS(DITA_OT_NS, DITA_OT_NS_PREFIX + ":" + ATTRIBUTE_NAME_LOCKTITLE, locktitleAttr);
        }
        final Node copy = resultDoc.importNode(elem, true);
        final Element stub = resultDoc.createElement(ELEMENT_STUB);
        stub.appendChild(copy);
        topicMetaTable.put(metaKey, stub);
      }
    }
  }

  private Map<String, Element> mergeMeta(
    final Map<String, Element> topicMetaTable,
    final Map<String, Element> inheritance,
    final Set<String> enableSet
  ) {
    // When inherited metadata need to be merged into current metadata
    // enableSet should be cascadeSet so that only metadata that can
    // be inherited are merged.
    // Otherwise enableSet should be metaSet in order to merge all
    // metadata.
    final Map<String, Element> res = new HashMap<>(topicMetaTable);
    for (String key : enableSet) {
      if (inheritance.containsKey(key)) {
        final Element value = inheritance.get(key);
        if (uniqueSet.contains(key)) {
          if (!res.containsKey(key)) {
            res.put(key, value);
          }
        } else { // not unique metadata
          if (!res.containsKey(key)) {
            res.put(key, value);
          } else {
            //not necessary to do node type check here
            //because inheritStub doesn't contains any node
            //other than Element.
            final Element stub = res.get(key);
            final Element inheritStub = value;
            if (stub != inheritStub) {
              // Merge the value if stub does not equal to inheritStub
              // Otherwise it will get into infinitive loop
              for (Element child : XMLUtils.getChildElements(inheritStub)) {
                final Element item = (Element) stub.getOwnerDocument().importNode(child, true);
                stub.appendChild(item);
              }
            }
            res.put(key, stub);
          }
        }
      }
    }
    return Collections.unmodifiableMap(res);
  }

  private void handleGlobalMeta(final Element metadata) {
    for (var elem : XMLUtils.getChildElements(metadata)) {
      final Attr classAttr = elem.getAttributeNode(ATTRIBUTE_NAME_CLASS);
      if (classAttr != null) {
        final String classValue = classAttr.getNodeValue();
        final String metaKey = classValue.substring(1, classValue.indexOf(STRING_BLANK, classValue.indexOf(SLASH)) + 1);
        if (TOPIC_METADATA.matches(classValue)) {
          //proceed the metadata in <metadata>
          handleGlobalMeta(elem);
        } else if (cascadeSet.contains(metaKey) && globalMeta.containsKey(metaKey)) {
          //append node to the list if it exist in global meta table
          globalMeta.get(metaKey).appendChild(resultDoc.importNode(elem, true));
        } else if (cascadeSet.contains(metaKey)) {
          final Element stub = resultDoc.createElement(ELEMENT_STUB);
          stub.appendChild(resultDoc.importNode(elem, true));
          globalMeta.put(metaKey, stub);
        }
      }
    }
  }

  /**
   * Get metadata for topics
   *
   * @return map of metadata by topic path
   */
  public Map<URI, Map<String, Element>> getMapping() {
    return Collections.unmodifiableMap(resultTable);
  }
}
