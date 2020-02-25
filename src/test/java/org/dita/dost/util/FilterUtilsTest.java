/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2012 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.util;

import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static javax.xml.XMLConstants.XML_NS_PREFIX;
import static javax.xml.XMLConstants.XML_NS_URI;
import static org.dita.dost.util.Constants.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.dita.dost.TestUtils;
import org.dita.dost.util.FilterUtils.Action;
import org.dita.dost.util.FilterUtils.FilterKey;

import org.dita.dost.util.FilterUtils.Flag;
import org.dita.dost.util.XMLUtils.AttributesBuilder;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;

import org.junit.Test;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

public class FilterUtilsTest {

    private static final QName PLATFORM = QName.valueOf("platform");
    private static final QName AUDIENCE = QName.valueOf("audience");
    private static final QName OS = QName.valueOf("os");
    private static final QName PROPS = QName.valueOf("props");
    private static final QName GUI = QName.valueOf("gui");
    private static final QName OTHERPROPS = QName.valueOf("otherprops");
    private static final QName REV = QName.valueOf("rev");

    private static final Map<FilterKey, Action> filterMap = ImmutableMap.<FilterKey, Action>builder()
            .put(new FilterKey(PLATFORM, "unix"), Action.INCLUDE)
            .put(new FilterKey(PLATFORM, "osx"), Action.INCLUDE)
            .put(new FilterKey(PLATFORM, "linux"), Action.INCLUDE)
            .put(new FilterKey(PLATFORM, "windows"), Action.EXCLUDE)
            .put(new FilterKey(AUDIENCE, "expert"), Action.INCLUDE)
            .put(new FilterKey(AUDIENCE, "novice"), Action.EXCLUDE)
            .build();

    @Test
    public void testNeedExcludeNoAttribute() {
        final FilterUtils f = new FilterUtils(false, filterMap, null, null);

        assertFalse(f.needExclude(new AttributesImpl(), new QName[0][0]));
    }

    @Test
    public void testNeedExcludeDefaultExclude() {
        final Map<FilterKey, Action> fm = new HashMap<>(filterMap);
        fm.put(new FilterKey(PLATFORM, null), Action.EXCLUDE);
        final FilterUtils f = new FilterUtils(false, fm, null, null);
        f.setLogger(new TestUtils.TestLogger());

        assertFalse(f.needExclude(new AttributesImpl(), new QName[0][0]));
        assertFalse(f.needExclude(attr(PLATFORM, "amiga unix windows"), new QName[0][0]));
        assertTrue(f.needExclude(attr(PLATFORM, "amiga windows"), new QName[0][0]));
        assertTrue(f.needExclude(attr(PLATFORM, "windows"), new QName[0][0]));
    }

    private final QName lang = new QName(XML_NS_URI, "lang", XML_NS_PREFIX);
    private final QName confidentiality = new QName("http://www.cms.com/", "confidentiality", "cms");

    @Test
    public void testFilterAnyAttribute() {

        final Map<FilterKey, Action> fm = new HashMap<>();
        fm.put(new FilterKey(QName.valueOf(ATTRIBUTE_NAME_REV), null), Action.EXCLUDE);
        fm.put(new FilterKey(lang, "fr"), Action.EXCLUDE);
        fm.put(new FilterKey(confidentiality, "confidential"), Action.EXCLUDE);
        final FilterUtils f = new FilterUtils(false, fm, null, null,
            ImmutableSet.of(QName.valueOf(ATTRIBUTE_NAME_REV), lang, confidentiality), emptySet()
        );
        f.setLogger(new TestUtils.TestLogger());

        assertFalse(f.needExclude(new AttributesBuilder().add(ATTRIBUTE_NAME_PLATFORM, "unix").build(), new QName[0][0]));
        assertTrue(f.needExclude(new AttributesBuilder().add(ATTRIBUTE_NAME_REV, "1").build(), new QName[0][0]));
        assertFalse(f.needExclude(new AttributesBuilder().add(lang, "en").build(), new QName[0][0]));
        assertTrue(f.needExclude(new AttributesBuilder().add(lang, "fr").build(), new QName[0][0]));
        assertTrue(f.needExclude(new AttributesBuilder().add(confidentiality, "confidential").build(), new QName[0][0]));
        assertFalse(f.needExclude(new AttributesBuilder().add(confidentiality, "public").build(), new QName[0][0]));
    }

