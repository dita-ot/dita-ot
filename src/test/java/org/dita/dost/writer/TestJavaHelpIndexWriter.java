/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying LICENSE file for applicable license.
 */
/*
 * Copyright 2010 IBM Corporation
 */
package org.dita.dost.writer;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.xml.resolver.CatalogManager;
import org.apache.xml.resolver.tools.CatalogResolver;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.dita.dost.TestUtils;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.index.IndexTerm;

public class TestJavaHelpIndexWriter {

    private static File tempDir;
    private static final File resourceDir = TestUtils.getResourceDir(TestJavaHelpIndexWriter.class);
    private static final File expDir = new File(resourceDir, "exp");
    private static final File etcDir = new File(resourceDir, "resource");

    @BeforeClass
    public static void setUp() throws IOException {
        tempDir = TestUtils.createTempDir(TestJavaHelpIndexWriter.class);
        TestUtils.resetXMLUnit();
    }

    @Test
    public void testwrite() throws DITAOTException, SAXException, IOException {
//        final Content content = new ContentImpl();
        final IndexTerm indexterm1 = new IndexTerm();
        indexterm1.setTermName("name1");
        indexterm1.setTermKey("indexkey1");
        final IndexTerm indexterm2 = new IndexTerm();
        indexterm2.setTermName("name2");
        indexterm2.setTermKey("indexkey2");
        indexterm1.addSubTerm(indexterm2);
        final List<IndexTerm> collection = new ArrayList<IndexTerm>();
        collection.add(indexterm1);
//        content.setCollection(collection);

        final JavaHelpIndexWriter javahelpindexwriter = new JavaHelpIndexWriter();
//        javahelpindexwriter.setContent(content);
        javahelpindexwriter.setTermList(collection);
        final File outFile = new File(tempDir, "javahelpindexwriteroutput.xml");
        javahelpindexwriter.write(outFile.getAbsoluteFile());

        final CatalogManager manager = new CatalogManager();
        manager.setIgnoreMissingProperties(true);
        manager.setUseStaticCatalog(false);
        manager.setPreferPublic(true);
        manager.setCatalogFiles(new File(etcDir, "catalog.xml").toURI().toString());
        final EntityResolver resolver = new CatalogResolver(manager);
        XMLUnit.setControlEntityResolver(resolver);
        XMLUnit.setTestEntityResolver(resolver);
        XMLUnit.setIgnoreWhitespace(true);
        assertXMLEqual(new InputSource(new File(expDir, "comparejavahelpindexwriteroutput.xml").toURI().toString()),
                new InputSource(outFile.toURI().toString()));
    }

    @AfterClass
    public static void tearDown() throws IOException {
        TestUtils.forceDelete(tempDir);
    }

}
