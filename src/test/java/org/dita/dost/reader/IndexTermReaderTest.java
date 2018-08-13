/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2011 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.reader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.dita.dost.util.XMLUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.dita.dost.TestUtils;
import org.dita.dost.index.IndexTerm;
import org.dita.dost.index.IndexTermCollection;
import org.dita.dost.index.IndexTermTarget;

/**
 * IndexTermReader unit test.
 * 
 * @author Jarno Elovirta
 */
public class IndexTermReaderTest {

    final File resourceDir = TestUtils.getResourceDir(IndexTermReaderTest.class);
    private File tempDir;

    @Before
    public void setUp() throws IOException {
        tempDir = TestUtils.createTempDir(getClass());
    }

    @Test
    public void testExtractIndexTerm() throws SAXException {
        final IndexTermCollection indexTermCollection = new IndexTermCollection();
        final File target = new File(tempDir, "concept.html");
        final IndexTermReader handler = new IndexTermReader(indexTermCollection);
        handler.setTargetFile(target.getAbsolutePath());
        final XMLReader xmlReader = XMLUtils.getXMLReader();
        xmlReader.setContentHandler(handler);
        final File source = new File(resourceDir, "concept.dita");
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(source);
            xmlReader.parse(new InputSource(inputStream));
        } catch (final Exception e) {
            fail(e.getMessage());
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (final IOException e) {
                    fail(e.getMessage());
                }
            }
        }
        final List<IndexTerm> act = indexTermCollection.getTermList();

        final List<IndexTerm> exp = new ArrayList<IndexTerm>();
        exp.add(generateIndexTerms(target, "Primary", "Secondary", "Tertiary"));
        exp.add(generateIndexTerms(target, "Primary normalized", "Secondary normalized", "Tertiary normalized"));
        exp.add(generateIndexTerms(target, " Primary unnormalized ", " Secondary unnormalized ", " Tertiary unnormalized "));
        exp.add(generateIndexTermErrorCondition(target, "Test empty title"));

        assertEquals(new HashSet<IndexTerm>(exp),
                new HashSet<IndexTerm>(act));
    }

    @After
    public void tearDown() throws IOException {
        TestUtils.forceDelete(tempDir);
    }

    private IndexTerm generateIndexTerms(final File target, final String... texts) {
        final LinkedList<IndexTerm> stack = new LinkedList<IndexTerm>();
        for (final String text: texts) {
            final IndexTerm primary = generateIndexTerm(target, text);
            if (!stack.isEmpty()) {
                stack.getLast().addSubTerm(primary);
            }
            stack.addLast(primary);
        }
        return stack.getFirst();
    }

    private IndexTerm generateIndexTerm(final File target, final String text) {
        final IndexTerm primary = new IndexTerm();
        primary.setTermName(text);
        primary.setTermKey(text);
        if (target != null) {
            final IndexTermTarget primaryTarget = new IndexTermTarget();
            primaryTarget.setTargetName("Index test");
            primaryTarget.setTargetURI(target.getAbsolutePath() + "#concept");
            primary.addTarget(primaryTarget);
        }
        return primary;
    }
    
    private IndexTerm generateIndexTermErrorCondition(final File target, final String text) {
        final IndexTerm primary = new IndexTerm();
        primary.setTermName(text);
        primary.setTermKey(text);
        if (target != null) {
            final IndexTermTarget primaryTarget = new IndexTermTarget();
            primaryTarget.setTargetName("***");
            primaryTarget.setTargetURI(target.getAbsolutePath() + "#error");
            primary.addTarget(primaryTarget);
        }
        return primary;
    }

}
