/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.module;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.DITAOTJavaLogger;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.pipeline.PipelineHashIO;
import org.dita.dost.reader.DitaValReader;
import org.dita.dost.reader.GenListModuleReader;
import org.dita.dost.util.Constants;
import org.dita.dost.util.FileUtils;
import org.dita.dost.util.FilterUtils;
import org.dita.dost.util.StringUtils;
import org.dita.dost.writer.PropertiesWriter;
import org.xml.sax.SAXException;

/**
 * This class extends AbstractPipelineModule, used to generate map and topic
 * list by parsing all the refered dita files.
 * 
 * @version 1.0 2004-11-25
 * 
 * @author Wu, Zhi Qiang
 */
public class GenMapAndTopicListModule extends AbstractPipelineModule {
	/** Set of all dita files */
	private Set ditaSet = null;

	/** Set of all topic files */
	private Set fullTopicSet = null;

	/** Set of all map files */
	private Set fullMapSet = null;

	/** Set of topic files containing href */
	private Set hrefTopicSet = null;

	/** Set of map files containing href */
	private Set hrefMapSet = null;

	/** Set of dita files containing conref */
	private Set conrefSet = null;

	/** Set of all images */
	private Set imageSet = null;
	
	/** Set of all images used for flagging */
	private Set flagImageSet = null;

	/** Set of all html files */
	private Set htmlSet = null;

	/** Set of all the href targets */
	private Set hrefTargetSet = null;

	/** Set of all the conref targets */
	private Set conrefTargetSet = null;
	
	/** Set of all the copy-to sources */
	private Set copytoSourceSet = null;
	
	/** Set of all the non-conref targets */
	private Set nonConrefCopytoTargetSet = null;
	
	/** Set of sources of those copy-to that were ignored */
	private Set ignoredCopytoSourceSet = null;
	
	/** Map of all copy-to (target,source) */
	private Map copytoMap = null;
	
	/** List of files waiting for parsing */
	private List waitList = null;

	/** List of parsed files */
	private List doneList = null;

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

