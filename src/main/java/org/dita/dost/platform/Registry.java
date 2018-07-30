/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2018 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.platform;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Registry {

    public final String name;
    public final SemVer vers;
    public final List<Dependency> deps;
    public final String url;
    public final String cksum;

    @JsonCreator
    public Registry(@JsonProperty("name") String name,
                    @JsonProperty("vers") String vers,
                    @JsonProperty("deps") Dependency[] deps,
                    @JsonProperty("url") String url,
                    @JsonProperty("cksum") String cksum) {
        this.name = name;
        this.vers = new SemVer(vers);
        this.deps = Collections.unmodifiableList(Arrays.asList(deps));
        this.url = url;
        this.cksum = cksum;
    }

    public static class Dependency {

        public final String name;
        public final SemVerMatch req;

        @JsonCreator
        public Dependency(@JsonProperty("name") String name,
                           @JsonProperty("req") String req) {
            this.name = name;
            this.req = new SemVerMatch(req);
        }
    }

}
