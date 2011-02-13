/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2005, 2006 All Rights Reserved.
 */
package org.dita.dost.module;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.index.IndexTerm;
import org.dita.dost.index.IndexTermCollection;
import org.dita.dost.log.DITAOTJavaLogger;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.pipeline.PipelineHashIO;
import org.dita.dost.reader.DitamapIndexTermReader;
import org.dita.dost.reader.IndexTermReader;
import org.dita.dost.util.Constants;
import org.dita.dost.util.FileUtils;
import org.dita.dost.util.StringUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * This class extends AbstractPipelineModule, used to extract indexterm from
 * dita/ditamap files.
 * 
 * @version 1.0 2005-04-30
 * 
 * @author Wu, Zhi Qiang
 */
public class IndexTermExtractModule implements AbstractPipelineModule {
	/** The input map */
	private String inputMap = null;

	/** The extension of the target file */
	private String targetExt = null;

	/** The basedir of the input file for parsing */
	private String baseInputDir = null;

	/** The list of topics */
	private List<String> topicList = null;

	/** The list of ditamap files */
	private List<String> ditamapList = null;

	private DITAOTJavaLogger javaLogger = new DITAOTJavaLogger();
	private IndexTermCollection indexTermCollection;

	/**
	 * Create a default instance.
	 */
	public IndexTermExtractModule() {
	}

	/**
	 * 
	 * @see org.dita.dost.module.AbstractPipelineModule#execute(org.dita.dost.pipeline.AbstractPipelineInput)
	 */
	public AbstractPipelineOutput execute(AbstractPipelineInput input)
			throws DITAOTException {
	    indexTermCollection = IndexTermCollection.getInstantce(); 
		try {
			indexTermCollection.clear();
			parseAndValidateInput(input);
			extractIndexTerm();
			indexTermCollection.sort();
			indexTermCollection.outputTerms();
		} catch (Exception e) {
			javaLogger.logException(e);
		}

		return null;
	}

	private void parseAndValidateInput(AbstractPipelineInput input)
			throws DITAOTException {
		StringTokenizer tokenizer = null;
		Properties prop = new Properties();
		String outputRoot = null;
		int lastIndexOfDot;
		String ditalist;
		String resource_only_list;
		Properties params = new Properties();
		PipelineHashIO hashIO = (PipelineHashIO) input;
		
		String baseDir = hashIO
				.getAttribute(Constants.ANT_INVOKER_PARAM_BASEDIR);
		String tempDir = ((PipelineHashIO)input).getAttribute(Constants.ANT_INVOKER_PARAM_TEMPDIR);
		String output = hashIO
				.getAttribute(Constants.ANT_INVOKER_EXT_PARAM_OUTPUT);
		String encoding = hashIO
				.getAttribute(Constants.ANT_INVOKER_EXT_PARAM_ENCODING);
		String indextype = hashIO
				.getAttribute(Constants.ANT_INVOKER_EXT_PARAM_INDEXTYPE);
		
		String indexclass = hashIO
				.getAttribute(Constants.ANT_INVOKER_EXT_PARAM_INDEXCLASS);
		
		inputMap = hashIO.getAttribute(Constants.ANT_INVOKER_PARAM_INPUTMAP);
		targetExt = hashIO
				.getAttribute(Constants.ANT_INVOKER_EXT_PARAM_TARGETEXT);
		

		if (!new File(tempDir).isAbsolute()) {
        	tempDir = new File(baseDir, tempDir).getAbsolutePath();
        }
		
		if (!new File(output).isAbsolute()) {
			output = new File(baseDir, output).getAbsolutePath();
		}
		
		baseInputDir = tempDir;		
		ditalist = new File(tempDir, "dita.list").getAbsolutePath();
		InputStream in = null;
		try {
			in = new FileInputStream(ditalist);
			prop.load(in);
		} catch (Exception e) {
			String msg = null;
			params.put("%1", ditalist);
			msg = MessageUtils.getMessage("DOTJ011E", params).toString();
			msg = new StringBuffer(msg).append(Constants.LINE_SEPARATOR)
					.append(e.toString()).toString();
			throw new DITAOTException(msg, e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					javaLogger.logException(e);
				}
			}
		}

		/*
		 * Parse topic list and ditamap list from the input dita.list file
		 */
		tokenizer = new StringTokenizer(prop
				.getProperty(Constants.FULL_DITA_TOPIC_LIST), Constants.COMMA);
		resource_only_list = prop.getProperty(Constants.RESOURCE_ONLY_LIST, "");
		topicList = new ArrayList<String>(tokenizer.countTokens());
		while (tokenizer.hasMoreTokens()) {
			String t = tokenizer.nextToken();
			if (!resource_only_list.contains(t))
				topicList.add(t);
		}

