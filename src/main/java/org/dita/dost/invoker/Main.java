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

import com.google.common.base.Strings;
import org.apache.tools.ant.*;
import org.apache.tools.ant.input.DefaultInputHandler;
import org.apache.tools.ant.input.InputHandler;
import org.apache.tools.ant.launch.AntMain;
import org.apache.tools.ant.property.ResolvePropertyMap;
import org.apache.tools.ant.util.ClasspathUtils;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.ant.util.ProxySetup;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.platform.Plugins;
import org.dita.dost.project.Project.Context;
import org.dita.dost.project.Project.Publication;
import org.dita.dost.project.ProjectFactory;
import org.dita.dost.util.Configuration;
import org.dita.dost.util.URLUtils;

import java.io.*;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static org.dita.dost.invoker.Arguments.*;
import static org.dita.dost.util.Configuration.transtypes;
import static org.dita.dost.util.Constants.ANT_TEMP_DIR;

/**
 * Command line entry point into DITA-OT. This class is entered via the canonical
 * `public static void main` entry point and reads the command line arguments.
 * It then assembles and executes an Ant project.
 */
public class Main extends org.apache.tools.ant.Main implements AntMain {

    private static final String ANT_ARGS_INPUT = "args.input";
    static final String ANT_ARGS_RESOURCES = "args.resources";
    static final String ANT_ARGS_INPUTS = "args.inputs";
    private static final String ANT_OUTPUT_DIR = "output.dir";
    private static final String ANT_BASE_TEMP_DIR = "base.temp.dir";
    private static final String ANT_TRANSTYPE = "transtype";
    private static final String ANT_PLUGIN_FILE = "plugin.file";
    private static final String ANT_PLUGIN_ID = "plugin.id";

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
    static final ResourceBundle locale = ResourceBundle.getBundle("cli", new Locale("en", "US"));

    /**
     * Prints the message of the Throwable if it (the message) is not {@code null}.
     *
     * @param t Throwable to print the message of. Must not be {@code null}
     */
    private void printMessage(final Throwable t) {
        final String message = t.getMessage();
        if (message != null && !message.trim().isEmpty()) {
            printErrorMessage("" + message);
        }
    }

    private void printErrorMessage(final String msg) {
        if (args != null && args.useColor) {
            System.err.print(DefaultLogger.ANSI_RED);
            System.err.print(String.format(locale.getString("error_msg"), msg));
            System.err.println(DefaultLogger.ANSI_RESET);
        } else {
            System.err.println(String.format(locale.getString("error_msg"), msg));
        }
        System.err.println();
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
    public static void start(final String[] args, final Properties additionalUserProperties,
                             final ClassLoader coreLoader) {
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
                for (final Enumeration<Object> e = additionalUserProperties.keys(); e.hasMoreElements(); ) {
                    final String key = (String) e.nextElement();
                    final String property = additionalUserProperties.getProperty(key);
                    props.put(key, property);
                }
            }
        }

