package org.dita.dost.util;

import java.io.File;
import java.util.HashSet;
import java.util.Hashtable;

import org.dita.dost.log.DITAOTJavaLogger;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class MergeUtils {

	private static MergeUtils instance = null;
	private Hashtable idMap;
	private int index;
	private HashSet visitSet;
	private DITAOTJavaLogger logger = null;
	
	private MergeUtils() {
		super();
		// TODO Auto-generated constructor stub
		idMap = new Hashtable();
		visitSet = new HashSet(Constants.INT_256);
		logger = new DITAOTJavaLogger();
		index = 0;
	}

	public static MergeUtils getInstance(){
		if(instance == null){
			instance = new MergeUtils();
		}
		return instance;
	}
	
	public boolean findId(String Id){
		return (Id != null && idMap.containsKey(Id.trim().replaceAll(Constants.DOUBLE_BACK_SLASH,
				Constants.SLASH)))?true:false;
	}
	
	public String addId (String Id){
		if(Id == null){
			return null;
		}
		String localId=Id.trim().replaceAll(Constants.DOUBLE_BACK_SLASH,
				Constants.SLASH);
		index ++;
		idMap.put(localId,"unique_"+Integer.toString(index));
		return "unique_"+Integer.toString(index);
	}
	
	public String getIdValue (String Id){
		if (Id==null){
			return null;
		}
		String localId = Id.trim().replaceAll(Constants.DOUBLE_BACK_SLASH,
				Constants.SLASH);
		return (String) idMap.get(localId);
	}
	
	public void addId (String Id, String Value){
		if(Id != null && Value != null){
			String localId=Id.trim().replaceAll(Constants.DOUBLE_BACK_SLASH,
					Constants.SLASH);
			String localValue = Value.trim();
			idMap.put(localId, localValue);
		}		
	}
	
	public boolean isVisited(String path){
		String localPath = path;
		int index = path.indexOf(Constants.SHARP);
		if(index != -1){
			localPath=localPath.substring(0,index);
		}
		return visitSet.contains(localPath.trim().replaceAll(Constants.DOUBLE_BACK_SLASH,
				Constants.SLASH));
	}
	
	public void visit(String path){
		String localPath = path;
		int index = path.indexOf(Constants.SHARP);
		if(index != -1){
			localPath=localPath.substring(0,index);
		}
		visitSet.add(localPath.trim().replaceAll(Constants.DOUBLE_BACK_SLASH,
				Constants.SLASH));
	}
	
	public String getFirstTopicId(String path, String dir){
		String localPath = path;
		String localDir = dir;
		if(path != null && dir != null){
			localPath = localPath.trim();
			localDir = localDir.trim();
		}else{
			return null;
		}
		TopicIdParser parser;
		XMLReader reader;
		StringBuffer firstTopicId = new StringBuffer();
		parser = new TopicIdParser(firstTopicId);
		try{
            if (System.getProperty(Constants.SAX_DRIVER_PROPERTY) == null){
                //The default sax driver is set to xerces's sax driver
                System.setProperty(Constants.SAX_DRIVER_PROPERTY,Constants.SAX_DRIVER_DEFAULT_CLASS);
            }
            reader = XMLReaderFactory.createXMLReader();
            reader.setContentHandler(parser);            
            reader.parse(localDir+File.separator+localPath);
        }catch (Exception e){
            logger.logException(e);
        }
		return firstTopicId.toString();
		
	}
}
