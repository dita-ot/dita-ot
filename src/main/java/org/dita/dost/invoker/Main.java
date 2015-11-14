/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.BuildLogger;
import org.apache.tools.ant.DemuxInputStream;
import org.apache.tools.ant.DemuxOutputStream;
import org.apache.tools.ant.Diagnostics;
import org.apache.tools.ant.ExitStatusException;
import org.apache.tools.ant.Location;
import org.apache.tools.ant.MagicNames;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.ProjectHelperRepository;
import org.apache.tools.ant.PropertyHelper;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.input.DefaultInputHandler;
import org.apache.tools.ant.input.InputHandler;
import org.apache.tools.ant.launch.AntMain;
import org.apache.tools.ant.property.ResolvePropertyMap;
import org.apache.tools.ant.util.ClasspathUtils;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.ant.util.ProxySetup;
import org.dita.dost.util.Configuration;

/**
 * Command line entry point into DITA-OT. This class is entered via the canonical
 * `public static void main` entry point and reads the command line arguments.
 * It then assembles and executes an Ant project.
 */
public class Main extends org.apache.tools.ant.Main implements AntMain {

    private static abstract class Argument {
        final String property;

        Argument(final String property) {
            this.property = property;
        }

        abstract String getValue(final String value);
    }

    private static class StringArgument extends Argument {
        StringArgument(final String property) {
            super(property);
        }

        @Override
        String getValue(final String value) {
            return value;
        }
    }

    private static class FileArgument extends Argument {
        FileArgument(final String property) {
            super(property);
        }

        @Override
        String getValue(final String value) {
            return new File(value).getAbsolutePath();
        }
    }

    private static class FileOrUriArgument extends Argument {
        FileOrUriArgument(final String property) {
            super(property);
        }

        @Override
        String getValue(final String value) {
            final File f = new File(value);
            if (f.exists()) {
                return f.getAbsolutePath();
            } else {
                return value;
            }
        }
    }

    /**
     * A Set of args are are handled by the launcher and should not be seen by
     * Main.
     */
    private static final Set<String> LAUNCH_COMMANDS = new HashSet<>();
    static {
        LAUNCH_COMMANDS.add("-lib");
        LAUNCH_COMMANDS.add("-cp");
        LAUNCH_COMMANDS.add("-noclasspath");
        LAUNCH_COMMANDS.add("--noclasspath");
        LAUNCH_COMMANDS.add("-nouserlib");
        LAUNCH_COMMANDS.add("-main");
    }

    private static final Map<String, Argument> ARGUMENTS = new HashMap<>();
    static {
        ARGUMENTS.put("-f", new StringArgument("transtype"));
        ARGUMENTS.put("-format", new StringArgument("transtype"));
        ARGUMENTS.put("-transtype", new StringArgument("transtype"));
        ARGUMENTS.put("-i", new FileOrUriArgument("args.input"));
        ARGUMENTS.put("-input", new FileOrUriArgument("args.input"));
        ARGUMENTS.put("-o", new FileArgument("output.dir"));
        ARGUMENTS.put("-output", new FileArgument("output.dir"));
        ARGUMENTS.put("-filter", new FileArgument("args.filter"));
        ARGUMENTS.put("-t", new FileArgument("dita.temp.dir"));
        ARGUMENTS.put("-temp", new FileArgument("dita.temp.dir"));
    }
    private static final Map<String, String> RESERVED_PROPERTIES = new HashMap<>();
    static {
        for (final Map.Entry<String, Argument> a: ARGUMENTS.entrySet()) {
            RESERVED_PROPERTIES.put(a.getValue().property, a.getKey());
        }
    }