        // expect the worst
        int exitCode = 1;
        try {
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
    public static void main(final String[] args) {
        start(args, null, null);
    }

    /**
     * Constructor used when creating Main for later arg processing and startup
     */
    public Main() {
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

        if (args.justPrintUsage) {
            args.printUsage(false);
            return;
        } else if (args.justPrintDiagnostics) {
            Diagnostics.doReport(System.out, args.msgOutputLevel);
            return;
        }

        final File integratorFile = findBuildFile(System.getProperty("dita.dir"), "integrator.xml");
        if (args instanceof PluginsArguments) {
            printPlugins();
            return;
        } else if (args instanceof VersionArguments) {
            printVersion();
            return;
        } else if (args instanceof TranstypesArguments) {
            printTranstypes();
            return;
        } else if (args instanceof DeliverablesArguments) {
            final DeliverablesArguments deliverablesArgs = (DeliverablesArguments) args;
            if (deliverablesArgs.projectFile == null) {
                printErrorMessage(locale.getString("deliverables.error.project_not_defined"));
                args.printUsage(true);
                throw new BuildException("");
            }
            printDeliverables(deliverablesArgs.projectFile);
            return;
        } else if (args instanceof InstallArguments) {
            final InstallArguments installArgs = (InstallArguments) args;
            buildFile = integratorFile;
            targets.clear();
            if (installArgs.installFile != null) {
                targets.add("install");
                final File f = new File(installArgs.installFile.replace('/', File.separatorChar)).getAbsoluteFile();
                if (f.exists()) {
                    definedProps.put(ANT_PLUGIN_FILE, f.getAbsolutePath());
                } else {
                    definedProps.put(ANT_PLUGIN_FILE, installArgs.installFile);
                }
            } else {
                targets.add("integrate");
            }
        } else if (args instanceof UninstallArguments) {
            final UninstallArguments installArgs = (UninstallArguments) args;
            if (installArgs.uninstallId == null) {
                printErrorMessage(locale.getString("uninstall.error.identifier_not_defined"));
                args.printUsage(true);
                throw new BuildException("");
            }
            buildFile = integratorFile;
            targets.clear();
            targets.add("uninstall");
            definedProps.put(ANT_PLUGIN_ID, installArgs.uninstallId);
        } else if (args instanceof ConversionArguments) {
            final ConversionArguments conversionArgs = (ConversionArguments) args;
            if (conversionArgs.projectFile == null) {
                String err = null;
                if (!definedProps.containsKey(ANT_TRANSTYPE) && !definedProps.containsKey(ANT_ARGS_INPUT)) {
                    err = locale.getString("conversion.error.input_and_transformation_not_defined");
                } else if (!definedProps.containsKey(ANT_TRANSTYPE)) {
                    err = locale.getString("conversion.error.transformation_not_defined");
                } else if (!definedProps.containsKey(ANT_ARGS_INPUT)) {
                    err = locale.getString("conversion.error.input_not_defined");
                }
                if (err != null) {
                    printErrorMessage(err);
                    args.printUsage(true);
                    throw new BuildException("");
                }
            } else {
                projectProps = handleProject(conversionArgs.projectFile, definedProps);
            }
            // default values
            if (!definedProps.containsKey(ANT_OUTPUT_DIR)) {
                definedProps.put(ANT_OUTPUT_DIR, new File(new File("."), "out").getAbsolutePath());
            }
            if (!definedProps.containsKey(ANT_BASE_TEMP_DIR) && !definedProps.containsKey(ANT_TEMP_DIR)) {
                definedProps.put(ANT_BASE_TEMP_DIR, new File(System.getProperty("java.io.tmpdir")).getAbsolutePath());
            }
        } else {
            throw new RuntimeException("Command or subcommand not supported: " + args.getClass().getCanonicalName());
        }

        // make sure buildfile exists
        if (!args.buildFile.exists() || buildFile.isDirectory()) {
            System.out.println("Buildfile " + buildFile + " does not exist!");
            throw new BuildException("Build failed");
        }

        // Normalize buildFile for re-import detection
        buildFile = FileUtils.getFileUtils().normalize(buildFile.getAbsolutePath());

        if (args.msgOutputLevel >= Project.MSG_VERBOSE) {
            System.out.println("Buildfile " + buildFile);
        }

        if (args.logFile != null) {
            PrintStream logTo;
            try {
                logTo = new PrintStream(new FileOutputStream(args.logFile));
            } catch (final IOException ioe) {
                throw new BuildException("Cannot write on the specified log file. "
                        + "Make sure the path exists and you have write permissions.");
            }
            out = logTo;
            err = logTo;
            System.setOut(out);
            System.setErr(err);
        }
        readyToRun = true;
    }

    private List<Map<String, Object>> handleProject(final File projectFile, final Map<String, Object> definedProps) {
        final URI base = projectFile.toURI();
        final org.dita.dost.project.Project project = readProjectFile(projectFile);
        final String runDeliverable = (String) definedProps.get("project.deliverable");

        final List<Map<String, Object>> projectProps = project.deliverables.stream()
                .filter(deliverable -> runDeliverable != null ? Objects.equals(deliverable.id, runDeliverable) : true)
                .map(deliverable -> {
                    final Map<String, Object> props = new HashMap<>(definedProps);

                    final Context context = deliverable.context;
                    final URI input = base.resolve(context.inputs.inputs.get(0).href);
                    props.put(ANT_ARGS_INPUT, input.toString());
                    URI outputDir = new File(props.get(ANT_OUTPUT_DIR).toString()).toURI();
                    outputDir = outputDir.getPath().endsWith("/")
                            ? outputDir
                            : URLUtils.setPath(outputDir, outputDir.getPath() + "/");
                    final Path output = deliverable.output != null
                            ? Paths.get(outputDir.resolve(deliverable.output))
                            : Paths.get(outputDir);
                    props.put(ANT_OUTPUT_DIR, output.toString());
                    final Publication publications = deliverable.publication;
                    props.put("transtype", publications.transtype);
                    publications.params.forEach(param -> {
                        if (props.containsKey(param.name)) {
                            return;
                        }
                        if (param.value != null) {
                            final Argument argument = ArgumentParser.getPluginArguments().getOrDefault(param.name, new StringArgument(param.name, null));
                            final String value = argument.getValue(param.value);
                            props.put(param.name, value);
                        } else {
                            final String value;
                            final Argument argument = ArgumentParser.getPluginArguments().get("--" + param.name);
                            if (argument != null && (argument instanceof FileArgument || argument instanceof AbsoluteFileArgument)) {
                                if (param.href != null) {
                                    value = Paths.get(base.resolve(param.href)).toString();
                                } else {
                                    value = Paths.get(base).resolve(param.path).toString();
                                }
                            } else {
                                if (param.href != null) {
                                    value = param.href.toString();
                                } else {
                                    value = URLUtils.toFile(param.path.toString()).toString();
                                }
                            }
                            props.put(param.name, value);
                        }
                    });
                    if (!context.profiles.ditavals.isEmpty()) {
                        final String filters = context.profiles.ditavals.stream()
                                .map(ditaVal -> Paths.get(base.resolve(ditaVal.href)).toString())
                                .collect(Collectors.joining(File.pathSeparator));
                        props.put("args.filter", filters);
                    }

                    return props;
                })
                .collect(Collectors.toList());
        if (runDeliverable != null && projectProps.isEmpty()) {
            printErrorMessage(String.format(locale.getString("project.error.deliverable_not_found"), runDeliverable));
            throw new BuildException("");
        }

        return projectProps;
    }

    private org.dita.dost.project.Project readProjectFile(final File projectFile) throws BuildException {
        if (!projectFile.exists()) {
            printErrorMessage(String.format(locale.getString("project.error.project_file_not_found"), projectFile));
            throw new BuildException("");
        }
        try {
            final ProjectFactory factory = ProjectFactory.getInstance();
            factory.setLax(true);
            return factory.load(projectFile.toURI());
        } catch (Exception e) {
            printErrorMessage(e.getMessage());
            throw new BuildException("");
        }
    }

    /**
     * Handle the --plugins argument
     */
    private void printPlugins() {
        final List<String> installedPlugins = Plugins.getInstalledPlugins();
        for (final String plugin : installedPlugins) {
            System.out.println(plugin);
        }
    }

    /**
     * Handle the --transtypes argument
     */
    private void printTranstypes() {
        for (final String transtype : transtypes) {
            System.out.println(transtype);
        }
    }

    /**
     * Handle the --deliverables argument
     */
    private void printDeliverables(final File projectFile) {
        final List<Map.Entry<String, String>> pairs = readProjectFile(projectFile).deliverables.stream()
                .filter(deliverable -> deliverable.id != null)
                .map(deliverable -> new AbstractMap.SimpleEntry<>(deliverable.id, deliverable.name))
                .collect(Collectors.toList());
        final int length = pairs.stream()
                .map(Map.Entry::getKey)
                .map(String::length)
                .reduce(Integer::max)
                .orElse(0);
        for (Map.Entry<String, String> pair : pairs) {
            System.out.println(Strings.padEnd(pair.getKey(), length, ' ')
                    + (pair.getValue() != null ? ("  " + pair.getValue()) : ""));
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

        if (parent != null && args.msgOutputLevel >= Project.MSG_VERBOSE) {
            System.out.println("Searching in " + parent.getAbsolutePath());
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
        if (args.msgOutputLevel >= Project.MSG_INFO) {
            System.out.println("Searching for " + suffix + " ...");
        }

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

            // use a system manager that prevents from System.exit()
            final SecurityManager oldsm = System.getSecurityManager();

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
                new ResolvePropertyMap(project, propertyHelper, propertyHelper.getExpanders()).resolveAllProperties(
                        props, null, false);

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
                // put back the original security manager
                // The following will never eval to true. (PD)
                if (oldsm != null) {
                    System.setSecurityManager(oldsm);
                }

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
                printErrorMessage("Caught an exception while logging the" + " end of the build.  Exception was:");
                t.printStackTrace();
                if (error != null) {
                    printErrorMessage("There has been an error prior to" + " that:");
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
                final BuildListener listener = ClasspathUtils.newInstance(className,
                        Main.class.getClassLoader(), BuildListener.class);
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
            handler = ClasspathUtils.newInstance(args.inputHandlerClassname, Main.class.getClassLoader(),
                    InputHandler.class);
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
                logger = ClasspathUtils.newInstance(args.loggerClassname, Main.class.getClassLoader(),
                        BuildLogger.class);
            } catch (final BuildException e) {
                printErrorMessage("The specified logger class " + args.loggerClassname + " could not be used because "
                        + e.getMessage());
                throw new RuntimeException();
            }
        } else {
            logger = new DefaultLogger();
            ((DefaultLogger) logger).useColor(args.useColor);
        }

        logger.setMessageOutputLevel(args.msgOutputLevel);
        logger.setOutputPrintStream(out);
        logger.setErrorPrintStream(err);
        logger.setEmacsMode(args.emacsMode);

        return logger;
    }

    /**
     * Prints the Ant version information to <code>System.out</code>.
     *
     * @throws BuildException if the version information is unavailable
     */
    private void printVersion() throws BuildException {
        System.out.println(String.format(locale.getString("version"), Configuration.configuration.get("otversion")));
    }
}
