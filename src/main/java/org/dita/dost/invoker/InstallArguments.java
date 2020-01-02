/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2019 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.invoker;

import org.apache.tools.ant.Project;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

public class InstallArguments extends Arguments {

    String installFile;

    @Override
    Arguments parse(final String[] arguments) {
        final Deque<String> args = new ArrayDeque<>(Arrays.asList(arguments));
        while (!args.isEmpty()) {
            final String arg = args.pop();
            if (arg.equals("install")) {
                handleSubcommandInstall(arg, args);
            } else if (isLongForm(arg, "-install")) {
                handleArgInstall(arg, args);
            } else {
                parseCommonOptions(arg, args);
            }
        }
        if (msgOutputLevel < Project.MSG_INFO) {
            emacsMode = true;
        }
        return this;
    }

    /**
     * Handle the --install argument
     */
    private void handleArgInstall(final String arg, final Deque<String> args) {
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

    private void handleSubcommandInstall(final String arg, final Deque<String> args) {
        String value;
        value = args.peek();
        if (value != null && !value.startsWith("-")) {
            value = args.pop();
        } else {
            value = null;
        }
        if (value != null) {
            installFile = value;
        }
    }

    @Override
    void printUsage() {
        final StringBuilder msg = new StringBuilder();
        msg.append("Usage: dita install [<file> | <url> | <id>]\n");
        msg.append("Arguments: \n");
        msg.append("  <file>                       install plug-in from a local ZIP file\n");
        msg.append("  <url>                        install plug-in from a URL\n");
        msg.append("  <id>                         install plug-in from plugin registry\n");
        msg.append("Options: \n");
        msg.append("  -d, --debug                  print debugging information\n");
        msg.append("  -h, --help                   print this message\n");
        msg.append("  -v, --verbose                verbose logging\n");
        System.out.println(msg.toString());
    }

}
