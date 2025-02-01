/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2019 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.invoker;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import org.apache.tools.ant.Project;
import org.junit.jupiter.api.Test;

public class ArgumentParserTest {

  private final ArgumentParser parser = new ArgumentParser();

  @Test
  public void empty() {
    final ConversionArguments act = (ConversionArguments) parser.processArgs(new String[0]);
  }

  @Test
  public void shortArguments() {
    final ConversionArguments act = (ConversionArguments) parser.processArgs(
      new String[] { "-i", "src", "-f", "html5", "-t", "tmp", "-o", "out", "-v" }
    );
    assertEquals(Collections.singletonList(new File("src").getAbsolutePath()), act.inputs);
    assertEquals(List.of("html5"), act.formats);
    assertEquals(new File("tmp").getAbsolutePath(), act.definedProps.get("dita.temp.dir"));
    assertEquals(new File("out").getAbsolutePath(), act.definedProps.get("output.dir"));
    assertEquals(Project.MSG_INFO, act.msgOutputLevel);
  }

  @Test
  public void longArguments() {
    final ConversionArguments act = (ConversionArguments) parser.processArgs(
      new String[] { "--input=src", "--format=html5", "--temp=tmp", "--output=out", "--verbose" }
    );
    assertEquals(Collections.singletonList(new File("src").getAbsolutePath()), act.inputs);
    assertEquals(List.of("html5"), act.formats);
    assertEquals(new File("tmp").getAbsolutePath(), act.definedProps.get("dita.temp.dir"));
    assertEquals(new File("out").getAbsolutePath(), act.definedProps.get("output.dir"));
    assertEquals(Project.MSG_INFO, act.msgOutputLevel);
  }

  @Test
  public void reinstallSubcommand() {
    final InstallArguments act = (InstallArguments) parser.processArgs(new String[] { "install" });
  }

  @Test
  public void installSubcommand() {
    final InstallArguments act = (InstallArguments) parser.processArgs(
      new String[] { "install", "org.dita.eclipsehelp" }
    );
    assertEquals("org.dita.eclipsehelp", act.installFile);
  }

  @Test
  public void installSubcommand_optionForm() {
    final InstallArguments act = (InstallArguments) parser.processArgs(
      new String[] { "--install=org.dita.eclipsehelp" }
    );
    assertEquals("org.dita.eclipsehelp", act.installFile);
  }

  @Test
  public void installSubcommand_optionForm_globalArgument() {
    final InstallArguments act = (InstallArguments) parser.processArgs(
      new String[] { "-v", "--install=org.dita.eclipsehelp" }
    );
    assertEquals("org.dita.eclipsehelp", act.installFile);
  }

  @Test
  public void uninstallSubcommand() {
    final UninstallArguments act = (UninstallArguments) parser.processArgs(
      new String[] { "uninstall", "org.dita.eclipsehelp" }
    );
    assertEquals("org.dita.eclipsehelp", act.uninstallId);
  }

  @Test
  public void uninstallSubcommand_optionForm() {
    final UninstallArguments act = (UninstallArguments) parser.processArgs(
      new String[] { "--uninstall=org.dita.eclipsehelp" }
    );
    assertEquals("org.dita.eclipsehelp", act.uninstallId);
  }

  @Test
  public void validateSubcommand() {
    final ValidateArguments act = (ValidateArguments) parser.processArgs(new String[] { "validate", "-i", "root.map" });
    assertEquals("validate", act.definedProps.get("transtype"));
    assertEquals("root.map", act.definedProps.get("args.input"));
  }

  @Test
  public void validateSubcommand_context() {
    final ValidateArguments act = (ValidateArguments) parser.processArgs(
      new String[] { "validate", "-p", "project.yml", "--context=book" }
    );
    System.out.println(act.definedProps);
    assertEquals("validate", act.definedProps.get("transtype"));
    assertEquals(new File("project.yml").getAbsoluteFile(), act.projectFile);
    assertEquals("book", act.definedProps.get("project.context"));
  }

  @Test
  public void pluginsSubcommand() {
    final PluginsArguments act = (PluginsArguments) parser.processArgs(new String[] { "plugins" });
  }

  @Test
  public void pluginsSubcommand_optionForm() {
    final PluginsArguments act = (PluginsArguments) parser.processArgs(new String[] { "--plugins" });
  }

  @Test
  public void transtypesSubcommand() {
    final TranstypesArguments act = (TranstypesArguments) parser.processArgs(new String[] { "transtypes" });
  }

  @Test
  public void transtypesSubcommand__optionForm() {
    final TranstypesArguments act = (TranstypesArguments) parser.processArgs(new String[] { "--transtypes" });
  }

  @Test
  public void deliverablesSubcommand() {
    final DeliverablesArguments act = (DeliverablesArguments) parser.processArgs(
      new String[] { "deliverables", "project.json" }
    );
    assertEquals(new File("project.json").getAbsoluteFile(), act.projectFile);
  }

  @Test
  public void deliverablesSubcommand__withOption() {
    final DeliverablesArguments act = (DeliverablesArguments) parser.processArgs(
      new String[] { "deliverables", "-p", "project.json" }
    );
    assertEquals(new File("project.json").getAbsoluteFile(), act.projectFile);
  }

  @Test
  public void deliverablesSubcommand__optionForm() {
    final DeliverablesArguments act = (DeliverablesArguments) parser.processArgs(
      new String[] { "--deliverables", "-p", "project.json" }
    );
    assertEquals(new File("project.json").getAbsoluteFile(), act.projectFile);
  }

  @Test
  public void initSubcommand() {
    final InitArguments act = (InitArguments) parser.processArgs(new String[] { "init", "template" });
    assertEquals("template", act.template);
  }

  @Test
  public void initSubcommand__withOption() {
    final InitArguments act = (InitArguments) parser.processArgs(new String[] { "init", "template", "-o", "out" });
    assertEquals("template", act.template);
    assertEquals(Paths.get("out").toAbsolutePath(), act.output);
  }

  @Test
  public void initSubcommand__withLongOption() {
    final InitArguments act = (InitArguments) parser.processArgs(new String[] { "init", "template", "--output=out" });
    assertEquals("template", act.template);
    assertEquals(Paths.get("out").toAbsolutePath(), act.output);
  }

  @Test
  public void initSubcommand__globalArgument() {
    final InitArguments act = (InitArguments) parser.processArgs(new String[] { "init", "-v", "template" });
    assertEquals("template", act.template);
  }
}
