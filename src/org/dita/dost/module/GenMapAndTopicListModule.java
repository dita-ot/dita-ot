/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.module;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import javax.xml.parsers.ParserConfigurationException;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.DITAOTFileLogger;
import org.dita.dost.log.DITAOTJavaLogger;
import org.dita.dost.log.MessageBean;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.pipeline.PipelineHashIO;
import org.dita.dost.reader.DitaValReader;
import org.dita.dost.reader.GenListModuleReader;
import org.dita.dost.reader.GrammarPoolManager;
import org.dita.dost.util.Constants;
import org.dita.dost.util.DelayConrefUtils;
import org.dita.dost.util.FileUtils;
import org.dita.dost.util.FilterUtils;
import org.dita.dost.util.OutputUtils;
import org.dita.dost.util.StringUtils;
import org.dita.dost.util.TimingUtils;
import org.dita.dost.writer.PropertiesWriter;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


/**
 * This class extends AbstractPipelineModule, used to generate map and topic
 * list by parsing all the refered dita files.
 * 
 * @version 1.0 2004-11-25
 * 
 * @author Wu, Zhi Qiang
 */
public class GenMapAndTopicListModule implements AbstractPipelineModule {
	/** Set of all dita files */
	private Set<String> ditaSet = null;

	/** Set of all topic files */
	private Set<String> fullTopicSet = null;

	/** Set of all map files */
	private Set<String> fullMapSet = null;

	/** Set of topic files containing href */
	private Set<String> hrefTopicSet = null;
	
	/** Set of href topic files with anchor ID */
	private Set<String> hrefWithIDSet = null;
	
	/** Set of chunk topic with anchor ID */
	private Set<String> chunkTopicSet = null;

	/** Set of map files containing href */
	private Set<String> hrefMapSet = null;

	/** Set of dita files containing conref */
	private Set<String> conrefSet = null;
	
	/** Set of topic files containing coderef */
	private Set<String> coderefSet = null;

	/** Set of all images */
	private Set<String> imageSet = null;
	
	/** Set of all images used for flagging */
	private Set<String> flagImageSet = null;

	/** Set of all html files */
	private Set<String> htmlSet = null;

	/** Set of all the href targets */
	private Set<String> hrefTargetSet = null;

	/** Set of all the conref targets */
	private Set<String> conrefTargetSet = null;
	
	/** Set of all the copy-to sources */
	private Set<String> copytoSourceSet = null;
	
	/** Set of all the non-conref targets */
	private Set<String> nonConrefCopytoTargetSet = null;
	
	/** Set of sources of those copy-to that were ignored */
	private Set<String> ignoredCopytoSourceSet = null;
	
	/** Set of subsidiary files */
	private Set<String> subsidiarySet = null;
	
	/** Set of relative flag image files */
	private Set<String> relFlagImagesSet=null;
	
	/** Map of all copy-to (target,source) */
	private Map<String, String> copytoMap = null;
	
	/** List of files waiting for parsing */
	private List<String> waitList = null;

	/** List of parsed files */
	private List<String> doneList = null;
	
	/** Set of outer dita files */
	private Set<String> outDitaFilesSet=null;
	
	/** Set of sources of conacion */
	private Set<String> conrefpushSet;
	
	/** Set of files containing keyref */
	private Set<String> keyrefSet = null;
	
	/** Set of files with "@processing-role=resource-only" */
	private Set<String> resourceOnlySet;
	
	/** Map of all key definitions*/
	private Map<String,String> keysDefMap = null;
	
	/** Basedir for processing */
	private String baseInputDir = null;

	/** Tempdir for processing */
	private String tempDir = null;

	/** ditadir for processing */
	private String ditaDir = null;

	private String inputFile = null;
	
	private String ditavalFile = null;

	private int uplevels = 0;

	private String prefix = "";

	private DITAOTJavaLogger javaLogger = new DITAOTJavaLogger();

	private GenListModuleReader reader = null;
	
	private boolean xmlValidate=true;
	
	private String relativeValue=null;
	
	private String formatRelativeValue=null;
	
	private String rootFile=null;
	
	private OutputStreamWriter keydef;
	
	// keydef file from keys used in schema files
	private OutputStreamWriter schemekeydef;
	
	//Added by William on 2009-06-25 for req #12014 start
	//export file
	private OutputStreamWriter export;
	//Added by William on 2009-06-25 for req #12014 end
	
	private Set<String> schemeSet;
	
	private Map<String, Set<String>> schemeDictionary = null;
	//Added by William on 2009-07-18 for req #12014 start
	private String transtype;
	//Added by William on 2009-07-18 for req #12014 end
	
	//Added by William on 2010-06-09 for bug:3013079 start
	private Map<String, String> exKeyDefMap = null;
	//Added by William on 2010-06-09 for bug:3013079 end
	
	private DITAOTFileLogger fileLogger = DITAOTFileLogger.getInstance();
	
	private String moduleStartMsg = "GenMapAndTopicListModule.execute(): Starting...";
	
	private String moduleEndMsg = "GenMapAndTopicListModule.execute(): Execution time: ";
	
	//Added on 2010-08-24 for bug:2994593 start
	/** use grammar pool cache */
	private String gramcache = "yes";
	//Added on 2010-08-24 for bug:2994593 end
	
