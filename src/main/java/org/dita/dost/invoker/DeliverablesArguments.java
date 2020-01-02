/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2019 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.invoker;

import org.apache.tools.ant.BuildException;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Map;

public class DeliverablesArguments extends Arguments {

    File projectFile;

    @Override
    DeliverablesArguments parse(final String[] arguments) {
        useColor = getUseColor();
        final Deque<String> args = new ArrayDeque<>(Arrays.asList(arguments));
        while (!args.isEmpty()) {
            final String arg = args.pop();
            if (arg.equals("deliverables")) {
                handleSubcommand(arg, args);
            } else if (isLongForm(arg, "-deliverables")) {
                // ignore
            } else if (isLongForm(arg, "-project") || arg.equals("-p")) {
                handleArgProject(arg, args);
            } else {
                parseCommonOptions(arg, args);
            }
        }
        return this;
    }

    private void handleSubcommand(final String arg, final Deque<String> args) {
        String value;
        value = args.peek();
        if (value != null && !value.startsWith("-")) {
            value = args.pop();
        } else {
            value = null;
        }
        if (value != null) {
            projectFile = new File(value).getAbsoluteFile();
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

    @Override
    void printUsage() {
        final StringBuilder msg = new StringBuilder();
        msg.append("Usage: dita deliverables <file> [options]\n");
        msg.append("Arguments: \n");
        msg.append("  <file>                       project file\n");
        msg.append("Options: \n");
        msg.append("  -d, --debug                  print debugging information\n");
        msg.append("  -h, --help                   print this message\n");
        msg.append("  -v, --verbose                verbose logging\n");
        System.out.println(msg.toString());
    }

}
