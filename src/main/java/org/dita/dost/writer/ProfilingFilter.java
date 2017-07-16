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
import org.dita.dost.util.FilterUtils.Flag.FlagImage;
import org.dita.dost.util.StringUtils;
import org.dita.dost.util.XMLUtils;
import org.dita.dost.util.XMLUtils.AttributesBuilder;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static javax.xml.XMLConstants.NULL_NS_URI;
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
                        writeStartFlag(flag);
                    }

                }
            }
        }

        flagStack.push(flags);
    }

    private void writeStartFlag(final Flag flag) throws SAXException {
        final StringBuilder outputclass = new StringBuilder();
        if (flag.color != null) {
            outputclass.append("color:").append(flag.color).append(";");
        }
        if (flag.backcolor != null) {
            outputclass.append("background-color:").append(flag.backcolor).append(";");
        }
        if (flag.style != null) {
            for (final String style : flag.style) {
                outputclass.append("text-decoration:").append(style).append(";");
            }
        }

        final AttributesBuilder atts = new AttributesBuilder()
                .add(ATTRIBUTE_NAME_CLASS, "+ topic/foreign ditaot-d/ditaval-startprop ");
        if (outputclass.length() != 0) {
            atts.add(ATTRIBUTE_NAME_OUTPUTCLASS, outputclass.toString());
        }
        getContentHandler().startElement(NULL_NS_URI, "ditaval-startprop", "ditaval-startprop",
                atts.build());
        writeProp(flag, true);
        getContentHandler().endElement(NULL_NS_URI, "ditaval-startprop", "ditaval-startprop");
    }

    private void writeProp(Flag flag, final boolean isStart) throws SAXException {
        final AttributesBuilder propAtts = new AttributesBuilder().add("action", "flag");
        if (flag.color != null) {
            propAtts.add("color", flag.color);
        }
        if (flag.backcolor != null) {
            propAtts.add("backcolor", flag.backcolor);
        }
        if (flag.style != null) {
            propAtts.add("style", Stream.of(flag.style).collect(Collectors.joining(" ")));
        }
        getContentHandler().startElement(NULL_NS_URI, "prop", "prop", propAtts.build());
        if (isStart && flag.startflag != null) {
            writeFlag(flag.startflag, "startflag");
        }
        if (!isStart && flag.endflag != null) {
            writeFlag(flag.endflag, "endflag");
        }
        getContentHandler().endElement(NULL_NS_URI, "prop", "prop");
    }

    private void writeFlag(final FlagImage startflag, final String tag) throws SAXException {
        final AttributesBuilder propAtts = new AttributesBuilder().add("action", "flag");
        final URI abs = startflag.href;
        propAtts.add("http://dita-ot.sourceforge.net/ns/201007/dita-ot", "imagerefuri", "dita-ot:imagerefuri", "CDATA", abs.toString());
        final URI rel = abs.resolve(".").relativize(abs);
        propAtts.add("http://dita-ot.sourceforge.net/ns/201007/dita-ot", "original-imageref", "dita-ot:original-imageref", "CDATA", rel.toString());
        propAtts.add("imageref", rel.toString());
        getContentHandler().startElement(NULL_NS_URI, tag, tag, propAtts.build());
        if (startflag.alt != null) {
            getContentHandler().startElement(NULL_NS_URI, "alt-text", "alt-text", XMLUtils.EMPTY_ATTRIBUTES);
            final char[] chars = startflag.alt.toCharArray();
            getContentHandler().characters(chars, 0, chars.length);
            getContentHandler().endElement(NULL_NS_URI, "alt-text", "alt-text");
        }
        getContentHandler().endElement(NULL_NS_URI, tag, tag);
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName)
            throws SAXException {
        final List<Flag> flags = flagStack.pop();
        if (flags != null) {
            for (final Flag flag : flags) {
                writeEndFlag(flag);
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

    private void writeEndFlag(final Flag flag) throws SAXException {
        getContentHandler().startElement(NULL_NS_URI, "ditaval-endprop", "ditaval-endprop",
                new AttributesBuilder()
                        .add(ATTRIBUTE_NAME_CLASS, "+ topic/foreign ditaot-d/ditaval-endprop ")
                        .build());
        writeProp(flag, false);
        getContentHandler().endElement(NULL_NS_URI, "ditaval-endprop", "ditaval-endprop");
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
