/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2007 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.

 */
package org.dita.dost.reader;

import static java.util.Arrays.asList;
import static org.dita.dost.module.GenMapAndTopicListModule.ELEMENT_STUB;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.URLUtils.stripFragment;

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
  private final Map<URI, Map<String, Element>> resultTable = new HashMap<>(16);

  private static final Set<String> uniqueSet = Collections.unmodifiableSet(
    new HashSet<>(
      asList(
        TOPIC_CRITDATES.matcher,
        TOPIC_PERMISSIONS.matcher,
        TOPIC_PUBLISHER.matcher,
        TOPIC_SOURCE.matcher,
        MAP_SEARCHTITLE.matcher,
        TOPIC_SEARCHTITLE.matcher
      )
    )
  );
  private static final Set<String> cascadeSet = Collections.unmodifiableSet(
    new HashSet<>(
      asList(
        TOPIC_AUDIENCE.matcher,
        TOPIC_AUTHOR.matcher,
        TOPIC_CATEGORY.matcher,
        TOPIC_COPYRIGHT.matcher,
        TOPIC_CRITDATES.matcher,
        TOPIC_METADATA.matcher,
        TOPIC_PERMISSIONS.matcher,
        TOPIC_PRODINFO.matcher,
        TOPIC_PUBLISHER.matcher
      )
    )
  );
  private static final Set<String> metaSet = Collections.unmodifiableSet(
    new HashSet<>(
      asList(
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
      )
    )
  );
  private static final List<String> metaPos = Collections.unmodifiableList(
    asList(
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
      MAP_SHORTDESC.matcher,
      TOPIC_SHORTDESC.matcher,
      TOPIC_NAVTITLE.matcher,
      TOPIC_METADATA.matcher
    )
  );

  private final Map<String, Element> globalMeta;
  /** Current document. */
  private Document doc = null;
  /** Result metadata document. */
  private Document resultDoc = null;
  /** Current file. */
  private File filePath = null;

  /**
   * Constructor.
   */
  public MapMetaReader() {
    super();
    globalMeta = new HashMap<>(16);
    resultDoc = XMLUtils.getDocumentBuilder().newDocument();
    resultTable.clear();
  }

  /**
   * read map files.
   * @param filename filename
   */
  @Override
  public void read(final File filename) throws DITAOTException {
    filePath = filename;

    //clear the history on global metadata table
    globalMeta.clear();
    super.read(filename);
  }

  @Override
  public Document process(final Document doc) {
    this.doc = doc;
    for (var elem : XMLUtils.getChildElements(doc.getDocumentElement())) {
      final String classAttr = elem.getAttribute(ATTRIBUTE_NAME_CLASS);
      // if this node is topicmeta node under root
      if (MAP_TOPICMETA.matches(classAttr)) {
        handleGlobalMeta(elem);
        // if this node is topicref node under root
      } else if (MAP_TOPICREF.matches(classAttr)) {
        handleTopicref(elem, globalMeta);
      }
    }
    // Indexterm elements with either start or end attribute should not been
    // move to referenced dita file's prolog section.
    for (final Map<String, Element> resultTableEntry : resultTable.values()) {
      for (final Map.Entry<String, Element> mapEntry : resultTableEntry.entrySet()) {
        final String key = mapEntry.getKey();
        if (TOPIC_KEYWORDS.matcher.equals(key)) {
          removeIndexTermRecursive(mapEntry.getValue());
        }
      }
    }
    return doc;
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
    final Attr hrefAttr = topicref.getAttributeNode(ATTRIBUTE_NAME_HREF);
    final Attr copytoAttr = topicref.getAttributeNode(ATTRIBUTE_NAME_COPY_TO);
    final Attr scopeAttr = topicref.getAttributeNode(ATTRIBUTE_NAME_SCOPE);
    final Attr formatAttr = topicref.getAttributeNode(ATTRIBUTE_NAME_FORMAT);
    Map<String, Element> current = mergeMeta(null, Collections.unmodifiableMap(inheritance), cascadeSet);
    Element metaNode = null;
    final boolean hasDitaTopicTarget = hrefAttr != null && isLocalScope(scopeAttr) && isDitaFormat(formatAttr);

    for (Element elem : XMLUtils.getChildElements(topicref)) {
      String classAttr = elem.getAttribute(ATTRIBUTE_NAME_CLASS);
      if (MAP_TOPICMETA.matches(classAttr)) {
        // the parent topicref refers to a valid dita topic
        if (hasDitaTopicTarget) {
          metaNode = elem;
          current = handleMeta(elem, inheritance);
        }
      } else if (MAP_TOPICREF.matches(classAttr)) {
        handleTopicref(elem, current);
      }
    }

    if (!current.isEmpty() && hasDitaTopicTarget) {
      URI topicPath;
      if (copytoAttr != null) {
        final URI copyToUri = stripFragment(URLUtils.toURI(copytoAttr.getNodeValue()));
        topicPath = job.tempDirURI.relativize(filePath.toURI().resolve(copyToUri));
      } else {
        final URI hrefUri = URLUtils.toURI(hrefAttr.getNodeValue());
        topicPath = job.tempDirURI.relativize(filePath.toURI().resolve(hrefUri));
      }
      if (resultTable.containsKey(topicPath)) {
        //if the result table already contains some result
        //metadata for current topic path.
        final Map<String, Element> previous = resultTable.get(topicPath);
        resultTable.put(topicPath, mergeMeta(previous, current, metaSet));
      } else {
        resultTable.put(topicPath, cloneElementMap(current));
      }
      final Map<String, Element> metas = resultTable.get(topicPath);
      if (!metas.isEmpty()) {
        if (metaNode != null) {
          topicref.removeChild(metaNode);
        }
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

  private boolean isLocalScope(final Attr scopeAttr) {
    return scopeAttr == null || scopeAttr.getNodeValue().equals(ATTR_SCOPE_VALUE_LOCAL);
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
    final Map<String, Element> res;
    if (topicMetaTable == null) {
      res = new HashMap<>(16);
    } else {
      res = new HashMap<>(topicMetaTable);
    }

    for (String key : enableSet) {
      if (inheritance.containsKey(key)) {
        if (uniqueSet.contains(key)) {
          if (!res.containsKey(key)) {
            res.put(key, inheritance.get(key));
          }
        } else { // not unique metadata
          if (!res.containsKey(key)) {
            res.put(key, inheritance.get(key));
          } else {
            //not necessary to do node type check here
            //because inheritStub doesn't contains any node
            //other than Element.
            final Element stub = res.get(key);
            final Node inheritStub = inheritance.get(key);
            if (stub != inheritStub) {
              // Merge the value if stub does not equal to inheritStub
              // Otherwise it will get into infinitive loop
              for (Node child : XMLUtils.toList(inheritStub.getChildNodes())) {
                final Node item = stub.getOwnerDocument().importNode(child, true);
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
