/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */
package org.dita.dost.writer;

import static org.dita.dost.util.Constants.*;

import org.dita.dost.log.MessageUtils;
import org.dita.dost.util.FilterUtils;
import org.dita.dost.util.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.HashMap;
import java.util.Map;

/**
 * Profiling filter strips out the content that is not necessary in the output.
 */
public final class ProfilingFilter extends AbstractXMLFilter {

	/** when exclude is true the tag will be excluded. */
	private boolean exclude;
	/** foreign/unknown nesting level */
	private int foreignLevel;
	/** level is used to count the element level in the filtering */
	private int level;
	/** Contains the attribution specialization paths for {@code props} attribute */
	private String[][] props;
	/** Filter utils */
	private FilterUtils filterUtils;
	/** Flag that an element has been written */
	private boolean elementOutput;
    /** Namespace prefixes for current element. */
    private Map<String, String> prefixes = new HashMap<>();
    /** Flag that last element was excluded. */
    private boolean lastElementExcluded = false;

	/**
	 * Create new profiling filter.
	 */
	public ProfilingFilter() {
		super();
	}

	/**
	 * Set content filter.
	 * 
	 * @param filterUtils filter utils
	 */
	public void setFilterUtils(final FilterUtils filterUtils) {
		this.filterUtils = filterUtils;
	}
	
	/**
	 * Get flag whether elements were output.
	 */
	public boolean hasElementOutput() {
	    return elementOutput;
	}

	// SAX methods

	@Override
	public void startElement(final String uri, final String localName, final String qName, final Attributes atts)
			throws SAXException {
		if (foreignLevel > 0) {
			foreignLevel++;
		} else if (foreignLevel == 0) {
			final String classAttr = atts.getValue(ATTRIBUTE_NAME_CLASS);
			if (classAttr == null && !ELEMENT_NAME_DITA.equals(localName)) {
				logger.info(MessageUtils.getInstance().getMessage("DOTJ030I", localName).toString());
			}
			if (classAttr != null && (TOPIC_TOPIC.matches(classAttr) || MAP_MAP.matches(classAttr))) {
				final String domains = atts.getValue(ATTRIBUTE_NAME_DOMAINS);
				if (domains == null) {
					logger.info(MessageUtils.getInstance().getMessage("DOTJ029I", localName).toString());
				} else {
					props = StringUtils.getExtProps(domains);
				}
			}
			if (classAttr != null && (TOPIC_FOREIGN.matches(classAttr) || TOPIC_UNKNOWN.matches(classAttr))) {
				foreignLevel = 1;
			}
		}

		if (exclude) {
			// If it is the start of a child of an excluded tag, level increase
			level++;
		} else { // exclude shows whether it's excluded by filtering
			if (foreignLevel <= 1 && filterUtils.needExclude(atts, props)) {
				exclude = true;
				level = 0;
			} else {
			    elementOutput = true;
                for (final Map.Entry<String, String> prefix: prefixes.entrySet()) {
                    getContentHandler().startPrefixMapping(prefix.getKey(), prefix.getValue());
                }
                prefixes.clear();
				getContentHandler().startElement(uri, localName, qName, atts);
			}
		}
	}

	@Override
	public void endElement(final String uri, final String localName, final String qName)
			throws SAXException {
		if (foreignLevel > 0) {
			foreignLevel--;
		}
        lastElementExcluded = exclude;
		if (exclude) {
			if (level > 0) {
				// If it is the end of a child of an excluded tag, level
				// decrease
				level--;
			} else {
				exclude = false;
			}
		} else { // exclude shows whether it's excluded by filtering
			getContentHandler().endElement(uri, localName, qName);
		}

	}

    @Override
    public void characters(final char[] ch, final int start, final int length)
            throws SAXException {
        if (!exclude) {
        	getContentHandler().characters(ch, start, length);
        }
    }

	@Override
	public void endDocument() throws SAXException {
		if (!exclude) {
        	getContentHandler().endDocument();
        }
	}

	@Override
	public void endPrefixMapping(final String prefix) throws SAXException {
		if (!lastElementExcluded) {
			getContentHandler().endPrefixMapping(prefix);
		}
	}

	@Override
	public void ignorableWhitespace(final char[] ch, final int start, final int length)
			throws SAXException {
		if (!exclude) {
        	getContentHandler().characters(ch, start, length);
        }
	}

	@Override
	public void processingInstruction(final String target, final String data)
			throws SAXException {
		if (!exclude) {
        	getContentHandler().processingInstruction(target, data);
        }
	}

	@Override
	public void skippedEntity(final String name) throws SAXException {
		if (!exclude) {
        	getContentHandler().skippedEntity(name);
        }
	}

	@Override
	public void startDocument() throws SAXException {
		exclude = false;
		foreignLevel = 0;
		level = 0;
		props = null;
        getContentHandler().startDocument();
    }

	@Override
	public void startPrefixMapping(final String prefix, final String uri) throws SAXException {
        prefixes.put(prefix, uri);
	}
	
}
