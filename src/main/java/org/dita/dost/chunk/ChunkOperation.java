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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.dita.dost.util.URLUtils.toURI;

class ChunkOperation {

    public enum Operation {
        BY_TOPIC,
        BY_DOCUMENT,
        TO_CONTENT
    }

    public final Operation operation;
    public final URI src;
    public final String id;
    public final URI dst;
    public final List<ChunkOperation> children;

    private ChunkOperation(final Operation operation,
                           final URI src,
                           final String id,
                           final URI dst,
                           final List<ChunkOperation> children) {
        this.operation = operation;
        this.src = src;
        this.id = id;
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
                id.equals(that.id) &&
                Objects.equals(dst, that.dst) &&
                Objects.equals(children, that.children);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operation, src, id, dst, children);
    }

    public static ChunkBuilder builder(final Operation operation) {
        return new ChunkBuilder(operation);
    }

    public static class ChunkBuilder {
        public final Operation operation;
        private URI src;
        private URI dst;
        private String id;
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

        public ChunkBuilder addChild(final ChunkBuilder child) {
            children.add(child);
            return this;
        }

        public ChunkOperation build() {
            final URI src = this.src != null ? this.src : toURI(id + ".dita");
            final URI dst = this.dst != null ? this.dst : toURI(id + ".dita");
            final List<ChunkOperation> cos = children.stream().map(ChunkBuilder::build).collect(Collectors.toList());
            return new ChunkOperation(operation, src, id, dst, Collections.unmodifiableList(cos));
        }
    }
}
