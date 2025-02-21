/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2013 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
/* Derived from Apache Ant. */
/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.dita.dost.invoker;

import static java.nio.file.FileVisitResult.CONTINUE;
import static org.dita.dost.invoker.Arguments.*;
import static org.dita.dost.log.DITAOTAntLogger.USE_COLOR;
import static org.dita.dost.platform.PluginParser.FEATURE_ELEM;
import static org.dita.dost.platform.PluginParser.FEATURE_ID_ATTR;
import static org.dita.dost.util.Configuration.transtypes;
import static org.dita.dost.util.Constants.ANT_TEMP_DIR;
import static org.dita.dost.util.LangUtils.pair;
import static org.dita.dost.util.LangUtils.zipWithIndex;
import static org.dita.dost.util.XMLUtils.toList;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.tools.ant.*;
import org.apache.tools.ant.input.DefaultInputHandler;
import org.apache.tools.ant.input.InputHandler;
import org.apache.tools.ant.launch.AntMain;
import org.apache.tools.ant.property.ResolvePropertyMap;
import org.apache.tools.ant.util.ClasspathUtils;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.ant.util.ProxySetup;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.log.StandardLogger;
import org.dita.dost.platform.PluginInstall;
import org.dita.dost.platform.PluginUninstall;
import org.dita.dost.platform.Plugins;
import org.dita.dost.platform.SemVerMatch;
import org.dita.dost.project.Project.Context;
import org.dita.dost.project.Project.Publication;
import org.dita.dost.project.ProjectFactory;
import org.dita.dost.util.Configuration;
import org.dita.dost.util.URLUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

/**
 * Command line entry point into DITA-OT. This class is entered via the canonical
 * `public static void main` entry point and reads the command line arguments.
 * It then assembles and executes an Ant project.
 */
public class Main extends org.apache.tools.ant.Main implements AntMain {

  private static final String SYSTEM_PROPERTY_DITA_HOME = "dita.dir";
  private static final String ANT_ARGS_INPUT = "args.input";
  static final String ANT_ARGS_RESOURCES = "args.resources";
  static final String ANT_ARGS_INPUTS = "args.inputs";
  protected static final String ANT_OUTPUT_DIR = "output.dir";
  private static final String ANT_BASE_TEMP_DIR = "base.temp.dir";
  private static final String ANT_TRANSTYPE = "transtype";
  private static final String ANT_PLUGIN_FILE = "plugin.file";
  private static final String ANT_PLUGIN_ID = "plugin.id";
  private static final String ANT_PROJECT_DELIVERABLE = "project.deliverable";
  private static final String ANT_PROJECT_CONTEXT = "project.context";
  private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
  private static final Map<String, String> RESERVED_PARAMS = Map.of(
    "output.dir",
    "output",
    "transtype",
    "transtype",
    "args.input",
    "input",
    "args.filter",
    "profiles"
  );
  private static final String CONFIGURATION_FILE = ".ditaotrc";

  /** @deprecated since 4.2 */
  @Deprecated
  private static final String CONFIGURATION_FILE_OLD = "local.properties";

  /**
   * File that we are using for configuration.
   */
  private File buildFile;

  /**
   * Stream to use for logging.
   */
  private static PrintStream out = System.out;

  /**
   * Stream that we are using for logging error messages.
   */
  private static PrintStream err = System.err;

  /**
   * The build targets.
   */
  private final Vector<String> targets = new Vector<>();

  /**
   * Set of properties that can be used by tasks.
   */
  private List<Map<String, Object>> projectProps;

  /**
   * Whether or not this instance has successfully been constructed and is
   * ready to run.
   */
  private boolean readyToRun = false;

  private final ArgumentParser argumentParser = new ArgumentParser();
  private Arguments args;
  public static final ResourceBundle locale = ResourceBundle.getBundle("cli", new Locale("en", "US"));

  /**
   * Prints the message of the Throwable if it (the message) is not {@code null}.
   *
   * @param t Throwable to print the message of. Must not be {@code null}
   */
  private void printMessage(final Throwable t) {
    final String message = t.getMessage();
    if (message != null && !message.trim().isEmpty()) {
      printErrorMessage(message, t);
    }
  }

  private void printErrorMessage(final String msg, final Throwable t) {
    logger.error(msg, t);
  }

  /**
   * Creates a new instance of this class using the arguments specified, gives
   * it any extra user properties which have been specified, and then runs the
   * build using the classloader provided.
   *
   * @param args                     Command line arguments. Must not be <code>null</code>.
   * @param additionalUserProperties Any extra properties to use in this
   *                                 build. May be <code>null</code>, which is the equivalent to
   *                                 passing in an empty set of properties.
   * @param coreLoader               Classloader used for core classes. May be
   *                                 <code>null</code> in which case the system classloader is
   *                                 used.
   */
  @Deprecated
  public static void start(
    final String[] args,
    final Properties additionalUserProperties,
    final ClassLoader coreLoader
  ) {
    final Main m = new Main();
    m.startAnt(args, additionalUserProperties, coreLoader);
  }

