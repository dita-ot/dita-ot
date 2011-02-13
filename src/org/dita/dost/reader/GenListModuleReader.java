/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.reader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;
import java.util.Map.Entry;

import org.apache.xerces.xni.grammars.XMLGrammarPool;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.exception.DITAOTXMLErrorHandler;
import org.dita.dost.log.DITAOTJavaLogger;
import org.dita.dost.log.MessageBean;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.util.CatalogUtils;
import org.dita.dost.util.Constants;
import org.dita.dost.util.DITAAttrUtils;
import org.dita.dost.util.FileUtils;
import org.dita.dost.util.FilterUtils;
import org.dita.dost.util.OutputUtils;
import org.dita.dost.util.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;


/**
 * This class extends AbstractReader, used to parse relevant dita topics 
 * and ditamap files for GenMapAndTopicListModule.
 * 
 * @version 1.0 2004-11-25
 * 
 * @author Wu, Zhi Qiang
 */
public class GenListModuleReader extends AbstractXMLReader {
	/** XMLReader instance for parsing dita file */
	private static XMLReader reader = null;

	/** Map of XML catalog info */
	private static HashMap<String, String> catalogMap = null;

	/** Basedir of the current parsing file */
	private String currentDir = null;

	/** Flag for conref in parsing file */
	private boolean hasConRef = false;

	/** Flag for href in parsing file */
	private boolean hasHref = false;
	
	/** Flag for keyref in parsing file */
	private boolean hasKeyRef = false;
	
	/** Flag for whether parsing file contains coderef */
	private boolean hasCodeRef = false;

	/** Set of all the non-conref and non-copyto targets refered in current parsing file */
	private Set<String> nonConrefCopytoTargets = null;
	
	/** Set of conref targets refered in current parsing file */
	private Set<String> conrefTargets = null;
	
	/** Set of href nonConrefCopytoTargets refered in current parsing file */
	private Set<String> hrefTargets = null;
	
	/** Set of href targets with anchor appended */
	private Set<String> hrefTopicSet = null;
	
	/** Set of chunk targets */
	private Set<String> chunkTopicSet = null;
	
	/** Set of subject schema files */
	private Set<String> schemeSet = null;
	
	/** Set of subsidiary files */
	private Set<String> subsidiarySet = null;

	/** Set of sources of those copy-to that were ignored */
	private Set<String> ignoredCopytoSourceSet = null;
	
	/** Map of copy-to target to souce	*/
	private Map<String, String> copytoMap = null;
	
	/** Map of key definitions */
	private Map<String, String> keysDefMap = null;
	
	//Added on 20100826 for bug:3052913 start
	//Map to store multi-level keyrefs
	private Map<String, String>keysRefMap = null;
	//Added on 20100826 for bug:3052913 end
	
	/** Flag for conrefpush   */
	private boolean hasconaction = false;  
	
	/** Flag used to mark if parsing entered into excluded element */
	private boolean insideExcludedElement = false;
	
	/** Used to record the excluded level */
	private int excludedLevel = 0;
	
	/** foreign/unknown nesting level */
    private int foreignLevel = 0;
    
    /** chunk nesting level */
    private int chunkLevel = 0;
    
    //Added by William on 2010-06-17 for bug:3016739 start
    /** mark topics in reltables */
    private int relTableLevel = 0;
    //Added by William on 2010-06-17 for bug:3016739 end
    
    /** chunk to-navigation level */
    private int chunkToNavLevel = 0;
	
    /** Topic group nesting level */
    private int topicGroupLevel = 0;
    
	/** Flag used to mark if current file is still valid after filtering */
	private boolean isValidInput = false;
	
	private String props; // contains the attribution specialization from props
	
	private DITAOTJavaLogger javaLogger = null;
	
	/** Set of outer dita files */
	private Set<String> outDitaFilesSet=null;
	
	private static String rootDir = null;
	
	private String currentFile=null;
	
	private static String rootFilePath=null;
	
	//Added on 2010-08-24 for bug:3086552 start
	private static boolean setSystemid = true;
	//Added on 2010-08-24 for bug:3086552 end
	
    private Stack<String> processRoleStack; // stack for @processing-role value
    private int processRoleLevel; // Depth inside a @processing-role parent
    private Set<String> resourceOnlySet; // Topics with role of "resource-only"
    private Set<String> crossSet;
    private Set<String> schemeRefSet = null;
    
    /** Subject scheme document root */
    //private Document schemeRoot = null;
    
    /** Current processing node */
    //private Element currentElement = null;
    
    /** Relationship graph between subject schema */
    private Map<String, Set<String>> relationGraph = null;
    
    //Added by William on 2009-06-25 for req #12014 start
    //StringBuffer to store <exportanchors> elements
    private StringBuffer result = new StringBuffer();
    //flag to show whether a file has <exportanchors> tag
	private boolean hasExport = false;
	//for topic/dita files whether a </file> tag should be added
	private boolean shouldAppendEndTag = false;
	//store the href of topicref tag
	private String topicHref = "";
	//topicmeta set for merge multiple exportanchors into one
	//each topicmeta/prolog can define many exportanchors
	private HashSet<String> topicMetaSet = null;
	//refered topic id
	private String topicId = "";
	//Map to store plugin id
	private Map<String, Set<String>> pluginMap = new HashMap<String, Set<String>>();
    //transtype
    private String transtype;
    //Added by William on 2010-03-01 for update onlytopicinmap option start
    //Map to store referenced branches.
    private Map<String, List<String>> vaildBranches;
    //int to mark referenced nested elements.
    private int level;
    //topicref stack
    private Stack<String> topicrefStack;
    //store the primary ditamap file name.
    private String primaryDitamap = "";
    //Added by William on 2010-03-01 for update onlytopicinmap option end.
    
    //Added by William on 2010-06-01 for bug:3005748 start
    //Get DITAAttrUtil
    private DITAAttrUtils ditaAttrUtils = DITAAttrUtils.getInstance();
    //Added by William on 2010-06-01 for bug:3005748 end
    
    //Added by William on 2010-06-09 for bug:3013079 start
    //store the external/peer keydefs
    private Map<String, String> exKeysDefMap = null;
    //Added by William on 2010-06-09 for bug:3013079 end
    
    /**
	 * Get transtype.
	 * @return the transtype
	 */
	public String getTranstype() {
		return transtype;
	}

	/**
	 * Set transtype.
	 * @param transtype the transtype to set
	 */
	public void setTranstype(String transtype) {
		this.transtype = transtype;
	}
	
	/**
	 * @return the pluginMap
	 */
	public Map<String, Set<String>> getPluginMap() {
		return pluginMap;
	}

	/**
	 * @return the result
	 */
	public StringBuffer getResult() {
		return result;
	}
	//Added by William on 2009-06-25 for req #12014 end