    @Test
    public void testFilterAnyAttributeDisabled() {
        final Map<FilterKey, Action> fm = new HashMap<>();
        fm.put(new FilterKey(QName.valueOf(ATTRIBUTE_NAME_REV), null), Action.EXCLUDE);
        fm.put(new FilterKey(lang, "fr"), Action.EXCLUDE);
        fm.put(new FilterKey(confidentiality, "confidential"), Action.EXCLUDE);
        final FilterUtils f = new FilterUtils(false, fm, null, null);
        f.setLogger(new TestUtils.TestLogger());

        assertFalse(f.needExclude(new AttributesBuilder().add(ATTRIBUTE_NAME_PLATFORM, "unix").build(), new QName[0][0]));
        assertFalse(f.needExclude(new AttributesBuilder().add(ATTRIBUTE_NAME_REV, "1").build(), new QName[0][0]));
        assertFalse(f.needExclude(new AttributesBuilder().add(lang, "en").build(), new QName[0][0]));
        assertFalse(f.needExclude(new AttributesBuilder().add(lang, "fr").build(), new QName[0][0]));
        assertFalse(f.needExclude(new AttributesBuilder().add(confidentiality, "confidential").build(), new QName[0][0]));
        assertFalse(f.needExclude(new AttributesBuilder().add(confidentiality, "public").build(), new QName[0][0]));
    }

    @Test
    public void testNeedExcludeDefaultInclude() {
        final Map<FilterKey, Action> fm = new HashMap<>(filterMap);
        fm.put(new FilterKey(PLATFORM, null), Action.INCLUDE);
        final FilterUtils f = new FilterUtils(false, fm, null, null);
        f.setLogger(new TestUtils.TestLogger());

        assertFalse(f.needExclude(attr(PLATFORM, "amiga unix windows"), new QName[0][0]));
        assertFalse(f.needExclude(attr(PLATFORM, "amiga windows"), new QName[0][0]));
        assertTrue(f.needExclude(attr(PLATFORM, "windows"), new QName[0][0]));
    }

    @Test
    public void testNeedExclude() {
        final FilterUtils f = new FilterUtils(false, filterMap, null, null);
        f.setLogger(new TestUtils.TestLogger());

        assertFalse(f.needExclude(attr(PLATFORM, "amiga unix windows"), new QName[0][0]));
        assertFalse(f.needExclude(attr(PLATFORM, "amiga windows"), new QName[0][0]));
        assertTrue(f.needExclude(attr(PLATFORM, "windows"), new QName[0][0]));
    }

    @Test
    public void testNeedExcludeMultipleAttributes() {
        final FilterUtils f = new FilterUtils(false, filterMap, null, null);
        f.setLogger(new TestUtils.TestLogger());

        final AttributesImpl amigaUnix = new AttributesImpl();
        XMLUtils.addOrSetAttribute(amigaUnix, PLATFORM, "amiga unix windows");
        XMLUtils.addOrSetAttribute(amigaUnix, AUDIENCE, "expert");
        assertFalse(f.needExclude(amigaUnix, new QName[0][0]));

        final AttributesImpl amiga = new AttributesImpl();
        XMLUtils.addOrSetAttribute(amiga, PLATFORM, "amiga windows");
        XMLUtils.addOrSetAttribute(amiga, AUDIENCE, "expert");
        assertFalse(f.needExclude(amiga, new QName[0][0]));

        final AttributesImpl windows = new AttributesImpl();
        XMLUtils.addOrSetAttribute(windows, PLATFORM, "windows");
        XMLUtils.addOrSetAttribute(windows, AUDIENCE, "novice");
        assertTrue(f.needExclude(windows, new QName[0][0]));
    }

