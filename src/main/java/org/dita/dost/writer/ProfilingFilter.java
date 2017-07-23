/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2012 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.writer;

import org.dita.dost.log.MessageUtils;
import org.dita.dost.util.DitaClass;
import org.dita.dost.util.FilterUtils;
import org.dita.dost.util.FilterUtils.Flag;
import org.dita.dost.util.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.*;

import static org.dita.dost.util.Constants.*;

/**
 * Profiling filter strips out the content that is not necessary in the output.
 */
public final class ProfilingFilter extends AbstractXMLFilter {

    /** when exclude is true the tag will be excluded. */
    private boolean exclude;
    /** DITA class values for open elements **/
    private final Deque<DitaClass> classes = new LinkedList<>();
    /** level is used to count the element level in the filtering */
    private int level;
    /** Contains the attribution specialization paths for {@code props} attribute */
    private String[][] props;
    /** Filter utils */
    private FilterUtils filterUtils;
    /** Flag that an element has been written */
    private boolean elementOutput;
    /** Namespace prefixes for current element. */
    private final Map<String, String> prefixes = new HashMap<>();
    /** Flag that last element was excluded. */
    private boolean lastElementExcluded = false;
    private Deque<List<Flag>> flagStack = new LinkedList<>();

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
        List<Flag> flags = null;

        final DitaClass cls = atts.getValue(ATTRIBUTE_NAME_CLASS) != null ? new DitaClass(atts.getValue(ATTRIBUTE_NAME_CLASS)) : new DitaClass("");
        if (cls.isValid()) {
            classes.addFirst(cls);
        } else {
            classes.addFirst(null);
        }

        if (cls.isValid() && (TOPIC_TOPIC.matches(cls) || MAP_MAP.matches(cls))) {
            final String domains = atts.getValue(ATTRIBUTE_NAME_DOMAINS);
            if (domains == null) {
                logger.info(MessageUtils.getInstance().getMessage("DOTJ029I", localName).toString());
            } else {
                props = StringUtils.getExtProps(domains);
            }
        }

        if (exclude) {
            // If it is the start of a child of an excluded tag, level increase
            level++;
        } else { // exclude shows whether it's excluded by filtering
            if (cls.isValid() && filterUtils.needExclude(atts, props)) {
                exclude = true;
                level = 0;
            } else {
                elementOutput = true;
                for (final Map.Entry<String, String> prefix: prefixes.entrySet()) {
                    getContentHandler().startPrefixMapping(prefix.getKey(), prefix.getValue());
                }
                prefixes.clear();
                getContentHandler().startElement(uri, localName, qName, atts);
                if (cls.isValid()) {
                    flags = filterUtils.getFlags(atts, props);
                    for (final Flag flag: flags) {
                        FilterUtils.writeStartFlag(getContentHandler(), flag);
                    }
                }
            }
        }

        flagStack.push(flags);
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName)
            throws SAXException {
        final List<Flag> flags = flagStack.pop();
        if (flags != null) {
            for (final Flag flag : flags) {
                FilterUtils.writeEndFlag(getContentHandler(), flag);
            }
        }

        classes.pop();
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
        classes.clear();
        level = 0;
        props = null;
        getContentHandler().startDocument();
    }

    @Override
    public void startPrefixMapping(final String prefix, final String uri) throws SAXException {
        prefixes.put(prefix, uri);
    }

}
