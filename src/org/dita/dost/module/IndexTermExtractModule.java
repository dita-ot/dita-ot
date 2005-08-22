/*
 * (c) Copyright IBM Corp. 2005 All Rights Reserved.
 */
package org.dita.dost.module;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.StringTokenizer;

import org.dita.dost.index.IndexTerm;
import org.dita.dost.index.IndexTermCollection;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.pipeline.PipelineHashIO;
import org.dita.dost.reader.DitamapIndexTermReader;
import org.dita.dost.reader.IndexTermReader;
import org.dita.dost.util.Constants;
import org.dita.dost.util.StringUtils;
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

	/** The list of href targets */
	private List hrefTargetList = null;

	/** The list of ditamap files */
	private List ditamapList = null;

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
	public AbstractPipelineOutput execute(AbstractPipelineInput input) {
		try {
			parseAndValidateInput(input);
			extractIndexTerm();
			IndexTermCollection.sort();
			IndexTermCollection.outputTerms();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}

		return null;
	}

	private void parseAndValidateInput(AbstractPipelineInput input)
			throws FileNotFoundException, IOException {
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

		inputMap = hashIO.getAttribute(Constants.ANT_INVOKER_PARAM_INPUTMAP);
		targetExt = hashIO
				.getAttribute(Constants.ANT_INVOKER_EXT_PARAM_TARGETEXT);

		if (ditalist == null) {
			throw new RuntimeException(
					"Please specify the input dita.list file.");
		}

		if (output == null) {
			throw new RuntimeException("Please specify the output file.");
		}

		if (targetExt == null) {
			throw new RuntimeException("Please specify the target extension.");
		}

		if (indextype == null) {
			throw new RuntimeException("Please specify the index type.");
		}

		if (ditalist.indexOf(File.separator) != -1) {
			inputDir = ditalist.substring(0, ditalist
					.lastIndexOf(File.separator) + 1);
		}

		prop.load(new FileInputStream(ditalist));

		/*
		 * Parse href targets and ditamap list from the input dita.list file
		 */
		tokenizer = new StringTokenizer(prop
				.getProperty(Constants.HREF_TARGET_LIST), Constants.COMMA);
		hrefTargetList = new ArrayList(tokenizer.countTokens());
		while (tokenizer.hasMoreTokens()) {
			hrefTargetList.add(tokenizer.nextToken());
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

		if (encoding != null) {
			Locale locale = new Locale(encoding.substring(0, 1), encoding
					.substring(3, 4));
			IndexTerm.setTermLocale(locale);
		}
	}

	private void extractIndexTerm() throws IOException, SAXException {
		int hrefTargetNum = hrefTargetList.size();
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

			for (int i = 0; i < hrefTargetNum; i++) {
				String target = (String) hrefTargetList.get(i);
				String targetPathFromMap = StringUtils.getRelativePathFromMap(
						inputMap, target);
				String targetPathFromMapWithoutExt = targetPathFromMap
						.substring(0, targetPathFromMap.lastIndexOf("."));
				handler.setTargetFile(new StringBuffer(
						targetPathFromMapWithoutExt).append(targetExt)
						.toString());
				inputStream = new FileInputStream(new File(inputDir, target));
				xmlReader.parse(new InputSource(inputStream));
				inputStream.close();
			}

			DitamapIndexTermReader ditamapIndexTermReader = new DitamapIndexTermReader();
			xmlReader.setContentHandler(ditamapIndexTermReader);

			for (int j = 0; j < ditamapNum; j++) {
				String ditamap = (String) ditamapList.get(j);
				String currentMapPathName = StringUtils.getRelativePathFromMap(
						inputMap, ditamap);
				String mapPathFromInputMap = "";

				if (currentMapPathName.lastIndexOf(Constants.SLASH) != -1) {
					mapPathFromInputMap = currentMapPathName.substring(0,
							currentMapPathName.lastIndexOf(Constants.SLASH));
				}

				ditamapIndexTermReader.setMapPath(mapPathFromInputMap);
				inputStream = new FileInputStream(new File(inputDir, ditamap));
				xmlReader.parse(new InputSource(inputStream));
				inputStream.close();
			}
		} finally {
			if (inputStream != null) {
				inputStream.close();
			}
		}
	}

}
