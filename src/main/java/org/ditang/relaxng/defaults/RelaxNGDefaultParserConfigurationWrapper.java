/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2011 George Bina
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.ditang.relaxng.defaults;

import java.io.IOException;
import java.util.Locale;

import org.apache.xerces.parsers.XIncludeAwareParserConfiguration;
import org.apache.xerces.xni.XMLDTDContentModelHandler;
import org.apache.xerces.xni.XMLDTDHandler;
import org.apache.xerces.xni.XMLDocumentHandler;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLConfigurationException;
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.apache.xerces.xni.parser.XMLErrorHandler;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.apache.xerces.xni.parser.XMLParserConfiguration;

/**
 * Wrapper over an XML Parser configuration which also provides default values from the associated
 * RelaxNG schema
 */
public class RelaxNGDefaultParserConfigurationWrapper implements XMLParserConfiguration {
  /**
   * The wrapped configuration
   */
  private XMLParserConfiguration config;

  /**
   * Default constructor
   */
  public RelaxNGDefaultParserConfigurationWrapper() {
    XIncludeAwareParserConfiguration c = new XIncludeAwareParserConfiguration();
    this.config = c;
  }
  
  /**
   * Constructor
   * @param config The wrapped configuration.
   */
  public RelaxNGDefaultParserConfigurationWrapper(XMLParserConfiguration config) {
    this.config = config;
  }
  
  /**
   * @see org.apache.xerces.xni.parser.XMLParserConfiguration#parse(org.apache.xerces.xni.parser.XMLInputSource)
   */
  @Override
  public void parse(XMLInputSource inputSource) throws XNIException,
      IOException {
    config.parse(inputSource);
  }

  /**
   * @see org.apache.xerces.xni.parser.XMLParserConfiguration#addRecognizedFeatures(java.lang.String[])
   */
  @Override
  public void addRecognizedFeatures(String[] featureIds) {
    config.addRecognizedFeatures(featureIds);
  }

  /**
   * @see org.apache.xerces.xni.parser.XMLParserConfiguration#setFeature(java.lang.String, boolean)
   */
  @Override
  public void setFeature(String featureId, boolean state)
      throws XMLConfigurationException {
    config.setFeature(featureId, state);
  }

  /**
   * @see org.apache.xerces.xni.parser.XMLParserConfiguration#getFeature(java.lang.String)
   */
  @Override
  public boolean getFeature(String featureId) throws XMLConfigurationException {
    return config.getFeature(featureId);
  }

  /**
   * @see org.apache.xerces.xni.parser.XMLParserConfiguration#addRecognizedProperties(java.lang.String[])
   */
  @Override
  public void addRecognizedProperties(String[] propertyIds) {
    config.addRecognizedProperties(propertyIds);
  }

  /**
   * @see org.apache.xerces.xni.parser.XMLParserConfiguration#setProperty(java.lang.String, java.lang.Object)
   */
  @Override
  public void setProperty(String propertyId, Object value)
      throws XMLConfigurationException {
    config.setProperty(propertyId, value);
  }

  /**
   * @see org.apache.xerces.xni.parser.XMLParserConfiguration#getProperty(java.lang.String)
   */
  @Override
  public Object getProperty(String propertyId) throws XMLConfigurationException {
    return config.getProperty(propertyId);
  }

  /**
   * @see org.apache.xerces.xni.parser.XMLParserConfiguration#setErrorHandler(org.apache.xerces.xni.parser.XMLErrorHandler)
   */
  @Override
  public void setErrorHandler(XMLErrorHandler errorHandler) {
    config.setErrorHandler(errorHandler);
  }

  /**
   * @see org.apache.xerces.xni.parser.XMLParserConfiguration#getErrorHandler()
   */
  @Override
  public XMLErrorHandler getErrorHandler() {
    return config.getErrorHandler();
  }

  /**
   * @see org.apache.xerces.xni.parser.XMLParserConfiguration#setDocumentHandler(org.apache.xerces.xni.XMLDocumentHandler)
   */
  @Override
  public void setDocumentHandler(XMLDocumentHandler documentHandler) {
    config.setDocumentHandler(documentHandler);
  }

  /**
   * @see org.apache.xerces.xni.parser.XMLParserConfiguration#getDocumentHandler()
   */
  @Override
  public XMLDocumentHandler getDocumentHandler() {
    return config.getDocumentHandler();
  }

  /**
   * @see org.apache.xerces.xni.parser.XMLParserConfiguration#setDTDHandler(org.apache.xerces.xni.XMLDTDHandler)
   */
  @Override
  public void setDTDHandler(XMLDTDHandler dtdHandler) {
    config.setDTDHandler(dtdHandler);
  }

  /**
   * @see org.apache.xerces.xni.parser.XMLParserConfiguration#getDTDHandler()
   */
  @Override
  public XMLDTDHandler getDTDHandler() {
    return config.getDTDHandler();
  }

  /**
   * @see org.apache.xerces.xni.parser.XMLParserConfiguration#setDTDContentModelHandler(org.apache.xerces.xni.XMLDTDContentModelHandler)
   */
  @Override
  public void setDTDContentModelHandler(
      XMLDTDContentModelHandler dtdContentModelHandler) {
    config.setDTDContentModelHandler(dtdContentModelHandler);
    
  }

  /**
   * @see org.apache.xerces.xni.parser.XMLParserConfiguration#getDTDContentModelHandler()
   */
  @Override
  public XMLDTDContentModelHandler getDTDContentModelHandler() {
    return config.getDTDContentModelHandler();
  }

  /**
   * @see org.apache.xerces.xni.parser.XMLParserConfiguration#setEntityResolver(org.apache.xerces.xni.parser.XMLEntityResolver)
   */
  @Override
  public void setEntityResolver(XMLEntityResolver entityResolver) {
    config.setEntityResolver(entityResolver);
  }

  /**
   * @see org.apache.xerces.xni.parser.XMLParserConfiguration#getEntityResolver()
   */
  @Override
  public XMLEntityResolver getEntityResolver() {
    return config.getEntityResolver();
  }

  /**
   * @see org.apache.xerces.xni.parser.XMLParserConfiguration#setLocale(java.util.Locale)
   */
  @Override
  public void setLocale(Locale locale) throws XNIException {
    config.setLocale(locale);
  }

  /**
   * @see org.apache.xerces.xni.parser.XMLParserConfiguration#getLocale()
   */
  @Override
  public Locale getLocale() {
    return config.getLocale();
  }
}
