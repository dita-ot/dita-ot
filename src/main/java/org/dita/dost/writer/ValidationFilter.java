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
import java.util.Set;

import org.dita.dost.log.MessageUtils;
import org.dita.dost.module.Content;
import org.dita.dost.util.Configuration;
import org.dita.dost.util.FileUtils;
import org.dita.dost.util.StringUtils;
import org.dita.dost.util.URLUtils;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Validation and optional error recovery filter.
 */
public final class ValidationFilter extends AbstractXMLFilter {

    private final MessageUtils messageUtils = MessageUtils.getInstance();
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
	public void setContent(final Content content) {
		throw new UnsupportedOperationException();
	}

    /**
     * Set valid attribute values.
     * 
     * <p>The contents of the map is in pseudo-code
     * {@code Map<AttName, Map<ElemName, <Set<Value>>>}.
     * For default element mapping, the value is {@code *}.
     */
    public void setValidateMap(final Map<String, Map<String, Set<String>>> validateMap) {
        this.validateMap = validateMap;
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
		AttributesImpl modified = null;
		modified = validateLang(atts, modified);
		validateId(atts);
		modified = validateHref(atts, modified);
		validateKeys(atts);
		validateAttributeValues(qName, atts);
		getContentHandler().startElement(uri, localName, qName, modified != null ? modified : atts);
	}

	/**
	 * Validate xml:lang attribute.
	 * 
	 * @return modified attributes, {@code null} if there have been no changes 
	 */
    private AttributesImpl validateLang(final Attributes atts, final AttributesImpl modified) throws SAXException {
        AttributesImpl res = modified;
        final String lang = atts.getValue(XML_NS_URI, "lang");
		if (lang != null) {
			final int i = lang.indexOf('_');
			if (i != -1) {
				if (Configuration.processingMode == Configuration.Mode.STRICT) {
					throw new SAXException(messageUtils.getMessage("DOTJ056E", lang).setLocation(locator).toString());
				}
				logger.logError(messageUtils.getMessage("DOTJ056E", lang).setLocation(locator).toString());
				if (Configuration.processingMode == Configuration.Mode.LAX) {
					if (res == null) {
						res = new AttributesImpl(atts);
					}
					res.setValue(res.getIndex(XML_NS_URI, "lang"), lang.replace('_', '-'));
				}					
			}
		}
        return res;
    }

	/**
	 * Validate ID attribute.
	 */
    private void validateId(final Attributes atts) throws SAXException {
        final String cls = atts.getValue(ATTRIBUTE_NAME_CLASS);
        if (TOPIC_TOPIC.matches(cls) || MAP_MAP.matches(cls)) {
			topicIds.clear();
        } else if (TOPIC_RESOURCEID.matches(cls)) {
            // not considered a normal element ID
        } else {
			final String id = atts.getValue(ATTRIBUTE_NAME_ID);
			if (id != null) {
				if (topicIds.contains(id)) {
					if (Configuration.processingMode == Configuration.Mode.STRICT) {
						throw new SAXException(messageUtils.getMessage("DOTJ057E", id).setLocation(locator).toString());
					} else {
						logger.logWarn(messageUtils.getMessage("DOTJ057E", id).setLocation(locator).toString());			
					}
				}
				topicIds.add(id);
			}
		}
    }

	/**
	 * Validate href attribute.
	 * 
	 * @return modified attributes, {@code null} if there have been no changes
	 */
    private AttributesImpl validateHref(final Attributes atts, final AttributesImpl modified) {
        AttributesImpl res = modified;
        final String href = atts.getValue(ATTRIBUTE_NAME_HREF);
		if (href != null) {
			try {
				new URI(href);
			} catch (final URISyntaxException e) {
				switch (processingMode) {
                case STRICT:
                    throw new RuntimeException(messageUtils.getMessage("DOTJ054E", ATTRIBUTE_NAME_HREF, href).setLocation(locator) + ": " + e.getMessage(), e);
                case SKIP:
                    logger.logError(messageUtils.getMessage("DOTJ054E", ATTRIBUTE_NAME_HREF, href).setLocation(locator) + ", using invalid value.");
                    break;
                case LAX:
                    try {
                        final String u = new URI(URLUtils.clean(FileUtils.separatorsToUnix(href))).toASCIIString();
                        if (res == null) {
							res = new AttributesImpl(atts);
						}
                        res.setValue(res.getIndex(ATTRIBUTE_NAME_HREF), u);
                        logger.logError(messageUtils.getMessage("DOTJ054E", ATTRIBUTE_NAME_HREF, href).setLocation(locator) + ", using '" + u + "'.");
                    } catch (final URISyntaxException e1) {
                        logger.logError(messageUtils.getMessage("DOTJ054E", ATTRIBUTE_NAME_HREF, href).setLocation(locator) + ", using invalid value.");
                    }
                    break;
                }
			}
		}
		return res;
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
                        if (!StringUtils.isEmptyString(s) && !valueSet.contains(s)) {
                            logger.logWarn(messageUtils.getMessage("DOTJ049W", attrName, qName, attrValue, StringUtils.assembleString(valueSet, COMMA)).toString());
                        }
                    }
                }
            }
        }
    }

    /**
     * Validate keys attribute
     */
    private void validateKeys(final Attributes atts) {
        final String keys = atts.getValue(ATTRIBUTE_NAME_KEYS);
        if (keys != null) {
            for (final String key : keys.split(" ")) {
                if (!isValidKeyName(key)) {
                    logger.logError(messageUtils.getMessage("DOTJ055E", key).toString());
                }
            }
        }
    }
    
    private boolean isValidKeyName(final String key) {
        for (final char c : key.toCharArray()) {
            switch (c) {
            // disallowed characters
            case '{':
            case '}':
            case '[':
            case ']':
            case '/':
            case '#':
            case '?':
                return false;
            // URI characters
            case '-':
            case '.':
            case '_':
            case '~':
            case ':':
            case '@':
            case '!':
            case '$':
            case '&':
            case '\'':
            case '(':
            case ')':
            case '*':
            case '+':
            case ',':
            case ';':
            case '=':
                break;
            default:
                if (!((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9'))) {
                    return false;
                }
                break;
            }
        }
        return true;
    }
	
}
