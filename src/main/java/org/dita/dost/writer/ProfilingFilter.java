/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */
package org.dita.dost.writer;

import static org.dita.dost.util.Constants.*;

import org.dita.dost.log.MessageUtils;
import org.dita.dost.module.Content;
import org.dita.dost.util.DITAAttrUtils;
import org.dita.dost.util.FilterUtils;
import org.dita.dost.util.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Profiling filter strips out the content that is not necessary in the output.
 */
public final class ProfilingFilter extends AbstractXMLFilter {

	/** Transtype */
	private String transtype;
	/** when exclude is true the tag will be excluded. */
	private boolean exclude;
	/** foreign/unknown nesting level */
	private int foreignLevel;
	/** level is used to count the element level in the filtering */
	private int level;
	/** Contains the attribution specialization paths for {@code props} attribute */
	private String[][] props;
	private final DITAAttrUtils ditaAttrUtils = DITAAttrUtils.getInstance();
	/** Filter utils */
	private FilterUtils filterUtils;

	/**
	 * Create new profiling filter.
	 */
	public ProfilingFilter() {
		super();
	}
	
	@Override
	public void setContent(final Content content) {
		throw new UnsupportedOperationException();
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
	 * Set transtype.
	 * 
	 * @param transtype the transtype to set
	 */
	public void setTranstype(final String transtype) {
		this.transtype = transtype;
	}

	// SAX methods

	@Override
	public void startElement(final String uri, final String localName, final String qName, final Attributes atts)
			throws SAXException {
		// increase element level for nested tags.
		ditaAttrUtils.increasePrintLevel(atts.getValue(ATTRIBUTE_NAME_PRINT));
		if (ditaAttrUtils.needExcludeForPrintAttri(transtype)) {
			return;
		}

		if (foreignLevel > 0) {
			foreignLevel++;
		} else if (foreignLevel == 0) {
			final String classAttr = atts.getValue(ATTRIBUTE_NAME_CLASS);
			if (classAttr == null && !ELEMENT_NAME_DITA.equals(localName)) {
				logger.logInfo(MessageUtils.getInstance().getMessage("DOTJ030I", localName).toString());
			}
			if (classAttr != null && (TOPIC_TOPIC.matches(classAttr) || MAP_MAP.matches(classAttr))) {
				final String domains = atts.getValue(ATTRIBUTE_NAME_DOMAINS);
				if (domains == null) {
					logger.logInfo(MessageUtils.getInstance().getMessage("DOTJ029I", localName).toString());
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
				getContentHandler().startElement(uri, localName, qName, atts);
			}
		}
	}

	@Override
	public void endElement(final String uri, final String localName, final String qName)
			throws SAXException {
		// need to skip the tag
		if (ditaAttrUtils.needExcludeForPrintAttri(transtype)) {
			// decrease level
			ditaAttrUtils.decreasePrintLevel();
			// don't write the end tag
			return;
		}

		if (foreignLevel > 0) {
			foreignLevel--;
		}
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
		ditaAttrUtils.reset();
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
		if (!exclude) {
        	getContentHandler().startDocument();
        }
	}
	
}
