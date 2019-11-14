/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2019 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

/* Derived from Apache Ant. */
package org.dita.dost.invoker;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.util.FileUtils;
import org.dita.dost.platform.Plugins;
import org.dita.dost.util.Configuration;
import org.dita.dost.util.XMLUtils;
import org.w3c.dom.Element;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import static org.dita.dost.invoker.Arguments.*;
import static org.dita.dost.util.Constants.ANT_TEMP_DIR;
import static org.dita.dost.util.XMLUtils.getChildElements;
import static org.dita.dost.util.XMLUtils.toList;

/**
 * Command line argument parser.
 *
 * @since 3.4
 */
final class ArgumentParser {

    private boolean useColor;

    /**
     * A Set of args are are handled by the launcher and should not be seen by
     * Main.
     */
    private static final Set<String> LAUNCH_COMMANDS = new HashSet<>();

    static {
        LAUNCH_COMMANDS.add("-lib");
        LAUNCH_COMMANDS.add("-cp");
        LAUNCH_COMMANDS.add("-noclasspath");
        LAUNCH_COMMANDS.add("-nouserlib");
        LAUNCH_COMMANDS.add("-main");
    }

    private static final Map<String, Argument> ARGUMENTS = new HashMap<>();

    static {
        ARGUMENTS.put("-f", new StringArgument("transtype"));
        ARGUMENTS.put("--format", new StringArgument("transtype"));
        ARGUMENTS.put("--transtype", new StringArgument("transtype"));
        ARGUMENTS.put("--deliverable", new StringArgument("project.deliverable"));
        ARGUMENTS.put("-i", new FileOrUriArgument("args.input"));
        ARGUMENTS.put("--input", new FileOrUriArgument("args.input"));
        ARGUMENTS.put("-r", new FileOrUriArgument("args.resources"));
        ARGUMENTS.put("--resource", new FileOrUriArgument("args.resources"));
        ARGUMENTS.put("-o", new AbsoluteFileArgument("output.dir"));
        ARGUMENTS.put("--output", new AbsoluteFileArgument("output.dir"));
        ARGUMENTS.put("--filter", new AbsoluteFileListArgument("args.filter"));
        ARGUMENTS.put("-t", new AbsoluteFileArgument(ANT_TEMP_DIR));
        ARGUMENTS.put("--temp", new AbsoluteFileArgument(ANT_TEMP_DIR));
        ARGUMENTS.put("-p", new AbsoluteFileArgument("project.file"));
        ARGUMENTS.put("--project", new AbsoluteFileArgument("project.file"));
        addSingleHyphenOptions(ARGUMENTS);
    }

    private static void addSingleHyphenOptions(final Map<String, Argument> args) {
        for (final Map.Entry<String, Argument> e : new HashMap<>(args).entrySet()) {
            if (e.getKey().startsWith("--")) {
                args.put(e.getKey().substring(1), e.getValue());
            }
        }
    }

    private static Map<String, Argument> PLUGIN_ARGUMENTS;

    // Lazy load parameters
    public synchronized Map<String, Argument> getPluginArguments() {
        if (PLUGIN_ARGUMENTS == null) {
            final List<Element> params = toList(Plugins.getPluginConfiguration().getElementsByTagName("param"));
            PLUGIN_ARGUMENTS = params.stream()
                    .map(ArgumentParser::getArgument)
                    .collect(Collectors.toMap(
                            arg -> ("--" + arg.property),
                            arg -> arg,
                            ArgumentParser::mergeArguments));
        }
        return PLUGIN_ARGUMENTS;
    }

    private static Argument mergeArguments(final Argument a, final Argument b) {
        if (a instanceof EnumArgument && b instanceof EnumArgument) {
            final Set<String> vals = ImmutableSet.<String>builder()
                    .addAll(((EnumArgument) a).values)
                    .addAll(((EnumArgument) b).values)
                    .build();
            return new EnumArgument(a.property, vals);
        } else {
            return a;
        }
    }

