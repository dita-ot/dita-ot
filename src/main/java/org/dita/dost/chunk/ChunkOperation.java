/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2020 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.chunk;

import com.google.common.annotations.VisibleForTesting;
import org.w3c.dom.Element;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Collections.unmodifiableList;

class ChunkOperation {

    public enum Operation {
        COMBINE("combine"),
        SPLIT("split");
        public final String name;

        Operation(final String name) {
            this.name = name;
        }
    }

    public final Operation operation;
    public final URI src;
    public final URI dst;
    public final String id;
    public final Element topicref;
    public final List<ChunkOperation> children;

    @VisibleForTesting
    ChunkOperation(final Operation operation,
                   final URI src,
                   final URI dst,
                   final String id,
                   final Element topicref,
                   final List<ChunkOperation> children) {
        this.operation = operation;
        this.src = src;
        this.dst = dst;
        this.id = id;
        this.topicref = topicref;
        this.children = children;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChunkOperation that = (ChunkOperation) o;
        return operation == that.operation &&
                Objects.equals(src, that.src) &&
                Objects.equals(dst, that.dst) &&
                Objects.equals(id, that.id) &&
                Objects.equals(topicref, that.topicref) &&
                Objects.equals(children, that.children);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operation, src, dst, id, topicref, children);
    }

    @Override
    public String toString() {
        return "ChunkOperation{" +
                "operation=" + operation +
                ", src=" + src +
                ", dst=" + dst +
                ", id=" + id +
                ", children=" + children +
                '}';
    }

    public static ChunkBuilder builder(final Operation operation) {
        return new ChunkBuilder(operation);
    }

    public static class ChunkBuilder {
        private final Operation operation;
        private URI src;
        private URI dst;
        private String id;
        private Element topicref;
        private List<ChunkBuilder> children = new ArrayList<>();

        public ChunkBuilder(final Operation operation) {
            this.operation = operation;
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
            final URI src = this.src;
            final URI dst = this.dst;
            final List<ChunkOperation> cos = children.stream()
                    .map(ChunkBuilder::build)
                    .collect(Collectors.toList());
            return new ChunkOperation(operation, src, dst, id, topicref, unmodifiableList(cos));
        }
    }
}