	//Added on 2010-08-24 for bug:3086552 start
	private boolean setSystemid = true; 
	//Added on 2010-08-24 for bug:3086552 end
	/**
	 * Create a new instance and do the initialization.
	 * 
	 * @throws ParserConfigurationException never throw such exception
	 * @throws SAXException never throw such exception
	 */
	public GenMapAndTopicListModule() throws SAXException,
			ParserConfigurationException {
		ditaSet = new HashSet<String>(Constants.INT_128);
		fullTopicSet = new HashSet<String>(Constants.INT_128);
		fullMapSet = new HashSet<String>(Constants.INT_128);
		hrefTopicSet = new HashSet<String>(Constants.INT_128);
		hrefWithIDSet = new HashSet<String>(Constants.INT_128);
		chunkTopicSet = new HashSet<String>(Constants.INT_128);
		schemeSet = new HashSet<String>(Constants.INT_128);
		hrefMapSet = new HashSet<String>(Constants.INT_128);
		conrefSet = new HashSet<String>(Constants.INT_128);
		imageSet = new HashSet<String>(Constants.INT_128);
		flagImageSet = new LinkedHashSet<String>(Constants.INT_128);
		htmlSet = new HashSet<String>(Constants.INT_128);
		hrefTargetSet = new HashSet<String>(Constants.INT_128);
		subsidiarySet = new HashSet<String>(Constants.INT_16);
		waitList = new LinkedList<String>();
		doneList = new LinkedList<String>();
		conrefTargetSet = new HashSet<String>(Constants.INT_128);
		nonConrefCopytoTargetSet = new HashSet<String>(Constants.INT_128);
		copytoMap = new HashMap<String, String>();
		copytoSourceSet = new HashSet<String>(Constants.INT_128);
		ignoredCopytoSourceSet = new HashSet<String>(Constants.INT_128);
		outDitaFilesSet=new HashSet<String>(Constants.INT_128);
		relFlagImagesSet=new LinkedHashSet<String>(Constants.INT_128);
		conrefpushSet = new HashSet<String>(Constants.INT_128);
		keysDefMap = new HashMap<String, String>();
		exKeyDefMap = new HashMap<String, String>();
		keyrefSet = new HashSet<String>(Constants.INT_128);
		coderefSet = new HashSet<String>(Constants.INT_128);
		
		this.schemeDictionary = new HashMap<String, Set<String>>();
		
		//@processing-role
		resourceOnlySet = new HashSet<String>(Constants.INT_128);
	}

    /**
     * {@inheritDoc}
     */
	public AbstractPipelineOutput execute(AbstractPipelineInput input)
			throws DITAOTException {
		Date startTime = TimingUtils.getNowTime();
		
		try {
			fileLogger.logInfo(moduleStartMsg);
			parseInputParameters(input);
			
			//set grammar pool flag
			GrammarPoolManager.setGramCache(gramcache);

			GenListModuleReader.initXMLReader(ditaDir,xmlValidate,rootFile, setSystemid);
			//Added on 2010-08-24 for bug:3086552 start
			DitaValReader.initXMLReader(setSystemid);
			//Added on 2010-08-24 for bug:3086552 end
			
			// first parse filter file for later use
			parseFilterFile();
			
			addToWaitList(inputFile);
			processWaitList();
			//Depreciated function
			//The base directory does not change according to the referenceing topic files in the new resolution 
			updateBaseDirectory();
			refactoringResult();
			outputResult();
			keydef.write("</stub>");
			keydef.close();
			//Added by William on 2009-06-09 for scheme key bug start
			// write the end tag
			schemekeydef.write("</stub>");
			// close the steam
			schemekeydef.close();
			//Added by William on 2009-06-09 for scheme key bug end
			
			//Added by William on 2009-06-25 for req #12014 start
			// write the end tag
			export.write("</stub>");
			// close the steam
			export.close();
			//Added by William on 2009-06-25 for req #12014 end
		}catch(DITAOTException e){
			throw e;
		}catch (SAXException e) {
			throw new DITAOTException(e.getMessage(), e);
		} catch(Exception e){
			throw new DITAOTException(e.getMessage(), e);
		} finally {
			
			fileLogger.logInfo(moduleEndMsg + TimingUtils.reportElapsedTime(startTime));

		}

		return null;
	}

