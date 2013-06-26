/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */
package org.dita.dost.util;

import static org.dita.dost.util.Constants.*;

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
import org.dita.dost.platform.Integrator;


/**
 * Global configuration object for static configurations.
 * 
 * @since 1.5.3
 * @author Jarno Elovirta
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
        final Map<String, String> c = new HashMap<String, String>();
        
        final Properties pluginProperties = new Properties();
        InputStream plugingConfigurationInputStream = null;
        try {
            final ClassLoader loader = FileUtils.class.getClassLoader();
            plugingConfigurationInputStream = loader.getResourceAsStream(Integrator.class.getPackage().getName() + "/" + GEN_CONF_PROPERTIES);
            if (plugingConfigurationInputStream != null) {
                pluginProperties.load(plugingConfigurationInputStream);
            } else {
                final File configurationFile = new File("lib", Integrator.class.getPackage().getName() + File.separator + GEN_CONF_PROPERTIES);
                if (configurationFile.exists()) {
                    plugingConfigurationInputStream = new BufferedInputStream(new FileInputStream(configurationFile));
                    pluginProperties.load(plugingConfigurationInputStream);
                }
            }
        } catch (final IOException e) {
            logger.logError(e.getMessage(), e) ;
        } finally {
            if (plugingConfigurationInputStream != null) {
                try {
                    plugingConfigurationInputStream.close();
                } catch (final IOException ex) {
                    logger.logError(ex.getMessage(), ex) ;
                }
            }
        }
        for (final Map.Entry<Object, Object> e: pluginProperties.entrySet()) {
            c.put(e.getKey().toString(), e.getValue().toString());
        }
        
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
            logger.logError(e.getMessage(), e) ;
        } finally {
            if (configurationInputStream != null) {
                try {
                    configurationInputStream.close();
                } catch (final IOException ex) {
                    logger.logError(ex.getMessage(), ex) ;
                }
            }
        }
        for (final Map.Entry<Object, Object> e: properties.entrySet()) {
            c.put(e.getKey().toString(), e.getValue().toString());
        }
        
        configuration = Collections.unmodifiableMap(c);
    }

    /** Processing mode */
    public enum Mode {
        STRICT, SKIP, LAX
    }
    
    public static final Mode processingMode;
    static {
        final String mode = Configuration.configuration.get("processing-mode");
        processingMode = mode != null ? Mode.valueOf(mode.toUpperCase()) : Mode.LAX;
    }
    
    /** Private constructor to disallow instance creation. */
    private Configuration() {
    }

}
