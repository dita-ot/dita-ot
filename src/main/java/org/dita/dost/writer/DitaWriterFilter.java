/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2004, 2005 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.

 */
package org.dita.dost.writer;

import static org.dita.dost.reader.GenListModuleReader.isFormatDita;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.DitaUtils.isLocalScope;
import static org.dita.dost.util.URLUtils.*;

import java.io.File;
import java.net.URI;
import java.util.Map;
import java.util.Objects;
import javax.xml.namespace.QName;
import org.dita.dost.module.DebugAndFilterModule;
import org.dita.dost.module.reader.TempFileNameScheme;
import org.dita.dost.util.Constants;
import org.dita.dost.util.Job.FileInfo;
import org.dita.dost.util.StringUtils;
import org.dita.dost.util.URLUtils;
import org.dita.dost.util.XMLUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Insert document PIs and normalize attributes.
 *
 * <p>The following processing instructions are added before the root element:</p>
 * <dl>
 *   <dt>{@link Constants#PI_WORKDIR_TARGET}</dt>
 *   <dd>Absolute system path of the file parent directory. On Windows, a {@code /}
 *     is added to beginning of the path.</dd>
 *   <dt>{@link Constants#PI_WORKDIR_TARGET_URI}</dt>
 *   <dd>Absolute URI of the file parent directory.</dd>
 *   <dt>{@link Constants#PI_PATH2PROJ_TARGET}</dt>
 *   <dd>Relative system path to the output directory, with a trailing directory separator.
 *     When the source file is in the project root directory, processing instruction has no value.</dd>
 *   <dt>{@link Constants#PI_PATH2PROJ_TARGET_URI}</dt>
 *   <dd>Relative URI to the output directory, with a trailing path separator.
 *     When the source file is in the project root directory, processing instruction has value {@code ./}.</dd>
 *   <dt>{@link Constants#PI_PATH2ROOTMAP_TARGET_URI}</dt>
 *   <dd>Relative URI to the root map directory, with a trailing path separator.
 *     When the source file is in the project root directory, processing instruction has value {@code ./}.</dd>
 * </dl>
 *
 * @author Zhang, Yuan Peng
 */
public final class DitaWriterFilter extends AbstractXMLFilter {

  public static final String ATTRIBUTE_NAME_ORIG_FORMAT = "orig-" + ATTRIBUTE_NAME_FORMAT;

  /** Default value map. */
  private Map<QName, Map<String, String>> defaultValueMap = Map.of();
  /** Absolute path to current destination file. */
  private File outputFile;
  /** File infos by src. */
  private Map<URI, FileInfo> fileInfoMap;
  private TempFileNameScheme tempFileNameScheme;

  public DitaWriterFilter() {}

  public void setTempFileNameScheme(TempFileNameScheme tempFileNameScheme) {
    this.tempFileNameScheme = tempFileNameScheme;
  }

  /**
   * Set default value map.
   * @param defaultMap default value map
   */
  public void setDefaultValueMap(final Map<QName, Map<String, String>> defaultMap) {
    defaultValueMap = defaultMap;
  }

  public void setOutputFile(final File outputFile) {
    this.outputFile = outputFile;
  }

  // ContentHandler methods

  @Override
  public void endElement(final String uri, final String localName, final String qName) throws SAXException {
    getContentHandler().endElement(uri, localName, qName);
  }

  @Override
  public void startDocument() throws SAXException {
    // XXX May be require fixup
    final URI relativeToMap = URLUtils.getRelativePath(job.getInputFile(), currentFile);
    final File path2Project = DebugAndFilterModule.getPathtoProject(
      toFile(relativeToMap),
      toFile(currentFile),
      toFile(job.getInputFile()),
      job
    );
    final File path2rootmap = toFile(getRelativePath(currentFile, job.getInputFile())).getParentFile();
    getContentHandler().startDocument();
    if (!OS_NAME.toLowerCase().contains(OS_NAME_WINDOWS)) {
      getContentHandler().processingInstruction(PI_WORKDIR_TARGET, outputFile.getParentFile().getAbsolutePath());
    } else {
      getContentHandler()
        .processingInstruction(PI_WORKDIR_TARGET, UNIX_SEPARATOR + outputFile.getParentFile().getAbsolutePath());
    }
    getContentHandler().ignorableWhitespace(new char[] { '\n' }, 0, 1);
    getContentHandler().processingInstruction(PI_WORKDIR_TARGET_URI, outputFile.toURI().resolve(".").toString());
    getContentHandler().ignorableWhitespace(new char[] { '\n' }, 0, 1);
    if (path2Project != null) {
      getContentHandler().processingInstruction(PI_PATH2PROJ_TARGET, path2Project.getPath() + File.separator);
      getContentHandler()
        .processingInstruction(PI_PATH2PROJ_TARGET_URI, toURI(path2Project).toString() + URI_SEPARATOR);
    } else {
      getContentHandler().processingInstruction(PI_PATH2PROJ_TARGET, "");
      getContentHandler().processingInstruction(PI_PATH2PROJ_TARGET_URI, "." + URI_SEPARATOR);
    }
    if (path2rootmap != null) {
      getContentHandler()
        .processingInstruction(PI_PATH2ROOTMAP_TARGET_URI, toURI(path2rootmap).toString() + URI_SEPARATOR);
    } else {
      getContentHandler().processingInstruction(PI_PATH2ROOTMAP_TARGET_URI, "." + URI_SEPARATOR);
    }
    getContentHandler().ignorableWhitespace(new char[] { '\n' }, 0, 1);
  }

  @Override
  public void startElement(final String uri, final String localName, final String qName, final Attributes atts)
    throws SAXException {
    final Attributes res = processAttributes(qName, atts);

    getContentHandler().startElement(uri, localName, qName, res);
  }

  /**
   * Process attributes
   *
   * @param qName element name
   * @param atts input attributes
   */
  private Attributes processAttributes(final String qName, final Attributes atts) {
    AttributesImpl res = null;
    final int attsLen = atts.getLength();
    for (int i = 0; i < attsLen; i++) {
      final String attName = atts.getLocalName(i);
      final String origValue = atts.getValue(i);
      String attValue = null;
      if (origValue.equals(ATTR_VALUE_DITA_USE_CONREF_TARGET)) {
        // retain original value
      } else if (ATTRIBUTE_NAME_CONREF.equals(attName)) {
        if (res == null) {
          res = new AttributesImpl(atts);
        }
        attValue = replaceHREF(ATTRIBUTE_NAME_CONREF, atts).toString();
      } else if (ATTRIBUTE_NAME_HREF.equals(attName) || ATTRIBUTE_NAME_COPY_TO.equals(attName)) {
        if (isLocalScope(atts.getValue(ATTRIBUTE_NAME_SCOPE))) {
          if (res == null) {
            res = new AttributesImpl(atts);
          }
          attValue = replaceHREF(attName, atts).toString();
        }
      } else if (ATTRIBUTE_NAME_FORMAT.equals(attName)) {
        final String format = atts.getValue(ATTRIBUTE_NAME_FORMAT);
        final String scope = atts.getValue(ATTRIBUTE_NAME_SCOPE);
        // verify format is correct
        if (isFormatDita(format) && isLocalScope(scope)) {
          if (res == null) {
            res = new AttributesImpl(atts);
          }
          attValue = MAPGROUP_D_MAPREF.matches(atts) ? ATTR_FORMAT_VALUE_DITAMAP : ATTR_FORMAT_VALUE_DITA;
          if (!format.equals(ATTR_FORMAT_VALUE_DITA)) {
            XMLUtils.addOrSetAttribute(
              res,
              DITA_OT_NS,
              ATTRIBUTE_NAME_ORIG_FORMAT,
              DITA_OT_NS_PREFIX + ":" + ATTRIBUTE_NAME_ORIG_FORMAT,
              "CDATA",
              format
            );
          }
        }
        //      } else {
        //        attValue = getAttributeValue(qName, attQName, attValue);
      }
      if (attValue != null) {
        XMLUtils.addOrSetAttribute(
          res,
          atts.getURI(i),
          atts.getLocalName(i),
          atts.getQName(i),
          atts.getType(i),
          attValue
        );
      }
    }
    return Objects.requireNonNullElse(res, atts);
  }

  /**
   * Get attribute value or default if attribute is not defined
   *
   * @param elemQName element QName
   * @param attQName attribute QName
   * @param value attribute value
   * @return attribute value or default
   */
  private String getAttributeValue(final String elemQName, final QName attQName, final String value) {
    if (StringUtils.isEmptyString(value) && !defaultValueMap.isEmpty()) {
      final Map<String, String> defaultMap = defaultValueMap.get(attQName);
      if (defaultMap != null) {
        final String defaultValue = defaultMap.get(elemQName);
        if (defaultValue != null) {
          return defaultValue;
        }
      }
    }
    return value;
  }

  /**
   * Relativize absolute references if possible.
   *
   * @param attName attribute name
   * @param atts attributes
   * @return attribute value, may be {@code null}
   */
  private URI replaceHREF(final String attName, final Attributes atts) {
    URI attValue = toURI(atts.getValue(attName));
    if (attValue != null) {
      final String fragment = attValue.getFragment();
      if (fragment != null) {
        attValue = stripFragment(attValue);
      }
      if (attValue.toString().length() != 0) {
        final URI current = currentFile.resolve(attValue);
        final FileInfo f = job.getFileInfo(current);
        if (f != null) {
          final FileInfo cfi = job.getFileInfo(currentFile);
          final URI currrentFileTemp = job.tempDirURI.resolve(cfi.uri());
          final URI targetTemp = job.tempDirURI.resolve(f.uri());
          attValue = getRelativePath(currrentFileTemp, targetTemp);
        } else if (tempFileNameScheme != null) {
          final URI currrentFileTemp = job.tempDirURI.resolve(tempFileNameScheme.generateTempFileName(currentFile));
          final URI targetTemp = job.tempDirURI.resolve(tempFileNameScheme.generateTempFileName(current));
          final URI relativePath = getRelativePath(currrentFileTemp, targetTemp);
          attValue = relativePath;
        } else {
          attValue = getRelativePath(currentFile, current);
        }
      }
      if (fragment != null) {
        attValue = setFragment(attValue, fragment);
      }
    } else {
      return null;
    }
    return attValue;
  }
}
