/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */
package org.dita.dost.writer;

import static javax.xml.XMLConstants.*;
import static org.dita.dost.util.Configuration.processingMode;
import static org.dita.dost.util.Constants.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.dita.dost.log.MessageUtils;
import org.dita.dost.module.Content;
import org.dita.dost.util.Configuration;
import org.dita.dost.util.FileUtils;
import org.dita.dost.util.StringUtils;
import org.dita.dost.util.URLUtils;
import org.dita.dost.util.Configuration.Mode;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Validation and optional error recovery filter.
 */
public final class ValidationFilter extends AbstractXMLFilter {

	private final Set<String> topicIds = new HashSet<String>();
	private Map<String, Map<String, Set<String>>> validateMap = null;
	private Locator locator;
	
	/**
	 * Create new profiling filter.
	 */
	public ValidationFilter() {
		super();
	}
	
	@Override
	public void setContent(Content content) {
		throw new UnsupportedOperationException();
	}

	// Locator methods
    
	@Override
    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
        getContentHandler().setDocumentLocator(locator);
    }
	
	// SAX methods

	@Override
	public void startElement(final String uri, final String localName, final String qName, final Attributes atts)
			throws SAXException {
		AttributesImpl a = null;
		final String lang = atts.getValue(XML_NS_URI, "lang");
		if (lang != null) {
			final int i = lang.indexOf('_');
			if (i != -1) {
				if (Configuration.processingMode == Configuration.Mode.STRICT) {
					throw new SAXException(MessageUtils.getInstance().getMessage("DOTJ056E", lang).setLocation(locator).toString());
				} else {
					logger.logError(MessageUtils.getInstance().getMessage("DOTJ056E", lang).setLocation(locator).toString());
					if (Configuration.processingMode == Configuration.Mode.LAX) {
						if (a == null) {
							a = new AttributesImpl(atts);
						}
						a.setValue(a.getIndex(XML_NS_URI, "lang"), lang.replace('_', '-'));
					}					
				}
			}
		}
		if (TOPIC_TOPIC.matches(atts) || MAP_MAP.matches(atts)) {
			topicIds.clear();
		} else {
			final String id = atts.getValue(ATTRIBUTE_NAME_ID);
			if (id != null) {
				if (topicIds.contains(id)) {
					if (Configuration.processingMode == Configuration.Mode.STRICT) {
						throw new SAXException(MessageUtils.getInstance().getMessage("DOTJ057E", id).setLocation(locator).toString());
					} else {
						logger.logWarn(MessageUtils.getInstance().getMessage("DOTJ057E", id).setLocation(locator).toString());			
					}
				}
				topicIds.add(id);
			}
		}
		final String href = atts.getValue(ATTRIBUTE_NAME_HREF);
		if (href != null) {
			try {
				new URI(href);
			} catch (URISyntaxException e) {
				switch (processingMode) {
                case STRICT:
                    throw new RuntimeException(MessageUtils.getInstance().getMessage("DOTJ054E", ATTRIBUTE_NAME_HREF, href).setLocation(locator) + ": " + e.getMessage(), e);
                case SKIP:
                    logger.logError(MessageUtils.getInstance().getMessage("DOTJ054E", ATTRIBUTE_NAME_HREF, href).setLocation(locator) + ", using invalid value.");
                    break;
                case LAX:
                    try {
                        final String u = new URI(URLUtils.clean(FileUtils.separatorsToUnix(href))).toASCIIString();
                        if (a == null) {
							a = new AttributesImpl(atts);
						}
                        a.setValue(a.getIndex(ATTRIBUTE_NAME_HREF), u);
                        logger.logError(MessageUtils.getInstance().getMessage("DOTJ054E", ATTRIBUTE_NAME_HREF, href).setLocation(locator) + ", using '" + u + "'.");
                    } catch (final URISyntaxException e1) {
                        logger.logError(MessageUtils.getInstance().getMessage("DOTJ054E", ATTRIBUTE_NAME_HREF, href).setLocation(locator) + ", using invalid value.");
                    }
                    break;
                }
			}
		}
		validateAttributeValues(qName, atts);
		getContentHandler().startElement(uri, localName, qName, a != null ? a : atts);
	}
		
	/**
     * Validate attribute values
     * 
     * @param qName element name
     * @param atts attributes
     */
    private void validateAttributeValues(final String qName, final Attributes atts) {
        if (validateMap == null) {
            return;
        }
        for (int i = 0; i < atts.getLength(); i++) {
            final String attrName = atts.getQName(i);
            final Map<String, Set<String>> valueMap = validateMap.get(attrName);
            if (valueMap != null) {
                Set<String> valueSet = valueMap.get(qName);
                if (valueSet == null) {
                    valueSet = valueMap.get("*");
                }
                if (valueSet != null) {
                    final String attrValue = atts.getValue(i);
                    final String[] keylist = attrValue.trim().split("\\s+");
                    for (final String s : keylist) {
                        // Warning ? Value not valid.
                        if (!StringUtils.isEmptyString(s) && !valueSet.contains(s)) {
                            final Properties prop = new Properties();
                            prop.put("%1", attrName);
                            prop.put("%2", qName);
                            prop.put("%3", attrValue);
                            prop.put("%4", StringUtils.assembleString(valueSet, COMMA));
                            logger.logWarn(MessageUtils.getInstance().getMessage("DOTJ049W", prop).toString());
                        }
                    }
                }
            }
        }
    }

    /**
     * @param validateMap the validateMap to set
     */
    public void setValidateMap(final Map<String, Map<String, Set<String>>> validateMap) {
        this.validateMap = validateMap;
    }
	
}
