/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2011 George Bina
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.ditang.relaxng.defaults;

import java.io.IOException;

import org.apache.xerces.impl.Constants;
import org.apache.xerces.parsers.XIncludeAwareParserConfiguration;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.xni.XMLDocumentHandler;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.grammars.XMLGrammarPool;
import org.apache.xerces.xni.parser.XMLComponentManager;
import org.apache.xerces.xni.parser.XMLDocumentSource;

import com.thaiopensource.resolver.Resolver;

/**
 * @author george@oxygenxml.com
 * A parser configuration that adds in a module to add Relax NG specified default values.
 */
public class RelaxDefaultsParserConfiguration extends XIncludeAwareParserConfiguration  {
  
  /** Feature identifier: dynamic validation. */
  protected static final String DYNAMIC_VALIDATION =
      Constants.XERCES_FEATURE_PREFIX + Constants.DYNAMIC_VALIDATION_FEATURE;
  /** Feature identifier: validation. */
  protected static final String VALIDATION =
      Constants.SAX_FEATURE_PREFIX + Constants.VALIDATION_FEATURE;
  /**Schema validation**/
  protected static final String XERCES_SCHEMA_VALIDATION =
      "http://apache.org/xml/features/validation/schema";
    
  /**
   * An XML component that adds default attribute values by looking into a Relax NG schema
   * that includes a:defaultValue annotations.
   * See the Relax NG DTD compatibility specification.
   */
  protected RelaxNGDefaultsComponent fRelaxDefaults = null;

  /**
   * The special RNG resolver 
   */
  protected Resolver resolver;

  /**
   * Default constructor.
   */
  public RelaxDefaultsParserConfiguration() {
    this(null, null, null);
  }

  /** 
   * Constructs a parser configuration using the specified symbol table. 
   *
   * @param symbolTable The symbol table to use.
   */
  public RelaxDefaultsParserConfiguration(SymbolTable symbolTable) {
    this(symbolTable, null, null);
  }

  /**
   * Constructs a parser configuration using the specified symbol table and
   * grammar pool.
   * <p>
   *
   * @param symbolTable The symbol table to use.
   * @param grammarPool The grammar pool to use.
   * @param resolver The JING resolver
   */
  public RelaxDefaultsParserConfiguration(
      SymbolTable symbolTable,
      XMLGrammarPool grammarPool, Resolver resolver) {
    this(symbolTable, grammarPool, null, resolver);
  }

  /**
   * Constructs a parser configuration using the specified symbol table,
   * grammar pool, and parent settings.
   * <p>
   *
   * @param symbolTable    The symbol table to use.
   * @param grammarPool    The grammar pool to use.
   * @param parentSettings The parent settings.
   * @param resolver The resolver
   */
  public RelaxDefaultsParserConfiguration(
      SymbolTable symbolTable,
      XMLGrammarPool grammarPool,
      XMLComponentManager parentSettings, Resolver resolver) {
    super(symbolTable, grammarPool, parentSettings);
    this.resolver = resolver;
  }

  @Override
  public boolean parse(boolean complete) throws XNIException, IOException {
    if (fInputSource != null) {
      try {
        setFeature(DYNAMIC_VALIDATION, getFeature(VALIDATION) 
            || getFeature(XERCES_SCHEMA_VALIDATION));
      } catch(Exception ex) {
        //Could happen if the parser is not Xerces, most probably not.
        ex.printStackTrace();
      }
    }
    return super.parse(complete);
  }
  
  /** 
   * Configures the pipeline. 
   */
  @Override
  protected void configurePipeline() {
    super.configurePipeline();
    insertRelaxDefaultsComponent();
  }
  
  /**
   * Configures the pipeline.
   */
  @Override
  protected void configureXML11Pipeline() {
    super.configureXML11Pipeline();
    insertRelaxDefaultsComponent();
  }

  /**
   * Insert the Relax NG defaults component
   */
  protected void insertRelaxDefaultsComponent() {
    if (fRelaxDefaults == null) {
      fRelaxDefaults = new RelaxNGDefaultsComponent(resolver);
      addCommonComponent(fRelaxDefaults);
      fRelaxDefaults.reset(this);
    }
    XMLDocumentSource prev = fLastComponent;
    fLastComponent = fRelaxDefaults;
      
    XMLDocumentHandler next = prev.getDocumentHandler();
    prev.setDocumentHandler(fRelaxDefaults);
    fRelaxDefaults.setDocumentSource(prev);
    if (next != null) {
        fRelaxDefaults.setDocumentHandler(next);
        next.setDocumentSource(fRelaxDefaults);
    }
  }
  
}
