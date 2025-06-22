package org.dita.dost.util;

import static javax.xml.XMLConstants.NULL_NS_URI;
import static javax.xml.XMLConstants.XML_NS_URI;
import static org.dita.dost.util.Constants.*;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import org.xml.sax.Attributes;

/**
 * Stack to track cascading attributes in SAX.
 */
public class AttributeStack {

  private static final String[] LOCAL_NAMES = {
    ATTRIBUTE_NAME_AUDIENCE,
    ATTRIBUTE_NAME_PLATFORM,
    ATTRIBUTE_NAME_PRODUCT,
    ATTRIBUTE_NAME_OTHERPROPS,
    ATTRIBUTE_NAME_REV,
    ATTRIBUTE_NAME_PROPS,
    ATTRIBUTE_NAME_LINKING,
    ATTRIBUTE_NAME_TOC,
    ATTRIBUTE_NAME_PRINT,
    ATTRIBUTE_NAME_SEARCH,
    ATTRIBUTE_NAME_FORMAT,
    ATTRIBUTE_NAME_SCOPE,
    ATTRIBUTE_NAME_TYPE,
    ATTRIBUTE_NAME_DIR,
    ATTRIBUTE_NAME_TRANSLATE,
    ATTRIBUTE_NAME_PROCESSING_ROLE,
    ATTRIBUTE_NAME_CASCADE,
    ATTRIBUTE_NAME_LANG,
  };

  private static final String[] NAMESPACES = {
    NULL_NS_URI,
    NULL_NS_URI,
    NULL_NS_URI,
    NULL_NS_URI,
    NULL_NS_URI,
    NULL_NS_URI,
    NULL_NS_URI,
    NULL_NS_URI,
    NULL_NS_URI,
    NULL_NS_URI,
    NULL_NS_URI,
    NULL_NS_URI,
    NULL_NS_URI,
    NULL_NS_URI,
    NULL_NS_URI,
    NULL_NS_URI,
    NULL_NS_URI,
    XML_NS_URI,
  };

  private String[] stack;
  private final String[] localNames;
  private final String[] namespaces;
  private final int attributeCount;
  private final int[] localNameHashes;
  private final int[] namespaceHashes;
  private int top = -1;

  /**
   * Create new attribute stack for all cascading attributes.
   *
   * @see <a href="https://docs.oasis-open.org/dita/dita/v1.3/errata02/os/complete/part3-all-inclusive/archSpec/base/cascading-in-a-ditamap.html">Cascading of metadata attributes in a DITA map</a>
   */
  public AttributeStack() {
    this(NAMESPACES, LOCAL_NAMES);
  }

  /**
   * Create new attribute stack.
   * @param localNames names of attributes to collect
   */
  public AttributeStack(String... localNames) {
    this(Stream.generate(() -> NULL_NS_URI).limit(localNames.length).toArray(String[]::new), localNames.clone());
  }

  /**
   * Create new attribute stack.
   * @param qNames names of attributes to collect
   */
  public AttributeStack(QName... qNames) {
    this(
      Stream.of(qNames).map(QName::getNamespaceURI).toArray(String[]::new),
      Stream.of(qNames).map(QName::getLocalPart).toArray(String[]::new)
    );
  }

  private AttributeStack(String[] namespaces, String[] localNames) {
    if (namespaces.length != localNames.length) {
      throw new IllegalArgumentException(
        "namespaces and localNames must have same length: %s != %s".formatted(namespaces.length, localNames.length)
      );
    }
    this.localNames = localNames;
    this.namespaces = namespaces;
    this.attributeCount = localNames.length;
    this.localNameHashes = new int[attributeCount];
    this.namespaceHashes = new int[attributeCount];
    for (int i = 0; i < attributeCount; i++) {
      this.localNameHashes[i] = this.localNames[i].hashCode();
      this.namespaceHashes[i] = this.namespaces[i].hashCode();
    }
    this.stack = new String[16 * attributeCount];
  }

  /**
   * Push tracked attributes to stack.
   */
  public void push(Attributes attrs) {
    top++;
    if ((top + 1) * attributeCount > stack.length) {
      stack = Arrays.copyOf(stack, stack.length * 2);
    }

    int baseIndex = top * attributeCount;
    for (int i = 0; i < attributeCount; i++) {
      var value = attrs.getValue(namespaces[i], localNames[i]);
      if (value == null && top > 0) {
        value = stack[(top - 1) * attributeCount + i];
      }
      stack[baseIndex + i] = value;
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
    throw new IllegalArgumentException("Stack not initialized for attribute {%s}%s".formatted(namespace, localName));
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