    private static Argument getArgument(Element param) {
        final String name = param.getAttribute("name");
        final String type = param.getAttribute("type");
        switch (type) {
            case "file":
                return new FileArgument(name);
            case "enum":
                final Set<String> vals = getChildElements(param).stream()
                        .map(XMLUtils::getText)
                        .collect(Collectors.toSet());
                if (vals.size() == 2) {
                    for (Map.Entry<String, String> pair : TRUTHY_VALUES.entrySet()) {
                        if (vals.contains(pair.getKey()) && vals.contains(pair.getValue())) {
                            return new BooleanArgument(name, pair.getKey(), pair.getValue());
                        }
                    }
                }
                return new EnumArgument(name, vals);
            default:
                return new StringArgument(name);
        }
    }

    private static final Map<String, String> RESERVED_PROPERTIES = ImmutableMap.of(
            "transtype", "-f",
            "args.input", "-i",
            "output.dir", "-o",
            "args.filter", "--filter",
            ANT_TEMP_DIR, "-t"
    );

    private static final Map<String, String> TRUTHY_VALUES;

    static {
        TRUTHY_VALUES = ImmutableMap.<String, String>builder()
                .put("true", "false")
                .put("TRUE", "FALSE")
                .put("yes", "no")
                .put("YES", "NO")
                .put("1", "0")
                .put("on", "off")
                .put("ON", "OFF")
                .build();
    }

    /**
     * Our current message output status. Follows Project.MSG_XXX.
     */
    private int msgOutputLevel = Project.MSG_WARN;

    /**
     * File that we are using for configuration.
     */
    private File buildFile;
    /**
     * Run integrator
     */
    private boolean install;
    /**
     * Plug-in installation file. May be either a system path or a URL.
     */
    private String installFile;
    /**
     * Project file
     */
    private File projectFile;
    /**
     * Plug-in uninstall ID.
     */
    private String uninstallId;

    private List<String> inputs = new ArrayList<>();
    private List<String> resources = new ArrayList<>();

    /**
     * The build targets.
     */
    private final Vector<String> targets = new Vector<>();

    /**
     * Names of classes to add as listeners to project.
     */
    private final Vector<String> listeners = new Vector<>(1);

    /**
     * File names of property files to load on startup.
     */
    private final Vector<String> propertyFiles = new Vector<>(1);

    /**
     * Indicates whether this build is to support interactive input
     */
    private boolean allowInput = true;

    /**
     * keep going mode
     */
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

    private File logFile;

    /**
     * optional thread priority
     */
    private Integer threadPriority = null;

    /**
     * proxy flag: default is false
     */
    private boolean proxy = false;

    boolean justPrintUsage = false;
    boolean justPrintVersion = false;
    boolean justPrintDiagnostics = false;
    boolean justPrintPlugins = false;
    boolean justPrintTranstypes = false;
    boolean justPrintDeliverables = false;