    private static final Map<String, Argument> LEGACY_ARGUMENTS = new HashMap<>();
    static {
        // LEGACY_ARGUMENTS.put("/basedir", new StringArgument("basedir"));
        // LEGACY_ARGUMENTS.put("/ditadir", new StringArgument("dita.dir"));
        LEGACY_ARGUMENTS.put("/i", new FileArgument("args.input"));
        LEGACY_ARGUMENTS.put("/if", new FileArgument("dita.input"));
        LEGACY_ARGUMENTS.put("/id", new FileArgument("dita.input.dirname"));
        LEGACY_ARGUMENTS.put("/artlbl", new StringArgument("args.artlbl"));
        LEGACY_ARGUMENTS.put("/draft", new StringArgument("args.draft"));
        LEGACY_ARGUMENTS.put("/ftr", new StringArgument("args.ftr"));
        LEGACY_ARGUMENTS.put("/hdr", new StringArgument("args.hdr"));
        LEGACY_ARGUMENTS.put("/hdf", new StringArgument("args.hdf"));
        LEGACY_ARGUMENTS.put("/csspath", new StringArgument("args.csspath"));
        LEGACY_ARGUMENTS.put("/cssroot", new StringArgument("args.cssroot"));
        LEGACY_ARGUMENTS.put("/css", new StringArgument("args.css"));
        LEGACY_ARGUMENTS.put("/filter", new FileArgument("args.filter"));
        LEGACY_ARGUMENTS.put("/outdir", new FileArgument("output.dir"));
        LEGACY_ARGUMENTS.put("/transtype", new StringArgument("transtype"));
        LEGACY_ARGUMENTS.put("/indexshow", new StringArgument("args.indexshow"));
        LEGACY_ARGUMENTS.put("/outext", new StringArgument("args.outext"));
        LEGACY_ARGUMENTS.put("/copycss", new StringArgument("args.copycss"));
        LEGACY_ARGUMENTS.put("/xsl", new FileArgument("args.xsl"));
        LEGACY_ARGUMENTS.put("/xslpdf", new FileArgument("args.xsl.pdf"));
        LEGACY_ARGUMENTS.put("/tempdir", new FileArgument("dita.temp.dir"));
        LEGACY_ARGUMENTS.put("/cleantemp", new StringArgument("clean.temp"));
        LEGACY_ARGUMENTS.put("/foimgext", new StringArgument("args.fo.img.ext"));
        LEGACY_ARGUMENTS.put("/javahelptoc", new StringArgument("args.javahelp.toc"));
        LEGACY_ARGUMENTS.put("/javahelpmap", new StringArgument("args.javahelp.map"));
        LEGACY_ARGUMENTS.put("/eclipsehelptoc", new StringArgument("args.eclipsehelp.toc"));
        LEGACY_ARGUMENTS.put("/eclipsecontenttoc", new StringArgument("args.eclipsecontent.toc"));
        LEGACY_ARGUMENTS.put("/xhtmltoc", new StringArgument("args.xhtml.toc"));
        LEGACY_ARGUMENTS.put("/xhtmlclass", new StringArgument("args.xhtml.classattr"));
        LEGACY_ARGUMENTS.put("/usetasklabels", new StringArgument("args.gen.task.lbl"));
        LEGACY_ARGUMENTS.put("/logdir", new FileArgument("args.logdir"));
        LEGACY_ARGUMENTS.put("/ditalocale", new StringArgument("args.dita.locale"));
        LEGACY_ARGUMENTS.put("/fooutputrellinks", new StringArgument("args.fo.output.rel.links"));
        LEGACY_ARGUMENTS.put("/foincluderellinks", new StringArgument("args.fo.include.rellinks"));
        LEGACY_ARGUMENTS.put("/odtincluderellinks", new StringArgument("args.odt.include.rellinks"));
        LEGACY_ARGUMENTS.put("/retaintopicfo", new StringArgument("retain.topic.fo"));
        LEGACY_ARGUMENTS.put("/version", new StringArgument("args.eclipse.version"));
        LEGACY_ARGUMENTS.put("/provider", new StringArgument("args.eclipse.provider"));
        LEGACY_ARGUMENTS.put("/fouserconfig", new StringArgument("args.fo.userconfig"));
        LEGACY_ARGUMENTS.put("/htmlhelpincludefile", new StringArgument("args.htmlhelp.includefile"));
        LEGACY_ARGUMENTS.put("/validate", new StringArgument("validate"));
        LEGACY_ARGUMENTS.put("/outercontrol", new StringArgument("outer.control"));
        LEGACY_ARGUMENTS.put("/generateouter", new StringArgument("generate.copy.outer"));
        LEGACY_ARGUMENTS.put("/onlytopicinmap", new StringArgument("onlytopic.in.map"));
        LEGACY_ARGUMENTS.put("/debug", new StringArgument("args.debug"));
        LEGACY_ARGUMENTS.put("/grammarcache", new StringArgument("args.grammar.cache"));
        LEGACY_ARGUMENTS.put("/odtimgembed", new StringArgument("args.odt.img.embed"));
    }

    /** The default build file name. {@value} */
    public static final String DEFAULT_BUILD_FILENAME = "build.xml";

    /** Our current message output status. Follows Project.MSG_XXX. */
    private int msgOutputLevel = Project.MSG_WARN;

    /** File that we are using for configuration. */
    private File buildFile; /* null */
    /** Run integrator */
    private boolean install;
    /** Plug-in installation file. May be either a system path or a URL. */
    private String installFile;
    
    /** Plug-in uninstall ID. */
    private String uninstallId;

    /** Stream to use for logging. */
    private static PrintStream out = System.out;

    /** Stream that we are using for logging error messages. */
    private static PrintStream err = System.err;

    /** The build targets. */
    private final Vector<String> targets = new Vector<>();

    /** Set of properties that can be used by tasks. */
    private final Map<String, Object> definedProps = new HashMap<>();

    /** Names of classes to add as listeners to project. */
    private final Vector<String> listeners = new Vector<>(1);

    /** File names of property files to load on startup. */
    private final Vector<String> propertyFiles = new Vector<>(1);

    /** Indicates whether this build is to support interactive input */
    private boolean allowInput = true;

    /** keep going mode */
    private boolean keepGoingMode = false;

    /**
     * The Ant logger class. There may be only one logger. It will have the
     * right to use the 'out' PrintStream. The class must implements the
     * BuildLogger interface.
     */
    private String loggerClassname = null;

    /**
     * The Ant InputHandler class. There may be only one input handler.
     */
    private String inputHandlerClassname = null;

    /**
     * Whether or not output to the log is to be unadorned.
     */
    private boolean emacsMode = false;

    /**
     * Whether or not this instance has successfully been constructed and is
     * ready to run.
     */
    private boolean readyToRun = false;

    /**
     * Whether or not we should only parse and display the project help
     * information.
     */
    private boolean projectHelp = false;

    /**
     * Whether or not a logfile is being used. This is used to check if the
     * output streams must be closed.
     */
    private static boolean isLogFileUsed = false;

    /**
     * optional thread priority
     */
    private Integer threadPriority = null;

    /**
     * proxy flag: default is false
     */
    private boolean proxy = false;