	/**
	 * Create a new instance and do the initialization.
	 * 
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	public GenMapAndTopicListModule() throws SAXException,
			ParserConfigurationException {
		ditaSet = new HashSet(Constants.INT_128);
		fullTopicSet = new HashSet(Constants.INT_128);
		fullMapSet = new HashSet(Constants.INT_128);
		hrefTopicSet = new HashSet(Constants.INT_128);
		hrefMapSet = new HashSet(Constants.INT_128);
		conrefSet = new HashSet(Constants.INT_128);
		imageSet = new HashSet(Constants.INT_128);
		flagImageSet = new HashSet(Constants.INT_128);
		htmlSet = new HashSet(Constants.INT_128);
		hrefTargetSet = new HashSet(Constants.INT_128);
		waitList = new LinkedList();
		doneList = new LinkedList();
		conrefTargetSet = new HashSet(Constants.INT_128);
		nonConrefCopytoTargetSet = new HashSet(Constants.INT_128);
		copytoMap = new HashMap();
		copytoSourceSet = new HashSet(Constants.INT_128);
		ignoredCopytoSourceSet = new HashSet(Constants.INT_128);
	}

	/**
	 * Execute the module.
	 * 
	 * @param input
	 * @return
	 * @throws DITAOTException
	 */
	public AbstractPipelineOutput execute(AbstractPipelineInput input)
			throws DITAOTException {
		try {
			parseInputParameters(input);
			
			GenListModuleReader.initXMLReader(ditaDir);
			
			// first parse filter file for later use
			parseFilterFile();
			
			addToWaitList(inputFile);
			processWaitList();
			updateBaseDirectory();
			refactoringResult();
			outputResult();
		} catch (SAXException e) {
			throw new DITAOTException(e.getMessage(), e);
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

		/*
		 * Resolve relative paths base on the basedir.
		 */
		inFile = new File(ditaInput);
		if (!inFile.isAbsolute()) {
			inFile = new File(basedir, ditaInput);
		}
		if (!new File(tempDir).isAbsolute()) {
			tempDir = new File(basedir, tempDir).getAbsolutePath();
		}
		if (!new File(ditaDir).isAbsolute()) {
			ditaDir = new File(basedir, ditaDir).getAbsolutePath();
		}
		if (ditavalFile != null && !new File(ditavalFile).isAbsolute()) {
			ditavalFile = new File(basedir, ditavalFile).getAbsolutePath();
		}

		baseInputDir = new File(inFile.getAbsolutePath()).getParent();
		inputFile = inFile.getName();
	}
	
	/**
	 * @param baseInputDir
	 * @throws DITAOTException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	private void processWaitList() throws DITAOTException {
		reader = new GenListModuleReader();

		while (!waitList.isEmpty()) {
			processFile((String) waitList.remove(0));
		}
	}

	private void processFile(String currentFile) throws DITAOTException {
		File fileToParse = new File(baseInputDir, currentFile);
		String msg = null;
		Properties params = new Properties();
		params.put("%1", currentFile);		
		
		try {
			reader.setCurrentDir(new File(currentFile).getParent());
			reader.parse(fileToParse);

			// don't put it into dita.list if it is invalid
			if (reader.isValidInput()) {
				processParseResult(currentFile);
				categorizeCurrentFile(currentFile);
			} else if (!currentFile.equals(inputFile)) {
				javaLogger.logWarn(MessageUtils.getMessage("DOTJ021W", params).toString());
			}	
		} catch (Exception e) {
			if (currentFile.equals(inputFile)) {
				// stop the build if exception thrown when parsing input file.
				msg = MessageUtils.getMessage("DOTJ012F", params).toString();
				msg = new StringBuffer(msg).append(Constants.LINE_SEPARATOR)
						.append(e.toString()).toString();				
				throw new DITAOTException(msg, e);
			}

			msg = MessageUtils.getMessage("DOTJ013E", params).toString();
			javaLogger.logError(msg);
			javaLogger.logException(e);
		}
		
		if (!reader.isValidInput() && currentFile.equals(inputFile)) {
			// stop the build if all content in the input file was filtered out.
			msg = MessageUtils.getMessage("DOTJ022F", params).toString();							
			throw new DITAOTException(msg);
		}
		
		doneList.add(currentFile);
		reader.reset();
	}

	/**
	 * @param currentFile
	 */
	private void processParseResult(String currentFile) {
		Iterator iter = reader.getNonCopytoResult().iterator();
		Map cpMap = reader.getCopytoMap();
		
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
				StringBuffer buff = new StringBuffer();
				buff.append("Copy-to task [href=\"");
				buff.append(value);
				buff.append("\" copy-to=\"");
				buff.append(key);
				buff.append("\"] which points to another copy-to target");
				buff.append(" was ignored.");
				javaLogger.logWarn(buff.toString());
				ignoredCopytoSourceSet.add(value);
			} else {
				updateUplevels(key);
				copytoMap.put(key, value);				
			}
		}
		