    @Test
    public void testNeedExcludeDomainAttribute() {
        final Map<FilterKey, Action> fm = new HashMap<>();
        fm.put(new FilterKey(OS, "amiga"), Action.INCLUDE);
        fm.put(new FilterKey(OS, null), Action.EXCLUDE);
        final FilterUtils f = new FilterUtils(false, fm, null, null);
        f.setLogger(new TestUtils.TestLogger());

        assertFalse(f.needExclude(attr(OS, "amiga unix windows"), new QName[][] {{PROPS, OS}}));
        assertFalse(f.needExclude(attr(OS, "amiga windows"), new QName[][] {{PROPS, OS}}));
        assertFalse(f.needExclude(attr(OS, "amiga windows"), new QName[][] {{PROPS, OS, GUI}}));
        assertFalse(f.needExclude(attr(GUI, "amiga windows"),new QName[][] {{PROPS, OS, GUI}}));
        assertTrue(f.needExclude(attr(OS, "windows"), new QName[][] {{PROPS, OS}}));
    }
    
    @Test
    public void testNeedExcludeLabel() {
        final Map<FilterKey, Action> fm = new HashMap<>();
        fm.put(new FilterKey(OS, "amiga"), Action.INCLUDE);
        fm.put(new FilterKey(OS, null), Action.EXCLUDE);
        final FilterUtils f = new FilterUtils(false, fm, null, null);
        f.setLogger(new TestUtils.TestLogger());

        assertFalse(f.needExclude(attr(PROPS, "os(amiga unix windows)"), new QName[][] {{PROPS, OS}}));
        assertFalse(f.needExclude(attr(PROPS, "os(amiga windows)"), new QName[][] {{PROPS, OS, GUI}}));
        assertFalse(f.needExclude(attr(PROPS, "gui(amiga windows)"), new QName[][] {{PROPS, OS, GUI}}));
        assertTrue(f.needExclude(attr(PROPS, "os(windows)"), new QName[][] {{PROPS, OS}}));
        assertTrue(f.needExclude(attr(PROPS, "   os(   windows   )   "), new QName[][] {{PROPS, OS}}));
    }
    
    @Test
    public void testNeedExcludeOtherpropsLabel() {
        final Map<FilterKey, Action> fm = new HashMap<>();
        fm.put(new FilterKey(OS, "amiga"), Action.INCLUDE);
        fm.put(new FilterKey(OS, null), Action.EXCLUDE);
        final FilterUtils f = new FilterUtils(false, fm, null, null);
        f.setLogger(new TestUtils.TestLogger());

        assertFalse(f.needExclude(attr(OTHERPROPS, "os(amiga unix windows)"), new QName[0][0]));
        assertFalse(f.needExclude(attr(OTHERPROPS, "os(amiga windows)"), new QName[0][0]));
        assertTrue(f.needExclude(attr(OTHERPROPS, "os(windows)"), new QName[0][0]));
    }

    @Test
    public void testgetFlagsDefaultFlag() {
        final Flag flag = new Flag("prop", "red", null, null, null, null, null);
        final FilterUtils f = new FilterUtils(false,
                ImmutableMap.<FilterKey, Action>builder()
                        .put(new FilterKey(PLATFORM, null), flag)
                        .build(), null, null);
        f.setLogger(new TestUtils.TestLogger());

        assertEquals(
                emptySet(),
                f.getFlags(new AttributesImpl(), new QName[0][0]));
        assertEquals(
                singleton(flag),
                f.getFlags(attr(PLATFORM, "amiga unix windows"), new QName[0][0]));
    }

