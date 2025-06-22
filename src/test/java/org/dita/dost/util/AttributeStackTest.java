package org.dita.dost.util;

import static javax.xml.XMLConstants.XML_NS_URI;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.Constants.ATTRIBUTE_NAME_CASCADE;
import static org.dita.dost.util.Constants.ATTRIBUTE_NAME_DIR;
import static org.dita.dost.util.Constants.ATTRIBUTE_NAME_FORMAT;
import static org.dita.dost.util.Constants.ATTRIBUTE_NAME_LANG;
import static org.dita.dost.util.Constants.ATTRIBUTE_NAME_LINKING;
import static org.dita.dost.util.Constants.ATTRIBUTE_NAME_OTHERPROPS;
import static org.dita.dost.util.Constants.ATTRIBUTE_NAME_PRINT;
import static org.dita.dost.util.Constants.ATTRIBUTE_NAME_PROCESSING_ROLE;
import static org.dita.dost.util.Constants.ATTRIBUTE_NAME_PROPS;
import static org.dita.dost.util.Constants.ATTRIBUTE_NAME_REV;
import static org.dita.dost.util.Constants.ATTRIBUTE_NAME_SCOPE;
import static org.dita.dost.util.Constants.ATTRIBUTE_NAME_SEARCH;
import static org.dita.dost.util.Constants.ATTRIBUTE_NAME_TOC;
import static org.dita.dost.util.Constants.ATTRIBUTE_NAME_TRANSLATE;
import static org.dita.dost.util.Constants.ATTRIBUTE_NAME_TYPE;
import static org.junit.jupiter.api.Assertions.*;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import org.dita.dost.util.XMLUtils.AttributesBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AttributeStackTest {

  private AttributeStack stack;

  @BeforeEach
  void setUp() {
    stack = new AttributeStack("foo", "bar", "baz");
  }

  @Test
  void defaultAttributes() {
    stack = new AttributeStack();

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
    assertEquals("rev value", stack.peek("rev"));
    assertEquals("props value", stack.peek("props"));
    assertEquals("linking value", stack.peek("linking"));
    assertEquals("toc value", stack.peek("toc"));
    assertEquals("print value", stack.peek("print"));
    assertEquals("search value", stack.peek("search"));
    assertEquals("format value", stack.peek("format"));
    assertEquals("scope value", stack.peek("scope"));
    assertEquals("type value", stack.peek("type"));
    assertEquals("dir value", stack.peek("dir"));
    assertEquals("translate value", stack.peek("translate"));
    assertEquals("processing-role value", stack.peek("processing-role"));
    assertEquals("cascade value", stack.peek("cascade"));
    assertEquals("xml:lang value", stack.peek(XML_NS_URI, "lang"));
    assertEquals("deliveryTarget value", stack.peek("deliveryTarget"));
    assertEquals("subjectrefs value", stack.peek("subjectrefs"));
  }

  @Test
  void basicCascading() {
    stack.push(new AttributesBuilder().add("foo", "parent_foo").add("bar", "parent_bar").build());

    stack.push(new AttributesBuilder().add("foo", "child_foo").build());

    assertEquals("child_foo", stack.peek("foo"));
    assertEquals("parent_bar", stack.peek("bar"));
    assertNull(stack.peek("baz"));
  }

  @Test
  void basicCascading_QName() {
    stack =
      new AttributeStack(
        QName.valueOf("foo"),
        QName.valueOf("{https://example.com}foo"),
        QName.valueOf("bar"),
        QName.valueOf("{https://example.com}bar"),
        QName.valueOf("baz"),
        QName.valueOf("{https://example.com}baz")
      );

    stack.push(
      new AttributesBuilder()
        .add("foo", "parent_foo")
        .add("https://example.com", "foo", "parent_example_foo")
        .add("bar", "parent_bar")
        .add("https://example.com", "bar", "parent_example_bar")
        .build()
    );

    stack.push(
      new AttributesBuilder().add("foo", "child_foo").add("https://example.com", "foo", "child_example_foo").build()
    );

    assertEquals("child_foo", stack.peek("foo"));
    assertEquals("child_example_foo", stack.peek("https://example.com", "foo"));
    assertEquals("parent_bar", stack.peek("bar"));
    assertEquals("parent_example_bar", stack.peek("https://example.com", "bar"));
    assertNull(stack.peek("baz"));
    assertNull(stack.peek("https://example.com", "baz"));
  }

  @Test
  void multipleLevelCascading() {
    stack.push(new AttributesBuilder().add("foo", "level1_foo").add("bar", "level1_bar").build());

    stack.push(new AttributesBuilder().add("baz", "level2_baz").build());

    stack.push(new AttributesBuilder().add("foo", "level3_foo").build());

    assertEquals("level3_foo", stack.peek("foo"));
    assertEquals("level1_bar", stack.peek("bar"));
    assertEquals("level2_baz", stack.peek("baz"));
  }

  @Test
  void cascadingStopsAtFirstValue() {
    stack.push(new AttributesBuilder().add("foo", "level1_foo").build());

    stack.push(new AttributesBuilder().add("foo", "level2_foo").build());

    stack.push(new AttributesBuilder().add("bar", "level3_bar").build());

    assertEquals("level2_foo", stack.peek("foo"));
    assertEquals("level3_bar", stack.peek("bar"));
  }

  @Test
  void firstPushWithMissingAttributes() {
    stack.push(new AttributesBuilder().add("foo", "only_foo").build());

    assertEquals("only_foo", stack.peek("foo"));
    assertNull(stack.peek("bar"));
    assertNull(stack.peek("baz"));
  }

  @Test
  void emptyPushCascadesAll() {
    stack.push(
      new AttributesBuilder().add("foo", "parent_foo").add("bar", "parent_bar").add("baz", "parent_baz").build()
    );

    stack.push(new AttributesBuilder().build());

    assertEquals("parent_foo", stack.peek("foo"));
    assertEquals("parent_bar", stack.peek("bar"));
    assertEquals("parent_baz", stack.peek("baz"));
  }

  @Test
  void partialOverrideWithCascading() {
    stack.push(
      new AttributesBuilder().add("foo", "parent_foo").add("bar", "parent_bar").add("baz", "parent_baz").build()
    );

    stack.push(new AttributesBuilder().add("bar", "child_bar").build());

    assertEquals("parent_foo", stack.peek("foo"));
    assertEquals("child_bar", stack.peek("bar"));
    assertEquals("parent_baz", stack.peek("baz"));
  }

  @Test
  void popRestoresOriginalValues() {
    stack.push(new AttributesBuilder().add("foo", "parent_foo").add("bar", "parent_bar").build());

    stack.push(new AttributesBuilder().add("foo", "child_foo").build());

    assertEquals("child_foo", stack.peek("foo"));
    assertEquals("parent_bar", stack.peek("bar"));

    stack.pop();
    assertEquals("parent_foo", stack.peek("foo"));
    assertEquals("parent_bar", stack.peek("bar"));
  }

  @Test
  void deepCascadingChain() {
    stack.push(new AttributesBuilder().add("foo", "L1_foo").add("bar", "L1_bar").add("baz", "L1_baz").build());

    stack.push(new AttributesBuilder().add("foo", "L2_foo").build());

    stack.push(new AttributesBuilder().add("bar", "L3_bar").build());

    stack.push(new AttributesBuilder().add("baz", "L4_baz").build());

    stack.push(new AttributesBuilder().build());

    assertEquals("L2_foo", stack.peek("foo"));
    assertEquals("L3_bar", stack.peek("bar"));
    assertEquals("L4_baz", stack.peek("baz"));
  }

  @Test
  void cascadingWithSingleAttribute() {
    AttributeStack singleStack = new AttributeStack("only");

    singleStack.push(new AttributesBuilder().add("only", "first").build());

    singleStack.push(new AttributesBuilder().build());

    assertEquals("first", singleStack.peek("only"));
  }

  @Test
  void complexInheritanceScenario() {
    stack.push(
      new AttributesBuilder().add("foo", "root_style").add("bar", "root_color").add("baz", "root_font").build()
    );

    stack.push(new AttributesBuilder().add("foo", "section_style").build());

    stack.push(new AttributesBuilder().add("bar", "subsection_color").build());

    stack.push(new AttributesBuilder().add("baz", "paragraph_font").build());

    assertEquals("section_style", stack.peek("foo"));
    assertEquals("subsection_color", stack.peek("bar"));
    assertEquals("paragraph_font", stack.peek("baz"));

    stack.pop();
    assertEquals("section_style", stack.peek("foo"));
    assertEquals("subsection_color", stack.peek("bar"));
    assertEquals("root_font", stack.peek("baz"));

    stack.pop();
    assertEquals("section_style", stack.peek("foo"));
    assertEquals("root_color", stack.peek("bar"));
    assertEquals("root_font", stack.peek("baz"));

    stack.pop();
    assertEquals("root_style", stack.peek("foo"));
    assertEquals("root_color", stack.peek("bar"));
    assertEquals("root_font", stack.peek("baz"));
  }

  @Test
  void errorConditionsWithCascading() {
    stack.push(new AttributesBuilder().add("foo", "value").build());

    assertThrows(IllegalArgumentException.class, () -> stack.peek("unknown"));

    AttributeStack emptyStack = new AttributeStack("test");
    assertThrows(IllegalStateException.class, () -> emptyStack.peek("test"));
  }

  @Test
  void cascadingWithThreeAttributes() {
    AttributeStack tripleStack = new AttributeStack("first", "second", "third");

    tripleStack.push(new AttributesBuilder().add("first", "1st").add("second", "2nd").add("third", "3rd").build());

    tripleStack.push(new AttributesBuilder().add("first", "new_1st").build());

    assertEquals("new_1st", tripleStack.peek("first"));
    assertEquals("2nd", tripleStack.peek("second"));
    assertEquals("3rd", tripleStack.peek("third"));
  }

  @Test
  void sixteenLevelsDeepWithCascading() {
    for (int i = 1; i <= 16; i++) {
      if (i % 3 == 1) {
        stack.push(new AttributesBuilder().add("foo", "level" + i).build());
      } else {
        stack.push(new AttributesBuilder().build());
      }
    }

    assertEquals("level16", stack.peek("foo"));

    for (int i = 15; i >= 1; i--) {
      stack.pop();
      if (i % 3 == 1) {
        assertEquals("level" + i, stack.peek("foo"));
      } else {
        int expectedLevel = ((i - 1) / 3) * 3 + 1;
        if (expectedLevel >= 1) {
          assertEquals("level" + expectedLevel, stack.peek("foo"));
        }
      }
    }
  }

  @Test
  void clear() {
    stack.push(new AttributesBuilder().add("foo", "value").build());

    stack.clear();
    assertThrows(IllegalStateException.class, () -> stack.peek("foo"));
  }
}
