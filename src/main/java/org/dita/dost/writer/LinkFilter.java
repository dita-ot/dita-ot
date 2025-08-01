/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2017 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.writer;

import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.DitaUtils.isExternalScope;
import static org.dita.dost.util.URLUtils.*;
import static org.dita.dost.util.XMLUtils.addOrSetAttribute;

import com.google.common.annotations.VisibleForTesting;
import java.net.URI;
import org.dita.dost.util.AttributeStack;
import org.dita.dost.util.Job;
import org.dita.dost.util.Job.FileInfo;
import org.dita.dost.util.URLUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class LinkFilter extends AbstractXMLFilter {

  private final AttributeStack attributeStack = new AttributeStack(ATTRIBUTE_NAME_SCOPE);

  /**
   * Destination temporary file
   */
  private URI destFile;
  private URI base;

  @Override
  public void startElement(final String uri, final String localName, final String qName, final Attributes atts)
    throws SAXException {
    attributeStack.push(atts);

    Attributes res = atts;

    if (hasLocalDitaLink(atts)) {
      final AttributesImpl resAtts = new AttributesImpl(atts);
      int i = atts.getIndex(ATTRIBUTE_NAME_IMAGEREF);
      if (i == -1) {
        i = atts.getIndex(ATTRIBUTE_NAME_HREF);
      }
      if (i == -1) {
        i = atts.getIndex(ATTRIBUTE_NAME_DATA);
      }
      if (i != -1) {
        final URI resHref = getHref(toURI(atts.getValue(i)));
        addOrSetAttribute(resAtts, atts.getQName(i), resHref.toString());
        res = resAtts;
      }

      int copyToIndex = atts.getIndex(ATTRIBUTE_NAME_COPY_TO);
      if (copyToIndex != -1) {
        final URI resCopyTo = getHref(toURI(atts.getValue(copyToIndex)));
        addOrSetAttribute(resAtts, ATTRIBUTE_NAME_COPY_TO, resCopyTo.toString());
      }
    }

    getContentHandler().startElement(uri, localName, qName, res);
  }

  @Override
  public void endElement(final String uri, final String localName, final String qName) throws SAXException {
    getContentHandler().endElement(uri, localName, qName);

    attributeStack.pop();
  }

  private boolean hasLocalDitaLink(final Attributes atts) {
    final boolean hasHref = atts.getIndex(ATTRIBUTE_NAME_HREF) != -1;
    var scope = attributeStack.peek(ATTRIBUTE_NAME_SCOPE);
    if (hasHref && !isExternalScope(scope)) {
      return true;
    }
    final URI data = toURI(atts.getValue(ATTRIBUTE_NAME_DATA));
    if (data != null && !data.isAbsolute()) {
      return true;
    }
    final URI imageref = toURI(atts.getValue(ATTRIBUTE_NAME_IMAGEREF));
    if (imageref != null && !imageref.isAbsolute()) {
      return true;
    }
    return false;
  }

  @VisibleForTesting
  URI getHref(final URI target) {
    if (target.getFragment() != null && (target.getPath() == null || target.getPath().equals(""))) {
      return target;
    }
    final URI targetAbs = stripFragment(currentFile.resolve(target));
    final FileInfo targetFileInfo = job.getFileInfo(targetAbs);
    final FileInfo sourceFileInfo = job.getFileInfo(currentFile);
    if (targetFileInfo != null && sourceFileInfo != null) {
      final URI relTarget = URLUtils.getRelativePath(sourceFileInfo.result, targetFileInfo.result);
      return setFragment(relTarget, target.getFragment());
    } else {
      return target;
    }
  }

  public void setDestFile(final URI destFile) {
    assert destFile.isAbsolute();
    this.destFile = destFile;
  }

  @Override
  public void setJob(final Job job) {
    super.setJob(job);
    base = job.getInputDir();
  }
}
