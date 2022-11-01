/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2022 Radu Coravu
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.ditang.relaxng.defaults.pool;

import java.util.HashMap;
import java.util.Map;

import org.ditang.relaxng.defaults.RelaxNGDefaultValues;

/**
 * Caches RNG defaults.
 *
 * @author radu_coravu
 */
public class RNGDefaultsEnabledSynchronizedXMLGrammarPoolImpl
extends org.dita.dost.util.XMLGrammarPoolImplUtils implements RNGDefaultsEnabledGrammarPool {

    /**
     * Caches Relax NG default values based on the URL pointing to the RNG schemas.
     */
    private final Map<String, RelaxNGDefaultValues> rngDefaultValues = new HashMap<>();

    /**
     * @see org.ditang.relaxng.defaults.pool.RNGDefaultsEnabledGrammarPool#getRngDefaultValues(java.lang.String)
     */
    @Override
    public synchronized RelaxNGDefaultValues getRngDefaultValues(String systemID) {
        return rngDefaultValues.get(systemID);
    }

    /**
     * @see org.ditang.relaxng.defaults.pool.RNGDefaultsEnabledGrammarPool#putRngDefaultValues(java.lang.String, org.ditang.relaxng.defaults.RelaxNGDefaultValues)
     */
    @Override
    public synchronized void putRngDefaultValues(String systemID, RelaxNGDefaultValues defaults) {
        rngDefaultValues.put(systemID, defaults);
    }

    /**
     * @see org.apache.xerces.util.XMLGrammarPoolImpl#clear()
     */
    @Override
    public synchronized void clear() {
        rngDefaultValues.clear();
        super.clear();
    }

    /**
     * Get the size of the cache.
     * @return The size of the cache.
     */
    public synchronized int getCacheSize() {
        return rngDefaultValues.size();
    }
}
