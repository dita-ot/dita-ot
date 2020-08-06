/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2020 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.invoker;

import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static java.io.File.pathSeparator;
import static org.junit.Assert.assertEquals;

public class ConversionArgumentsTest {

    private ConversionArguments arguments;

    @Before
    public void setUp() {
        arguments = new ConversionArguments();
    }

    @Test
    public void input_short() {
        arguments.parse(new String[]{"-i", "foo.dita"});

        assertEquals("foo.dita", arguments.definedProps.get("args.input"));
    }

    @Test
    public void input_long() {
        arguments.parse(new String[]{"--input=foo.dita"});

        assertEquals("foo.dita", arguments.definedProps.get("args.input"));
    }

    @Test
    public void format_short() {
        arguments.parse(new String[]{"-f", "html5"});

        assertEquals("html5", arguments.definedProps.get("transtype"));
    }

    @Test
    public void format_long() {
        arguments.parse(new String[]{"--format=html5"});

        assertEquals("html5", arguments.definedProps.get("transtype"));
    }

    @Test
    public void resource_short_multipleOptions() {
        arguments.parse(new String[]{"-r", "foo.dita", "-r", "bar.dita"});

        assertEquals("foo.dita" + pathSeparator + "bar.dita", arguments.definedProps.get("args.resources"));
    }

    @Test
    public void resource_long_multipleOptions() {
        arguments.parse(new String[]{"--resource=foo.dita", "--resource=bar.dita"});

        assertEquals("foo.dita" + pathSeparator + "bar.dita", arguments.definedProps.get("args.resources"));
    }

    @Test
    public void filter_multipleOptions() {
        arguments.parse(new String[]{"--filter=foo.ditaval", "--filter=bar.ditaval"});

        assertEquals(new File("foo.ditaval").getAbsolutePath()
                        + pathSeparator
                        + new File("bar.ditaval").getAbsolutePath(),
                arguments.definedProps.get("args.filter"));
    }

    @Test
    public void filter_listValue() {
        arguments.parse(new String[]{"--filter=foo.ditaval" + pathSeparator + "bar.ditaval"});

        assertEquals(new File("foo.ditaval").getAbsolutePath()
                        + pathSeparator
                        + new File("bar.ditaval").getAbsolutePath(),
                arguments.definedProps.get("args.filter"));
    }
}
