/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2019 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.invoker;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

import static org.dita.dost.invoker.Main.locale;

public class UninstallArguments extends Arguments {

    String uninstallId;

    @Override
    UninstallArguments parse(final String[] arguments) {
        final Deque<String> args = new ArrayDeque<>(Arrays.asList(arguments));
        while (!args.isEmpty()) {
            final String arg = args.pop();
            if (arg.equals("uninstall")) {
                handleSubcommandUninstall(arg, args);
            } else if (isLongForm(arg, "-uninstall")) {
                handleArgUninstall(arg, args);
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
     * Handle the --uninstall argument
     */
    private void handleArgUninstall(final String arg, final Deque<String> args) {
        final int posEq = arg.indexOf("=");
        String value;
        if (posEq != -1) {
            value = arg.substring(posEq + 1);
        } else {
            value = args.peek();
            if (value != null && !value.startsWith("-")) {
                value = args.pop();
            } else {
                value = null;
            }
        }
        if (value == null) {
            throw new BuildException("You must specify an installation package when using the --uninstall argument");
        }
        uninstallId = value;
    }

    private void handleSubcommandUninstall(final String arg, final Deque<String> args) {
        String value;
        value = args.peek();
        if (value != null && !value.startsWith("-")) {
            value = args.pop();
        } else {
            value = null;
        }
        uninstallId = value;
    }

    @Override
    void printUsage(final boolean compact) {
        UsageBuilder.builder(compact)
                .usage(locale.getString("uninstall.usage"))
                .arguments(null, null, "id", locale.getString("uninstall.argument.id"))
                .print();
    }


}