  /**
   * Start Ant
   *
   * @param args                     command line args
   * @param additionalUserProperties properties to set beyond those that may
   *                                 be specified on the args list
   * @param coreLoader               - not used
   * @since Ant 1.6
   */
  @Override
  public void startAnt(final String[] args, final Properties additionalUserProperties, final ClassLoader coreLoader) {
    try {
      processArgs(args);
    } catch (final CliException exc) {
      handleLogfile();
      logger.setOutputLevel(Project.MSG_INFO);
      printMessage(exc);
      if (exc.info != null) {
        logger.info(exc.info);
      }
      exit(1);
      return;
    } catch (final BuildException exc) {
      handleLogfile();
      printMessage(exc);
      exit(1);
      return;
    } catch (final RuntimeException e) {
      handleLogfile();
      e.printStackTrace();
      exit(1);
      return;
    } catch (final Throwable exc) {
      handleLogfile();
      printMessage(exc);
      exit(1);
      return;
    }

    if (!readyToRun) {
      return;
    }

    if (additionalUserProperties != null) {
      for (Map<String, Object> props : projectProps) {
        for (final Enumeration<Object> e = additionalUserProperties.keys(); e.hasMoreElements();) {
          final String key = (String) e.nextElement();
          final String property = additionalUserProperties.getProperty(key);
          props.put(key, property);
        }
      }
    }

    // expect the worst
    int exitCode = 1;
    try {
      final long[] durations = new long[this.args.repeat];
      for (int i = 0; i < this.args.repeat; i++) {
        final long start = System.currentTimeMillis();
        try {
          for (Map<String, Object> props : projectProps) {
            runBuild(coreLoader, props);
          }
          exitCode = 0;
        } catch (final ExitStatusException ese) {
          exitCode = ese.getStatus();
          if (exitCode != 0) {
            throw ese;
          }
        }
        final long end = System.currentTimeMillis();
        durations[i] = end - start;
      }
      if (this.args.repeat > 1) {
        for (int i = 0; i < durations.length; i++) {
          logger.info(locale.getString("conversion.repeatDuration").formatted(i + 1, durations[i]));
        }
      }
    } catch (final BuildException be) {
      if (err != System.err) {
        printMessage(be);
      }
    } catch (final Throwable exc) {
      exc.printStackTrace();
      printMessage(exc);
    } finally {
      handleLogfile();
    }
    exit(exitCode);
  }

  /**
   * This operation is expected to call {@link System#exit(int)}, which is
   * what the base version does. However, it is possible to do something else.
   *
   * @param exitCode code to exit with
   */
  @Override
  protected void exit(final int exitCode) {
    System.exit(exitCode);
  }

  /**
   * Close logfiles, if we have been writing to them.
   *
   * @since Ant 1.6
   */
  private void handleLogfile() {
    if (args != null && args.logFile != null) {
      FileUtils.close(out);
      FileUtils.close(err);
    }
  }

  /**
   * Command line entry point. This method kicks off the building of a project
   * object and executes a build using either a given target or the default
   * target.
   *
   * @param args Command line arguments. Must not be <code>null</code>.
   */
  @Deprecated
  public static void main(final String[] args) {
    start(args, null, null);
  }

  final StandardLogger logger;

  /**
   * Constructor used when creating Main for later arg processing and startup
   */
  public Main() {
    logger = new StandardLogger(System.out, System.err, Project.MSG_INFO, true);
  }

