/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2010 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.reader;

import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static org.dita.dost.TestUtils.assertXMLEqual;
import static org.junit.jupiter.api.Assertions.*;

import com.google.common.collect.ImmutableList;
import java.io.File;
import java.io.StringReader;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.s9api.DOMDestination;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.serialize.SerializationProperties;
import net.sf.saxon.trans.XPathException;
import org.dita.dost.TestUtils;
import org.dita.dost.TestUtils.CachingLogger;
import org.dita.dost.TestUtils.CachingLogger.Message;
import org.dita.dost.store.StreamStore;
import org.dita.dost.util.Job;
import org.dita.dost.util.KeyDef;
import org.dita.dost.util.KeyScope;
import org.dita.dost.util.XMLUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class TestKeyrefReader {

  private static final File resourceDir = TestUtils.getResourceDir(TestKeyrefReader.class);
  private static final File srcDir = new File(resourceDir, "src");
  private final XMLUtils xmlUtils = new XMLUtils();

  private KeyrefReader keyrefreader;

  @BeforeEach
  public void setUp() {
    keyrefreader = new KeyrefReader();
  }

  @Test
  public void cascadeChildKeys_none() {
    final KeyScope src = keyScope(null, List.of("key"));

    final KeyScope act = keyrefreader.cascadeChildKeys(src);
    final KeyScope exp = keyScope(null, List.of("key"));
    assertEquals(exp, act);
  }

  @Test
  public void cascadeChildKeys_singleDepth() {
    final KeyScope src = keyScope(null, List.of("rootKey"), List.of(keyScope("first", List.of("firstKey"))));

    final KeyScope act = keyrefreader.cascadeChildKeys(src);
    final KeyScope exp = keyScope(
      null,
      asList("rootKey", "first.firstKey"),
      List.of(keyScope("first", List.of("firstKey")))
    );
    assertEquals(exp, act);
  }

  @Test
  public void cascadeChildKeys_secondDepth() {
    final KeyScope src = keyScope(
      null,
      List.of("rootKey"),
      ImmutableList.of(keyScope("first", List.of("firstKey"), List.of(keyScope("second", List.of("secondKey")))))
    );

    final KeyScope act = keyrefreader.cascadeChildKeys(src);
    final KeyScope exp = keyScope(
      null,
      asList("rootKey", "first.firstKey", "first.second.secondKey"),
      List.of(
        keyScope("first", asList("firstKey", "second.secondKey"), List.of(keyScope("second", List.of("secondKey"))))
      )
    );
    assertEquals(exp, act);
  }

  @Test
  public void testKeyrefReader() throws Exception {
    final File filename = new File(srcDir, "keyrefreader.xml");

    //        final Set <String> set = new HashSet<String> ();
    //        set.add("blatview");
    //        set.add("blatfeference");
    //        set.add("blatintro");
    //        set.add("keyword");
    //        set.add("escape");
    //        set.add("top");
    //        set.add("nested");
    keyrefreader.setLogger(new TestUtils.TestLogger());
    keyrefreader.setJob(new Job(srcDir, new StreamStore(srcDir, new XMLUtils())));
    //        keyrefreader.setKeys(set);
    keyrefreader.read(filename.toURI(), readMap(filename));
    final KeyScope act = keyrefreader.getKeyDefinition();

    final Map<String, String> exp = new HashMap<>();
    exp.put(
      "blatfeference",
      "<topicref keys='blatview blatfeference blatintro' href='blatview.dita' navtitle='blatview' locktitle='yes' class='- map/topicref '/>"
    );
    exp.put(
      "blatview",
      "<topicref keys='blatview blatfeference blatintro' href='blatview.dita' navtitle='blatview' locktitle='yes' class='- map/topicref '/>"
    );
    exp.put(
      "blatintro",
      "<topicref keys='blatview blatfeference blatintro' href='blatview.dita' navtitle='blatview' locktitle='yes' class='- map/topicref '/>"
    );
    exp.put(
      "keyword",
      "<topicref keys='keyword' class='- map/topicref '><topicmeta class='- map/topicmeta '><keywords class='- topic/keywords '><keyword class='- topic/keyword '>keyword value</keyword></keywords></topicmeta></topicref>"
    );
    exp.put(
      "escape",
      "<topicref keys='escape' class='- map/topicref ' navtitle='&amp;&lt;&gt;&quot;&apos;'><topicmeta class='- map/topicmeta '><keywords class='- topic/keywords '><keyword class='- topic/keyword '>&amp;&lt;&gt;&quot;&apos;</keyword></keywords></topicmeta></topicref>"
    );
    exp.put(
      "top",
      "<topicref keys='top' class='- map/topicref ' navtitle='top'><topicmeta class='- map/topicmeta '><keywords class='- topic/keywords '><keyword class='- topic/keyword '>top keyword</keyword></keywords></topicmeta><topicref keys='nested' class='- map/topicref ' navtitle='nested'><topicmeta class='- map/topicmeta '><keywords class='- topic/keywords '><keyword class='- topic/keyword '>nested keyword</keyword></keywords></topicmeta></topicref></topicref>"
    );
    exp.put(
      "nested",
      "<topicref keys='nested' class='- map/topicref ' navtitle='nested'><topicmeta class='- map/topicmeta '><keywords class='- topic/keywords '><keyword class='- topic/keyword '>nested keyword</keyword></keywords></topicmeta></topicref>"
    );

    assertEquals(exp.keySet(), act.keySet());
    for (Map.Entry<String, String> e : exp.entrySet()) {
      final Document ev = keyDefToDoc(e.getValue());
      final Document av = toDocument(act.get(e.getKey()).element);
      assertXMLEqual(ev, av);
    }
  }

  @Test
  public void testMergeMap() throws Exception {
    final File filename = new File(srcDir, "merged.xml");

    keyrefreader.read(filename.toURI(), readMap(filename));
    final KeyScope act = keyrefreader.getKeyDefinition();

    final Map<String, String> exp = new HashMap<>();
    exp.put(
      "toner-specs",
      "<keydef class=\"+ map/topicref mapgropup-d/keydef \" keys=\"toner-specs\" href=\"toner-type-a-specs.dita\"/>"
    );
    exp.put(
      "toner-handling",
      "<keydef class=\"+ map/topicref mapgropup-d/keydef \" keys=\"toner-handling\" href=\"toner-type-b-handling.dita\"/>"
    );
    exp.put(
      "toner-disposal",
      "<keydef class=\"+ map/topicref mapgropup-d/keydef \" keys=\"toner-disposal\" href=\"toner-type-c-disposal.dita\"/>"
    );

    assertEquals(exp.keySet(), act.keySet());
    for (Map.Entry<String, String> e : exp.entrySet()) {
      final Document ev = keyDefToDoc(e.getValue());
      final Document av = toDocument(act.get(e.getKey()).element);
      assertXMLEqual(ev, av);
    }
  }

  private static Document keyDefToDoc(final String key) throws Exception {
    final InputSource inputSource = new InputSource(new StringReader(key));
    final DocumentBuilder documentBuilder = XMLUtils.getDocumentBuilder();
    return documentBuilder.parse(inputSource);
  }

  // Oberon Technologies' tests

  @Test
  public void testSimpleKeyscope() {
    final File filename = new File(srcDir, "simpleKeyscope.ditamap");

    keyrefreader.read(filename.toURI(), readMap(filename));
    final KeyScope act = keyrefreader.getKeyDefinition();

    assertEquals(6, act.keySet().size());
    assertEquals("one.dita", act.get("test1").href.toString());
    assertEquals("four.dita", act.get("test2").href.toString());
    assertNull(act.get("test3"));
    assertEquals("two.dita", act.get("scope1.test2").href.toString());
    assertEquals("three.dita", act.get("scope1.test3").href.toString());
    assertEquals("two.dita", act.get("scope2.test2").href.toString());
    assertEquals("three.dita", act.get("scope2.test3").href.toString());

    final KeyScope scope1 = act.getChildScope("scope1");
    assertEquals(7, scope1.keySet().size());
    assertEquals("one.dita", scope1.get("test1").href.toString());
    assertEquals("four.dita", scope1.get("test2").href.toString());
    assertEquals("three.dita", scope1.get("test3").href.toString());
    assertEquals("two.dita", scope1.get("scope1.test2").href.toString());
    assertEquals("three.dita", scope1.get("scope1.test3").href.toString());
    assertEquals("two.dita", scope1.get("scope2.test2").href.toString());
    assertEquals("three.dita", scope1.get("scope2.test3").href.toString());

    final KeyScope scope2 = act.getChildScope("scope2");
    assertEquals(7, scope2.keySet().size());
    assertEquals("one.dita", scope1.get("test1").href.toString());
    assertEquals("four.dita", scope2.get("test2").href.toString());
    assertEquals("three.dita", scope2.get("test3").href.toString());
    assertEquals("two.dita", scope2.get("scope1.test2").href.toString());
    assertEquals("three.dita", scope2.get("scope1.test3").href.toString());
    assertEquals("two.dita", scope2.get("scope2.test2").href.toString());
    assertEquals("three.dita", scope2.get("scope2.test3").href.toString());
    //        KeySpace keyspace = loadKeySpace("simpleKeyscope/simpleKeyscope.ditamap");
    //        testKeyAttr(keyspace, "test1", "id", "one");
    //        testKeyAttr(keyspace, "test2", "id", "four");
    //        testKeyAttr(keyspace, "test3", "id", null);
    //        testKeyAttr(keyspace, "scope1.test2", "id", "two");
    //        testKeyAttr(keyspace, "scope1.test3", "id", "three");
    //        testKeyAttr(keyspace, "scope2.test2", "id", "two");
    //        testKeyAttr(keyspace, "scope2.test3", "id", "three");
    //
    //        KeySpace scope1 = keyspace.findChildScope("scope1");
    //        assertNotNull(scope1);
    //        testKeyAttr(scope1, "test1", "id", "one");
    //        testKeyAttr(scope1, "test2", "id", "four");
    //        testKeyAttr(scope1, "test3", "id", "three");
    //        testKeyAttr(scope1, "scope1.test2", "id", "two");
    //        testKeyAttr(scope1, "scope1.test3", "id", "three");
    //        testKeyAttr(scope1, "scope2.test2", "id", "two");
    //        testKeyAttr(scope1, "scope2.test3", "id", "three");
    //
    //        KeySpace scope2 = keyspace.findChildScope("scope2");
    //        assertNotNull(scope2);
    //        testKeyAttr(scope2, "test1", "id", "one");
    //        testKeyAttr(scope2, "test2", "id", "four");
    //        testKeyAttr(scope2, "test3", "id", "three");
    //        testKeyAttr(scope2, "scope1.test2", "id", "two");
    //        testKeyAttr(scope2, "scope1.test3", "id", "three");
    //        testKeyAttr(scope2, "scope2.test2", "id", "two");
    //        testKeyAttr(scope2, "scope2.test3", "id", "three");
  }

  @Test
  public void testQualifiedKeyOverride() {
    final File filename = new File(srcDir, "qualifiedKeyOverride.ditamap");

    keyrefreader.read(filename.toURI(), readMap(filename));
    final KeyScope act = keyrefreader.getKeyDefinition();

    assertEquals(6, act.keySet().size());
    assertEquals("one.dita", act.get("test1").href.toString());
    assertEquals("four.dita", act.get("test2").href.toString());
    assertEquals("one.dita", act.get("scope1.test1").href.toString());
    assertEquals("four.dita", act.get("scope1.test2").href.toString());
    assertEquals("two.dita", act.get("scope2.test1").href.toString());
    assertEquals("three.dita", act.get("scope2.test2").href.toString());

    final KeyScope scope1 = act.getChildScope("scope1");
    assertEquals(6, scope1.keySet().size());
    assertEquals("one.dita", scope1.get("test1").href.toString());
    assertEquals("four.dita", scope1.get("test2").href.toString());
    assertEquals("one.dita", scope1.get("scope1.test1").href.toString());
    // FIXME: Should be three.dita
    assertEquals("four.dita", scope1.get("scope1.test2").href.toString());
    assertEquals("two.dita", scope1.get("scope2.test1").href.toString());
    assertEquals("three.dita", scope1.get("scope2.test2").href.toString());
    //        final KeyScope scope2 = act.getChildScope("scope2");
    //        assertEquals(6, scope2.keySet().size());
    //        assertEquals("one.dita", scope2.get("test1").href.toString());
    //        assertEquals("four.dita", scope2.get("test2").href.toString());
    //        assertEquals("two.dita", scope2.get("scope1.test1").href.toString());
    //        assertEquals("three.dita", scope2.get("scope1.test2").href.toString());
    //        assertEquals("two.dita", scope2.get("scope2.test1").href.toString());
    //        assertEquals("three.dita", scope2.get("scope2.test2").href.toString());

    //        KeySpace keyspace = loadKeySpace("qualifiedKeyOverride/map.ditamap");
    //        testKeyAttr(keyspace, "test1", "id", "one");
    //        testKeyAttr(keyspace, "test2", "id", "four");
    //        testKeyAttr(keyspace, "scope1.test1", "id", "one");
    //        testKeyAttr(keyspace, "scope1.test2", "id", "three");
    //        testKeyAttr(keyspace, "scope2.test1", "id", "two");
    //        testKeyAttr(keyspace, "scope2.test2", "id", "three");
    //
    //        KeySpace scope1 = keyspace.findChildScope("scope1");
    //        assertNotNull(scope1);
    //        testKeyAttr(scope1, "test1", "id", "one");
    //        testKeyAttr(scope1, "test2", "id", "four");
    //        testKeyAttr(scope1, "scope1.test1", "id", "one");
    //        testKeyAttr(scope1, "scope1.test2", "id", "three");
    //        testKeyAttr(scope1, "scope2.test1", "id", "two");
    //        testKeyAttr(scope1, "scope2.test2", "id", "three");
  }

  @Test
  public void testMapWithKeyscopes() {
    final File filename = new File(srcDir, "map-with-keyscopes.ditamap");

    keyrefreader.read(filename.toURI(), readMap(filename));
    final KeyScope root = keyrefreader.getKeyDefinition();

    assertEquals(6, root.keySet().size());
    Assertions.assertNull(root.get("dita-europe.conferenceName").href);
    assertEquals(
      "http://www.ihg.com/holidayinn/hotels/us/en/munich/muchb/hoteldetail",
      root.get("dita-europe.hotel").href.toString()
    );
    assertEquals("images/holidayInn.jpg", root.get("dita-europe.hotelImage").href.toString());
    Assertions.assertNull(root.get("telematics.conferenceName").href);
    assertEquals("http://www.dolcemunich.com/", root.get("telematics.hotel").href.toString());
    assertEquals("images/dolce.jpg", root.get("telematics.hotelImage").href.toString());

    final KeyScope first = root.getChildScope("dita-europe");
    assertEquals(9, first.keySet().size());
    Assertions.assertNull(first.get("conferenceName").href);
    assertEquals(
      "http://www.ihg.com/holidayinn/hotels/us/en/munich/muchb/hoteldetail",
      first.get("hotel").href.toString()
    );
    assertEquals("images/holidayInn.jpg", first.get("hotelImage").href.toString());
    Assertions.assertNull(first.get("dita-europe.conferenceName").href);
    assertEquals(
      "http://www.ihg.com/holidayinn/hotels/us/en/munich/muchb/hoteldetail",
      first.get("dita-europe.hotel").href.toString()
    );
    assertEquals("images/holidayInn.jpg", first.get("dita-europe.hotelImage").href.toString());
    Assertions.assertNull(first.get("telematics.conferenceName").href);
    assertEquals("http://www.dolcemunich.com/", first.get("telematics.hotel").href.toString());
    assertEquals("images/dolce.jpg", first.get("telematics.hotelImage").href.toString());

    final KeyScope second = root.getChildScope("telematics");
    assertEquals(9, second.keySet().size());
    Assertions.assertNull(second.get("conferenceName").href);
    assertEquals("http://www.dolcemunich.com/", second.get("hotel").href.toString());
    assertEquals("images/dolce.jpg", second.get("hotelImage").href.toString());
    Assertions.assertNull(second.get("dita-europe.conferenceName").href);
    assertEquals(
      "http://www.ihg.com/holidayinn/hotels/us/en/munich/muchb/hoteldetail",
      second.get("dita-europe.hotel").href.toString()
    );
    assertEquals("images/holidayInn.jpg", second.get("dita-europe.hotelImage").href.toString());
    Assertions.assertNull(second.get("telematics.conferenceName").href);
    assertEquals("http://www.dolcemunich.com/", second.get("telematics.hotel").href.toString());
    assertEquals("images/dolce.jpg", second.get("telematics.hotelImage").href.toString());
  }

  @Test
  public void testMaprefKeyscope() {
    final File filename = new File(srcDir, "maprefKeyscope.ditamap");

    keyrefreader.read(filename.toURI(), readMap(filename));
    final KeyScope act = keyrefreader.getKeyDefinition();

    assertEquals(9, act.keySet().size());
    assertEquals("one.dita", act.get("scope1.key1").href.toString());
    assertEquals("two.dita", act.get("scope1.key2").href.toString());
    assertEquals("nested-one.dita", act.get("scope1.mapref.key1").href.toString());
    assertEquals("nested-three.dita", act.get("scope1.mapref.key3").href.toString());
    assertEquals("nested-one.dita", act.get("scope1.map.key1").href.toString());
    assertEquals("nested-three.dita", act.get("scope1.map.key3").href.toString());

    final KeyScope scope1 = act.getChildScope("scope1");
    assertEquals(15, scope1.keySet().size());
    assertEquals("one.dita", scope1.get("key1").href.toString());
    assertEquals("two.dita", scope1.get("key2").href.toString());
    assertEquals("one.dita", scope1.get("scope1.key1").href.toString());
    assertEquals("two.dita", scope1.get("scope1.key2").href.toString());
    assertEquals("nested-one.dita", scope1.get("scope1.mapref.key1").href.toString());
    assertEquals("nested-three.dita", scope1.get("scope1.mapref.key3").href.toString());
    assertEquals("nested-one.dita", scope1.get("scope1.map.key1").href.toString());
    assertEquals("nested-three.dita", scope1.get("scope1.map.key3").href.toString());

    final KeyScope scope3 = scope1.getChildScope("mapref");
    assertEquals(16, scope3.keySet().size());
    assertEquals("one.dita", scope3.get("key1").href.toString());
    assertEquals("two.dita", scope3.get("key2").href.toString());
    assertEquals("nested-three.dita", scope3.get("key3").href.toString());
    assertEquals("one.dita", scope3.get("scope1.key1").href.toString());
    assertEquals("two.dita", scope3.get("scope1.key2").href.toString());
    assertEquals("nested-one.dita", scope3.get("scope1.mapref.key1").href.toString());
    assertEquals("nested-three.dita", scope3.get("scope1.mapref.key3").href.toString());
    assertEquals("nested-one.dita", scope3.get("scope1.map.key1").href.toString());
    assertEquals("nested-three.dita", scope3.get("scope1.map.key3").href.toString());

    final KeyScope scope4 = scope1.getChildScope("map");
    assertEquals(16, scope4.keySet().size());
    assertEquals("one.dita", scope4.get("key1").href.toString());
    assertEquals("two.dita", scope4.get("key2").href.toString());
    assertEquals("nested-three.dita", scope4.get("key3").href.toString());
    assertEquals("one.dita", scope4.get("scope1.key1").href.toString());
    assertEquals("two.dita", scope4.get("scope1.key2").href.toString());
    assertEquals("nested-one.dita", scope4.get("scope1.mapref.key1").href.toString());
    assertEquals("nested-three.dita", scope4.get("scope1.mapref.key3").href.toString());
    assertEquals("nested-one.dita", scope4.get("scope1.map.key1").href.toString());
    assertEquals("nested-three.dita", scope4.get("scope1.map.key3").href.toString());

    final KeyScope scope2 = act.getChildScope("scope2");
    assertEquals(12, scope2.keySet().size());
    assertEquals("one.dita", scope2.get("key1").href.toString());
    assertEquals("two.dita", scope2.get("key2").href.toString());
    assertEquals("nested-three.dita", scope2.get("key3").href.toString());
    assertEquals("one.dita", scope2.get("scope1.key1").href.toString());
    assertEquals("two.dita", scope2.get("scope1.key2").href.toString());
    assertEquals("nested-one.dita", scope2.get("scope1.mapref.key1").href.toString());
    assertEquals("nested-three.dita", scope2.get("scope1.mapref.key3").href.toString());
    assertEquals("nested-one.dita", scope2.get("scope1.map.key1").href.toString());
    assertEquals("nested-three.dita", scope2.get("scope1.map.key3").href.toString());
  }

  // DITA 1.3 specification examples

  @Test
  public void testExample7() {
    final File filename = new File(srcDir, "example7.ditamap");

    keyrefreader.read(filename.toURI(), readMap(filename));
    final KeyScope root = keyrefreader.getKeyDefinition();

    assertEquals(2, root.keySet().size());
    assertNull(root.get("key-1"));
    assertEquals("topic-1.dita", root.get("scope-1.key-1").href.toString());
    assertEquals("topic-2.dita", root.get("scope-2.key-1").href.toString());

    final KeyScope scope1 = root.getChildScope("scope-1");
    assertEquals(3, scope1.keySet().size());
    assertEquals("topic-1.dita", scope1.get("key-1").href.toString());
    assertEquals("topic-1.dita", scope1.get("scope-1.key-1").href.toString());
    assertEquals("topic-2.dita", scope1.get("scope-2.key-1").href.toString());

    final KeyScope scope2 = root.getChildScope("scope-2");
    assertEquals(3, scope2.keySet().size());
    assertEquals("topic-2.dita", scope2.get("key-1").href.toString());
    assertEquals("topic-1.dita", scope2.get("scope-1.key-1").href.toString());
    assertEquals("topic-2.dita", scope2.get("scope-2.key-1").href.toString());
  }

  @Test
  public void testExample8() {
    final File filename = new File(srcDir, "example8.ditamap");

    keyrefreader.read(filename.toURI(), readMap(filename));
    final KeyScope root = keyrefreader.getKeyDefinition();

    //        log(root, "");

    final KeyScope a2 = root.getChildScope("A").getChildScope("A-2");
    assertEquals(12, a2.keySet().size());
    assertEquals("a1", a2.get("a").element.attribute("id"));
    assertEquals("d", a2.get("d").element.attribute("id"));
    assertEquals("d", a2.get("A-2.d").element.attribute("id"));
    assertNull(a2.get("c"));
    assertEquals("c", a2.get("A-1.c").element.attribute("id"));
    assertEquals("c", a2.get("A.A-1.c").element.attribute("id"));

    final KeyScope b = root.getChildScope("B");
    assertEquals(11, b.keySet().size());
    assertEquals("e", b.get("e").element.attribute("id"));
    assertEquals("a1", b.get("a").element.attribute("id"));
    assertEquals("a2", b.get("B.a").element.attribute("id"));
    assertNull(b.get("g"));
    assertEquals("g", b.get("B-2.g").element.attribute("id"));
  }

  @Test
  public void testKeysAndScope() {
    final File filename = new File(srcDir, "keysAndScope.ditamap");

    keyrefreader.read(filename.toURI(), readMap(filename));
    final KeyScope root = keyrefreader.getKeyDefinition();

    assertEquals(2, root.keySet().size());

    final KeyScope a2 = root.getChildScope("potatoes");
    assertEquals(3, a2.keySet().size());
    assertEquals("potatoes.dita", a2.get("potatoes.vegetable").href.toString());
    assertEquals("potatoes.dita", a2.get("vegetable").href.toString());
    assertEquals("carrots.dita", a2.get("carrots.vegetable").href.toString());

    final KeyScope b = root.getChildScope("carrots");
    assertEquals(3, b.keySet().size());
    assertEquals("potatoes.dita", b.get("potatoes.vegetable").href.toString());
    assertEquals("carrots.dita", b.get("vegetable").href.toString());
    assertEquals("carrots.dita", b.get("carrots.vegetable").href.toString());
  }

  @Test
  public void testMultipleValues() {
    final File filename = new File(srcDir, "multipleValues.ditamap");

    keyrefreader.read(filename.toURI(), readMap(filename));
    final KeyScope root = keyrefreader.getKeyDefinition();

    assertEquals(4, root.keySet().size());

    final KeyScope c1 = root.getChildScope("potatoes");
    assertEquals(5, c1.keySet().size());

    final KeyScope c2 = root.getChildScope("patate");
    assertEquals(5, c2.keySet().size());

    final KeyScope c3 = root.getChildScope("carrot");
    assertEquals(5, c3.keySet().size());

    final KeyScope c4 = root.getChildScope("morcov");
    assertEquals(5, c4.keySet().size());
  }

  @Test
  public void testSingleCircular() {
    final File filename = new File(srcDir, "circularSingle.ditamap");

    final CachingLogger logger = new CachingLogger();
    keyrefreader.setLogger(logger);

    keyrefreader.read(filename.toURI(), readMap(filename));

    assertEquals(1, logger.getMessages().size());
    assertEquals("[DOTJ069E][ERROR] Circular key definition same -> same.", logger.getMessages().get(0).message);
  }

  @Test
  public void testCircular() {
    final File filename = new File(srcDir, "circular.ditamap");

    final CachingLogger logger = new CachingLogger();
    keyrefreader.setLogger(logger);

    keyrefreader.read(filename.toURI(), readMap(filename));

    assertEquals(3, logger.getMessages().size());
    final Set<String> act = new HashSet<>(3);
    for (final Message msg : logger.getMessages()) {
      act.add(msg.message);
    }
    assertEquals(
      new HashSet<>(
        asList(
          "[DOTJ069E][ERROR] Circular key definition first -> second -> third -> first.",
          "[DOTJ069E][ERROR] Circular key definition second -> third -> first -> second.",
          "[DOTJ069E][ERROR] Circular key definition third -> first -> second -> third."
        )
      ),
      act
    );
  }

  @Test
  public void testRootScope() {
    final File filename = new File(srcDir, "rootScope.ditamap");

    keyrefreader.read(filename.toURI(), readMap(filename));
    final KeyScope root = keyrefreader.getKeyDefinition();

    assertEquals(2, root.keySet().size());
    assertEquals("one.dita", root.get("root.test1").href.toString());
    assertEquals("two.dita", root.get("root.nested.test2").href.toString());

    final KeyScope r = root.getChildScope("root");
    assertEquals(4, r.keySet().size());
    assertEquals("one.dita", r.get("test1").href.toString());
    assertEquals("one.dita", r.get("root.test1").href.toString());
    assertEquals("two.dita", r.get("nested.test2").href.toString());
    assertEquals("two.dita", r.get("root.nested.test2").href.toString());

    final KeyScope n = r.getChildScope("nested");
    assertEquals(5, n.keySet().size());
    assertEquals("two.dita", n.get("test2").href.toString());
    assertEquals("one.dita", n.get("test1").href.toString());
    assertEquals("one.dita", n.get("root.test1").href.toString());
    assertEquals("two.dita", n.get("nested.test2").href.toString());
    assertEquals("two.dita", n.get("root.nested.test2").href.toString());
  }

  @Test
  public void testDuplicateScopeNames() {
    final File filename = new File(srcDir, "duplicate.ditamap");

    keyrefreader.read(filename.toURI(), readMap(filename));
    final KeyScope root = keyrefreader.getKeyDefinition();

    assertEquals(1, root.keySet().size());

    final KeyScope r = root.childScopes().get(0);
    assertEquals("A", r.name());
    assertEquals(2, r.keySet().size());
    assertEquals("def1", r.get("a").element.attribute("id"));
    assertEquals("def1", r.get("A.a").element.attribute("id"));

    final KeyScope d = root.childScopes().get(1);
    assertEquals("A", d.name());
    assertEquals(2, d.keySet().size());
    assertEquals("def2", d.get("a").element.attribute("id"));
    assertEquals("def1", d.get("A.a").element.attribute("id"));
  }

  @Test
  public void testCopyto() {
    final File filename = new File(srcDir, "copyto.ditamap");

    keyrefreader.read(filename.toURI(), readMap(filename));
    final KeyScope root = keyrefreader.getKeyDefinition();

    assertEquals("topic-original.dita", root.get("original").href.toString());
    assertEquals("topic-copy.dita", root.get("copy").href.toString());
  }

  @Test
  public void testIntermediaryKeyRefCopyMetadata() {
    final File filename = new File(srcDir, "intermediaryKeyref.ditamap");
    keyrefreader.read(filename.toURI(), readMap(filename));
    final KeyScope root = keyrefreader.getKeyDefinition();
    KeyDef keyDef = root.keyDefinition().get("b");
    assertTrue(keyDef.element.toString().contains("Product A"));
  }

  private void log(final KeyScope scope, final String indent) {
    System.err.println(indent + "scope: " + scope.name());
    for (final Map.Entry<String, KeyDef> key : scope.keyDefinition().entrySet()) {
      System.err.println(indent + " * " + key.getKey() + "=" + key.getValue().href);
    }
    for (final KeyScope child : scope.childScopes()) {
      log(child, indent + "  ");
    }
  }

  private XdmNode readMap(final File file) {
    try {
      final StreamSource source = new StreamSource(file);
      source.setSystemId(file.toURI().toString());
      return xmlUtils.getProcessor().newDocumentBuilder().build(source);
    } catch (SaxonApiException e) {
      throw new RuntimeException(e);
    }
  }

  private KeyDef keyDef(String key) {
    return new KeyDef(key, URI.create("key.dita"), "local", "dita", null, null);
  }

  private KeyScope keyScope(String name, List<String> keydefs, List<KeyScope> keyscopes) {
    return new KeyScope(
      null,
      name,
      unmodifiableMap(keydefs.stream().collect(Collectors.toMap(key -> key, this::keyDef))),
      unmodifiableList(keyscopes)
    );
  }

  private KeyScope keyScope(String name, List<String> keydefs) {
    return keyScope(name, keydefs, emptyList());
  }

  private Document toDocument(final XdmNode node) {
    try {
      final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
      documentBuilderFactory.setNamespaceAware(true);
      final Document document = documentBuilderFactory.newDocumentBuilder().newDocument();
      final DOMDestination destination = new DOMDestination(document);
      final Receiver receiver = destination.getReceiver(
        xmlUtils.getProcessor().getUnderlyingConfiguration().makePipelineConfiguration(),
        new SerializationProperties()
      );
      receiver.open();
      receiver.append(node.getUnderlyingNode());
      receiver.close();
      return document;
    } catch (XPathException | ParserConfigurationException e) {
      throw new RuntimeException(e);
    }
  }
}
