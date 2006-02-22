/*
 * (c) Copyright IBM Corp. 2005, 2006 All Rights Reserved.
 */
package org.dita.dost.platform;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

/**
 *
 * @author Zhang, Yuan Peng
 */
public class Features {
	private String location;
	private Hashtable featureTable;
	private ArrayList requireList;
	private Hashtable metaTable;

	public Features(String location) {
		this.location = location;
		featureTable = new Hashtable(16);
		requireList = new ArrayList(8);
		metaTable = new Hashtable(16);
	}
	
	public String getLocation(){
		return location;
	}
	
	public String getFeature(String id){
		return (String) featureTable.get(id);
	}
	
	public Set getAllFeatures(){
		return featureTable.entrySet();
	}
	
	public void addFeature(String id, String value, String type){
		StringTokenizer valueTokenizer = new StringTokenizer(value,",");
		StringBuffer valueBuffer = new StringBuffer();
		while(valueTokenizer.hasMoreElements()){
			String valueElement = (String) valueTokenizer.nextElement();
			if(valueElement!=null && valueElement.trim()!=null){
				if("file".equals(type)){
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
	
	public void addRequire(String id){
		requireList.add(id);
	}
	
	public Iterator getRequireListIter(){
		return requireList.iterator();
	}
	
	public void addMeta(String type, String value){
		metaTable.put(type, value);
	}
	
	public String getMeta(String type){
		return (String) metaTable.get(type);
	}
}