    @Test
    public void testGetFlags() {
        final Flag flag = new Flag("prop", "red", null, null, null, null, null);
        final Flag revflag = new Flag("revprop", null, null, null, "solid", null, null);
        final FilterUtils f = new FilterUtils(false,
                ImmutableMap.<FilterKey, Action>builder()
                        .put(new FilterKey(PLATFORM, "unix"), flag)
                        .put(new FilterKey(PLATFORM, "osx"), flag)
                        .put(new FilterKey(PLATFORM, "linux"), flag)
                        .put(new FilterKey(AUDIENCE, "expert"), flag)
                        .put(new FilterKey(REV, "r1"), revflag)
                        .build(), null, null);
        f.setLogger(new TestUtils.TestLogger());

        assertEquals(
                singleton(flag),
                f.getFlags(attr(PLATFORM, "amiga unix windows"), new QName[0][0]));
        assertEquals(
                singleton(flag),
                f.getFlags(attr(PLATFORM, "amiga unix"), new QName[0][0]));
        assertEquals(
                singleton(revflag),
                f.getFlags(attr(REV, "r1 r2"), new QName[0][0]));
        assertEquals(
                emptySet(),
                f.getFlags(attr(PLATFORM, "amiga"), new QName[0][0]));
        assertEquals(
                emptySet(),
                f.getFlags(attr(PLATFORM, "windows"), new QName[0][0]));
    }

    // DITA 1.3

    @Test
    public void testExtCheckExclude() {
        final Map<FilterKey, Action> fm = new HashMap<>();
        fm.put(new FilterKey(OS, "amiga"), Action.INCLUDE);
        fm.put(new FilterKey(OS, null), Action.EXCLUDE);
        fm.put(new FilterKey(PLATFORM, null), Action.EXCLUDE);
        final FilterUtils f = new FilterUtils(false, fm, null, null);
        f.setLogger(new TestUtils.TestLogger());

        assertFalse(f.extCheckExclude(new QName[] {PLATFORM, OS}, Arrays.asList("amiga", "unix", "windows")));
        assertTrue(f.extCheckExclude(new QName[] {PLATFORM, OS}, Arrays.asList("osx")));
        assertFalse(f.extCheckExclude(new QName[] {PLATFORM, OS}, Arrays.asList( "unix", "amiga", "windows")));
    }
    
    @Test
    public void testNeedExcludeGroup() {
        final Map<FilterKey, Action> fm = new HashMap<>();
        fm.put(new FilterKey(OS, "amiga"), Action.INCLUDE);
        fm.put(new FilterKey(OS, null), Action.EXCLUDE);
        fm.put(new FilterKey(PLATFORM, null), Action.EXCLUDE);
        final FilterUtils f = new FilterUtils(false, fm, null, null);
        f.setLogger(new TestUtils.TestLogger());

        assertFalse(f.needExclude(attr(PLATFORM, "os(amiga unix windows)"), new QName[0][0]));
        assertFalse(f.needExclude(attr(PLATFORM, "os(amiga windows)"), new QName[0][0]));
        assertTrue(f.needExclude(attr(PLATFORM, "gui(amiga windows)"), new QName[0][0]));
        assertTrue(f.needExclude(attr(PLATFORM, "os(windows)"), new QName[0][0]));
        assertTrue(f.needExclude(attr(PLATFORM, "   os(   windows   )   "), new QName[0][0]));
    }
    

    @Test
    public void testNeedExcludeGroupMultiple() {
        final Map<FilterKey, Action> fm = new HashMap<>();
        fm.put(new FilterKey(OS, "amiga"), Action.EXCLUDE);
        fm.put(new FilterKey(OS, "windows"), Action.EXCLUDE);
        fm.put(new FilterKey(OS, null), Action.INCLUDE);
        fm.put(new FilterKey(PLATFORM, null), Action.INCLUDE);
        final FilterUtils f = new FilterUtils(false, fm, null, null);
        f.setLogger(new TestUtils.TestLogger());

        assertFalse(f.needExclude(attr(PLATFORM, "os(amiga unix windows) database(mongo)"), new QName[0][0]));
        assertTrue(f.needExclude(attr(PLATFORM, "os(amiga windows) database(mongo)"), new QName[0][0]));
        assertFalse(f.needExclude(attr(PLATFORM, "gui(amiga windows) database(mongo)"), new QName[0][0]));
        assertTrue(f.needExclude(attr(PLATFORM, "os(windows) database(mongo)"), new QName[0][0]));
        assertTrue(f.needExclude(attr(PLATFORM, "   os(   windows   )   database(  mongo  )   "), new QName[0][0]));
    }
    
