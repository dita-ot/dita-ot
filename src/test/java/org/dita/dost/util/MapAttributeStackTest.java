package org.dita.dost.util;

import static javax.xml.XMLConstants.XML_NS_URI;
import static org.dita.dost.util.Constants.ATTRIBUTE_NAME_SPECIALIZATIONS;
import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Stream;
import org.dita.dost.util.XMLUtils.AttributesBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class MapAttributeStackTest {

  private MapAttributeStack stack;

  @BeforeEach
  void setUp() {
    stack = new MapAttributeStack();
  }

  @Test
  void specializations() {
    stack.push(
      new AttributesBuilder()
        .add(
          ATTRIBUTE_NAME_SPECIALIZATIONS,
          "@props/audience @props/deliveryTarget @props/otherprops @props/platform @props/product"
        )
        .add("audience", "audience 1")
        .build()
    );

    stack.push(
      new AttributesBuilder()
        .add(
          ATTRIBUTE_NAME_SPECIALIZATIONS,
          "@props/audience @props/deliveryTarget @props/otherprops @props/platform @props/product @props/hardware-platform"
        )
        .add("audience", "audience 2")
        .add("hardware-platform", "hardware-platform 1")
        .build()
    );

    assertEquals("audience 2", stack.peek("audience"));
    assertEquals("hardware-platform 1", stack.peek("hardware-platform"));

    stack.pop();

    assertEquals("audience 1", stack.peek("audience"));
    assertEquals(null, stack.peek("hardware-platform"));
  }

  @Test
  void defaultAttributes() {
    stack.push(
      new AttributesBuilder()
        .add("audience", "audience value")
        .add("platform", "platform value")
        .add("product", "product value")
        .add("otherprops", "otherprops value")
        .add("rev", "rev value")
        .add("props", "props value")
        .add("linking", "linking value")
        .add("toc", "toc value")
        .add("print", "print value")
        .add("search", "search value")
        .add("format", "format value")
        .add("scope", "scope value")
        .add("type", "type value")
        .add("dir", "dir value")
        .add("translate", "translate value")
        .add("processing-role", "processing-role value")
        .add("cascade", "cascade value")
        .add(XML_NS_URI, "lang", "xml:lang value")
        .add("deliveryTarget", "deliveryTarget value")
        .add("subjectrefs", "subjectrefs value")
        .build()
    );

    assertEquals("audience value", stack.peek("audience"));
    assertEquals("platform value", stack.peek("platform"));
    assertEquals("product value", stack.peek("product"));
    assertEquals("otherprops value", stack.peek("otherprops"));
    assertEquals("props value", stack.peek("props"));
    assertEquals("deliveryTarget value", stack.peek("deliveryTarget"));
  }

  @Test
  void basicCascading() {
    stack.push(new AttributesBuilder().add("props", "parent_foo").add("audience", "parent_bar").build());

    stack.push(new AttributesBuilder().add("props", "child_foo").build());

    assertEquals("child_foo", stack.peek("props"));
    assertEquals("parent_bar", stack.peek("audience"));
    assertNull(stack.peek("platform"));
  }

  @Test
  void multipleLevelCascading() {
    stack.push(new AttributesBuilder().add("props", "level1_foo").add("audience", "level1_bar").build());

    stack.push(new AttributesBuilder().add("platform", "level2_baz").build());

    stack.push(new AttributesBuilder().add("props", "level3_foo").build());

    assertEquals("level3_foo", stack.peek("props"));
    assertEquals("level1_bar", stack.peek("audience"));
    assertEquals("level2_baz", stack.peek("platform"));
  }

  @Test
  void cascadingStopsAtFirstValue() {
    stack.push(new AttributesBuilder().add("props", "level1_foo").build());

    stack.push(new AttributesBuilder().add("props", "level2_foo").build());

    stack.push(new AttributesBuilder().add("audience", "level3_bar").build());

    assertEquals("level2_foo", stack.peek("props"));
    assertEquals("level3_bar", stack.peek("audience"));
  }

  @Test
  void firstPushWithMissingAttributes() {
    stack.push(new AttributesBuilder().add("props", "only_foo").build());

    assertEquals("only_foo", stack.peek("props"));
    assertNull(stack.peek("audience"));
    assertNull(stack.peek("platform"));
  }

  @Test
  void emptyPushCascadesAll() {
    stack.push(
      new AttributesBuilder()
        .add("props", "parent_foo")
        .add("audience", "parent_bar")
        .add("platform", "parent_baz")
        .build()
    );
    stack.push(new AttributesBuilder().build());

    assertEquals("parent_foo", stack.peek("props"));
    assertEquals("parent_bar", stack.peek("audience"));
    assertEquals("parent_baz", stack.peek("platform"));
  }

  @Test
  void partialOverrideWithCascading() {
    stack.push(
      new AttributesBuilder()
        .add("props", "parent_foo")
        .add("audience", "parent_bar")
        .add("platform", "parent_baz")
        .build()
    );
    stack.push(new AttributesBuilder().add("audience", "child_bar").build());

    assertEquals("parent_foo", stack.peek("props"));
    assertEquals("child_bar", stack.peek("audience"));
    assertEquals("parent_baz", stack.peek("platform"));
  }

  @Test
  void popRestoresOriginalValues() {
    stack.push(new AttributesBuilder().add("props", "parent_foo").add("audience", "parent_bar").build());
    stack.push(new AttributesBuilder().add("props", "child_foo").build());

    assertEquals("child_foo", stack.peek("props"));
    assertEquals("parent_bar", stack.peek("audience"));

    stack.pop();
    assertEquals("parent_foo", stack.peek("props"));
    assertEquals("parent_bar", stack.peek("audience"));
  }

  @Test
  void deepCascadingChain() {
    stack.push(
      new AttributesBuilder().add("props", "L1_foo").add("audience", "L1_bar").add("platform", "L1_baz").build()
    );

    stack.push(new AttributesBuilder().add("props", "L2_foo").build());

    stack.push(new AttributesBuilder().add("audience", "L3_bar").build());

    stack.push(new AttributesBuilder().add("platform", "L4_baz").build());

    stack.push(new AttributesBuilder().build());

    assertEquals("L2_foo", stack.peek("props"));
    assertEquals("L3_bar", stack.peek("audience"));
    assertEquals("L4_baz", stack.peek("platform"));
  }

  @Test
  void cascadingWithSingleAttribute() {
    stack.push(new AttributesBuilder().add("props", "first").build());

    stack.push(new AttributesBuilder().build());

    assertEquals("first", stack.peek("props"));
  }

  @Test
  void complexInheritanceScenario() {
    stack.push(
      new AttributesBuilder()
        .add("props", "root_style")
        .add("audience", "root_color")
        .add("platform", "root_font")
        .build()
    );

    stack.push(new AttributesBuilder().add("props", "section_style").build());

    stack.push(new AttributesBuilder().add("audience", "subsection_color").build());

    stack.push(new AttributesBuilder().add("platform", "paragraph_font").build());

    assertEquals("section_style", stack.peek("props"));
    assertEquals("subsection_color", stack.peek("audience"));
    assertEquals("paragraph_font", stack.peek("platform"));

    stack.pop();
    assertEquals("section_style", stack.peek("props"));
    assertEquals("subsection_color", stack.peek("audience"));
    assertEquals("root_font", stack.peek("platform"));

    stack.pop();
    assertEquals("section_style", stack.peek("props"));
    assertEquals("root_color", stack.peek("audience"));
    assertEquals("root_font", stack.peek("platform"));

    stack.pop();
    assertEquals("root_style", stack.peek("props"));
    assertEquals("root_color", stack.peek("audience"));
    assertEquals("root_font", stack.peek("platform"));
  }

  @Test
  void errorConditionsWithCascading() {
    stack.push(new AttributesBuilder().add("props", "value").build());

    assertNull(stack.peek("unknown"));

    var emptyStack = new MapAttributeStack();
    assertThrows(IllegalStateException.class, () -> emptyStack.peek("props"));
  }

  @Test
  void cascadingWithThreeAttributes() {
    stack.push(new AttributesBuilder().add("audience", "1st").add("platform", "2nd").add("product", "3rd").build());
    stack.push(new AttributesBuilder().add("audience", "new_1st").build());

    assertEquals("new_1st", stack.peek("audience"));
    assertEquals("2nd", stack.peek("platform"));
    assertEquals("3rd", stack.peek("product"));
  }

  @Test
  void sixteenLevelsDeepWithCascading() {
    for (int i = 1; i <= 16; i++) {
      if (i % 3 == 1) {
        stack.push(new AttributesBuilder().add("props", "level" + i).build());
      } else {
        stack.push(new AttributesBuilder().build());
      }
    }

    assertEquals("level16", stack.peek("props"));

    for (int i = 15; i >= 1; i--) {
      stack.pop();
      if (i % 3 == 1) {
        assertEquals("level" + i, stack.peek("props"));
      } else {
        int expectedLevel = ((i - 1) / 3) * 3 + 1;
        if (expectedLevel >= 1) {
          assertEquals("level" + expectedLevel, stack.peek("props"));
        }
      }
    }
  }

  @Test
  void clear() {
    stack.push(new AttributesBuilder().add("props", "value").build());

    stack.clear();
    assertThrows(IllegalStateException.class, () -> stack.peek("props"));
  }

  static Stream<Arguments> testParseSpecializationsArgs() {
    return Stream.of(
      Arguments.of("@props/foo", new String[] { "props", "foo" }),
      Arguments.of(" @props/foo ", new String[] { "props", "foo" }),
      Arguments.of("@props/foo @props/bar", new String[] { "props", "foo", "bar" }),
      Arguments.of("@props/foo @base/bar", new String[] { "props", "foo" }),
      Arguments.of("@base/bar", new String[] { "props" })
    );
  }

  @ParameterizedTest
  @MethodSource("testParseSpecializationsArgs")
  void testParseSpecializations(String src, String[] exp) {
    assertArrayEquals(exp, MapAttributeStack.parseSpecializations(src));
  }

  @ParameterizedTest
  @CsvSource(
    {
      "foo, bar, foo bar",
      "foo bar, baz, foo bar baz",
      "foo, bar baz, foo bar baz",
      "foo, foo, foo",
      "foo bar, foo, foo bar",
      "foo bar, bar, foo bar",
      "foo bar, foo bar, foo bar",
    }
  )
  void testMerge(String prev, String curr, String exp) {
    assertEquals(exp, MapAttributeStack.merge(prev, curr));
  }
}
