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

import static org.dita.dost.invoker.Main.locale;

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
    void printUsage(final boolean compact) {
        UsageBuilder.builder(compact)
                .usage(locale.getString("deliverables.usage"))
                .arguments(null, null, "file", locale.getString("deliverables.argument.file"))
                .print();
    }

}
