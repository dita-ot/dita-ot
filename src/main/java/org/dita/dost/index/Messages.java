/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2007 All Rights Reserved.
 */
package org.dita.dost.index;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
/**
 * Class to store messages.
 *
 */
public final class Messages {
    /**message bundle name.*/
    private static final String BUNDLE_NAME = "org.dita.dost.index.messages"; //$NON-NLS-1$

    /**
     * private constructor.
     */
    private Messages() {
    }
    /**
     * get specific message by key and locale.
     * @param key key
     * @param msgLocale locale
     * @return string
     */
    public static String getString (final String key, final Locale msgLocale){
        /*read message resource file.*/
        ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME, msgLocale);
        try {
            return RESOURCE_BUNDLE.getString(key);
        } catch (final MissingResourceException e) {
            return key;
        }
    }
}
