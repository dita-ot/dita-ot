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
import java.net.URI;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Registry(String name, SemVer vers, List<Dependency> deps, URI uri, String cksum) {
  @JsonCreator
  public Registry(
    @JsonProperty("name") String name,
    @JsonProperty("vers") String vers,
    @JsonProperty("deps") Dependency[] deps,
    @JsonProperty("url") String url,
    @JsonProperty("cksum") String cksum
  ) {
    this(
      name,
      new SemVer(vers),
      deps == null ? List.of() : List.of(deps),
      (url != null ? URI.create(url) : null),
      cksum
    );
  }

  public record Dependency(String name, SemVerMatch req) {
    @JsonCreator
    public Dependency(@JsonProperty("name") String name, @JsonProperty("req") String req) {
      this(name, new SemVerMatch(req));
    }
  }
}