    @Test
    public void testNeedExcludeMixedGroups() {
        final Map<FilterKey, Action> fm = new HashMap<>();
        fm.put(new FilterKey(PLATFORM, "unix"), Action.EXCLUDE);
        fm.put(new FilterKey(PLATFORM, "windows"), Action.EXCLUDE);
        fm.put(new FilterKey(PLATFORM, null), Action.INCLUDE);
        final FilterUtils f = new FilterUtils(false, fm, null, null);
        f.setLogger(new TestUtils.TestLogger());

        assertFalse(f.needExclude(attr(PLATFORM, "windows database(mongodb couchbase) unix osx"), new QName[0][0]));
        assertTrue(f.needExclude(attr(PLATFORM, "windows database(mongodb couchbase) unix"), new QName[0][0]));
        assertTrue(f.needExclude(attr(PLATFORM, "database(mongodb couchbase) unix"), new QName[0][0]));
    }
    
    @Test
    public void testGetUngroupedValue() {
        final FilterUtils f = new FilterUtils(false);
        
        {
            final Map<QName, List<String>> exp = new HashMap<QName, List<String>>();
            exp.put(null, Arrays.asList("foo", "bar", "bax"));
            assertEquals(exp, f.getGroups("foo bar bax"));
        }
        {
            final Map<QName, List<String>> exp = new HashMap<QName, List<String>>();
            exp.put(null, Arrays.asList("foo", "bar"));
            exp.put(QName.valueOf("group"), Arrays.asList("a", "b", "c"));
            assertEquals(exp, f.getGroups("foo group(a b c) bar"));
        }
        {
            final Map<QName, List<String>> exp = new HashMap<QName, List<String>>();
            exp.put(null, Arrays.asList("foo"));
            exp.put(QName.valueOf("group"), Arrays.asList("a", "b", "c"));
            assertEquals(exp, f.getGroups("foo group(a b c)"));
        }
        {
            final Map<QName, List<String>> exp = new HashMap<QName, List<String>>();
            exp.put(null, Arrays.asList("bar"));
            exp.put(QName.valueOf("group"), Arrays.asList("a", "b", "c"));
            assertEquals(exp, f.getGroups("group(a b c) bar"));
        }
        {
            final Map<QName, List<String>> exp = new HashMap<QName, List<String>>();
            exp.put(null, Arrays.asList("foo", "bar", "baz"));
            exp.put(QName.valueOf("group1"), Arrays.asList("a", "b", "c"));
            exp.put(QName.valueOf("group2"), Arrays.asList("d", "e", "f"));
            assertEquals(exp, f.getGroups("foo group1(a b c) bar group2(d e f) baz"));
        }
        {
            final Map<QName, List<String>> exp = new HashMap<QName, List<String>>();
            exp.put(QName.valueOf("group"), Arrays.asList("a", "b", "c"));
            assertEquals(exp, f.getGroups("group(a b) group(c)"));
        }
        {
            final Map<QName, List<String>> exp = new HashMap<QName, List<String>>();
            exp.put(QName.valueOf("group2"), Arrays.asList("a"));
            assertEquals(exp, f.getGroups("group1() group2(a)"));
        }
    }
    
    private Attributes attr(final QName name, final String value) {
        final AttributesImpl res = new AttributesImpl();
        XMLUtils.addOrSetAttribute(res, name, value);
        return res;
    }

