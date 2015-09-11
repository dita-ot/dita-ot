/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */
package org.dita.dost.writer;

import static javax.xml.XMLConstants.*;
import static org.dita.dost.util.Configuration.Mode;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.URLUtils.toURI;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import org.dita.dost.log.MessageUtils;
import org.dita.dost.util.*;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.LocatorImpl;

/**
 * Validation and optional error recovery filter.
 */
public final class ValidationFilter extends AbstractXMLFilter {

    private final MessageUtils messageUtils = MessageUtils.getInstance();
	private final Set<String> topicIds = new HashSet<>();
	private Map<String, Map<String, Set<String>>> validateMap = null;
	private Locator locator;
    /** Deque of domains attibute values */
	private final Deque<String[][]> domains = new LinkedList<>();
    /** Absolute URI to current file */
    private URI currentFile;
    private Job job;
//    /** Number of cols in tgroup */
//    private int cols;
//    /** Number or colspecs encountered */
//    private int columnNumber;
//    /** Location of cols attribute */
//    private Locator colsLocator;
    private Mode processingMode;

    /**
	 * Create new profiling filter.
	 */
	public ValidationFilter() {
		super();
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

    public void setCurrentFile(final URI currentFile) {
        assert currentFile.isAbsolute();
        this.currentFile = currentFile;
    }

    public void setJob(final Job job) {
        this.job = job;
    }

    public void setProcessingMode(final Mode processingMode) {
        this.processingMode = processingMode;
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
	    String d = atts.getValue(ATTRIBUTE_NAME_DOMAINS);
	    if (d != null) {
	        domains.addFirst(StringUtils.getExtProps(d));
	    } else {
	        domains.addFirst(domains.peekFirst());
	    } 
		AttributesImpl modified = null;
		modified = validateLang(atts, null);
		modified = validateId(localName, atts, modified);
		modified = validateHref(atts, modified);
        modified = processFormatDitamap(atts, modified);
		validateKeys(atts);
		validateKeyscope(atts);
		validateAttributeValues(qName, atts);
		validateAttributeGeneralization(atts);
//        validateCols(atts);

		getContentHandler().startElement(uri, localName, qName, modified != null ? modified : atts);
	}

	@Override
    public void endElement(final String uri, final String localName, final String qName) throws SAXException {
        domains.removeFirst();
        getContentHandler().endElement(uri, localName, qName);
    }

    // Validation methods

//    /**
//     * Validate table {@code cols} attribute.
//     *
//     * @param atts attributes
//     */
//    private void validateCols(final Attributes atts) throws SAXException  {
//        if (TOPIC_TGROUP.matches(atts)) {
//            colsLocator = locator != null ? new LocatorImpl(locator) : null;
//            final String c = atts.getValue(ATTRIBUTE_NAME_COLS);
//            try {
//                cols = Integer.parseInt(c.trim());
//            } catch (final NumberFormatException e) {
//                if (processingMode == Mode.STRICT) {
//                    throw new SAXException(messageUtils.getMessage("DOTJ062E", ATTRIBUTE_NAME_COLS, c).setLocation(locator).toString());
//                } else {
//                    logger.error(messageUtils.getMessage("DOTJ062E", ATTRIBUTE_NAME_COLS, c).setLocation(locator).toString());
//                }
//                cols = -1;
//            }
//        } else if (TOPIC_COLSPEC.matches(atts)) {
//            columnNumber++;
//        } else if (TOPIC_THEAD.matches(atts) || TOPIC_TBODY.matches(atts)) {
//            if (cols != -1 && columnNumber > cols) {
//                if (processingMode == Mode.STRICT) {
//                    throw new SAXException(messageUtils.getMessage("DOTJ063E", Integer.toString(cols), Integer.toString(columnNumber)).setLocation(colsLocator).toString());
//                } else {
//                    logger.error(messageUtils.getMessage("DOTJ063E", Integer.toString(cols), Integer.toString(columnNumber)).setLocation(colsLocator).toString());
//                }
//            }
//            columnNumber = 0;
//        }
//    }

    /**
	 * Validate and fix {@code xml:lang} attribute.
	 * 
	 * @return modified attributes, {@code null} if there have been no changes 
	 */
    private AttributesImpl validateLang(final Attributes atts, final AttributesImpl modified) throws SAXException {
        AttributesImpl res = modified;
        final String lang = atts.getValue(XML_NS_URI, "lang");
		if (lang != null) {
			final int i = lang.indexOf('_');
			if (i != -1) {
				if (processingMode == Mode.STRICT) {
					throw new SAXException(messageUtils.getMessage("DOTJ056E", lang).setLocation(locator).toString());
				}
                logger.error(messageUtils.getMessage("DOTJ056E", lang).setLocation(locator).toString());
				if (processingMode == Mode.LAX) {
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
	 * Validate {@code id} attribute for uniqueness.
	 */
    private AttributesImpl validateId(final String localName, final Attributes atts, final AttributesImpl modified) throws SAXException {
        AttributesImpl res = modified;
        final String cls = atts.getValue(ATTRIBUTE_NAME_CLASS);
        if (MAP_MAP.matches(cls)) {
            topicIds.clear();
        } else if (TOPIC_TOPIC.matches(cls)) {
            final String id = atts.getValue(ATTRIBUTE_NAME_ID);
            if (id == null) {
                switch (processingMode) {
                case STRICT:
                    throw new SAXException(messageUtils.getMessage("DOTJ067E", localName).setLocation(locator).toString());
                case LAX:
                    if (res == null) {
                        res = new AttributesImpl(atts);
                    }
                    final String gen = "gen_" + UUID.randomUUID().toString();
                    XMLUtils.addOrSetAttribute(res, ATTRIBUTE_NAME_ID, gen);
                    logger.error(messageUtils.getMessage("DOTJ066E", localName, gen).setLocation(locator).toString());
                    break;
                case SKIP:
                    logger.error(messageUtils.getMessage("DOTJ067E", localName).setLocation(locator).toString());
                    break;
                }
            }
            topicIds.clear();
        } else if (TOPIC_RESOURCEID.matches(cls) || DELAY_D_ANCHORID.matches(cls)) {
            // not considered a normal element ID
        } else {
			final String id = atts.getValue(ATTRIBUTE_NAME_ID);
			if (id != null) {
				if (topicIds.contains(id)) {
					if (processingMode == Mode.STRICT) {
						throw new SAXException(messageUtils.getMessage("DOTJ057E", id).setLocation(locator).toString());
					} else {
						logger.warn(messageUtils.getMessage("DOTJ057E", id).setLocation(locator).toString());			
					}
				}
				topicIds.add(id);
			}
		}
        return res;
    }

	/**
	 * Validate and fix {@code href} attribute for URI validity.
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
                    logger.error(messageUtils.getMessage("DOTJ054E", ATTRIBUTE_NAME_HREF, href).setLocation(locator) + ", using invalid value.");
                    break;
                case LAX:
                    try {
                        final String u = new URI(URLUtils.clean(href)).toASCIIString();
                        if (res == null) {
							res = new AttributesImpl(atts);
						}
                        res.setValue(res.getIndex(ATTRIBUTE_NAME_HREF), u);
                        logger.error(messageUtils.getMessage("DOTJ054E", ATTRIBUTE_NAME_HREF, href).setLocation(locator) + ", using '" + u + "'.");
                    } catch (final URISyntaxException e1) {
                        logger.error(messageUtils.getMessage("DOTJ054E", ATTRIBUTE_NAME_HREF, href).setLocation(locator) + ", using invalid value.");
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
        if (validateMap.isEmpty()) {
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
                            logger.warn(messageUtils.getMessage("DOTJ049W", attrName, qName, attrValue, StringUtils.join(valueSet, COMMA)).toString());
                        }
                    }
                }
            }
        }
    }

    /**
     * Validate {@code keys} attribute
     */
    private void validateKeys(final Attributes atts) {
        final String keys = atts.getValue(ATTRIBUTE_NAME_KEYS);
        if (keys != null) {
            for (final String key : keys.split(" ")) {
                if (!isValidKeyName(key)) {
                    logger.error(messageUtils.getMessage("DOTJ055E", key).toString());
                }
            }
        }
    }
    
    /**
     * Validate {@code keyscope} attribute
     */
    private void validateKeyscope(final Attributes atts) {
        final String keys = atts.getValue(ATTRIBUTE_NAME_KEYSCOPE);
        if (keys != null) {
            for (final String key : keys.trim().split("\\s+")) {
                if (!isValidKeyName(key)) {
                    logger.error(messageUtils.getMessage("DOTJ059E", key).toString());
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
	
    /**
     * Validate attribute generalization. A single element may not contain both generalized and specialized values for the same attribute.
     * 
     * @param atts attributes
     * @see <a href="http://docs.oasis-open.org/dita/v1.2/os/spec/archSpec/attributegeneralize.html">DITA 1.2 specification</a>
     */
    private void validateAttributeGeneralization(final Attributes atts) {
        final String[][] d = domains.peekFirst();
        if (d != null) {
            for (final String[] spec: d) {
                for (int i = spec.length - 1; i > -1; i--) {
                    if (atts.getValue(spec[i]) != null) {
                        for (int j = i - 1; j > -1; j--) {
                            if (atts.getValue(spec[j]) != null) {
                                logger.error(messageUtils.getMessage("DOTJ058E", spec[j], spec[i]).toString());
                            }
                        } 
                    }
                }
            }
        }
    }

    /**
     * Validate and fix topicref {@code format} attribute.
     *
     * @param atts original attributes
     * @param modified modified attributes
     * @return modified attributes, {@code null} if there have been no changes
     */
    private AttributesImpl processFormatDitamap(final Attributes atts, final AttributesImpl modified) {
        if (job == null) {
            return modified;
        }
        AttributesImpl res = modified;
        final String cls = atts.getValue(ATTRIBUTE_NAME_CLASS);
        if (MAP_TOPICREF.matches(cls)) {
            final String format = atts.getValue(ATTRIBUTE_NAME_FORMAT);
            final String scope = atts.getValue(ATTRIBUTE_NAME_SCOPE);
            final URI href = toURI(atts.getValue(ATTRIBUTE_NAME_HREF));
            if (format == null && (scope == null || scope.equals(ATTR_SCOPE_VALUE_LOCAL)) && href != null) {
                final URI target = currentFile.resolve(href);
                final Job.FileInfo fi = job.getFileInfo(target);
                if (fi != null && ATTR_FORMAT_VALUE_DITAMAP.equals(fi.format)) {
                    switch (processingMode) {
                        case STRICT:
                            throw new RuntimeException(messageUtils.getMessage("DOTJ061E").setLocation(locator).toString());
                        case SKIP:
                                logger.error(messageUtils.getMessage("DOTJ061E").setLocation(locator).toString());
                            break;
                        case LAX:
                                logger.error(messageUtils.getMessage("DOTJ061E").setLocation(locator).toString());
                            if (res == null) {
                                res = new AttributesImpl(atts);
                            }
                            XMLUtils.addOrSetAttribute(res, ATTRIBUTE_NAME_FORMAT, fi.format);
                            break;
                    }
                }
            }
        }
        return res;
    }

}
