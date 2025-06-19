package org.dita.dost.util;

import org.dita.dost.util.XMLUtils.AttributesBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class AttributeStackTest {
    private AttributeStack stack;

    @BeforeEach
    void setUp() {
        stack = new AttributeStack("foo", "bar");
    }

    @Test
    void pushSingleAttribute() {
        stack.push(new AttributesBuilder().add("foo", "FOO").build());
        assertEquals("FOO", stack.peek("foo"));
    }

    @Test
    void pushMultipleAttributes() {
        stack.push(new AttributesBuilder()
                .add("foo", "FOO_VALUE")
                .add("bar", "BAR_VALUE")
                .build());

        assertEquals("FOO_VALUE", stack.peek("foo"));
        assertEquals("BAR_VALUE", stack.peek("bar"));
    }

    @Test
    void peekNonExisting() {
        stack.push(new AttributesBuilder().add("foo", "FOO").build());
        assertNull(stack.peek("bar"));
    }

    @Test
    void multipleStackOperations() {
        stack.push(new AttributesBuilder().add("foo", "level1").build());
        assertEquals("level1", stack.peek("foo"));

        stack.push(new AttributesBuilder().add("foo", "level2").add("bar", "bar2").build());
        assertEquals("level2", stack.peek("foo"));
        assertEquals("bar2", stack.peek("bar"));

        stack.pop();
        assertEquals("level1", stack.peek("foo"));
        assertNull(stack.peek("bar"));

        stack.pop();
    }

    @Test
    void deepStackOperations() {
        // Push multiple levels
        for (int i = 1; i <= 5; i++) {
            stack.push(new AttributesBuilder()
                    .add("foo", "foo" + i)
                    .add("bar", "bar" + i)
                    .build());
        }

        assertEquals("foo5", stack.peek("foo"));
        assertEquals("bar5", stack.peek("bar"));

        for (int i = 4; i >= 1; i--) {
            stack.pop();
            assertEquals("foo" + i, stack.peek("foo"));
            assertEquals("bar" + i, stack.peek("bar"));
        }
    }

    @Test
    void emptyAttributes() {
        stack.push(new AttributesBuilder().build());
        assertNull(stack.peek("foo"));
        assertNull(stack.peek("bar"));
    }

    @Test
    void nullAttributeValues() {
        stack.push(new AttributesBuilder().add("foo", null).build());
        assertNull(stack.peek("foo"));
        assertNull(stack.peek("bar"));
    }

    @Test
    void overwriteAttributeValues() {
        stack.push(new AttributesBuilder().add("foo", "original").build());
        assertEquals("original", stack.peek("foo"));

        stack.push(new AttributesBuilder().add("foo", "overwritten").build());
        assertEquals("overwritten", stack.peek("foo"));

        stack.pop();
        assertEquals("original", stack.peek("foo"));
    }

    @Test
    void peekUninitializedAttribute() {
        stack.push(new AttributesBuilder().add("foo", "value").build());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> stack.peek("nonexistent")
        );

        assertEquals("Stack not initialized for attribute nonexistent", exception.getMessage());
    }

    @Test
    void peekFromEmptyStack() {
        assertThrows(NullPointerException.class, () -> stack.peek("foo"));
    }

    @Test
    void popEmptyStack() {
        assertThrows(java.util.NoSuchElementException.class, () -> stack.pop());
    }

    @Test
    void singleAttributeStack() {
        AttributeStack singleStack = new AttributeStack("only");
        singleStack.push(new AttributesBuilder().add("only", "value").build());

        assertEquals("value", singleStack.peek("only"));

        assertThrows(IllegalArgumentException.class, () -> singleStack.peek("other"));
    }

    @Test
    void threeAttributeStack() {
        AttributeStack tripleStack = new AttributeStack("first", "second", "third");
        tripleStack.push(new AttributesBuilder()
                .add("first", "1st")
                .add("second", "2nd")
                .add("third", "3rd")
                .build());

        assertEquals("1st", tripleStack.peek("first"));
        assertEquals("2nd", tripleStack.peek("second"));
        assertEquals("3rd", tripleStack.peek("third"));
    }

    @Test
    void caseSensitivity() {
        AttributeStack caseStack = new AttributeStack("Foo", "FOO");
        caseStack.push(new AttributesBuilder()
                .add("Foo", "mixed")
                .add("FOO", "upper")
                .build());

        assertEquals("mixed", caseStack.peek("Foo"));
        assertEquals("upper", caseStack.peek("FOO"));

        assertThrows(IllegalArgumentException.class, () -> caseStack.peek("foo"));
    }

    @Test
    void partialAttributeUpdates() {
        stack.push(new AttributesBuilder()
                .add("foo", "foo1")
                .add("bar", "bar1")
                .build());

        stack.push(new AttributesBuilder().add("foo", "foo2").build());

        assertEquals("foo2", stack.peek("foo"));
        assertNull(stack.peek("bar"));

        stack.pop();
        assertEquals("foo1", stack.peek("foo"));
        assertEquals("bar1", stack.peek("bar"));
    }

    @Test
    void sixteenLevelsDeep() {
        for (int i = 1; i <= 16; i++) {
            stack.push(new AttributesBuilder()
                    .add("foo", "level" + i)
                    .build());
        }

        assertEquals("level16", stack.peek("foo"));

        for (int i = 15; i >= 1; i--) {
            stack.pop();
            assertEquals("level" + i, stack.peek("foo"));
        }
    }
}