		tokenizer = new StringTokenizer(prop
				.getProperty(Constants.FULL_DITAMAP_LIST), Constants.COMMA);
		ditamapList = new ArrayList<String>(tokenizer.countTokens());
		while (tokenizer.hasMoreTokens()) {
			String t = tokenizer.nextToken();
			if (!resource_only_list.contains(t))
				ditamapList.add(t);
		}
		
		lastIndexOfDot = output.lastIndexOf(".");
		outputRoot = (lastIndexOfDot == -1) ? output : output.substring(0,
				lastIndexOfDot);

		indexTermCollection.setOutputFileRoot(outputRoot);
		indexTermCollection.setIndexType(indextype);
		indexTermCollection.setIndexClass(indexclass);
		//RFE 2987769 Eclipse index-see 
		indexTermCollection.setPipelineHashIO(hashIO);

		if (encoding != null && encoding.trim().length() > 0) {
			IndexTerm.setTermLocale(StringUtils.getLocale(encoding));
		}
	}

	private void extractIndexTerm() throws SAXException {
		int topicNum = topicList.size();
		int ditamapNum = ditamapList.size();
		FileInputStream inputStream = null;
		XMLReader xmlReader = null;
		IndexTermReader handler = new IndexTermReader(indexTermCollection);
		DitamapIndexTermReader ditamapIndexTermReader = new DitamapIndexTermReader(indexTermCollection, true);

		xmlReader = StringUtils.getXMLReader();

		try {
			xmlReader.setContentHandler(handler);

			for (int i = 0; i < topicNum; i++) {
				String target;
				String targetPathFromMap;
				String targetPathFromMapWithoutExt;
				handler.reset();
				target = (String) topicList.get(i);
				targetPathFromMap = FileUtils.getRelativePathFromMap(
						inputMap, target);
				targetPathFromMapWithoutExt = targetPathFromMap
						.substring(0, targetPathFromMap.lastIndexOf("."));
				handler.setTargetFile(new StringBuffer(
						targetPathFromMapWithoutExt).append(targetExt)
						.toString());
				
				try {
					//removed by Alan on Date:2009-11-02 for Work Item:#1590 start
					/*if(!new File(baseInputDir, target).exists()){
						javaLogger.logWarn("Cannot find file "+ target);
						continue;
					}*/
					//removed by Alan on Date:2009-11-02 for Work Item:#1590 end
					inputStream = new FileInputStream(
							new File(baseInputDir, target));
					xmlReader.parse(new InputSource(inputStream));
					inputStream.close();
				} catch (Exception e) {					
					Properties params = new Properties();
					StringBuffer buff=new StringBuffer();
					String msg = null;
					params.put("%1", target);
					msg = MessageUtils.getMessage("DOTJ013E", params).toString();
					javaLogger.logError(buff.append(msg).append(e.getMessage()).toString());
				}
			}

			xmlReader.setContentHandler(ditamapIndexTermReader);

			for (int j = 0; j < ditamapNum; j++) {
				String ditamap = (String) ditamapList.get(j);
				String currentMapPathName = FileUtils.getRelativePathFromMap(
						inputMap, ditamap);
				String mapPathFromInputMap = "";

				if (currentMapPathName.lastIndexOf(Constants.SLASH) != -1) {
					mapPathFromInputMap = currentMapPathName.substring(0,
							currentMapPathName.lastIndexOf(Constants.SLASH));
				}

				ditamapIndexTermReader.setMapPath(mapPathFromInputMap);
				try {
					//removed by Alan on Date:2009-11-02 for Work Item:#1590 start
					/*if(!new File(baseInputDir, ditamap).exists()){
						javaLogger.logWarn("Cannot find file "+ ditamap);
						continue;
					}*/
					//end by Alan on Date:2009-11-02 for Work Item:#1590 start
					inputStream = new FileInputStream(new File(baseInputDir,
							ditamap));
					xmlReader.parse(new InputSource(inputStream));
					inputStream.close();
				} 	catch (Exception e) {
					Properties params = new Properties();
					String msg = null;
					params.put("%1", ditamap);
					msg = MessageUtils.getMessage("DOTJ013E", params).toString();
					javaLogger.logError(msg);
					javaLogger.logException(e);
				}
			}
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					javaLogger.logException(e);
				}

			}
		}
	}

}