    @Test
    public void testGetFlagLabel() {
        final Flag flagRed = new Flag("prop", "red", null, null, null, null, null);
        final Flag flagBlue = new Flag("prop", "blue", null, null, null, null, null);
        
        final FilterUtils f = new FilterUtils(false,
                ImmutableMap.<FilterKey, Action>builder()
                        .put(new FilterKey(OS, "amiga"), flagRed)
                        .put(new FilterKey(OS, null), flagBlue)
                        .build(), null, null);
        f.setLogger(new TestUtils.TestLogger());

        assertEquals(
                new HashSet(asList(flagRed, flagBlue)),
                f.getFlags(attr(PROPS, "os(amiga unix windows)"), new QName[][] {{PROPS, OS}}));
        assertEquals(
                new HashSet(asList(flagRed, flagBlue)),
                f.getFlags(attr(PROPS, "os(amiga windows)"), new QName[][] {{PROPS, OS, GUI}}));
        assertEquals(
                new HashSet(asList(flagRed, flagBlue)),
                f.getFlags(attr(PROPS, "gui(amiga windows)"), new QName[][] {{PROPS, OS, GUI}}));
        assertEquals(
                singleton(flagBlue),
                f.getFlags(attr(PROPS, "os(windows)"), new QName[][] {{PROPS, OS}}));
        assertEquals(
                singleton(flagBlue),
                f.getFlags(attr(PROPS, "   os(   windows   )   "), new QName[][] {{PROPS, OS}}));
    }

    @Test
    public void testConflict() {
        final Flag flagRed = new Flag("prop", "red", null, null, null, null, null);
        final Flag flagBlue = new Flag("prop", "blue", null, null, null, null, null);
        final FilterUtils f = new FilterUtils(false,
                ImmutableMap.<FilterKey, Action>builder()
                        .put(new FilterKey(OS, "amiga"), flagRed)
                        .put(new FilterKey(OS, null), flagBlue)
                        .build(), "yellow", "green");
        f.setLogger(new TestUtils.TestLogger());

        final Flag flagYellow = new Flag("prop", "yellow", null, null, null, null, null);
        assertEquals(
                singleton(flagYellow),
                f.getFlags(attr(PROPS, "os(amiga unix windows)"), new QName[][] {{PROPS, OS}}));
        assertEquals(
                singleton(flagYellow),
                f.getFlags(attr(PROPS, "os(amiga windows)"), new QName[][] {{PROPS, OS, GUI}}));
        assertEquals(
                singleton(flagYellow),
                f.getFlags(attr(PROPS, "gui(amiga windows)"), new QName[][] {{PROPS, OS, GUI}}));
        assertEquals(
                singleton(flagBlue),
                f.getFlags(attr(PROPS, "os(windows)"), new QName[][] {{PROPS, OS}}));
        assertEquals(
                singleton(flagBlue),
                f.getFlags(attr(PROPS, "   os(   windows   )   "), new QName[][] {{PROPS, OS}}));
    }

    @Test
    public void getLinkingAttribute_hrefNotEmpty() throws Exception {
        // given
        FilterUtils filterUtils = new FilterUtils(false);
        Attributes attributes = mock(Attributes.class);
        doReturn("link").when(attributes).getValue(ArgumentMatchers.eq(ATTRIBUTE_NAME_HREF));

        // when
        String link = filterUtils.getLinkingAttribute(attributes);

        // then
        assertEquals("link", link);
    }

    @Test
    public void getLinkingAttribute_fallbackToConref_hrefEmptyString() throws Exception {
        // given
        FilterUtils filterUtils = new FilterUtils(false);
        Attributes attributes = mock(Attributes.class);
        doReturn("").when(attributes).getValue(ArgumentMatchers.eq(ATTRIBUTE_NAME_HREF));
        doReturn("conref").when(attributes).getValue(ArgumentMatchers.eq(ATTRIBUTE_NAME_CONREF));

        // when
        String link = filterUtils.getLinkingAttribute(attributes);

        // then
        assertEquals("conref", link);
    }

