package org.dita.dost.util;

import java.util.Arrays;
import org.xml.sax.Attributes;

public class AttributeStack {

  private String[][] stack;
  private final String[] names;
  private final int[] nameHashes;
  private int top = -1;

  public AttributeStack(String... names) {
    this.names = names.clone();
    this.nameHashes = new int[this.names.length];
      for (int i = 0; i < this.names.length; i++) {
          this.nameHashes[i] = this.names[i].hashCode();
      }
    this.stack = new String[16][names.length];
  }

  public void push(Attributes attrs) {
    if (++top >= stack.length) {
      stack = Arrays.copyOf(stack, stack.length * 2);
      for (int i = stack.length / 2; i < stack.length; i++) {
        stack[i] = new String[names.length];
      }
    }

    var res = stack[top];
    for (int i = 0; i < res.length; i++) {
      var value = attrs.getValue(names[i]);
      if (value == null && top > 0) {
        value = stack[top - 1][i];
      }
      res[i] = value;
    }
  }

  public String peek(String name) {
    if (top < 0) {
      throw new IllegalStateException("Stack is empty");
    }

    var nameHash = name.hashCode(); 
    for (int i = 0; i < names.length; i++) {
      if (nameHashes[i] == nameHash && name.equals(names[i])) {
        return stack[top][i];
      }
    }
    throw new IllegalArgumentException("Stack not initialized for attribute " + name);
  }

  public void pop() {
    if (top < 0) {
      throw new IllegalStateException("Stack is empty");
    }

    Arrays.fill(stack[top], null);
    top--;
  }
}