	/**
	 * Constructor.
	 */
	public GenListModuleReader() {
		nonConrefCopytoTargets = new HashSet<String>(Constants.INT_64);
		hrefTargets = new HashSet<String>(Constants.INT_32);
		hrefTopicSet = new HashSet<String>(Constants.INT_32);
		chunkTopicSet = new HashSet<String>(Constants.INT_32);
		schemeSet = new HashSet<String>(Constants.INT_32);
		schemeRefSet = new HashSet<String>(Constants.INT_32);
		conrefTargets = new HashSet<String>(Constants.INT_32);
		copytoMap = new HashMap<String, String>(Constants.INT_16);
		subsidiarySet = new HashSet<String>(Constants.INT_16);
		ignoredCopytoSourceSet = new HashSet<String>(Constants.INT_16);
		outDitaFilesSet=new HashSet<String>(Constants.INT_64);
		keysDefMap = new HashMap<String, String>();
		keysRefMap = new HashMap<String, String>();
		
		exKeysDefMap = new HashMap<String, String>();
		
		processRoleLevel = 0;
		processRoleStack = new Stack<String>();
		resourceOnlySet = new HashSet<String>(Constants.INT_32);
		crossSet = new HashSet<String>(Constants.INT_32);
		
		//store the topicmeta element
		topicMetaSet = new HashSet<String>(Constants.INT_16);
		
		vaildBranches = new HashMap<String, List<String>>(Constants.INT_32);
		level = 0;
		topicrefStack = new Stack<String>();
		
		//schemeRoot = null;
		//currentElement = null;
		
		props = null;
		reader.setContentHandler(this);
		javaLogger = new DITAOTJavaLogger();
		try {
			reader.setProperty(Constants.LEXICAL_HANDLER_PROPERTY,this);
		} catch (SAXNotRecognizedException e1) {
			javaLogger.logException(e1);
		} catch (SAXNotSupportedException e1) {
			javaLogger.logException(e1);
		}
		
		try {
			Class.forName(Constants.RESOLVER_CLASS);
			reader.setEntityResolver(CatalogUtils.getCatalogResolver());
		}catch (ClassNotFoundException e){
			reader.setEntityResolver(this);
		}
		
	}

	/**
     * Init xml reader used for pipeline parsing.
	 *
     * @param ditaDir ditaDir
     * @param validate whether validate input file
     * @param rootFile input file
	 * @throws SAXException parsing exception
     */
	public static void initXMLReader(String ditaDir,boolean validate,String rootFile, boolean arg_setSystemid) throws SAXException {
		DITAOTJavaLogger javaLogger=new DITAOTJavaLogger();
		
		//to check whether the current parsing file's href value is out of inputmap.dir
		rootDir=new File(rootFile).getAbsoluteFile().getParent();
		rootDir = FileUtils.removeRedundantNames(rootDir);
		rootFilePath=new File(rootFile).getAbsolutePath();
		rootFilePath = FileUtils.removeRedundantNames(rootFilePath);
		reader = StringUtils.getXMLReader();
		reader.setFeature(Constants.FEATURE_NAMESPACE_PREFIX, true);
		if(validate==true){
			reader.setFeature(Constants.FEATURE_VALIDATION, true);
			reader.setFeature(Constants.FEATURE_VALIDATION_SCHEMA, true);
		}else{
			String msg=MessageUtils.getMessage("DOTJ037W").toString();
			javaLogger.logWarn(msg);
		}
		XMLGrammarPool grammarPool = GrammarPoolManager.getGrammarPool();
		setGrammarPool(reader, grammarPool);
		
		CatalogUtils.setDitaDir(ditaDir);
		catalogMap = CatalogUtils.getCatalog(ditaDir);
		//Added on 2010-08-24 for bug:3086552 start
		setSystemid= arg_setSystemid;
		//Added on 2010-08-24 for bug:3086552 end
	}

	/**
	 * 
	 * Reset the internal variables.
	 */
    public void reset() {
    	hasKeyRef = false;
		hasConRef = false;
		hasHref = false;
		hasCodeRef = false;
		currentDir = null;
		insideExcludedElement = false;
		excludedLevel = 0;
		foreignLevel = 0;
		chunkLevel = 0;
		relTableLevel = 0;
		chunkToNavLevel = 0;
		topicGroupLevel = 0;
		isValidInput = false;
		hasconaction = false;
		nonConrefCopytoTargets.clear();
		hrefTargets.clear();
		hrefTopicSet.clear();
		chunkTopicSet.clear();
		conrefTargets.clear();
		copytoMap.clear();
		ignoredCopytoSourceSet.clear();
		outDitaFilesSet.clear();
		keysDefMap.clear();
		keysRefMap.clear();
		exKeysDefMap.clear();
		schemeSet.clear();
		schemeRefSet.clear();
		
		//clear level
		level = 0;
		//clear stack
		topicrefStack.clear();
		
		
		//@processing-role
		processRoleLevel = 0;
		processRoleStack.clear();
		
		//reset utils
		ditaAttrUtils.reset();
		/* 
		 * Don't clean up these sets, we need them through
		 * the whole phase to determine a topic's processing-role.
		 */
		//resourceOnlySet.clear();
		//crossSet.clear();
	}

	/**
	 * To see if the parsed file has conref inside.
	 * 
	 * @return true if has conref and false otherwise
	 */
	public boolean hasConRef() {
		return hasConRef;
	}
	
	/**
	 * To see if the parsed file has keyref inside.
	 * 
	 * @return true if has keyref and false otherwise
	 */
	public boolean hasKeyRef(){
		return hasKeyRef;
	}
	
	/**
	 * To see if the parsed file has coderef inside.
	 * 
	 * @return true if has coderef and false otherwise
	 */
	public boolean hasCodeRef(){
		return hasCodeRef;
	}

	/**
	 * To see if the parsed file has href inside.
	 * 
	 * @return true if has href and false otherwise
	 */
	public boolean hasHref() {
		return hasHref;
	}

	/**
	 * Get all targets except copy-to.
	 * 
	 * @return Returns allTargets.
	 */
	public Set<String> getNonCopytoResult() {
		Set<String> nonCopytoSet = new HashSet<String>(Constants.INT_128);
		
		nonCopytoSet.addAll(nonConrefCopytoTargets);
		nonCopytoSet.addAll(conrefTargets);
		nonCopytoSet.addAll(copytoMap.values());
		nonCopytoSet.addAll(ignoredCopytoSourceSet);
		//Added by William on 2010-03-04 for bug:2957938 start
		addCoderefFiles(nonCopytoSet);
		//Added by William on 2010-03-04 for bug:2957938 end
		return nonCopytoSet;
	}

	/**
	 * Add coderef outside coderef files. 
	 * @param nonCopytoSet
	 */
	private void addCoderefFiles(Set<String> nonCopytoSet) {
		
		for(String filename : subsidiarySet){
			//only activated on /generateout:3 & is out file.
			if(isOutFile(filename) && OutputUtils.getGeneratecopyouter() 
			  == OutputUtils.OLDSOLUTION){
				nonCopytoSet.add(filename);
			}
		}
		//nonCopytoSet.addAll(subsidiarySet);
		
	}

	/**
	 * Get the href target.
	 * 
	 * @return Returns the hrefTargets.
	 */
	public Set<String> getHrefTargets() {
		return hrefTargets;
	}
	
	/**
	 * Get conref targets.
	 * 
	 * @return Returns the conrefTargets.
	 */
	public Set<String> getConrefTargets() {
		return conrefTargets;
	}
	
	/**
	 * Get subsidiary targets.
	 * 
	 * @return Returns the subsidiarySet.
	 */
	public Set<String> getSubsidiaryTargets() {
		return subsidiarySet;
	}
	
	/**
	 * Get outditafileslist.
	 * 
	 * @return Returns the outditafileslist.
	 */
	public Set<String> getOutDitaFilesSet(){
		return outDitaFilesSet;
	}
	
	/**
	 * Get non-conref and non-copyto targets.
	 * 
	 * @return Returns the nonConrefCopytoTargets.
	 */
	public Set<String> getNonConrefCopytoTargets() {
		return nonConrefCopytoTargets;
	}
	
	/**
     * Returns the ignoredCopytoSourceSet.
     *
     * @return Returns the ignoredCopytoSourceSet.
     */
	public Set<String> getIgnoredCopytoSourceSet() {
		return ignoredCopytoSourceSet;
	}