    @Test
    public void getLinkingAttribute_fallbackToConref_hrefNull() throws Exception {
        // given
        FilterUtils filterUtils = new FilterUtils(false);
        Attributes attributes = mock(Attributes.class);
        doReturn(null).when(attributes).getValue(ArgumentMatchers.eq(ATTRIBUTE_NAME_HREF));
        doReturn("conref").when(attributes).getValue(ArgumentMatchers.eq(ATTRIBUTE_NAME_CONREF));

        // when
        String link = filterUtils.getLinkingAttribute(attributes);

        // then
        assertEquals("conref", link);
    }

    @Test
    public void getLinkingAttribute_hrefNull_conrefNull() throws Exception {
        // given
        FilterUtils filterUtils = new FilterUtils(false);
        Attributes attributes = mock(Attributes.class);
        doReturn(null).when(attributes).getValue(ArgumentMatchers.eq(ATTRIBUTE_NAME_HREF));
        doReturn(null).when(attributes).getValue(ArgumentMatchers.eq(ATTRIBUTE_NAME_CONREF));

        // when
        String link = filterUtils.getLinkingAttribute(attributes);

        // then
        assertNull(link);
    }

    @Test
    public void matchFileName_emptyString() throws Exception {
        // given
        FilterUtils filterUtils = new FilterUtils(false);

        // when
        String fileName = filterUtils.matchFileName("");

        // then
        assertEquals("", fileName);
    }

    @Test
    public void matchFileName_noFragment() throws Exception {
        // given
        FilterUtils filterUtils = new FilterUtils(false);

        // when
        String fileName = filterUtils.matchFileName("topic4.xml");

        // then
        assertEquals("topic4.xml", fileName);
    }

    @Test
    public void matchFileName_withFragment() throws Exception {
        // given
        FilterUtils filterUtils = new FilterUtils(false);

        // when
        String fileName = filterUtils.matchFileName("topic4.xml#bla");

        // then
        assertEquals("topic4.xml", fileName);
    }

    @Test
    public void matchFileName_fragmentOnly() throws Exception {
        // given
        FilterUtils filterUtils = new FilterUtils(false);

        // when
        String fileName = filterUtils.matchFileName("#bla");

        // then
        assertEquals("", fileName);
    }



    @Test
    public void targetsFilteredFile_linkNull() throws Exception {
        // given
        FilterUtils filterUtils = new FilterUtils(false);
        Attributes attributes = mock(Attributes.class);
        doReturn(null).when(attributes).getValue(ArgumentMatchers.eq(ATTRIBUTE_NAME_HREF));
        doReturn(null).when(attributes).getValue(ArgumentMatchers.eq(ATTRIBUTE_NAME_CONREF));

        // when
        boolean filteredFile = filterUtils.targetsFilteredFile(attributes);

        // then
        assertFalse(filteredFile);
    }

    @Test
    public void targetsFilteredFile_linkEmpty() throws Exception {
        // given
        FilterUtils filterUtils = new FilterUtils(false);
        Attributes attributes = mock(Attributes.class);
        doReturn("").when(attributes).getValue(ArgumentMatchers.eq(ATTRIBUTE_NAME_HREF));
        doReturn("").when(attributes).getValue(ArgumentMatchers.eq(ATTRIBUTE_NAME_CONREF));

        // when
        boolean filteredFile = filterUtils.targetsFilteredFile(attributes);

        // then
        assertFalse(filteredFile);
    }

    @Test
    public void targetsFilteredFile_filtered() throws Exception {
        // given
        Job job = new Job();
        Job.FileInfo fileInfo = new Job.FileInfo(new URI("topic4.xml"));
        fileInfo.src = new URI("topic4.xml");
        fileInfo.isFiltered = true;
        job.add(fileInfo);
        FilterUtils filterUtils = new FilterUtils(false);
        Attributes attributes = mock(Attributes.class);
        doReturn("topic4.xml").when(attributes).getValue(ArgumentMatchers.eq(ATTRIBUTE_NAME_HREF));

        // when
        boolean filtered = filterUtils.targetsFilteredFile(attributes);

        // then
        assertTrue(filtered);
    }

