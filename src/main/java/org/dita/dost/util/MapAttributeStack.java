package org.dita.dost.util;

import static javax.xml.XMLConstants.NULL_NS_URI;
import static javax.xml.XMLConstants.XML_NS_URI;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.Constants.MAP_MAP;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.xml.sax.Attributes;

public class MapAttributeStack {

  private static final String[] LOCAL_NAMES = {
    ATTRIBUTE_NAME_AUDIENCE,
    ATTRIBUTE_NAME_PLATFORM,
    ATTRIBUTE_NAME_PRODUCT,
    ATTRIBUTE_NAME_OTHERPROPS,
    ATTRIBUTE_NAME_PROPS,
    ATTRIBUTE_NAME_DELIVERY_TARGET,
    "hardware-platform",
  };

  private final String[] localNames;
  private final String[] namespaces;
  private final int attributeCount;
  private final int[] localNameHashes;
  private final int[] namespaceHashes;
  private String[] stack;
  private int top = -1;
  private boolean[] mergeStack;

  public MapAttributeStack() {
    this.localNames = LOCAL_NAMES;
    this.namespaces = Stream.generate(() -> NULL_NS_URI).limit(LOCAL_NAMES.length).toArray(String[]::new);
    this.attributeCount = localNames.length;
    this.localNameHashes = new int[attributeCount];
    this.namespaceHashes = new int[attributeCount];
    for (int i = 0; i < attributeCount; i++) {
      this.localNameHashes[i] = this.localNames[i].hashCode();
      this.namespaceHashes[i] = this.namespaces[i].hashCode();
    }
    this.stack = new String[16 * attributeCount];

    this.mergeStack = new boolean[stack.length / attributeCount];
  }

  public void push(Attributes attrs) {
    top++;
    ensureCapacity();

    var cascade = attrs.getValue(ATTRIBUTE_NAME_CASCADE);
    if (cascade != null) {
      mergeStack[top] = cascade.equals(ATTRIBUTE_CASCADE_VALUE_MERGE);
    } else if (MAP_MAP.matches(attrs)) {
      mergeStack[top] = true;
    } else if (top > 0) {
      mergeStack[top] = mergeStack[top - 1];
    } else {
      mergeStack[top] = false;
    }

    int baseIndex = top * attributeCount;
    for (int i = 0; i < attributeCount; i++) {
      var value = attrs.getValue(namespaces[i], localNames[i]);
      if (top > 0) {
        if (value == null) {
          var prev = stack[(top - 1) * attributeCount + i];
          value = prev;
        } else if (mergeStack[top]) {
          var prev = stack[(top - 1) * attributeCount + i];
          if (prev != null) {
            value = merge(value, prev);
          }
        }
      }
      stack[baseIndex + i] = value;
    }
  }

  // TODO: optimize
  private String merge(String prev, String curr) {
    return Stream
      .of(prev, curr)
      .flatMap(s -> Arrays.stream(s.strip().split("\\s+")))
      .filter(token -> !token.isEmpty())
      .distinct()
      .collect(Collectors.joining(" "));
  }

  private void ensureCapacity() {
    if ((top + 1) * attributeCount > stack.length) {
      stack = Arrays.copyOf(stack, stack.length * 2);
      mergeStack = Arrays.copyOf(mergeStack, mergeStack.length * 2);
    }
  }

  /**
   * Peek the top most attribute on the stack.
   * @param localName name of the attribute in namespace
   * @return cascaded value for the attribute, may be {@code null}
   */
  public String peek(String localName) {
    return peek(NULL_NS_URI, localName);
  }

  /**
   * Peek the top most attribute on the stack.
   * @param namespace namespace URI of the attribute
   * @param localName local name of the attribute
   * @return cascaded value for the attribute, may be {@code null}
   */
  public String peek(String namespace, String localName) {
    if (top < 0) {
      throw new IllegalStateException("Stack is empty");
    }

    var nameHash = localName.hashCode();
    var namespaceHash = namespace.hashCode();
    for (int i = 0; i < attributeCount; i++) {
      if (
        nameHash == localNameHashes[i] &&
        namespaceHash == namespaceHashes[i] &&
        localName.equals(localNames[i]) &&
        namespace.equals(namespaces[i])
      ) {
        return stack[top * attributeCount + i];
      }
    }
    return null;
  }

  /**
   * Pop attribute from the top of the stack.
   */
  public void pop() {
    if (top < 0) {
      throw new IllegalStateException("Stack is empty");
    }

    int baseIndex = top * attributeCount;
    Arrays.fill(stack, baseIndex, baseIndex + attributeCount, null);
    top--;
  }

  /**
   * Clear stack.
   */
  public void clear() {
    top = -1;
  }
}
