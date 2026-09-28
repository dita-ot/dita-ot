/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2016 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.reader;

import static org.dita.dost.reader.ChunkMapReader.CHUNK_TO_CONTENT;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.DitaUtils.isDitaFormat;
import static org.dita.dost.util.DitaUtils.isLocalScope;
import static org.dita.dost.util.URLUtils.*;

import java.net.URI;
import java.util.*;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.util.AttributeStack;
import org.dita.dost.util.XMLUtils;
import org.dita.dost.writer.AbstractXMLFilter;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Collect copy-to information from a map.
 *
 * <p>
 * <strong>Not thread-safe</strong>. Instances can be reused by calling
 * {@link #reset()} between calls to parse.
 * </p>
 */
public final class CopyToReader extends AbstractXMLFilter {

  /**
   * Map of copy-to target to source
   */
  private final Map<URI, URI> copyToMap = new HashMap<>(16);
  /**
   * chunk nesting level
   */
  private int chunkLevel = 0;
  /**
   * Stack for cascading attributes.
   */
  private final AttributeStack attributeStack = new AttributeStack();

  /**
   * Get the copy-to map.
   *
   * @return copy-to map
   */
  public Map<URI, URI> getCopyToMap() {
    return copyToMap;
  }

  /**
   * Set current file absolute path
   *
   * @param currentFile absolute path to current file
   */
  public void setCurrentFile(final URI currentFile) {
    assert currentFile.isAbsolute();
    super.setCurrentFile(currentFile);
  }

  /**
   * Reset the internal variables.
   */
  public void reset() {
    chunkLevel = 0;
    copyToMap.clear();
    attributeStack.clear();
  }

  @Override
  public void startDocument() throws SAXException {
    attributeStack.push(
      new XMLUtils.AttributesBuilder().add(ATTRIBUTE_NAME_PROCESSING_ROLE, ATTR_PROCESSING_ROLE_VALUE_NORMAL).build()
    );

    getContentHandler().startDocument();
  }

  @Override
  public void startElement(final String uri, final String localName, final String qName, final Attributes atts)
    throws SAXException {
    attributeStack.push(atts);

    final String classValue = atts.getValue(ATTRIBUTE_NAME_CLASS);

    if (chunkLevel > 0) {
      chunkLevel++;
    } else if (atts.getValue(ATTRIBUTE_NAME_CHUNK) != null) {
      chunkLevel++;
    }

    if (MAP_TOPICREF.matches(classValue)) {
      parseAttribute(atts);
    }

    getContentHandler().startElement(uri, localName, qName, atts);
  }

  @Override
  public void endElement(final String uri, final String localName, final String qName) throws SAXException {
    attributeStack.pop();

    if (chunkLevel > 0) {
      chunkLevel--;
    }

    getContentHandler().endElement(uri, localName, qName);
  }

  /**
   * Clean up.
   */
  @Override
  public void endDocument() throws SAXException {
    attributeStack.clear();

    getContentHandler().endDocument();
  }

  /**
   * Parse the input attributes for needed information.
   *
   * @param atts all attributes
   */
  private void parseAttribute(final Attributes atts) {
    if (
      atts.getValue(ATTRIBUTE_NAME_CHUNK) == null || !atts.getValue(ATTRIBUTE_NAME_CHUNK).contains(CHUNK_TO_CONTENT)
    ) {
      final URI href = toURI(atts.getValue(ATTRIBUTE_NAME_HREF));

      if (
        href != null &&
        isDitaFormat(attributeStack.peek(ATTRIBUTE_NAME_FORMAT)) &&
        isLocalScope(atts.getValue(ATTRIBUTE_NAME_SCOPE))
      ) {
        final URI hrefAbs = stripFragment(currentFile.resolve(href));
        assert hrefAbs.isAbsolute();

        var copyTo = toURI(atts.getValue(ATTRIBUTE_NAME_COPY_TO));
        if (copyTo != null) {
          final URI copyToAbs = stripFragment(copyTo.isAbsolute() ? copyTo : currentFile.resolve(copyTo));
          assert copyToAbs.isAbsolute();

          final URI copyToSourceAbs = copyToMap.get(copyToAbs);
          if (copyToSourceAbs != null) {
            if (!hrefAbs.equals(copyToSourceAbs)) {
              logger.warn(
                MessageUtils.getMessage("DOTX065W", href.toString(), copyToAbs.toString()).setLocation(atts).toString()
              );
            }
          } else {
            copyToMap.put(copyToAbs, hrefAbs);
          }
        }
      }
    }
  }
}
