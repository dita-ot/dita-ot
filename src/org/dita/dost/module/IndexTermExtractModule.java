/*
 * (c) Copyright IBM Corp. 2005 All Rights Reserved.
 */
package org.dita.dost.module;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * This class extends AbstractPipelineModule, used to extract indexterm from
 * dita/ditamap files.
 * 
 * @version 1.0 2005-04-30
 * 
 * @author Wu, Zhi Qiang
 */
public class IndexTermExtractModule extends AbstractPipelineModule {
	/** The input map */
	private String inputMap = null;

	/** The extension of the target file */
	private String targetExt = null;

	/** The basedir of the input file for parsing */
	private String inputDir = null;

	/** The list of topics */
	private List topicList = null;

	/** The list of ditamap files */
	private List ditamapList = null;

	private DITAOTJavaLogger javaLogger = new DITAOTJavaLogger();

	/**
	 * Create a default instance.
	 */
	public IndexTermExtractModule() {
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.dita.dost.module.AbstractPipelineModule#execute(org.dita.dost.pipeline.AbstractPipelineInput)
	 */
	public AbstractPipelineOutput execute(AbstractPipelineInput input)
			throws DITAOTException {
		try {
			parseAndValidateInput(input);
			extractIndexTerm();
			IndexTermCollection.sort();
			IndexTermCollection.outputTerms();
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
		PipelineHashIO hashIO = (PipelineHashIO) input;
		String ditalist = hashIO
				.getAttribute(Constants.ANT_INVOKER_EXT_PARAM_INPUT);
		String output = hashIO
				.getAttribute(Constants.ANT_INVOKER_EXT_PARAM_OUTPUT);
		String encoding = hashIO
				.getAttribute(Constants.ANT_INVOKER_EXT_PARAM_ENCODING);
		String indextype = hashIO
				.getAttribute(Constants.ANT_INVOKER_EXT_PARAM_INDEXTYPE);
		Properties params = new Properties();

		inputMap = hashIO.getAttribute(Constants.ANT_INVOKER_PARAM_INPUTMAP);
		targetExt = hashIO
				.getAttribute(Constants.ANT_INVOKER_EXT_PARAM_TARGETEXT);

		if (ditalist == null) {
			params.put("%1", "the input dita.list file");
			throw new DITAOTException(MessageUtils.getMessage("DOTJ010E",
					params).toString());
		}

		if (output == null) {
			params.put("%1", "the output file");
			throw new DITAOTException(MessageUtils.getMessage("DOTJ010E",
					params).toString());
		}

		if (targetExt == null) {
			params.put("%1", "the target extension");
			throw new DITAOTException(MessageUtils.getMessage("DOTJ010E",
					params).toString());
		}

		if (indextype == null) {
			params.put("%1", "the index type");
			throw new DITAOTException(MessageUtils.getMessage("DOTJ010E",
					params).toString());
		}

		if (ditalist.indexOf(File.separator) != -1) {
			inputDir = ditalist.substring(0, ditalist
					.lastIndexOf(File.separator) + 1);
		}

		try {
			prop.load(new FileInputStream(ditalist));
		} catch (Exception e) {
			params.put("%1", ditalist);
			String msg = MessageUtils.getMessage("DOTJ011E", params).toString();
			msg = new StringBuffer(msg).append(Constants.LINE_SEPARATOR)
					.append(e.toString()).toString();
			throw new DITAOTException(msg, e);
		}

		/*
		 * Parse topic list and ditamap list from the input dita.list file
		 */
		tokenizer = new StringTokenizer(prop
				.getProperty(Constants.FULL_DITA_TOPIC_LIST), Constants.COMMA);
		topicList = new ArrayList(tokenizer.countTokens());
		while (tokenizer.hasMoreTokens()) {
			topicList.add(tokenizer.nextToken());
		}

		tokenizer = new StringTokenizer(prop
				.getProperty(Constants.FULL_DITAMAP_LIST), Constants.COMMA);
		ditamapList = new ArrayList(tokenizer.countTokens());
		while (tokenizer.hasMoreTokens()) {
			ditamapList.add(tokenizer.nextToken());
		}

		outputRoot = output.lastIndexOf(".") == -1 ? output : output.substring(
				0, output.lastIndexOf("."));
		IndexTermCollection.setOutputFileRoot(outputRoot);

		IndexTermCollection.setIndexType(indextype);

		if (encoding != null && encoding.trim().length() > 0) {
			Locale locale = new Locale(encoding.substring(0, 1), encoding
					.substring(3, 4));
			IndexTerm.setTermLocale(locale);
		}
	}

	private void extractIndexTerm() throws SAXException {
		int topicNum = topicList.size();
		int ditamapNum = ditamapList.size();
		FileInputStream inputStream = null;
		XMLReader xmlReader = null;
		IndexTermReader handler = new IndexTermReader();

		if (System.getProperty(Constants.SAX_DRIVER_PROPERTY) == null) {
			// The default sax driver is set to xerces's sax driver
			System.setProperty(Constants.SAX_DRIVER_PROPERTY,
					Constants.SAX_DRIVER_DEFAULT_CLASS);
		}

		xmlReader = XMLReaderFactory.createXMLReader();

		try {
			xmlReader.setContentHandler(handler);

			for (int i = 0; i < topicNum; i++) {
				handler.reset();
				String target = (String) topicList.get(i);
				String targetPathFromMap = FileUtils.getRelativePathFromMap(
						inputMap, target);
				String targetPathFromMapWithoutExt = targetPathFromMap
						.substring(0, targetPathFromMap.lastIndexOf("."));
				handler.setTargetFile(new StringBuffer(
						targetPathFromMapWithoutExt).append(targetExt)
						.toString());
				try {
					inputStream = new FileInputStream(
							new File(inputDir, target));
					xmlReader.parse(new InputSource(inputStream));
					inputStream.close();
				} catch (Exception e) {					
					Properties params = new Properties();
					params.put("%1", target);
					String msg = MessageUtils.getMessage("DOTJ013E", params).toString();
					javaLogger.logError(msg);
					javaLogger.logException(e);
				}
			}

			DitamapIndexTermReader ditamapIndexTermReader = new DitamapIndexTermReader();
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
					inputStream = new FileInputStream(new File(inputDir,
							ditamap));
					xmlReader.parse(new InputSource(inputStream));
					inputStream.close();
				} catch (Exception e) {
					Properties params = new Properties();
					params.put("%1", ditamap);
					String msg = MessageUtils.getMessage("DOTJ013E", params).toString();
					javaLogger.logError(msg);
					javaLogger.logException(e);
				}
			}
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
				}

			}
		}
	}

}