	private void parseInputParameters(AbstractPipelineInput input) {
		File inFile = null;
		PipelineHashIO hashIO = (PipelineHashIO) input;
		String basedir = hashIO
				.getAttribute(Constants.ANT_INVOKER_PARAM_BASEDIR);
		String ditaInput = hashIO
				.getAttribute(Constants.ANT_INVOKER_PARAM_INPUTMAP);

		tempDir = hashIO.getAttribute(Constants.ANT_INVOKER_PARAM_TEMPDIR);
		ditaDir = hashIO.getAttribute(Constants.ANT_INVOKER_EXT_PARAM_DITADIR);
		ditavalFile = hashIO.getAttribute(Constants.ANT_INVOKER_PARAM_DITAVAL);
		String valueOfValidate=hashIO.getAttribute(Constants.ANT_INVOKER_EXT_PARAM_VALIDATE);
		
		//Added by William on 2009-07-18 for req #12014 start
        //get transtype
        transtype = ((PipelineHashIO) input).getAttribute(Constants.ANT_INVOKER_EXT_PARAM_TRANSTYPE);
        //Added by William on 2009-07-18 for req #12014 start
        
        gramcache = ((PipelineHashIO) input).getAttribute(Constants.ANT_INVOKER_EXT_PARAM_GRAMCACHE);
        
		if(valueOfValidate!=null){
			if("false".equalsIgnoreCase(valueOfValidate))
				xmlValidate=false;
			else
				xmlValidate=true;
		}
		
		//Added on 2010-08-24 for bug:3086552 start
		String setSystemid_tmp = ((PipelineHashIO) input).getAttribute(Constants.ANT_INVOKER_EXT_PARAN_SETSYSTEMID);
		if(setSystemid_tmp.equals("yes")) {
			setSystemid = true;
		} else {
			setSystemid = false;
		}
		//Added on 2010-08-24 for bug:3086552 end
		
		//For the output control
		OutputUtils.setGeneratecopyouter(hashIO.getAttribute(Constants.ANT_INVOKER_EXT_PARAM_GENERATECOPYOUTTER));
		OutputUtils.setOutterControl(hashIO.getAttribute(Constants.ANT_INVOKER_EXT_PARAM_OUTTERCONTROL));
		OutputUtils.setOnlyTopicInMap(hashIO.getAttribute(Constants.ANT_INVOKER_EXT_PARAM_ONLYTOPICINMAP));
		
        //Set the OutputDir
		File path=new File(hashIO.getAttribute(Constants.ANT_INVOKER_EXT_PARAM_OUTPUTDIR));
		if(path.isAbsolute())
			OutputUtils.setOutputDir(hashIO.getAttribute(Constants.ANT_INVOKER_EXT_PARAM_OUTPUTDIR));
		else{
			StringBuffer buff=new StringBuffer(hashIO.getAttribute(Constants.ANT_INVOKER_PARAM_BASEDIR)).append(File.separator).append(path);
			OutputUtils.setOutputDir(buff.toString());
			
		} 
			
		
		
		/*
		 * Resolve relative paths base on the basedir.
		 */
		inFile = new File(ditaInput);
		if (!inFile.isAbsolute()) {
			inFile = new File(basedir, ditaInput);
		}
		if (!new File(tempDir).isAbsolute()) {
			tempDir = new File(basedir, tempDir).getAbsolutePath();
		}else{
			tempDir = FileUtils.removeRedundantNames(tempDir);
		}
		if (!new File(ditaDir).isAbsolute()) {
			ditaDir = new File(basedir, ditaDir).getAbsolutePath();
		}else{
			ditaDir = FileUtils.removeRedundantNames(ditaDir);
		}
		if (ditavalFile != null && !new File(ditavalFile).isAbsolute()) {
			ditavalFile = new File(basedir, ditavalFile).getAbsolutePath();
		}

		baseInputDir = new File(inFile.getAbsolutePath()).getParent();
		baseInputDir = FileUtils.removeRedundantNames(baseInputDir);
		
		rootFile=inFile.getAbsolutePath();
		rootFile = FileUtils.removeRedundantNames(rootFile);
		
		inputFile = inFile.getName();
		try {
			keydef = new OutputStreamWriter(new FileOutputStream(new File(tempDir,"keydef.xml")));
			keydef.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
			keydef.write("<stub>");
			//Added by William on 2009-06-09 for scheme key bug
			// create the keydef file for scheme files
			schemekeydef = new OutputStreamWriter(new FileOutputStream(
					new File(tempDir, "schemekeydef.xml")));
			// write the head
			schemekeydef.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
			schemekeydef.write("<stub>");
			
			//Added by William on 2009-06-25 for req #12014 start
			// create the export file for exportanchors
			// write the head
			export = new OutputStreamWriter(new FileOutputStream(
					new File(tempDir, Constants.FILE_NAME_EXPORT_XML)));
			export.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
			export.write("<stub>");
			//Added by William on 2009-06-25 for req #12014 end
		} catch (FileNotFoundException e) {
			javaLogger.logException(e);
		} catch (IOException e){
			javaLogger.logException(e);
		}
			
		//Set the mapDir
		OutputUtils.setInputMapPathName(inFile.getAbsolutePath());	
	}
	
	/**
	 * 
	 * @throws DITAOTException
	 * 
	 */
	private void processWaitList() throws DITAOTException {
		reader = new GenListModuleReader();
		//Added by William on 2009-07-18 for req #12014 start
		reader.setTranstype(transtype);
		//Added by William on 2009-07-18 for req #12014 end
		
		if(FileUtils.isDITAMapFile(inputFile)){
			reader.setPrimaryDitamap(inputFile);
		}
		
		
		while (!waitList.isEmpty()) {
			processFile((String) waitList.remove(0));
		}
	}

	private void processFile(String currentFile) throws DITAOTException {
		
		String logMsg = "GenMapAndTopicListModule.processFile(): Processing file " + currentFile + "...";
		fileLogger.logInfo(logMsg);
		
		File fileToParse;
		File file=new File(currentFile);
		if(file.isAbsolute()){
			fileToParse=file;
			currentFile=FileUtils.getRelativePathFromMap(rootFile,currentFile);
			
		}else{	
			fileToParse = new File(baseInputDir, currentFile);
		}
		String msg = null;
		Properties params = new Properties();
		params.put("%1", currentFile);		

		try {
			fileToParse = fileToParse.getCanonicalFile();
			if (FileUtils.isValidTarget(currentFile.toLowerCase()))
			{
				reader.setTranstype(transtype);
				reader.setCurrentDir(new File(currentFile).getParent());
				reader.parse(fileToParse);
				
			}else{
				//edited by Alan on Date:2009-11-02 for Work Item:#1590 start
				/*javaLogger.logWarn("Input file name is not valid DITA file name.");*/
				Properties prop = new Properties();
				prop.put("%1", fileToParse);
				javaLogger.logWarn(MessageUtils.getMessage("DOTJ021W", params).toString());
				//edited by Alan on Date:2009-11-02 for Work Item:#1590 end
			}

			// don't put it into dita.list if it is invalid
			if (reader.isValidInput()) {
				processParseResult(currentFile);
				categorizeCurrentFile(currentFile);
			} else if (!currentFile.equals(inputFile)) {
				javaLogger.logWarn(MessageUtils.getMessage("DOTJ021W", params).toString());
			}	
		}catch(SAXParseException sax){
			
			// To check whether the inner third level is DITAOTBuildException
			// :FATALERROR
				Exception inner = sax.getException();
				if (inner != null && inner instanceof DITAOTException) {// second level
					System.out.println(inner.getMessage());
					throw (DITAOTException)inner;
				}
				if (currentFile.equals(inputFile)) {
					// stop the build if exception thrown when parsing input file.
					MessageBean msgBean=MessageUtils.getMessage("DOTJ012F", params);
					msg = MessageUtils.getMessage("DOTJ012F", params).toString();
					msg = new StringBuffer(msg).append(":")
							.append(sax.getMessage()).toString();				
					throw new DITAOTException(msgBean, sax,msg);
				}
				StringBuffer buff=new StringBuffer();
				msg = MessageUtils.getMessage("DOTJ013E", params).toString();
				buff.append(msg).append(Constants.LINE_SEPARATOR).append(sax.getMessage());
				javaLogger.logError(buff.toString());
		}
		catch (Exception e) {
			
			if (currentFile.equals(inputFile)) {
				// stop the build if exception thrown when parsing input file.
				MessageBean msgBean=MessageUtils.getMessage("DOTJ012F", params);
				msg = MessageUtils.getMessage("DOTJ012F", params).toString();
				msg = new StringBuffer(msg).append(":")
						.append(e.getMessage()).toString();				
				throw new DITAOTException(msgBean, e,msg);
			}
			StringBuffer buff=new StringBuffer();
			msg = MessageUtils.getMessage("DOTJ013E", params).toString();
			buff.append(msg).append(Constants.LINE_SEPARATOR).append(e.getMessage());
			javaLogger.logError(buff.toString());
		}
		
		if (!reader.isValidInput() && currentFile.equals(inputFile)) {
					
			if(xmlValidate==true){
				// stop the build if all content in the input file was filtered out.
				msg = MessageUtils.getMessage("DOTJ022F", params).toString();		
				throw new DITAOTException(msg);
			}else{
				// stop the build if the content of the file is not valid.
				msg = MessageUtils.getMessage("DOTJ034F", params).toString();		
				throw new DITAOTException(msg);
			}

		}
		
		doneList.add(currentFile);
		reader.reset();

	}

