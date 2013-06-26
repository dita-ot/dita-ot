/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2005, 2006 All Rights Reserved.
 */
package org.dita.dost.platform;

import java.io.File;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Parser to parse description file of plugin.
 * @author Zhang, Yuan Peng
 */
final class DescParser extends DefaultHandler{

    private static final String EXTENSION_POINT_ELEM = "extension-point";
    private static final String EXTENSION_POINT_NAME_ATTR = "name";
    private static final String EXTENSION_POINT_ID_ATTR = "id";
    private static final String TEMPLATE_ELEM = "template";
    private static final String TEMPLATE_FILE_ATTR = "file";
    private static final String META_ELEM = "meta";
    private static final String META_VALUE_ATTR = "value";
    private static final String META_TYPE_ATTR = "type";
    private static final String REQUIRE_ELEM = "require";
    private static final String REQUIRE_IMPORTANCE_ATTR = "importance";
    private static final String REQUIRE_PLUGIN_ATTR = "plugin";
    private static final String FEATURE_ELEM = "feature";
    private static final String FEATURE_ID_ATTR = "extension";
    private static final String PLUGIN_ELEM = REQUIRE_PLUGIN_ATTR;
    private static final String PLUGIN_ID_ATTR = "id";
    
    private String currentPlugin = null;
    private final Features features;

    /**
     * Constructor initialize Feature with location.
     * @param location location
     */
    public DescParser(final File location, final File ditaDir) {
        super();
        features = new Features(location, ditaDir);
    }

    /**
     * Process configuration start element.
     */
    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
        if( PLUGIN_ELEM.equals(qName) ){
            currentPlugin = attributes.getValue(PLUGIN_ID_ATTR);
            features.setPluginId(currentPlugin);
        } else if (EXTENSION_POINT_ELEM.equals(qName)){
            addExtensionPoint(attributes);
        } else if (FEATURE_ELEM.equals(qName)){
            features.addFeature(attributes.getValue(FEATURE_ID_ATTR), attributes);
        } else if (REQUIRE_ELEM.equals(qName)){
            features.addRequire(attributes.getValue(REQUIRE_PLUGIN_ATTR), attributes.getValue(REQUIRE_IMPORTANCE_ATTR));
        } else if (META_ELEM.equals(qName)){
            features.addMeta(attributes.getValue(META_TYPE_ATTR), attributes.getValue(META_VALUE_ATTR));
        } else if (TEMPLATE_ELEM.equals(qName)){
            features.addTemplate(attributes.getValue(TEMPLATE_FILE_ATTR));
        }
    }

    /**
     * Get plug-in features.
     * 
     * @return plug-in features
     */
    public Features getFeatures() {
        return features;
    }

    /**
     * Add extension point.
     * 
     * @param atts extension point element attributes
     * @throws IllegalArgumentException if extension ID is {@code null}
     */
    private void addExtensionPoint(final Attributes atts) {
        final String id = atts.getValue(EXTENSION_POINT_ID_ATTR);
        if (id == null) {
            throw new IllegalArgumentException(EXTENSION_POINT_ID_ATTR + " attribute not set on extension-point");
        }
        final String name = atts.getValue(EXTENSION_POINT_NAME_ATTR);
        features.addExtensionPoint(new ExtensionPoint(id, name, currentPlugin));
    }
}
