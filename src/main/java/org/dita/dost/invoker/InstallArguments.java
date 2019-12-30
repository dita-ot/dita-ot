/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2019 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.invoker;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class InstallArguments extends Arguments {

    /**
     * Plug-in installation file. May be either a system path or a URL.
     */
    final String installFile;

    public InstallArguments(String installFile,
                            boolean useColor, int msgOutputLevel, File buildFile, Vector<String> targets,
                            Vector<String> listeners, Vector<String> propertyFiles, boolean allowInput, boolean keepGoingMode,
                            String loggerClassname, String inputHandlerClassname, boolean emacsMode, Integer threadPriority,
                            boolean proxy, boolean justPrintUsage, boolean justPrintVersion, boolean justPrintDiagnostics,
                            File logFile, Map<String, Object> definedProps) {
        super(useColor, msgOutputLevel, buildFile, targets, listeners, propertyFiles, allowInput, keepGoingMode,
                loggerClassname, inputHandlerClassname, emacsMode, threadPriority, proxy, justPrintUsage, justPrintVersion,
                justPrintDiagnostics, logFile, definedProps);
        this.installFile = installFile;
    }

    @Override
    void printUsage() {
        final StringBuilder msg = new StringBuilder();
        msg.append("Usage: dita install [<file> | <url> | <id>]\n");
//        msg.append("   or: dita --uninstall <id>\n");
//        msg.append("   or: dita --plugins\n");
//        msg.append("   or: dita --transtypes\n");
//        msg.append("   or: dita --deliverables\n");
//        msg.append("   or: dita --help\n");
//        msg.append("   or: dita --version\n");
        msg.append("Arguments: \n");
//        msg.append("  -i <file>, --input=<file>    input file\n");
//        msg.append("  -f <name>, --format=<name>   output format (transformation type)\n");
//        msg.append("  -p <name>, --project=<name>  run project file\n");
//        msg.append("  -r <file>, --resource=<file> resource file\n");
        msg.append("  [<file>]           install plug-in from a local ZIP file\n");
        msg.append("  [<url>]            install plug-in from a URL\n");
        msg.append("  [<id>]             install plug-in from plugin registry\n");
        msg.append("                     reload plug-ins\n");
//        msg.append("  --uninstall <id>             uninstall plug-in with the ID\n");
//        msg.append("  --plugins                    print list of installed plug-ins\n");
//        msg.append("  --transtypes                 print list of installed transtypes\n");
//        msg.append("  --deliverables               print list of deliverables in project\n");
//        msg.append("  --version                    print version information and exit\n");
        msg.append("Options: \n");
        msg.append("  -h, --help                   print this message\n");
//        msg.append("  -o, --output=<dir>          output directory\n");
        // msg.append("  -diagnostics           print information that might be helpful to"
        // + lSep);
        // msg.append("                         diagnose or report problems." +
        // lSep);
        // msg.append("  -quiet, -q             be extra quiet" + lSep);
//        msg.append("  --filter=<files>             filter and flagging files\n");
//        msg.append("  --force                      force install plug-in\n");
//        msg.append("  -t, --temp=<dir>             temporary directory\n");
        msg.append("  -v, --verbose                verbose logging\n");
        msg.append("  -d, --debug                  print debugging information\n");
        // msg.append("  -emacs, -e             produce logging information without adornments"
        // + lSep);
        // msg.append("  -lib <path>            specifies a path to search for jars and classes"
        // + lSep);
        msg.append("  -l, --logfile=<file>        use given file for log\n");
        // msg.append("  -logger <classname>    the class which is to perform logging"
        // + lSep);
        // msg.append("  -listener <classname>  add an instance of class as a project listener"
        // + lSep);
        // msg.append("  -noinput               do not allow interactive input"
        // + lSep);
        // msg.append("  -buildfile <file>      use given buildfile" + lSep);
        // msg.append("    -file    <file>              ''" + lSep);
        // msg.append("    -f       <file>              ''" + lSep);
//        msg.append("  --<property>=<value>         use value for given property\n");
//        msg.append("  --propertyfile=<name>        load all properties from file\n");
        // msg.append("  -keep-going, -k        execute all targets that do not depend"
        // + lSep);
        // msg.append("                         on failed target(s)" + lSep);
        // msg.append("  -inputhandler <class>  the class which will handle input requests"
        // + lSep);
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

}
