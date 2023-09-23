/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2020 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.invoker;

import static org.dita.dost.invoker.Main.locale;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

class VersionArguments extends Arguments {

  @Override
  VersionArguments parse(final String[] arguments) {
    final Deque<String> args = new ArrayDeque<>(Arrays.asList(arguments));
    while (!args.isEmpty()) {
      final String arg = args.pop();
      if (arg.equals("version") || isLongForm(arg, "-version")) {
        // ignore
      } else {
        parseCommonOptions(arg, args);
      }
    }
    return this;
  }

  @Override
  String getUsage(final boolean compact) {
    return UsageBuilder.builder(compact).usage(locale.getString("version.usage")).build();
  }
}
