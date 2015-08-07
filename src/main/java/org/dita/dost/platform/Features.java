/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2005, 2006 All Rights Reserved.
 */
package org.dita.dost.platform;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.w3c.dom.Element;

import org.dita.dost.util.FileUtils;

/**
 * Collection of features.
 * @author Zhang, Yuan Peng
 */
final class Features {

    private String id;
    private final File pluginDir;
    private final File ditaDir;
    private final Map<String, ExtensionPoint> extensionPoints;
    private final Hashtable<String,List<String>> featureTable;
    private final List<PluginRequirement> requireList;
    private final Hashtable<String,String> metaTable;
    private final List<String> templateList;

    /**
     * Constructor init pluginDir.
     * @param pluginDir absolute plugin directory path
     * @param ditaDir base directory
     */
    public Features(final File pluginDir, final File ditaDir) {
        this.pluginDir = pluginDir;
        this.ditaDir = ditaDir;
        extensionPoints= new HashMap<>();
        featureTable = new Hashtable<>(16);
        requireList = new ArrayList<>(8);
        metaTable = new Hashtable<>(16);
        templateList = new ArrayList<>(8);
    }

    /**
     * Return the feature pluginDir.
     * @return pluginDir
     */
    public File getPluginDir(){
        return pluginDir;
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
    public List<String> getFeature(final String id){
        return featureTable.get(id);
    }

    /**
     * Return the set of all features.
     * @return features
     */
    public Map<String, List<String>> getAllFeatures(){
        return featureTable;
    }

    void addExtensionPoint(final ExtensionPoint extensionPoint) {
        extensionPoints.put(extensionPoint.id, extensionPoint);
    }

    /**
     * Add feature to the feature table.
     * @param id feature id
     * @param elem configuration element
     */
    public final void addFeature(final String id, final Element elem) {
        boolean isFile;
        String value = elem.getAttribute("file");
        if (!value.isEmpty()) {
            isFile = true;
        } else {
            value = elem.getAttribute("value");
            isFile = "file".equals(elem.getAttribute("type"));
        }
        final StringTokenizer valueTokenizer = new StringTokenizer(value, Integrator.FEAT_VALUE_SEPARATOR);
        final List<String> valueBuffer = new ArrayList<>();
        if (featureTable.containsKey(id)) {
            valueBuffer.addAll(featureTable.get(id));
        }
        while (valueTokenizer.hasMoreElements()) {
            final String valueElement = valueTokenizer.nextToken();
            if (valueElement != null && valueElement.trim().length() != 0) {
                if (isFile && !FileUtils.isAbsolutePath(valueElement)) {
                    valueBuffer.add(pluginDir + File.separator + valueElement.trim());
                } else {
                    valueBuffer.add(valueElement.trim());
                }
            }
        }
        featureTable.put(id, valueBuffer);
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
