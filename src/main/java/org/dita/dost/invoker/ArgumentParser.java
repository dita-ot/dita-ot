/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2019 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

/* Derived from Apache Ant. */
package org.dita.dost.invoker;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.dita.dost.platform.Plugins;
import org.dita.dost.util.XMLUtils;
import org.w3c.dom.Element;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.dita.dost.util.XMLUtils.getChildElements;
import static org.dita.dost.util.XMLUtils.toList;

/**
 * Command line argument parser.
 *
 * @since 3.4
 */
final class ArgumentParser {

    private static final Map<String, String> TRUTHY_VALUES;

    static {
        TRUTHY_VALUES = ImmutableMap.<String, String>builder()
                .put("true", "false")
                .put("TRUE", "FALSE")
                .put("yes", "no")
                .put("YES", "NO")
                .put("1", "0")
                .put("on", "off")
                .put("ON", "OFF")
                .build();
    }

    static Arguments.Argument mergeArguments(final Arguments.Argument a, final Arguments.Argument b) {
        if (a instanceof Arguments.EnumArgument && b instanceof Arguments.EnumArgument) {
            final Set<String> vals = ImmutableSet.<String>builder()
                    .addAll(((Arguments.EnumArgument) a).values)
                    .addAll(((Arguments.EnumArgument) b).values)
                    .build();
            return new Arguments.EnumArgument(a.property, a.desc, vals);
        } else {
            return a;
        }
    }

    static Arguments.Argument getArgument(Element param) {
        final String name = param.getAttribute("name");
        final String type = param.getAttribute("type");
        final String desc = param.getAttribute("desc");
        switch (type) {
            case "file":
                return new Arguments.FileArgument(name, desc);
            case "enum":
                final Set<String> vals = getChildElements(param).stream()
                        .map(XMLUtils::getText)
                        .collect(Collectors.toSet());
                if (vals.size() == 2) {
                    for (Map.Entry<String, String> pair : TRUTHY_VALUES.entrySet()) {
                        if (vals.contains(pair.getKey()) && vals.contains(pair.getValue())) {
                            return new Arguments.BooleanArgument(name, desc, pair.getKey(), pair.getValue());
                        }
                    }
                }
                return new Arguments.EnumArgument(name, desc, vals);
            default:
                return new Arguments.StringArgument(name, desc);
        }
    }

    private static Map<String, Arguments.Argument> PLUGIN_ARGUMENTS;

    // Lazy load parameters
    static synchronized Map<String, Arguments.Argument> getPluginArguments() {
        if (PLUGIN_ARGUMENTS == null) {
            final List<Element> params = toList(Plugins.getPluginConfiguration().getElementsByTagName("param"));
            PLUGIN_ARGUMENTS = params.stream()
                    .map(ArgumentParser::getArgument)
                    .collect(Collectors.toMap(
                            arg -> ("--" + arg.property),
                            arg -> arg,
                            ArgumentParser::mergeArguments));
        }
        return PLUGIN_ARGUMENTS;
    }

    /**
     * Process command line arguments. When ant is started from Launcher,
     * launcher-only arguments do not get passed through to this routine.
     *
     * @param arguments the command line arguments.
     * @since Ant 1.6
     */
    public Arguments processArgs(final String[] arguments) {
        for (final String subcommand : arguments) {
            switch (getName(subcommand)) {
                case "plugins":
                    return new PluginsArguments().parse(arguments);
                case "version":
                    return new VersionArguments().parse(arguments);
                case "transtypes":
                    return new TranstypesArguments().parse(arguments);
                case "deliverables":
                    return new DeliverablesArguments().parse(arguments);
                case "install":
                    return new InstallArguments().parse(arguments);
                case "uninstall":
                    return new UninstallArguments().parse(arguments);
            }
        }
        return new ConversionArguments().parse(arguments);
    }

    private String getName(final String subcommand) {
        final int start = subcommand.startsWith("--") ? 2 : (subcommand.startsWith("-") ? 1 : 0);
        final int end = subcommand.indexOf('=');
        return subcommand.substring(start, end != -1 ? end :subcommand.length());
    }
}
