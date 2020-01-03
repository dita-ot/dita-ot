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
            } else if (isLongForm(arg, "-force")) {
                definedProps.put("force", "true");
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
        UsageBuilder.builder()
                .usage("dita install [<file> | <url> | <id>]")
                .arguments(null, null, "file", "install plug-in from a local ZIP file")
                .arguments(null, null, "url", "install plug-in from a URL")
                .arguments(null, null, "id", "install plug-in from plugin registry")
                .options(null, "force", null, "force install plug-in")
                .print();
    }

}
