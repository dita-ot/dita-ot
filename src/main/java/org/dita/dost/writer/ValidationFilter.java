/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2013 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.writer;

import org.dita.dost.log.MessageUtils;
import org.dita.dost.util.Job;
import org.dita.dost.util.StringUtils;
import org.dita.dost.util.URLUtils;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static javax.xml.XMLConstants.XML_NS_URI;
import static org.dita.dost.chunk.ChunkModule.isLocalScope;
import static org.dita.dost.util.Configuration.Mode;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.URLUtils.toURI;
import static org.dita.dost.util.XMLUtils.addOrSetAttribute;

/**
 * Validation and optional error recovery filter.
 */
public final class ValidationFilter extends AbstractXMLFilter {

    private final Set<String> topicIds = new HashSet<>();
    private Map<QName, Map<String, Set<String>>> validateMap = null;
    private Locator locator;
    /** Deque of domains attibute values */
    private final Deque<QName[][]> domains = new LinkedList<>();
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
    public void setValidateMap(final Map<QName, Map<String, Set<String>>> validateMap) {
        this.validateMap = validateMap;
    }

    public void setProcessingMode(final Mode processingMode) {
        this.processingMode = processingMode;
    }

    // Locator methods

    @Override
    public void setDocumentLocator(final Locator locator) {
        this.locator = locator;
        super.setDocumentLocator(locator);
    }

