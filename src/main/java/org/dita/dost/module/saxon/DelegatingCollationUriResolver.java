package org.dita.dost.module.saxon;

import net.sf.saxon.lib.CollationURIResolver;

/**
 * Enables dynamic configuration of Collation URI resolvers by the Open Toolkit
 * by providing the Collator's URI.
 * @since 3.3
 *
 */
public interface DelegatingCollationUriResolver extends CollationURIResolver {
  
  /**
   * Sets the base resolver to delegate to if the resolver does not
   * handle the specified URI. This allows multiple plugins to 
   * contribute collation URI resolvers.
   * 
   * @param baseResolver The resolver to delegate to.
   */
  public void setBaseResolver(CollationURIResolver baseResolver);
  

}
