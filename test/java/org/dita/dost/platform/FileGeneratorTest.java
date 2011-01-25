/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2011 All Rights Reserved.
 */
package org.dita.dost.platform;

import static org.junit.Assert.assertEquals;

import org.dita.dost.TestUtils;
import org.dita.dost.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class FileGeneratorTest {

    private final File resourceDir = new File("test-stub", FileGeneratorTest.class.getSimpleName());
    private File tempDir;
    
    private static File tempFile;
    private final static Hashtable<String, String> features = new Hashtable<String, String>();
    static {        
        features.put("element", "foo,bar,baz");
        features.put("attribute", "foo,bar,baz");
    }
    
    @Before
    public void setUp() throws Exception {
        tempDir = TestUtils.createTempDir(getClass());
        tempFile = new File(tempDir, "dummy_template.xml");
        FileUtils.copyFile(new File(resourceDir, "src" + File.separator + "dummy_template.xml"),
                           tempFile);
    }

    @Test
    public void testGenerate() throws IOException {
        final FileGenerator f = new FileGenerator(features);
        f.generate(tempFile.getAbsolutePath());
        
        assertEquals(TestUtils.readFileToString(new File(resourceDir, "exp" + File.separator + "dummy.xml")),
                     TestUtils.readFileToString(new File(tempDir, "dummy.xml")));
    }

    @After
    public void tearDown() throws IOException {
        TestUtils.forceDelete(tempDir);
    }

    private static abstract class AbstractAction implements IAction {
        protected List<String> inputs = new ArrayList<String>();
        protected Map<String, String> params = new HashMap<String, String>();
        protected Hashtable<String, String> features;
        public void setInput(final String input) {
            final StringTokenizer inputTokenizer = new StringTokenizer(input, Integrator.FEAT_VALUE_SEPARATOR);
            while(inputTokenizer.hasMoreElements()){
                inputs.add(inputTokenizer.nextToken());
            }
        }
        public void addParam(final String name, final String value) {
    		params.put(name, value);
    	}
        public void setFeatures(Hashtable<String, String> features) {
            this.features = features;
        }
        public abstract String getResult();
    } 
    
    public static class ElementAction extends AbstractAction {
        @Override
        public String getResult() {
            final Map<String, String> paramsExp = new HashMap<String, String>();
            paramsExp.put(FileGenerator.PARAM_TEMPLATE, tempFile.getAbsolutePath());
            paramsExp.put("extension", "element");
            assertEquals(paramsExp, params);
            final List<String> inputExp = Arrays.asList(new String[] {"foo", "bar", "baz"});
            assertEquals(inputExp, inputs);
            assertEquals(FileGeneratorTest.features, features);
            return "<foo bar='baz'>quz</foo>";
        }
    }
    
    public static class AttributeAction extends AbstractAction {
        @Override
        public String getResult() {
            final Map<String, String> paramsExp = new HashMap<String, String>();
            paramsExp.put(FileGenerator.PARAM_TEMPLATE, tempFile.getAbsolutePath());
            paramsExp.put(FileGenerator.PARAM_LOCALNAME, "foo");
            assertEquals(paramsExp, params);
            final List<String> inputExp = Arrays.asList(new String[] {"attribute"});
            assertEquals(inputExp, inputs);
            assertEquals(FileGeneratorTest.features, features);
            return " foo='bar'";
        }
    }
    
}
