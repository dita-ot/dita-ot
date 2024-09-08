/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2024 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.invoker;

import static org.dita.dost.invoker.Main.locale;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Map;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

public class InitArguments extends Arguments {

  String template;
  Path output;

  @Override
  InitArguments parse(final String[] arguments) {
    final Deque<String> args = new ArrayDeque<>(Arrays.asList(arguments));
    while (!args.isEmpty()) {
      final String arg = args.pop();
      if (arg.equals("init")) {
        // Ignore
      } else if (isLongForm(arg, "-template") || arg.equals("-t")) {
        handleArgTemplate(arg, args);
      } else if (isLongForm(arg, "-output") || arg.equals("-o")) {
        handleArgOutput(arg, args);
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
  private void handleArgTemplate(final String arg, final Deque<String> args) {
    final Map.Entry<String, String> entry = parse(arg, args);
    if (entry.getValue() == null) {
      throw new BuildException("You must specify an installation package when using the --uninstall argument");
    }
    template = entry.getKey();
  }

  private void handleArgOutput(final String arg, final Deque<String> args) {
    final Map.Entry<String, String> entry = parse(arg, args);
    if (entry.getValue() == null) {
      throw new BuildException("You must specify an installation package when using the --uninstall argument");
    }
    output = Paths.get(entry.getKey()).toAbsolutePath();
  }

  @Override
  String getUsage(final boolean compact) {
    return UsageBuilder
      .builder(compact, useColor)
      .usage(locale.getString("init.usage"))
      .options("t", "template", "name", locale.getString("init.argument.template"))
      .options("o", "output", "dir", locale.getString("init.argument.output"))
      .build();
  }
}
