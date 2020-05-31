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

import static org.dita.dost.invoker.Main.locale;

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
    void printUsage(final boolean compact) {
        UsageBuilder.builder(compact)
                .usage(locale.getString("transtypes.usage"))
                .print();
    }

}
