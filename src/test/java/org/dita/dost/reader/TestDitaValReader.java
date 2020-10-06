/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2010 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.reader;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.dita.dost.TestUtils;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.store.StreamStore;
import org.dita.dost.util.FilterUtils;
import org.dita.dost.util.FilterUtils.Action;
import org.dita.dost.util.FilterUtils.FilterKey;
import org.dita.dost.util.Job;
import org.dita.dost.util.XMLUtils;
import org.junit.Before;
import org.junit.Test;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.emptySet;
import static javax.xml.XMLConstants.XML_NS_URI;
import static org.dita.dost.TestUtils.CachingLogger.Message.Level.WARN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestDitaValReader {

    private static final QName PLATFORM = QName.valueOf("platform");
    private static final QName PRODUCT = QName.valueOf("product");
    private static final QName AUDIENCE = QName.valueOf("audience");
    private static final QName PROPS = QName.valueOf("props");
    private static final QName KEYWORD = QName.valueOf("keyword");
    private static final QName REV = QName.valueOf("rev");
    private static final QName LANG = new QName(XML_NS_URI, "lang", "xml");
    private static final QName CONFIDENTIALITY = new QName("http://www.cms.com/", "confidentiality");


    private final File resourceDir = TestUtils.getResourceDir(TestDitaValReader.class);

    private Job job;
    private DITAOTLogger logger;

    @Before
    public void setUp() throws IOException {
        logger = new TestUtils.TestLogger();
        final XMLUtils xmlUtils = new XMLUtils();
        xmlUtils.setLogger(logger);
        job = new Job(resourceDir, new StreamStore(resourceDir, xmlUtils));
    }

    @Test
    public void testRead() {
        final File ditavalFile = new File(resourceDir, "src" + File.separator + "DITAVAL_1.ditaval");
        DitaValReader reader = new DitaValReader();
        reader.setLogger(logger);
        reader.setJob(job);
        reader.read(ditavalFile.toURI());
        final Map<FilterKey, Action> map = reader.getFilterMap();
        assertTrue(map.get(new FilterKey(AUDIENCE, "Cindy")) instanceof FilterUtils.Include);
        assertTrue(map.get(new FilterKey(QName.valueOf("produt"), "p1")) instanceof FilterUtils.Flag);
        assertTrue(map.get(new FilterKey(PRODUCT, "ABase_ph")) instanceof FilterUtils.Exclude);
        assertTrue(map.get(new FilterKey(PRODUCT, "AExtra_ph")) instanceof FilterUtils.Include);
        assertTrue(map.get(new FilterKey(PRODUCT, "Another_ph")) instanceof FilterUtils.Exclude);
        assertTrue(map.get(new FilterKey(PLATFORM, "Windows")) instanceof FilterUtils.Flag);
        assertTrue(map.get(new FilterKey(PLATFORM, "Linux")) instanceof FilterUtils.Flag);
        assertTrue(map.get(new FilterKey(KEYWORD, "key1")) instanceof FilterUtils.Exclude);
        assertTrue(map.get(new FilterKey(KEYWORD, "key2")) instanceof FilterUtils.Flag);
        assertTrue(map.get(new FilterKey(KEYWORD, "key3")) instanceof FilterUtils.Include);
        assertTrue(map.get(new FilterKey(PROPS, null)) instanceof FilterUtils.Include);
    }

    @Test
    public void testAnyAttributeDisabled() {
        final File ditavalFile = new File(resourceDir, "src" + File.separator + "any.ditaval");
        DitaValReader reader = new DitaValReader();
        TestUtils.CachingLogger logger = new TestUtils.CachingLogger();
        reader.setLogger(logger);
        reader.setJob(job);
        reader.read(ditavalFile.toURI());
        final Map<FilterKey, Action> act = reader.getFilterMap();

        final Map<FilterKey, Action> exp = ImmutableMap.of(
                new FilterKey(PLATFORM, "windows"), Action.EXCLUDE,
                new FilterKey(LANG, "fr"), Action.EXCLUDE,
                new FilterKey(CONFIDENTIALITY, "confidential"), Action.EXCLUDE,
                new FilterKey(QName.valueOf("default"), null), Action.INCLUDE
        );
        assertEquals(exp, act);
        assertEquals(Arrays.asList(WARN), logger.getMessages().stream().map(msg -> msg.level).collect(Collectors.toList()));
    }

    @Test
    public void testAnyAttribute() {
        final File ditavalFile = new File(resourceDir, "src" + File.separator + "any.ditaval");
        DitaValReader reader = new DitaValReader(ImmutableSet.of(LANG, CONFIDENTIALITY, REV), emptySet());
        TestUtils.CachingLogger logger = new TestUtils.CachingLogger();
        reader.setLogger(logger);
        reader.setJob(job);
        reader.read(ditavalFile.toURI());
        final Map<FilterKey, Action> act = reader.getFilterMap();
        final Map<FilterKey, Action> exp = ImmutableMap.of(
                new FilterKey(PLATFORM, "windows"), Action.EXCLUDE,
                new FilterKey(LANG, "fr"), Action.EXCLUDE,
                new FilterKey(CONFIDENTIALITY, "confidential"), Action.EXCLUDE,
                new FilterKey(REV, "10"), Action.EXCLUDE,
                new FilterKey(QName.valueOf("default"), null), Action.INCLUDE
        );
        assertEquals(exp, act);
    }

}
