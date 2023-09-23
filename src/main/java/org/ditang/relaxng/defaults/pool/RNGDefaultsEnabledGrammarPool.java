/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2022 Radu Coravu
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.ditang.relaxng.defaults.pool;

import org.ditang.relaxng.defaults.RelaxNGDefaultValues;

/**
 * Caches RNG default values
 *
 * @author radu_coravu
 */
public interface RNGDefaultsEnabledGrammarPool {
  /**
   * Get RNG default values for a certain system ID
   *
   * @param systemID The main schema system ID
   * @return Returns the rng Default Values mapped to the main schema system ID.
   */
  public RelaxNGDefaultValues getRngDefaultValues(String systemID);

  /**
   * Puts the rng Default Values mapped to the main schema system ID.
   * @param systemID The main schema system ID
   * @param defaults The associated RNG defaults.
   */
  public void putRngDefaultValues(String systemID, RelaxNGDefaultValues defaults);
}
