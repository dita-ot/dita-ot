/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */
package org.dita.dost.util;

import static org.dita.dost.util.Constants.*;

import java.util.regex.Pattern;

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

    /** Module/type pair for the most specialized type, with a single preceding and following space character. */
    public final String matcher;
    /** Type name, i.e. local element name. */
    public final String localName;
    /** Normalized specialization hierarchy string. */
    private final String stringValue;

    // Constructors

    /**
     * Constructor
     * 
     * @param cls DITA specialization hierarchy string
     */
    public DitaClass(final String cls) {
        final String[] tokens = WHITESPACE.split(cls);
        final String last = tokens[tokens.length - 1];
        matcher = ' ' + last + ' ';
        localName = last.substring(last.indexOf('/') + 1);
        final StringBuilder sb = new StringBuilder();
        for (final String s: tokens) {
            sb.append(s).append(' ');
        }
        stringValue = sb.toString();
    }

    // Public methods

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((stringValue == null) ? 0 : stringValue.hashCode());
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
        if (stringValue == null) {
            if (other.stringValue != null) {
                return false;
            }
        } else if (!stringValue.equals(other.stringValue)) {
            return false;
        }
        return true;
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
        return cls != null && cls.toString().indexOf(matcher) != -1;
    }

    /**
     * Test if given DITA class string matches this DITA class.
     * 
     * @param classString DITA element class string
     * @return {@code true} if given class matches this class, otherwise {@code false}
     */
    public boolean matches(final String classString) {
        return classString != null && classString.indexOf(matcher) != -1;
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

}
