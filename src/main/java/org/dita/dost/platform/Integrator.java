/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2005, 2006 All Rights Reserved.
 */
package org.dita.dost.platform;

import org.dita.dost.log.DITAOTJavaLogger;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.util.Configuration;
import org.dita.dost.util.FileUtils;
import org.dita.dost.util.StringUtils;
import org.dita.dost.util.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;
import static javax.xml.XMLConstants.XML_NS_PREFIX;
import static javax.xml.XMLConstants.XML_NS_URI;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.dita.dost.platform.PluginParser.FEATURE_ELEM;
import static org.dita.dost.platform.PluginParser.FEATURE_ID_ATTR;
import static org.dita.dost.util.Configuration.configuration;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.URLUtils.getRelativePath;
import static org.dita.dost.util.URLUtils.toFile;

/**
 * Integrator is the main class to control and excute the integration of the
 * toolkit and different plug-ins.
 * 
 * @author Zhang, Yuan Peng
 */
public final class Integrator {

    private static final String CONF_PLUGIN_ORDER = "plugin.order";
    private static final String CONF_PLUGIN_IGNORES = "plugin.ignores";
    private static final String CONF_PLUGIN_DIRS = "plugindirs";
    /** Feature name for supported image extensions. */
    private static final String FEAT_IMAGE_EXTENSIONS = "dita.image.extensions";
    /** Feature name for supported image extensions. */
    private static final String FEAT_HTML_EXTENSIONS = "dita.html.extensions";
    /** Feature name for supported resource file extensions. */
    private static final String FEAT_RESOURCE_EXTENSIONS = "dita.resource.extensions";
    /** Feature name for print transformation types. */
    private static final String FEAT_PRINT_TRANSTYPES = "dita.transtype.print";
    private static final String FEAT_LIB_EXTENSIONS = "dita.conductor.lib.import";
    private static final String ELEM_PLUGINS = "plugins";

    public static final String FEAT_VALUE_SEPARATOR = ",";
    private static final String PARAM_VALUE_SEPARATOR = ";";

    public static final Pattern ID_PATTERN = Pattern.compile("[0-9a-zA-Z_\\-]+(?:\\.[0-9a-zA-Z_\\-]+)*");
    public static final Pattern VERSION_PATTERN = Pattern.compile("\\d+(?:\\.\\d+(?:\\.\\d+(?:\\.[0-9a-zA-Z_\\-]+)?)?)?");

    /** Plugin table which contains detected plugins. */
    private final Map<String, Features> pluginTable;
    private final Set<String> templateSet = new HashSet<>(16);
    private final File ditaDir;
    /** Plugin configuration file. */
    private final Set<File> descSet;
    private final XMLReader reader;
    private final Document pluginsDoc;
    private final PluginParser parser;
    private DITAOTLogger logger;
    private final Set<String> loadedPlugin;
    private final Hashtable<String, List<String>> featureTable;
    @Deprecated
    private File propertiesFile;
    private final Set<String> extensionPoints;
    private boolean strict = false;
    private final Map<String, Integer> pluginOrder = new HashMap<>();
    private Properties properties;

    /**
     * Default Constructor.
     */
    public Integrator(final File ditaDir) {
        this.ditaDir = ditaDir;
        pluginTable = new HashMap<>(16);
        descSet = new HashSet<>(16);
        loadedPlugin = new HashSet<>(16);
        featureTable = new Hashtable<>(16);
        extensionPoints = new HashSet<>();
        try {
            reader = XMLUtils.getXMLReader();
        } catch (final Exception e) {
            throw new RuntimeException("Failed to initialize XML parser: " + e.getMessage(), e);
        }
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
        parser = new PluginParser(ditaDir);
        try {
            pluginsDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        } catch (final ParserConfigurationException e) {
            throw new RuntimeException("Failed to create compound document: " + e.getMessage(), e);
        }
//        pluginsDoc.setResult(new StreamResult(new File(ditaDir, RESOURCES_DIR + File.separator + "plugins.xml")));
    }

