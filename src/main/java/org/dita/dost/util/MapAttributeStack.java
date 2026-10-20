package org.dita.dost.util;

import static javax.xml.XMLConstants.NULL_NS_URI;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.Constants.MAP_MAP;

import com.google.common.annotations.VisibleForTesting;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.xml.sax.Attributes;

public class MapAttributeStack {

  private static final String[] LOCAL_NAMES = {
    ATTRIBUTE_NAME_PROPS,
    ATTRIBUTE_NAME_AUDIENCE,
    ATTRIBUTE_NAME_PLATFORM,
    ATTRIBUTE_NAME_PRODUCT,
    ATTRIBUTE_NAME_OTHERPROPS,
    ATTRIBUTE_NAME_DELIVERY_TARGET,
  };

  private String[][] localNames;
  private String[][] stack;
  private int top = -1;
  private boolean[] mergeStack;

  public MapAttributeStack() {
    this.localNames = new String[16][];
    this.stack = new String[16][];
    this.mergeStack = new boolean[16];
  }

  public void push(Attributes attrs) {
    top++;
    ensureCapacity();

    var specializations = attrs.getValue(ATTRIBUTE_NAME_SPECIALIZATIONS);
    if (specializations != null) {
      localNames[top] = parseSpecializations(specializations);
    } else {
      localNames[top] = top == 0 ? LOCAL_NAMES : localNames[top - 1];
    }

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

    var length = localNames[top].length;
    stack[top] = new String[length];
    for (int i = 0; i < length; i++) {
      var value = attrs.getValue(NULL_NS_URI, localNames[top][i]);
      if (top > 0) {
        if (value == null) {
          var prev = stack[top - 1][i];
          value = prev;
        } else if (mergeStack[top]) {
          var prev = stack[top - 1][i];
          if (prev != null) {
            value = merge(value, prev);
          }
        }
      }
      stack[top][i] = value;
    }
  }

  @VisibleForTesting
  static String[] parseSpecializations(String specializations) {
    var res = new LinkedHashSet<>(LOCAL_NAMES.length);
    res.add(ATTRIBUTE_NAME_PROPS);
    Arrays
      .stream(specializations.trim().split("\\s+"))
      .filter(token -> token.startsWith("@props/"))
      .flatMap(token -> {
        String[] names = token.split("/");
        return Arrays.stream(names, 1, names.length);
      })
      .forEach(res::add);
    return res.toArray(String[]::new);
  }

  @VisibleForTesting
  static String merge(String prev, String curr) {
    return Stream
      .of(prev, curr)
      .flatMap(s -> Arrays.stream(s.strip().split("\\s+")))
      .filter(token -> !token.isEmpty())
      .distinct()
      .collect(Collectors.joining(" "));
  }

  private void ensureCapacity() {
    if (top > stack.length) {
      localNames = Arrays.copyOf(localNames, localNames.length * 2);
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

    var length = localNames[top].length;
    for (int i = 0; i < length; i++) {
      if (localName.equals(localNames[top][i])) {
        return stack[top][i];
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

    localNames[top] = null;
    stack[top] = null;

    top--;
  }

  /**
   * Clear stack.
   */
  public void clear() {
    top = -1;
  }
}