	/**
	 * Get the copy-to map.
	 * 
	 * @return copy-to map
	 */
	public Map<String, String> getCopytoMap() {
		return copytoMap;
	}
	
	/**
	 * Get the Key definitions.
	 * 
	 * @return Key definitions map
	 */
	public Map<String,String> getKeysDMap(){
		return keysDefMap;
	}
	
	//Added by William on 2010-06-09 for bug:3013079 start
	public Map<String, String> getExKeysDefMap() {
		return exKeysDefMap;
	}
	//Added by William on 2010-06-09 for bug:3013079 end
	
	/**
	 * Set the relative directory of current file.
	 * 
	 * @param dir dir
	 */
	public void setCurrentDir(String dir) {
		this.currentDir = dir;
	}

	/**
	 * Check if the current file is valid after filtering.
	 * 
	 * @return true if valid and false otherwise
	 */
	public boolean isValidInput() {
		return isValidInput;
	}
	
	/**
	 * Check if the current file has conaction.
	 * @return true if has conaction and false otherwise
	 */
	public boolean hasConaction(){
		return hasconaction;
	}
	/**
	 * Parse input xml file.
	 * 
	 * @param file file
	 * @throws SAXException SAXException
	 * @throws IOException IOException
	 * @throws FileNotFoundException FileNotFoundException
	 */
	public void parse(File file) throws FileNotFoundException, IOException, SAXException {
		
		currentFile=file.getAbsolutePath();
		
		reader.setErrorHandler(new DITAOTXMLErrorHandler(file.getName()));
		//Added on 2010-08-24 for bug:3086552 start
		InputSource is = new InputSource(new FileInputStream(file));
		//Set the system ID
		if(setSystemid)
			//is.setSystemId(URLUtil.correct(file).toString());
		    is.setSystemId(file.toURI().toURL().toString());
		//Added on 2010-08-24 for bug:3086552 end
		reader.parse(is); 
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes atts) throws SAXException {
		String domains = null;
		Properties params = new Properties();
		
		//Added by William on 2010-06-01 for bug:3005748 start
		String printValue = atts.getValue(Constants.ATTRIBUTE_NAME_PRINT);
		//increase element level for nested tags.
		ditaAttrUtils.increasePrintLevel(printValue);
		//Exclude the topic if it is needed.
		if(ditaAttrUtils.needExcludeForPrintAttri(transtype)){
			return;
		}
		//Added by William on 2010-06-01 for bug:3005748 end
		
		String attrValue = atts.getValue(Constants.ATTRIBUTE_NAME_PROCESSING_ROLE);
		String href = atts.getValue(Constants.ATTRIBUTE_NAME_HREF);
	    if (attrValue != null) {
	        processRoleStack.push(attrValue);
	        processRoleLevel++;
	        if (Constants.ATTR_PROCESSING_ROLE_VALUE_RESOURCE_ONLY.equals(attrValue)) {
	            if (href != null) {
	                resourceOnlySet.add(FileUtils.resolveFile(currentDir, href));
	            }
	        } else if (Constants.ATTR_PROCESSING_ROLE_VALUE_NORMAL.equalsIgnoreCase(attrValue)) {
	            if (href != null) {
	                crossSet.add(FileUtils.resolveFile(currentDir, href));
	            }
	        }
	    } else if (processRoleLevel > 0) {
	        processRoleLevel++;
	        if (Constants.ATTR_PROCESSING_ROLE_VALUE_RESOURCE_ONLY.equalsIgnoreCase(
	                processRoleStack.peek())) {
	            if (href != null) {
	                resourceOnlySet.add(FileUtils.resolveFile(currentDir, href));
	            }
	        } else if (Constants.ATTR_PROCESSING_ROLE_VALUE_NORMAL.equalsIgnoreCase(
	        		processRoleStack.peek())) {
	            if (href != null) {
	                crossSet.add(FileUtils.resolveFile(currentDir, href));
	            }
	        }
	    } else {
            if (href != null) {
                crossSet.add(FileUtils.resolveFile(currentDir, href));
            }
	    }
		
		attrValue = atts.getValue(Constants.ATTRIBUTE_NAME_CLASS);
		
		//Added by William on 2009-06-24 for req #12014 start
		//has class attribute
		if(attrValue!=null){
			
			//when meets topic tag
			if(attrValue.contains(Constants.ATTR_CLASS_VALUE_TOPIC)){
				topicId = atts.getValue(Constants.ATTRIBUTE_NAME_ID);
				//relpace place holder with first topic id
				//Get relative file name
				String filename = FileUtils.getRelativePathFromMap(
						rootFilePath, currentFile);
				if(result.indexOf(filename + Constants.QUESTION) != -1){
					result = new StringBuffer(result.toString().replace(filename + Constants.QUESTION, topicId));
				}
				
			}
			// WEK: As of 14 Dec 2009, transtype is sometimes null, not sure under what conditions.
//			System.out.println(" + [DEBUG] transtype=" + transtype);
			//get plugin id only transtype = eclipsehelp
			if(FileUtils.isDITAMapFile(currentFile)&&
				rootFilePath.equals(currentFile)&&
				attrValue.contains(Constants.ATTR_CLASS_VALUE_MAP)&&
				Constants.INDEX_TYPE_ECLIPSEHELP.equals(transtype)){
				String pluginId = atts.getValue(Constants.ATTRIBUTE_NAME_ID);
				if(pluginId == null){
					pluginId = "org.sample.help.doc";
				}
				Set<String> set = StringUtils.restoreSet(pluginId);
				pluginMap.put("pluginId", set);
			}
			
			//merge multiple exportanchors into one
			//Each <topicref> can only have one <topicmeta>.
			//Each <topic> can only have one <prolog>
			//and <metadata> can have more than one exportanchors
			if (Constants.INDEX_TYPE_ECLIPSEHELP.equals(transtype)) {
				if (attrValue.contains(Constants.ATTR_CLASS_VALUE_TOPICMETA)
						|| attrValue.contains(Constants.ATTR_CLASS_VALUE_PROLOG)) {
					topicMetaSet.add(qName);
				}
				// If the file has <exportanchors> tags only transtype =
				// eclipsehelp
				if (attrValue.contains(Constants.ATTR_CLASS_VALUE_EXPORTANCHORS)) {
					hasExport = true;
					// If current file is a ditamap file
					if (FileUtils.isDITAMapFile(currentFile)) {
						// if dita file's extension name is ".xml"
						String editedHref = "";
						if (topicHref.endsWith(Constants.FILE_EXTENSION_XML)) {
							// change the extension to ".dita" for latter
							// compare
							editedHref = topicHref.replace(
									Constants.FILE_EXTENSION_XML,
									Constants.FILE_EXTENSION_DITA);
						} else {
							editedHref = topicHref;
						}
						// editedHref = editedHref.replace(File.separator, "/");
						// create file element in the StringBuffer
						result.append("<file name=\"" + editedHref + "\">");
						// if <exportanchors> is defined in topicmeta(topicref),
						// there is only one topic id
						result.append("<topicid name=\"" + topicId + "\"/>");

						// If current file is topic file
					} else if (FileUtils.isDITATopicFile(currentFile)) {
						String filename = FileUtils.getRelativePathFromMap(
								rootFilePath, currentFile);
						// if dita file's extension name is ".xml"
						if (filename.endsWith(Constants.FILE_EXTENSION_XML)) {
							// change the extension to ".dita" for latter
							// compare
							filename = filename.replace(
									Constants.FILE_EXTENSION_XML,
									Constants.FILE_EXTENSION_DITA);
						}
						// filename = FileUtils.normalizeDirectory(currentDir,
						// filename);
						filename = filename.replace(Constants.BACK_SLASH,
								Constants.SLASH);
						// create file element in the StringBuffer
						result.append("<file name=\"" + filename + "\">");
						// if <exportanchors> is defined in metadata(topic),
						// there can be many topic ids
						result.append("<topicid name=\"" + topicId + "\">");

						shouldAppendEndTag = true;
					}
					// meet <anchorkey> tag
				} else if (attrValue
						.contains(Constants.ATTR_CLASS_VALUE_ANCHORKEY)) {
					// create keyref element in the StringBuffer
					// TODO in topic file is no keys
					String keyref = atts
							.getValue(Constants.ATTRIBUTE_NAME_KEYREF);
					result.append("<keyref name=\"" + keyref + "\"/>");
					// meet <anchorid> tag
				} else if (attrValue.contains(Constants.ATTR_CLASS_VALUE_ANCHORID)) {
					// create keyref element in the StringBuffer
					String id = atts.getValue(Constants.ATTRIBUTE_NAME_ID);
					// If current file is a ditamap file
					// The id can only be element id within a topic
					if (FileUtils.isDITAMapFile(currentFile)) {
						// only for dita format
						/*
						 * if(!"".equals(topicHref)){ String absolutePathToFile
						 * = FileUtils.resolveFile((new
						 * File(rootFilePath)).getParent(),topicHref); //whether
						 * the id is a topic id
						 * if(FileUtils.isDITAFile(absolutePathToFile)){ found =
						 * DelayConrefUtils
						 * .getInstance().findTopicId(absolutePathToFile, id); }
						 * //other format file }else{ found = false; }
						 */
						// id shouldn't be same as topic id in the case of duplicate insert
						if (!topicId.equals(id)) {
							result.append("<id name=\"" + id + "\"/>");
						}
					} else if (FileUtils.isDITATopicFile(currentFile)) {
						// id shouldn't be same as topic id in the case of duplicate insert
						if (!topicId.equals(id)) {
							// topic id found
							result.append("<id name=\"" + id + "\"/>");
						}
					}
				}
			}
		}
		//Added by William on 2009-06-24 for req #12014 end
		
		// Generate Scheme relationship graph
		if (attrValue != null) {
			if (attrValue.contains(Constants.ATTR_CLASS_VALUE_SUBJECT_SCHEME)) {
				if (this.relationGraph == null)
					this.relationGraph = new LinkedHashMap<String, Set<String>>();
				//Make it easy to do the BFS later.
				Set<String> children = this.relationGraph.get("ROOT");
				if (children == null || children.isEmpty()) {
					children = new LinkedHashSet<String>();
				}
				children.add(this.currentFile);
				this.relationGraph.put("ROOT", children);
				schemeRefSet.add(FileUtils.getRelativePathFromMap(rootFilePath, currentFile));
			} else if (attrValue.contains(Constants.ATTR_CLASS_VALUE_SCHEME_REF)) {
				Set<String> children = this.relationGraph.get(this.currentFile);
				if (children == null) {
					children = new LinkedHashSet<String>();
					this.relationGraph.put(currentFile, children);
				}
				if (href != null)
					children.add(FileUtils.resolveFile(rootDir, href));
			}
		}
		
		if(foreignLevel > 0){
			//if it is an element nested in foreign/unknown element
			//do not parse it
			foreignLevel ++;
			return;
		} else if(attrValue != null && 
        		(attrValue.indexOf(Constants.ATTR_CLASS_VALUE_FOREIGN) != -1 || 
        				attrValue.indexOf(Constants.ATTR_CLASS_VALUE_UNKNOWN) != -1)){
        	foreignLevel ++;
        }
		
		if(chunkLevel > 0) {
			chunkLevel++;
		} else if(atts.getValue(Constants.ATTRIBUTE_NAME_CHUNK) != null) {
			chunkLevel++;
		}
		//Added by William on 2010-6-17 for bug:3016739 start
		if(relTableLevel > 0) {
			relTableLevel ++;
		} else if(attrValue != null && 
				attrValue.indexOf(Constants.ATTR_CLASS_VALUE_RELTABLE) != -1){
			relTableLevel++;
		}
		//Added by William on 2010-6-17 for bug:3016739 end
		
		
		if(chunkToNavLevel > 0) {
			chunkToNavLevel++;
		} else if(atts.getValue(Constants.ATTRIBUTE_NAME_CHUNK) != null
				&& atts.getValue(Constants.ATTRIBUTE_NAME_CHUNK).indexOf("to-navigation") != -1){
			chunkToNavLevel++;
		}
		
		if(topicGroupLevel > 0) {
			topicGroupLevel++;
		} else if (atts.getValue(Constants.ATTRIBUTE_NAME_CLASS) != null
				&& atts.getValue(Constants.ATTRIBUTE_NAME_CLASS).contains(Constants.ATTR_CLASS_VALUE_TOPIC_GROUP)) {
			topicGroupLevel++;
		}
		
		if(attrValue==null && !Constants.ELEMENT_NAME_DITA.equals(localName)){
    		params.clear();
			params.put("%1", localName);
    		javaLogger.logInfo(MessageUtils.getMessage("DOTJ030I", params).toString());			
		}		
		
        if (attrValue != null && attrValue.indexOf(Constants.ATTR_CLASS_VALUE_TOPIC) != -1){
        	domains = atts.getValue(Constants.ATTRIBUTE_NAME_DOMAINS);
        	if(domains==null){
        		params.clear();
				params.put("%1", localName);
        		javaLogger.logInfo(MessageUtils.getMessage("DOTJ029I", params).toString());
        	}else
        		props = StringUtils.getExtProps(domains);
        }        
		
		if (insideExcludedElement) {
			++excludedLevel;
			return;
		}
		
		// Ignore element that has been filtered out.
		if (FilterUtils.needExclude(atts, props)) {
			insideExcludedElement = true;
			++excludedLevel;
			return;
		}
				
		/* 
		 * For ditamap, set it to valid if element <map> or extended from 
		 * <map> was found, this kind of element's class attribute must 
		 * contains 'map/map';
		 * For topic files, set it to valid if element <title> or extended 
		 * from <title> was found, this kind of element's class attribute 
		 * must contains 'topic/title'.
		 */
		
		if (attrValue != null) {
			if ((attrValue.indexOf(Constants.ATTR_CLASS_VALUE_MAP) != -1)
					|| (attrValue.indexOf(Constants.ATTR_CLASS_VALUE_TITLE) != -1)) {
				isValidInput = true;
			}else if (attrValue.indexOf(Constants.ATTR_CLASS_VALUE_OBJECT) != -1){
				parseAttribute(atts, Constants.ATTRIBUTE_NAME_DATA);
			}
		}
		
		
		//Added by William on 2010-03-02 for /onlytopicinmap update start.
		//onlyTopicInMap is on.
		if(OutputUtils.getOnlyTopicInMap() && this.canResolved()){
			//Get class attribute value
			String classValue = atts.getValue(Constants.ATTRIBUTE_NAME_CLASS);
			
				//topicref(only defined in ditamap file.)
				if(classValue.contains(Constants.ATTR_CLASS_VALUE_TOPICREF)){
					
					//get href attribute value.
					String hrefValue = atts.getValue(Constants.ATTRIBUTE_NAME_HREF);
					
					//get conref attribute value.
					String conrefValue = atts.getValue(Constants.ATTRIBUTE_NAME_CONREF);
					
					//has href attribute and refer to ditamap file.
					if(!StringUtils.isEmptyString(hrefValue)){
						//exclude external resources
						String attrScope = atts.getValue(Constants.ATTRIBUTE_NAME_SCOPE);
						if ("external".equalsIgnoreCase(attrScope)
								|| "peer".equalsIgnoreCase(attrScope)
								|| hrefValue.indexOf(Constants.COLON_DOUBLE_SLASH) != -1
								|| hrefValue.startsWith(Constants.SHARP)) {
							return;
						}
						//normalize href value.
						File target=new File(hrefValue);
						//caculate relative path for href value.
						String fileName = null;
						if(target.isAbsolute()){
							fileName = FileUtils.getRelativePathFromMap(rootFilePath,hrefValue);
						}
						fileName = FileUtils.normalizeDirectory(currentDir, hrefValue);
						//change '\' to '/' for comparsion.
						fileName = fileName.replaceAll(Constants.DOUBLE_BACK_SLASH,
								Constants.SLASH);
						
						boolean canParse = parseBranch(atts, hrefValue, fileName);
						if(!canParse){
							return;
						}else{
							topicrefStack.push(localName);
						}
						
					}else if(!StringUtils.isEmptyString(conrefValue)){
						
						//exclude external resources
						String attrScope = atts.getValue(Constants.ATTRIBUTE_NAME_SCOPE);
						if ("external".equalsIgnoreCase(attrScope)
								|| "peer".equalsIgnoreCase(attrScope)
								|| conrefValue.indexOf(Constants.COLON_DOUBLE_SLASH) != -1
								|| conrefValue.startsWith(Constants.SHARP)) {
							return;
						}
						//normalize href value.
						File target=new File(conrefValue);
						//caculate relative path for href value.
						String fileName = null;
						if(target.isAbsolute()){
							fileName = FileUtils.getRelativePathFromMap(rootFilePath,conrefValue);
						}
						fileName = FileUtils.normalizeDirectory(currentDir, conrefValue);
						
						//change '\' to '/' for comparsion.
						fileName = fileName.replaceAll(Constants.DOUBLE_BACK_SLASH,
								Constants.SLASH);
				
						boolean canParse = parseBranch(atts, conrefValue, fileName);
						if(!canParse){
							return;
						}else{
							topicrefStack.push(localName);
						}
					}
				}
		}
		//Added by William on 2010-03-02 for /onlytopicinmap update end.
		
		parseAttribute(atts, Constants.ATTRIBUTE_NAME_CONREF);
		parseAttribute(atts, Constants.ATTRIBUTE_NAME_HREF);
		parseAttribute(atts, Constants.ATTRIBUTE_NAME_COPY_TO);
		parseAttribute(atts, Constants.ATTRIBUTE_NAME_IMG);
		parseAttribute(atts, Constants.ATTRIBUTE_NAME_CONACTION);
		parseAttribute(atts, Constants.ATTRIBUTE_NAME_KEYS);
		parseAttribute(atts, Constants.ATTRIBUTE_NAME_CONKEYREF);
		parseAttribute(atts, Constants.ATTRIBUTE_NAME_KEYREF);

	}

