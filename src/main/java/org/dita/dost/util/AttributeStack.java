package org.dita.dost.util;

import java.util.Arrays;
import org.xml.sax.Attributes;

public class AttributeStack {

  private String[] stack;
  private final String[] names;
  private final int attributeCount;
  private final int[] nameHashes;
  private int top = -1;

  public AttributeStack(String... names) {
    this.names = names.clone();
    this.attributeCount = names.length;
    this.nameHashes = new int[attributeCount];
    for (int i = 0; i < attributeCount; i++) {
      this.nameHashes[i] = this.names[i].hashCode();
    }
    this.stack = new String[16 * attributeCount];
  }

  public void push(Attributes attrs) {
    top++;
    if ((top + 1) * attributeCount > stack.length) {
      stack = Arrays.copyOf(stack, stack.length * 2);
    }

    int baseIndex = top * attributeCount;
    for (int i = 0; i < attributeCount; i++) {
      var value = attrs.getValue(names[i]);
      if (value == null && top > 0) {
        value = stack[(top - 1) * attributeCount + i];
      }
      stack[baseIndex + i] = value;
    }
  }

  public String peek(String name) {
    if (top < 0) {
      throw new IllegalStateException("Stack is empty");
    }

    var nameHash = name.hashCode();
    for (int i = 0; i < attributeCount; i++) {
      if (nameHash == nameHashes[i] && name.equals(names[i])) {
        return stack[top * attributeCount + i];
      }
    }
    throw new IllegalArgumentException("Stack not initialized for attribute " + name);
  }

  public void pop() {
    if (top < 0) {
      throw new IllegalStateException("Stack is empty");
    }

    int baseIndex = top * attributeCount;
    Arrays.fill(stack, baseIndex, baseIndex + attributeCount, null);
    top--;
  }
}
