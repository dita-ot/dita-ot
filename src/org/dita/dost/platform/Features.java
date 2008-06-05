/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2005, 2006 All Rights Reserved.
 */
package org.dita.dost.platform;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.dita.dost.util.Constants;
import org.dita.dost.util.FileUtils;

/**
 * Collection of features
 * @author Zhang, Yuan Peng
 */
public class Features {
	private String location = null;
	private Hashtable<String,String> featureTable;
	private List<PluginRequirement> requireList;
	private Hashtable<String,String> metaTable;
	private List<String> templateList;

	/**
	 * Default constructor
	 */
	public Features() {
		super();
		featureTable = new Hashtable<String,String>(Constants.INT_16);
		requireList = new ArrayList<PluginRequirement>(Constants.INT_8);
		metaTable = new Hashtable<String,String>(Constants.INT_16);
		templateList = new ArrayList<String>(Constants.INT_8);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Constructor init location 
	 * @param location
	 */
	public Features(String location) {
		this.location = location;
		featureTable = new Hashtable<String,String>(Constants.INT_16);
		requireList = new ArrayList<PluginRequirement>(Constants.INT_8);
		metaTable = new Hashtable<String,String>(Constants.INT_16);
		templateList = new ArrayList<String>(Constants.INT_8);
	}
	
	/**
	 * Return the feature location
	 * @return
	 */
	public String getLocation(){
		return location;
	}
	
	/**
	 * Return the feature name by id.
	 * @param id
	 * @return
	 */
	public String getFeature(String id){
		return featureTable.get(id);
	}
	
	/**
	 * Return the set of all features.
	 * @return
	 */
	public Set<Map.Entry<String,String>> getAllFeatures(){
		return featureTable.entrySet();
	}
	
	/**
	 * Add feature to the feature table.
	 * @param id
	 * @param value
	 * @param type
	 */
	public void addFeature(String id, String value, String type){
		StringTokenizer valueTokenizer = new StringTokenizer(value,",");
		StringBuffer valueBuffer = new StringBuffer();
		while(valueTokenizer.hasMoreElements()){
			String valueElement = (String) valueTokenizer.nextElement();
			if(valueElement!=null && valueElement.trim()!=null){
				if("file".equals(type) && !FileUtils.isAbsolutePath(valueElement)){
					valueBuffer.append(location).append(File.separatorChar);
				}
				valueBuffer.append(valueElement.trim());
				if(valueTokenizer.hasMoreElements()){
					valueBuffer.append(",");
				}
			}
		}
		featureTable.put(id, valueBuffer.toString());
	}
	
	/**
	 * Add the required feature id.
	 * @param id
	 */
	public void addRequire(String id){
		PluginRequirement requirement = new PluginRequirement();
		requirement.addPlugins(id);
		requireList.add(requirement);
	}

	/**
	 * Add the required feature id.
	 * @param id
	 * @param importance
	 */
	public void addRequire(String id, String importance){
		PluginRequirement requirement = new PluginRequirement();
		requirement.addPlugins(id);
		if (importance != null) {
			requirement.setRequired(importance.equals("required"));
		}
		requireList.add(requirement);
	}

	/**
	 * Get the iterator of required list.
	 * @return
	 */
	public Iterator<PluginRequirement> getRequireListIter(){
		return requireList.iterator();
	}
	
	/**
	 * Add meta info to meta table
	 * @param type
	 * @param value
	 */
	public void addMeta(String type, String value){
		metaTable.put(type, value);
	}
	
	/**
	 * Return meat info specifying type
	 * @param type
	 * @return
	 */
	public String getMeta(String type){
		return metaTable.get(type);
	}
	
	/**
	 * Add a template.
	 * @param file
	 */
	public void addTemplate(String file){
		templateList.add(file);
	}
	
	public List<String> getAllTemplates(){
		return templateList;
	}
}