	/**
	 * @param currentFile
	 */
	private void processParseResult(String currentFile) {
		Iterator<String> iter = reader.getNonCopytoResult().iterator();
		Map<String, String> cpMap = reader.getCopytoMap();
		Map<String, String> kdMap = reader.getKeysDMap();
		//Added by William on 2010-06-09 for bug:3013079 start
		//the reader's reset method will clear the map.
		Map<String, String> exKdMap = reader.getExKeysDefMap();
		exKeyDefMap.putAll(exKdMap);
		//Added by William on 2010-06-09 for bug:3013079 end
		
		/*
		 * Category non-copyto result and update uplevels accordingly
		 */
		while (iter.hasNext()) {
			String file = (String) iter.next();
			categorizeResultFile(file);
			updateUplevels(file);
		}

		/*
		 * Update uplevels for copy-to targets, and store
		 * copy-to map.
		 * 
		 * Note: same key(target) copy-to will be ignored.
		 */
		iter = cpMap.keySet().iterator();
		while (iter.hasNext()) {
			String key = (String) iter.next();
			String value = (String) cpMap.get(key);
			
			if (copytoMap.containsKey(key)) {
				//edited by Alan on Date:2009-11-02 for Work Item:#1590 start
				/*StringBuffer buff = new StringBuffer();
				buff.append("Copy-to task [href=\"");
				buff.append(value);
				buff.append("\" copy-to=\"");
				buff.append(key);
				buff.append("\"] which points to another copy-to target");
				buff.append(" was ignored.");
				javaLogger.logWarn(buff.toString());*/
        		Properties prop = new Properties();
        		prop.setProperty("%1", value);
        		prop.setProperty("%2", key);
        		javaLogger.logWarn(MessageUtils.getMessage("DOTX065W", prop).toString());
				//edited by Alan on Date:2009-11-02 for Work Item:#1590 end
				ignoredCopytoSourceSet.add(value);
			} else {
				updateUplevels(key);
				copytoMap.put(key, value);				
			}
		}
		// TODO Added by William on 2009-06-09 for scheme key bug(497)
		schemeSet.addAll(reader.getSchemeRefSet());
		
		/**
		 * collect key definitions 
		 */
		iter = kdMap.keySet().iterator();
		while(iter.hasNext()){
			String key = (String)iter.next();
			String value = (String)kdMap.get(key);
			if(keysDefMap.containsKey(key)){
			// if there already exists duplicated key definition in different map files.
			// Should only emit this if in a debug mode; comment out for now
				/*Properties prop = new Properties();
				prop.put("%1", key);
				prop.put("%2", value);
				prop.put("%3", currentFile);
				javaLogger
						.logInfo(MessageUtils.getMessage("DOTJ048I", prop).toString());*/
			}else{
				updateUplevels(key);
				// add the ditamap where it is defined.
				/*try {
					keydef.write("<keydef ");
					keydef.write("keys=\""+key+"\" ");
					keydef.write("href=\""+value+"\" ");
					keydef.write("source=\""+currentFile+"\"/>");
					keydef.write("\n");
					keydef.flush();
				} catch (IOException e) {

					javaLogger.logException(e);
				}*/
				
				keysDefMap.put(key, value+"("+currentFile+")");
			}
			// TODO Added by William on 2009-06-09 for scheme key bug(532-547)
			// if the current file is also a schema file
			if (schemeSet.contains(currentFile)) {
				// write the keydef into the scheme keydef file
				try {
					schemekeydef.write("<keydef ");
					schemekeydef.write("keys=\"" + key + "\" ");
					schemekeydef.write("href=\"" + value + "\" ");
					schemekeydef.write("source=\"" + currentFile + "\"/>");
					schemekeydef.write("\n");
					schemekeydef.flush();
				} catch (IOException e) {

					javaLogger.logException(e);
				}
			}
			
		}
		
		hrefTargetSet.addAll(reader.getHrefTargets());
		hrefWithIDSet.addAll(reader.getHrefTopicSet());
		chunkTopicSet.addAll(reader.getChunkTopicSet());
		//schemeSet.addAll(reader.getSchemeRefSet());
		conrefTargetSet.addAll(reader.getConrefTargets());
		nonConrefCopytoTargetSet.addAll(reader.getNonConrefCopytoTargets());
		ignoredCopytoSourceSet.addAll(reader.getIgnoredCopytoSourceSet());
		subsidiarySet.addAll(reader.getSubsidiaryTargets());
		outDitaFilesSet.addAll(reader.getOutFilesSet());
		resourceOnlySet.addAll(reader.getResourceOnlySet());
		
		// Generate topic-scheme dictionary
		Set<String> hrfSet = reader.getHrefTargets();
		Set<String> children = null;
		if (reader.getSchemeSet() != null && reader.getSchemeSet().size() > 0) {
			
			children = this.schemeDictionary.get(currentFile);
			if (children == null)
				children = new HashSet<String>();
			children.addAll(reader.getSchemeSet());
			//for Linux support
			currentFile = currentFile.replace(Constants.BACK_SLASH, Constants.SLASH);
			
			this.schemeDictionary.put(currentFile, children);
			Iterator<String> it = hrfSet.iterator();
			while (it.hasNext()) {
				String filename = it.next();
				
				//for Linux support
				filename = filename.replace(Constants.BACK_SLASH, Constants.SLASH);
				
				children = this.schemeDictionary.get(filename);
				if (children == null)
					children = new HashSet<String>();
				children.addAll(reader.getSchemeSet());
				this.schemeDictionary.put(filename, children);
			}
		}
	}

