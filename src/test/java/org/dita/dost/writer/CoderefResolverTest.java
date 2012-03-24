/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2011 All Rights Reserved.
 */
package org.dita.dost.writer;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;

import java.io.File;
import java.io.IOException;

import org.custommonkey.xmlunit.XMLUnit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.dita.dost.TestUtils;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.util.FileUtils;

public class CoderefResolverTest {

    private static final File resourceDir = new File(TestUtils.testStub, CoderefResolverTest.class.getSimpleName());
    private static final File srcDir = new File(resourceDir, "src");
    private static final File expDir = new File(resourceDir, "exp");
    private static File tempDir;

    @BeforeClass
    public static void setup() throws IOException {
        tempDir = TestUtils.createTempDir(CoderefResolverTest.class);
    }

    @Test
    public void testWrite() throws DITAOTException, SAXException, IOException {
        final File f = new File(tempDir, "test.dita");
        FileUtils.copyFile(new File(srcDir, "test.dita"), f);
        FileUtils.copyFile(new File(srcDir, "code.xml"), new File(tempDir, "code.xml"));
        FileUtils.copyFile(new File(srcDir, "utf-8.xml"), new File(tempDir, "utf-8.xml"));

        final CoderefResolver filter = new CoderefResolver();
        filter.setLogger(new TestUtils.TestLogger());
        filter.write(f.getAbsolutePath());

        TestUtils.resetXMLUnit();
        XMLUnit.setIgnoreWhitespace(true);
        assertXMLEqual(new InputSource(new File(expDir, "test.dita").toURI().toString()),
                new InputSource(f.toURI().toString()));
    }

    @AfterClass
    public static void teardown() throws IOException {
        TestUtils.forceDelete(tempDir);
    }

}