    /**
     * Process command line arguments. When ant is started from Launcher,
     * launcher-only arguments do not get passed through to this routine.
     *
     * @param arguments the command line arguments.
     * @since Ant 1.6
     */
    public Arguments processArgs(final String[] arguments) {
        useColor = getUseColor();

        final Map<String, Object> definedProps = new HashMap<>();
        final Deque<String> args = new ArrayDeque<>(Arrays.asList(arguments));
        while (!args.isEmpty()) {
            final String arg = args.pop();

            if (isLongForm(arg, "-help") || arg.equals("-h")) {
                justPrintUsage = true;
            } else if (isLongForm(arg, "-version")) {
                justPrintVersion = true;
            } else if (isLongForm(arg, "-plugins")) {
                justPrintPlugins = true;
            } else if (isLongForm(arg, "-transtypes")) {
                justPrintTranstypes = true;
            } else if (isLongForm(arg, "-deliverables")) {
                justPrintDeliverables = true;
            } else if (isLongForm(arg, "-install")) {
                handleArgInstall(arg, args);
            } else if (isLongForm(arg, "-project") || arg.equals("-p")) {
                handleArgProject(arg, args);
            } else if (isLongForm(arg, "-force")) {
                definedProps.put("force", "true");
            } else if (isLongForm(arg, "-uninstall")) {
                handleArgUninstall(arg, args);
            } else if (isLongForm(arg, "-diagnostics")) {
                justPrintDiagnostics = true;
            } else if (isLongForm(arg, "-verbose") || arg.equals("-v")) {
                msgOutputLevel = Project.MSG_INFO;
            } else if (isLongForm(arg, "-debug") || arg.equals("-d")) {
                msgOutputLevel = Project.MSG_VERBOSE;
            } else if (isLongForm(arg, "-noinput")) {
                allowInput = false;
            } else if (isLongForm(arg, "-logfile") || arg.equals("-l")) {
                handleArgLogFile(arg, args);
            } else if (isLongForm(arg, "-buildfile") || isLongForm(arg, "-file")) { //|| arg.equals("-f")
                handleArgBuildFile(args);
            } else if (isLongForm(arg, "-listener")) {
                handleArgListener(args);
            } else if (arg.startsWith("-D")) {
                definedProps.putAll(handleArgDefine(arg, args));
            } else if (isLongForm(arg, "-logger")) {
                handleArgLogger(args);
            } else if (isLongForm(arg, "-inputhandler")) {
                handleArgInputHandler(args);
            } else if (isLongForm(arg, "-emacs") || arg.equals("-e")) {
                emacsMode = true;
            } else if (isLongForm(arg, "-propertyfile")) {
                handleArgPropertyFile(arg, args);
            } else if (arg.equals("-k") || isLongForm(arg, "-keep-going")) {
                keepGoingMode = true;
            } else if (isLongForm(arg, "-nice")) {
                handleArgNice(args);
            } else if (isLongForm(arg, "-input") || arg.equals("-i")) {
                handleArgInput(arg, args, ARGUMENTS.get(getArgumentName(arg)));
            } else if (isLongForm(arg, "-resource") || arg.equals("-r")) {
                handleArgResource(arg, args, ARGUMENTS.get(getArgumentName(arg)));
            } else if (ARGUMENTS.containsKey(getArgumentName(arg))) {
                definedProps.putAll(handleParameterArg(arg, args, ARGUMENTS.get(getArgumentName(arg))));
            } else if (getPluginArguments().containsKey(getArgumentName(arg))) {
                definedProps.putAll(handleParameterArg(arg, args, getPluginArguments().get(getArgumentName(arg))));
            } else if (LAUNCH_COMMANDS.contains(arg)) {
                // catch script/ant mismatch with a meaningful message
                // we could ignore it, but there are likely to be other
                // version problems, so we stamp down on the configuration now
                final String msg = "Ant's Main method is being handed " + "an option " + arg
                        + " that is only for the launcher class."
                        + "\nThis can be caused by a version mismatch between "
                        + "the ant script/.bat file and Ant itself.";
                throw new BuildException(msg);
            } else if (isLongForm(arg, "-autoproxy")) {
                proxy = true;
            } else if (arg.startsWith("-") || arg.startsWith("/")) {
                // we don't have any more args to recognize!
                throw new IllegalArgumentException(arg);
            } else {
                // if it's no other arg, it may be the target
                targets.addElement(arg);
            }
        }
        if (!inputs.isEmpty()) {
            definedProps.put("args.input", inputs.get(0));
        }
        if (!resources.isEmpty()) {
            definedProps.put("args.resources", String.join(File.pathSeparator, resources));
        }

        if (install && msgOutputLevel < Project.MSG_INFO) {
            emacsMode = true;
        }

        definedProps.putAll(loadPropertyFiles());

        return new Arguments(useColor, msgOutputLevel, buildFile, install, installFile, projectFile, uninstallId,
                inputs, targets, listeners, propertyFiles, allowInput, keepGoingMode, loggerClassname,
                inputHandlerClassname, emacsMode, threadPriority, proxy, justPrintUsage, justPrintVersion,
                justPrintDiagnostics, justPrintPlugins, justPrintTranstypes, justPrintDeliverables, logFile,
                definedProps, resources);
    }

