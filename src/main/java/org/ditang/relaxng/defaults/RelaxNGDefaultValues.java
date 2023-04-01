/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2011 George Bina
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.ditang.relaxng.defaults;

import com.thaiopensource.relaxng.pattern.DefaultValuesExtractor;
import com.thaiopensource.relaxng.pattern.Pattern;
import com.thaiopensource.resolver.Resolver;
import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.util.PropertyMapBuilder;
import com.thaiopensource.validate.SchemaReader;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.Validator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.ditang.relaxng.defaults.OxygenRelaxNGSchemaReader.SchemaWrapper;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Relax NG default values gatherer.
 *
 * @author george@oxygenxml.com
 */
public abstract class RelaxNGDefaultValues {
  /** Error handler */
  private final ErrorHandler eh;
  /** Resolver */
  private final Resolver resolver;
  /** <code>true</code> to keep a reference to the schema. */
  private final boolean keepSchema;

  /**
   * Constructor.
   *
   * @param resolver The resolver
   * @param eh The error handler
   */
  protected RelaxNGDefaultValues(Resolver resolver, ErrorHandler eh) {
    this(resolver, eh, false);
  }

  /**
   * Constructor.
   *
   * @param resolver The resolver
   * @param eh The error handler
   * @param keepSchema <code>true</code> to keep a reference to the schema.
   */
  protected RelaxNGDefaultValues(Resolver resolver, ErrorHandler eh, boolean keepSchema) {
    this.resolver = resolver;
    this.eh = eh;
    this.keepSchema = keepSchema;
  }

  /**
   * @return a schema reader. Can be an XML or compact syntax schema reader.
   */
  protected abstract SchemaReader getSchemaReader();

  /** Stores collected values. */
  private DefaultValuesCollector defaultValuesCollector = null;
  /** <code>true</code> if there were update errors. */
  private boolean hasUpdateErrors;
  /** The schema reference */
  private SchemaWrapper schema;

  /** Collects default values. Listener for the default values extractor. */
  class DefaultValuesCollector implements DefaultValuesExtractor.DefaultValuesListener {
    /** Stores the default attributes as a hash map with the element info as key. */
    private final HashMap<String, List<Attribute>> defaults = new HashMap<>();

    /**
     * Constructor.
     *
     * @param start The Relax NG schema pattern.
     */
    public DefaultValuesCollector(Pattern start) {
      new DefaultValuesExtractor(this).parsePattern(start);
    }

    /**
     * Get a key for an element.
     *
     * @return A string formed from the element local name and its namespace.
     */
    private String getKey(String elementLocalName, String elementNamespace) {
      return elementLocalName + "#" + (elementNamespace == null ? "" : elementNamespace);
    }

    /**
     * Get the default attributes for an element.
     *
     * @param elementLocalName The element local name.
     * @param elementNamespace The element namespace. Use null or empty for no namespace.
     * @return A list of Attribute objects or null if no defaults.
     */
    private List<Attribute> getDefaultAttributes(String elementLocalName, String elementNamespace) {
      return defaults.get(getKey(elementLocalName, elementNamespace));
    }

    /**
     * Default attribute notification.
     *
     * @see
     *     com.thaiopensource.relaxng.pattern.DefaultValuesExtractor.DefaultValuesListener#defaultValue(java.lang.String,
     *     java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void defaultValue(
        String elementLocalName,
        String elementNamespace,
        String attributeLocalName,
        String attributeNamepsace,
        String value) {
      String key = getKey(elementLocalName, elementNamespace);
      List<Attribute> list = defaults.computeIfAbsent(key, k -> new ArrayList<>());
      list.add(new Attribute(attributeLocalName, attributeNamepsace, value));
    }
  }

  /** Stores information about a default attribute. */
  class Attribute {
    /** The attribute local name */
    final String localName;
    /** The attribute namespace */
    final String namespace;
    /** The attribute default value */
    final String value;

    public Attribute(String localName, String namespace, String value) {
      this.localName = localName;
      this.namespace = namespace;
      this.value = value;
    }
  }

  /**
   * Updates the annotation model.
   *
   * @param in The schema input source.
   */
  public void update(InputSource in) throws SAXException {
    defaultValuesCollector = null;
    PropertyMapBuilder builder = new PropertyMapBuilder();
    // Set the resolver
    builder.put(ValidateProperty.RESOLVER, resolver);
    builder.put(ValidateProperty.ERROR_HANDLER, eh);
    PropertyMap properties = builder.toPropertyMap();
    try {
      SchemaWrapper sw = (SchemaWrapper) getSchemaReader().createSchema(in, properties);
      if (keepSchema) {
        schema = sw;
      }
      Pattern start = sw.getStart();
      defaultValuesCollector = new DefaultValuesCollector(start);
      hasUpdateErrors = false;
    } catch (Exception e) {
      hasUpdateErrors = true;
      eh.warning(new SAXParseException("Error loading defaults: " + e.getMessage(), null, e));
    }
  }

  /**
   * @return Returns <code>true</code> if there were errors updating the defaults.
   */
  public boolean hasUpdateErrors() {
    return hasUpdateErrors;
  }

  /**
   * Get the default attributes for an element.
   *
   * @param localName The element local name.
   * @param namespace The element namespace. Use null or empty for no namespace.
   * @return A list of Attribute objects or null if no defaults.
   */
  public List<Attribute> getDefaultAttributes(String localName, String namespace) {
    if (defaultValuesCollector != null) {
      return defaultValuesCollector.getDefaultAttributes(localName, namespace);
    }
    return null;
  }

  /**
   * @param properties The properties
   * @return Returns the validator.
   */
  public Validator createValidator(PropertyMap properties) {
    return schema.createValidator(properties);
  }

  /**
   * @return Returns <code>true</code> if keeps a reference to the schema.
   */
  public boolean isKeepSchema() {
    return keepSchema;
  }
}
