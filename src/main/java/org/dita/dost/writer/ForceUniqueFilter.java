/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2014 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.writer;

import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.DitaUtils.isLocalScope;
import static org.dita.dost.util.FileUtils.getExtension;
import static org.dita.dost.util.FileUtils.replaceExtension;
import static org.dita.dost.util.URLUtils.*;

import java.net.URI;
import java.util.*;
import org.dita.dost.module.reader.TempFileNameScheme;
import org.dita.dost.util.Job.FileInfo;
import org.dita.dost.util.XMLUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Create copy-to attributes for duplicate topicrefs.
 */
public final class ForceUniqueFilter extends AbstractXMLFilter {

  private final Map<URI, Integer> topicrefCount = new HashMap<>();
  private final Deque<Boolean> ignoreStack = new ArrayDeque<>();
  /**
   * Generated copy-to mappings, key is target topic and value is source topic.
   */
  public final Map<FileInfo, FileInfo> copyToMap = new HashMap<>();
  private TempFileNameScheme tempFileNameScheme;

  /**
   * Stack used to detect the current element type.
   */
  private final Deque<Optional<String>> classElementStack = new ArrayDeque<>();

  /**
   * Stack used to hold the current topicref parent.
   */
  private final Deque<ParentTopicref> topicrefParentsStack = new ArrayDeque<>();

  // ContentHandler methods

  @Override
  public void startElement(final String uri, final String localName, final String qName, final Attributes atts)
    throws SAXException {
    ignoreStack.push(MAP_RELTABLE.matches(atts) ? false : ignoreStack.isEmpty() || ignoreStack.peek());

    final String classValue = atts.getValue(ATTRIBUTE_NAME_CLASS);
    classElementStack.addFirst(Optional.ofNullable(classValue));

    Attributes res = atts;
    if (ignoreStack.peek() && MAP_TOPICREF.matches(res)) {
      final URI href = toURI(res.getValue(ATTRIBUTE_NAME_HREF));
      final URI copyTo = toURI(res.getValue(ATTRIBUTE_NAME_COPY_TO));
      final URI source = copyTo != null ? copyTo : href;
      String currentScope = res.getValue(ATTRIBUTE_NAME_SCOPE);
      String scope = getCascadingScope(currentScope);
      final String format = res.getValue(ATTRIBUTE_NAME_FORMAT);
      String currentProcesingResource = res.getValue(ATTRIBUTE_NAME_PROCESSING_ROLE);
      final String processingRole = getCascadingProcessingRole(currentProcesingResource);
      FileInfo dstFi = null;
      if (
        source != null &&
        isLocalScope(scope) &&
        (format == null || format.equals(ATTR_FORMAT_VALUE_DITA)) &&
        (processingRole == null || processingRole.equals(ATTR_PROCESSING_ROLE_VALUE_NORMAL))
      ) {
        final URI file = stripFragment(source);
        Integer count = topicrefCount.getOrDefault(file, 0);

        final ParentTopicref parentTopicref = topicrefParentsStack.peek();
        boolean parentTopicHasEmbededSubtopics = false;
        if (parentTopicref != null && parentTopicref.href != null) {
          final URI parentHref = stripFragment(parentTopicref.href);
          parentTopicHasEmbededSubtopics = href != null && parentHref.equals(file) && href.getFragment() != null;
        }
        if (parentTopicHasEmbededSubtopics) {
          dstFi = parentTopicref.dstFi;
        } else {
          count++;
          topicrefCount.put(file, count);
        }

        if (count > 1) { // not only reference to this topic
          final FileInfo srcFi = job.getFileInfo(currentFile.resolve(stripFragment(source)));
          if (srcFi != null) {
            if (dstFi == null) {
              dstFi = generateCopyToTarget(srcFi, count);
            }
            copyToMap.put(dstFi, srcFi);
            final URI dstTempAbs = job.tempDirURI.resolve(dstFi.uri());
            final URI targetRel = getRelativePath(currentFile, dstTempAbs);
            final URI target = setFragment(targetRel, href.getFragment());

            final AttributesImpl buf = new AttributesImpl(atts);
            XMLUtils.addOrSetAttribute(buf, ATTRIBUTE_NAME_COPY_TO, target.toString());
            res = buf;
          }
        }
      }
      topicrefParentsStack.push(new ParentTopicref(href, dstFi, currentScope, currentProcesingResource));
    }

    getContentHandler().startElement(uri, localName, qName, res);
  }

  /**
   * Get the scope of the element, looking also at cascading values.
   * @param currentScope The current scope.
   * @return The cascading scope.
   */
  private String getCascadingScope(String currentScope) {
    String scope = currentScope;
    if (scope == null) {
      Iterator<ParentTopicref> iter = topicrefParentsStack.descendingIterator();
      while (iter.hasNext()) {
        ParentTopicref parentTopicref = iter.next();
        if (parentTopicref.scope != null) {
          scope = parentTopicref.scope;
          break;
        }
      }
    }
    return scope;
  }

  /**
   * Get the processing role of the element, looking also at cascading values.
   * @param currentProcessingRole The current processing role.
   * @return The cascading processing role.
   */
  private String getCascadingProcessingRole(String currentProcessingRole) {
    String processingRole = currentProcessingRole;
    if (processingRole == null) {
      Iterator<ParentTopicref> iter = topicrefParentsStack.descendingIterator();
      while (iter.hasNext()) {
        ParentTopicref parentTopicref = iter.next();
        if (parentTopicref.processingRole != null) {
          processingRole = parentTopicref.processingRole;
          break;
        }
      }
    }
    return processingRole;
  }

  @Override
  public void endElement(final String uri, final String localName, final String qName) throws SAXException {
    getContentHandler().endElement(uri, localName, qName);

    final Optional<String> classValue = classElementStack.removeFirst();
    if (classValue.isPresent() && ignoreStack.peek() && MAP_TOPICREF.matches(classValue.get())) {
      topicrefParentsStack.pop();
    }
    ignoreStack.pop();
  }

  private FileInfo generateCopyToTarget(final FileInfo srcFi, final int count) {
    for (int i = 0;; i++) {
      final StringBuilder ext = new StringBuilder();
      ext.append('_');
      ext.append(Integer.toString(count));
      if (i > 0) {
        ext.append('_');
        ext.append(Integer.toString(i));
      }
      ext.append('.');
      ext.append(getExtension(srcFi.result().toString()));

      final URI dstResult = toURI(replaceExtension(srcFi.result().toString(), ext.toString()));
      final URI dstTemp = tempFileNameScheme.generateTempFileName(dstResult);
      if (job.getFileInfo(dstTemp) == null) {
        return new FileInfo.Builder(srcFi).uri(dstTemp).result(dstResult).build();
      }
    }
  }

  public void setTempFileNameScheme(TempFileNameScheme tempFileNameScheme) {
    this.tempFileNameScheme = tempFileNameScheme;
  }

  /**
   * Holds additional information about the parent for a topicref.
   *
   * @param href           The href value for the parent topic.
   * @param dstFi          Information about the destination file used in the output.
   * @param scope          Scope attribute on the topicref
   * @param processingRole processing-role attribute on the topicref
   */
  private record ParentTopicref(URI href, FileInfo dstFi, String scope, String processingRole) {
    /**
     * Constructor.
     *
     * @param href           The href value for the parent topic.
     * @param dstFi          Information about the destination file used in the output.
     * @param scope          Value of scope attribute
     * @param processingRole Value of processing-role attribute.
     */
    private ParentTopicref {}
  }
}
