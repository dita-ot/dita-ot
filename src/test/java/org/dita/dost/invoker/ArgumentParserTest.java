/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2019 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.invoker;

import org.apache.tools.ant.Project;
import org.junit.Test;

import java.io.File;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class ArgumentParserTest {

    private final ArgumentParser parser = new ArgumentParser();

    @Test
    public void empty() {
        final ConversionArguments act = (ConversionArguments) parser.processArgs(new String[0]);
    }

    @Test
    public void shortArguments() {
        final ConversionArguments act = (ConversionArguments) parser.processArgs(new String[]{
                "-i", "src",
                "-f", "html5",
                "-t", "tmp",
                "-o", "out",
                "-v"
        });
        assertEquals(Collections.singletonList(new File("src").getAbsolutePath()), act.inputs);
        assertEquals(new File("src").getAbsolutePath(), act.definedProps.get("args.input"));
        assertEquals("html5", act.definedProps.get("transtype"));
        assertEquals(new File("tmp").getAbsolutePath(), act.definedProps.get("dita.temp.dir"));
        assertEquals(new File("out").getAbsolutePath(), act.definedProps.get("output.dir"));
        assertEquals(Project.MSG_INFO, act.msgOutputLevel);
    }

    @Test
    public void longArguments() {
        final ConversionArguments act = (ConversionArguments) parser.processArgs(new String[]{
                "--input=src",
                "--format=html5",
                "--temp=tmp",
                "--output=out",
                "--verbose"
        });
        assertEquals(Collections.singletonList(new File("src").getAbsolutePath()), act.inputs);
        assertEquals(new File("src").getAbsolutePath(), act.definedProps.get("args.input"));
        assertEquals("html5", act.definedProps.get("transtype"));
        assertEquals(new File("tmp").getAbsolutePath(), act.definedProps.get("dita.temp.dir"));
        assertEquals(new File("out").getAbsolutePath(), act.definedProps.get("output.dir"));
        assertEquals(Project.MSG_INFO, act.msgOutputLevel);
    }

    @Test
    public void reinstallSubcommand() {
        final InstallArguments act = (InstallArguments) parser.processArgs(new String[]{
                "install"
        });
    }

    @Test
    public void installSubcommand() {
        final InstallArguments act = (InstallArguments) parser.processArgs(new String[]{
                "install",
                "org.dita.eclipsehelp"
        });
        assertEquals("org.dita.eclipsehelp", act.installFile);
    }

    @Test
    public void installSubcommand_optionForm() {
        final InstallArguments act = (InstallArguments) parser.processArgs(new String[]{
                "--install=org.dita.eclipsehelp"
        });
        assertEquals("org.dita.eclipsehelp", act.installFile);
    }

    @Test
    public void installSubcommand_optionForm_globalArgument() {
        final InstallArguments act = (InstallArguments) parser.processArgs(new String[]{
                "-v",
                "--install=org.dita.eclipsehelp"
        });
        assertEquals("org.dita.eclipsehelp", act.installFile);
    }

    @Test
    public void uninstallSubcommand() {
        final UninstallArguments act = (UninstallArguments) parser.processArgs(new String[]{
                "uninstall",
                "org.dita.eclipsehelp"
        });
        assertEquals("org.dita.eclipsehelp", act.uninstallId);
    }

    @Test
    public void uninstallSubcommand_optionForm() {
        final UninstallArguments act = (UninstallArguments) parser.processArgs(new String[]{
                "--uninstall=org.dita.eclipsehelp"
        });
        assertEquals("org.dita.eclipsehelp", act.uninstallId);
    }

    @Test
    public void pluginsSubcommand() {
        final PluginsArguments act = (PluginsArguments) parser.processArgs(new String[]{
                "plugins"
        });
    }

    @Test
    public void pluginsSubcommand_optionForm() {
        final PluginsArguments act = (PluginsArguments) parser.processArgs(new String[]{
                "--plugins"
        });
    }

    @Test
    public void transtypesSubcommand() {
        final TranstypesArguments act = (TranstypesArguments) parser.processArgs(new String[]{
                "transtypes"
        });
    }

    @Test
    public void transtypesSubcommand__optionForm() {
        final TranstypesArguments act = (TranstypesArguments) parser.processArgs(new String[]{
                "--transtypes"
        });
    }

    @Test
    public void deliverablesSubcommand() {
        final DeliverablesArguments act = (DeliverablesArguments) parser.processArgs(new String[]{
                "deliverables",
                "project.json"
        });
        assertEquals(new File("project.json").getAbsoluteFile(), act.projectFile);
    }

    @Test
    public void deliverablesSubcommand__withOption() {
        final DeliverablesArguments act = (DeliverablesArguments) parser.processArgs(new String[]{
                "deliverables",
                "-p", "project.json"
        });
        assertEquals(new File("project.json").getAbsoluteFile(), act.projectFile);
    }

    @Test
    public void deliverablesSubcommand__optionForm() {
        final DeliverablesArguments act = (DeliverablesArguments) parser.processArgs(new String[]{
                "--deliverables",
                "-p", "project.json"
        });
        assertEquals(new File("project.json").getAbsoluteFile(), act.projectFile);
    }


}