		hrefTargetSet.addAll(reader.getHrefTargets());
		conrefTargetSet.addAll(reader.getConrefTargets());
		nonConrefCopytoTargetSet.addAll(reader.getNonConrefCopytoTargets());
		ignoredCopytoSourceSet.addAll(reader.getIgnoredCopytoSourceSet());
	}

	private void categorizeCurrentFile(String currentFile) {
		String lcasefn = currentFile.toLowerCase();
		
		ditaSet.add(currentFile);
		
		if (FileUtils.isTopicFile(currentFile)) {
			hrefTargetSet.add(currentFile);
		}

		if (reader.hasConRef()) {
			conrefSet.add(currentFile);
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
		String lcasefn = file.toLowerCase();
		if (FileUtils.isDITAFile(lcasefn)) {
			addToWaitList(file);
		}

		if (FileUtils.isSupportedImageFile(lcasefn)) {
			imageSet.add(file);
		}

		if (FileUtils.isHTMLFile(lcasefn)) {
			htmlSet.add(file);
		}
	}

	/*
	 * Update uplevels if needed.
	 * 
	 * @param file
	 */
	private void updateUplevels(String file) {
		// for uplevels (../../)
		int lastIndex = file.lastIndexOf("..");
		if (lastIndex != -1) {
			int newUplevels = lastIndex / 3 + 1;
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

	private void parseFilterFile() {
		if (ditavalFile != null) {
			DitaValReader ditaValReader = new DitaValReader();
			
			ditaValReader.read(ditavalFile);			
			// Store filter map for later use
			FilterUtils.setFilterMap(ditaValReader.getFilterMap());			
			// Store flagging image used for image copying
			flagImageSet.addAll(ditaValReader.getImageList());
		}
	}
	
	private void refactoringResult() {
		handleConref();		
		handleCopyto();		
	}

	private void handleCopyto() {
		Map tempMap = new HashMap();
		Set pureCopytoSources = new HashSet(Constants.INT_128);
		Set totalCopytoSources = new HashSet(Constants.INT_128);
		
		/*
		 * Validate copy-to map, remove those without valid sources
		 */		
		Iterator iter = copytoMap.keySet().iterator();
		while (iter.hasNext()) {
			String key = (String) iter.next();
			String value = (String) copytoMap.get(key);
			if (new File(baseInputDir, value).exists()) {
				tempMap.put(key, value);
				//Add the copy-to target to conreflist when its source has conref
				if(conrefSet.contains(value))
					conrefSet.add(key);
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
		Set pureConrefTargets = new HashSet(Constants.INT_128);
		Iterator iter = conrefTargetSet.iterator();
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
		File dir = new File(tempDir);
		Set copytoSet = new HashSet(Constants.INT_128);
		Iterator iter = null;
		
		if (!dir.exists()) {
			dir.mkdirs();
		}

		prop.put("user.input.dir", baseInputDir);
		prop.put("user.input.file", prefix + inputFile);

		addSetToProperties(prop, Constants.FULL_DITAMAP_TOPIC_LIST, ditaSet);
		addSetToProperties(prop, Constants.FULL_DITA_TOPIC_LIST, fullTopicSet);
		addSetToProperties(prop, Constants.FULL_DITAMAP_LIST, fullMapSet);
		addSetToProperties(prop, Constants.HREF_DITA_TOPIC_LIST, hrefTopicSet);
		addSetToProperties(prop, Constants.CONREF_LIST, conrefSet);
		addSetToProperties(prop, Constants.IMAGE_LIST, imageSet);
		addSetToProperties(prop, Constants.FLAG_IMAGE_LIST, flagImageSet);
		addSetToProperties(prop, Constants.HTML_LIST, htmlSet);
		addSetToProperties(prop, Constants.HREF_TARGET_LIST, hrefTargetSet);
		addSetToProperties(prop, Constants.CONREF_TARGET_LIST, conrefTargetSet);
		addSetToProperties(prop, Constants.COPYTO_SOURCE_LIST, copytoSourceSet);
		
		/*
		 * Convert copyto map into set and output
		 */
		iter = copytoMap.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			copytoSet.add(entry.toString());
		}
		addSetToProperties(prop, Constants.COPYTO_TARGET_TO_SOURCE_MAP_LIST, copytoSet);
		
		content.setValue(prop);
		writer.setContent(content);
		writer.write(outputFile.getAbsolutePath());
	}

	private void addSetToProperties(Properties prop, String key, Set set) {
		String value = null;
		Set newSet = new HashSet(Constants.INT_128);
		Iterator iter = set.iterator();

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
				newSet.add(FileUtils.removeRedundantNames(new StringBuffer(prefix).append(file).toString())
						.replaceAll(Constants.DOUBLE_BACK_SLASH,
								Constants.SLASH));
			}
		}

		value = StringUtils.assembleString(newSet, Constants.COMMA);

		prop.put(key, value);

		// clear set
		set.clear();
		newSet.clear();
	}

}