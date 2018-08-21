/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2011 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.util;

import static org.dita.dost.platform.Integrator.CONF_PARSER_FORMAT;
import static org.dita.dost.util.Constants.*;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import org.dita.dost.platform.Integrator;


/**
 * Global configuration object for static configurations.
 *
 * @since 1.5.3
 * @author Jarno Elovirta
 */
public final class Configuration {

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

        final Properties applicationProperties = new Properties();
        try (InputStream applicationInputStream = Configuration.class.getClassLoader().getResourceAsStream(APP_CONF_PROPERTIES)) {
            if (applicationInputStream != null) {
                applicationProperties.load(applicationInputStream);
                for (final Map.Entry<Object, Object> e: applicationProperties.entrySet()) {
                    c.put(e.getKey().toString(), e.getValue().toString());
                }
            }
        } catch (final IOException e) {
            System.err.println(e.getMessage());
        }

        final Properties pluginProperties = new Properties();
        InputStream plugingConfigurationInputStream = null;
        try {
            final ClassLoader loader = FileUtils.class.getClassLoader();
            plugingConfigurationInputStream = loader.getResourceAsStream(Integrator.class.getPackage().getName() + "/" + GEN_CONF_PROPERTIES);
            if (plugingConfigurationInputStream != null) {
                pluginProperties.load(plugingConfigurationInputStream);
            } else {
                final File configurationFile = new File("config", Integrator.class.getPackage().getName() + File.separator + GEN_CONF_PROPERTIES);
                if (configurationFile.exists()) {
                    plugingConfigurationInputStream = new BufferedInputStream(new FileInputStream(configurationFile));
                    pluginProperties.load(plugingConfigurationInputStream);
                }
            }
        } catch (final IOException e) {
            System.err.println(e.getMessage());
        } finally {
            if (plugingConfigurationInputStream != null) {
                try {
                    plugingConfigurationInputStream.close();
                } catch (final IOException ex) {
                    System.err.println(ex.getMessage()) ;
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
                final File configurationFile = new File("config", CONF_PROPERTIES);
                if (configurationFile.exists()) {
                    configurationInputStream = new BufferedInputStream(new FileInputStream(configurationFile));
                    properties.load(configurationInputStream);
                }
            }
        } catch (final IOException e) {
            System.err.println(e.getMessage()) ;
        } finally {
            if (configurationInputStream != null) {
                try {
                    configurationInputStream.close();
                } catch (final IOException ex) {
                    System.err.println(ex.getMessage()) ;
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
        /** Processing fails on error. */
        STRICT,
        /** Processing continues after error and will not attempt error recovery */
        SKIP,
        /** Processing continues after error with error recovery */
        LAX
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
            System.err.println("Failed to read print transtypes from configuration, using defaults.");
            types.add(TRANS_TYPE_PDF);
        }
        printTranstype = Collections.unmodifiableList(types);
    }

    /** List of transtypes. */
    public static final List<String> transtypes;
    static {
        final List<String> types = new ArrayList<>();
        final String printTranstypes = Configuration.configuration.get(CONF_TRANSTYPES);
        if (printTranstypes != null) {
            if (printTranstypes.trim().length() > 0) {
                for (final String transtype: printTranstypes.split(CONF_LIST_SEPARATOR)) {
                    types.add(transtype.trim());
                }
            }
        } else {
            System.err.println("Failed to read transtypes from configuration, using empty list.");
        }
        transtypes = Collections.unmodifiableList(types);
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
    public static final Map<String, Map<String, Boolean>> parserFeatures;
    static {
        final Map<String, String> m = new HashMap<>();
        final Map<String, Map<String, Boolean>> f = new HashMap<>();
        for (final Map.Entry<String, String> e: configuration.entrySet()) {
            final String key = e.getKey();
            if (key.startsWith(CONF_PARSER_FORMAT) && key.indexOf('.', CONF_PARSER_FORMAT.length()) == -1) {
                final String format = key.substring(CONF_PARSER_FORMAT.length());
                final String cls = e.getValue();
                m.put(format, cls);

                final String fs = configuration.get(CONF_PARSER_FORMAT + format + ".features");
                if (fs != null) {
                    for (final String pairs : fs.split(";")) {
                        final String[] tokens = pairs.split("=");
                        Map<String, Boolean> fm = f.getOrDefault(format, new HashMap<>());
                        fm.put(tokens[0], Boolean.parseBoolean(tokens[1]));
                        f.put(format, fm);
                    }
                }
            }
        }
        parserMap = Collections.unmodifiableMap(m);
        parserFeatures = Collections.unmodifiableMap(f);
    }

    public static final Set<String> ditaFormat;
    static {
        final Set<String> s = new HashSet<>();
        for (final Map.Entry<String, String> e: configuration.entrySet()) {
            final String key = e.getKey();
            if (key.startsWith(CONF_PARSER_FORMAT) && key.indexOf('.', CONF_PARSER_FORMAT.length()) == -1) {
                s.add(key.substring(CONF_PARSER_FORMAT.length()));
            }
        }
        ditaFormat = Collections.unmodifiableSet(s);
    }

}
