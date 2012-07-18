/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2010 All Rights Reserved.
 */
package org.dita.dost.writer;


import static org.junit.Assert.assertEquals;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import org.dita.dost.TestUtils;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.module.Content;
import org.dita.dost.module.ContentImpl;
import org.dita.dost.pipeline.PipelineFacade;
import org.dita.dost.pipeline.PipelineHashIO;
import org.dita.dost.reader.DitaValReader;
import org.dita.dost.util.FilterUtils.Action;
import org.dita.dost.util.FilterUtils.FilterKey;
import org.dita.dost.util.Constants;
import org.dita.dost.util.FilterUtils;
import org.dita.dost.util.OutputUtils;
import org.dita.dost.writer.DitaWriter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


public class TestDitaWriter {

    private final File resourceDir = TestUtils.testStub;
    private File tempDir;

    public DitaWriter writer;

    private final File baseDir = new File(resourceDir, "DITA-OT1.5");
    private final File inputDir = new File("DITAVAL");
    private final File inputMap = new File(inputDir, "DITAVAL_testdata1.ditamap");
    private final File outDir = new File(tempDir, "out");
    private final File ditavalFile = new File(inputDir, "DITAVAL_1.ditaval");

    private DocumentBuilder builder;

    private PipelineHashIO pipelineInput;

    @Before
    public void setUp() throws Exception {
        tempDir = TestUtils.createTempDir(getClass());

        final PipelineFacade facade = new PipelineFacade();
        facade.setLogger(new TestUtils.TestLogger());
        pipelineInput = new PipelineHashIO();
        pipelineInput.setAttribute("inputmap", inputMap.getPath());
        pipelineInput.setAttribute("basedir", baseDir.getAbsolutePath());
        pipelineInput.setAttribute("inputdir", inputDir.getPath());
        pipelineInput.setAttribute("outputdir", outDir.getAbsolutePath());
        pipelineInput.setAttribute("tempDir", tempDir.getAbsolutePath());
        pipelineInput.setAttribute("ditadir", new File("src" + File.separator + "main").getAbsolutePath());
        pipelineInput.setAttribute("ditaext", ".xml");
        pipelineInput.setAttribute("indextype", "xhtml");
        pipelineInput.setAttribute("encoding", "en-US");
        pipelineInput.setAttribute("targetext", ".html");
        pipelineInput.setAttribute("validate", "false");
        pipelineInput.setAttribute("generatecopyouter", "1");
        pipelineInput.setAttribute("outercontrol", "warn");
        pipelineInput.setAttribute("onlytopicinmap", "false");
        pipelineInput.setAttribute("ditalist", new File(tempDir, "dita.list").getAbsolutePath());
        pipelineInput.setAttribute("maplinks", new File(tempDir, "maplinks.unordered").getAbsolutePath());
        pipelineInput.setAttribute("transtype", "xhtml");
        pipelineInput.setAttribute("ditaval", ditavalFile.getPath());
        pipelineInput.setAttribute(Constants.ANT_INVOKER_EXT_PARAN_SETSYSTEMID, "no");

        facade.execute("GenMapAndTopicList", pipelineInput);

        writer = new DitaWriter();
        writer.setLogger(new TestUtils.TestLogger());
        writer.initXMLReader(new File("src" + File.separator + "main").getAbsoluteFile(), false, true);

        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
        builder = factory.newDocumentBuilder();
    }

    @Test
    public void testWrite() throws DITAOTException, ParserConfigurationException, SAXException, IOException {
        String ditavalFile = pipelineInput.getAttribute(Constants.ANT_INVOKER_PARAM_DITAVAL);
        ditavalFile = new File(baseDir, ditavalFile).getAbsolutePath();
        final DitaValReader filterReader = new DitaValReader();
        filterReader.read(ditavalFile);
        
        filterReader.loadSubjectScheme(new File(inputDir, "subject_scheme.ditamap").getPath());
        writer.setValidateMap(filterReader.getValidValuesMap());
        writer.setDefaultValueMap(filterReader.getDefaultValueMap());

        final Map<FilterKey, Action> map = filterReader.getFilterMap();
        assertEquals(Action.INCLUDE, map.get(new FilterKey("audience", "Cindy")));
        assertEquals(Action.FLAG, map.get(new FilterKey("produt", "p1")));
        assertEquals(Action.EXCLUDE, map.get(new FilterKey("product", "ABase_ph")));
        assertEquals(Action.INCLUDE, map.get(new FilterKey("product", "AExtra_ph")));
        assertEquals(Action.EXCLUDE, map.get(new FilterKey("product", "Another_ph")));
        assertEquals(Action.FLAG, map.get(new FilterKey("platform", "Windows")));
        assertEquals(Action.FLAG, map.get(new FilterKey("platform", "Linux")));
        assertEquals(Action.EXCLUDE, map.get(new FilterKey("keyword", "key1")));
        assertEquals(Action.FLAG, map.get(new FilterKey("keyword", "key2")));
        assertEquals(Action.INCLUDE, map.get(new FilterKey("keyword", "key3")));
        assertEquals(Action.EXCLUDE, map.get(new FilterKey("product", "key1")));
        assertEquals(Action.FLAG, map.get(new FilterKey("product", "key2")));
        assertEquals(Action.INCLUDE, map.get(new FilterKey("product", "key3")));

        final FilterUtils filterUtils = new FilterUtils();
        filterUtils.setLogger(new TestUtils.TestLogger());
        filterUtils.setFilterMap(map);
        writer.setFilterUtils(filterUtils);
        final OutputUtils outputUtils = new OutputUtils();
        outputUtils.setInputMapPathName(new File(baseDir, inputDir.getPath() + File.separator + "keyword.dita"));
        writer.setOutputUtils(outputUtils);
        writer.setTempDir(tempDir.getAbsoluteFile());
        writer.setExtName(".xml");
        writer.write(new File(baseDir, inputDir.getPath()).getAbsoluteFile(), "keyword.dita");

        compareKeyword(new File(baseDir, new File(inputDir, "keyword.dita").getPath()),
                new String[] {"prodname1", "prodname2", "prodname3"},
                new String[] {"key1", "key2", "key3"});

        compareKeyword(new File(tempDir, "keyword.xml"),
                new String[] {"prodname2", "prodname3"},
                new String[] {"key2", "key3"});
    }

    private void compareKeyword(final File filePath, final String[] ids,
            final String[] products) throws SAXException, IOException {
        final Document document = builder.parse(filePath.toURI().toString());
        final Element elem = document.getDocumentElement();
        final NodeList nodeList = elem.getElementsByTagName("keyword");
        for(int i = 0; i<nodeList.getLength(); i++){
            assertEquals(ids[i], ((Element)nodeList.item(i)).getAttribute("id"));
            assertEquals(products[i], ((Element)nodeList.item(i)).getAttribute("product"));
        }
    }

    @After
    public void tearDown() throws IOException {
        TestUtils.forceDelete(tempDir);
    }

}
