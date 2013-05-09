/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2005, 2006 All Rights Reserved.
 */
package org.dita.dost.platform;

import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.Configuration.*;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import org.xml.sax.SAXException;

import org.xml.sax.ErrorHandler;

import org.xml.sax.SAXParseException;

import org.dita.dost.log.DITAOTJavaLogger;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.util.FileUtils;
import org.dita.dost.util.StringUtils;
import org.xml.sax.XMLReader;

/**
 * Integrator is the main class to control and excute the integration of the
 * toolkit and different plug-ins.
 * 
 * @author Zhang, Yuan Peng
 */
public final class Integrator {

    private static final String CONF_PLUGIN_IGNORES = "plugin.ignores";
    private static final String CONF_PLUGIN_DIRS = "plugindirs";
    /** Feature name for supported image extensions. */
    public static final String FEAT_TOPIC_EXTENSIONS = "dita.topic.extensions";
    /** Feature name for supported image extensions. */
    public static final String FEAT_MAP_EXTENSIONS = "dita.map.extensions";
    /** Feature name for supported image extensions. */
    public static final String FEAT_IMAGE_EXTENSIONS = "dita.image.extensions";
    /** Feature name for supported image extensions. */
    public static final String FEAT_HTML_EXTENSIONS = "dita.html.extensions";
    /** Feature name for supported resource file extensions. */
    public static final String FEAT_RESOURCE_EXTENSIONS = "dita.resource.extensions";
    /** Feature name for print transformation types. */
    public static final String FEAT_PRINT_TRANSTYPES = "dita.transtype.print";

    public static final String FEAT_VALUE_SEPARATOR = ",";
    public static final String PARAM_VALUE_SEPARATOR = ";";
    public static final String PARAM_NAME_SEPARATOR = "=";

    private static final Pattern ID_PATTERN = Pattern.compile("[0-9a-zA-Z_\\-]+(?:\\.[0-9a-zA-Z_\\-]+)*");
    private static final Pattern VERSION_PATTERN = Pattern.compile("\\d+(?:\\.\\d+(?:\\.\\d+(?:\\.[0-9a-zA-Z_\\-])?)?)?");

    /** Plugin table which contains detected plugins. */
    private final Map<String, Features> pluginTable;
    private final Set<String> templateSet = new HashSet<String>(INT_16);
    private File ditaDir;
    /** Plugin configuration file. */
    private final Set<File> descSet;
    private final XMLReader reader;
    private DITAOTLogger logger;
    private final Set<String> loadedPlugin;
    private final Hashtable<String, String> featureTable;
    @Deprecated
    private File propertiesFile;
    private final Set<String> extensionPoints;
    private boolean strict = false;

    private Properties properties;

    /**
     * Execute point of Integrator.
     */
    public void execute() {
        if (logger == null) {
            logger = new DITAOTJavaLogger();
        }

        // Read the properties file, if it exists.
        properties = new Properties();
        if (propertiesFile != null) {
            FileInputStream propertiesStream = null;
            try {
                propertiesStream = new FileInputStream(propertiesFile);
                properties.load(propertiesStream);
            } catch (final Exception e) {
                if (strict) {
                    throw new RuntimeException(e);
                } else {
                    logger.logError(e.getMessage(), e) ;
                }
            } finally {
                if (propertiesStream != null) {
                    try {
                        propertiesStream.close();
                    } catch (final IOException e) {
                        logger.logError(e.getMessage(), e) ;
                    }
                }
            }
        }
        if (!properties.containsKey(CONF_PLUGIN_DIRS)) {
            properties.setProperty(CONF_PLUGIN_DIRS, configuration.containsKey(CONF_PLUGIN_DIRS) ? configuration.get(CONF_PLUGIN_DIRS) : "plugins;demo");
        }
        if (!properties.containsKey(CONF_PLUGIN_IGNORES)) {
            properties.setProperty(CONF_PLUGIN_IGNORES, configuration.containsKey(CONF_PLUGIN_IGNORES) ? configuration.get(CONF_PLUGIN_IGNORES) : "");
        }

        // Get the list of plugin directories from the properties.
        final String[] pluginDirs = properties.getProperty(CONF_PLUGIN_DIRS).split(PARAM_VALUE_SEPARATOR);

        final Set<String> pluginIgnores = new HashSet<String>();
        if (properties.getProperty(CONF_PLUGIN_IGNORES) != null) {
            pluginIgnores.addAll(Arrays.asList(properties.getProperty(CONF_PLUGIN_IGNORES).split(PARAM_VALUE_SEPARATOR)));
        }

        for (final String tmpl : properties.getProperty(CONF_TEMPLATES, "").split(PARAM_VALUE_SEPARATOR)) {
            final String t = tmpl.trim();
            if (t.length() != 0) {
                templateSet.add(t);
            }
        }

        for (final String pluginDir2 : pluginDirs) {
            File pluginDir = new File(pluginDir2);
            if (!pluginDir.isAbsolute()) {
                pluginDir = new File(ditaDir, pluginDir.getPath());
            }
            final File[] pluginFiles = pluginDir.listFiles();

            for (int i = 0; (pluginFiles != null) && (i < pluginFiles.length); i++) {
                final File f = pluginFiles[i];
                final File descFile = new File(pluginFiles[i], "plugin.xml");
                if (pluginFiles[i].isDirectory() && !pluginIgnores.contains(f.getName()) && descFile.exists()) {
                    descSet.add(descFile);
                }
            }
        }

        parsePlugin();
        integrate();
    }

