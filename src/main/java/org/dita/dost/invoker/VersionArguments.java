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
import org.apache.tools.ant.Project;

class VersionArguments extends Arguments {

  @Override
  VersionArguments parse(final String[] arguments) {
    msgOutputLevel = Project.MSG_INFO;
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
    return UsageBuilder.builder(compact, useColor).usage(locale.getString("version.usage")).build();
  }
}
