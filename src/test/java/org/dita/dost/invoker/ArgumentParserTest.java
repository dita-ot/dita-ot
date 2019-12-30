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
}