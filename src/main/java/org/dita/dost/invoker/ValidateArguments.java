/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2024 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.invoker;

import static org.dita.dost.invoker.ArgumentParser.getPluginArguments;
import static org.dita.dost.invoker.Main.locale;
import static org.dita.dost.util.Constants.ANT_TEMP_DIR;

import com.google.common.collect.ImmutableMap;
import java.io.File;
import java.util.*;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

class ValidateArguments extends Arguments {

  private static final Map<String, Argument> ARGUMENTS = new HashMap<>();

  static {
    //    ARGUMENTS.put("--deliverable", new StringArgument("project.deliverable", null));
    ARGUMENTS.put("-i", new FileOrUriArgument("args.input", null));
    ARGUMENTS.put("--input", new FileOrUriArgument("args.input", null));
    ARGUMENTS.put("-r", new FileOrUriArgument("args.resources", null));
    ARGUMENTS.put("--resource", new FileOrUriArgument("args.resources", null));
    ARGUMENTS.put("--filter", new AbsoluteFileListArgument("args.filter", null));
    ARGUMENTS.put("-t", new AbsoluteFileArgument(ANT_TEMP_DIR, null));
    ARGUMENTS.put("--temp", new AbsoluteFileArgument(ANT_TEMP_DIR, null));
    ARGUMENTS.put("-p", new AbsoluteFileArgument("project.file", null));
    ARGUMENTS.put("--project", new AbsoluteFileArgument("project.file", null));
    ARGUMENTS.put("--context", new AbsoluteFileArgument("project.context", null));
    for (final Map.Entry<String, Argument> e : new HashSet<>(ARGUMENTS.entrySet())) {
      if (e.getKey().startsWith("--")) {
        ARGUMENTS.put(e.getKey().substring(1), e.getValue());
      }
    }
  }

  /**
   * Project file
   */
  File projectFile;
  private final List<String> inputs = new ArrayList<>();
  private final List<String> resources = new ArrayList<>();

  @Override
  ValidateArguments parse(final String[] arguments) {
    msgOutputLevel = Project.MSG_WARN;
    final Deque<String> args = new ArrayDeque<>(Arrays.asList(arguments));
    while (!args.isEmpty()) {
      final String arg = args.pop();
      if (arg.equals("validate")) {
        definedProps.put("transtype", "validate");
        break;
      } else if (arg.startsWith("-")) {
        parseCommonOptions(arg, args);
      }
    }
    while (!args.isEmpty()) {
      final String arg = args.pop();
      if (isLongForm(arg, "-project") || arg.equals("-p")) {
        handleArgProject(arg, args);
      } else if (isLongForm(arg, "-input") || arg.equals("-i")) {
        handleArgInput(arg, args, ARGUMENTS.get(getArgumentName(arg)));
      } else if (isLongForm(arg, "-context")) {
        handleContext(arg, args);
      } else if (isLongForm(arg, "-filter")) {
        handleArgFilter(arg, args, ARGUMENTS.get(getArgumentName(arg)));
      } else if (isLongForm(arg, "-resource") || arg.equals("-r")) {
        handleArgResource(arg, args, ARGUMENTS.get(getArgumentName(arg)));
      } else if (isLongForm(arg, "-verbose") || arg.equals("-v")) {
        msgOutputLevel = Project.MSG_INFO;
      } else if (isLongForm(arg, "-debug") || arg.equals("-d")) {
        msgOutputLevel = Project.MSG_VERBOSE;
      } else if (ARGUMENTS.containsKey(getArgumentName(arg))) {
        definedProps.putAll(handleParameterArg(arg, args, ARGUMENTS.get(getArgumentName(arg))));
      } else if (getPluginArguments().containsKey(getArgumentName(arg))) {
        definedProps.putAll(handleParameterArg(arg, args, getPluginArguments().get(getArgumentName(arg))));
      } else {
        parseCommonOptions(arg, args);
      }
    }
    if (!inputs.isEmpty()) {
      definedProps.put("args.input", inputs.get(0));
    }
    if (!resources.isEmpty()) {
      definedProps.put("args.resources", String.join(File.pathSeparator, resources));
    }
    return this;
  }

  /**
   * Handler parameter argument
   */
  private Map<String, Object> handleParameterArg(final String arg, final Deque<String> args, final Argument argument) {
    final Map.Entry<String, String> entry = parse(arg, args);
    if (entry.getValue() == null) {
      throw new BuildException("Missing value for property %s".formatted(entry.getKey()));
    }
    return ImmutableMap.of(argument.property, argument.getValue(entry.getValue()));
  }

  private void handleContext(final String arg, final Deque<String> args) {
    final Map.Entry<String, String> entry = parse(arg, args);
    if (entry.getValue() == null || entry.getValue().isBlank()) {
      throw new BuildException("Missing value for context %s".formatted(entry.getKey()));
    }
    definedProps.put(ARGUMENTS.get(getArgumentName(arg)).property, entry.getValue());
  }

  private void handleArgInput(final String arg, final Deque<String> args, final Argument argument) {
    final Map.Entry<String, String> entry = parse(arg, args);
    if (entry.getValue() == null || entry.getValue().isBlank()) {
      throw new BuildException("Missing value for input " + entry.getKey());
    }
    inputs.add(argument.getValue(entry.getValue()));
  }

  private void handleArgFilter(final String arg, final Deque<String> args, final Argument argument) {
    final Map.Entry<String, String> entry = parse(arg, args);
    if (entry.getValue() == null || entry.getValue().isBlank()) {
      throw new BuildException("Missing value for input " + entry.getKey());
    }
    final Object prev = definedProps.get(argument.property);
    final String value = prev != null
      ? prev + File.pathSeparator + argument.getValue(entry.getValue())
      : argument.getValue(entry.getValue());
    definedProps.put(argument.property, value);
  }

  private void handleArgResource(final String arg, final Deque<String> args, final Argument argument) {
    final Map.Entry<String, String> entry = parse(arg, args);
    if (entry.getValue() == null || entry.getValue().isBlank()) {
      throw new BuildException("Missing value for resource " + entry.getKey());
    }
    resources.add(argument.getValue(entry.getValue()));
  }

  /**
   * Handle the --project argument
   */
  private void handleArgProject(final String arg, final Deque<String> args) {
    final Map.Entry<String, String> entry = parse(arg, args);
    if (entry.getValue() == null || entry.getValue().isBlank()) {
      throw new BuildException("Missing value for project " + entry.getKey());
    }
    projectFile = new File(entry.getValue()).getAbsoluteFile();
  }

  @Override
  String getUsage(final boolean compact) {
    return UsageBuilder
      .builder(compact, useColor)
      .usage(locale.getString("validate.usage"))
      .arguments("i", "input", "file", locale.getString("conversion.argument.input"))
      .arguments("p", "project", "file", locale.getString("conversion.argument.project"))
      .options("r", "resource", "file", locale.getString("conversion.option.resource"))
      .options(null, "filter", "files", locale.getString("conversion.option.filter"))
      .build();
  }
}
