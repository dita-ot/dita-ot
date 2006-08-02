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
		visitSet = new HashSet();
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
		if (Id != null && idMap.containsKey(Id.trim().replaceAll(Constants.DOUBLE_BACK_SLASH,
				Constants.SLASH))){
			return true;
		}else {
			return false;
		}
	}
	
	public String addId (String Id){
		if(Id == null){
			return null;
		}
		Id=Id.trim().replaceAll(Constants.DOUBLE_BACK_SLASH,
				Constants.SLASH);
		index ++;
		idMap.put(Id,"unique_"+Integer.toString(index));
		return "unique_"+Integer.toString(index);
	}
	
	public String getIdValue (String Id){
		if (Id==null){
			return null;
		}
		Id = Id.trim().replaceAll(Constants.DOUBLE_BACK_SLASH,
				Constants.SLASH);
		return (String) idMap.get(Id);
	}
	
	public void addId (String Id, String Value){
		if(Id != null && Value != null){
			Id=Id.trim().replaceAll(Constants.DOUBLE_BACK_SLASH,
					Constants.SLASH);
			Value = Value.trim();
			idMap.put(Id, Value);
		}		
	}
	
	public boolean isVisited(String path){
		int index = path.indexOf(Constants.SHARP);
		if(index != -1){
			path=path.substring(0,index);
		}
		return visitSet.contains(path.trim().replaceAll(Constants.DOUBLE_BACK_SLASH,
				Constants.SLASH));
	}
	
	public void visit(String path){
		int index = path.indexOf(Constants.SHARP);
		if(index != -1){
			path=path.substring(0,index);
		}
		visitSet.add(path.trim().replaceAll(Constants.DOUBLE_BACK_SLASH,
				Constants.SLASH));
	}
	
	public String getFirstTopicId(String path, String dir){
		if(path != null && dir != null){
			path = path.trim();
			dir = dir.trim();
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
            reader.parse(dir+File.separator+path);
        }catch (Exception e){
            logger.logException(e);
        }
		return firstTopicId.toString();
		
	}
}
