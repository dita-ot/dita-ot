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
    var res = new String[names.length];
    for (int i = 0; i < res.length; i++) {
      var value = attrs.getValue(names[i]);
      if (value == null) {
        var previous = stack.peek();
        if (previous != null) {
          value = previous[i];
        }
      }
      res[i] = value;
    }
    stack.push(res);
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
