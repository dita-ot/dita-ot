/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2005, 2006 All Rights Reserved.
 */
package org.dita.dost.platform;

import static org.dita.dost.util.Constants.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;

import org.dita.dost.util.FileUtils;

/**
 * Collection of features.
 * @author Zhang, Yuan Peng
 */
final class Features {

    private String id;
    private final File location;
    private final File ditaDir;
    private final Map<String, ExtensionPoint> extensionPoints;
    private final Hashtable<String,String> featureTable;
    private final List<PluginRequirement> requireList;
    private final Hashtable<String,String> metaTable;
    private final List<String> templateList;

    /**
     * Constructor init location.
     * @param location location
     * @param ditaDir base directory
     */
    public Features(final File location, final File ditaDir) {
        this.location = location;
        this.ditaDir = ditaDir;
        extensionPoints= new HashMap<String, ExtensionPoint>();
        featureTable = new Hashtable<String,String>(INT_16);
        requireList = new ArrayList<PluginRequirement>(INT_8);
        metaTable = new Hashtable<String,String>(INT_16);
        templateList = new ArrayList<String>(INT_8);
    }

    /**
     * Return the feature location.
     * @return location
     */
    public File getLocation(){
        return location;
    }

    /**
     * Get DITA-OT base directory
     * @return base directory
     */
    public File getDitaDir() {
        return ditaDir;
    }

    void setPluginId(final String id) {
        this.id = id;
    }

    String getPluginId() {
        return id;
    }

    Map<String, ExtensionPoint> getExtensionPoints() {
        return Collections.unmodifiableMap(extensionPoints);
    }

    /**
     * Return the feature name by id.
     * @param id feature id
     * @return feature name
     */
    public String getFeature(final String id){
        return featureTable.get(id);
    }

    /**
     * Return the set of all features.
     * @return features
     */
    public Map<String,String> getAllFeatures(){
        return featureTable;
    }

    void addExtensionPoint(final ExtensionPoint extensionPoint) {
        extensionPoints.put(extensionPoint.id, extensionPoint);
    }

    /**
     * Add feature to the feature table.
     * @param id feature id
     * @param value feature value
     * @param type feature type, may be {@code null}
     * @deprecated use {@link #addFeature(String, Attributes)} instead
     */
    @Deprecated
    public void addFeature(final String id, final String value, final String type) {
        final AttributesImpl atts = new AttributesImpl();
        atts.addAttribute("", "value", "value", "CDATA", value);
        if (type != null) {
            atts.addAttribute("", "type", "type", "CDATA", type);
        }
        addFeature(id, atts);
    }

    /**
     * Add feature to the feature table.
     * @param id feature id
     * @param attributes configuration element attributes
     */
    public final void addFeature(final String id, final Attributes attributes){
        boolean isFile;
        String value = attributes.getValue("file");
        if (value != null) {
            isFile = true;
        } else {
            value = attributes.getValue("value");
            isFile = "file".equals(attributes.getValue("type"));
        }
        final StringTokenizer valueTokenizer = new StringTokenizer(value, Integrator.FEAT_VALUE_SEPARATOR);
        final StringBuffer valueBuffer = new StringBuffer();
        if (featureTable.containsKey(id)) {
            valueBuffer.append(featureTable.get(id));
            valueBuffer.append(Integrator.FEAT_VALUE_SEPARATOR);
        }
        while(valueTokenizer.hasMoreElements()){
            final String valueElement = valueTokenizer.nextToken();
            if(valueElement!=null && valueElement.trim().length() != 0){
                if(isFile && !FileUtils.isAbsolutePath(valueElement)){
                    valueBuffer.append(location).append(File.separatorChar);
                }
                valueBuffer.append(valueElement.trim());
                if(valueTokenizer.hasMoreElements()){
                    valueBuffer.append(Integrator.FEAT_VALUE_SEPARATOR);
                }
            }
        }
        featureTable.put(id, valueBuffer.toString());
    }

    /**
     * Add the required feature id.
     * @param id feature id
     */
    public void addRequire(final String id){
        final PluginRequirement requirement = new PluginRequirement();
        requirement.addPlugins(id);
        requireList.add(requirement);
    }

    /**
     * Add the required feature id.
     * @param id feature id
     * @param importance importance
     */
    public void addRequire(final String id, final String importance){
        final PluginRequirement requirement = new PluginRequirement();
        requirement.addPlugins(id);
        if (importance != null) {
            requirement.setRequired(importance.equals("required"));
        }
        requireList.add(requirement);
    }

    /**
     * Get the iterator of required list.
     * @return iterator
     */
    public Iterator<PluginRequirement> getRequireListIter(){
        return requireList.iterator();
    }

    /**
     * Add meta info to meta table.
     * @param type type
     * @param value value
     */
    public void addMeta(final String type, final String value){
        metaTable.put(type, value);
    }

    /**
     * Return meat info specifying type.
     * @param type type
     * @return meat info
     */
    public String getMeta(final String type){
        return metaTable.get(type);
    }

    /**
     * Add a template.
     * @param file file name
     */
    public void addTemplate(final String file){
        templateList.add(file);
    }
    /**
     * get all templates.
     * @return templates list
     */
    public List<String> getAllTemplates(){
        return templateList;
    }
}
