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
import java.util.*;

import org.dita.dost.log.DITAOTJavaLogger;
import org.dita.dost.platform.Integrator;


/**
 * Global configuration object for static configurations.
 * 
 * @since 1.5.3
 * @author Jarno Elovirta
 */
public final class Configuration {

    public static final DITAOTJavaLogger logger = new DITAOTJavaLogger();
    /** Debug mode to aid in development, not intended for end users. */
    public static final boolean DEBUG = false;

    /**
     * Immutable configuration properties.
     * 
     * <p>If configuration file is not found e.g. during integration, the
     * configuration will be an empty.</p>
     */
    public final static Map<String, String> configuration;
    static {
        final Map<String, String> c = new HashMap<>();
        
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
            logger.error(e.getMessage(), e) ;
        } finally {
            if (plugingConfigurationInputStream != null) {
                try {
                    plugingConfigurationInputStream.close();
                } catch (final IOException ex) {
                    logger.error(ex.getMessage(), ex) ;
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
            logger.error(e.getMessage(), e) ;
        } finally {
            if (configurationInputStream != null) {
                try {
                    configurationInputStream.close();
                } catch (final IOException ex) {
                    logger.error(ex.getMessage(), ex) ;
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
    
    /** Private constructor to disallow instance creation. */
    private Configuration() {
    }
    
    /** List of print-oriented transtypes. */
    public static final List<String> printTranstype;
    static {
        final List<String> types = new ArrayList<>();
        final String printTranstypes = Configuration.configuration.get(CONF_PRINT_TRANSTYPES);
        if (printTranstypes != null) {
            if (printTranstypes.trim().length() > 0) {
                for (final String transtype: printTranstypes.split(CONF_LIST_SEPARATOR)) {
                    types.add(transtype.trim());
                }
            }
        } else {
            new DITAOTJavaLogger().error("Failed to read print transtypes from configuration, using defaults.");
            types.add(TRANS_TYPE_PDF);
        }
        printTranstype = Collections.unmodifiableList(types);
    }

    /** Map of plug-in resource directories. */
    public static final Map<String, File> pluginResourceDirs;
    static {
        final Map<String, File> ps = new HashMap<>();
        for (final Map.Entry<String, String> e: configuration.entrySet()) {
            final String key = e.getKey();
            if (key.startsWith("plugin.") && key.endsWith(".dir")) {
                ps.put(key.substring(7, key.length() - 4), new File(e.getValue()));
            }
        }
        pluginResourceDirs = Collections.unmodifiableMap(ps);
    }
    
    public static final Map<String, String> parserMap;
    static {
        final Map<String, String> m = new HashMap<>();
        for (final Map.Entry<String, String> e: configuration.entrySet()) {
            final String key = e.getKey();
            if (key.startsWith("parser.")) {
                m.put(key.substring(7), e.getValue());
            }
        }
        parserMap = Collections.unmodifiableMap(m);
    }

    public static final Set<String> ditaFormat;
    static {
        final Set<String> s = new HashSet<>();
        for (final Map.Entry<String, String> e: configuration.entrySet()) {
            final String key = e.getKey();
            if (key.startsWith("parser.")) {
                s.add(key.substring(7));
            }
        }
        ditaFormat = Collections.unmodifiableSet(s);
    }

}