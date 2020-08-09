/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2013 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.util;

import net.sf.saxon.s9api.XdmNode;
import org.dita.dost.exception.DITAOTException;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.net.URI;
import java.util.Collection;

import static javax.xml.XMLConstants.NULL_NS_URI;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.XMLUtils.EMPTY_ATTRIBUTES;

/**
 * Key definition.
 */
public class KeyDef {

    public static final String ELEMENT_STUB = "stub";
    private static final String ATTRIBUTE_SOURCE = "source";
    private static final String ATTRIBUTE_HREF = "href";
    private static final String ATTRIBUTE_SCOPE = "scope";
    private static final String ATTRIBUTE_FORMAT = "format";
    private static final String ATTRIBUTE_KEYS = "keys";
    private static final String ELEMENT_KEYDEF = "keydef";

    /** Space delimited list of key names */
    public final String keys;
    public URI href;
    public final String scope;
    public final URI source;
    public final XdmNode element;
    public final String format;

    /**
     * Construct new key definition.
     *
     * @param keys key name
     * @param href href URI, may be {@code null}
     * @param scope link scope, may be {@code null}
     * @param source key definition source, may be {@code null}
     */
    public KeyDef(final String keys, final URI href, final String scope, final String format, final URI source, final XdmNode element) {
        //assert href.isAbsolute();
        this.keys = keys;
        this.href = href == null || href.toString().isEmpty() ? null : href;
        this.scope = scope == null ? ATTR_SCOPE_VALUE_LOCAL : scope;
        this.format = format == null ? ATTR_FORMAT_VALUE_DITA : format;
        this.source = source;
        this.element = element;
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder().append(keys).append(EQUAL);
        if (href != null) {
            buf.append(href.toString());
        }
        if (scope != null) {
            buf.append(LEFT_BRACKET).append(scope).append(RIGHT_BRACKET);
        }
        if (source != null) {
            buf.append(LEFT_BRACKET).append(source.toString()).append(RIGHT_BRACKET);
        }
        return buf.toString();
    }

    /**
     * Write key definition XML configuration file
     *
     * @param handler key definition output resource
     * @param keydefs list of key definitions
     * @throws DITAOTException if writing configuration file failed
     */
    public static void writeKeydef(final ContentHandler handler, final Collection<KeyDef> keydefs) throws DITAOTException {
        try {
            handler.startDocument();
            handler.startElement(NULL_NS_URI, ELEMENT_STUB, ELEMENT_STUB, EMPTY_ATTRIBUTES);
            for (final KeyDef k : keydefs) {
                final XMLUtils.AttributesBuilder atts = new XMLUtils.AttributesBuilder();
                atts.add(ATTRIBUTE_KEYS, k.keys);
                if (k.href != null) {
                    atts.add(ATTRIBUTE_HREF, k.href.toString());
                }
                if (k.scope != null) {
                    atts.add(ATTRIBUTE_SCOPE, k.scope);
                }
                if (k.format != null) {
                    atts.add(ATTRIBUTE_FORMAT, k.format);
                }
                if (k.source != null) {
                    atts.add(ATTRIBUTE_SOURCE, k.source.toString());
                }
                handler.startElement(NULL_NS_URI, ELEMENT_KEYDEF, ELEMENT_KEYDEF, atts.build());
                handler.endElement(NULL_NS_URI, ELEMENT_KEYDEF, ELEMENT_KEYDEF);
            }
            handler.endElement(NULL_NS_URI, ELEMENT_STUB, ELEMENT_STUB);
            handler.endDocument();
        } catch (final SAXException e) {
            throw new DITAOTException("Failed to write key definition file: " + e.getMessage(), e);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((href == null) ? 0 : href.hashCode());
        result = prime * result + ((keys == null) ? 0 : keys.hashCode());
        result = prime * result + ((scope == null) ? 0 : scope.hashCode());
        result = prime * result + ((format == null) ? 0 : format.hashCode());
        result = prime * result + ((source == null) ? 0 : source.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof KeyDef)) {
            return false;
        }
        KeyDef other = (KeyDef) obj;
        if (href == null) {
            if (other.href != null) {
                return false;
            }
        } else if (!href.equals(other.href)) {
            return false;
        }
        if (keys == null) {
            if (other.keys != null) {
                return false;
            }
        } else if (!keys.equals(other.keys)) {
            return false;
        }
        if (scope == null) {
            if (other.scope != null) {
                return false;
            }
        } else if (!scope.equals(other.scope)) {
            return false;
        }
        if (format == null) {
            if (other.format != null) {
                return false;
            }
        } else if (!format.equals(other.format)) {
            return false;
        }
        if (source == null) {
            if (other.source != null) {
                return false;
            }
        } else if (!source.equals(other.source)) {
            return false;
        }
        return true;
    }

}