    /**
     * Execute point of Integrator.
     */
    public void execute() throws Exception {
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
                    logger.error(e.getMessage(), e) ;
                }
            } finally {
                if (propertiesStream != null) {
                    try {
                        propertiesStream.close();
                    } catch (final IOException e) {
                        logger.error(e.getMessage(), e) ;
                    }
                }
            }
        } else {
            properties.putAll(Configuration.configuration);
        }
        if (!properties.containsKey(CONF_PLUGIN_DIRS)) {
            properties.setProperty(CONF_PLUGIN_DIRS, configuration.containsKey(CONF_PLUGIN_DIRS) ? configuration.get(CONF_PLUGIN_DIRS) : "plugins;demo");
        }
        if (!properties.containsKey(CONF_PLUGIN_IGNORES)) {
            properties.setProperty(CONF_PLUGIN_IGNORES, configuration.containsKey(CONF_PLUGIN_IGNORES) ? configuration.get(CONF_PLUGIN_IGNORES) : "");
        }

        // Get the list of plugin directories from the properties.
        final String[] pluginDirs = properties.getProperty(CONF_PLUGIN_DIRS).split(PARAM_VALUE_SEPARATOR);

        final Set<String> pluginIgnores = new HashSet<>();
        if (properties.getProperty(CONF_PLUGIN_IGNORES) != null) {
            pluginIgnores.addAll(Arrays.asList(properties.getProperty(CONF_PLUGIN_IGNORES).split(PARAM_VALUE_SEPARATOR)));
        }

        final String pluginOrderProperty = properties.getProperty(CONF_PLUGIN_ORDER);
        if (pluginOrderProperty != null) {
            final List<String> plugins = asList(pluginOrderProperty.trim().split("\\s+"));
            Collections.reverse(plugins);
            int priority = 1;
            for (final String plugin: plugins) {
                pluginOrder.put(plugin, priority++);
            }
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
    private void integrate() throws Exception {
        writePlugins();

        // Collect information for each feature id and generate a feature table.
        final FileGenerator fileGen = new FileGenerator(featureTable, pluginTable);
        fileGen.setLogger(logger);
        for (final String currentPlugin : orderPlugins(pluginTable.keySet())) {
            loadPlugin(currentPlugin);
        }

        // generate the files from template
        for (final String template : templateSet) {
            final File templateFile = new File(ditaDir, template);
            logger.debug("Process template " + templateFile.getPath());
            fileGen.generate(templateFile);
        }

        // generate configuration properties
        final Properties configuration = new Properties();
        // image extensions, support legacy property file extension
        final Set<String> imgExts = new HashSet<>();
        for (final String ext : properties.getProperty(CONF_SUPPORTED_IMAGE_EXTENSIONS, "").split(CONF_LIST_SEPARATOR)) {
            final String e = ext.trim();
            if (e.length() != 0) {
                imgExts.add(e);
            }
        }
        if (featureTable.containsKey(FEAT_IMAGE_EXTENSIONS)) {
            for (final String ext : featureTable.get(FEAT_IMAGE_EXTENSIONS)) {
                final String e = ext.trim();
                if (e.length() != 0) {
                    imgExts.add(e);
                }
            }
        }
        configuration.put(CONF_SUPPORTED_IMAGE_EXTENSIONS, StringUtils.join(imgExts, CONF_LIST_SEPARATOR));
        // extensions
        configuration.put(CONF_SUPPORTED_HTML_EXTENSIONS, readExtensions(FEAT_HTML_EXTENSIONS));
        configuration.put(CONF_SUPPORTED_RESOURCE_EXTENSIONS, readExtensions(FEAT_RESOURCE_EXTENSIONS));

        // print transtypes
        final Set<String> printTranstypes = new HashSet<>();
        if (featureTable.containsKey(FEAT_PRINT_TRANSTYPES)) {
            for (final String ext : featureTable.get(FEAT_PRINT_TRANSTYPES)) {
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
        configuration.put(CONF_PRINT_TRANSTYPES, StringUtils.join(printTranstypes, CONF_LIST_SEPARATOR));

        for (final Entry<String, Features> e: pluginTable.entrySet()) {
            final Features f = e.getValue();
            final String name = "plugin."+ e.getKey() + ".dir";
            final List<String> baseDirValues = f.getFeature("dita.basedir-resource-directory");
            if (Boolean.parseBoolean(baseDirValues == null || baseDirValues.isEmpty() ? null : baseDirValues.get(0))) {
                //configuration.put(name, ditaDir.getAbsolutePath());
                configuration.put(name, ".");
            } else {
                configuration.put(name, FileUtils.getRelativePath(new File(ditaDir, "dummy"), f.getPluginDir()).getPath());
            }
        }
        configuration.putAll(getParserConfiguration());
        
        OutputStream out = null;
        try {
            final File outFile = new File(ditaDir, "lib" + File.separator + getClass().getPackage().getName() + File.separator + GEN_CONF_PROPERTIES);
            if (!(outFile.getParentFile().exists()) && !outFile.getParentFile().mkdirs()) {
                throw new RuntimeException("Failed to make directory " + outFile.getParentFile().getAbsolutePath());
            }
            logger.debug("Generate configuration properties " + outFile.getPath());
            out = new BufferedOutputStream(new FileOutputStream(outFile));
            configuration.store(out, "DITA-OT runtime configuration, do not edit manually");
        } catch (final Exception e) {
            if (strict) {
                throw new RuntimeException("Failed to write configuration properties: " + e.getMessage(), e);
            } else {
                logger.error(e.getMessage(), e) ;
            }
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (final IOException e) {
                    logger.error(e.getMessage(), e) ;
                }
            }
        }

        final Collection<File> jars = featureTable.containsKey(FEAT_LIB_EXTENSIONS) ? relativize(new LinkedHashSet<>(featureTable.get(FEAT_LIB_EXTENSIONS))) : Collections.EMPTY_SET;
        writeEnvShell(jars);
        writeEnvBatch(jars);
    }

    private Iterable<String> orderPlugins(final Set<String> ids) {
        final List<String> res = new ArrayList<>(ids);
        Collections.sort(res, new Comparator<String>() {
            @Override
            public int compare(final String s1, final String s2) {
                final int score1 = pluginOrder.containsKey(s1) ? pluginOrder.get(s1) : 0;
                final int score2 = pluginOrder.containsKey(s2) ? pluginOrder.get(s2) : 0;
                if (score1 < score2) {
                    return 1;
                } else if (score1 > score2) {
                    return -1;
                } else {
                    return s1.compareTo(s2);
                }
            }
        });
        return res;
    }

    private Map<String, String> getParserConfiguration() {
        final Map<String, String> res = new HashMap<>();
        final NodeList features = pluginsDoc.getElementsByTagName(FEATURE_ELEM);
        for (int i = 0; i < features.getLength(); i++) {
            final Element feature = (Element) features.item(i);
            if (feature.getAttribute(FEATURE_ID_ATTR).equals("dita.parser")) {
                final NodeList parsers = feature.getElementsByTagName("parser");
                for (int j = 0; j < parsers.getLength(); j++) {
                    final Element parser = (Element) parsers.item(j);
                    res.put("parser." + parser.getAttribute("format"), parser.getAttribute("class"));
                }
            }
        }
        return res;
    }

    private Collection<File> relativize(final Collection<String> src) {
        final Collection<File> res = new ArrayList<>(src.size());
        final File base = new File(ditaDir, "dummy");
        for (final String lib: src) {
            res.add(FileUtils.getRelativePath(base, toFile(lib)));
        }
        return res;
    }

    private void writeEnvShell(final Collection<File> jars) {
        Writer out = null;
        try {
            final File outFile = new File(ditaDir, "resources" + File.separator + "env.sh");
            if (!(outFile.getParentFile().exists()) && !outFile.getParentFile().mkdirs()) {
                throw new RuntimeException("Failed to make directory " + outFile.getParentFile().getAbsolutePath());
            }
            logger.debug("Generate environment shell " + outFile.getPath());
            out = new BufferedWriter(new FileWriter(outFile));

            out.write("#!/bin/sh\n");
            for (final File relativeLib: jars) {
                out.write("CLASSPATH=\"$CLASSPATH:");
                if (!relativeLib.isAbsolute()) {
                    out.write("$DITA_HOME" + UNIX_SEPARATOR);
                }
                out.write(relativeLib.toString().replace(File.separator, UNIX_SEPARATOR));
                out.write("\"\n");
            }
        } catch (final Exception e) {
            if (strict) {
                throw new RuntimeException("Failed to write environment shell: " + e.getMessage(), e);
            } else {
                logger.error(e.getMessage(), e) ;
            }
        } finally {
            closeQuietly(out);
        }
    }

    private void writeEnvBatch(final Collection<File> jars) {
        Writer out = null;
        try {
            final File outFile = new File(ditaDir, "resources" + File.separator + "env.bat");
            if (!(outFile.getParentFile().exists()) && !outFile.getParentFile().mkdirs()) {
                throw new RuntimeException("Failed to make directory " + outFile.getParentFile().getAbsolutePath());
            }
            logger.debug("Generate environment batch " + outFile.getPath());
            out = new BufferedWriter(new FileWriter(outFile));

            for (final File relativeLib: jars) {
                out.write("set \"CLASSPATH=%CLASSPATH%;");
                if (!relativeLib.isAbsolute()) {
                    out.write("%DITA_HOME%" + WINDOWS_SEPARATOR);
                }
                out.write(relativeLib.toString().replace(File.separator, WINDOWS_SEPARATOR));
                out.write("\"\r\n");
            }
        } catch (final Exception e) {
            if (strict) {
                throw new RuntimeException("Failed to write environment batch: " + e.getMessage(), e);
            } else {
                logger.error(e.getMessage(), e) ;
            }
        } finally {
            closeQuietly(out);
        }
    }

    /**
     * Read plug-in feature.
     * 
     * @param featureName plug-in feature name
     * @return combined list of values
     */
    private String readExtensions(final String featureName) {
        final Set<String> exts = new HashSet<>();
        if (featureTable.containsKey(featureName)) {
            for (final String ext : featureTable.get(featureName)) {
                final String e = ext.trim();
                if (e.length() != 0) {
                    exts.add(e);
                }
            }
        }
        return StringUtils.join(exts, CONF_LIST_SEPARATOR);
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
            final Map<String, List<String>> featureSet = pluginFeatures.getAllFeatures();
            for (final Map.Entry<String, List<String>> currentFeature : featureSet.entrySet()) {
                if (!extensionPoints.contains(currentFeature.getKey())) {
                    final String msg = "Plug-in " + plugin + " uses an undefined extension point "
                            + currentFeature.getKey();
                    if (strict) {
                        throw new RuntimeException(msg);
                    } else {
                        logger.debug(msg);
                    }
                }
                if (featureTable.containsKey(currentFeature.getKey())) {
                    final List<String> value = featureTable.get(currentFeature.getKey());
                    value.addAll(currentFeature.getValue());
                    featureTable.put(currentFeature.getKey(), value);
                } else {
                    featureTable.put(currentFeature.getKey(), currentFeature.getValue());
                }
            }

            for (final String templateName : pluginFeatures.getAllTemplates()) {
                templateSet.add(FileUtils.getRelativeUnixPath(ditaDir + File.separator + "dummy",
                        pluginFeatures.getPluginDir() + File.separator + templateName));
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
                    logger.warn(msg);
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
        final Element root = pluginsDoc.createElement(ELEM_PLUGINS);
        pluginsDoc.appendChild(root);
        if (!descSet.isEmpty()) {
            final URI b = new File(ditaDir, RESOURCES_DIR + File.separator + "plugins.xml").toURI();
            for (final File descFile : descSet) {
                logger.debug("Read plug-in configuration " + descFile.getPath());
                final Element plugin = parseDesc(descFile);
                if (plugin != null) {
                    final URI base = getRelativePath(b, descFile.toURI());
                    plugin.setAttributeNS(XML_NS_URI, XML_NS_PREFIX + ":base", base.toString());
                    root.appendChild(pluginsDoc.importNode(plugin, true));
                }
            }
        }
    }

    private void writePlugins() throws TransformerException {
        final File plugins = new File(ditaDir, RESOURCES_DIR + File.separator + "plugins.xml");
        logger.debug("Writing " + plugins);
        try {
            final Transformer serializer = TransformerFactory.newInstance().newTransformer();
            serializer.transform(new DOMSource(pluginsDoc), new StreamResult(plugins));
        } catch (final TransformerConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Parse plugin configuration file
     * 
     * @param descFile plugin configuration
     */
    private Element parseDesc(final File descFile) {
        try {
            parser.setPluginDir(descFile.getParentFile());
            final Element root = parser.parse(descFile.getAbsoluteFile());
            final Features f = parser.getFeatures();
            final String id = f.getPluginId();
            validatePlugin(f);
            extensionPoints.addAll(f.getExtensionPoints().keySet());
            pluginTable.put(id, f);
            return root;
        } catch (final RuntimeException e) {
            throw e;
        } catch (final SAXParseException e) {
            final RuntimeException ex = new RuntimeException("Failed to parse " + descFile.getAbsolutePath() + ": " + e.getMessage(), e);
            if (strict) {
                throw ex;
            } else {
                logger.error(ex.getMessage(), ex) ;
            }
        } catch (final Exception e) {
            if (strict) {
                throw new RuntimeException(e);
            } else {
                logger.error(e.getMessage(), e) ;
            }
        }
        return null;
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
                logger.warn(msg);
            }
        }
        final List<String> version = f.getFeature("package.version");
        if (version != null && !version.isEmpty() && !VERSION_PATTERN.matcher(version.get(0)).matches()) {
            final String msg = "Plug-in version '" + version + "' doesn't follow recommended syntax rules, support for nonconforming version may be removed in future releases.";
            if (strict) {
                throw new IllegalArgumentException(msg);
            } else {
                logger.warn(msg);
            }
        }
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
    static String getValue(final Map<String, Features> featureTable, final String extension) {
        final List<String> buf = new ArrayList<>();
        for (final Features f : featureTable.values()) {
            final List<String> v = f.getFeature(extension);
            if (v != null) {
                buf.addAll(v);
            }
        }
        if (buf.isEmpty()) {
            return null;
        } else {
            return StringUtils.join(buf, ",");
        }
    }

}
