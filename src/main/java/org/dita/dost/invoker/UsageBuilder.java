/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2020 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.invoker;

import static org.dita.dost.invoker.DefaultLogger.ANSI_BOLD;
import static org.dita.dost.invoker.DefaultLogger.ANSI_RESET;
import static org.dita.dost.invoker.Main.locale;

import java.util.*;

public class UsageBuilder {

  private final StringBuilder buf = new StringBuilder();
  private final List<String> usages = new ArrayList<>();
  private final Map<String, String> subcommands = new HashMap<>();
  private final Map<Key, String> options = new HashMap<>();
  private final Map<Key, String> arguments = new LinkedHashMap<>();
  private final List<String> footers = new ArrayList<>();
  private final boolean useColor;

  private UsageBuilder(final boolean compact, final boolean useColor) {
    this.useColor = useColor;
    options("h", "help", null, locale.getString("help.option.help"));
    if (!compact) {
      options("d", "debug", null, locale.getString("help.option.debug"));
      options("v", "verbose", null, locale.getString("help.option.verbose"));
      options(null, "stacktrace", null, locale.getString("help.option.stacktrace"));
      options(null, "no-color", null, locale.getString("help.option.no-color"));
    }
  }

  public static UsageBuilder builder(final boolean compact, final boolean useColor) {
    return new UsageBuilder(compact, useColor);
  }

  public UsageBuilder usage(final String usage) {
    usages.add(usage);
    return this;
  }

  public UsageBuilder subcommands(final String subcommand, final String desc) {
    subcommands.put(subcommand, desc);
    return this;
  }

  public UsageBuilder options(final String shortKey, final String longKey, final String value, final String desc) {
    options.put(new Key(shortKey, longKey, value), desc);
    return this;
  }

  public UsageBuilder arguments(final String shortKey, final String longKey, final String value, final String desc) {
    arguments.put(new Key(shortKey, longKey, value), desc);
    return this;
  }

  public UsageBuilder footer(final String desc) {
    footers.add(desc);
    return this;
  }

  public String build() {
    final String padding = getPadding();

    bold("Usage");
    buf.append(":\n");
    for (String usage : usages) {
      buf.append("  ").append(usage).append("\n");
    }
    if (!subcommands.isEmpty()) {
      buf.append("\n");
      bold("Subcommands");
      buf.append(":\n");
      for (Map.Entry<String, String> subcommand : sortSubCommands(subcommands)) {
        buf
          .append("  ")
          .append(subcommand.getKey())
          .append(padding.substring(subcommand.getKey().length()))
          .append(subcommand.getValue())
          .append("\n");
      }
      buf.append("\n  See 'dita <subcommand> --help' for details about a specific subcommand.\n");
    }
    if (!arguments.isEmpty()) {
      buf.append("\n");
      bold("Arguments");
      buf.append(":\n");
      for (Map.Entry<Key, String> argument : arguments.entrySet()) {
        buf
          .append("  ")
          .append(argument.getKey())
          .append(padding.substring(argument.getKey().toString().length()))
          .append(argument.getValue())
          .append("\n");
      }
    }
    if (!options.isEmpty()) {
      buf.append("\n");
      bold("Options");
      buf.append(":\n");
      for (Map.Entry<Key, String> option : sort(options)) {
        buf
          .append("  ")
          .append(option.getKey())
          .append(padding.substring(option.getKey().toString().length()))
          .append(option.getValue())
          .append("\n");
      }
    }
    if (!footers.isEmpty()) {
      buf.append("\n");
      for (String footer : footers) {
        buf.append(footer).append("\n");
      }
    }
    return buf.toString();
  }

  private List<Map.Entry<Key, String>> sort(Map<Key, String> arguments) {
    final List<Map.Entry<Key, String>> entries = new ArrayList<>(arguments.entrySet());
    entries.sort(Map.Entry.comparingByKey());
    return entries;
  }

  private void bold(final String text) {
    if (useColor) {
      buf.append(ANSI_BOLD).append(text).append(ANSI_RESET);
    } else {
      buf.append(text);
    }
  }

  private List<Map.Entry<String, String>> sortSubCommands(Map<String, String> arguments) {
    final List<Map.Entry<String, String>> entries = new ArrayList<>(arguments.entrySet());
    entries.sort(Map.Entry.comparingByKey());
    return entries;
  }

  private String getPadding() {
    int max = 0;
    for (String key : subcommands.keySet()) {
      max = Math.max(max, key.length());
    }
    for (Key key : options.keySet()) {
      max = Math.max(max, key.toString().length());
    }
    for (Key key : arguments.keySet()) {
      max = Math.max(max, key.toString().length());
    }
    return " ".repeat(Math.max(0, max + 2));
  }

  private record Key(String shortKey, String longKey, String value) implements Comparable<Key> {
    @Override
    public String toString() {
      final StringBuilder buf = new StringBuilder();
      if (shortKey != null) {
        buf.append("-").append(shortKey);
        if (value != null) {
          buf.append(" <").append(value).append(">");
        }
      }
      if (shortKey != null && longKey != null) {
        buf.append(", ");
      }
      if (longKey != null) {
        buf.append("--").append(longKey);
        if (value != null) {
          buf.append("=<").append(value).append(">");
        }
      }
      if (shortKey == null && longKey == null & value != null) {
        buf.append("<").append(value).append(">");
      }
      return buf.toString();
    }

    @Override
    public int compareTo(Key o) {
      if (longKey != null && o.longKey != null) {
        return longKey.compareTo(o.longKey);
      } else if (value != null && o.value != null) {
        return value.compareTo(o.value);
      } else {
        return 0;
      }
    }
  }
}
