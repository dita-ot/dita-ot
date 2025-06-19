package org.dita.dost.util;

import java.util.ArrayDeque;
import java.util.Deque;
import org.xml.sax.Attributes;

public class AttributeStack {

  private final Deque<String[]> stack = new ArrayDeque<>();
  private final String[] names;

  public AttributeStack(String... names) {
    this.names = names;
  }

  public void push(Attributes attrs) {
    var values = new String[names.length];
    for (int i = 0; i < values.length; i++) {
      values[i] = attrs.getValue(names[i]);
    }
    stack.push(values);
  }

  public String peek(String name) {
    for (int i = 0; i < names.length; i++) {
      if (name.equals(names[i])) {
        return stack.peek()[i];
      }
    }
    throw new IllegalArgumentException("Stack not initialized for attribute " + name);
  }

  public void pop() {
    stack.pop();
  }
}
