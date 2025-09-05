/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2020 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.chunk;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.w3c.dom.Element;

record ChunkOperation(
  org.dita.dost.chunk.ChunkOperation.Operation operation,
  URI src,
  URI dst,
  String id,
  Element topicref,
  List<ChunkOperation> children,
  Select select
) {
  private ChunkOperation(
    org.dita.dost.chunk.ChunkOperation.Operation operation,
    URI src,
    URI dst,
    String id,
    Element topicref,
    List<ChunkOperation> children
  ) {
    this(operation, src, dst, id, topicref, children, Select.DOCUMENT);
  }

  public enum Operation {
    COMBINE("combine"),
    SPLIT("split");

    public final String name;

    Operation(final String name) {
      this.name = name;
    }
  }

  public enum Select {
    DOCUMENT("select-document"),
    BRANCH("select-branch"),
    TOPIC("select-topic");

    public final String value;

    Select(final String value) {
      this.value = value;
    }

    public static Select of(String value) {
      for (Select select : values()) {
        if (select.value.equals(value)) {
          return select;
        }
      }
      return null;
    }
  }

  @Override
  public String toString() {
    return (
      "ChunkOperation{" +
      "operation=" +
      operation +
      ", src=" +
      src +
      ", dst=" +
      dst +
      ", id=" +
      id +
      ", children=" +
      children +
      '}'
    );
  }

  public static ChunkBuilder builder(final Operation operation) {
    return new ChunkBuilder(operation);
  }

  public static ChunkBuilder builder(final ChunkOperation operation) {
    return new ChunkBuilder(operation);
  }

  public static class ChunkBuilder {

    private final Operation operation;
    private URI src;
    private URI dst;
    private String id;
    private Element topicref;
    private List<ChunkBuilder> children = new ArrayList<>();
    private Select select = Select.DOCUMENT;

    public ChunkBuilder(final Operation operation) {
      this.operation = operation;
    }

    public ChunkBuilder(final ChunkOperation orig) {
      this.operation = orig.operation;
      this.src = orig.src;
      this.dst = orig.dst;
      this.id = orig.id;
      this.topicref = orig.topicref;
      this.children = orig.children.stream().map(ChunkOperation::builder).collect(Collectors.toList());
      this.select = orig.select;
    }

    public ChunkBuilder select(final Select select) {
      this.select = select;
      return this;
    }

    public ChunkBuilder src(final URI src) {
      //            assert src.isAbsolute();
      this.src = src;
      return this;
    }

    public ChunkBuilder dst(final URI dst) {
      //            assert dst.isAbsolute();
      this.dst = dst;
      return this;
    }

    public ChunkBuilder id(final String id) {
      this.id = id;
      return this;
    }

    public ChunkBuilder topicref(final Element topicref) {
      this.topicref = topicref;
      return this;
    }

    public ChunkBuilder addChild(final ChunkBuilder child) {
      children.add(child);
      return this;
    }

    public ChunkOperation build() {
      final List<ChunkOperation> cos = children.stream().map(ChunkBuilder::build).toList();
      return new ChunkOperation(operation, src, dst, id, topicref, cos, select);
    }
  }
}
