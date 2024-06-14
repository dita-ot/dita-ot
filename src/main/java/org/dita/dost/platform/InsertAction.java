/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2005, 2006 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.

 */
package org.dita.dost.platform;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;
import javax.xml.parsers.SAXParserFactory;
import org.dita.dost.log.DITAOTLogger;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * InsertAction implements IAction and insert the resource
 * provided by plug-ins into the xsl files, ant scripts and xml catalog.
 * @author Zhang, Yuan Peng
 */
class InsertAction extends XMLFilterImpl implements IAction {

  private final XMLReader reader;
  private DITAOTLogger logger;
  private boolean useClasspath;
  private final Set<Value> fileNameSet;
  final Map<String, String> paramTable;
  private int elemLevel = 0;
  /** Current processing file. */
  String currentFile;

  /**
   * Default Constructor.
   */
  public InsertAction() {
    fileNameSet = new LinkedHashSet<>(16);
    paramTable = new HashMap<>();
    try {
      final SAXParserFactory factory = SAXParserFactory.newInstance();
      factory.setNamespaceAware(true);
      reader = factory.newSAXParser().getXMLReader();
      reader.setContentHandler(this);
    } catch (final Exception e) {
      throw new RuntimeException("Failed to initialize parser: " + e.getMessage(), e);
    }
  }

  @Override
  public void setInput(final List<Value> input) {
    fileNameSet.addAll(input);
  }

  @Override
  public void addParam(final String name, final String value) {
    paramTable.put(name, value);
  }

  @Override
  public String getResult() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void getResult(final ContentHandler retBuf) throws SAXException {
    setContentHandler(retBuf);
    try {
      for (final Value fileName : fileNameSet) {
        if (fileName instanceof Value.PathValue pathValue) {
          currentFile = pathValue.getPath();
        } else {
          logger.error("Catalog import must be a file feature: " + fileName.value());
          continue;
        }
        reader.parse(currentFile);
      }
    } catch (SAXException | RuntimeException e) {
      throw e;
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void setFeatures(final Map<String, Plugin> h) {}

  @Override
  public void setLogger(final DITAOTLogger logger) {
    this.logger = logger;
  }

  @Override
  public void setUseClasspath(boolean useClasspath) {
    this.useClasspath = useClasspath;
  }

  // XMLFilter methods

  @Override
  public void startPrefixMapping(String prefix, String uri) throws SAXException {
    if (elemLevel != 0) {
      getContentHandler().startPrefixMapping(prefix, uri);
    }
  }

  @Override
  public void endPrefixMapping(String prefix) throws SAXException {
    if (elemLevel != 0) {
      getContentHandler().endPrefixMapping(prefix);
    }
  }

  @Override
  public void startElement(final String uri, final String localName, final String qName, final Attributes attributes)
    throws SAXException {
    if (elemLevel != 0) {
      getContentHandler().startElement(uri, localName, qName, attributes);
    }
    elemLevel++;
  }

  @Override
  public void endElement(final String uri, final String localName, final String qName) throws SAXException {
    elemLevel--;
    if (elemLevel != 0) {
      getContentHandler().endElement(uri, localName, qName);
    }
  }

  @Override
  public void startDocument() throws SAXException {
    elemLevel = 0;
    // suppress
  }

  @Override
  public void endDocument() throws SAXException {
    // suppress
  }
}