	/**
	 * Method for see whether a branch should be parsed.
	 * @param atts {@link Attributes}
	 * @param hrefValue {@link String}
	 * @param fileName normalized file name(remove '#')
	 * @return boolean
	 */
	private boolean parseBranch(Attributes atts, String hrefValue, String fileName) {
		//current file is primary ditamap file.
		//parse every branch.
		String currentFileRelative = FileUtils.getRelativePathFromMap(
				rootFilePath, currentFile);
		if(currentDir == null && currentFileRelative.equals(primaryDitamap)){
			//add branches into map
			addReferredBranches(hrefValue, fileName);
			return true;
		}else{
			//current file is a sub-ditamap one.
			//get branch's id
			String id = atts.getValue(Constants.ATTRIBUTE_NAME_ID);
			//this branch is not referenced
			if(level == 0 && StringUtils.isEmptyString(id)){
				//There is occassion that the whole ditamap should be parsed
				boolean found = searchBrachesMap(id);
				if(found){
					//Add this branch into map for parsing.
					addReferredBranches(hrefValue, fileName);
					//update level
					level++;
					return true;
				}else{
					return false;
				}
			//this brach is a decendent of a referenced one
			}else if(level != 0){
				//Add this branch into map for parsing.
				addReferredBranches(hrefValue, fileName);
				//update level
				level++;
				return true;
			//This branch has an id but is a new one	
			}else if(!StringUtils.isEmptyString(id)){
				//search branches map.
				boolean found = searchBrachesMap(id);
				//branch is referenced
				if(found){
					//Add this branch into map for parsing.
					addReferredBranches(hrefValue, fileName);
					//update level
					level ++;
					return true;
				}else{
					//this branch is not referenced
					return false;
				}
			}else{
				return false;
			}
		}
	}

