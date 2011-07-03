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
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.module.Content;
import org.dita.dost.util.FileUtils;

/**
 * Coderef element resolver filter.
 */
public final class CoderefResolver extends AbstractXMLFilter {
	
    // Variables ---------------------------------------------------------------
    
	private File currentFile = null;
	private int ignoreDepth = 0;
	
	// Constructors ------------------------------------------------------------
	
	/**
	 * Constructor.
	 */
	public CoderefResolver() {
	}
	
	// AbstractWriter methods --------------------------------------------------
	
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
        currentFile = new File(filename);
        super.write(filename);
    }
	
	// XMLFilter methods -------------------------------------------------------

	@Override
	public void startElement(final String uri, final String localName, final String name,
			final Attributes atts) throws SAXException {
		if (ignoreDepth > 0) {
		    ignoreDepth++;
		    return;
		}
	    
		if (PR_D_CODEREF.matches(atts)) {
		    ignoreDepth++;
		    try{
		        final String hrefValue = atts.getValue(ATTRIBUTE_NAME_HREF);
    			if (hrefValue != null){
    				final String codeFile = FileUtils.normalizeDirectory(currentFile.getParentFile().getAbsolutePath(), hrefValue);
    				if (new File(codeFile).exists()){
    					FileReader codeReader = null;
    					try {
    						codeReader = new FileReader(new File(codeFile));
    						final char[] buffer = new char[INT_1024 * INT_4];
    						int len;
    						while ((len = codeReader.read(buffer)) != -1) {
    						    super.characters(buffer, 0, len);
    						}
    					} catch (final Exception e) {
    					    logger.logException(new Exception("Failed to process code reference " + codeFile));
    					} finally {
    						if (codeReader != null) {
    							try {
    								codeReader.close();
    							} catch (final IOException e) {
    								logger.logException(e);
    							}
    						}
    					}
    				} else {
                        final Properties prop = new Properties();
                        prop.put("%1", hrefValue);
                        prop.put("%2", atts.getValue(ATTRIBUTE_NAME_XTRF));
                        prop.put("%3", atts.getValue(ATTRIBUTE_NAME_XTRC));
                        logger.logWarn(MessageUtils.getMessage("DOTJ051E",prop).toString());
    				}
    			} else {
    			    //logger.logDebug("Code reference target not defined");
    			}
            } catch (final Exception e) {
                logger.logException(e);
            }
		} else {
		    super.startElement(uri, localName, name, atts);
		}
	}

	@Override
	public void endElement(final String uri, final String localName, final String name)
			throws SAXException {
	    if (ignoreDepth > 0) {
            ignoreDepth--;
            return;
        }

	    super.endElement(uri, localName, name);
	}
	
}
