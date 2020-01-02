/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2019 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.invoker;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

public class TranstypesArguments extends Arguments {

    @Override
    TranstypesArguments parse(final String[] arguments) {
        final Deque<String> args = new ArrayDeque<>(Arrays.asList(arguments));
        while (!args.isEmpty()) {
            final String arg = args.pop();
            if (arg.equals("transtypes") || isLongForm(arg, "-transtypes")) {
                // ignore
            } else {
                parseCommonOptions(arg, args);
            }
        }
        return this;
    }

    @Override
    void printUsage() {
        final StringBuilder msg = new StringBuilder();
        msg.append("Usage: dita transtypes [options]\n");
        msg.append("Options: \n");
        msg.append("  -d, --debug                  print debugging information\n");
        msg.append("  -h, --help                   print this message\n");
        msg.append("  -v, --verbose                verbose logging\n");
        System.out.println(msg.toString());
    }

}