	/**
	 * Search braches map with branch id and current file name.
	 * @param id String branch id.
	 * @return boolean true if found and false otherwise.
	 */
	private boolean searchBrachesMap(String id) {
		//caculate relative path for current file.
		String currentFileRelative = FileUtils.getRelativePathFromMap(
				rootFilePath, currentFile);
		//seach the map with id & current file name.
		if(vaildBranches.containsKey(currentFileRelative)){
			List<String> branchIdList = vaildBranches.get(currentFileRelative);
			//the branch is referenced.
			if(branchIdList.contains(id)){
				
				return true;
			}else if(branchIdList.size() == 0){
				//the whole map is referenced
				
				return true;
			}else{
				//the branch is not referred
				return false;
			}
		}else{
			//current file is not refered
			return false;
		}
	}

	/**
	 * Add branches into map.
	 * @param hrefValue
	 * @param fileName
	 */
	private void addReferredBranches(String hrefValue, String fileName) {
		String branchId = null;
		
		//href value has branch id.
		if(hrefValue.contains(Constants.SHARP)){
			branchId = hrefValue.substring(hrefValue.lastIndexOf(Constants.SHARP) + 1);
			//The map contains the file name
			if(vaildBranches.containsKey(fileName)){
				List<String> branchIdList = vaildBranches.get(fileName);
				branchIdList.add(branchId);
			}else{
				List<String> branchIdList = new ArrayList<String>();
				branchIdList.add(branchId);
				vaildBranches.put(fileName, branchIdList);
			}
		//href value has no branch id
		}else{
			vaildBranches.put(fileName, new ArrayList<String>());
		}
	}

	/**
	 * Clean up.
	 * @see org.dita.dost.reader.AbstractXMLReader#endDocument()
	 */
	@Override
	public void endDocument() throws SAXException {
		if (processRoleLevel > 0) {
			processRoleLevel--;
			processRoleStack.pop();
		}
		//Added by William on 2009-07-15 for req #12014 start
		if(FileUtils.isDITATopicFile(currentFile) && shouldAppendEndTag){
			result.append("</file>");
			//should reset
			shouldAppendEndTag = false;
		}
		//Added by William on 2009-07-15 for req #12014 end
		//update keysDefMap for multi-level keys for bug:3052913
		checkMultiLevelKeys(keysDefMap, keysRefMap);
	}