    private boolean getUseColor() {
        final String os = System.getProperty("os.name");
        if (os != null && os.startsWith("Windows")) {
            return false;
        }
        return Boolean.parseBoolean(Configuration.configuration.getOrDefault("cli.color", "true"));
    }

    private void handleArgLogFile(String arg, Deque<String> args) {
        final Map.Entry<String, String> entry = parse(arg, args);
        if (entry.getValue() == null) {
            throw new BuildException("Missing value for log file " + entry.getKey());
        }
        logFile = new File(entry.getValue());
        useColor = false;
    }

    private boolean isLongForm(String arg, String property) {
        final String name = arg.contains("=") ? arg.substring(0, arg.indexOf('=')) : arg;
        return name.equals(property) || name.equals("-" + property);
    }

    /**
     * Handle the --install argument
     */
    private void handleArgInstall(final String arg, final Deque<String> args) {
        install = true;
        String name = arg;
        final int posEq = name.indexOf("=");
        String value;
        if (posEq != -1) {
            value = name.substring(posEq + 1);
        } else {
            value = args.peek();
            if (value != null && !value.startsWith("-")) {
                value = args.pop();
            } else {
                value = null;
            }
        }
        if (value != null) {
            installFile = value;
        }
    }

    /**
     * Handle the --project argument
     */
    private void handleArgProject(final String arg, final Deque<String> args) {
        final Map.Entry<String, String> entry = parse(arg, args);
        if (entry.getValue() == null) {
            throw new BuildException("Missing value for project " + entry.getKey());
        }
        projectFile = new File(entry.getValue()).getAbsoluteFile();
    }

    /**
     * Handle the --uninstall argument
     */
    private void handleArgUninstall(final String arg, final Deque<String> args) {
        install = true;
        String name = arg;
        final int posEq = name.indexOf("=");
        String value;
        if (posEq != -1) {
            value = name.substring(posEq + 1);
        } else {
            value = args.peek();
            if (value != null && !value.startsWith("-")) {
                value = args.pop();
            } else {
                value = null;
            }
        }
        if (value == null) {
            throw new BuildException("You must specify a installation package when using the --uninstall argument");
        }
        uninstallId = value;
    }

    /**
     * Handle the --buildfile, --file, -f argument
     */
    private void handleArgBuildFile(final Deque<String> args) {
        final String value = args.pop();
        if (value == null) {
            throw new BuildException("You must specify a buildfile when using the --buildfile argument");
        }
        buildFile = new File(value.replace('/', File.separatorChar));
    }

    /**
     * Handle --listener argument
     */
    private void handleArgListener(final Deque<String> args) {
        final String value = args.pop();
        if (value == null) {
            throw new BuildException("You must specify a classname when using the --listener argument");
        }
        listeners.addElement(value);
    }

    /**
     * Handler -D argument
     */
    private Map<String, Object> handleArgDefine(final String arg, final Deque<String> args) {
        /*
         * Interestingly enough, we get to here when a user uses -Dname=value.
         * However, in some cases, the OS goes ahead and parses this out to args
         * {"-Dname", "value"} so instead of parsing on "=", we just make the
         * "-D" characters go away and skip one argument forward.
         *
         * I don't know how to predict when the JDK is going to help or not, so
         * we simply look for the equals sign.
         */
        final Map.Entry<String, String> entry = parse(arg.substring(2), args);
        if (entry.getValue() == null) {
            throw new BuildException("Missing value for property " + entry.getKey());
        }

        if (RESERVED_PROPERTIES.containsKey(entry.getKey())) {
            throw new BuildException("Property " + entry.getKey() + " cannot be set with -D, use " + RESERVED_PROPERTIES.get(entry.getKey()) + " instead");
        }
        return ImmutableMap.of(entry.getKey(), entry.getValue());
    }

