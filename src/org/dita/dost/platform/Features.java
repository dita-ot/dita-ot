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
	private Hashtable featureTable;
	private List requireList;
	private Hashtable metaTable;
	private List templateList;

	/**
	 * Default constructor
	 */
	public Features() {
		super();
		featureTable = new Hashtable(Constants.INT_16);
		requireList = new ArrayList(Constants.INT_8);
		metaTable = new Hashtable(Constants.INT_16);
		templateList = new ArrayList(Constants.INT_8);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Constructor init location 
	 * @param location
	 */
	public Features(String location) {
		this.location = location;
		featureTable = new Hashtable(Constants.INT_16);
		requireList = new ArrayList(Constants.INT_8);
		metaTable = new Hashtable(Constants.INT_16);
		templateList = new ArrayList(Constants.INT_8);
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
		return (String) featureTable.get(id);
	}
	
	/**
	 * Return the set of all features.
	 * @return
	 */
	public Set getAllFeatures(){
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
		requireList.add(id);
	}
	
	/**
	 * Get the iterator of required list.
	 * @return
	 */
	public Iterator getRequireListIter(){
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
		return (String) metaTable.get(type);
	}
	
	/**
	 * Add a template.
	 * @param file
	 */
	public void addTemplate(String file){
		templateList.add(file);
	}
	
	public List getAllTemplates(){
		return templateList;
	}
}
