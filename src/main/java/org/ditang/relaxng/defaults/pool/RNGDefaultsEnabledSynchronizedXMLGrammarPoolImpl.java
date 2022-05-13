/*
 * Copyright (c) 2019 Syncro Soft SRL - All Rights Reserved.
 *
 * This file contains proprietary and confidential source code.
 * Unauthorized copying of this file, via any medium, is strictly prohibited.
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
   * @see ro.sync.xml.parser.RNGDefaultsEnabledGrammarPool#getRngDefaultValues(java.lang.String)
   */
  @Override
  public synchronized RelaxNGDefaultValues getRngDefaultValues(String systemID) {
    return rngDefaultValues.get(systemID);
  }
  
  /**
   * @see ro.sync.xml.parser.RNGDefaultsEnabledGrammarPool#putRngDefaultValues(java.lang.String, org.ditang.relaxng.defaults.RelaxNGDefaultValues)
   */
  @Override
  public synchronized void putRngDefaultValues(String systemID, RelaxNGDefaultValues defaults) {
    rngDefaultValues.put(systemID, defaults);
  }
  
  /**
   * @see org.ditang.relaxng.pool.SynchronizedXMLGrammarPoolImpl#clear()
   */
  @Override
  public synchronized void clear() {
    rngDefaultValues.clear();
    super.clear();
  }
}