    private Map.Entry<String, String> parse(final String arg, final Deque<String> args) {
        String name = arg;
        String value;
        final int posEq = name.indexOf("=");
        if (posEq > 0) {
            value = name.substring(posEq + 1);
            name = name.substring(0, posEq);
        } else {
            value = args.pop();
        }
        return new AbstractMap.SimpleEntry(name, value);
    }

    /**
     * Handler input argument
     */
    private void handleArgInput(final String arg, final Deque<String> args, final Argument argument) {
        final Map.Entry<String, String> entry = parse(arg, args);
        if (entry.getValue() == null) {
            throw new BuildException("Missing value for input " + entry.getKey());
        }
        inputs.add(argument.getValue((String) entry.getValue()));
    }

    private void handleArgResource(final String arg, final Deque<String> args, final Argument argument) {
        final Map.Entry<String, String> entry = parse(arg, args);
        if (entry.getValue() == null) {
            throw new BuildException("Missing value for resource " + entry.getKey());
        }
        resources.add(argument.getValue(entry.getValue()));
    }

    /**
     * Handler parameter argument
     */
    private Map<String, Object> handleParameterArg(final String arg, final Deque<String> args, final Argument argument) {
        final Map.Entry<String, String> entry = parse(arg, args);
        if (entry.getValue() == null) {
            throw new BuildException("Missing value for property " + entry.getKey());
        }
        return ImmutableMap.of(argument.property, argument.getValue(entry.getValue()));
    }

    /**
     * Get argument name
     */
    private String getArgumentName(final String arg) {
        int pos = arg.indexOf("=");
        if (pos == -1) {
            pos = arg.indexOf(":");
        }
        return arg.substring(0, pos != -1 ? pos : arg.length());
    }

    /**
     * Handle the --logger argument.
     */
    private void handleArgLogger(final Deque<String> args) {
        if (loggerClassname != null) {
            throw new BuildException("Only one logger class may be specified.");
        }
        loggerClassname = args.pop();
        if (loggerClassname == null) {
            throw new BuildException("You must specify a classname when using the -logger argument");
        }
    }

    /**
     * Handle the --inputhandler argument.
     */
    private void handleArgInputHandler(final Deque<String> args) {
        if (inputHandlerClassname != null) {
            throw new BuildException("Only one input handler class may be specified.");
        }
        inputHandlerClassname = args.pop();
        if (inputHandlerClassname == null) {
            throw new BuildException("You must specify a classname when using the --inputhandler" + " argument");
        }
    }

    /**
     * Handle the --propertyfile argument.
     */
    private void handleArgPropertyFile(final String arg, final Deque<String> args) {
        final Map.Entry<String, String> entry = parse(arg.substring(2), args);
        if (entry.getValue() == null) {
            throw new BuildException("You must specify a property filename when using the --propertyfile argument");
        }
        propertyFiles.addElement(entry.getValue());
    }

    /**
     * Handle the --nice argument.
     */
    private void handleArgNice(final Deque<String> args) {
        final String value = args.pop();
        if (value == null) {
            throw new BuildException("You must supply a niceness value (1-10) after the --nice option");
        }
        try {
            threadPriority = Integer.decode(value);
        } catch (final NumberFormatException e) {
            throw new BuildException("Unrecognized niceness value: " + value);
        }

        if (threadPriority < Thread.MIN_PRIORITY || threadPriority > Thread.MAX_PRIORITY) {
            throw new BuildException("Niceness value is out of the range 1-10");
        }
    }

    /**
     * Load the property files specified by --propertyfile
     */
    private Map<String, Object> loadPropertyFiles() {
        final Map<String, Object> definedProps = new HashMap<>();
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
                    final Argument arg = getPluginArguments().get("--" + name);
                    final String value = props.getProperty(name);
                    if (arg != null) {
                        definedProps.put(name, arg.getValue(value));
                    } else {
                        definedProps.put(name, value);
                    }
                }
            }
        }
        return definedProps;
    }
}
