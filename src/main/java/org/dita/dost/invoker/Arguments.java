/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2019 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

/* Derived from Apache Ant. */
package org.dita.dost.invoker;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.dita.dost.util.Configuration;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Command line arguments.
 *
 * @since 3.4
 */
abstract class Arguments {

    boolean useColor;
    File logFile;
    /**
     * Our current message output status. Follows Project.MSG_XXX.
     */
    int msgOutputLevel = Project.MSG_WARN;
    /**
     * File that we are using for configuration.
     */
    File buildFile;
    /**
     * Names of classes to add as listeners to project.
     */
    Vector<String> listeners;
    /**
     * Indicates whether this build is to support interactive input
     */
    boolean allowInput;
    /**
     * keep going mode
     */
    boolean keepGoingMode;
    /**
     * The Ant logger class. There may be only one logger. It will have the
     * right to use the 'out' PrintStream. The class must implements the
     * BuildLogger interface.
     */
    String loggerClassname;
    /**
     * The Ant InputHandler class. There may be only one input handler.
     */
    String inputHandlerClassname;
    /**
     * Whether or not output to the log is to be unadorned.
     */
    boolean emacsMode;
    /**
     * optional thread priority
     */
    Integer threadPriority;
    /**
     * proxy flag: default is false
     */
    boolean proxy;
    boolean justPrintUsage;
    boolean justPrintVersion;
    boolean justPrintDiagnostics;
    final Map<String, Object> definedProps = new HashMap<>();

    Arguments() {
        useColor = getUseColor();
    }

    abstract void printUsage(boolean compact);

    abstract Arguments parse(String[] arguments);

    boolean getUseColor() {
        final String os = System.getProperty("os.name");
        if (os != null && os.startsWith("Windows")) {
            return false;
        } else if (System.getenv("NO_COLOR") != null) {
            return false;
        } else if (Objects.equals(System.getenv("TERM"), "dumb")) {
            return false;
        }
        return Boolean.parseBoolean(Configuration.configuration.getOrDefault("cli.color", "true"));
    }

    boolean isLongForm(String arg, String property) {
        final String name = arg.contains("=") ? arg.substring(0, arg.indexOf('=')) : arg;
        return name.equals(property) || name.equals("-" + property);
    }

    void parseCommonOptions(final String arg, final Deque<String> args) {
        if (isLongForm(arg, "-help") || arg.equals("-h")) {
            justPrintUsage = true;
        } else if (isLongForm(arg, "-verbose") || arg.equals("-v")) {
            msgOutputLevel = Project.MSG_INFO;
        } else if (isLongForm(arg, "-debug") || arg.equals("-d")) {
            msgOutputLevel = Project.MSG_VERBOSE;
        } else if (isLongForm(arg, "-emacs") || arg.equals("-e")) {
            emacsMode = true;
        } else if (isLongForm(arg, "-logfile") || arg.equals("-l")) {
            handleArgLogFile(arg, args);
        } else if (isLongForm(arg, "-buildfile") || isLongForm(arg, "-file")) {
            handleArgBuildFile(args);
        } else if (isLongForm(arg, "-listener")) {
            handleArgListener(args);
        } else if (isLongForm(arg, "-logger")) {
            handleArgLogger(args);
        } else if (isLongForm(arg, "-no-color")) {
            useColor = false;
        } else {
            throw new BuildException("Unsupported argument: %s", arg);
        }
    }

    void handleArgLogFile(String arg, Deque<String> args) {
        final Map.Entry<String, String> entry = parse(arg, args);
        if (entry.getValue() == null) {
            throw new BuildException("Missing value for log file " + entry.getKey());
        }
        logFile = new File(entry.getValue());
        useColor = false;
    }

    Map.Entry<String, String> parse(final String arg, final Deque<String> args) {
        String name = arg;
        String value;
        final int posEq = name.indexOf("=");
        if (posEq > 0) {
            value = name.substring(posEq + 1);
            name = name.substring(0, posEq);
        } else {
            value = args.pop();
        }
        return new AbstractMap.SimpleEntry<>(name, value);
    }

    /**
     * Handle the --buildfile, --file, -f argument
     */
    void handleArgBuildFile(final Deque<String> args) {
        final String value = args.pop();
        if (value == null) {
            throw new BuildException("You must specify a buildfile when using the --buildfile argument");
        }
        buildFile = new File(value.replace('/', File.separatorChar));
    }

    /**
     * Handle --listener argument
     */
    void handleArgListener(final Deque<String> args) {
        final String value = args.pop();
        if (value == null) {
            throw new BuildException("You must specify a classname when using the --listener argument");
        }
        listeners.addElement(value);
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

    static abstract class Argument {
        final String property;
        final String desc;

        Argument(final String property, final String desc) {
            this.property = property;
            this.desc = desc;
        }

        abstract String getValue(final String value);
    }

    static class StringArgument extends Argument {
        StringArgument(final String property, final String desc) {
            super(property, desc);
        }

        @Override
        String getValue(final String value) {
            return value;
        }
    }

    static class BooleanArgument extends Argument {
        final String trueValue;
        final String falseValue;

        BooleanArgument(final String property, final String desc, final String trueValue, final String falseValue) {
            super(property, desc);
            this.trueValue = trueValue;
            this.falseValue = falseValue;
        }

        @Override
        String getValue(final String value) {
            switch (value.toLowerCase()) {
                case "true":
                case "yes":
                case "on":
                case "1":
                    return trueValue;
                default:
                    return falseValue;
            }
        }
    }

    static class EnumArgument extends Argument {
        final Set<String> values;

        EnumArgument(final String property, final String desc, final Set<String> values) {
            super(property, desc);
            this.values = values;
        }

        @Override
        String getValue(final String value) {
            if (!values.contains(value)) {
                throw new BuildException("Invalid value for property " + property + ": " + value);
            }
            return value;
        }
    }

    static class FileArgument extends Argument {
        FileArgument(final String property, final String desc) {
            super(property, desc);
        }

        @Override
        String getValue(final String value) {
            return Paths.get(value).normalize().toString();
        }
    }

    static class AbsoluteFileArgument extends Argument {
        AbsoluteFileArgument(final String property, final String desc) {
            super(property, desc);
        }

        @Override
        String getValue(final String value) {
            return Paths.get(value).toAbsolutePath().normalize().toString();
        }
    }

    static class AbsoluteFileListArgument extends Argument {
        AbsoluteFileListArgument(final String property, final String desc) {
            super(property, desc);
        }

        @Override
        String getValue(final String value) {
            return Arrays.stream(value.split(File.pathSeparator))
                    .map(oneFile -> new File(oneFile).getAbsolutePath())
                    .collect(Collectors.joining(File.pathSeparator));
        }
    }

    static class FileOrUriArgument extends Argument {
        FileOrUriArgument(final String property, final String desc) {
            super(property, desc);
        }

        @Override
        String getValue(final String value) {
            final Path f = Paths.get(value).toAbsolutePath().normalize();
            if (Files.exists(f)) {
                return f.toString();
            } else {
                return value;
            }
        }
    }

}
