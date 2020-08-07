/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2016 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.reader;

import com.google.common.collect.ImmutableMap;
import org.dita.dost.TestUtils;
import org.dita.dost.TestUtils.CachingLogger;
import org.dita.dost.TestUtils.CachingLogger.Message;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.store.StreamStore;
import org.dita.dost.util.Job;
import org.dita.dost.util.XMLUtils;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.net.URI;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.dita.dost.TestUtils.CachingLogger.Message.Level.WARN;

import static org.junit.Assert.assertEquals;

public class CopyToReaderTest {

    private final File resourceDir = TestUtils.getResourceDir(CopyToReaderTest.class);
    private final File srcDir = new File(resourceDir, "src");
    private CopyToReader reader;
    private XMLReader parser;
    private CachingLogger logger;

    @Before
    public void setUp() throws Exception {
        logger = new CachingLogger();

        reader = new CopyToReader();
        reader.setJob(new Job(srcDir, new StreamStore(srcDir, new XMLUtils())));
        reader.setLogger(logger);
        reader.setContentHandler(new DefaultHandler());

        parser = XMLUtils.getXMLReader();
        parser.setContentHandler(reader);
    }

    @Test
    public void testGetCopytoMap() throws Exception {
        final URI inputFile = new File(srcDir, "test.ditamap").toURI();
        reader.setCurrentFile(inputFile);
        parser.parse(inputFile.toString());

        final Map<URI, URI> exp = ImmutableMap.of(
                inputFile.resolve("direct.dita"), inputFile.resolve("topic.dita"),
                inputFile.resolve("keyref.dita"), inputFile.resolve("topic.dita"),
                inputFile.resolve("b.dita"), inputFile.resolve("a.dita"),
                inputFile.resolve("skip-b.dita"), inputFile.resolve("skip-a.dita")
        );

        assertEquals(exp, reader.getCopyToMap());
        assertEquals(0, logger.getMessages().size());
    }

    @Test
    public void testGetCopytoMapKeyref() throws Exception {
        final URI inputFile = new File(srcDir, "keyref.ditamap").toURI();
        reader.setCurrentFile(inputFile);
        parser.parse(inputFile.toString());

        final Map<URI, URI> exp = ImmutableMap.of(
                inputFile.resolve("keyref-target.dita"), inputFile.resolve("keyref-source-a.dita")
        );

        assertEquals(exp, reader.getCopyToMap());
        assertEquals(0, logger.getMessages().size());
    }

    @Test
    public void testGetCopytoMapSame() throws Exception {
        final URI inputFile = new File(srcDir, "same.ditamap").toURI();
        reader.setCurrentFile(inputFile);
        parser.parse(inputFile.toString());

        final Map<URI, URI> exp = ImmutableMap.of(
                inputFile.resolve("target.dita"), inputFile.resolve("source-a.dita")
        );

        assertEquals(exp, reader.getCopyToMap());
        final List<Message> expLog = Arrays.asList(
                new Message(WARN, MessageFormat.format(MessageUtils.getMessage("DOTX065W").toString(), "source-b.dita", new File(srcDir, "target.dita").toURI()), null));
        assertEquals(expLog, logger.getMessages());
    }
}
