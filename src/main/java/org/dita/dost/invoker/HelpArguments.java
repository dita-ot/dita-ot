/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2024 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.invoker;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import org.apache.tools.ant.Project;

/**
 * Help subcommand is intended to serve as a fallback to --help option. It should not be documented as
 * a subcommand, but instead it's a recovery when user without prior knowledge guesses that help can be
 * accessed with the help subcommand.
 */
public class HelpArguments extends Arguments {

  String subcommand;

  @Override
  HelpArguments parse(final String[] arguments) {
    final Deque<String> args = new ArrayDeque<>(Arrays.asList(arguments));
    while (!args.isEmpty()) {
      final String arg = args.pop();
      if (arg.equals("help")) {
        break;
      } else if (arg.startsWith("-")) {
        parseCommonOptions(arg, args);
      } else {
        subcommand = arg;
      }
    }
    while (!args.isEmpty()) {
      final String arg = args.pop();
      if (arg.startsWith("-")) {
        parseCommonOptions(arg, args);
      } else {
        subcommand = arg;
      }
    }
    if (msgOutputLevel < Project.MSG_INFO) {
      emacsMode = true;
    }
    justPrintUsage = true;

    return this;
  }

  @Override
  String getUsage(final boolean compact) {
    if (subcommand == null) {
      return new ConversionArguments().getUsage(false);
    }
    return switch (subcommand) {
      case "plugins" -> new PluginsArguments().getUsage(false);
      case "version" -> new VersionArguments().getUsage(false);
      case "transtypes" -> new TranstypesArguments().getUsage(false);
      case "deliverables" -> new DeliverablesArguments().getUsage(false);
      case "install" -> new InstallArguments().getUsage(false);
      case "uninstall" -> new UninstallArguments().getUsage(false);
      case "init" -> new InitArguments().getUsage(false);
      default -> new ConversionArguments().getUsage(false);
    };
  }
}