	private void categorizeCurrentFile(String currentFile) {
		String lcasefn = currentFile.toLowerCase();
		
		ditaSet.add(currentFile);
		
		if (FileUtils.isTopicFile(currentFile)) {
			hrefTargetSet.add(currentFile);
		}

		if(reader.hasConaction()){
			conrefpushSet.add(currentFile);
		}
		                        
		if (reader.hasConRef()) {
			conrefSet.add(currentFile);
		}
		
		if(reader.hasKeyRef()){
			keyrefSet.add(currentFile);
		}
		
		if(reader.hasCodeRef()){
			coderefSet.add(currentFile);
		}
		
		if (FileUtils.isDITATopicFile(lcasefn)) {
			fullTopicSet.add(currentFile);
			if (reader.hasHref()) {
				hrefTopicSet.add(currentFile);
			}
		}

		if (FileUtils.isDITAMapFile(lcasefn)) {
			fullMapSet.add(currentFile);
			if (reader.hasHref()) {
				hrefMapSet.add(currentFile);
			}
		}
	}

	private void categorizeResultFile(String file) {
		//edited by william on 2009-08-06 for bug:2832696 start
		String lcasefn = null;
		String format = null;
		//has format attribute set
		if(file.contains(Constants.STICK)){
			//get lower case file name
			lcasefn = file.substring(0, file.indexOf(Constants.STICK)).toLowerCase();
			//get format attribute
			format = file.substring(file.indexOf(Constants.STICK)+1);
			file = file.substring(0, file.indexOf(Constants.STICK));
		}else{
			lcasefn = file.toLowerCase();
		}
		
		//Added by William on 2010-03-04 for bug:2957938 start
		//avoid files referred by coderef being added into wait list
		if(subsidiarySet.contains(lcasefn)){
			return;
		}
		//Added by William on 2010-03-04 for bug:2957938 end
		
		if (FileUtils.isDITAFile(lcasefn)
			&& (format == null ||
			Constants.ATTR_FORMAT_VALUE_DITA.equalsIgnoreCase(format)||
			Constants.ATTR_FORMAT_VALUE_DITAMAP.equalsIgnoreCase(format))) {
			
			addToWaitList(file);
		}else if(!FileUtils.isSupportedImageFile(lcasefn)){
			htmlSet.add(file);
		}
		//edited by william on 2009-08-06 for bug:2832696 end
		if (FileUtils.isSupportedImageFile(lcasefn)) {
			imageSet.add(file);
		}

		if (FileUtils.isHTMLFile(lcasefn)) {
			htmlSet.add(file);
		}
		
		if (FileUtils.isPDFFile(lcasefn)) {
			htmlSet.add(file);
		}
		//Added by William on 2009-10-10 for resources bug:2873560 start
		if (FileUtils.isSWFile(lcasefn)) {
			htmlSet.add(file);
		}
		//Added by William on 2009-10-10 for resources bug:2873560 end
	}

	/*
	 * Update uplevels if needed.
	 * 
	 * @param file
	 */
	private void updateUplevels(String file) {
		
		//Added by william on 2009-08-06 for bug:2832696 start
		if(file.contains(Constants.STICK)){
			file = file.substring(0, file.indexOf(Constants.STICK));
		}
		//Added by william on 2009-08-06 for bug:2832696 end
		
		// for uplevels (../../)
		//modified start by wxzhang 20070518
		//".."-->"../"
		int lastIndex = FileUtils.removeRedundantNames(file).replaceAll(Constants.DOUBLE_BACK_SLASH,
				Constants.SLASH).lastIndexOf("../");
//		modified end by wxzhang 20070518
		if (lastIndex != -1) {
			int newUplevels = lastIndex / 3  + 1;
			uplevels = newUplevels > uplevels ? newUplevels : uplevels;
		}
	}

	/**
	 * Add the given file the wait list if it has not been parsed.
	 * 
	 * @param file
	 */
	private void addToWaitList(String file) {
		if (doneList.contains(file) || waitList.contains(file)) {
			return;
		}

		waitList.add(file);
	}

	private void updateBaseDirectory() {
		baseInputDir = new File(baseInputDir).getAbsolutePath();

		for (int i = uplevels; i > 0; i--) {
			File file = new File(baseInputDir);
			baseInputDir = file.getParent();
			prefix = new StringBuffer(file.getName()).append(File.separator).append(prefix).toString();
		}
	}
	
