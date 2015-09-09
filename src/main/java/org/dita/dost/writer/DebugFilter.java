/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */
package org.dita.dost.writer;

import static org.dita.dost.util.Constants.*;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.dita.dost.util.XMLUtils;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Add debug attributes.
 * 
 * <p>The following attributes are added to elements:</p>
 * <dl>
 *   <dt>{@link org.dita.dost.util.Constants#ATTRIBUTE_NAME_XTRF xtrf}</dt>
 *   <dd>Absolute system path of the source file.</dd>
 *   <dt>{@link org.dita.dost.util.Constants#ATTRIBUTE_NAME_XTRC xtrc}</dt>
 *   <dd>Element location in the document, {@code element-name ":" element-count ";" row-number ":" colum-number}.</dd>
 * </dl>
 */
public final class DebugFilter extends AbstractXMLFilter {

    private String inputFile;
	private Locator locator;
	private final Map<String, Integer> counterMap = new HashMap<>();
	private int foreignLevel;
	
	/**
	 * Set input file.
	 * 
	 * @param inputFile absolute path to input file
	 */
	public void setInputFile(final File inputFile) {
	    this.inputFile = inputFile.getAbsoluteFile().toURI().toString();
	}

	/**
     * Set input file.
     * 
     * @param inputFile absolute URI to input file
     */
    public void setInputFile(final URI inputFile) {
        this.inputFile = inputFile.toString();
    }
	
	// Locator methods
    
	@Override
    public void setDocumentLocator(final Locator locator) {
        this.locator = locator;
        getContentHandler().setDocumentLocator(locator);
    }
	
	// SAX methods
	
	@Override
	public void startElement(final String uri, final String localName, final String qName, final Attributes atts)
			throws SAXException {
	    if (foreignLevel > 0) {
            foreignLevel++;
        } else if (foreignLevel == 0) {
            final String classValue = atts.getValue(ATTRIBUTE_NAME_CLASS);
            if (TOPIC_FOREIGN.matches(classValue) || TOPIC_UNKNOWN.matches(classValue)) {
                foreignLevel = 1;
            }
        }
	    
		final AttributesImpl res = new AttributesImpl(atts);
		if (foreignLevel <= 1){
    		XMLUtils.addOrSetAttribute(res, ATTRIBUTE_NAME_XTRF, inputFile);
    		
            Integer nextValue;
            if (counterMap.containsKey(qName)) {
                final Integer value = counterMap.get(qName);
                nextValue = value + 1;
            } else {
                nextValue = 1;
            }
            counterMap.put(qName, nextValue);
            final StringBuilder xtrc = new StringBuilder(qName).append(COLON).append(nextValue.toString());
            if (locator != null) {                                
                xtrc.append(';')
                    .append(Integer.toString(locator.getLineNumber()))
                    .append(COLON)
                    .append(Integer.toString(locator.getColumnNumber()));
            }
            XMLUtils.addOrSetAttribute(res, ATTRIBUTE_NAME_XTRC, xtrc.toString());
		}
	    super.startElement(uri, localName, qName, res);
	}

	@Override
    public void endElement(final String uri, final String localName, final String qName)
            throws SAXException {
        if (foreignLevel > 0){
            foreignLevel--;
        }
        super.endElement(uri, localName, qName);
	}
	
}