	/**
	 * Check if the current file is a ditamap with "@processing-role=resource-only".
	 * @see org.dita.dost.reader.AbstractXMLReader#startDocument()
	 */
	@Override
	public void startDocument() throws SAXException {
		String href = FileUtils.getRelativePathFromMap(rootFilePath, currentFile);
		if (FileUtils.isDITAMapFile(currentFile)
				&& resourceOnlySet.contains(href)
				&& !crossSet.contains(href)) {
			processRoleLevel++;
			processRoleStack.push(Constants.ATTR_PROCESSING_ROLE_VALUE_RESOURCE_ONLY);
		}
	}

	/**
	 * @see org.dita.dost.reader.AbstractXMLReader#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void endElement(String uri, String localName, String qName) throws SAXException {		
		
		//subject scheme
		//if (currentElement != null && currentElement != schemeRoot.getDocumentElement()) {
		//	currentElement = (Element)currentElement.getParentNode();
		//}
		
		//@processing-role
		if (processRoleLevel > 0) {
	        if (processRoleLevel == processRoleStack.size()) {
	            processRoleStack.pop();
	        }
	        processRoleLevel--;
	    }
		
		if (foreignLevel > 0){
			foreignLevel --;
			return;
		}
		
		if (chunkLevel > 0) {
			chunkLevel--;
		}
		
		//Added by William on 2010-06-17 for bug:3016739 start
		if (relTableLevel > 0) {
			relTableLevel--;
		}
		//Added by William on 2010-06-17 for bug:3016739 end
		
		if (chunkToNavLevel > 0) {
			chunkToNavLevel--;
		}
		
		if (topicGroupLevel > 0) {
			topicGroupLevel--;
		}
		
		if (insideExcludedElement) {
			// end of the excluded element, mark the flag as false 
			if (excludedLevel == 1) {
				insideExcludedElement = false;
			}
			--excludedLevel;
		}
		//Added by William on 2009-06-24 for req #12014 start
		//<exportanchors> over should write </file> tag
		
		if(topicMetaSet.contains(qName) && hasExport){
			//If current file is a ditamap file
			if(FileUtils.isDITAMapFile(currentFile)){
				result.append("</file>");
			//If current file is topic file
			}else if(FileUtils.isDITATopicFile(currentFile)){
				result.append("</topicid>");
			}
			hasExport = false;
			topicMetaSet.clear();
		}
		//Added by William on 2009-06-24 for req #12014 start
		
		//Added by William on 2010-03-02 for /onlytopicinmap update start
		if(!topicrefStack.isEmpty() && localName.equals(topicrefStack.peek())){
			level--;
			topicrefStack.pop();
		}
		//Added by William on 2010-03-02 for /onlytopicinmap update end
		
		//Added by William on 2010-06-01 for bug:3005748 start
		//decrease element level.
		ditaAttrUtils.decreasePrintLevel();
		//Added by William on 2010-06-01 for bug:3005748 end
		
	}

	/**
	 * Resolve the publicId used in XMLCatalog.
	 * @see org.dita.dost.reader.AbstractXMLReader#resolveEntity(String, String)
	 * @param publicId publicId in doctype declarations
	 * @param systemId systemId in doctype declarations
	 * @throws java.io.IOException if dita-catalog.xml is not available 
	 * @exception org.xml.sax.SAXException if dita-catalog.xml is not in valid format.
	 */
	public InputSource resolveEntity(String publicId, String systemId)
			throws SAXException, IOException {
		if (catalogMap.get(publicId) != null) {
			File dtdFile = new File((String) catalogMap.get(publicId));
			return new InputSource(dtdFile.getAbsolutePath());
		}else if (catalogMap.get(systemId) != null){
			File schemaFile = new File((String) catalogMap.get(systemId));
			return new InputSource(schemaFile.getAbsolutePath());
		}

		return null;
	}