    @Test
    public void targetsFilteredFile_notFiltered() throws Exception {
        // given
        Job job = new Job();
        Job.FileInfo fileInfo = new Job.FileInfo(new URI("topic4.xml"));
        fileInfo.src = new URI("topic4.xml");
        fileInfo.isFiltered = false;
        job.add(fileInfo);
        FilterUtils filterUtils = new FilterUtils(false);
        Attributes attributes = mock(Attributes.class);
        doReturn("topic4.xml").when(attributes).getValue(ArgumentMatchers.eq(ATTRIBUTE_NAME_HREF));

        // when
        boolean filtered = filterUtils.targetsFilteredFile(attributes);

        // then
        assertFalse(filtered);
    }

    private Attributes mockAttributesForFilteredKeydef(Path topicPath) {
        Attributes attributes = mock(Attributes.class);
        doReturn("+ map/topicref mapgroup-d/keydef ").when(attributes).getValue("class");
        doReturn(topicPath.toString()).when(attributes).getValue("href");
        doReturn("ktopic4").when(attributes).getValue("keys");
        doReturn("resource-only").when(attributes).getValue("processing-role");
        doReturn("internal").when(attributes).getValue("audience");
        return attributes;
    }

    @Test
    public void needsExclusion_filteredKeydef() throws Exception {
        // given
        Element element = mock(Element.class);
        QName[][] properties = null;

        Path topic = Paths.get(TestUtils.getResourceDir(FilterUtilsTest.class).toString(), "topic4.xml");
        doReturn(topic.toUri().toString()).when(element).getAttribute(ATTRIBUTE_NAME_HREF);
        FilterUtils filterUtils = spy(new FilterUtils(false));
        Job job = new Job();
        Attributes attributes = mockAttributesForFilteredKeydef(topic);
        doReturn(attributes).when(filterUtils).getAttributes(eq(element));
        doReturn(true).when(filterUtils).needExclude(eq(attributes), eq(properties));
        Job.FileInfo fileInfo = new Job.FileInfo(topic.toUri());
        fileInfo.src = topic.toUri();
        Job.instance.add(fileInfo);

        // when
        boolean needsExclusion = filterUtils.needsExclusion(element, properties);

        // then
        assertTrue(needsExclusion);
        assertTrue(Job.instance.isKeydefFiltered("ktopic4"));
        assertFalse(Job.instance.getFileInfo(topic.toUri()).isFiltered);
    }

    @Test
    public void needsExclusion_filteredKeydef_filteredTopic() throws Exception {
        // given
        Element element = mock(Element.class);
        QName[][] properties = null;

        Path topic = Paths.get(TestUtils.getResourceDir(FilterUtilsTest.class).toString(), "topic4filtered.xml");
        doReturn(topic.toUri().toString()).when(element).getAttribute(ATTRIBUTE_NAME_HREF);
        FilterUtils filterUtils = spy(new FilterUtils(false));
        Job job = new Job();
        Attributes attributes = mockAttributesForFilteredKeydef(topic);
        doReturn(attributes).when(filterUtils).getAttributes(eq(element));
        doReturn(true).when(filterUtils).needExclude(any(Attributes.class), eq(properties));
        Job.FileInfo fileInfo = new Job.FileInfo(topic.toUri());
        fileInfo.src = topic.toUri();
        Job.instance.add(fileInfo);

        // when
        boolean needsExclusion = filterUtils.needsExclusion(element, properties);

        // then
        assertTrue(needsExclusion);
        assertTrue(Job.instance.isKeydefFiltered("ktopic4"));
        assertTrue(Job.instance.getFileInfo(topic.toUri()).isFiltered);
    }

}
