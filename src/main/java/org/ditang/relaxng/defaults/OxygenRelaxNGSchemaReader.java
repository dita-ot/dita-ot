/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2011 George Bina
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.ditang.relaxng.defaults;

import java.io.IOException;

import javax.xml.transform.sax.SAXSource;

import org.ditang.relaxng.defaults.RelaxNGDefaultValues.DefaultValuesCollector;
import org.relaxng.datatype.DatatypeLibraryFactory;
import org.relaxng.datatype.helpers.DatatypeLibraryLoader;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import com.thaiopensource.relaxng.parse.IllegalSchemaException;
import com.thaiopensource.relaxng.pattern.FeasibleTransform;
import com.thaiopensource.relaxng.pattern.IdTypeMap;
import com.thaiopensource.relaxng.pattern.IdTypeMapBuilder;
import com.thaiopensource.relaxng.pattern.Pattern;
import com.thaiopensource.relaxng.pattern.SchemaBuilderImpl;
import com.thaiopensource.relaxng.pattern.SchemaPatternBuilder;
import com.thaiopensource.resolver.xml.sax.SAXResolver;
import com.thaiopensource.util.PropertyId;
import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.validate.AbstractSchema;
import com.thaiopensource.validate.CombineSchema;
import com.thaiopensource.validate.IncorrectSchemaException;
import com.thaiopensource.validate.ResolverFactory;
import com.thaiopensource.validate.Schema;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.Validator;
import com.thaiopensource.validate.prop.rng.RngProperty;
import com.thaiopensource.validate.prop.wrap.WrapProperty;
import com.thaiopensource.validate.rng.impl.FeasibleIdTypeMapSchema;
import com.thaiopensource.validate.rng.impl.IdTypeMapSchema;
import com.thaiopensource.validate.rng.impl.PatternSchema;
import com.thaiopensource.validate.rng.impl.SchemaReaderImpl;

/**
 * Schema Reader for RelaxNG
 * @author george@oxygenxml.com
 */
public abstract class OxygenRelaxNGSchemaReader extends SchemaReaderImpl {
	  /**
	   * The Schema Wrapper.
	   */
	  public static class SchemaWrapper implements Schema {
	    /**
	     * The wrapped schema.
	     */
	    private Schema schema;
	    
	    /**
	     * The start pattern.
	     */
	    private Pattern start;
	    
	    /**
	     * The ID Type map.
	     */
	    private IdTypeMap idTypeMap;

	    /**
	     * Get start Pattern.
	     * 
	     * @return start pattern.
	     */
	    public Pattern getStart() {
	      return start;
	    }

	    /**
	     * Set start pattern.
	     * 
	     * @param start The start pattern.
	     */
	    public void setStart(Pattern start) {
	      this.start = start;
	    }

	    /**
	     * Constructor.
	     * 
	     * @param wrapped The wrapped schema.
	     */
	    public SchemaWrapper(Schema wrapped) {
	      schema = wrapped;
	    }

	    /**
	     * @see com.thaiopensource.validate.Schema#createValidator(com.thaiopensource.util.PropertyMap)
	     */
	    public Validator createValidator(PropertyMap properties) {
	      return schema.createValidator(properties);
	    }

	    /**
	     * @see com.thaiopensource.validate.Schema#getProperties()
	     */
	    public PropertyMap getProperties() {
	      return schema.getProperties();
	    }
	    /**
	     * Get the ID Type map.
	     * 
	     * @return Returns the idTypeMap.
	     */
	    private IdTypeMap getIdTypeMap() {
	      return idTypeMap;
	    }
	    	    
	    /**
	     * Set the ID Type map.
	     * 
	     * @param idTypeMap The idTypeMap to set.
	     */
	    public void setIdTypeMap(IdTypeMap idTypeMap) {
	      this.idTypeMap = idTypeMap;
	    }
	  }
	
	
  /**
   * Supported property ids.
   */
  private static final PropertyId<?>[] supportedPropertyIds = {
    ValidateProperty.XML_READER_CREATOR,
    ValidateProperty.ERROR_HANDLER,
    ValidateProperty.ENTITY_RESOLVER,
    ValidateProperty.URI_RESOLVER,
    ValidateProperty.RESOLVER,
    RngProperty.DATATYPE_LIBRARY_FACTORY,
    RngProperty.CHECK_ID_IDREF,
    RngProperty.FEASIBLE,
    WrapProperty.ATTRIBUTE_OWNER,
  };
  
  /***
   * Create a schema from an input source and a property map.
   */
  public Schema createSchema(SAXSource source, PropertyMap properties) throws IOException, SAXException, IncorrectSchemaException {
    SchemaPatternBuilder spb = new SchemaPatternBuilder();
    SAXResolver resolver = ResolverFactory.createResolver(properties);
    ErrorHandler eh = properties.get(ValidateProperty.ERROR_HANDLER);
    DatatypeLibraryFactory dlf = properties.get(RngProperty.DATATYPE_LIBRARY_FACTORY);
    if (dlf == null) {
      //Create a new Data Type Library Loader
      dlf = new DatatypeLibraryLoader();
    }
    try {
      Pattern start = SchemaBuilderImpl.parse(createParseable(source, resolver, eh, properties), eh, dlf, spb,
          properties.contains(WrapProperty.ATTRIBUTE_OWNER));
      //Wrap the pattern
      return wrapPattern2(start, spb, properties);
    }
    catch (IllegalSchemaException e) {
      throw new IncorrectSchemaException();
    }
  }

  /**
   * Make a schema wrapper.
   * 
   * @param start Start pattern.
   * @param spb The schema pattern builder.
   * @param properties The properties map.
   * @return The schema wrapper.
   */
  private static SchemaWrapper wrapPattern2(Pattern start, SchemaPatternBuilder spb, PropertyMap properties)
    throws SAXException, IncorrectSchemaException {
    
    if (properties.contains(RngProperty.FEASIBLE)) {
      //Use a feasible transform
      start = FeasibleTransform.transform(spb, start);
    }
    //Get properties for supported IDs
    properties = AbstractSchema.filterProperties(properties, supportedPropertyIds);
    Schema schema = new PatternSchema(spb, start, properties);
    IdTypeMap idTypeMap = null;
    if (spb.hasIdTypes() && properties.contains(RngProperty.CHECK_ID_IDREF)) {
      //Check ID/IDREF
      ErrorHandler eh = properties.get(ValidateProperty.ERROR_HANDLER);
      idTypeMap = new IdTypeMapBuilder(eh, start).getIdTypeMap();
      if (idTypeMap == null) {
        throw new IncorrectSchemaException();
      }
      Schema idSchema;
      if (properties.contains(RngProperty.FEASIBLE)) {
        idSchema = new FeasibleIdTypeMapSchema(idTypeMap, properties);
      } else {
        idSchema = new IdTypeMapSchema(idTypeMap, properties);
      }
      schema = new CombineSchema(schema, idSchema, properties);
    }
    //Wrap the schema
    SchemaWrapper sw = new SchemaWrapper(schema);
    sw.setStart(start);
    sw.setIdTypeMap(idTypeMap);      
    return sw;
  }
}