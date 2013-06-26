/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2010 All Rights Reserved.
 */
package org.dita.dost.invoker;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.dita.dost.TestUtils;
import org.dita.dost.exception.DITAOTException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestCmdInvoker {

    private final PrintStream originalOut = System.out;

    private File tempDir;
    private String ditaArg;
    private String tempArg;

    @Before
    public void setUp() throws IOException {
        tempDir = TestUtils.createTempDir(getClass());
        ditaArg = "/ditadir:" + new File("src", "main").getAbsolutePath();
        tempArg = "/tempdir:" + tempDir.getAbsolutePath();
    }

    @Test
    public void testProcessArguments() throws Exception {
        final String input[] = { ditaArg, "/i:abc.ditamap", "/transtype:xhtml", tempArg };
        final CommandLineInvoker test = new CommandLineInvoker();
        test.processArguments(input);
    }

    @Test(expected = DITAOTException.class)
    public void testProcessArgsWrongParam() throws Exception {
        final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        final String input[] = { ditaArg, "/i:abc.ditamap", "/abc:def" };
        final CommandLineInvoker test = new CommandLineInvoker();
        try {
            System.setOut(new PrintStream(outContent));
            test.processArguments(input);
        } catch (final DITAOTException e) {
            throw e;
        } finally {
            System.setOut(originalOut);
            assertTrue(outContent.size() > 0);
        }
    }

    @Test(expected = DITAOTException.class)
    public void testProcessArgsEmptyValue() throws Exception {
        final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        final String input[] = { ditaArg, "/i:" };
        final CommandLineInvoker test = new CommandLineInvoker();
        try {
            System.setOut(new PrintStream(outContent));
            test.processArguments(input);
        } catch (final DITAOTException e) {
            throw e;
        } finally {
            System.setOut(originalOut);
            assertTrue(outContent.size() > 0);
        }
    }

    @After
    public void tearDown() throws IOException {
        TestUtils.forceDelete(tempDir);
    }

}
