/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2021 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.module;

import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.PipelineHashIO;
import org.dita.dost.util.XMLUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.dita.dost.util.Constants.ANT_INVOKER_EXT_PARAM_STYLE;
import static org.dita.dost.util.Constants.ANT_INVOKER_EXT_PARAM_TRANSTYPE;

@RunWith(Parameterized.class)
public class MoveMetaModuleTest extends AbstractModuleTest {
    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"MatadataInheritance_foreign"},
                {"MatadataInheritance_keywords"},
                {"MatadataInheritance_linktext"},
                {"MatadataInheritance_othermeta"},
                {"MatadataInheritance_pemissions"},
                {"MatadataInheritance_pemissions_replace"},
                {"MatadataInheritance_prodinfo"},
                {"MatadataInheritance_publisher"},
                {"MatadataInheritance_resourceid"},
                {"MatadataInheritance_searchtitle"},
                {"MatadataInheritance_shortdesc"},
                {"MatadataInheritance_source"},
                {"MatadataInheritance_source_replace"},
                {"MatadataInheritance_unknown"},
                {"MetadataInheritance_audience"},
                {"MetadataInheritance_author"},
                {"MetadataInheritance_category"},
                {"MetadataInheritance_copyright"},
                {"MetadataInheritance_critdates"},
                {"MetadataInheritance_critdates_replace"},
                {"MetadataInheritance_data"},
                {"MetadataInheritance_dataabout"}
        });
    }

    public MoveMetaModuleTest(String testCase) {
        super(testCase, Collections.emptyMap());
    }

    @Override
    protected AbstractPipelineInput getAbstractPipelineInput() {
        final AbstractPipelineInput input = new PipelineHashIO();
        input.setAttribute(ANT_INVOKER_EXT_PARAM_TRANSTYPE, "html5");
        input.setAttribute(ANT_INVOKER_EXT_PARAM_STYLE,
                Paths.get("src", "main", "plugins", "org.dita.base", "xsl", "preprocess", "mappull.xsl").toString());
        return input;
    }

    @Override
    protected AbstractPipelineModule getModule(final File tempDir) {
        return new MoveMetaModule();
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        final XMLUtils xmlUtils = new XMLUtils();
        final DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        final DocumentBuilder b = f.newDocumentBuilder();
        for (File file : tempDir.listFiles((dir, name) -> name.endsWith("dita") || name.endsWith("ditamap"))) {
            final Document d = b.parse(file);
            d.appendChild(d.createProcessingInstruction("workdir-uri", tempDir.toURI().toString()));
            xmlUtils.writeDocument(d, file);
        }
    }

    @Test
    @Ignore
    public void parallelFile() {
        // Ignore because MoveMetaModule doesn't use parallel features
    }

    @Test
    @Ignore
    public void parallelMemory() {
        // Ignore because MoveMetaModule doesn't use parallel features
    }
}
