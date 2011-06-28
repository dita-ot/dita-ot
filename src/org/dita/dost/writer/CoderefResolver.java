/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2010 All Rights Reserved.
 */
package org.dita.dost.writer;

import static org.dita.dost.util.Constants.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.exception.DITAOTXMLErrorHandler;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.module.Content;
import org.dita.dost.util.FileUtils;
import org.dita.dost.util.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
/**
 * CoderefResolver class, resolving 
 * coderef attribute in topic file.
 *
 */
public final class CoderefResolver extends AbstractXMLWriter {
	
	private OutputStreamWriter output = null;
	
	private XMLReader reader = null;
		
	private File currentFile = null;
	
	private final Set<String> coderefSpec;
	/**
	 * Constructor.
	 */
	public CoderefResolver() {
		coderefSpec = new HashSet<String>();
		try {
            reader = StringUtils.getXMLReader();
            reader.setContentHandler(this);
            reader.setProperty(LEXICAL_HANDLER_PROPERTY,this);
            reader.setFeature(FEATURE_NAMESPACE_PREFIX, true);
        } catch (final Exception e) {
        	logger.logException(e);
        }

	}
	@Override
	public void setContent(final Content content) {
	    // NOOP
	}
	
	@Override
	public void write(final String filename) throws DITAOTException {
        // ignore in-exists file
        if (filename == null || !new File(filename).exists()) {
            return;
        }

	    FileOutputStream fileOutput = null;
        try {                     
            final File inputFile = new File(filename);
            currentFile = inputFile;
            final File outputFile = new File(filename + FILE_EXTENSION_TEMP);
            fileOutput = new FileOutputStream(outputFile);
            output = new OutputStreamWriter(fileOutput, UTF8);
            reader.setErrorHandler(new DITAOTXMLErrorHandler(filename));
            reader.parse(filename);
            output.flush();
            output.close();
            
            if(!inputFile.delete()){
            	final Properties prop = new Properties();
            	prop.put("%1", inputFile.getPath());
            	prop.put("%2", outputFile.getPath());
            	logger.logError(MessageUtils.getMessage("DOTJ009E", prop).toString());
            }
            if(!outputFile.renameTo(inputFile)){
            	final Properties prop = new Properties();
            	prop.put("%1", inputFile.getPath());
            	prop.put("%2", outputFile.getPath());
            	logger.logError(MessageUtils.getMessage("DOTJ009E", prop).toString());
            }
        } catch (final Exception e) {
        	logger.logException(e);
        }finally {
            try {
            	if (fileOutput != null) {
            		fileOutput.close();
            	}
            }catch (final Exception e) {
				logger.logException(e);
            }
        }
	}

	@Override
	public void characters(final char[] ch, final int start, final int length)
			throws SAXException {
		try {
            output.write(StringUtils.escapeXML(ch, start, length));
        } catch (final Exception e) {
        	logger.logException(e);
        }
	}

	@Override
	public void ignorableWhitespace(final char[] ch, final int start, final int length)
			throws SAXException {
		try {
            output.write(ch, start, length);
        } catch (final Exception e) {
        	logger.logException(e);
        }
	}

	@Override
	public void processingInstruction(final String target, final String data)
			throws SAXException {
		try {
        	super.processingInstruction(target, data);
        	final String pi = (data != null) ? target + STRING_BLANK + data : target;
            output.write(LESS_THAN + QUESTION 
                    + pi + QUESTION + GREATER_THAN);
        } catch (final Exception e) {
        	logger.logException(e);
        }
	}

	@Override
	public void startElement(final String uri, final String localName, final String name,
			final Attributes atts) throws SAXException {
		final String classValue = atts.getValue(ATTRIBUTE_NAME_CLASS);
		final String hrefValue = atts.getValue(ATTRIBUTE_NAME_HREF);
		try{
			if (classValue != null
					&& classValue.contains(ATTR_CLASS_VALUE_CODEREF)){
				//TODO resolve coderef and pull in program content
				coderefSpec.add(name);
				if (hrefValue != null){
					final String codeFile = FileUtils.normalizeDirectory(
							currentFile.getParentFile().getAbsolutePath(), hrefValue);
					if (new File(codeFile).exists()){
						FileReader codeReader = null;
						try {
							codeReader = new FileReader(new File(codeFile));
    						final char[] buffer = new char[INT_1024 * INT_4];
    						int len;
    						while((len = codeReader.read(buffer)) != -1){
    							output.write(StringUtils.escapeXML(buffer, 0, len));
    						}
						} finally {
							if (codeReader != null) {
								try {
									codeReader.close();
								} catch (final IOException e) {
									logger.logException(e);
								}
							}
						}
					}else{
						//report error of href target is not valid
					}
				}else{
					//report error of href attribute is null
				}
			}else{
				output.write(LESS_THAN + name);
				for (int i=0; i<atts.getLength(); i++){
					output.write(STRING_BLANK + atts.getQName(i)
							+ EQUAL + QUOTATION
							+ atts.getValue(i) + QUOTATION);
				}
				output.write(GREATER_THAN);
			}
		}catch (final Exception e){
			logger.logException(e);
		}
	}

	@Override
	public void endElement(final String uri, final String localName, final String name)
			throws SAXException {
		try{
			if(!coderefSpec.contains(name)){
				output.write(LESS_THAN + SLASH 
						+ name + GREATER_THAN);
			}
		}catch (final Exception e){
			logger.logException(e);
		}		
	}
}
