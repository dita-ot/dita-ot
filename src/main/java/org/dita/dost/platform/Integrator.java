/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2005, 2006 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.

 */
package org.dita.dost.platform;

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
import static org.dita.dost.util.XMLUtils.toList;

import com.google.common.annotations.VisibleForTesting;
import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.*;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.util.Configuration;
import org.dita.dost.util.FileUtils;
import org.dita.dost.util.StringUtils;
import org.dita.dost.util.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

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
  /**
   * Feature name for supported image extensions.
   */
  private static final String FEAT_IMAGE_EXTENSIONS = "dita.image.extensions";
  /**
   * Feature name for supported image extensions.
   */
  private static final String FEAT_HTML_EXTENSIONS = "dita.html.extensions";
  /**
   * Feature name for supported resource file extensions.
   */
  private static final String FEAT_RESOURCE_EXTENSIONS = "dita.resource.extensions";
  /**
   * Feature name for print transformation types.
   */
  private static final String FEAT_PRINT_TRANSTYPES = "dita.transtype.print";
  private static final String FEAT_TRANSTYPES = "dita.conductor.transtype.check";
  private static final String FEAT_LIB_EXTENSIONS = "dita.conductor.lib.import";
  private static final String ELEM_PLUGINS = "plugins";

  private static final String LIB_DIR = "lib";
  private static final String CONFIG_DIR = "config";

  public static final String FEAT_VALUE_SEPARATOR = ",";
  private static final String PARAM_VALUE_SEPARATOR = ";";

  private static final Set<PosixFilePermission> PERMISSIONS = Set.of(
    PosixFilePermission.OWNER_READ,
    PosixFilePermission.OWNER_WRITE,
    PosixFilePermission.OWNER_EXECUTE,
    PosixFilePermission.GROUP_READ,
    PosixFilePermission.GROUP_EXECUTE,
    PosixFilePermission.OTHERS_READ,
    PosixFilePermission.OTHERS_EXECUTE
  );

  public static final String CONF_PARSER_FORMAT = "parser.";

  /** Plugin table which contains detected plugins. */
  private final Map<String, Plugin> pluginTable;
  private final Map<String, Value> templateSet;
  private final File ditaDir;
  /**
   * Plugin configuration file.
   */
  private final Set<File> descSet;
  private final XMLReader reader;
  private final Document pluginsDoc;
  private final PluginParser parser;
  private DITAOTLogger logger;
  private final Set<String> loadedPlugin;
  private final Map<String, List<Value>> featureTable;

  @Deprecated
  private File propertiesFile;

  private final Set<String> extensionPoints;
  private final Map<String, Integer> pluginOrder = new HashMap<>();
  private Properties properties;
  private Set<String> pluginList;

  /**
   * Default Constructor.
   */
  public Integrator(final File ditaDir) {
    this.ditaDir = ditaDir;
    pluginTable = new HashMap<>(16);
    templateSet = new HashMap<>(16);
    descSet = new HashSet<>(16);
    loadedPlugin = new HashSet<>(16);
    featureTable = new HashMap<>(16);
    extensionPoints = new HashSet<>();
    try {
      final SAXParserFactory parserFactory = SAXParserFactory.newInstance();
      parserFactory.setNamespaceAware(true);
      reader = parserFactory.newSAXParser().getXMLReader();
    } catch (final Exception e) {
      throw new RuntimeException("Failed to initialize XML parser: " + e.getMessage(), e);
    }
    reader.setErrorHandler(
      new ErrorHandler() {
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
      }
    );
    parser = new PluginParser(ditaDir);
    try {
      final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);
      pluginsDoc = factory.newDocumentBuilder().newDocument();
    } catch (ParserConfigurationException e) {
      throw new RuntimeException("Failed to initialize XML parser: " + e.getMessage(), e);
    }

    pluginList = getPluginIds(readPlugins());
  }

  private void init() {
    // Read the properties file, if it exists.
    properties = new Properties();
    if (propertiesFile != null) {
      try (InputStream propertiesStream = Files.newInputStream(propertiesFile.toPath())) {
        properties.load(propertiesStream);
      } catch (final Exception e) {
        throw new RuntimeException(e);
      }
    } else {
      properties.putAll(Configuration.configuration);
    }
    if (!properties.containsKey(CONF_PLUGIN_DIRS)) {
      properties.setProperty(
        CONF_PLUGIN_DIRS,
        configuration.getOrDefault(CONF_PLUGIN_DIRS, String.join(PARAM_VALUE_SEPARATOR, "plugins", "demo"))
      );
    }
    if (!properties.containsKey(CONF_PLUGIN_IGNORES)) {
      properties.setProperty(CONF_PLUGIN_IGNORES, configuration.getOrDefault(CONF_PLUGIN_IGNORES, ""));
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
      for (final String plugin : plugins) {
        pluginOrder.put(plugin, priority++);
      }
    }

    for (final String tmpl : properties.getProperty(CONF_TEMPLATES, "").split(PARAM_VALUE_SEPARATOR)) {
      final String t = tmpl.trim();
      if (t.length() != 0) {
        logger.warn(MessageUtils.getMessage("DOTJ080W", "templates", "template").toString());
        templateSet.put(t, null);
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
  }

  /**
   * Execute point of Integrator.
   */
  public void execute() throws Exception {
    init();
    mergePlugins();
    integrate();
    logChanges(pluginList, getPluginIds(pluginsDoc));
  }

  private void logChanges(final Set<String> orig, final Set<String> mod) {
    final List<String> removed = new ArrayList<>(orig);
    removed.removeAll(mod);
    removed.sort(Comparator.naturalOrder());
    for (final String p : removed) {
      logger.info("Removed {}", p);
    }
    final List<String> added = new ArrayList<>(mod);
    added.removeAll(orig);
    added.sort(Comparator.naturalOrder());
    for (final String p : added) {
      logger.info("Added {}", p);
    }
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
    for (final Entry<String, Value> template : templateSet.entrySet()) {
      final File templateFile = new File(ditaDir, template.getKey());
      logger.trace("Process template " + templateFile.getPath());
      //            fileGen.setPluginId(template.getValue().id);
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
      for (final Value ext : featureTable.get(FEAT_IMAGE_EXTENSIONS)) {
        final String e = ext.value().trim();
        if (e.length() != 0) {
          imgExts.add(e);
        }
      }
    }
    configuration.put(CONF_SUPPORTED_IMAGE_EXTENSIONS, String.join(CONF_LIST_SEPARATOR, imgExts));
    // extensions
    configuration.put(CONF_SUPPORTED_HTML_EXTENSIONS, readExtensions(FEAT_HTML_EXTENSIONS));
    configuration.put(CONF_SUPPORTED_RESOURCE_EXTENSIONS, readExtensions(FEAT_RESOURCE_EXTENSIONS));

    // transtypes
    final String transtypes = featureTable
      .entrySet()
      .stream()
      .filter(e -> e.getKey().equals(FEAT_TRANSTYPES))
      .flatMap(e -> e.getValue().stream().map(Value::value))
      .distinct()
      .collect(Collectors.joining(CONF_LIST_SEPARATOR));
    configuration.put(CONF_TRANSTYPES, transtypes);

    // print transtypes
    final Set<String> printTranstypes = new HashSet<>();
    if (featureTable.containsKey(FEAT_PRINT_TRANSTYPES)) {
      for (final Value ext : featureTable.get(FEAT_PRINT_TRANSTYPES)) {
        final String e = ext.value().trim();
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
    configuration.put(CONF_PRINT_TRANSTYPES, String.join(CONF_LIST_SEPARATOR, printTranstypes));

    for (final Entry<String, Plugin> e : pluginTable.entrySet()) {
      final Plugin f = e.getValue();
      final String name = "plugin." + e.getKey() + ".dir";
      final List<Value> baseDirValues = f.getFeature("dita.basedir-resource-directory");
      if (
        Boolean.parseBoolean(baseDirValues == null || baseDirValues.isEmpty() ? null : baseDirValues.get(0).value())
      ) {
        //configuration.put(name, ditaDir.getAbsolutePath());
        configuration.put(name, ".");
      } else {
        configuration.put(
          name,
          FileUtils.getRelativeUnixPath(new File(ditaDir, "dummy").getAbsolutePath(), f.pluginDir().getAbsolutePath())
        );
      }
    }
    configuration.putAll(getParserConfiguration());

    writePluginProperties(configuration);
    processMessages();
    writeMessageBundle();

    final Collection<File> jars = featureTable.containsKey(FEAT_LIB_EXTENSIONS)
      ? relativize(featureTable.get(FEAT_LIB_EXTENSIONS))
      : Collections.emptySet();
    writeEnvShell(jars);
    writeEnvBatch(jars);

    final Collection<File> libJars = new ArrayList<>();
    libJars.addAll(getLibJars());
    libJars.addAll(jars);
    writeStartcmdShell(libJars);
    writeStartcmdBatch(libJars);
    writeConfigurationJar();

    customIntegration();
  }

  private record Message(String id, String severity, String reason, String response) {}

  private void processMessages() throws IOException {
    final Path messagesXmlFile = ditaDir.toPath().resolve(CONFIG_DIR).resolve("messages.xml");
    if (Files.exists(messagesXmlFile)) {
      final List<Message> messages = readMessages(messagesXmlFile);
      writeMessages(messages, messagesXmlFile);
    }
  }

  private void writeMessages(List<Message> messages, Path messagesXmlFile) throws IOException {
    try (final OutputStream messagesOut = Files.newOutputStream(messagesXmlFile)) {
      final XMLStreamWriter out = XMLOutputFactory.newInstance().createXMLStreamWriter(messagesOut);
      out.writeStartDocument();
      out.writeStartElement("messages");
      for (Message message : messages) {
        out.writeStartElement("message");
        out.writeAttribute("id", message.id());
        out.writeAttribute("type", message.severity());
        out.writeStartElement("reason");
        if (message.reason() != null) {
          out.writeCharacters(message.reason());
        }
        out.writeEndElement();
        out.writeStartElement("response");
        if (message.response() != null) {
          out.writeCharacters(message.response());
        }
        out.writeEndElement();
        out.writeEndElement();
      }
      out.writeEndElement();
      out.writeEndDocument();
      out.close();
    } catch (XMLStreamException e) {
      throw new IOException(e);
    }
  }

  /** Read and merge messages. */
  private List<Message> readMessages(Path messagesXmlFile) throws IOException {
    final Map<String, Message> messages = new HashMap<>();
    try (final InputStream in = Files.newInputStream(messagesXmlFile)) {
      final XMLStreamReader src = XMLInputFactory.newInstance().createXMLStreamReader(new StreamSource(in));
      String id = null;
      String severity = null;
      String reason = null;
      String response = null;
      while (src.hasNext()) {
        final int type = src.next();
        switch (type) {
          case XMLEvent.START_ELEMENT:
            switch (src.getLocalName()) {
              case "message" -> {
                id = src.getAttributeValue(XMLConstants.NULL_NS_URI, "id");
                severity = src.getAttributeValue(XMLConstants.NULL_NS_URI, "type");
              }
              case "reason" -> reason = src.getElementText();
              case "response" -> response = src.getElementText();
            }
            break;
          case XMLEvent.END_ELEMENT:
            if (src.getLocalName().equals("message")) {
              final Message prev = messages.get(id);
              if (prev == null) {
                messages.put(id, new Message(id, severity, reason, response));
              } else {
                logger.trace("Override message {}", id);
                messages.put(
                  id,
                  new Message(
                    id,
                    Objects.requireNonNullElse(severity, prev.severity()),
                    Objects.requireNonNullElse(reason, prev.reason()),
                    Objects.requireNonNullElse(response, prev.response())
                  )
                );
              }
              id = null;
              severity = null;
              reason = null;
              response = null;
            }
            break;
          case XMLStreamConstants.CHARACTERS:
            break;
        }
      }
      src.close();
    } catch (XMLStreamException e) {
      throw new IOException(e);
    }
    return messages.values().stream().sorted(Comparator.comparing(Message::id)).toList();
  }

  private void writeMessageBundle() throws IOException, XMLStreamException {
    // Write messages properties
    final Properties messages = readMessageBundle();
    final Path messagesFile = ditaDir.toPath().resolve(CONFIG_DIR).resolve("messages_en_US.properties");
    try (final OutputStream messagesOut = Files.newOutputStream(messagesFile)) {
      messages.store(messagesOut, null);
    }
  }

  private void writePluginProperties(Properties configuration) {
    final Path outFile = ditaDir
      .toPath()
      .resolve(CONFIG_DIR)
      .resolve(getClass().getPackage().getName())
      .resolve(GEN_CONF_PROPERTIES);
    try {
      Files.createDirectories(outFile.getParent());
    } catch (IOException e) {
      throw new RuntimeException("Failed to make directory " + outFile.getParent());
    }
    logger.trace("Generate configuration properties {}", outFile);
    try (OutputStream out = Files.newOutputStream(outFile)) {
      configuration.store(out, "DITA-OT runtime configuration, do not edit manually");
    } catch (final Exception e) {
      throw new RuntimeException("Failed to write configuration properties: " + e.getMessage(), e);
    }
  }

  /**
   * Create legacy configuration JAR. The configuration JAR is used by e.g. DITA-OT Gradle plug-in so we have to keep on
   * generating it.
   */
  private void writeConfigurationJar() throws IOException {
    final Path outFile = ditaDir.toPath().resolve("lib").resolve("dost-configuration.jar");
    logger.trace("Generate configuration JAR {}", outFile);
    try (OutputStream out = Files.newOutputStream(outFile); final ZipOutputStream zip = new ZipOutputStream(out)) {
      var config = ditaDir.toPath().resolve("config");
      Consumer<Path> copy = (Path path) -> {
        final Path file = config.resolve(path);
        if (!Files.exists(file)) {
          return;
        }
        try {
          ZipEntry entry = new ZipEntry(path.toString().replace('\\', '/'));
          zip.putNextEntry(entry);
          Files.copy(file, zip);
          zip.flush();
          zip.closeEntry();
        } catch (IOException e) {
          throw new UncheckedIOException(e);
        }
      };

      copy.accept(Paths.get("messages.xml"));
      Files
        .list(config)
        .map(Path::getFileName)
        .filter(path -> path.toString().startsWith("messages_") && path.toString().endsWith(".properties"))
        .forEach(copy);
      copy.accept(Paths.get("plugins.xml"));
      copy.accept(Paths.get("configuration.properties"));
      copy.accept(Paths.get("CatalogManager.properties"));
      copy.accept(Paths.get("org.dita.dost.platform", "plugin.properties"));
    } catch (IOException e) {
      throw new IOException("Failed to write configuration JAR", e);
    }
  }

  private Properties readMessageBundle() throws IOException, XMLStreamException {
    final Properties messages = new Properties();
    //        final Path basePluginDir = pluginTable.get("org.dita.base").getPluginDir().toPath();
    //        final File messagesXmlFile = basePluginDir.resolve(CONFIG_DIR).resolve("messages.xml").toFile();
    final Path messagesXmlFile = ditaDir.toPath().resolve(CONFIG_DIR).resolve("messages.xml");
    if (Files.exists(messagesXmlFile)) {
      try (final InputStream in = Files.newInputStream(messagesXmlFile)) {
        final XMLStreamReader src = XMLInputFactory.newInstance().createXMLStreamReader(new StreamSource(in));
        String id = null;
        final StringBuilder buf = new StringBuilder();
        while (src.hasNext()) {
          final int type = src.next();
          switch (type) {
            case XMLEvent.START_ELEMENT -> {
              if (src.getLocalName().equals("message")) {
                id = src.getAttributeValue(XMLConstants.NULL_NS_URI, "id");
              } else if (id != null) {
                buf.append(src.getElementText()).append(' ');
              }
            }
            case XMLEvent.END_ELEMENT -> {
              if (src.getLocalName().equals("message")) {
                messages.put(id, convertMessage(buf.toString()));
                id = null;
                buf.delete(0, buf.length());
              }
            }
          }
        }
        src.close();
      }
    }
    return messages;
  }

  @VisibleForTesting
  static String convertMessage(final String src) {
    final String res = src.replaceAll("[\\s\\n]+", " ").replace("'", "''").replace("{", "'{").trim();
    final StringBuilder buf = new StringBuilder();
    final Matcher m = Pattern.compile("%(\\d)").matcher(res);
    int offset = 0;
    while (m.find()) {
      final int index = Integer.parseInt(m.group(1));
      buf.append(res, offset, m.start());
      buf.append("{").append(index - 1).append("}");
      offset = m.end();
    }
    buf.append(res.substring(offset));
    return buf.toString();
  }

  private Collection<File> getLibJars() {
    final String[] libJars = new File(ditaDir, LIB_DIR).list((dir, name) -> name.endsWith(".jar"));
    final List<File> res = new ArrayList<>(libJars.length);
    for (String l : libJars) {
      res.add(new File(LIB_DIR + File.separator + l));
    }
    res.sort(Comparator.comparing(File::getAbsolutePath));
    return res;
  }

  private void customIntegration() {
    final ServiceLoader<CustomIntegrator> customIntegrators = ServiceLoader.load(CustomIntegrator.class);
    for (final CustomIntegrator customIntegrator : customIntegrators) {
      customIntegrator.setLogger(logger);
      customIntegrator.setDitaDir(ditaDir);
      try {
        customIntegrator.process();
      } catch (final Exception e) {
        logger.error("Custom integrator {} failed: {}", customIntegrator.getClass().getName(), e.getMessage(), e);
      }
    }
  }

  private Iterable<String> orderPlugins(final Set<String> ids) {
    final List<String> res = new ArrayList<>(ids);
    res.sort((s1, s2) -> {
      final int score1 = pluginOrder.getOrDefault(s1, 0);
      final int score2 = pluginOrder.getOrDefault(s2, 0);
      if (score1 < score2) {
        return 1;
      } else if (score1 > score2) {
        return -1;
      } else {
        return s1.compareTo(s2);
      }
    });
    return res;
  }

  private Map<String, String> getParserConfiguration() {
    final Map<String, String> res = new HashMap<>();
    final List<Element> features = toList(pluginsDoc.getElementsByTagName(FEATURE_ELEM));
    for (final Element feature : features) {
      if (feature.getAttribute(FEATURE_ID_ATTR).equals("dita.parser")) {
        final List<Element> parsers = toList(feature.getElementsByTagName("parser"));
        for (final Element parser : parsers) {
          final String format = parser.getAttribute("format");
          res.put(CONF_PARSER_FORMAT + format, parser.getAttribute("class"));
          final List<Element> fs = toList(parser.getElementsByTagName("feature"));
          final List<String> fsv = fs
            .stream()
            .map(f -> f.getAttribute("name") + "=" + f.getAttribute("value"))
            .toList();
          if (!fsv.isEmpty()) {
            res.put(CONF_PARSER_FORMAT + format + ".features", String.join(PARAM_VALUE_SEPARATOR, fsv));
          }
        }
      }
    }
    return res;
  }

  private Collection<File> relativize(final Collection<Value> src) {
    final File base = new File(ditaDir, "dummy");
    return src
      .stream()
      .flatMap(lib -> {
        if (lib instanceof Value.PathValue path) {
          return Stream.of(toFile(path.getPath()));
        } else {
          logger.error("Library import must be a file feature: " + lib.value());
          return Stream.empty();
        }
      })
      .map(libFile -> {
        if (!libFile.exists()) {
          throw new IllegalArgumentException("Library file not found: " + libFile.getAbsolutePath());
        }
        return FileUtils.getRelativePath(base, libFile);
      })
      .toList();
  }

  private void writeEnvShell(final Collection<File> jars) {
    final Path outFile = ditaDir.toPath().resolve(CONFIG_DIR).resolve("env.sh");
    try {
      Files.createDirectories(outFile.getParent());
    } catch (IOException e) {
      throw new RuntimeException("Failed to make directory " + outFile.getParent());
    }
    logger.trace("Generate environment shell {}", outFile);
    try (Writer out = Files.newBufferedWriter(outFile)) {
      out.write("#!/bin/sh\n");
      for (final File relativeLib : jars) {
        out.write("CLASSPATH=\"$CLASSPATH:");
        if (!relativeLib.isAbsolute()) {
          out.write("$DITA_HOME" + UNIX_SEPARATOR);
        }
        out.write(relativeLib.toString().replace(File.separator, UNIX_SEPARATOR));
        out.write("\"\n");
      }
      try {
        Files.setPosixFilePermissions(outFile, PERMISSIONS);
      } catch (final UnsupportedOperationException e) {
        // not supported
      }
    } catch (final IOException e) {
      throw new RuntimeException("Failed to write environment shell: " + e.getMessage(), e);
    }
  }

  private void writeEnvBatch(final Collection<File> jars) {
    final Path outFile = ditaDir.toPath().resolve(CONFIG_DIR).resolve("env.bat");
    try {
      Files.createDirectories(outFile.getParent());
    } catch (IOException e) {
      throw new RuntimeException("Failed to make directory " + outFile.getParent());
    }
    logger.trace("Generate environment batch {}", outFile);
    try (Writer out = Files.newBufferedWriter(outFile)) {
      for (final File relativeLib : jars) {
        out.write("set \"CLASSPATH=%CLASSPATH%;");
        if (!relativeLib.isAbsolute()) {
          out.write("%DITA_HOME%" + WINDOWS_SEPARATOR);
        }
        out.write(relativeLib.toString().replace(File.separator, WINDOWS_SEPARATOR));
        out.write("\"\r\n");
      }
      outFile.toFile().setExecutable(true);
    } catch (final IOException e) {
      throw new RuntimeException("Failed to write environment batch: " + e.getMessage(), e);
    }
  }

  private void writeStartcmdShell(final Collection<File> jars) {
    Writer out = null;
    try {
      final File outFile = new File(ditaDir, "startcmd.sh");
      if (!(outFile.getParentFile().exists()) && !outFile.getParentFile().mkdirs()) {
        throw new RuntimeException("Failed to make directory " + outFile.getParentFile().getAbsolutePath());
      }
      logger.trace("Generate start command shell {}", outFile.getPath());
      out = Files.newBufferedWriter(outFile.toPath());

      out.write(
        """
        #!/bin/sh
        # Generated file, do not edit manually"
        echo "NOTE: The startcmd.sh has been deprecated, use the 'dita' command instead."

        realpath() {
          case $1 in
            /*) echo "$1" ;;
            *) echo "$PWD/${1#./}" ;;
          esac
        }

        if [ "${DITA_HOME:+1}" = "1" ] && [ -e "$DITA_HOME" ]; then
          export DITA_DIR="$(realpath "$DITA_HOME")"
        else #elif [ "${DITA_HOME:+1}" != "1" ]; then
          export DITA_DIR="$(dirname "$(realpath "$0")")"
        fi

        if [ -f "$DITA_DIR"/bin/ant ] && [ ! -x "$DITA_DIR"/bin/ant ]; then
          chmod +x "$DITA_DIR"/bin/ant
        fi

        export ANT_OPTS="-Xmx512m $ANT_OPTS"
        export ANT_OPTS="$ANT_OPTS -Djavax.xml.transform.TransformerFactory=net.sf.saxon.TransformerFactoryImpl"
        export ANT_HOME="$DITA_DIR"
        export PATH="$DITA_DIR"/bin:"$PATH"

        NEW_CLASSPATH="$DITA_DIR/lib:$NEW_CLASSPATH"
        """
      );
      for (final File relativeLib : jars) {
        out.write("NEW_CLASSPATH=\"");
        if (!relativeLib.isAbsolute()) {
          out.write("$DITA_DIR" + UNIX_SEPARATOR);
        }
        out.write(relativeLib.toString().replace(File.separator, UNIX_SEPARATOR));
        out.write(":$NEW_CLASSPATH\"\n");
      }
      out.write(
        """
        if test -n "$CLASSPATH"; then
          export CLASSPATH="$NEW_CLASSPATH":"$CLASSPATH"
        else
          export CLASSPATH="$NEW_CLASSPATH"
        fi

        cd "$DITA_DIR"
        "$SHELL"
        """
      );
      try {
        Files.setPosixFilePermissions(outFile.toPath(), PERMISSIONS);
      } catch (final UnsupportedOperationException e) {
        // not supported
      }
    } catch (final IOException e) {
      throw new RuntimeException("Failed to write start command shell: " + e.getMessage(), e);
    } finally {
      closeQuietly(out);
    }
  }

  private void writeStartcmdBatch(final Collection<File> jars) {
    Writer out = null;
    try {
      final File outFile = new File(ditaDir, "startcmd.bat");
      if (!(outFile.getParentFile().exists()) && !outFile.getParentFile().mkdirs()) {
        throw new RuntimeException("Failed to make directory " + outFile.getParentFile().getAbsolutePath());
      }
      logger.trace("Generate start command batch {}", outFile.getPath());
      out = Files.newBufferedWriter(outFile.toPath());

      out.write(
        """
        @echo off\r
        REM Generated file, do not edit manually\r
        echo "NOTE: The startcmd.bat has been deprecated, use the dita.bat command instead."\r
        pause\r
        \r
        REM Get the absolute path of DITAOT's home directory\r
        set DITA_DIR=%~dp0\r
        \r
        REM Set environment variables\r
        set ANT_OPTS=-Xmx512m %ANT_OPTS%\r
        set ANT_OPTS=%ANT_OPTS% -Djavax.xml.transform.TransformerFactory=net.sf.saxon.TransformerFactoryImpl\r
        set ANT_HOME=%DITA_DIR%\r
        set PATH=%DITA_DIR%\\bin;%PATH%\r
        set CLASSPATH=%DITA_DIR%lib;%CLASSPATH%\r
        """
      );
      for (final File relativeLib : jars) {
        out.write("set CLASSPATH=");
        if (!relativeLib.isAbsolute()) {
          out.write("%DITA_DIR%");
        }
        out.write(relativeLib.toString().replace(File.separator, WINDOWS_SEPARATOR));
        out.write(";%CLASSPATH%\r\n");
      }
      out.write("start \"DITA-OT\" cmd.exe\r\n");
      outFile.setExecutable(true);
    } catch (final IOException e) {
      throw new RuntimeException("Failed to write start command batch: " + e.getMessage(), e);
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
      for (final Value ext : featureTable.get(featureName)) {
        final String e = ext.value().trim();
        if (e.length() != 0) {
          exts.add(e);
        }
      }
    }
    return String.join(CONF_LIST_SEPARATOR, exts);
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
      final Plugin pluginFeatures = pluginTable.get(plugin);
      final Map<String, List<Value>> featureSet = pluginFeatures.features();
      for (final Map.Entry<String, List<Value>> currentFeature : featureSet.entrySet()) {
        final String key = currentFeature.getKey();
        final List<Value> values = currentFeature.getValue();
        if (!extensionPoints.contains(key)) {
          throw new RuntimeException("Plug-in %s uses an undefined extension point %s".formatted(plugin, key));
        }
        if (featureTable.containsKey(key)) {
          final List<Value> value = featureTable.get(key);
          value.addAll(values);
          featureTable.put(key, value);
        } else {
          //Make shallow clone to avoid making modifications directly to list inside the current feature.
          List<Value> currentFeatureValue = values;
          featureTable.put(key, currentFeatureValue != null ? new ArrayList<>(currentFeatureValue) : null);
        }
      }

      for (final String templateName : pluginFeatures.templates()) {
        final String template = new File(pluginFeatures.pluginDir().toURI().resolve(templateName)).getAbsolutePath();
        final String templatePath = FileUtils.getRelativeUnixPath(ditaDir + File.separator + "dummy", template);
        templateSet.put(templatePath, new Value.StringValue(pluginFeatures.pluginId(), templateName));
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
    final Plugin pluginFeatures = pluginTable.get(currentPlugin);
    // check whether dependcy is satisfied
    for (PluginRequirement requirement : pluginFeatures.requiredPlugins()) {
      boolean anyPluginFound = false;
      // Iterate over all alternatives in plugin requirement.
      for (String requiredPlugin : requirement.plugins()) {
        if (pluginTable.containsKey(requiredPlugin)) {
          if (!loadedPlugin.contains(requiredPlugin)) {
            // required plug-in is not loaded
            loadPlugin(requiredPlugin);
          }
          // As soon as any plugin is found, it's OK.
          anyPluginFound = true;
        }
      }
      if (!anyPluginFound && requirement.required()) {
        // not contain any plugin required by current plugin
        final String msg = MessageUtils.getMessage("DOTJ020W", requirement.toString(), currentPlugin).toString();
        throw new RuntimeException(msg);
      }
    }
    return true;
  }

  private Document readPlugins() {
    final File plugins = new File(ditaDir, CONFIG_DIR + File.separator + "plugins.xml");
    if (!plugins.exists()) {
      return null;
    }
    try {
      final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);
      return factory.newDocumentBuilder().parse(plugins);
    } catch (ParserConfigurationException | SAXException | IOException e) {
      throw new RuntimeException(e);
    }
  }

  private Set<String> getPluginIds(final Document doc) {
    if (doc == null) {
      return Collections.emptySet();
    }
    final List<Element> ps = toList(doc.getElementsByTagName("plugin"));
    return ps
      .stream()
      .filter(p -> p.getAttributeNode("id") != null)
      .map(p -> p.getAttribute("id"))
      .collect(Collectors.toSet());
  }

  /**
   * Merge plugin configuration files.
   */
  private void mergePlugins() {
    final Element root = pluginsDoc.createElement(ELEM_PLUGINS);
    pluginsDoc.appendChild(root);
    if (!descSet.isEmpty()) {
      final URI b = new File(ditaDir, CONFIG_DIR + File.separator + "plugins.xml").toURI();
      for (final File descFile : descSet) {
        logger.trace("Read plug-in configuration {}", descFile.getPath());
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
    final File plugins = new File(ditaDir, CONFIG_DIR + File.separator + "plugins.xml");
    logger.trace("Writing {}", plugins);
    try {
      new XMLUtils().writeDocument(pluginsDoc, plugins);
    } catch (final IOException e) {
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
      final Plugin f = parser.getPlugin();
      extensionPoints.addAll(f.extensionPoints().keySet());
      pluginTable.put(f.pluginId(), f);
      return root;
    } catch (final RuntimeException e) {
      throw e;
    } catch (final SAXParseException e) {
      final RuntimeException ex = new RuntimeException(
        "Failed to parse " + descFile.getAbsolutePath() + ": " + e.getMessage(),
        e
      );
      throw ex;
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Set the properties file.
   *
   * @param propertiesfile properties file
   */
  @Deprecated
  public void setProperties(final File propertiesfile) {
    propertiesFile = propertiesfile;
  }

  /**
   * Set logger.
   *
   * @param logger logger instance
   */
  public void setLogger(final DITAOTLogger logger) {
    this.logger = logger;
    this.parser.setLogger(logger);
  }

  /**
   * Get all and combine extension values
   *
   * @param featureTable plugin features
   * @param extension    extension ID
   * @return combined extension value, {@code null} if no value available
   */
  static String getValue(final Map<String, Plugin> featureTable, final String extension) {
    final List<Value> buf = new ArrayList<>();
    for (final Plugin f : featureTable.values()) {
      final List<Value> v = f.getFeature(extension);
      if (v != null) {
        buf.addAll(v);
      }
    }
    if (buf.isEmpty()) {
      return null;
    } else {
      return buf.stream().map(Value::value).collect(Collectors.joining(","));
    }
  }

  /**
   * Add ID of a plugin that has been removed.
   *
   * @param name plugin ID
   */
  public void addRemoved(String name) {
    pluginList.remove(name);
  }
}