	private String getUpdateLevels(){
		int current=uplevels;
		StringBuffer buff=new StringBuffer();
		while(current>0){
			buff.append(".."+Constants.FILE_SEPARATOR);
			current--;
		}
		return buff.toString();
	}
	private String formatRelativeValue(String value){
		StringBuffer buff=new StringBuffer();
		if(value==null || value.length()==0)
			return "";
		int index=0;
		//$( )+.[^{\
		while(index<value.length()){
				char current=value.charAt(index);
				switch (current){
					case '.':
						buff.append("\\.");
						break;
					case '\\':
						buff.append("[\\\\|/]");
						break;
					case '(':
						buff.append("\\(");
						break;
					case ')':
						buff.append("\\)");
						break;
					case '[':
						buff.append("\\[");
						break;
					case ']':
						buff.append("\\]");
						break;
					case '{':
						buff.append("\\{");
						break;
					case '}':
						buff.append("\\}");
						break;
					case '^':
						buff.append("\\^");
						break;
					case '+':
						buff.append("\\+");
						break;
					case '$':
						buff.append("\\$");
						break;
					default:
						buff.append(current);
				}
			index++;
		}
		return buff.toString();
	}
	
	private void parseFilterFile() {
		if (ditavalFile != null) {
			DitaValReader ditaValReader = new DitaValReader();
			
			ditaValReader.read(ditavalFile);			
			// Store filter map for later use
			FilterUtils.setFilterMap(ditaValReader.getFilterMap());			
			// Store flagging image used for image copying
			flagImageSet.addAll(ditaValReader.getImageList());
			relFlagImagesSet.addAll(ditaValReader.getRelFlagImageList());
		}else{
			FilterUtils.setFilterMap(null);
		}
	}
	
	private void refactoringResult() {
		handleConref();		
		handleCopyto();		
	}

	private void handleCopyto() {
		Map<String, String> tempMap = new HashMap<String, String>();
		Set<String> pureCopytoSources = new HashSet<String>(Constants.INT_128);
		Set<String> totalCopytoSources = new HashSet<String>(Constants.INT_128);
		
		/*
		 * Validate copy-to map, remove those without valid sources
		 */		
		Iterator<String> iter = copytoMap.keySet().iterator();
		while (iter.hasNext()) {
			String key = (String) iter.next();
			String value = (String) copytoMap.get(key);
			if (new File(baseInputDir + File.separator + prefix , value).exists()) {
				tempMap.put(key, value);
				//Add the copy-to target to conreflist when its source has conref
				if(conrefSet.contains(value)){
					conrefSet.add(key);
				}
			}
		}
		
		copytoMap = tempMap;
		
		/*
		 * Add copy-to targets into ditaSet, fullTopicSet
		 */
		ditaSet.addAll(copytoMap.keySet());
		fullTopicSet.addAll(copytoMap.keySet());
		
		/*
		 * Get pure copy-to sources
		 */
		totalCopytoSources.addAll(copytoMap.values());
		totalCopytoSources.addAll(ignoredCopytoSourceSet);
		iter = totalCopytoSources.iterator();
		while (iter.hasNext()) {
			String src = (String) iter.next();
			if (!nonConrefCopytoTargetSet.contains(src) && !copytoMap.keySet().contains(src)) {
				pureCopytoSources.add(src);
			}
		}
		
		copytoSourceSet = pureCopytoSources;
		
		/*
		 * Remove pure copy-to sources from ditaSet, fullTopicSet
		 */
		ditaSet.removeAll(pureCopytoSources);
		fullTopicSet.removeAll(pureCopytoSources);
	}

	private void handleConref() {
		/*
		 * Get pure conref targets
		 */
		Set<String> pureConrefTargets = new HashSet<String>(Constants.INT_128);
		Iterator<String> iter = conrefTargetSet.iterator();
		while (iter.hasNext()) {
			String target = (String) iter.next();
			if (!nonConrefCopytoTargetSet.contains(target)) {
				pureConrefTargets.add(target);
			}
		}
		conrefTargetSet = pureConrefTargets;
		
		/*
		 * Remove pure conref targets from ditaSet, fullTopicSet
		 */
		ditaSet.removeAll(pureConrefTargets);
		fullTopicSet.removeAll(pureConrefTargets);
	}
	
