package org.dita.dost.util;

import static javax.xml.XMLConstants.NULL_NS_URI;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.Constants.MAP_MAP;

import com.google.common.annotations.VisibleForTesting;
import java.util.Arrays;
import java.util.LinkedHashSet;
import org.xml.sax.Attributes;

public class ProfilingAttributeStack {

  private static final String[] LOCAL_NAMES = {
    ATTRIBUTE_NAME_PROPS,
    ATTRIBUTE_NAME_AUDIENCE,
    ATTRIBUTE_NAME_PLATFORM,
    ATTRIBUTE_NAME_PRODUCT,
    ATTRIBUTE_NAME_OTHERPROPS,
  };

  private static final String PROPS_PREFIX = "@props/";

  private String[][] localNames;
  private String[][] stack;
  private int top = -1;
  private boolean[] mergeStack;

  public ProfilingAttributeStack() {
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
    var res = new LinkedHashSet<String>();
    res.add(ATTRIBUTE_NAME_PROPS);
    final int len = specializations.length();
    int pos = 0;
    while (pos < len) {
      while (pos < len && specializations.charAt(pos) <= ' ') {
        pos++;
      }
      int end = pos;
      while (end < len && specializations.charAt(end) > ' ') {
        end++;
      }
      if (specializations.startsWith(PROPS_PREFIX, pos)) {
        int start = pos + PROPS_PREFIX.length();
        for (int i = start; i <= end; i++) {
          if (i == end || specializations.charAt(i) == '/') {
            if (i > start) {
              res.add(specializations.substring(start, i));
            }
            start = i + 1;
          }
        }
      }
      pos = end;
    }
    return res.toArray(String[]::new);
  }

  @VisibleForTesting
  static String merge(final String prev, final String curr) {
    final StringBuilder res = new StringBuilder(prev.length() + curr.length() + 1);
    appendTokens(res, prev);
    appendTokens(res, curr);
    return res.toString();
  }

  private static void appendTokens(final StringBuilder res, final String value) {
    final int len = value.length();
    int pos = 0;
    while (pos < len) {
      while (pos < len && value.charAt(pos) <= ' ') {
        pos++;
      }
      int end = pos;
      while (end < len && value.charAt(end) > ' ') {
        end++;
      }
      if (end > pos && !containsToken(res, value, pos, end)) {
        if (!res.isEmpty()) {
          res.append(' ');
        }
        res.append(value, pos, end);
      }
      pos = end;
    }
  }

  private static boolean containsToken(final StringBuilder res, final String value, final int from, final int to) {
    final int length = to - from;
    final int len = res.length();
    int pos = 0;
    while (pos < len) {
      int end = res.indexOf(" ", pos);
      if (end == -1) {
        end = len;
      }
      if (end - pos == length) {
        int i = 0;
        while (i < length && res.charAt(pos + i) == value.charAt(from + i)) {
          i++;
        }
        if (i == length) {
          return true;
        }
      }
      pos = end + 1;
    }
    return false;
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
