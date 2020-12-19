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

import javax.xml.namespace.QName;
import java.util.*;
import java.util.stream.Collectors;

import static org.dita.dost.util.Constants.*;

/**
 * Profiling filter strips out the content that is not necessary in the output.
 */
public final class ProfilingFilter extends AbstractXMLFilter {

    /** when exclude is true the tag will be excluded. */
    private boolean exclude;
    /** level is used to count the element level in the filtering */
    private int level;
    /** Contains the attribution specialization paths for {@code props} attribute */
    private QName[][] props;
    /** Filter utils */
    private List<FilterUtils> filterUtils;
    /** Flag that an element has been written */
    private boolean elementOutput;
    /** Namespace prefixes for current element. */
    private final Map<String, String> prefixes = new HashMap<>();
    /** Flag that last element was excluded. */
    private boolean lastElementExcluded = false;
    private final Deque<Set<Flag>> flagStack = new LinkedList<>();
    private final boolean doFlag;

    /**
     * Create new profiling filter.
     */
    public ProfilingFilter() {
        this(true);
    }

    /**
     * Create new profiling filter.
     *
     * @param doFlag is flagging enabled
     */
    public ProfilingFilter(final boolean doFlag) {
        super();
        this.doFlag = doFlag;
    }

    /**
     * Set content filter.
     *
     * @param filterUtils filter utils
     */
    public void setFilterUtils(final FilterUtils filterUtils) {
        this.filterUtils = Collections.singletonList(filterUtils);
    }

    /**
     * Set content filter.
     *
     * @param filterUtils filter utils
     */
    public void setFilterUtils(final List<FilterUtils> filterUtils) {
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
        Set<Flag> flags = null;

        final DitaClass cls = atts.getValue(ATTRIBUTE_NAME_CLASS) != null ? DitaClass.getInstance(atts) : DitaClass.getInstance("");
        if (cls.isValid() && (TOPIC_TOPIC.matches(cls) || MAP_MAP.matches(cls))) {
            final String domains = atts.getValue(ATTRIBUTE_NAME_DOMAINS);
            if (domains != null) {
                props = StringUtils.getExtProps(domains);
            } else {
                final String specializations = atts.getValue(ATTRIBUTE_NAME_SPECIALIZATIONS);
                if (specializations != null) {
                    props = StringUtils.getExtPropsFromSpecializations(specializations);
                }
            }
        }

        if (exclude) {
            // If it is the start of a child of an excluded tag, level increase
            level++;
        } else { // exclude shows whether it's excluded by filtering
            if (cls.isValid() && filterUtils.stream().anyMatch(f -> f.needExclude(atts, props))) {
                exclude = true;
                level = 0;
            } else {
                elementOutput = true;
                for (final Map.Entry<String, String> prefix: prefixes.entrySet()) {
                    getContentHandler().startPrefixMapping(prefix.getKey(), prefix.getValue());
                }
                prefixes.clear();
                getContentHandler().startElement(uri, localName, qName, atts);
                if (doFlag && cls.isValid()) {
                    flags = filterUtils.stream()
                            .flatMap(f -> f.getFlags(atts, props).stream())
                            .map(f -> f.adjustPath(currentFile, job))
                            .collect(Collectors.toSet());
                    for (final Flag flag: flags) {
                        flag.writeStartFlag(getContentHandler());
                    }
                }
            }
        }

        if (doFlag) {
            flagStack.push(flags);
        }
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName)
            throws SAXException {
        if (doFlag) {
            final Set<Flag> flags = flagStack.pop();
            if (flags != null) {
                for (final Flag flag : flags) {
                    flag.writeEndFlag(getContentHandler());
                }
            }
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
        level = 0;
        props = null;
        getContentHandler().startDocument();
    }

    @Override
    public void startPrefixMapping(final String prefix, final String uri) throws SAXException {
        prefixes.put(prefix, uri);
    }

}
