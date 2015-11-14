/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
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

/**
 * DITAOTCollator class.
 *
 * @author Wu, Zhi Qiang
 */
public final class DITAOTCollator implements Comparator {
    private static final HashMap<Locale, DITAOTCollator> cache = new HashMap<>();

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
    public static DITAOTCollator getInstance(final Locale locale) {
        if (locale == null) {
            throw new NullPointerException("Locale may not be null");
        }
        DITAOTCollator instance = null;
        instance = cache.get(locale);
        if (instance == null) {
            instance = new DITAOTCollator(locale);
            cache.put(locale, instance);
        }
        return instance;
    }

    private Object collatorInstance = null;
    private Method compareMethod = null;

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
    private DITAOTCollator(final Locale locale) {
        init(locale);
    }

    /**
     * Comparing method required to compare.
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(final Object source, final Object target) {
        try {
            return (Integer) compareMethod.invoke(collatorInstance, source, target);
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Initialization.
     * @param locale
     */
    private void init(final Locale locale) {
        Class<?> c = null;

        try {
            c = Class.forName("com.ibm.icu.text.Collator");
        } catch (final Exception e) {
            c = Collator.class;
        }

        try {
            final Method m = c.getDeclaredMethod("getInstance",
                    Locale.class);
            collatorInstance = m.invoke(null, locale);
            compareMethod = c.getDeclaredMethod("compare", Object.class, Object.class);
        } catch (final Exception e) {
            throw new RuntimeException("Failed to initialize collator: " + e.getMessage(), e);
        }
    }

}