	/*
	 * Parse the input attributes for needed information.
	 */
	private void parseAttribute(Attributes atts, String attrName) throws SAXException {
		String attrValue = atts.getValue(attrName);
		String filename = null;
		String attrClass = atts.getValue(Constants.ATTRIBUTE_NAME_CLASS);
		String attrScope = atts.getValue(Constants.ATTRIBUTE_NAME_SCOPE);
		String attrFormat = atts.getValue(Constants.ATTRIBUTE_NAME_FORMAT);
		String attrType = atts.getValue(Constants.ATTRIBUTE_NAME_TYPE);
		
		//Added on 20100830 for bug:3052156 start
		String codebase = atts.getValue(Constants.ATTRIBUTE_NAME_CODEBASE);
		//Added on 20100830 for bug:3052156 end
		
		if (attrValue == null) {
			return;
		}
		
		// @conkeyref will be resolved to @conref in Debug&Fileter step
		if (Constants.ATTRIBUTE_NAME_CONREF.equals(attrName) || Constants.ATTRIBUTE_NAME_CONKEYREF.equals(attrName)) {
			hasConRef = true;
		} else if (Constants.ATTRIBUTE_NAME_HREF.equals(attrName)) {
			if(attrClass != null && 
					attrClass.contains(Constants.ATTR_CLASS_VALUE_CODEREF) ){
				//if current element is <coderef> or its specialization
				//set hasCodeRef to true
				hasCodeRef = true;
			}else{
				hasHref = true;
			}			
		} else if(Constants.ATTRIBUTE_NAME_KEYREF.equals(attrName)){
			hasKeyRef = true;
		}
				
		// collect the key definitions
		if(Constants.ATTRIBUTE_NAME_KEYS.equals(attrName) && !attrValue.equals(Constants.STRING_EMPTY)){
			
			String target = atts.getValue(Constants.ATTRIBUTE_NAME_HREF);
			
			String keyRef = atts.getValue(Constants.ATTRIBUTE_NAME_KEYREF);
			
			//Added by William on 2009-10-15 for ampersand bug:2878492 start
			if(target != null){
				target = StringUtils.escapeXML(target);
			}
			//Added by William on 2009-10-15 for ampersand bug:2878492 end
			
			//Added by Alan for bug ID: 2870935 on Date: 2009-10-10 begin
			String copy_to = atts.getValue(Constants.ATTRIBUTE_NAME_COPY_TO);
			if (!StringUtils.isEmptyString(copy_to)) {
				target = copy_to;
			}
			//Added by Alan for bug ID: 2870935 on Date: 2009-10-10 end
			//Added on 20100825 for bug:3052904 start
			//avoid NullPointException
			if(target == null){
				target = "";
			}
			//Added on 20100825 for bug:3052904 end
			//store the target
			String temp = target;
			
			// Many keys can be defined in a single definition, like keys="a b c", a, b and c are seperated by blank.
			for(String key: attrValue.split(" ")){
				if(!keysDefMap.containsKey(key) && !key.equals("")){
					if(target != null && !target.equals(Constants.STRING_EMPTY)){
						if(attrScope!=null && (attrScope.equals("external") || attrScope.equals("peer"))){
							//Added by William on 2010-06-09 for bug:3013079 start
							//store external or peer resources.
							exKeysDefMap.put(key, target);
							//Added by William on 2010-06-09 for bug:3013079 end
							keysDefMap.put(key, target);
						}else{
							String tail = "";
							if(target.indexOf(Constants.SHARP) != -1){
								tail = target.substring(target.indexOf(Constants.SHARP));
								target = target.substring(0, target.indexOf(Constants.SHARP));
							}
							if(new File(target).isAbsolute())
								target = FileUtils.getRelativePathFromMap(rootFilePath, target);
							target = FileUtils.normalizeDirectory(currentDir, target);
							keysDefMap.put(key, target + tail);
						}
					}else if(!StringUtils.isEmptyString(keyRef)){
						//store multi-level keys.
						keysRefMap.put(key, keyRef);
					}else{
						// target is null or empty, it is useful in the future when consider the content of key definition
						keysDefMap.put(key, "");
					}
				}else{
					Properties prop = new Properties();
					prop.setProperty("%1", key);
					prop.setProperty("%2", target);
					javaLogger.logWarn(MessageUtils.getMessage("DOTJ045W", prop).toString());
				}
				//restore target
				target = temp;
			}
		}
		

		/*
		if (attrValue.startsWith(Constants.SHARP)
				|| attrValue.indexOf(Constants.COLON_DOUBLE_SLASH) != -1){
			return;
		}
		*/
		/*
		 * SF Bug 2724090, broken links in conref'ed footnotes.
		 * 
		 * NOTE: Need verification.
		 
		if (attrValue.startsWith(Constants.SHARP)) {
			attrValue = currentFile;
		}
		*/
		//external resource is filtered here.
		if ("external".equalsIgnoreCase(attrScope)
				|| "peer".equalsIgnoreCase(attrScope)
				|| attrValue.indexOf(Constants.COLON_DOUBLE_SLASH) != -1
				|| attrValue.startsWith(Constants.SHARP)) {
			return;
		}
		//Added by William on 2010-01-05 for bug:2926417 start
		if(attrValue.startsWith("file:/") && attrValue.indexOf("file://") == -1){
			attrValue = attrValue.substring("file:/".length());
			//Unix like OS
			if(Constants.SLASH.equals(File.separator)){
				attrValue = Constants.SLASH + attrValue;
			}
		}
		//Added by William on 2010-01-05 for bug:2926417 end
		File target=new File(attrValue);
		if(target.isAbsolute() && 
			!Constants.ATTRIBUTE_NAME_DATA.equals(attrName)){
			attrValue=FileUtils.getRelativePathFromMap(rootFilePath,attrValue);
		//for object tag bug:3052156
		}else if(Constants.ATTRIBUTE_NAME_DATA.equals(attrName)){
			if(!StringUtils.isEmptyString(codebase)){
				filename = FileUtils.normalizeDirectory(codebase, attrValue);
			}else{
				filename = FileUtils.normalizeDirectory(currentDir, attrValue);	
			}
		}else{
			//noraml process.
			filename = FileUtils.normalizeDirectory(currentDir, attrValue);
		}
		
		
		try{
			filename = URLDecoder.decode(filename, Constants.UTF8);
		}catch(UnsupportedEncodingException e){
			
		}
		
		if (attrClass.contains(Constants.ATTR_CLASS_VALUE_TOPICREF)) {
			if (Constants.ATTR_TYPE_VALUE_SUBJECT_SCHEME.equalsIgnoreCase(attrType)) {
				schemeSet.add(filename);
			}
			
			//Added by William on 2009-06-24 for req #12014 start
			//only transtype = eclipsehelp
			if(Constants.INDEX_TYPE_ECLIPSEHELP.equals(transtype)){
				//For only format of the href is dita topic
				if (attrFormat == null ||
						Constants.ATTR_FORMAT_VALUE_DITA.equalsIgnoreCase(attrFormat)){
					if(attrName.equals(Constants.ATTRIBUTE_NAME_HREF)){
						topicHref = filename;
						
						topicHref = topicHref.replace(Constants.BACK_SLASH, Constants.SLASH);
						//attrValue has topicId
						if(attrValue.lastIndexOf(Constants.SHARP) != -1){
							//get the topicId position
							int position = attrValue.lastIndexOf(Constants.SHARP);
							topicId = attrValue.substring(position + 1);
						}else{
							//get the first topicId(vaild href file)
							if(FileUtils.isDITAFile(topicHref)){
								//topicId = MergeUtils.getInstance().getFirstTopicId(topicHref, (new File(rootFilePath)).getParent(), true);
								//to be unique
								topicId = topicHref + Constants.QUESTION;
							}
						}
					}
				}else{
					topicHref = "";
					topicId = "";
				}
			}
			//Added by William on 2009-06-24 for req #12014 end
		}
		//files referred by coderef won't effect the uplevels, code has already returned.
		if (("DITA-foreign".equals(attrType) &&
				Constants.ATTRIBUTE_NAME_DATA.equals(attrName))
				|| attrClass!=null && attrClass.contains(Constants.ATTR_CLASS_VALUE_CODEREF)){
			
			subsidiarySet.add(filename);
			return;
		}

		/*
		 * Collect non-conref and non-copyto targets
		 */
		if (FileUtils.isValidTarget(filename.toLowerCase()) && 
				(StringUtils.isEmptyString(atts.getValue(Constants.ATTRIBUTE_NAME_COPY_TO)) ||
						!FileUtils.isTopicFile(atts.getValue(Constants.ATTRIBUTE_NAME_COPY_TO).toLowerCase()) ||
						(atts.getValue(Constants.ATTRIBUTE_NAME_CHUNK)!=null && atts.getValue(Constants.ATTRIBUTE_NAME_CHUNK).contains("to-content")) )
				&& !Constants.ATTRIBUTE_NAME_CONREF.equals(attrName)
				&& !Constants.ATTRIBUTE_NAME_COPY_TO.equals(attrName) && 
				(canResolved() || FileUtils.isSupportedImageFile(filename.toLowerCase()))) {
			//edited by william on 2009-08-06 for bug:2832696 start
			if(attrFormat!=null){
				nonConrefCopytoTargets.add(filename + Constants.STICK + attrFormat);
			}else{
				nonConrefCopytoTargets.add(filename);
			}
			//nonConrefCopytoTargets.add(filename);
			//edited by william on 2009-08-06 for bug:2832696 end
		}
		//outside ditamap files couldn't cause warning messages, it is stopped here   
		if (attrFormat != null &&
				!Constants.ATTR_FORMAT_VALUE_DITA.equalsIgnoreCase(attrFormat)){
			//The format of the href is not dita topic
			//The logic after this "if" clause is not related to files other than dita topic.
			//Therefore, we need to return here to filter out those files in other format.
			return;
		}

		/*
		 * Collect only href target topic files for index extracting.
		 */
		if (Constants.ATTRIBUTE_NAME_HREF.equals(attrName)
				&& FileUtils.isTopicFile(filename) && canResolved()) {
			hrefTargets.add(new File(filename).getPath());
			toOutFile(new File(filename).getPath());
			//use filename instead(It has already been resolved before-hand) bug:3058124
			//String pathWithoutID = FileUtils.resolveFile(currentDir, attrValue);
			if (chunkLevel > 0 && chunkToNavLevel == 0 && topicGroupLevel == 0 && relTableLevel == 0) {
				chunkTopicSet.add(filename);
			} else {
				hrefTopicSet.add(filename);
			}
		}
		
		//Added on 20100827 for bug:3052156 start
		//add a warning message for outer files refered by @data
		/*if(Constants.ATTRIBUTE_NAME_DATA.equals(attrName)){
			toOutFile(new File(filename).getPath());
		}*/
		//Added on 20100827 for bug:3052156 end
		
		/*
		 * Collect only conref target topic files.
		 */
		if (Constants.ATTRIBUTE_NAME_CONREF.equals(attrName)
				&& FileUtils.isDITAFile(filename)) {
			conrefTargets.add(filename);
			toOutFile(new File(filename).getPath());
		}
		
		// Collect copy-to (target,source) into hash map
		if (Constants.ATTRIBUTE_NAME_COPY_TO.equals(attrName)
				&& FileUtils.isTopicFile(filename)) {
			String href = atts.getValue(Constants.ATTRIBUTE_NAME_HREF);
			
			if (StringUtils.isEmptyString(href)) {
				StringBuffer buff = new StringBuffer();
				buff.append("[WARN]: Copy-to task [href=\"\" copy-to=\"");
				buff.append(filename);
				buff.append("\"] was ignored.");
				javaLogger.logWarn(buff.toString());
			} else if (copytoMap.get(filename) != null){
				//edited by Alan on Date:2009-11-02 for Work Item:#1590 start
				/*StringBuffer buff = new StringBuffer();
				buff.append("Copy-to task [href=\"");
				buff.append(href);
				buff.append("\" copy-to=\"");
				buff.append(filename);
				buff.append("\"] which points to another copy-to target");
				buff.append(" was ignored.");
				javaLogger.logWarn(buff.toString());*/
        		Properties prop = new Properties();
        		prop.setProperty("%1", href);
        		prop.setProperty("%2", filename);
        		javaLogger.logWarn(MessageUtils.getMessage("DOTX065W", prop).toString());
				//edited by Alan on Date:2009-11-02 for Work Item:#1590 end
				ignoredCopytoSourceSet.add(href);
			} else if (!(atts.getValue(Constants.ATTRIBUTE_NAME_CHUNK) != null && atts.getValue(Constants.ATTRIBUTE_NAME_CHUNK).contains("to-content"))){
				copytoMap.put(filename, FileUtils.normalizeDirectory(currentDir, href));
			}
			
			String pathWithoutID = FileUtils.resolveFile(currentDir, attrValue);
			if (chunkLevel > 0 && chunkToNavLevel == 0 && topicGroupLevel == 0) {
				chunkTopicSet.add(pathWithoutID);
			} else {
				hrefTopicSet.add(pathWithoutID);
			}
				
		}
		
		/*
		 * Collect the conaction source topic file
		 */
		if(Constants.ATTRIBUTE_NAME_CONACTION.equals(attrName)){
			if(attrValue.equals("mark")||attrValue.equals("pushreplace")){
				hasconaction = true;
			}
				
		}
	}
	
