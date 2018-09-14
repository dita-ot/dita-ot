/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2018 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.platform;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Registry {

    public final String name;
    public final SemVer vers;
    public final List<Dependency> deps;
    public final URL url;
    public final String cksum;

    @JsonCreator
    public Registry(@JsonProperty("name") String name,
                    @JsonProperty("vers") String vers,
                    @JsonProperty("deps") Dependency[] deps,
                    @JsonProperty("url") String url,
                    @JsonProperty("cksum") String cksum) {
        this.name = name;
        this.vers = new SemVer(vers);
        this.deps = deps == null ? emptyList() : unmodifiableList(Arrays.asList(deps));
        try {
            this.url = url != null ? new URL(url) : null;
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
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
