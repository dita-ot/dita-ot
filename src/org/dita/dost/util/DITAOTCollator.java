/*
 * (c) Copyright IBM Corp. 2006 All Rights Reserved.
 */
package org.dita.dost.util;

import java.lang.reflect.Method;
import java.text.Collator;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;

/**
 * Class description goes here. 
 *
 * @author Wu, Zhi Qiang
 */
public class DITAOTCollator implements Comparator {
	static HashMap cache = new HashMap();
	
	private Object collatorInstance;
	private Method compareMethod;
	
	DITAOTCollator(Locale locale) {
		init(locale);
	}
	
	private void init(Locale locale) {
		Class c = null;
		
		try {
			c = Class.forName("com.ibm.icu.text.Collator");
			System.out.println("Using ICU collator for " + locale.toString());
		} catch (Exception e) {
			c = Collator.class;
			System.out.println("Using JDK collator for " + locale.toString());
		}
		
		try {
			Method m = c.getDeclaredMethod("getInstance",
					new Class[] { Locale.class });
			collatorInstance = m.invoke(null, new Object[] { locale });
			compareMethod = c.getDeclaredMethod("compare", new Class[] {
					Object.class, Object.class });
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static synchronized DITAOTCollator getInstance() {
		return getInstance(Locale.US);
	}
	
	public static synchronized DITAOTCollator getInstance(Locale locale) {
		DITAOTCollator instance = null;		
		instance = (DITAOTCollator) cache.get(locale);		
		if (instance == null) {
			instance = new DITAOTCollator(locale);
			cache.put(locale, instance);
		}
		
		return instance;
	}
	
	public int compare(Object source, Object target) {
		try {
			return ((Integer) compareMethod.invoke(collatorInstance, new Object[] {
					source, target})).intValue();
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

}