    // SAX methods

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes atts)
            throws SAXException {
        final String d = atts.getValue(ATTRIBUTE_NAME_DOMAINS);
        if (d != null) {
            domains.addFirst(StringUtils.getExtProps(d));
        } else {
            final String s = atts.getValue(ATTRIBUTE_NAME_SPECIALIZATIONS);
            if (s != null) {
                domains.addFirst(StringUtils.getExtPropsFromSpecializations(s));
            } else {
                domains.addFirst(domains.peekFirst());
            }
        }
        AttributesImpl modified;
        modified = validateLang(atts, null);
        modified = validateId(localName, atts, modified);
        modified = validateReference(ATTRIBUTE_NAME_HREF, atts, modified);
        modified = validateReference(ATTRIBUTE_NAME_CONREF, atts, modified);
        modified = validateScope(atts, modified);
        modified = processFormatDitamap(atts, modified);
        validateKeys(atts);
        validateKeyscope(atts);
        validateAttributeValues(qName, atts);
        validateAttributeGeneralization(atts);

        super.startElement(uri, localName, qName, modified != null ? modified : atts);
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName) throws SAXException {
        domains.removeFirst();
        super.endElement(uri, localName, qName);
    }

    // Validation methods

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
                    throw new SAXException(MessageUtils.getMessage("DOTJ056E", lang).setLocation(locator).toString());
                }
                logger.error(MessageUtils.getMessage("DOTJ056E", lang).setLocation(locator).toString());
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
                    throw new SAXException(MessageUtils.getMessage("DOTJ067E", localName).setLocation(locator).toString());
                case LAX:
                    if (res == null) {
                        res = new AttributesImpl(atts);
                    }
                    final String gen = "gen_" + UUID.randomUUID().toString();
                    addOrSetAttribute(res, ATTRIBUTE_NAME_ID, gen);
                    logger.error(MessageUtils.getMessage("DOTJ066E", localName, gen).setLocation(locator).toString());
                    break;
                case SKIP:
                    logger.error(MessageUtils.getMessage("DOTJ067E", localName).setLocation(locator).toString());
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
                        throw new SAXException(MessageUtils.getMessage("DOTJ057E", id).setLocation(locator).toString());
                    } else {
                        logger.warn(MessageUtils.getMessage("DOTJ057E", id).setLocation(locator).toString());
                    }
                }
                topicIds.add(id);
            }
        }
        return res;
    }

    /**
     * Validate and fix {@code href} or {@code conref} attribute for URI validity.
     *
     * @return modified attributes, {@code null} if there have been no changes
     */
    private AttributesImpl validateReference(final String attrName, final Attributes atts, final AttributesImpl modified) {
        AttributesImpl res = modified;
        final String href = atts.getValue(attrName);
        if (href != null) {
            try {
                final URI uri = new URI(href);
                final URI abs = URLUtils.stripFragment(currentFile.resolve(uri)).normalize();
                if (abs.getScheme() != null && abs.getScheme().equals("file")) {
                    final File p = new File(abs);
                    try {
                        final File canFile = p.getCanonicalFile();
                        final String absPath = p.getAbsolutePath();
                        final String canPath = canFile.toString();
                        if (!Objects.equals(absPath, canPath) && Objects.equals(absPath.toLowerCase(), canPath.toLowerCase())) {
                            switch (processingMode) {
                                case STRICT:
                                    throw new RuntimeException(MessageUtils.getMessage("DOTJ083E", abs.toString()).setLocation(locator).toString());
                                case SKIP:
                                    logger.error(MessageUtils.getMessage("DOTJ083E", abs.toString()).setLocation(locator).toString() + ", using authored value.");
                                    break;
                                case LAX:
                                    final URI corrected = URLUtils.setFragment(currentFile.relativize(canFile.toURI()), uri.getFragment());
                                    if (res == null) {
                                        res = new AttributesImpl(atts);
                                    }
                                    res.setValue(res.getIndex(attrName), currentFile.toString());
                                    logger.error(MessageUtils.getMessage("DOTJ083E", abs.toString()).setLocation(locator).toString() + ", using " + corrected + ".");
                                    break;
                            }
                        }
                    } catch (IOException e) {
                        logger.debug(String.format("Failed to resolve real path for %s: %s", p, e.getMessage()), e);
                    }
                }
            } catch (final URISyntaxException e) {
                switch (processingMode) {
                case STRICT:
                    throw new RuntimeException(MessageUtils.getMessage("DOTJ054E", attrName, href).setLocation(locator) + ": " + e.getMessage(), e);
                case SKIP:
                    logger.error(MessageUtils.getMessage("DOTJ054E", attrName, href).setLocation(locator) + ", using invalid value.");
                    break;
                case LAX:
                    try {
                        final URI uri = new URI(URLUtils.clean(href.trim()));
                        if (res == null) {
                            res = new AttributesImpl(atts);
                        }
                        res.setValue(res.getIndex(attrName), uri.toASCIIString());
                        logger.error(MessageUtils.getMessage("DOTJ054E", attrName, href).setLocation(locator) + ", using '" + uri.toASCIIString() + "'.");
                    } catch (final URISyntaxException e1) {
                        logger.error(MessageUtils.getMessage("DOTJ054E", attrName, href).setLocation(locator) + ", using invalid value.");
                    }
                    break;
                }
            }
        }
        return res;
    }

    /**
     * Fix implicit {@code scope} attribute.
     *
     * @return modified attributes, {@code null} if there have been no changes
     */
    private AttributesImpl validateScope(final Attributes atts, final AttributesImpl modified) {
        AttributesImpl res = modified;
        final String scope = atts.getValue(ATTRIBUTE_NAME_SCOPE);
        final URI href = toURI(atts.getValue(ATTRIBUTE_NAME_HREF));
        if (scope == null && href != null && href.isAbsolute()) {
            final boolean sameScheme = Objects.equals(currentFile.getScheme(), href.getScheme());
            final boolean sameAuthority = Objects.equals(currentFile.getRawAuthority(), href.getRawAuthority());
            if (!(sameScheme && sameAuthority)) {
                switch (processingMode) {
                    case LAX:
                        if (res == null) {
                            res = new AttributesImpl(atts);
                        }
                        addOrSetAttribute(res, ATTRIBUTE_NAME_SCOPE, ATTR_SCOPE_VALUE_EXTERNAL);
                        logger.warn(MessageUtils.getMessage("DOTJ075W", href.toString()).setLocation(locator).toString());
                        break;
                    default:
                        logger.warn(MessageUtils.getMessage("DOTJ076W", href.toString()).setLocation(locator) + ", using invalid value.");
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
            final QName attrName = new QName(atts.getURI(i), atts.getLocalName(i));
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
                            logger.warn(MessageUtils.getMessage("DOTJ049W",
                                    attrName.toString(), qName, attrValue, StringUtils.join(valueSet, COMMA)).toString());
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
                    logger.error(MessageUtils.getMessage("DOTJ055E", key).toString());
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
                    logger.error(MessageUtils.getMessage("DOTJ059E", key).toString());
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
        final QName[][] d = domains.peekFirst();
        if (d != null) {
            for (final QName[] spec: d) {
                for (int i = spec.length - 1; i > -1; i--) {
                    if (atts.getValue(spec[i].getNamespaceURI(), spec[i].getLocalPart()) != null) {
                        for (int j = i - 1; j > -1; j--) {
                            if (atts.getValue(spec[j].getNamespaceURI(), spec[j].getLocalPart()) != null) {
                                logger.error(MessageUtils.getMessage("DOTJ058E",
                                        spec[j].toString(), spec[i].toString()).toString());
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
            if (format == null && isLocalScope(scope) && href != null) {
                final URI target = currentFile.resolve(href);
                final Job.FileInfo fi = job.getFileInfo(target);
                if (fi != null && ATTR_FORMAT_VALUE_DITAMAP.equals(fi.format)) {
                    switch (processingMode) {
                        case STRICT:
                            throw new RuntimeException(MessageUtils.getMessage("DOTJ061E").setLocation(locator).toString());
                        case SKIP:
                                logger.error(MessageUtils.getMessage("DOTJ061E").setLocation(locator).toString());
                            break;
                        case LAX:
                                logger.error(MessageUtils.getMessage("DOTJ061E").setLocation(locator).toString());
                            if (res == null) {
                                res = new AttributesImpl(atts);
                            }
                            addOrSetAttribute(res, ATTRIBUTE_NAME_FORMAT, fi.format);
                            break;
                    }
                }
            }
        }
        return res;
    }

}
