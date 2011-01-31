/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2006 All Rights Reserved.
 */
package org.dita.dost.util;

import java.lang.reflect.Method;
import java.text.Collator;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;

import org.dita.dost.log.DITAOTJavaLogger;

/**
 * DITAOTCollator class. 
 *
 * @author Wu, Zhi Qiang
 */
public class DITAOTCollator implements Comparator {
	static HashMap<Locale, DITAOTCollator> cache = new HashMap<Locale, DITAOTCollator>();
	
	/**
	 * Return the DITAOTCollator instance, Locale.US is default.
	 * @return DITAOTCollator
	 */
	public static DITAOTCollator getInstance() {
			return getInstance(Locale.US);
	}
	
	/**
	 * Return the DITAOTCollator instance specifying Locale.
	 * @param locale the locale
	 * @return DITAOTCollator
	 */
	public static DITAOTCollator getInstance(Locale locale) {
		DITAOTCollator instance = null;
		instance = (DITAOTCollator) cache.get(locale);		
		if (instance == null) {
			instance = new DITAOTCollator(locale);
			cache.put(locale, instance);
		}
		return instance;
	}
	
	private Object collatorInstance = null;
	private Method compareMethod = null;
	private DITAOTJavaLogger logger = new DITAOTJavaLogger();
	
	/**
	 * Default Constructor
	 */
	private DITAOTCollator(){
		this(Locale.US);
	}
	
	/**
	 * Constructor specifying Locale.
	 * @param locale
	 */
	private DITAOTCollator(Locale locale) {
		init(locale);
	}
	
	/**
	 * Comparing method required to compare.
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Object source, Object target) {
		try {
			return ((Integer) compareMethod.invoke(collatorInstance, new Object[] {
					source, target})).intValue();
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	
	/**
	 * Initialization.
	 * @param locale
	 */
	private void init(Locale locale) {
		Class<?> c = null;
		
		try {
			c = Class.forName("com.ibm.icu.text.Collator");
			logger.logInfo("Using ICU collator for " + locale.toString());
		} catch (Exception e) {
			c = Collator.class;
			logger.logInfo("Using JDK collator for " + locale.toString());
		}
		
		try {
			Method m = c.getDeclaredMethod("getInstance",
					new Class[] { Locale.class });
			collatorInstance = m.invoke(null, new Object[] { locale });
			compareMethod = c.getDeclaredMethod("compare", new Class[] {
					Object.class, Object.class });
		} catch (Exception e) {
			logger.logException(e);
		}
	}

}
