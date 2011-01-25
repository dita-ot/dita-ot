/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/**
 * Copyright (c) 2009 Really Strategies, Inc.
 */
package org.dita.dost.reader;

import org.apache.xerces.xni.grammars.XMLGrammarPool;
import org.dita.dost.util.XMLGrammarPoolImplUtils;

/**
 * Manages creation and access to a master Xerces grammar pool.
 * The grammar pool is managed as a ThreadLocal variable so it can
 * be used across Ant task invocations.
 */
public class GrammarPoolManager {
	
	//flag whether use grammar caching.
	private static String gramCache;

	public static XMLGrammarPool initializeGrammarPool() {
		XMLGrammarPool pool = null;
		try {
		    pool = new XMLGrammarPoolImplUtils(gramCache);
		    //set grammar caching flag
		    
		}
		catch (Exception e) {
			System.out.println("Failed to create Xerces grammar pool for caching DTDs and schemas");
		}
		grammarPool.set(pool);
		return pool;
	}

	private static ThreadLocal<XMLGrammarPool> grammarPool = new ThreadLocal<XMLGrammarPool>() {
		protected synchronized XMLGrammarPool initialvalue() {
			XMLGrammarPool grammarPool = initializeGrammarPool();
			set(grammarPool);
			return grammarPool;
		}

		
	};
	
	

	/**
	 * @return 
	 * 
	 */
	public static XMLGrammarPool getGrammarPool() {
		XMLGrammarPool pool = grammarPool.get();
		if (pool == null)
			pool = initializeGrammarPool();
		return pool;
	}

	public static void setGramCache(String gramCache) {
		GrammarPoolManager.gramCache = gramCache;
	}

}
	  	 