    /**
     * Generate and process plugin files.
     */
    private void integrate() {
        // Collect information for each feature id and generate a feature table.
        final FileGenerator fileGen = new FileGenerator(featureTable, pluginTable);
        fileGen.setLogger(logger);
        for (final String currentPlugin : pluginTable.keySet()) {
            loadPlugin(currentPlugin);
        }

        // generate the files from template
        for (final String template : templateSet) {
            final File templateFile = new File(ditaDir, template);
            logger.logDebug("Process template " + templateFile.getPath());
            fileGen.generate(templateFile);
        }

        // generate configuration properties
        final Properties configuration = new Properties();
        // image extensions, support legacy property file extension
        final Set<String> imgExts = new HashSet<String>();
        for (final String ext : properties.getProperty(CONF_SUPPORTED_IMAGE_EXTENSIONS, "").split(CONF_LIST_SEPARATOR)) {
            final String e = ext.trim();
            if (e.length() != 0) {
                imgExts.add(e);
            }
        }
        if (featureTable.containsKey(FEAT_IMAGE_EXTENSIONS)) {
            for (final String ext : featureTable.get(FEAT_IMAGE_EXTENSIONS).split(FEAT_VALUE_SEPARATOR)) {
                final String e = ext.trim();
                if (e.length() != 0) {
                    imgExts.add(e);
                }
            }
        }
        configuration.put(CONF_SUPPORTED_IMAGE_EXTENSIONS, StringUtils.assembleString(imgExts, CONF_LIST_SEPARATOR));
        // extensions
        configuration.put(CONF_SUPPORTED_TOPIC_EXTENSIONS, readExtensions(FEAT_TOPIC_EXTENSIONS));
        configuration.put(CONF_SUPPORTED_MAP_EXTENSIONS, readExtensions(FEAT_MAP_EXTENSIONS));
        configuration.put(CONF_SUPPORTED_HTML_EXTENSIONS, readExtensions(FEAT_HTML_EXTENSIONS));
        configuration.put(CONF_SUPPORTED_RESOURCE_EXTENSIONS, readExtensions(FEAT_RESOURCE_EXTENSIONS));

        // print transtypes
        final Set<String> printTranstypes = new HashSet<String>();
        if (featureTable.containsKey(FEAT_PRINT_TRANSTYPES)) {
            for (final String ext : featureTable.get(FEAT_PRINT_TRANSTYPES).split(FEAT_VALUE_SEPARATOR)) {
                final String e = ext.trim();
                if (e.length() != 0) {
                    printTranstypes.add(e);
                }
            }
        }
        // support legacy property
        final String printTranstypeValue = properties.getProperty(CONF_PRINT_TRANSTYPES);
        if (printTranstypeValue != null) {
            printTranstypes.addAll(Arrays.asList(printTranstypeValue.split(PARAM_VALUE_SEPARATOR)));
        }
        configuration.put(CONF_PRINT_TRANSTYPES, StringUtils.assembleString(printTranstypes, CONF_LIST_SEPARATOR));

        OutputStream out = null;
        try {
            final File outFile = new File(ditaDir, "lib" + File.separator + getClass().getPackage().getName() + File.separator + GEN_CONF_PROPERTIES);
            if (!(outFile.getParentFile().exists()) && !outFile.getParentFile().mkdirs()) {
                throw new RuntimeException("Failed to make directory " + outFile.getParentFile().getAbsolutePath());
            }
            logger.logDebug("Generate configuration properties " + outFile.getPath());
            out = new BufferedOutputStream(new FileOutputStream(outFile));
            configuration.store(out, "DITA-OT runtime configuration, do not edit manually");
        } catch (final Exception e) {
            if (strict) {
                throw new RuntimeException("Failed to write configuration properties: " + e.getMessage(), e);
            } else {
                logger.logError(e.getMessage(), e) ;
            }
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (final IOException e) {
                    logger.logError(e.getMessage(), e) ;
                }
            }
        }
    }

    /**
     * Read plug-in feature.
     * 
     * @param featureName plug-in feature name
     * @return combined list of values
     */
    private String readExtensions(final String featureName) {
        final Set<String> exts = new HashSet<String>();
        if (featureTable.containsKey(featureName)) {
            for (final String ext : featureTable.get(featureName).split(FEAT_VALUE_SEPARATOR)) {
                final String e = ext.trim();
                if (e.length() != 0) {
                    exts.add(e);
                }
            }
        }
        return StringUtils.assembleString(exts, CONF_LIST_SEPARATOR);
    }

    /**
     * Load the plug-ins and aggregate them by feature and fill into feature
     * table.
     * 
     * @param plugin plugin ID
     * @return {@code true}> if plugin was loaded, otherwise {@code false}
     */
    private boolean loadPlugin(final String plugin) {
        if (checkPlugin(plugin)) {
            final Features pluginFeatures = pluginTable.get(plugin);
            final Map<String, String> featureSet = pluginFeatures.getAllFeatures();
            for (final Map.Entry<String, String> currentFeature : featureSet.entrySet()) {
                if (!extensionPoints.contains(currentFeature.getKey())) {
                    final String msg = "Plug-in " + plugin + " uses an undefined extension point "
                            + currentFeature.getKey();
                    if (strict) {
                        throw new RuntimeException(msg);
                    } else {
                        logger.logDebug(msg);
                    }
                }
                if (featureTable.containsKey(currentFeature.getKey())) {
                    final String value = featureTable.remove(currentFeature.getKey());
                    featureTable.put(currentFeature.getKey(), new StringBuffer(value).append(FEAT_VALUE_SEPARATOR)
                            .append(currentFeature.getValue()).toString());
                } else {
                    featureTable.put(currentFeature.getKey(), currentFeature.getValue());
                }
            }

            for (final String templateName : pluginFeatures.getAllTemplates()) {
                templateSet.add(FileUtils.getRelativePath(ditaDir + File.separator + "dummy",
                        pluginFeatures.getLocation() + File.separator + templateName));
            }
            loadedPlugin.add(plugin);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Check whether the plugin can be loaded.
     * 
     * @param currentPlugin plugin ID
     * @return {@code true} if plugin can be loaded, otherwise {@code false}
     */
    private boolean checkPlugin(final String currentPlugin) {
        final Features pluginFeatures = pluginTable.get(currentPlugin);
        final Iterator<PluginRequirement> iter = pluginFeatures.getRequireListIter();
        // check whether dependcy is satisfied
        while (iter.hasNext()) {
            boolean anyPluginFound = false;
            final PluginRequirement requirement = iter.next();
            final Iterator<String> requiredPluginIter = requirement.getPlugins();
            while (requiredPluginIter.hasNext()) {
                // Iterate over all alternatives in plugin requirement.
                final String requiredPlugin = requiredPluginIter.next();
                if (pluginTable.containsKey(requiredPlugin)) {
                    if (!loadedPlugin.contains(requiredPlugin)) {
                        // required plug-in is not loaded
                        loadPlugin(requiredPlugin);
                    }
                    // As soon as any plugin is found, it's OK.
                    anyPluginFound = true;
                }
            }
            if (!anyPluginFound && requirement.getRequired()) {
                // not contain any plugin required by current plugin
                final String msg = MessageUtils.getInstance().getMessage("DOTJ020W", requirement.toString(), currentPlugin).toString();
                if (strict) {
                    throw new RuntimeException(msg);
                } else {
                    logger.logWarn(msg);
                }
                return false;
            }
        }
        return true;
    }

    /**
     * Parse plugin configuration files.
     */
    private void parsePlugin() {
        if (!descSet.isEmpty()) {
            for (final File descFile : descSet) {
                logger.logDebug("Read plug-in configuration " + descFile.getPath());
                parseDesc(descFile);
            }
        }
    }

    /**
     * Parse plugin configuration file
     * 
     * @param descFile plugin configuration
     */
    private void parseDesc(final File descFile) {
        try {
            final DescParser parser = new DescParser(descFile.getParentFile(), ditaDir);
            reader.setContentHandler(parser);
            reader.setErrorHandler(new ErrorHandler() {
                @Override
                public void error(final SAXParseException e) throws SAXException {
                    throw e;
                }
                @Override
                public void fatalError(final SAXParseException e) throws SAXException {
                    throw e;
                }
                @Override
                public void warning(final SAXParseException e) throws SAXException {
                    throw e;
                }
            });
            reader.parse(descFile.getAbsolutePath());
            final Features f = parser.getFeatures();
            final String id = f.getPluginId();
            validatePlugin(f);
            setDefaultValues(f);
            extensionPoints.addAll(f.getExtensionPoints().keySet());
            pluginTable.put(id, f);
        } catch (final SAXParseException e) {
            final RuntimeException ex = new RuntimeException("Failed to parse " + descFile.getAbsolutePath() + ": " + e.getMessage(), e);
            if (strict) {
                throw ex;
            } else {
                logger.logError(ex.getMessage(), ex) ;
            }
        } catch (final Exception e) {
            if (strict) {
                throw new RuntimeException(e);
            } else {
                logger.logError(e.getMessage(), e) ;
            }
        }
    }

    /**
     * Validate plug-in configuration.
     * 
     * Follow OSGi symbolic name syntax rules:
     * 
     * <pre>
     * digit         ::= [0..9]
     * alpha         ::= [a..zA..Z]
     * alphanum      ::= alpha | digit
     * token         ::= ( alphanum | '_' | '-' )+
     * symbolic-name ::= token('.'token)*
     * </pre>
     * 
     * Follow OSGi bundle version syntax rules:
     * 
     * <pre>
     * version   ::= major( '.' minor ( '.' micro ( '.' qualifier )? )? )?
     * major     ::= number
     * minor     ::=number
     * micro     ::=number
     * qualifier ::= ( alphanum | '_' | '-' )+
     * </pre>
     * 
     * @param f Features to validate
     */
    private void validatePlugin(final Features f) {
        final String id = f.getPluginId();
        if (!ID_PATTERN.matcher(id).matches()) {
            final String msg = "Plug-in ID '" + id + "' doesn't follow recommended syntax rules, support for nonconforming IDs may be removed in future releases.";
            if (strict) {
                throw new IllegalArgumentException(msg);
            } else {
                logger.logWarn(msg);
            }
        }
        final String version = f.getFeature("package.version");
        if (version != null && !VERSION_PATTERN.matcher(version).matches()) {
            final String msg = "Plug-in version '" + version + "' doesn't follow recommended syntax rules, support for nonconforming version may be removed in future releases.";
            if (strict) {
                throw new IllegalArgumentException(msg);
            } else {
                logger.logWarn(msg);
            }
        }
    }

    /**
     * Set default values.
     * 
     * @param f Features to set defaults to
     */
    private void setDefaultValues(final Features f) {
        if (f.getFeature("package.version") == null) {
            f.addFeature("package.version", "0.0.0", null);
        }
    }

    /**
     * Default Constructor.
     */
    public Integrator() {
        pluginTable = new HashMap<String, Features>(INT_16);
        descSet = new HashSet<File>(INT_16);
        loadedPlugin = new HashSet<String>(INT_16);
        featureTable = new Hashtable<String, String>(INT_16);
        extensionPoints = new HashSet<String>();
        try {
            reader = StringUtils.getXMLReader();
        } catch (final Exception e) {
            throw new RuntimeException("Failed to initialize XML parser: " + e.getMessage(), e);
        }
    }

    /**
     * Set the ditaDir.
     * 
     * @param ditadir dita directory
     */
    public void setDitaDir(final File ditadir) {
        ditaDir = ditadir;
    }


    /**
     * Set the properties file.
     * 
     * @param propertiesfile properties file
     */
    public void setProperties(final File propertiesfile) {
        propertiesFile = propertiesfile;
    }

    /**
     * Setter for strict/lax mode.
     * 
     * @param strict {@code true} for strict mode, {@code false} for lax mode
     */
    public void setStrict(final boolean strict) {
        this.strict = strict;
    }

    /**
     * Set logger.
     * 
     * @param logger logger instance
     */
    public void setLogger(final DITAOTLogger logger) {
        this.logger = logger;
    }

    /**
     * Get all and combine extension values
     * 
     * @param featureTable plugin features
     * @param extension extension ID
     * @return combined extension value, {@code null} if no value available
     */
    static final String getValue(final Map<String, Features> featureTable, final String extension) {
        final List<String> buf = new ArrayList<String>();
        for (final Features f : featureTable.values()) {
            final String v = f.getFeature(extension);
            if (v != null) {
                buf.add(v);
            }
        }
        if (buf.isEmpty()) {
            return null;
        } else {
            return StringUtils.assembleString(buf, ",");
        }
    }

    /**
     * Command line interface for testing.
     * 
     * @param args arguments
     */
    public static void main(final String[] args) {
        final Integrator abc = new Integrator();
        final File currentDir = new File(".");
        abc.setDitaDir(currentDir);
        abc.setProperties(new File("integrator.properties"));
        abc.execute();
    }

}
