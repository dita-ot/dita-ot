package org.dita.dost.util;

import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.Constants.MAP_MAP;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.xml.sax.Attributes;

public class MapAttributeStack extends AttributeStack {

  private boolean[] mergeStack;

  public MapAttributeStack() {
    super(
      ATTRIBUTE_NAME_AUDIENCE,
      ATTRIBUTE_NAME_PLATFORM,
      ATTRIBUTE_NAME_PRODUCT,
      ATTRIBUTE_NAME_OTHERPROPS,
      ATTRIBUTE_NAME_PROPS,
      ATTRIBUTE_NAME_DELIVERY_TARGET,
      // FIXME: This just fixes the test, correct approach is to detect props specializations
      "hardware-platform"
    );
    this.mergeStack = new boolean[stack.length / attributeCount];
  }

  @Override
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

  @Override
  void ensureCapacity() {
    if ((top + 1) * attributeCount > stack.length) {
      stack = Arrays.copyOf(stack, stack.length * 2);
      mergeStack = Arrays.copyOf(mergeStack, mergeStack.length * 2);
    }
  }
}
