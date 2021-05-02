/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2011 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.util;

import static net.sf.saxon.s9api.streams.Predicates.hasAttribute;
import static org.dita.dost.util.Constants.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import com.google.common.annotations.VisibleForTesting;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;

/**
 * DITA specialization hierarchy object.
 *
 * <p>Instances of this class are immutable and are safe for use by multiple concurrent threads.</p>
 *
 * @since 1.5.3
 * @author Jarno Elovirta
 */
public final class DitaClass {

    // Variables

    private static final Pattern WHITESPACE = Pattern.compile("\\s+");
    private static final Pattern VALID_DITA_CLASS = Pattern.compile("(\\+|-)\\s+(topic|map)/\\S+\\s+" +
                                                         "([\\S[^/]]+/\\S+\\s+)*");

    private static final Map<String, DitaClass> cache = new ConcurrentHashMap<>();

    /** ModuleElem/type pair for the most specialized type, with a single preceding and following space character. */
    public final String matcher;
    /** Type name, i.e. local element name. */
    public final String localName;
    /** Normalized specialization hierarchy string. */
    private final String stringValue;
    /** Does this class value use valid DITA class syntax */
    private boolean validDitaClass = false;

    // Constructors

    /**
     * Constructor. Use {@link #getInstance(String)} instead.
     *
     * @param cls DITA specialization hierarchy string
     */
    @VisibleForTesting
    DitaClass(final String cls) {
        final String[] tokens = WHITESPACE.split(cls);
        final String last = tokens[tokens.length - 1];
        matcher = ' ' + last + ' ';
        localName = last.substring(last.indexOf('/') + 1);
        final StringBuilder sb = new StringBuilder();
        for (final String s: tokens) {
            sb.append(s).append(' ');
        }
        stringValue = sb.toString();
        validDitaClass = VALID_DITA_CLASS.matcher(stringValue).matches();
    }

    /**
     * Get class instance.
     * @param cls DITA class, may be {@code null}
     * @return DITA class, {@code null} if the input was {@code null}
     */
    public static DitaClass getInstance(final String cls) {
        if (cls == null) {
            return null;
        }
        return cache.computeIfAbsent(WHITESPACE.matcher(cls).replaceAll(" "), DitaClass::new);
    }

    /**
     * Get class instance.
     * @param atts attributes, may be {@code null}
     * @return DITA class, {@code null} if the input didn't contains a class
     */
    public static DitaClass getInstance(final Attributes atts) {
        if (atts == null) {
            return null;
        }
        return getInstance(atts.getValue(ATTRIBUTE_NAME_CLASS));
    }

    /**
     * Get class instance.
     * @param elem element, may be {@code null}
     * @return DITA class, {@code null} if the input didn't contains a class
     */
    public static DitaClass getInstance(final Element elem) {
        if (elem == null) {
            return null;
        }
        final Attr attr = elem.getAttributeNode(ATTRIBUTE_NAME_CLASS);
        if (attr == null) {
            return null;
        }
        return getInstance(attr.getNodeValue());
    }

    // Public methods

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (stringValue.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DitaClass other = (DitaClass) obj;
        return stringValue.equals(other.stringValue);
    }

    /**
     * Get DITA specialization hierarchy string, i.e. the class attribute value.
     *
     * @return specialization hierarchy string
     */
    @Override
    public String toString() {
        return stringValue;
    }

    /**
     * Test if given DITA class matches this DITA class.
     *
     * @param cls DITA element class
     * @return {@code true} if given class matches this class, otherwise {@code false}
     */
    public boolean matches(final DitaClass cls) {
        return cls != null && cls.toString().contains(matcher);
    }

    /**
     * Test if given DITA class string matches this DITA class.
     *
     * @param classString DITA element class string
     * @return {@code true} if given class matches this class, otherwise {@code false}
     */
    public boolean matches(final String classString) {
        return classString != null && classString.contains(matcher);
    }

    /**
     * Test if given DITA class string matches this DITA class.
     *
     * @param atts SAX attributes
     * @return {@code true} if given attribute set has a class attribute and it matches this class, otherwise {@code false}
     */
    public boolean matches(final Attributes atts) {
        return matches(atts.getValue(ATTRIBUTE_NAME_CLASS));
    }

    /**
     * Test if given DITA class string matches this DITA class.
     *
     * @param node DOM DITA element
     * @return {@code true} if given node is an Element and its class matches this class, otherwise {@code false}
     */
    public boolean matches(final Node node) {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            return matches(((Element) node).getAttribute(ATTRIBUTE_NAME_CLASS));
        }
        return false;
    }

    /**
     * Test if given DITA class string matches this DITA class.
     *
     * @param node S9api DITA element
     * @return {@code true} if given node is an Element and its class matches this class, otherwise {@code false}
     */
    public boolean matches(final XdmNode node) {
        if (node.getNodeKind() == XdmNodeKind.ELEMENT) {
            return matches(node.attribute(ATTRIBUTE_NAME_CLASS));
        }
        return false;
    }

    /**
     * S9api predicate to match element node against DITA class.
     *
     * @return predicate that returns {@code true} if given node is an Element and its class matches this class,
     * otherwise {@code false}
     */
    public Predicate<XdmNode> matcher() {
        return item -> item.getNodeKind() == XdmNodeKind.ELEMENT
                && matches(item.attribute(ATTRIBUTE_NAME_CLASS));
    }

    /**
     * Test if the current DitaClass is a valid DITA class value
     *
     * @return {@code true} if uses valid DITA class syntax, otherwise {@code false}
     */
    public boolean isValid() {
        return validDitaClass;
    }

}
