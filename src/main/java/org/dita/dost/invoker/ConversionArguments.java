/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2019 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.invoker;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.util.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

import static org.dita.dost.util.Constants.ANT_TEMP_DIR;

public class ConversionArguments extends Arguments {

    /**
     * A Set of args are are handled by the launcher and should not be seen by
     * Main.
     */
    private static final Set<String> LAUNCH_COMMANDS = ImmutableSet.of(
            "-lib",
            "-cp",
            "-noclasspath",
            "-nouserlib",
            "-main"
    );


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
        for (final Map.Entry<String, Argument> e : new HashSet<>(ARGUMENTS.entrySet())) {
            if (e.getKey().startsWith("--")) {
                ARGUMENTS.put(e.getKey().substring(1), e.getValue());
            }
        }
    }

    private static final Map<String, String> RESERVED_PROPERTIES = ImmutableMap.of(
            "transtype", "-f",
            "args.input", "-i",
            "output.dir", "-o",
            "args.filter", "--filter",
            ANT_TEMP_DIR, "-t"
    );

    /**
     * Project file
     */
    File projectFile;

    public List<String> inputs = new ArrayList<>();
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

    @Override
    ConversionArguments parse(final String[] arguments) {
        final Deque<String> args = new ArrayDeque<>(Arrays.asList(arguments));
        while (!args.isEmpty()) {
            final String arg = args.pop();

            if (isLongForm(arg, "-help") || arg.equals("-h")) {
                justPrintUsage = true;
            } else if (isLongForm(arg, "-project") || arg.equals("-p")) {
                handleArgProject(arg, args);
            } else if (isLongForm(arg, "-force")) {
                definedProps.put("force", "true");
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
            } else if (ArgumentParser.getPluginArguments().containsKey(getArgumentName(arg))) {
                definedProps.putAll(handleParameterArg(arg, args, ArgumentParser.getPluginArguments().get(getArgumentName(arg))));
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
        definedProps.putAll(loadPropertyFiles());

        return this;
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
                    final Argument arg = ArgumentParser.getPluginArguments().get("--" + name);
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

    @Override
    void printUsage() {
        final StringBuilder msg = new StringBuilder();
        msg.append("Usage: dita -i <file> -f <name> [options]\n");
        msg.append("   or: dita --project=<file> [options]\n");
        msg.append("   or: dita --propertyfile=<file> [options]\n");
        msg.append("Subcommands: \n");
        msg.append("   deliverables\n");
        msg.append("   install\n");
        msg.append("   plugins\n");
        msg.append("   transtypes\n");
        msg.append("   uninstall\n");
        msg.append("   version\n");
        msg.append("Arguments: \n");
        msg.append("  -i <file>, --input=<file>    input file\n");
        msg.append("  -f <name>, --format=<name>   output format (transformation type)\n");
        msg.append("  -p <name>, --project=<name>  run project file\n");
        msg.append("  -r <file>, --resource=<file> resource file\n");
        msg.append("  -h, --help                   print this message\n");
        msg.append("  --version                    print version information and exit\n");
        msg.append("Options: \n");
        msg.append("  -d, --debug                  print debugging information\n");
        msg.append("  --filter=<files>             filter and flagging files\n");
        msg.append("  --force                      force install plug-in\n");
        msg.append("  -l, --logfile=<file>         use given file for log\n");
        msg.append("  -o, --output=<dir>           output directory\n");
        msg.append("  --<property>=<value>         use value for given property\n");
        msg.append("  --propertyfile=<name>        load all properties from file\n");
        msg.append("  -t, --temp=<dir>             temporary directory\n");
        msg.append("  -v, --verbose                verbose logging\n");
        System.out.println(msg.toString());
    }
}
