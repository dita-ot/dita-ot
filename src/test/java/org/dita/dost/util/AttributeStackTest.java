package org.dita.dost.util;

import static org.junit.jupiter.api.Assertions.*;

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
  void basicCascading() {
    stack.push(new AttributesBuilder().add("foo", "parent_foo").add("bar", "parent_bar").build());

    stack.push(new AttributesBuilder().add("foo", "child_foo").build());

    assertEquals("child_foo", stack.peek("foo"));
    assertEquals("parent_bar", stack.peek("bar"));
    assertNull(stack.peek("baz"));
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
