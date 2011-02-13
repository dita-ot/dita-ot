/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.util;

import java.io.File;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import org.dita.dost.log.DITAOTJavaLogger;
import org.xml.sax.XMLReader;

/**
 * Utility that topic merge utilize. 
 * 
 */
public class MergeUtils {

	private static MergeUtils instance = null;
	private Hashtable<String, String> idMap;
	private int index;
	private Set<String> visitSet;
	private DITAOTJavaLogger logger = null;
	
	/**
	 * Default Constructor
	 */
	private MergeUtils() {
		super();
		// TODO Auto-generated constructor stub
		idMap = new Hashtable<String, String>();
		visitSet = new HashSet<String>(Constants.INT_256);
		logger = new DITAOTJavaLogger();
		index = 0;
	}

	/**
	 * Return the MergeUtils instance. Singleton.
	 * @return MergeUtils
	 */
	public static synchronized MergeUtils getInstance(){
		if(instance == null){
			instance = new MergeUtils();
		}
		return instance;
	}
	
	/**
	 * Resets all internal data structures.
	 */
	public void reset(){
		idMap.clear();
		visitSet.clear();
		index = 0;
	}
	
	/**
	 * Find the topic id from idMap.
	 * @param Id topic id
	 * @return true if find and false otherwise
	 */
	public boolean findId(String Id){
		return (Id != null && idMap.containsKey(FileUtils.removeRedundantNames(Id.trim().replaceAll(Constants.DOUBLE_BACK_SLASH,
				Constants.SLASH), Constants.SLASH)))
			? true
			: false;
	}
	
	/**
	 * Add topic id to the idMap.
	 * @param Id topic id
	 * @return updated topic id
	 */
	public String addId (String Id){
		String localId = Id;
		if(Id == null){
			return null;
		}
		localId=Id.trim().replaceAll(Constants.DOUBLE_BACK_SLASH,
				Constants.SLASH);
		index ++;
		idMap.put(FileUtils.removeRedundantNames(localId, Constants.SLASH),"unique_"+Integer.toString(index));
		return "unique_"+Integer.toString(index);
	}

	/**
	 * Add topic id-value pairs to idMap.
	 * @param Id id
	 * @param Value value
	 */
	public void addId (String Id, String Value){
		if(Id != null && Value != null){
			String localId=Id.trim().replaceAll(Constants.DOUBLE_BACK_SLASH,
					Constants.SLASH);
			String localValue = Value.trim();
			idMap.put(FileUtils.removeRedundantNames(localId, Constants.SLASH), localValue);
		}		
	}
	
	/**
	 * Return the value corresponding to the id.
	 * @param Id id
	 * @return value
	 */
	public String getIdValue (String Id){
		String localId = Id;
		if (Id==null){
			return null;
		}
		localId = Id.trim().replaceAll(Constants.DOUBLE_BACK_SLASH,
				Constants.SLASH);
		return (String) idMap.get(FileUtils.removeRedundantNames(localId, Constants.SLASH));
	}
	
	/**
	 * Return if this path has been visited before.
	 * @param path path
	 * @return true if has been visited
	 */
	public boolean isVisited(String path){
		int idx;
		String localPath = path;
		idx = path.indexOf(Constants.SHARP);
		if(idx != -1){
			localPath=localPath.substring(0,idx);
		}
		return visitSet.contains(FileUtils.removeRedundantNames(localPath.trim().replaceAll(Constants.DOUBLE_BACK_SLASH,
				Constants.SLASH), Constants.SLASH));
	}
	
	/**
	 * Visit the path.
	 * @param path path
	 */
	public void visit(String path){
		String localPath = path;
		int idx = path.indexOf(Constants.SHARP);
		if(idx != -1){
			localPath=localPath.substring(0,idx);
		}
		visitSet.add(FileUtils.removeRedundantNames(localPath.trim().replaceAll(Constants.DOUBLE_BACK_SLASH,
				Constants.SLASH), Constants.SLASH));
	}
	
	/**
	 * 
	 * Get the first topic id.
	 * @param path file path
	 * @param dir file dir
	 * @param useCatalog whether use catalog file for validation
	 * @return topic id
	 */
	public String getFirstTopicId(String path, String dir, boolean useCatalog){
		String localPath = path;
		String localDir = dir;
		TopicIdParser parser;
		XMLReader reader;
		StringBuffer firstTopicId = new StringBuffer();
		
		if(path != null && dir != null){
			localPath = localPath.trim();
			localDir = localDir.trim();
		}else{
			return null;
		}
		parser = new TopicIdParser(firstTopicId);
		try{
            reader = StringUtils.getXMLReader();
            reader.setContentHandler(parser);
            
            if(useCatalog){
            	try {
        			Class.forName(Constants.RESOLVER_CLASS);
        			reader.setEntityResolver(CatalogUtils.getCatalogResolver());
        		}catch (ClassNotFoundException e){
        			logger.logException(e);
        		}
            }
            reader.parse(localDir+File.separator+localPath);
        }catch (Exception e){
            logger.logException(e);
        }
		return firstTopicId.toString();
		
	}
	
}