  /**
   * Process command line arguments. When ant is started from Launcher,
   * launcher-only arguments do not get passed through to this routine.
   *
   * @param arguments the command line arguments.
   * @since Ant 1.6
   */
  private void processArgs(final String[] arguments) {
    args = argumentParser.processArgs(arguments);
    final Map<String, Object> definedProps = new HashMap<>(args.definedProps);
    projectProps = Collections.singletonList(definedProps);
    buildFile = args.buildFile;
    logger.setOutputLevel(args.msgOutputLevel);
    logger.setUseColor(args.useColor);

    if (args.justPrintUsage) {
      logger.setOutputLevel(Project.MSG_INFO);
      logger.info(args.getUsage(false));
      return;
    } else if (args.justPrintDiagnostics) {
      Diagnostics.doReport(System.out, args.msgOutputLevel);
      return;
    }

    if (args instanceof PluginsArguments) {
      printPlugins();
      return;
    } else if (args instanceof VersionArguments) {
      printVersion();
      return;
    } else if (args instanceof final ValidateArguments validateArgs) {
      final File ditaDir = new File(System.getProperty(SYSTEM_PROPERTY_DITA_HOME));
      final File basePluginDir = new File(ditaDir, Configuration.pluginResourceDirs.get("org.dita.base").getPath());
      buildFile = findBuildFile(basePluginDir.getAbsolutePath(), "build.xml");
      definedProps.putAll(getLocalProperties(ditaDir));
      definedProps.put(USE_COLOR, Boolean.toString(validateArgs.useColor));
      if (validateArgs.projectFile == null) {
        projectProps = Collections.singletonList(definedProps);
      } else {
        projectProps = collectContextProperties(validateArgs.projectFile, definedProps);
      }
      for (Map<String, Object> projectProp : projectProps) {
        String err = null;
        if (!projectProp.containsKey(ANT_ARGS_INPUT)) {
          err = locale.getString("conversion.error.input_not_defined");
        }
        if (err != null) {
          throw new CliException(err, args.getUsage(true));
        }
        // default values
        if (!projectProp.containsKey(ANT_BASE_TEMP_DIR)) {
          projectProp.put(ANT_BASE_TEMP_DIR, new File(System.getProperty("java.io.tmpdir")).getAbsolutePath());
        }
      }
    } else if (args instanceof TranstypesArguments) {
      printTranstypes();
      return;
    } else if (args instanceof final DeliverablesArguments deliverablesArgs) {
      printDeliverables(deliverablesArgs);
      return;
    } else if (args instanceof final InitArguments initArgs) {
      init(initArgs);
      return;
    } else if (args instanceof final InstallArguments installArgs) {
      install(installArgs);
      return;
    } else if (args instanceof final UninstallArguments installArgs) {
      uninstall(installArgs);
      return;
    } else if (args instanceof final ConversionArguments conversionArgs) {
      final File ditaDir = new File(System.getProperty(SYSTEM_PROPERTY_DITA_HOME));
      final File basePluginDir = new File(ditaDir, Configuration.pluginResourceDirs.get("org.dita.base").getPath());
      buildFile = findBuildFile(basePluginDir.getAbsolutePath(), "build.xml");
      definedProps.putAll(getLocalProperties(ditaDir));
      definedProps.put(USE_COLOR, Boolean.toString(conversionArgs.useColor));
      if (conversionArgs.projectFile == null) {
        projectProps = collectArguments(conversionArgs.inputs, conversionArgs.formats, definedProps);
      } else {
        projectProps = collectProperties(conversionArgs.projectFile, definedProps);
      }
      final String tempDirToken = "temp" + LocalDateTime.now().format(dateTimeFormatter);
      for (Map<String, Object> projectProp : projectProps) {
        String err = null;
        if (!projectProp.containsKey(ANT_TRANSTYPE) && !projectProp.containsKey(ANT_ARGS_INPUT)) {
          err = locale.getString("conversion.error.input_and_transformation_not_defined");
        } else if (!projectProp.containsKey(ANT_TRANSTYPE)) {
          err = locale.getString("conversion.error.transformation_not_defined");
        } else if (!projectProp.containsKey(ANT_ARGS_INPUT)) {
          err = locale.getString("conversion.error.input_not_defined");
        }
        if (err != null) {
          throw new CliException(err, args.getUsage(true));
        }
        // default values
        if (!definedProps.containsKey(ANT_OUTPUT_DIR)) {
          definedProps.put(ANT_OUTPUT_DIR, new File(new File("."), "out").getAbsolutePath());
        }
        if (!projectProp.containsKey(ANT_BASE_TEMP_DIR)) {
          projectProp.put(ANT_BASE_TEMP_DIR, new File(System.getProperty("java.io.tmpdir")).getAbsolutePath());
        }
        if (projectProp.containsKey(ANT_PROJECT_DELIVERABLE)) {
          if (projectProp.containsKey(ANT_TEMP_DIR)) {
            final Path tempDir = Paths.get(
              projectProp.get(ANT_TEMP_DIR).toString(),
              projectProp.get(ANT_PROJECT_DELIVERABLE).toString()
            );
            projectProp.put(ANT_TEMP_DIR, tempDir.toAbsolutePath().toString());
          } else {
            final Path tempDir = Paths.get(
              projectProp.get(ANT_BASE_TEMP_DIR).toString(),
              tempDirToken,
              projectProp.get(ANT_PROJECT_DELIVERABLE).toString()
            );
            projectProp.put(ANT_TEMP_DIR, tempDir.toAbsolutePath().toString());
          }
        }
      }
    } else {
      throw new RuntimeException("Command or subcommand not supported: " + args.getClass().getCanonicalName());
    }

    // make sure buildfile exists
    if (!buildFile.exists() || buildFile.isDirectory()) {
      throw new CliException("Buildfile " + buildFile + " does not exist!");
    }

    // Normalize buildFile for re-import detection
    buildFile = FileUtils.getFileUtils().normalize(buildFile.getAbsolutePath());
    logger.debug("Buildfile " + buildFile);

    if (args.logFile != null) {
      PrintStream logTo;
      try {
        logTo = new PrintStream(new FileOutputStream(args.logFile));
      } catch (final IOException ioe) {
        throw new CliException(
          "Cannot write to the specified log file. Make sure the path exists and you have write permissions."
        );
      }
      out = logTo;
      err = logTo;
      System.setOut(out);
      System.setErr(err);
    }
    readyToRun = true;
  }

