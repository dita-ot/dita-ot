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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.exception.DITAOTXMLErrorHandler;
import org.dita.dost.log.DITAOTJavaLogger;
import org.dita.dost.log.MessageBean;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.util.CatalogUtils;
import org.dita.dost.util.Constants;
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
import org.xml.sax.helpers.XMLReaderFactory;


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
    
	/**
	 * Constructor
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
		
		processRoleLevel = 0;
		processRoleStack = new Stack<String>();
		resourceOnlySet = new HashSet<String>(Constants.INT_32);
		crossSet = new HashSet<String>(Constants.INT_32);
		
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
     * @throws SAXException
     * @param ditaDir 
     */
	public static void initXMLReader(String ditaDir,boolean validate,String rootFile) throws SAXException {
		DITAOTJavaLogger javaLogger=new DITAOTJavaLogger();
		if (System.getProperty(Constants.SAX_DRIVER_PROPERTY) == null) {
			// The default sax driver is set to xerces's sax driver
			StringUtils.initSaxDriver();
		}
		
		//to check whether the current parsing file's href value is out of inputmap.dir
		rootDir=new File(rootFile).getAbsoluteFile().getParent();
		rootFilePath=new File(rootFile).getAbsolutePath();
		reader = XMLReaderFactory.createXMLReader();
		reader.setFeature(Constants.FEATURE_NAMESPACE_PREFIX, true);
		if(validate==true){
			reader.setFeature(Constants.FEATURE_VALIDATION, true);
			reader.setFeature(Constants.FEATURE_VALIDATION_SCHEMA, true);
		}else{
			String msg=MessageUtils.getMessage("DOTJ037W").toString();
			javaLogger.logWarn(msg);
		}
		
		CatalogUtils.initCatalogResolver(ditaDir);
		catalogMap = CatalogUtils.getCatalog(ditaDir);
	}

	/**
	 * 
	 * Reset the internal variables
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
		schemeSet.clear();
		schemeRefSet.clear();
		
		//@processing-role
		processRoleLevel = 0;
		processRoleStack.clear();
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
	 * @return
	 */
	public boolean hasConRef() {
		return hasConRef;
	}
	
	/**
	 * To see if the parsed file has keyref inside.
	 * 
	 * @return 
	 */
	public boolean hasKeyRef(){
		return hasKeyRef;
	}
	
	/**
	 * To see if the parsed file has coderef inside.
	 * 
	 * @return
	 */
	public boolean hasCodeRef(){
		return hasCodeRef;
	}

	/**
	 * To see if the parsed file has href inside.
	 * 
	 * @return
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
		
		return nonCopytoSet;
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
     * Returns the ignoredCopytoSourceSet
     *
     * @return Returns the ignoredCopytoSourceSet.
     */
	public Set<String> getIgnoredCopytoSourceSet() {
		return ignoredCopytoSourceSet;
	}

	/**
	 * Get the copy-to map.
	 * 
	 * @return
	 */
	public Map<String, String> getCopytoMap() {
		return copytoMap;
	}
	
	/**
	 * Get the Key definitions.
	 * 
	 * @return
	 */
	public Map<String,String> getKeysDMap(){
		return keysDefMap;
	}
	/**
	 * Set the relative directory of current file.
	 * 
	 * @param dir
	 */
	public void setCurrentDir(String dir) {
		this.currentDir = dir;
	}

	/**
	 * Check if the current file is valid after filtering.
	 * 
	 * @return
	 */
	public boolean isValidInput() {
		return isValidInput;
	}
	
	/**
	 * Check if the current file has conaction
	 */
	public boolean hasConaction(){
		return hasconaction;
	}
	/**
	 * Parse input xml file.
	 * 
	 * @param file
	 * @throws SAXException 
	 * @throws IOException 
	 * @throws FileNotFoundException
	 */
	public void parse(File file) throws FileNotFoundException, IOException, SAXException {	
		currentFile=file.getAbsolutePath();
		reader.setErrorHandler(new DITAOTXMLErrorHandler(file.getName()));
		reader.parse(new InputSource(new FileInputStream(file)));	
	}

	/**
	 * Parse specific attributes for info used in later process.
	 * 
	 * @param uri
	 * @param localName
	 * @param qName
	 * @param atts
	 * 
	 * @exception org.xml.sax.SAXException
	 */
	public void startElement(String uri, String localName, String qName,
			Attributes atts) throws SAXException {
		String domains = null;
		Properties params = new Properties();
		
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
		
		// Generate Scheme relationship graph
		if (attrValue != null) {
			if (attrValue.contains(Constants.ATTR_CLASS_VALUE_SUBJECT_SCHEME)) {
				if (this.relationGraph == null)
					this.relationGraph = new LinkedHashMap<String, Set<String>>();
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
	 * Clean up.
	 * @see org.dita.dost.reader.AbstractXMLReader#endDocument()
	 */
	@Override
	public void endDocument() throws SAXException {
		if (processRoleLevel > 0) {
			processRoleLevel--;
			processRoleStack.pop();
		}
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

	/** (non-Javadoc)
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
	}

	/**
	 * Resolve the publicId used in XMLCatalog.
	 * 
	 * @param publicId
	 * @param systemId
	 * @throws java.io.IOException
	 * @exception org.xml.sax.SAXException
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
			// Many keys can be defined in a single definition, like keys="a b c", a, b and c are seperated by blank.
			for(String key: attrValue.split(" ")){
				if(!keysDefMap.containsKey(key) && !key.equals("")){
					if(target != null && !target.equals(Constants.STRING_EMPTY)){
						if(attrScope!=null && (attrScope.equals("external") || attrScope.equals("peer"))){
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
		
		if ("external".equalsIgnoreCase(attrScope)
				|| "peer".equalsIgnoreCase(attrScope)
				|| attrValue.indexOf(Constants.COLON_DOUBLE_SLASH) != -1
				|| attrValue.startsWith(Constants.SHARP)) {
			return;
		}
		
		File target=new File(attrValue);
		if(target.isAbsolute()){
			attrValue=FileUtils.getRelativePathFromMap(rootFilePath,attrValue);
		}
		
		filename = FileUtils.normalizeDirectory(currentDir, attrValue);
		try{
			filename = URLDecoder.decode(filename, Constants.UTF8);
		}catch(UnsupportedEncodingException e){
			
		}
		
		if (attrClass.contains(Constants.ATTR_CLASS_VALUE_TOPICREF)) {
			if (Constants.ATTR_TYPE_VALUE_SUBJECT_SCHEME.equalsIgnoreCase(attrType)) {
				schemeSet.add(filename);
			}
		}
		
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
				&& !Constants.ATTRIBUTE_NAME_COPY_TO.equals(attrName) && canResolved() ) {
			nonConrefCopytoTargets.add(filename);
		}
		
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
			String pathWithoutID = FileUtils.resolveFile(currentDir, attrValue);
			if (chunkLevel > 0 && chunkToNavLevel == 0 && topicGroupLevel == 0) {
				chunkTopicSet.add(pathWithoutID);
			} else {
				hrefTopicSet.add(pathWithoutID);
			}
		}
		
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
				buff.append("Copy-to task [href=\"\" copy-to=\"");
				buff.append(filename);
				buff.append("\"] was ignored.");
				javaLogger.logWarn(buff.toString());
			} else if (copytoMap.get(filename) != null){
				StringBuffer buff = new StringBuffer();
				buff.append("Copy-to task [href=\"");
				buff.append(href);
				buff.append("\" copy-to=\"");
				buff.append(filename);
				buff.append("\"] which points to another copy-to target");
				buff.append(" was ignored.");
				javaLogger.logWarn(buff.toString());
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
	
	public Set<String> getSchemeSet() {
		return this.schemeSet;
	}
	
	public Set<String> getSchemeRefSet() {
		return this.schemeRefSet;
	}
	
	/**
	 * List of files with "@processing-role=resource-only"
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
	
	public Map<String, Set<String>> getRelationshipGrap() {
		return this.relationGraph;
	}

	/**
	 * @return the catalogMap
	 */
	public static HashMap<String, String> getCatalogMap() {
		return catalogMap;
	}
}