	private void outputResult() throws DITAOTException {
		Properties prop = new Properties();
		PropertiesWriter writer = new PropertiesWriter();
		Content content = new ContentImpl();
		File outputFile = new File(tempDir, Constants.FILE_NAME_DITA_LIST);
		File xmlDitalist=new File(tempDir, Constants.FILE_NAME_DITA_LIST_XML);
		File dir = new File(tempDir);
		Set<String> copytoSet = new HashSet<String>(Constants.INT_128);
		Set<String> keysDefSet = new HashSet<String>(Constants.INT_128);
		Iterator<Entry<String, String>> iter = null;
		
		if (!dir.exists()) {
			dir.mkdirs();
		}

		prop.put("user.input.dir", baseInputDir);
		prop.put("user.input.file", prefix + inputFile);
		
		prop.put("user.input.file.listfile", "usr.input.file.list");
		File inputfile=new File(tempDir,"usr.input.file.list");
		Writer bufferedWriter = null;
		try {
			bufferedWriter=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(inputfile)));
			bufferedWriter.write(prefix+inputFile);
			bufferedWriter.flush();
		} catch (FileNotFoundException e) {
			javaLogger.logException(e);
		} catch (IOException e) {
			javaLogger.logException(e);
		} finally {
			if (bufferedWriter != null) {
				try {
	                bufferedWriter.close();
                } catch (IOException e) {
                	javaLogger.logException(e);
                }
			}
		}
		
		//add out.dita.files,tempdirToinputmapdir.relative.value to solve the output problem
		relativeValue=prefix;
		formatRelativeValue=formatRelativeValue(relativeValue);
		prop.put("tempdirToinputmapdir.relative.value", formatRelativeValue);
		
		prop.put("uplevels", getUpdateLevels());
		addSetToProperties(prop, Constants.OUT_DITA_FILES_LIST, outDitaFilesSet);
		
		addSetToProperties(prop, Constants.FULL_DITAMAP_TOPIC_LIST, ditaSet);
		addSetToProperties(prop, Constants.FULL_DITA_TOPIC_LIST, fullTopicSet);
		addSetToProperties(prop, Constants.FULL_DITAMAP_LIST, fullMapSet);
		addSetToProperties(prop, Constants.HREF_DITA_TOPIC_LIST, hrefTopicSet);
		addSetToProperties(prop, Constants.CONREF_LIST, conrefSet);
		addSetToProperties(prop, Constants.IMAGE_LIST, imageSet);
		addSetToProperties(prop, Constants.FLAG_IMAGE_LIST, flagImageSet);
		addSetToProperties(prop, Constants.HTML_LIST, htmlSet);
		addSetToProperties(prop, Constants.HREF_TARGET_LIST, hrefTargetSet);
		addSetToProperties(prop, Constants.HREF_TOPIC_LIST, hrefWithIDSet);
		addSetToProperties(prop, Constants.CHUNK_TOPIC_LIST, chunkTopicSet);
		addSetToProperties(prop, Constants.SUBJEC_SCHEME_LIST, schemeSet);
		addSetToProperties(prop, Constants.CONREF_TARGET_LIST, conrefTargetSet);
		addSetToProperties(prop, Constants.COPYTO_SOURCE_LIST, copytoSourceSet);
		addSetToProperties(prop, Constants.SUBSIDIARY_TARGET_LIST, subsidiarySet);
		addSetToProperties(prop, Constants.CONREF_PUSH_LIST, conrefpushSet);
		addSetToProperties(prop, Constants.KEYREF_LIST, keyrefSet);
		addSetToProperties(prop, Constants.CODEREF_LIST, coderefSet);
		
		//@processing-role
		addSetToProperties(prop, Constants.RESOURCE_ONLY_LIST, resourceOnlySet);
		
		addFlagImagesSetToProperties(prop,Constants.REL_FLAGIMAGE_LIST,relFlagImagesSet);
		
		/*
		 * Convert copyto map into set and output
		 */
		iter = copytoMap.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String, String> entry = iter.next();
			copytoSet.add(entry.toString());
		}
		iter = keysDefMap.entrySet().iterator();
		while(iter.hasNext()){
			Map.Entry<String, String> entry = iter.next();
			keysDefSet.add(entry.toString());
		}
		addSetToProperties(prop, Constants.COPYTO_TARGET_TO_SOURCE_MAP_LIST, copytoSet);
		addSetToProperties(prop, Constants.KEY_LIST, keysDefSet);
		content.setValue(prop);
		writer.setContent(content);
		
		writer.write(outputFile.getAbsolutePath());
		writer.writeToXML(xmlDitalist.getAbsolutePath());
		
		// Output relation-graph
		writeMapToXML(reader.getRelationshipGrap(), Constants.FILE_NAME_SUBJECT_RELATION);
		// Output topic-scheme dictionary
		writeMapToXML(this.schemeDictionary, Constants.FILE_NAME_SUBJECT_DICTIONARY);
		
		//added by Willam on 2009-07-17 for req #12014 start
		if(Constants.INDEX_TYPE_ECLIPSEHELP.equals(transtype)){
			// Output plugin id
			File pluginIdFile = new File(tempDir, Constants.FILE_NAME_PLUGIN_XML);
			DelayConrefUtils.getInstance().writeMapToXML(reader.getPluginMap(),pluginIdFile);
			//write the result into the file
			StringBuffer result = reader.getResult();
			try {
				export.write(result.toString());
			} catch (IOException e) {
				
				e.printStackTrace();
			}
		}
		//added by Willam on 2009-07-17 for req #12014 end
		
	}
	
	private void writeMapToXML(Map<String, Set<String>> m, String filename) {
		if (m == null) return;
		Properties prop = new Properties();
		Iterator<Map.Entry<String, Set<String>>> iter = m.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String, Set<String>> entry = iter.next();
			String key = entry.getKey();
			String value = StringUtils.assembleString(entry.getValue(), Constants.COMMA);
			prop.setProperty(key, value);
		}
		File outputFile = new File(tempDir, filename);
		OutputStream os = null;
		try {
			os = new FileOutputStream(outputFile);
			prop.storeToXML(os, null);
			os.close();
		} catch (IOException e) {
			this.javaLogger.logException(e);
		} finally {
			if (os != null) {
    			try {
    				os.close();
    			} catch (Exception e) {
    				javaLogger.logException(e);
    			}
			}
		}
	}

	private void addSetToProperties(Properties prop, String key, Set<String> set) {
		String value = null;
		Set<String> newSet = new LinkedHashSet<String>(Constants.INT_128);
		Iterator<String> iter = set.iterator();

		while (iter.hasNext()) {
			String file = (String) iter.next();			
			if (new File(file).isAbsolute()) {
				// no need to append relative path before absolute paths
				newSet.add(FileUtils.removeRedundantNames(file));
			} else {
				/*
				 * In ant, all the file separator should be slash, so we need to replace
				 * all the back slash with slash.
				 */
				int index=file.indexOf(Constants.EQUAL);
				if(index!=-1){
					//keyname
					String to=file.substring(0,index);
					String source=file.substring(index+1);
					
						//TODO Added by William on 2009-05-14 for keyref bug start
						//When generating key.list
						if(Constants.KEY_LIST.equals(key)){
							
							String repStr = FileUtils.removeRedundantNames(new StringBuffer(prefix).append(to).toString())
							.replaceAll(Constants.DOUBLE_BACK_SLASH,
									Constants.SLASH) + Constants.EQUAL +
									FileUtils.removeRedundantNames(new StringBuffer(prefix).append(source).toString())
							.replaceAll(Constants.DOUBLE_BACK_SLASH,
									Constants.SLASH);
							
							StringBuffer result = new StringBuffer(repStr);
							
							//move the prefix position
							//maps/target_topic_1=topics/target-topic-a.xml(root-map-01.ditamap)-->
							//target_topic_1=topics/target-topic-a.xml(maps/root-map-01.ditamap)
							if(!"".equals(prefix)){
								String prefix1 = prefix.replace("\\", "/");
								if(repStr.indexOf(prefix1)!=-1){
									result = new StringBuffer();
									result.append(repStr.substring(prefix1.length()));
									result.insert(result.lastIndexOf(Constants.LEFT_BRACKET)+1, prefix1);
									//Added by William on 2010-06-08 for bug:3013079 start
									//if this key definition refer to a external resource
									if(exKeyDefMap.containsKey(to)){
										int pos = result.indexOf(prefix1);
										result.delete(pos, pos + prefix1.length());
									}
									//Added by William on 2010-06-08 for bug:3013079 end
									
									newSet.add(result.toString());
								}
							}else{
								//no prefix
								newSet.add(result.toString());
							}
						//TODO Added by William on 2009-05-14 for keyref bug end
							
						//Added by William on 2010-06-10 for bug:3013545 start
						writeKeyDef(to, result);
						//Added by William on 2010-06-10 for bug:3013545 end
							
					}else{
						//other case do nothing
						newSet.add(FileUtils.removeRedundantNames(new StringBuffer(prefix).append(to).toString())
							.replaceAll(Constants.DOUBLE_BACK_SLASH,
									Constants.SLASH) + Constants.EQUAL +
									FileUtils.removeRedundantNames(new StringBuffer(prefix).append(source).toString())
							.replaceAll(Constants.DOUBLE_BACK_SLASH,
									Constants.SLASH));
					}
				}else{
				newSet.add(FileUtils.removeRedundantNames(new StringBuffer(prefix).append(file).toString())
						.replaceAll(Constants.DOUBLE_BACK_SLASH,
								Constants.SLASH));
				}
			}
		}
		
		/*
		 * write filename in the list to a file, in order to use the includesfile attribute in ant script
		 */
		String fileKey=key.substring(0,key.lastIndexOf("list"))+"file";
		prop.put(fileKey, key.substring(0, key.lastIndexOf("list"))+".list");
		File list = new File(tempDir, prop.getProperty(fileKey));
		Writer bufferedWriter = null;
		try {
			bufferedWriter=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(list)));
			Iterator<String> it=newSet.iterator();
			while(it.hasNext()){
				bufferedWriter.write((String)it.next());
				if(it.hasNext())
					bufferedWriter.write("\n");
			}
			bufferedWriter.flush();
			bufferedWriter.close();
		} catch (FileNotFoundException e) {
			javaLogger.logException(e);
		} catch (IOException e) {
			javaLogger.logException(e);
		} finally {
			if (bufferedWriter != null) {
				try {
	                bufferedWriter.close();
                } catch (IOException e) {
	                javaLogger.logException(e);
                }
			}
		}
		
		value = StringUtils.assembleString(newSet, Constants.COMMA);
		prop.put(key, value);
		
		// clear set
		set.clear();
		newSet.clear();
	}
	
	//Added by William on 2010-06-10 for bug:3013545 start
	/** Write keydef into keydef.xml.
	 * @param keyName key name.
	 * @param result keydef.
	 */
	private void writeKeyDef(String keyName, StringBuffer result) {
		try {
			int equalIndex = result.indexOf(Constants.EQUAL);
			int leftBracketIndex = result.lastIndexOf(Constants.LEFT_BRACKET);
			int rightBracketIndex = result.lastIndexOf(Constants.RIGHT_BRACKET);
			//get href
			String href = result.substring(equalIndex + 1, leftBracketIndex);
			//get source file
			String sourcefile = 
				result.substring(leftBracketIndex + 1, rightBracketIndex);
			keydef.write("<keydef ");
			keydef.write("keys=\""+ keyName +"\" ");
			keydef.write("href=\""+ href +"\" ");
			keydef.write("source=\""+ sourcefile +"\"/>");
			keydef.write("\n");
			keydef.flush();
		} catch (IOException e) {

			javaLogger.logException(e);
		}
	}
	//Added by William on 2010-06-10 for bug:3013545 end
	
	/**
	 * add FlagImangesSet to Properties, which needn't to change the dir level, just ouput to the ouput dir.
	 * 
	 * @param Properties 
	 * @param key
	 * @param set
	 */
	private void addFlagImagesSetToProperties(Properties prop, String key, Set<String> set) {
		String value = null;
		Set<String> newSet = new LinkedHashSet<String>(Constants.INT_128);
		Iterator<String> iter = set.iterator();

		while (iter.hasNext()) {
			String file = (String) iter.next();			
			if (new File(file).isAbsolute()) {
				// no need to append relative path before absolute paths
				newSet.add(FileUtils.removeRedundantNames(file));
			} else {
				/*
				 * In ant, all the file separator should be slash, so we need to replace
				 * all the back slash with slash.
				 */
				newSet.add(FileUtils.removeRedundantNames(new StringBuffer().append(file).toString())
						.replaceAll(Constants.DOUBLE_BACK_SLASH,
								Constants.SLASH));
			}
		}

		//write list attribute to file
		String fileKey=key.substring(0,key.lastIndexOf("list"))+"file";
		prop.put(fileKey, key.substring(0, key.lastIndexOf("list"))+".list");
		File list = new File(tempDir, prop.getProperty(fileKey));
		Writer bufferedWriter = null;
		try {
			bufferedWriter=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(list)));
			Iterator<String> it=newSet.iterator();
			while(it.hasNext()){
				bufferedWriter.write((String)it.next());
				if(it.hasNext())
					bufferedWriter.write("\n");
			}
			bufferedWriter.flush();
			bufferedWriter.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (bufferedWriter != null) {
				try {
	                bufferedWriter.close();
                } catch (IOException e) {
	                javaLogger.logException(e);
                }
			}
		}
		

		value = StringUtils.assembleString(newSet, Constants.COMMA);

		prop.put(key, value);

		// clear set
		set.clear();
		newSet.clear();
	}
	
	

}