	//Added on 20100826 for bug:3052913 start
	//get multi-level keys list
	private List<String> getKeysList(String key, Map<String, String> keysRefMap) {
		
		List<String> list = new ArrayList<String>();
		
		//Iterate the map to look for multi-level keys
		Iterator<Entry<String, String>> iter = keysRefMap.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String, String> entry = iter.next();
			//Multi-level key found
			if(entry.getValue().equals(key)){
				//add key into the list
				String entryKey = entry.getKey();
				list.add(entryKey);
				//still have multi-level keys
				if(keysRefMap.containsValue(entryKey)){
					//rescuive point
					List<String> tempList = getKeysList(entryKey, keysRefMap);
					list.addAll(tempList);
				}
			}
		}
		
		return list;
	}
	//update keysDefMap for multi-level keys
	private void checkMultiLevelKeys(Map<String, String> keysDefMap,
			Map<String, String> keysRefMap) {
		
		String key = null;
		String value = null;
		//tempMap storing values to avoid ConcurrentModificationException
		Map<String, String> tempMap = new HashMap<String, String>();
		Iterator<Entry<String, String>> iter = keysDefMap.entrySet().iterator();
		
		while (iter.hasNext()) {
			Map.Entry<String, String> entry = iter.next();
			key = entry.getKey();
			value = entry.getValue();
			//there is multi-level keys exist.
			if(keysRefMap.containsValue(key)){
				//get multi-level keys
				 List<String> keysList = getKeysList(key, keysRefMap);
				 for (String multikey : keysList) {
					 //update tempMap
					 tempMap.put(multikey, value);
				}
			}
		}
		//update keysDefMap.
		keysDefMap.putAll(tempMap);
	}
	//Added on 20100826 for bug:3052913 end

	private boolean isOutFile(String toCheckPath) {
		if (!toCheckPath.startsWith(".."))
			return false;
		else
			return true;
	}

	private boolean isMapFile() {
		String current=FileUtils.removeRedundantNames(currentFile);
		if(FileUtils.isDITAMapFile(current))	
			return true;
		else
			return false;
	}
	private boolean canResolved(){
		if ((OutputUtils.getOnlyTopicInMap() == false) || isMapFile() )
			return true;
		else
			return false;
	}
	private void addToOutFilesSet(String hrefedFile) {
		if (canResolved()) {
			outDitaFilesSet.add(hrefedFile);
		}

	}
	/*
	private Element createElement(String uri, String qName,
			Attributes atts) {
		if (schemeRoot != null) {
			Element element = schemeRoot.createElementNS(uri, qName);
			for (int i = 0; i < atts.getLength(); i++) {
				element.setAttribute(atts.getQName(i), atts.getValue(i));
			}
			return element;
		}
		return null;
	}
	*/
	private void toOutFile(String filename) throws SAXException {
		//the filename is a relative path from the dita input file
		Properties prop=new Properties();
		prop.put("%1", FileUtils.normalizeDirectory(rootDir, filename));
		prop.put("%2", FileUtils.removeRedundantNames(currentFile));
		if ((OutputUtils.getGeneratecopyouter() == OutputUtils.NOT_GENERATEOUTTER) 
				|| (OutputUtils.getGeneratecopyouter() == OutputUtils.GENERATEOUTTER)) {
			if (isOutFile(filename)) {
				if (OutputUtils.getOutterControl().equals(OutputUtils.OUTTERCONTROL_FAIL)){
					MessageBean msgBean=MessageUtils.getMessage("DOTJ035F", prop);	
					throw new SAXParseException(null,null,new DITAOTException(msgBean,null,msgBean.toString()));	
				}
				if (OutputUtils.getOutterControl().equals(OutputUtils.OUTTERCONTROL_WARN)){
					String message=MessageUtils.getMessage("DOTJ036W",prop).toString();
					javaLogger.logWarn(message);
				}
				addToOutFilesSet(filename);
			}

		}

	}
	/**
	 * Get out file set.
	 * @return out file set
	 */
	public Set<String> getOutFilesSet(){
		return outDitaFilesSet;
	}

	/**
	 * @return the hrefTopicSet
	 */
	public Set<String> getHrefTopicSet() {
		return hrefTopicSet;
	}

	/**
	 * @return the chunkTopicSet
	 */
	public Set<String> getChunkTopicSet() {
		return chunkTopicSet;
	}
	/**
	 * Get scheme set.
	 * @return scheme set
	 */
	public Set<String> getSchemeSet() {
		return this.schemeSet;
	}
	/**
	 * Get scheme ref set.
	 * @return scheme ref set
	 */
	public Set<String> getSchemeRefSet() {
		return this.schemeRefSet;
	}
	
	/**
	 * List of files with "@processing-role=resource-only".
	 * @return the resource-only set
	 */
	public Set<String> getResourceOnlySet() {
		resourceOnlySet.removeAll(crossSet);
        return resourceOnlySet;
    }
	
	/**
	 * Get document root of the merged subject schema.
	 * @return
	 */
	//public Document getSchemeRoot() {
	//	return schemeRoot;
	//}
	/**
	 * Get getRelationshipGrap.
	 * @return relationship grap
	 */
	public Map<String, Set<String>> getRelationshipGrap() {
		return this.relationGraph;
	}

	/**
	 * @return the catalogMap
	 */
	public static HashMap<String, String> getCatalogMap() {
		return catalogMap;
	}

	public String getPrimaryDitamap() {
		return primaryDitamap;
	}

	public void setPrimaryDitamap(String primaryDitamap) {
		this.primaryDitamap = primaryDitamap;
	}
	
}