/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2005, 2006 All Rights Reserved.
 */
package org.dita.dost.util;

import org.apache.xerces.impl.xs.XSDDescription;
import org.apache.xerces.util.XMLGrammarPoolImpl;
import org.apache.xerces.xni.grammars.Grammar;
import org.apache.xerces.xni.grammars.XMLGrammarDescription;

/**
 * Self implemented XML Grammar pool for grammar(schema/dtd) caching.
 * @author william
 * 
 */
public final class XMLGrammarPoolImplUtils extends XMLGrammarPoolImpl {

    private boolean gramCache = true;
    private static final Grammar[] INITIAL_GRAMMAR_SET = new Grammar[0];


    /** Constructs a grammar pool with a default number of buckets. */
    public XMLGrammarPoolImplUtils() {
        super();
    }

    /** Constructs a grammar pool with a default number of buckets. */
    public XMLGrammarPoolImplUtils(final boolean gramCache) {
        super();
        this.gramCache = gramCache;
    }

    /** Constructs a grammar pool with a specified number of buckets. */
    public XMLGrammarPoolImplUtils(final int initialCapacity) {
        super(initialCapacity);
    }
    /**
     * @see org.apache.xerces.xni.grammars.XMLGrammarPool#retrieveInitialGrammarSet(String)
     */
    @Override
    public Grammar[] retrieveInitialGrammarSet(final String grammarType) {
        return INITIAL_GRAMMAR_SET;
    }

    /**
     * Returns the hash code value for the given grammar description.
     * 
     * @param desc
     *            The grammar description
     * @return The hash code value
     */
    @Override
    public int hashCode(final XMLGrammarDescription desc) {
        if (desc instanceof XSDDescription) {
//            final String systemId = ((XSDDescription) desc).getLiteralSystemId();
//            return systemId == null ? 0 : systemId.hashCode();
        	// return -1 for XSD grammar hashcode because we want to disable XSD grammar caching
        	return -1;
        } else {
            return desc.hashCode();
        }
    }

    /**
     * This method checks whether two grammars are the same. Currently, we
     * compare the root element names(public id) for DTD grammars and the system id
     * for Schema grammars. The application can override this behaviour and add
     * its own logic.
     * 
     * @param desc1
     *            The grammar description
     * @param desc2
     *            The grammar description of the grammar to be compared to
     * @return True if the grammars are equal, otherwise false
     */
    @Override
    public boolean equals(final XMLGrammarDescription desc1,
            final XMLGrammarDescription desc2) {
        //grammar pool caching enabled.
        if (gramCache) {
            if (desc1 instanceof XSDDescription
                    && desc2 instanceof XSDDescription) {
//                return desc1.getLiteralSystemId().equals(
//                        desc2.getLiteralSystemId());
            	// always return false for XSD grammar to disable XSD grammar caching
            	return false;
            } else {
                return desc1.equals(desc2);
            }
        }else{
            //disabled
            return false;
        }
    }

}
