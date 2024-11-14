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
        handleSubcommandInit(arg, args);
        //      } else if (isLongForm(arg, "-template") || arg.equals("-t")) {
        //        handleArgTemplate(arg, args);
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

  //  private void handleArgTemplate(final String arg, final Deque<String> args) {
  //    final Map.Entry<String, String> entry = parse(arg, args);
  //    if (entry.getValue() == null) {
  //      throw new BuildException("You must specify a template name");
  //    }
  //    template = entry.getValue();
  //  }

  private void handleArgOutput(final String arg, final Deque<String> args) {
    final Map.Entry<String, String> entry = parse(arg, args);
    if (entry.getValue() == null) {
      throw new BuildException("You must specify an output directory");
    }
    output = Paths.get(entry.getValue()).toAbsolutePath();
  }

  private void handleSubcommandInit(final String arg, final Deque<String> args) {
    String value;
    value = args.peek();
    if (value != null && !value.startsWith("-")) {
      value = args.pop();
    } else {
      value = null;
    }
    template = value;
  }

  @Override
  String getUsage(final boolean compact) {
    return UsageBuilder
      .builder(compact, useColor)
      .usage(locale.getString("init.usage"))
      .arguments(null, null, "template", locale.getString("init.argument.template"))
      //      .options("t", "template", "name", locale.getString("init.argument.template"))
      .options("o", "output", "dir", locale.getString("init.option.output"))
      .build();
  }
}