    /**
     * Prints the message of the Throwable if it (the message) is not
     * <code>null</code>.
     * 
     * @param t Throwable to print the message of. Must not be <code>null</code>
     *            .
     */
    private static void printMessage(final Throwable t) {
        final String message = t.getMessage();
        if (message != null && !message.trim().isEmpty()) {
            System.err.println("Error: " + message);
        }
    }

    /**
     * Creates a new instance of this class using the arguments specified, gives
     * it any extra user properties which have been specified, and then runs the
     * build using the classloader provided.
     * 
     * @param args Command line arguments. Must not be <code>null</code>.
     * @param additionalUserProperties Any extra properties to use in this
     *            build. May be <code>null</code>, which is the equivalent to
     *            passing in an empty set of properties.
     * @param coreLoader Classloader used for core classes. May be
     *            <code>null</code> in which case the system classloader is
     *            used.
     */
    public static void start(final String[] args, final Properties additionalUserProperties,
            final ClassLoader coreLoader) {
        final Main m = new Main();
        m.startAnt(args, additionalUserProperties, coreLoader);
    }

    /**
     * Start Ant
     * 
     * @param args command line args
     * @param additionalUserProperties properties to set beyond those that may
     *            be specified on the args list
     * @param coreLoader - not used
     * 
     * @since Ant 1.6
     */
    @Override
    public void startAnt(final String[] args, final Properties additionalUserProperties, final ClassLoader coreLoader) {

        try {
            processArgs(args);
        } catch (final Throwable exc) {
            handleLogfile();
            printMessage(exc);
            exit(1);
            return;
        }

        if (additionalUserProperties != null) {
            for (final Enumeration<Object> e = additionalUserProperties.keys(); e.hasMoreElements();) {
                final String key = (String) e.nextElement();
                final String property = additionalUserProperties.getProperty(key);
                definedProps.put(key, property);
            }
        }

        // expect the worst
        int exitCode = 1;
        try {
            try {
                runBuild(coreLoader);
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
    private static void handleLogfile() {
        if (isLogFileUsed) {
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
     * @param args the command line arguments.
     * 
     * @since Ant 1.6
     */
    private void processArgs(final String[] args) {
        String searchForThis = null;
        boolean searchForFile = false;
        PrintStream logTo = null;

        // cycle through given args

        boolean justPrintUsage = false;
        boolean justPrintVersion = false;
        boolean justPrintDiagnostics = false;

        for (int i = 0; i < args.length; i++) {
            final String arg = args[i];

            if (arg.equals("-help") || arg.equals("-h")) {
                justPrintUsage = true;
            } else if (arg.equals("-version")) {
                justPrintVersion = true;
            } else if (arg.equals("-install")) {
                i = handleArgInstall(args, i);
            } else if (arg.equals("-uninstall")) {
                i = handleArgUninstall(args, i);
            } else if (arg.equals("-diagnostics")) {
                justPrintDiagnostics = true;
                // } else if (arg.equals("-quiet") || arg.equals("-q")) {
                // msgOutputLevel = Project.MSG_WARN;
            } else if (arg.equals("-verbose") || arg.equals("-v")) {
                msgOutputLevel = Project.MSG_INFO;
            } else if (arg.equals("-debug") || arg.equals("-d")) {
                msgOutputLevel = Project.MSG_VERBOSE;
            } else if (arg.equals("-noinput")) {
                allowInput = false;
            } else if (arg.equals("-logfile") || arg.equals("-l")) {
                try {
                    final File logFile = new File(args[i + 1]);
                    i++;
                    logTo = new PrintStream(new FileOutputStream(logFile));
                    isLogFileUsed = true;
                } catch (final IOException ioe) {
                    final String msg = "Cannot write on the specified log file. "
                            + "Make sure the path exists and you have write " + "permissions.";
                    throw new BuildException(msg);
                } catch (final ArrayIndexOutOfBoundsException aioobe) {
                    final String msg = "You must specify a log file when " + "using the -log argument";
                    throw new BuildException(msg);
                }
            } else if (arg.equals("-buildfile") || arg.equals("-file")) { //|| arg.equals("-f")
                i = handleArgBuildFile(args, i);
            } else if (arg.equals("-listener")) {
                i = handleArgListener(args, i);
            } else if (arg.startsWith("-D")) {
                i = handleArgDefine(args, i);
            } else if (arg.equals("-logger")) {
                i = handleArgLogger(args, i);
            } else if (arg.equals("-inputhandler")) {
                i = handleArgInputHandler(args, i);
            } else if (arg.equals("-emacs") || arg.equals("-e")) {
                emacsMode = true;
            } else if (arg.equals("-projecthelp") || arg.equals("-p")) {
                // set the flag to display the targets and quit
                projectHelp = true;
            } else if (arg.equals("-find") || arg.equals("-s")) {
                searchForFile = true;
                // eat up next arg if present, default to build.xml
                if (i < args.length - 1) {
                    searchForThis = args[++i];
                }
            } else if (arg.startsWith("-propertyfile")) {
                i = handleArgPropertyFile(args, i);
            } else if (arg.equals("-k") || arg.equals("-keep-going")) {
                keepGoingMode = true;
            } else if (arg.equals("-nice")) {
                i = handleArgNice(args, i);
            } else if (ARGUMENTS.containsKey(getArgumentName(arg))) {
                i = handleParameterArg(args, i);
            } else if (LEGACY_ARGUMENTS.containsKey(getArgumentName(arg))) {
                i = handleLegacyParameterArg(args, i);
            } else if (LAUNCH_COMMANDS.contains(arg)) {
                // catch script/ant mismatch with a meaningful message
                // we could ignore it, but there are likely to be other
                // version problems, so we stamp down on the configuration now
                final String msg = "Ant's Main method is being handed " + "an option " + arg
                        + " that is only for the launcher class."
                        + "\nThis can be caused by a version mismatch between "
                        + "the ant script/.bat file and Ant itself.";
                throw new BuildException(msg);
            } else if (arg.equals("-autoproxy")) {
                proxy = true;
            } else if (arg.startsWith("-") || arg.startsWith("/")) {
                // we don't have any more args to recognize!
                final String msg = "Error: Unknown argument: " + arg;
                System.err.println(msg);
                printUsage();
                throw new BuildException("");
            } else {
                // if it's no other arg, it may be the target
                targets.addElement(arg);
            }
        }

        if (justPrintUsage || justPrintVersion || justPrintDiagnostics) {
            if (justPrintVersion) {
                printVersion(msgOutputLevel);
            }
            if (justPrintUsage) {
                printUsage();
            }
            if (justPrintDiagnostics) {
                Diagnostics.doReport(System.out, msgOutputLevel);
            }
            return;
        } else if (install) {
            buildFile = findBuildFile(System.getProperty("dita.dir"), "integrator.xml");
            targets.clear();
            if (installFile != null) {
                targets.add("install");                
                final File f = new File(installFile.replace('/', File.separatorChar)).getAbsoluteFile();
                if (f.exists()) {
                    definedProps.put("plugin.file", f.getAbsolutePath());
                } else {
                    definedProps.put("plugin.file", installFile);
                }
            } else if (uninstallId != null) {
                targets.add("uninstall");
                definedProps.put("plugin.id", uninstallId);
            } else {
                targets.add("integrate");
            }
        } else {
            if (!definedProps.containsKey("transtype")) {
                System.err.println("Error: Transformation type not defined");
                printUsage();
                throw new BuildException("");
                //justPrintUsage = true;
            }
            if (!definedProps.containsKey("args.input")) {
                System.err.println("Error: Input file not defined");
                printUsage();
                throw new BuildException("");
                //justPrintUsage = true;
            }
            // default values
            if (!definedProps.containsKey("output.dir")) {
                definedProps.put("output.dir", new File(new File("."), "out").getAbsolutePath());
            }
            if (!definedProps.containsKey("base.temp.dir") && !definedProps.containsKey("dita.temp.dir")) {
                definedProps.put("base.temp.dir", new File(System.getProperty("java.io.tmpdir")).getAbsolutePath());
            }
        }

        // if buildFile was not specified on the command line,
        if (buildFile == null) {
            // but -find then search for it
            if (searchForFile) {
                if (searchForThis != null) {
                    buildFile = findBuildFile(System.getProperty("user.dir"), searchForThis);
                    if (buildFile == null) {
                        throw new BuildException("Could not locate a build file!");
                    }
                } else {
                    // no search file specified: so search an existing default
                    // file
                    final Iterator<ProjectHelper> it = ProjectHelperRepository.getInstance().getHelpers();
                    do {
                        final ProjectHelper helper = it.next();
                        searchForThis = helper.getDefaultBuildFile();
                        if (msgOutputLevel >= Project.MSG_VERBOSE) {
                            System.out.println("Searching the default build file: " + searchForThis);
                        }
                        buildFile = findBuildFile(System.getProperty("user.dir"), searchForThis);
                    } while (buildFile == null && it.hasNext());
                    if (buildFile == null) {
                        throw new BuildException("Could not locate a build file!");
                    }
                }
            } else {
                // no build file specified: so search an existing default file
                final Iterator<ProjectHelper> it = ProjectHelperRepository.getInstance().getHelpers();
                do {
                    final ProjectHelper helper = it.next();
                    buildFile = new File(helper.getDefaultBuildFile());
                    if (msgOutputLevel >= Project.MSG_VERBOSE) {
                        System.out.println("Trying the default build file: " + buildFile);
                    }
                } while (!buildFile.exists() && it.hasNext());
            }
        }

        // make sure buildfile exists
        if (!buildFile.exists()) {
            System.out.println("Buildfile: " + buildFile + " does not exist!");
            throw new BuildException("Build failed");
        }

        if (buildFile.isDirectory()) {
            final File whatYouMeant = new File(buildFile, "build.xml");
            if (whatYouMeant.isFile()) {
                buildFile = whatYouMeant;
            } else {
                System.out.println("What? Buildfile: " + buildFile + " is a dir!");
                throw new BuildException("Build failed");
            }
        }

        // Normalize buildFile for re-import detection
        buildFile = FileUtils.getFileUtils().normalize(buildFile.getAbsolutePath());

        // Load the property files specified by -propertyfile
        loadPropertyFiles();

        if (msgOutputLevel >= Project.MSG_VERBOSE) {
            System.out.println("Buildfile: " + buildFile);
        }

        if (logTo != null) {
            out = logTo;
            err = logTo;
            System.setOut(out);
            System.setErr(err);
        }
        readyToRun = true;
    }

    // --------------------------------------------------------
    // Methods for handling the command line arguments
    // --------------------------------------------------------

    /** Handle the -install argument */
    private int handleArgInstall(final String[] args, int pos) {
        install = true;
        if (pos + 1 < args.length && !args[pos + 1].startsWith("-")) {
            installFile = args[++pos];
        }
        return pos;
    }
    
    /** Handle the -uninstall argument */
    private int handleArgUninstall(final String[] args, int pos) {
        try {
            install = true;
            uninstallId = args[++pos];
        } catch (final ArrayIndexOutOfBoundsException aioobe) {
            throw new BuildException("You must specify a installation package when using the -uninstall argument");
        }
        return pos;
    }
    
    /** Handle the -buildfile, -file, -f argument */
    private int handleArgBuildFile(final String[] args, int pos) {
        try {
            buildFile = new File(args[++pos].replace('/', File.separatorChar));
        } catch (final ArrayIndexOutOfBoundsException aioobe) {
            throw new BuildException("You must specify a buildfile when using the -buildfile argument");
        }
        return pos;
    }

    /** Handle -listener argument */
    private int handleArgListener(final String[] args, int pos) {
        try {
            listeners.addElement(args[pos + 1]);
            pos++;
        } catch (final ArrayIndexOutOfBoundsException aioobe) {
            final String msg = "You must specify a classname when " + "using the -listener argument";
            throw new BuildException(msg);
        }
        return pos;
    }

    /** Handler -D argument */
    private int handleArgDefine(final String[] args, int argPos) {
        /*
         * Interestingly enough, we get to here when a user uses -Dname=value.
         * However, in some cases, the OS goes ahead and parses this out to args
         * {"-Dname", "value"} so instead of parsing on "=", we just make the
         * "-D" characters go away and skip one argument forward.
         * 
         * I don't know how to predict when the JDK is going to help or not, so
         * we simply look for the equals sign.
         */
        final String arg = args[argPos];
        String name = arg.substring(2, arg.length());
        String value = null;
        final int posEq = name.indexOf("=");
        if (posEq > 0) {
            value = name.substring(posEq + 1);
            name = name.substring(0, posEq);
        } else if (argPos < args.length - 1) {
            value = args[++argPos];
        } else {
            throw new BuildException("Missing value for property " + name);
        }
        
        if (RESERVED_PROPERTIES.containsKey(name)) {
            throw new BuildException("Property " + name + " cannot be set with -D, use " + RESERVED_PROPERTIES.get(name) + " instead");
        }
        definedProps.put(name, value);
        return argPos;
    }

    /** Handler parameter argument */
    private int handleParameterArg(final String[] args, int argPos) {
        final String arg = args[argPos];
        String name = arg.substring(0, arg.length());
        String value = null;
        int posEq = name.indexOf("=");
        if (posEq > 0) {
            value = name.substring(posEq + 1);
            name = name.substring(0, posEq);
        } else if (argPos < args.length - 1) {
            value = args[++argPos];
        } else {
            throw new BuildException("Missing value for property " + name);
        }
        final Argument a = ARGUMENTS.get(name);
        
        definedProps.put(a.property, a.getValue(value));
        return argPos;
    }
    
    /** Handler legacy parameter argument */
    private int handleLegacyParameterArg(final String[] args, int argPos) {
        final String arg = args[argPos];
        String name = arg.substring(0, arg.length());
        String value = null;
        int posEq = name.indexOf(":");
        if (posEq > 0) {
            value = name.substring(posEq + 1);
            name = name.substring(0, posEq);
        } else if (argPos < args.length - 1) {
            value = args[++argPos];
        } else {
            throw new BuildException("Missing value for property " + name);
        }
        final Argument a = LEGACY_ARGUMENTS.get(name);
        
        definedProps.put(a.property, a.getValue(value));
        return argPos;
    }

    /** Get argument name */
    private String getArgumentName(final String arg) {
        int pos = arg.indexOf("=");
        if (pos == -1) {
            pos = arg.indexOf(":");
        }
        return arg.substring(0, pos != -1 ? pos : arg.length());
    }

    /** Handle the -logger argument. */
    private int handleArgLogger(final String[] args, int pos) {
        if (loggerClassname != null) {
            throw new BuildException("Only one logger class may be specified.");
        }
        try {
            loggerClassname = args[++pos];
        } catch (final ArrayIndexOutOfBoundsException aioobe) {
            throw new BuildException("You must specify a classname when using the -logger argument");
        }
        return pos;
    }

    /** Handle the -inputhandler argument. */
    private int handleArgInputHandler(final String[] args, int pos) {
        if (inputHandlerClassname != null) {
            throw new BuildException("Only one input handler class may " + "be specified.");
        }
        try {
            inputHandlerClassname = args[++pos];
        } catch (final ArrayIndexOutOfBoundsException aioobe) {
            throw new BuildException("You must specify a classname when" + " using the -inputhandler" + " argument");
        }
        return pos;
    }

    /** Handle the -propertyfile argument. */
    private int handleArgPropertyFile(final String[] args, int pos) {
        try {
            propertyFiles.addElement(args[++pos]);
        } catch (final ArrayIndexOutOfBoundsException aioobe) {
            final String msg = "You must specify a property filename when " + "using the -propertyfile argument";
            throw new BuildException(msg);
        }
        return pos;
    }

    /** Handle the -nice argument. */
    private int handleArgNice(final String[] args, int pos) {
        try {
            threadPriority = Integer.decode(args[++pos]);
        } catch (final ArrayIndexOutOfBoundsException aioobe) {
            throw new BuildException("You must supply a niceness value (1-10)" + " after the -nice option");
        } catch (final NumberFormatException e) {
            throw new BuildException("Unrecognized niceness value: " + args[pos]);
        }

        if (threadPriority < Thread.MIN_PRIORITY || threadPriority > Thread.MAX_PRIORITY) {
            throw new BuildException("Niceness value is out of the range 1-10");
        }
        return pos;
    }

    /** Load the property files specified by -propertyfile */
    private void loadPropertyFiles() {
        for (int propertyFileIndex = 0; propertyFileIndex < propertyFiles.size(); propertyFileIndex++) {
            final String filename = propertyFiles.elementAt(propertyFileIndex);
            final Properties props = new Properties();
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(filename);
                props.load(fis);
            } catch (final IOException e) {
                System.out.println("Could not load property file " + filename + ": " + e.getMessage());
            } finally {
                FileUtils.close(fis);
            }

            // ensure that -D properties take precedence
            final Enumeration propertyNames = props.propertyNames();
            while (propertyNames.hasMoreElements()) {
                final String name = propertyNames.nextElement().toString();
                if (!definedProps.containsKey(name)) {
                    definedProps.put(name, props.getProperty(name));
                }
            }
        }
    }

    /**
     * Helper to get the parent file for a given file.
     * <p>
     * Added to simulate File.getParentFile() from JDK 1.2.
     * 
     * @deprecated since 1.6.x
     * 
     * @param file File to find parent of. Must not be <code>null</code>.
     * @return Parent file or null if none
     */
    @Deprecated
    private File getParentFile(final File file) {
        final File parent = file.getParentFile();

        if (parent != null && msgOutputLevel >= Project.MSG_VERBOSE) {
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
     * @param start Leaf directory of search. Must not be <code>null</code>.
     * @param suffix Suffix filename to look for in parents. Must not be
     *            <code>null</code>.
     * 
     * @return A handle to the build file if one is found, <code>null</code> if
     *         not
     */
    private File findBuildFile(final String start, final String suffix) {
        if (msgOutputLevel >= Project.MSG_INFO) {
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
     * @param coreLoader The classloader to use to find core classes. May be
     *            <code>null</code>, in which case the system classloader is
     *            used.
     * 
     * @exception BuildException if the build fails
     */
    private void runBuild(final ClassLoader coreLoader) throws BuildException {

        if (!readyToRun) {
            return;
        }

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
            SecurityManager oldsm = null;
            oldsm = System.getSecurityManager();

            // SecurityManager can not be installed here for backwards
            // compatibility reasons (PD). Needs to be loaded prior to
            // ant class if we are going to implement it.
            // System.setSecurityManager(new NoExitSecurityManager());
            try {
                if (allowInput) {
                    project.setDefaultInputStream(System.in);
                }
                System.setIn(new DemuxInputStream(project));
                System.setOut(new PrintStream(new DemuxOutputStream(project, false)));
                System.setErr(new PrintStream(new DemuxOutputStream(project, true)));

                if (!projectHelp) {
                    project.fireBuildStarted();
                }

                // set the thread priorities
                if (threadPriority != null) {
                    try {
                        project.log("Setting Ant's thread priority to " + threadPriority, Project.MSG_VERBOSE);
                        Thread.currentThread().setPriority(threadPriority);
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

                project.setKeepGoingMode(keepGoingMode);
                if (proxy) {
                    // proxy setup if enabled
                    final ProxySetup proxySetup = new ProxySetup(project);
                    proxySetup.enableProxies();
                }

                ProjectHelper.configureProject(project, buildFile);

                if (projectHelp) {
                    printDescription(project);
                    printTargets(project, msgOutputLevel > Project.MSG_INFO, msgOutputLevel > Project.MSG_VERBOSE);
                    return;
                }

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
            if (!projectHelp) {
                try {
                    project.fireBuildFinished(error);
                } catch (final Throwable t) {
                    // yes, I know it is bad style to catch Throwable,
                    // but if we don't, we lose valuable information
                    System.err.println("Caught an exception while logging the" + " end of the build.  Exception was:");
                    t.printStackTrace();
                    if (error != null) {
                        System.err.println("There has been an error prior to" + " that:");
                        error.printStackTrace();
                    }
                    throw new BuildException(t);
                }
            } else if (error != null) {
                project.log(error.toString(), Project.MSG_ERR);
            }
        }
    }

    /**
     * Adds the listeners specified in the command line arguments, along with
     * the default listener, to the specified project.
     * 
     * @param project The project to add listeners to. Must not be
     *            <code>null</code>.
     */
    @Override
    protected void addBuildListeners(final Project project) {

        // Add the default listener
        project.addBuildListener(createLogger());

        final int count = listeners.size();
        for (int i = 0; i < count; i++) {
            final String className = listeners.elementAt(i);
            final BuildListener listener = (BuildListener) ClasspathUtils.newInstance(className,
                    Main.class.getClassLoader(), BuildListener.class);
            project.setProjectReference(listener);

            project.addBuildListener(listener);
        }
    }

    /**
     * Creates the InputHandler and adds it to the project.
     * 
     * @param project the project instance.
     * 
     * @exception BuildException if a specified InputHandler implementation
     *                could not be loaded.
     */
    private void addInputHandler(final Project project) throws BuildException {
        InputHandler handler = null;
        if (inputHandlerClassname == null) {
            handler = new DefaultInputHandler();
        } else {
            handler = (InputHandler) ClasspathUtils.newInstance(inputHandlerClassname, Main.class.getClassLoader(),
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
        BuildLogger logger = null;
        if (loggerClassname != null) {
            try {
                logger = (BuildLogger) ClasspathUtils.newInstance(loggerClassname, Main.class.getClassLoader(),
                        BuildLogger.class);
            } catch (final BuildException e) {
                System.err.println("The specified logger class " + loggerClassname + " could not be used because "
                        + e.getMessage());
                throw new RuntimeException();
            }
        } else {
            logger = new DefaultLogger();
        }

        logger.setMessageOutputLevel(msgOutputLevel);
        logger.setOutputPrintStream(out);
        logger.setErrorPrintStream(err);
        logger.setEmacsMode(emacsMode);

        return logger;
    }

    /**
     * Prints the usage information for this class to <code>System.out</code>.
     */
    private static void printUsage() {
        final StringBuilder msg = new StringBuilder();
        msg.append("Usage: dita -i <file> -f <name> [options]\n");
        msg.append("   or: dita -install [<file>]\n");
        msg.append("   or: dita -uninstall <id>\n");
        msg.append("   or: dita -help\n");
        msg.append("   or: dita -version\n");
        msg.append("Arguments: \n");
        msg.append("  -i, -input <file>      input file\n");
        msg.append("  -f, -format <name>     output format (transformation type)\n");
        msg.append("  -install [<file>]      install plug-in from a ZIP file or reload plugins\n");
        msg.append("  -uninstall <id>        uninstall plug-in with the ID\n");
        msg.append("  -h, -help              print this message\n");
        msg.append("  -version               print version information and exit\n");
        msg.append("Options: \n");
        msg.append("  -o, -output <dir>      output directory\n");
        // msg.append("  -projecthelp, -p       print project help information" + lSep);
        // msg.append("  -diagnostics           print information that might be helpful to"
        // + lSep);
        // msg.append("                         diagnose or report problems." +
        // lSep);
        // msg.append("  -quiet, -q             be extra quiet" + lSep);
        msg.append("  -filter <file>         filter and flagging file\n");
        msg.append("  -t, -temp <dir>        temporary directory\n");
        msg.append("  -v, -verbose           verbose logging\n");
        msg.append("  -d, -debug             print debugging information\n");
        // msg.append("  -emacs, -e             produce logging information without adornments"
        // + lSep);
        // msg.append("  -lib <path>            specifies a path to search for jars and classes"
        // + lSep);
        msg.append("  -l, logfile <file>     use given file for log\n");
        // msg.append("  -logger <classname>    the class which is to perform logging"
        // + lSep);
        // msg.append("  -listener <classname>  add an instance of class as a project listener"
        // + lSep);
        // msg.append("  -noinput               do not allow interactive input"
        // + lSep);
        // msg.append("  -buildfile <file>      use given buildfile" + lSep);
        // msg.append("    -file    <file>              ''" + lSep);
        // msg.append("    -f       <file>              ''" + lSep);
        msg.append("  -D<property>=<value>   use value for given property\n");
        // msg.append("  -keep-going, -k        execute all targets that do not depend"
        // + lSep);
        // msg.append("                         on failed target(s)" + lSep);
        msg.append("  -propertyfile <name>   load all properties from file with -D\n");
        msg.append("                         properties taking precedence");
        // msg.append("  -inputhandler <class>  the class which will handle input requests"
        // + lSep);
        // msg.append("  -find <file>           (s)earch for buildfile towards the root of"
        // + lSep);
        // msg.append("    -s  <file>           the filesystem and use it" +
        // lSep);
        // msg.append("  -nice  number          A niceness value for the main thread:"
        // + lSep
        // +
        // "                         1 (lowest) to 10 (highest); 5 is the default"
        // + lSep);
        // msg.append("  -nouserlib             Run ant without using the jar files from"
        // + lSep
        // + "                         ${user.home}/.ant/lib" + lSep);
        // msg.append("  -noclasspath           Run ant without using CLASSPATH"
        // + lSep);
        // msg.append("  -autoproxy             Java1.5+: use the OS proxy settings"
        // + lSep);
        // msg.append("  -main <class>          override Ant's normal entry point");
        System.out.println(msg.toString());
    }

    /**
     * Prints the Ant version information to <code>System.out</code>.
     * 
     * @exception BuildException if the version information is unavailable
     */
    private static void printVersion(final int logLevel) throws BuildException {
        System.out.println("DITA-OT version " + Configuration.configuration.get("otversion"));
        // System.out.println(getAntVersion());
    }

    /**
     * Prints the description of a project (if there is one) to
     * <code>System.out</code>.
     * 
     * @param project The project to display a description of. Must not be
     *            <code>null</code>.
     */
    private static void printDescription(final Project project) {
        if (project.getDescription() != null) {
            project.log(project.getDescription());
        }
    }

    /**
     * Targets in imported files with a project name and not overloaded by the
     * main build file will be in the target map twice. This method removes the
     * duplicate target.
     * 
     * @param targets the targets to filter.
     * @return the filtered targets.
     */
    private static Map<String, Target> removeDuplicateTargets(final Map<String, Target> targets) {
        final Map<Location, Target> locationMap = new HashMap<>();
        for (final Map.Entry<String, Target> entry : targets.entrySet()) {
            final String name = entry.getKey();
            final Target target = entry.getValue();
            final Target otherTarget = locationMap.get(target.getLocation());
            // Place this entry in the location map if
            // a) location is not in the map
            // b) location is in map, but it's name is longer
            // (an imported target will have a name. prefix)
            if (otherTarget == null || otherTarget.getName().length() > name.length()) {
                locationMap.put(target.getLocation(), target); // Smallest name
                // wins
            }
        }
        final Map<String, Target> ret = new HashMap<>();
        for (final Target target : locationMap.values()) {
            ret.put(target.getName(), target);
        }
        return ret;
    }

    /**
     * Prints a list of all targets in the specified project to
     * <code>System.out</code>, optionally including subtargets.
     * 
     * @param project The project to display a description of. Must not be
     *            <code>null</code>.
     * @param printSubTargets Whether or not subtarget names should also be
     *            printed.
     */
    private static void printTargets(final Project project, boolean printSubTargets, final boolean printDependencies) {
        // find the target with the longest name
        int maxLength = 0;
        final Map<String, Target> ptargets = removeDuplicateTargets(project.getTargets());
        String targetName;
        String targetDescription;
        Target currentTarget;
        // split the targets in top-level and sub-targets depending
        // on the presence of a description
        final Vector<String> topNames = new Vector<>();
        final Vector<String> topDescriptions = new Vector<>();
        final Vector<Enumeration<String>> topDependencies = new Vector<>();
        final Vector<String> subNames = new Vector<>();
        final Vector<Enumeration<String>> subDependencies = new Vector<>();

        for (Target target : ptargets.values()) {
            currentTarget = target;
            targetName = currentTarget.getName();
            if (targetName.equals("")) {
                continue;
            }
            targetDescription = currentTarget.getDescription();
            // maintain a sorted list of targets
            if (targetDescription == null) {
                final int pos = findTargetPosition(subNames, targetName);
                subNames.insertElementAt(targetName, pos);
                if (printDependencies) {
                    subDependencies.insertElementAt(currentTarget.getDependencies(), pos);
                }
            } else {
                final int pos = findTargetPosition(topNames, targetName);
                topNames.insertElementAt(targetName, pos);
                topDescriptions.insertElementAt(targetDescription, pos);
                if (targetName.length() > maxLength) {
                    maxLength = targetName.length();
                }
                if (printDependencies) {
                    topDependencies.insertElementAt(currentTarget.getDependencies(), pos);
                }
            }
        }

        printTargets(project, topNames, topDescriptions, topDependencies, "Main targets:", maxLength);
        // if there were no main targets, we list all subtargets
        // as it means nothing has a description
        if (topNames.size() == 0) {
            printSubTargets = true;
        }
        if (printSubTargets) {
            printTargets(project, subNames, null, subDependencies, "Other targets:", 0);
        }

        final String defaultTarget = project.getDefaultTarget();
        if (defaultTarget != null && !"".equals(defaultTarget)) {
            // shouldn't need to check but...
            project.log("Default target: " + defaultTarget);
        }
    }

    /**
     * Searches for the correct place to insert a name into a list so as to keep
     * the list sorted alphabetically.
     * 
     * @param names The current list of names. Must not be <code>null</code>.
     * @param name The name to find a place for. Must not be <code>null</code>.
     * 
     * @return the correct place in the list for the given name
     */
    private static int findTargetPosition(final Vector<String> names, final String name) {
        final int size = names.size();
        int res = size;
        for (int i = 0; i < size && res == size; i++) {
            if (name.compareTo(names.elementAt(i)) < 0) {
                res = i;
            }
        }
        return res;
    }

    /**
     * Writes a formatted list of target names to <code>System.out</code> with
     * an optional description.
     * 
     * 
     * @param project the project instance.
     * @param names The names to be printed. Must not be <code>null</code>.
     * @param descriptions The associated target descriptions. May be
     *            <code>null</code>, in which case no descriptions are
     *            displayed. If non-<code>null</code>, this should have as many
     *            elements as <code>names</code>.
     * @param topDependencies The list of dependencies for each target. The
     *            dependencies are listed as a non null enumeration of String.
     * @param heading The heading to display. Should not be <code>null</code>.
     * @param maxlen The maximum length of the names of the targets. If
     *            descriptions are given, they are padded to this position so
     *            they line up (so long as the names really <i>are</i> shorter
     *            than this).
     */
    private static void printTargets(final Project project, final Vector<String> names,
            final Vector<String> descriptions, final Vector<Enumeration<String>> dependencies, final String heading,
            final int maxlen) {
        // now, start printing the targets and their descriptions
        final String lSep = System.getProperty("line.separator");
        // got a bit annoyed that I couldn't find a pad function
        String spaces = "    ";
        while (spaces.length() <= maxlen) {
            spaces += spaces;
        }
        final StringBuilder msg = new StringBuilder();
        msg.append(heading + lSep + lSep);
        final int size = names.size();
        for (int i = 0; i < size; i++) {
            msg.append(" ");
            msg.append(names.elementAt(i));
            if (descriptions != null) {
                msg.append(spaces.substring(0, maxlen - names.elementAt(i).length() + 2));
                msg.append(descriptions.elementAt(i));
            }
            msg.append(lSep);
            if (!dependencies.isEmpty()) {
                final Enumeration<String> deps = dependencies.elementAt(i);
                if (deps.hasMoreElements()) {
                    msg.append("   depends on: ");
                    while (deps.hasMoreElements()) {
                        msg.append(deps.nextElement());
                        if (deps.hasMoreElements()) {
                            msg.append(", ");
                        }
                    }
                    msg.append(lSep);
                }
            }
        }
        project.log(msg.toString(), Project.MSG_WARN);
    }
}
