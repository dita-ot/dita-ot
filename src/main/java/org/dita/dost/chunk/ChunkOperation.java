/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2020 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.chunk;

import com.google.common.annotations.VisibleForTesting;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

class ChunkOperation {

    public enum Operation {
        SELECT_TOPIC("select-topic"),
        SELECT_DOCUMENT("select-document"),
        SELECT_BRANCH("select-branch"),
        /** Split */
        BY_TOPIC("by-topic"),
        BY_DOCUMENT("by-document"),
        /** Merge */
        TO_CONTENT("to-content"),
        TO_NAVIGATION("to-navigation");

        public final String token;

        Operation(final String token) {
            this.token = token;
        }
    }

    public final Operation operation;
    public final URI src;
    public final URI dst;
    public final List<ChunkOperation> children;

    @VisibleForTesting
    ChunkOperation(final Operation operation,
                   final URI src,
                   final URI dst,
                   final List<ChunkOperation> children) {
        this.operation = operation;
        this.src = src;
        this.dst = dst;
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
                Objects.equals(children, that.children);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operation, src, dst, children);
    }

    @Override
    public String toString() {
        return "ChunkOperation{" +
                "operation=" + operation +
                ", src=" + src +
                ", dst=" + dst +
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

        public ChunkBuilder addChild(final ChunkBuilder child) {
            children.add(child);
            return this;
        }

        public ChunkOperation build() {
            final URI src = this.src;
            final URI dst = this.dst;
            final List<ChunkOperation> cos = children.stream().map(ChunkBuilder::build).collect(Collectors.toList());
            return new ChunkOperation(operation, src, dst, Collections.unmodifiableList(cos));
        }
    }
}
