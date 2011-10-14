/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2011 All Rights Reserved.
 */
package org.dita.dost.util;

import static org.dita.dost.util.Constants.CONF_PROPERTIES;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.dita.dost.log.DITAOTJavaLogger;


/**
 * Global configuration object for static configurations.
 * 
 * @since 1.5.3
 */
public final class Configuration {

    private static final DITAOTJavaLogger logger = new DITAOTJavaLogger();

    /**
     * Immutable configuration properties.
     * 
     * <p>If configuration file is not found e.g. during integration, the
     * configuration will be an empty.</p>
     */
    public final static Map<String, String> configuration;
    static {
        final Properties properties = new Properties();
        InputStream configurationInputStream = null;
        try {
            final ClassLoader loader = FileUtils.class.getClassLoader();
            configurationInputStream = loader.getResourceAsStream(CONF_PROPERTIES);
            if (configurationInputStream != null) {
                properties.load(configurationInputStream);
            } else {
                final File configurationFile = new File("lib", CONF_PROPERTIES);
                if (configurationFile.exists()) {
                    configurationInputStream = new BufferedInputStream(new FileInputStream(configurationFile));
                    properties.load(configurationInputStream);
                }
            }
        } catch (final IOException e) {
            logger.logException(e);
        } finally {
            if (configurationInputStream != null) {
                try {
                    configurationInputStream.close();
                } catch (final IOException ex) {
                    logger.logException(ex);
                }
            }
        }
        final Map<String, String> c = new HashMap<String, String>();
        for (final Map.Entry<Object, Object> e: properties.entrySet()) {
            c.put(e.getKey().toString(), e.getValue().toString());
        }
        configuration = Collections.unmodifiableMap(c);
    }

    /** Private constructor to disallow instance creation. */
    private Configuration() {
    }

}