  private List<Map<String, Object>> collectArguments(
    List<String> inputs,
    List<String> formats,
    Map<String, Object> definedProps
  ) {
    if (inputs.isEmpty() || formats.isEmpty()) {
      return List.of(definedProps);
    }
    final String input = inputs.get(inputs.size() - 1);
    return formats
      .stream()
      .map(format -> {
        final Map<String, Object> res = new HashMap<>(definedProps);
        res.put(ANT_ARGS_INPUT, input);
        res.put(ANT_TRANSTYPE, format);
        return res;
      })
      .toList();
  }

  private void uninstall(UninstallArguments installArgs) {
    if (installArgs.uninstallId == null) {
      throw new CliException(locale.getString("uninstall.error.identifier_not_defined"), args.getUsage(true));
    }
    final PluginUninstall pluginUninstall = new PluginUninstall();
    pluginUninstall.setId(installArgs.uninstallId);
    pluginUninstall.setDitaDir(new File(System.getProperty(SYSTEM_PROPERTY_DITA_HOME)));
    pluginUninstall.setLogger(logger);

    try {
      pluginUninstall.execute();
    } catch (CliException e) {
      throw e;
    } catch (Exception e) {
      throw new CliException(e.getMessage(), e);
    }
  }

  private void init(InitArguments initArguments) {
    if (initArguments.list) {
      printInitList();
      return;
    }

    if (initArguments.template == null) {
      throw new CliException(locale.getString("init.error.template_not_defined"), initArguments.getUsage(true));
    }
    final var target = Optional
      .ofNullable(initArguments.output)
      .orElseGet(() -> Paths.get(".").toAbsolutePath().normalize());
    final var source = getTemplateDir(initArguments.template);
    try {
      Files.walkFileTree(
        source,
        new SimpleFileVisitor<>() {
          @Override
          public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            var targetDir = target.resolve(source.relativize(dir));
            try {
              Files.copy(dir, targetDir);
            } catch (FileAlreadyExistsException e) {
              if (!Files.isDirectory(targetDir)) {
                throw e;
              }
            }
            return CONTINUE;
          }

          @Override
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            var dst = target.resolve(source.relativize(file));
            logger.debug("Create {}", dst);
            Files.copy(file, dst);
            return CONTINUE;
          }
        }
      );
      logger.info(locale.getString("init.info.create").formatted(target, initArguments.template));
    } catch (FileAlreadyExistsException e) {
      throw new BuildException(locale.getString("init.error.file_already_exists").formatted(e.getMessage()), e);
    } catch (IOException e) {
      throw new BuildException(locale.getString("init.error.create_failed").formatted(e.getMessage()), e);
    }
  }

  /**
   * Get init template source directory.
   * @param template template name
   * @return template source directory
   * @throws BuildException when template not found
   */
  private Path getTemplateDir(String template) {
    final List<Element> plugins = toList(Plugins.getPluginConfiguration().getElementsByTagName(FEATURE_ELEM));
    return plugins
      .stream()
      .filter(feature -> Objects.equals(feature.getAttribute(FEATURE_ID_ATTR), "init.template"))
      .map(feature ->
        Optional
          .ofNullable(feature.getAttributeNode("file"))
          .map(Attr::getValue)
          .map(value -> Paths.get(URI.create(feature.getBaseURI()).resolve(value)))
          .orElse(null)
      )
      .filter(dir -> Objects.equals(template, dir.getFileName().toString()))
      .filter(Files::exists)
      .findAny()
      .orElseThrow(() -> new BuildException(locale.getString("init.error.template_not_found").formatted(template)));
  }

  /**
   * Print list of init templates and their descriptions.
   */
  private void printInitList() {
    final List<Element> plugins = toList(Plugins.getPluginConfiguration().getElementsByTagName(FEATURE_ELEM));
    var templates = plugins
      .stream()
      .filter(feature -> Objects.equals(feature.getAttribute(FEATURE_ID_ATTR), "init.template"))
      .map(feature ->
        Map.entry(
          Optional
            .ofNullable(feature.getAttributeNode("file"))
            .map(Attr::getValue)
            .map(value -> Paths.get(URI.create(feature.getBaseURI()).resolve(value)).getFileName().toString())
            .orElse(null),
          Optional.ofNullable(feature.getAttributeNode("desc")).map(Attr::getValue).orElse(null)
        )
      )
      .filter(entry -> Objects.nonNull(entry.getKey()))
      .sorted(Map.Entry.comparingByKey())
      .toList();
    if (!templates.isEmpty()) {
      var width = templates
        .stream()
        .map(stringStringEntry -> stringStringEntry.getKey().length())
        .max(Integer::compare)
        .get();
      templates.forEach(dir ->
        logger.info(dir.getKey() + " ".repeat(width - dir.getKey().length()) + "  " + dir.getValue())
      );
    }
  }

  private void install(InstallArguments installArgs) {
    final PluginInstall pluginInstall = new PluginInstall();
    pluginInstall.setDitaDir(new File(System.getProperty(SYSTEM_PROPERTY_DITA_HOME)));
    pluginInstall.setLogger(logger);
    if (installArgs.definedProps.containsKey("force")) {
      pluginInstall.setForce(Boolean.parseBoolean(installArgs.definedProps.get("force").toString()));
    }
    if (installArgs.installFile != null) {
      parseInstallFile(installArgs.installFile, pluginInstall);
    }

    try {
      pluginInstall.execute();
    } catch (CliException e) {
      throw e;
    } catch (Exception e) {
      throw new CliException(e.getMessage(), e);
    }
  }

  private void parseInstallFile(String installFile, PluginInstall pluginInstall) {
    final File pluginFile = new File(installFile.replace('/', File.separatorChar)).getAbsoluteFile();
    if (pluginFile.exists()) {
      try {
        pluginInstall.setPluginFile(pluginFile.toPath());
        return;
      } catch (InvalidPathException e) {
        // Ignore
      }
    }

    try {
      final URI uri = new URI(installFile);
      if (uri.isAbsolute()) {
        pluginInstall.setPluginUri(uri);
        return;
      }
    } catch (URISyntaxException e) {
      // Ignore
    }

    if (installFile.contains("@")) {
      final String[] tokens = installFile.split("@");
      pluginInstall.setPluginName(tokens[0]);
      pluginInstall.setPluginVersion(new SemVerMatch(tokens[1]));
    } else {
      pluginInstall.setPluginName(installFile);
      pluginInstall.setPluginVersion(null);
    }
  }

  /**
   * Read configuration files and return combined map of properties. Files have the following priority:
   *
   * <ol>
   *     <li><code>{@link #CONFIGURATION_FILE .ditaotrc}</code> in current directory and recursively ancestor directories</li>
   *     <li><code>{@link #CONFIGURATION_FILE .ditaotrc}</code> in user home directory.</li>
   *     <li><code>{@link #CONFIGURATION_FILE .ditaotrc}</code> in DITA-OT installation directory.</li>
   *     <li><code>{@link #CONFIGURATION_FILE local.properties}</code> in DITA-OT installation directory.</li>
   * </ol>
   *
   * <p>Duplicate properties are overridden.</p>
   *
   * @param ditaDir DITA-OT installation directory
   * @return combined properties from configuration files
   */
  private Map<String, Object> getLocalProperties(File ditaDir) {
    final Map<String, Object> res = new HashMap<>();
    var files = Stream
      .iterate(new File("").getAbsoluteFile(), Objects::nonNull, File::getParentFile)
      .map(dir -> new File(dir, CONFIGURATION_FILE))
      .collect(Collectors.toList());
    final File homeFile = new File(new File(System.getProperty("user.home")), CONFIGURATION_FILE);
    if (!files.contains(homeFile)) {
      files.add(homeFile);
    }
    final File installDirFile = new File(ditaDir, CONFIGURATION_FILE);
    if (!files.contains(installDirFile)) {
      files.add(installDirFile);
    }
    final File installDirFileLegacy = new File(ditaDir, CONFIGURATION_FILE_OLD);
    if (!files.contains(installDirFileLegacy)) {
      files.add(installDirFileLegacy);
    }
    Collections.reverse(files);
    files.stream().filter(File::exists).map(this::readProperties).forEach(res::putAll);
    return res;
  }

  private Map<String, Object> readProperties(File localPropertiesFile) {
    logger.debug("Reading " + localPropertiesFile);
    try (InputStream in = Files.newInputStream(localPropertiesFile.toPath())) {
      final Properties localProperties = new Properties();
      localProperties.load(in);
      return localProperties
        .entrySet()
        .stream()
        .collect(Collectors.toMap(e -> e.getKey().toString(), Map.Entry::getValue));
    } catch (IOException e) {
      logger.error("Failed to read " + localPropertiesFile, e);
      return Collections.emptyMap();
    }
  }

  private List<Map<String, Object>> collectProperties(final File projectFile, final Map<String, Object> definedProps) {
    final URI base = projectFile.toURI();
    final org.dita.dost.project.Project project = readProjectFile(projectFile);

    return collectProperties(project, base, definedProps);
  }

  @VisibleForTesting
  List<Map<String, Object>> collectProperties(
    final org.dita.dost.project.Project project,
    final URI base,
    final Map<String, Object> definedProps
  ) {
    final String runDeliverable = (String) definedProps.get(ANT_PROJECT_DELIVERABLE);

    final List<Map<String, Object>> projectProps = zipWithIndex(project.deliverables())
      .filter(entry -> runDeliverable == null || Objects.equals(entry.getKey().id(), runDeliverable))
      .map(entry -> {
        final org.dita.dost.project.Project.Deliverable deliverable = entry.getKey();
        final Map<String, Object> props = new HashMap<>(definedProps);

        props.put(
          ANT_PROJECT_DELIVERABLE,
          deliverable.id() != null ? deliverable.id() : String.format("deliverable-%d", entry.getValue() + 1)
        );
        final Context context = deliverable.context();
        final URI input = base.resolve(context.inputs().inputs().get(0).href());
        props.put(ANT_ARGS_INPUT, input.toString());
        final Path output = getOutputDir(deliverable, props);
        props.put(ANT_OUTPUT_DIR, output.toString());
        final Publication publications = deliverable.publication();
        props.put(ANT_TRANSTYPE, publications.transtype());
        publications
          .params()
          .forEach(param -> {
            if (props.containsKey(param.name())) {
              return;
            }
            if (param.value() != null) {
              final Argument argument = ArgumentParser
                .getPluginArguments()
                .getOrDefault(param.name(), new StringArgument(param.name(), null));
              final String value = argument.getValue(param.value());
              props.put(param.name(), value);
            } else {
              final String value;
              final Argument argument = ArgumentParser.getPluginArguments().get("--" + param.name());
              if (argument != null && (argument instanceof FileArgument || argument instanceof AbsoluteFileArgument)) {
                if (param.href() != null) {
                  value = Paths.get(base.resolve(param.href())).toString();
                } else {
                  value = Paths.get(base).resolve(param.path()).toString();
                }
              } else {
                if (param.href() != null) {
                  value = param.href().toString();
                } else {
                  value = URLUtils.toFile(param.path().toString()).toString();
                }
              }
              props.put(param.name(), value);
            }
          });
        final List<org.dita.dost.project.Project.Deliverable.Profile.DitaVal> ditavals = Stream
          .concat(publications.profiles().ditavals().stream(), context.profiles().ditavals().stream())
          .toList();
        if (!ditavals.isEmpty()) {
          final String filters = ditavals
            .stream()
            .map(ditaVal -> Paths.get(base.resolve(ditaVal.href())).toString())
            .collect(Collectors.joining(File.pathSeparator));
          props.put("args.filter", filters);
        }

        return props;
      })
      .collect(Collectors.toList());
    if (runDeliverable != null && projectProps.isEmpty()) {
      throw new CliException(locale.getString("project.error.deliverable_not_found").formatted(runDeliverable));
    }

    return projectProps;
  }

  private List<Map<String, Object>> collectContextProperties(
    final File projectFile,
    final Map<String, Object> definedProps
  ) {
    final URI base = projectFile.toURI();
    final org.dita.dost.project.Project project = readProjectFile(projectFile);

    return collectContextProperties(project, base, definedProps);
  }

  @VisibleForTesting
  List<Map<String, Object>> collectContextProperties(
    final org.dita.dost.project.Project project,
    final URI base,
    final Map<String, Object> definedProps
  ) {
    final String runContext = (String) definedProps.get(ANT_PROJECT_CONTEXT);

    final List<Map<String, Object>> projectProps = zipWithIndex(project.contexts())
      .filter(entry -> runContext == null || Objects.equals(entry.getKey().id(), runContext))
      .map(entry -> {
        final org.dita.dost.project.Project.Context context = entry.getKey();
        final Map<String, Object> props = new HashMap<>(definedProps);

        props.put(
          ANT_PROJECT_CONTEXT,
          context.id() != null ? context.id() : String.format("context-%d", entry.getValue() + 1)
        );
        final URI input = base.resolve(context.inputs().inputs().get(0).href());
        props.put(ANT_ARGS_INPUT, input.toString());
        props.put(ANT_TRANSTYPE, "validate");
        final List<org.dita.dost.project.Project.Deliverable.Profile.DitaVal> ditavals = context
          .profiles()
          .ditavals()
          .stream()
          .toList();
        if (!ditavals.isEmpty()) {
          final String filters = ditavals
            .stream()
            .map(ditaVal -> Paths.get(base.resolve(ditaVal.href())).toString())
            .collect(Collectors.joining(File.pathSeparator));
          props.put("args.filter", filters);
        }

        return props;
      })
      .collect(Collectors.toList());
    if (runContext != null && projectProps.isEmpty()) {
      throw new CliException(locale.getString("project.error.deliverable_not_found").formatted(runContext));
    }

    return projectProps;
  }

  @VisibleForTesting
  protected Path getOutputDir(
    final org.dita.dost.project.Project.Deliverable deliverable,
    final Map<String, Object> props
  ) {
    URI outputDir = new File(props.getOrDefault(ANT_OUTPUT_DIR, "out").toString()).getAbsoluteFile().toURI();
    outputDir = outputDir.getPath().endsWith("/") ? outputDir : URLUtils.setPath(outputDir, outputDir.getPath() + "/");
    return Paths.get(deliverable.output() != null ? outputDir.resolve(deliverable.output()) : outputDir);
  }

  private org.dita.dost.project.Project readProjectFile(final File projectFile) throws BuildException {
    if (!projectFile.exists()) {
      throw new CliException(locale.getString("project.error.project_file_not_found").formatted(projectFile));
    }
    try {
      final ProjectFactory factory = ProjectFactory.getInstance();
      factory.setLax(true);
      final org.dita.dost.project.Project res = factory.load(projectFile.toURI());
      validateProject(res);
      return res;
    } catch (Exception e) {
      throw new CliException(e.getMessage());
    }
  }

  private void validateProject(org.dita.dost.project.Project project) throws IOException {
    for (org.dita.dost.project.Project.Deliverable deliverable : project.deliverables()) {
      for (Publication.Param param : deliverable.publication().params()) {
        if (RESERVED_PARAMS.containsKey(param.name())) {
          printErrorMessage(
            MessageUtils.getMessage("DOTJ085E", param.name(), RESERVED_PARAMS.get(param.name())).toString(),
            null
          );
        }
      }
    }
  }

  /**
   * Handle the --plugins argument
   */
  private void printPlugins() {
    logger.setOutputLevel(Project.MSG_INFO);
    final List<Map.Entry<String, String>> installedPlugins = Plugins.getInstalledPlugins();
    for (final Map.Entry<String, String> entry : installedPlugins) {
      if (entry.getValue() != null) {
        logger.info("{0}@{1}", entry.getKey(), entry.getValue());
      } else {
        logger.info(entry.getKey());
      }
    }
  }

  /**
   * Handle the --transtypes argument
   */
  private void printTranstypes() {
    logger.setOutputLevel(Project.MSG_INFO);
    for (final String transtype : transtypes) {
      logger.info(transtype);
    }
  }

  private void printDeliverables(DeliverablesArguments deliverablesArgs) {
    logger.setOutputLevel(Project.MSG_INFO);
    if (deliverablesArgs.projectFile == null) {
      throw new CliException(locale.getString("deliverables.error.project_not_defined"), args.getUsage(true));
    }
    final List<Map.Entry<String, String>> pairs = readProjectFile(deliverablesArgs.projectFile)
      .deliverables()
      .stream()
      .filter(deliverable -> deliverable.id() != null)
      .map(deliverable -> pair(deliverable.id(), deliverable.name()))
      .toList();
    final int length = pairs.stream().map(Map.Entry::getKey).map(String::length).reduce(Integer::max).orElse(0);
    for (Map.Entry<String, String> pair : pairs) {
      logger.info(
        Strings.padEnd(pair.getKey(), length, ' ') + (pair.getValue() != null ? ("  " + pair.getValue()) : "")
      );
    }
  }

  /**
   * Helper to get the parent file for a given file.
   * <p>
   * Added to simulate File.getParentFile() from JDK 1.2.
   *
   * @param file File to find parent of. Must not be <code>null</code>.
   * @return Parent file or null if none
   * @deprecated since 1.6.x
   */
  @Deprecated
  private File getParentFile(final File file) {
    final File parent = file.getParentFile();

    if (parent != null) {
      logger.trace("Searching in " + parent.getAbsolutePath());
    }

    return parent;
  }

  /**
   * Search parent directories for the build file.
   * <p>
   * Takes the given target as a suffix to append to each parent directory in
   * search of a build file. Once the root of the file-system has been reached
   * <code>null</code> is returned.
   *
   * @param start  Leaf directory of search. Must not be <code>null</code>.
   * @param suffix Suffix filename to look for in parents. Must not be
   *               <code>null</code>.
   * @return A handle to the build file if one is found, <code>null</code> if
   * not
   */
  private File findBuildFile(final String start, final String suffix) {
    logger.debug("Searching for " + suffix);

    File parent = new File(new File(start).getAbsolutePath());
    File file = new File(parent, suffix);

    // check if the target file exists in the current directory
    while (!file.exists()) {
      // change to parent directory
      parent = getParentFile(parent);

      // if parent is null, then we are at the root of the fs,
      // complain that we can't find the build file.
      if (parent == null) {
        return null;
      }

      // refresh our file handle
      file = new File(parent, suffix);
    }

    return file;
  }

  /**
   * Executes the build. If the constructor for this instance failed (e.g.
   * returned after issuing a warning), this method returns immediately.
   *
   * @param coreLoader   The classloader to use to find core classes. May be
   *                     <code>null</code>, in which case the system classloader is
   *                     used.
   * @param definedProps Set of properties that can be used by tasks.
   * @throws BuildException if the build fails
   */
  private void runBuild(final ClassLoader coreLoader, Map<String, Object> definedProps) throws BuildException {
    final Project project = new Project();
    project.setCoreLoader(coreLoader);

    Throwable error = null;

    try {
      addBuildListeners(project);
      addInputHandler(project);

      final PrintStream savedErr = System.err;
      final PrintStream savedOut = System.out;
      final InputStream savedIn = System.in;

      // SecurityManager can not be installed here for backwards
      // compatibility reasons (PD). Needs to be loaded prior to
      // ant class if we are going to implement it.
      // System.setSecurityManager(new NoExitSecurityManager());
      try {
        if (args.allowInput) {
          project.setDefaultInputStream(System.in);
        }
        System.setIn(new DemuxInputStream(project));
        System.setOut(new PrintStream(new DemuxOutputStream(project, false)));
        System.setErr(new PrintStream(new DemuxOutputStream(project, true)));

        project.fireBuildStarted();

        // set the thread priorities
        if (args.threadPriority != null) {
          try {
            project.log("Setting Ant's thread priority to " + args.threadPriority, Project.MSG_VERBOSE);
            Thread.currentThread().setPriority(args.threadPriority);
          } catch (final SecurityException swallowed) {
            // we cannot set the priority here.
            project.log("A security manager refused to set the -nice value");
          }
        }

        project.init();

        // resolve properties
        final PropertyHelper propertyHelper = PropertyHelper.getPropertyHelper(project);
        final HashMap<String, Object> props = new HashMap<>(definedProps);
        new ResolvePropertyMap(project, propertyHelper, propertyHelper.getExpanders())
          .resolveAllProperties(props, null, false);

        // set user-define properties
        for (final Map.Entry<String, Object> ent : props.entrySet()) {
          final String arg = ent.getKey();
          final Object value = ent.getValue();
          project.setUserProperty(arg, String.valueOf(value));
        }

        project.setUserProperty(MagicNames.ANT_FILE, buildFile.getAbsolutePath());
        project.setUserProperty(MagicNames.ANT_FILE_TYPE, MagicNames.ANT_FILE_TYPE_FILE);

        project.setKeepGoingMode(args.keepGoingMode);
        if (args.proxy) {
          // proxy setup if enabled
          final ProxySetup proxySetup = new ProxySetup(project);
          proxySetup.enableProxies();
        }

        ProjectHelper.configureProject(project, buildFile);

        // make sure that we have a target to execute
        if (targets.size() == 0) {
          if (project.getDefaultTarget() != null) {
            targets.addElement(project.getDefaultTarget());
          }
        }

        project.executeTargets(targets);
      } finally {
        System.setOut(savedOut);
        System.setErr(savedErr);
        System.setIn(savedIn);
      }
    } catch (final RuntimeException | Error exc) {
      error = exc;
      throw exc;
    } finally {
      try {
        project.fireBuildFinished(error);
      } catch (final Throwable t) {
        // yes, I know it is bad style to catch Throwable,
        // but if we don't, we lose valuable information
        printErrorMessage("Caught an exception while logging the end of the build. Exception was:", null);
        t.printStackTrace();
        if (error != null) {
          printErrorMessage("There has been an error prior to that:", null);
          error.printStackTrace();
        }
        throw new BuildException(t);
      }
    }
  }

  /**
   * Adds the listeners specified in the command line arguments, along with
   * the default listener, to the specified project.
   *
   * @param project The project to add listeners to. Must not be
   *                <code>null</code>.
   */
  @Override
  protected void addBuildListeners(final Project project) {
    // Add the default listener
    project.addBuildListener(createLogger());

    if (args.listeners != null) {
      for (String className : args.listeners) {
        final BuildListener listener = ClasspathUtils.newInstance(
          className,
          Main.class.getClassLoader(),
          BuildListener.class
        );
        project.setProjectReference(listener);
        project.addBuildListener(listener);
      }
    }
  }

  /**
   * Creates the InputHandler and adds it to the project.
   *
   * @param project the project instance.
   * @throws BuildException if a specified InputHandler implementation
   *                        could not be loaded.
   */
  private void addInputHandler(final Project project) throws BuildException {
    final InputHandler handler;
    if (args.inputHandlerClassname == null) {
      handler = new DefaultInputHandler();
    } else {
      handler = ClasspathUtils.newInstance(args.inputHandlerClassname, Main.class.getClassLoader(), InputHandler.class);
      project.setProjectReference(handler);
    }
    project.setInputHandler(handler);
  }

  // XXX: (Jon Skeet) Any reason for writing a message and then using a bare
  // RuntimeException rather than just using a BuildException here? Is it
  // in case the message could end up being written to no loggers (as the
  // loggers could have failed to be created due to this failure)?

  /**
   * Creates the default build logger for sending build events to the ant log.
   *
   * @return the logger instance for this build.
   */
  private BuildLogger createLogger() {
    BuildLogger logger;
    if (args.loggerClassname != null) {
      try {
        logger = ClasspathUtils.newInstance(args.loggerClassname, Main.class.getClassLoader(), BuildLogger.class);
      } catch (final BuildException e) {
        printErrorMessage(
          "The specified logger class " + args.loggerClassname + " could not be used because " + e.getMessage(),
          e
        );
        throw new RuntimeException();
      }
    } else if (Configuration.configuration.getOrDefault("cli.log-format", "legacy").equals("legacy")) {
      logger = new org.apache.tools.ant.DefaultLogger();
    } else {
      logger = new DefaultLogger().useColor(args.useColor).setPrintStacktrace(args.printStacktrace);
    }

    logger.setMessageOutputLevel(args.msgOutputLevel);
    logger.setOutputPrintStream(out);
    logger.setErrorPrintStream(err);
    logger.setEmacsMode(args.emacsMode);

    return logger;
  }

  /**
   * Prints the Ant version information logger.
   *
   * @throws BuildException if the version information is unavailable
   */
  private void printVersion() throws BuildException {
    logger.setOutputLevel(Project.MSG_INFO);
    logger.info(locale.getString("version").formatted(Configuration.configuration.get("otversion")));
  }
}
