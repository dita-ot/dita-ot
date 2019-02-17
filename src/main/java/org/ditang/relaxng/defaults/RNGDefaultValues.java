/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2011 George Bina
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.ditang.relaxng.defaults;

import javax.xml.transform.sax.SAXSource;

import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import com.thaiopensource.relaxng.parse.Parseable;
import com.thaiopensource.relaxng.parse.sax.SAXParseable;
import com.thaiopensource.relaxng.pattern.AnnotationsImpl;
import com.thaiopensource.relaxng.pattern.CommentListImpl;
import com.thaiopensource.relaxng.pattern.NameClass;
import com.thaiopensource.relaxng.pattern.Pattern;
import com.thaiopensource.resolver.Resolver;
import com.thaiopensource.resolver.xml.sax.SAXResolver;
import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.util.VoidValue;
import com.thaiopensource.validate.SchemaReader;

/**
 * @author george@oxygenxml.com
 * 
 */
public class RNGDefaultValues extends RelaxNGDefaultValues {
  /**
   * Schema reader for RNG schemas.
   */
  public static class OxygenXMLSchemaReader extends OxygenRelaxNGSchemaReader {
    /**
     * The schema reader instance.
     */
    private static final SchemaReader theInstance = new OxygenXMLSchemaReader();

    /**
     * Private constructor.
     */
    private OxygenXMLSchemaReader() {
      super();
    }

    /**
     * Get the singleton instance.
     * 
     * @return The instance.
     */
    public static SchemaReader getInstance() {
      return theInstance;
    }

    /**
     * Creates a parseable object from a catalog resolved input source
     * associated to a RNG schema.
     * 
     * @return the parseable object
     */
    @Override
    protected Parseable<Pattern, NameClass, Locator, VoidValue, CommentListImpl, AnnotationsImpl> createParseable(
        SAXSource source, SAXResolver resolver, ErrorHandler eh,
        PropertyMap properties) throws SAXException {
      if (source.getXMLReader() == null) {
        source = new SAXSource(resolver.createXMLReader(),
            source.getInputSource());
      }
      return new SAXParseable<>(
              source, resolver, eh);
    }

  }

  /**
   * Constructor
   * @param resolver The resolver
   * @param eh The error handler
   */
  public RNGDefaultValues(Resolver resolver, ErrorHandler eh) {
    super(resolver, eh);    
  }

  /**
   * Return the <code>OxygenXMLSchemaReader</code> instance.
   */
  @Override
  protected SchemaReader getSchemaReader() {
    return OxygenXMLSchemaReader.getInstance();
  }
}
