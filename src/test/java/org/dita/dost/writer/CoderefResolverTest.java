/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */
package org.dita.dost.writer;

import static org.apache.commons.io.FileUtils.*;
import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;

import java.io.File;
import java.io.IOException;

import org.custommonkey.xmlunit.XMLUnit;
import org.dita.dost.util.Job;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.dita.dost.TestUtils;
import org.dita.dost.exception.DITAOTException;

public class CoderefResolverTest {

    private static final File resourceDir = TestUtils.getResourceDir(CoderefResolverTest.class);
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
        copyFile(new File(srcDir, "test.dita"), f);
        copyFile(new File(srcDir, "code.xml"), new File(tempDir, "code.xml"));
        copyFile(new File(srcDir, "utf-8.xml"), new File(tempDir, "utf-8.xml"));
        copyFile(new File(srcDir, "plain.txt"), new File(tempDir, "plain.txt"));

        final CoderefResolver filter = new CoderefResolver();
        filter.setLogger(new TestUtils.TestLogger());
        filter.setJob(new Job(tempDir));
        filter.write(f.getAbsoluteFile());

        TestUtils.resetXMLUnit();
        XMLUnit.setIgnoreWhitespace(false);
        assertXMLEqual(new InputSource(new File(expDir, "test.dita").toURI().toString()),
                new InputSource(f.toURI().toString()));
    }

    @AfterClass
    public static void teardown() throws IOException {
        TestUtils.forceDelete(tempDir);
    }

}
