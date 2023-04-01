/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2019 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.project;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.util.List;

/**
 * Project file AST builder.
 */
public class ProjectBuilder {

  @JsonProperty("deliverables")
  public List<Deliverable> deliverables;

  @JsonProperty("includes")
  public List<URI> includes;

  @JsonProperty("publications")
  public List<Publication> publications;

  @JsonProperty("contexts")
  public List<Context> contexts;

  @JsonCreator
  public ProjectBuilder(
    @JsonProperty("deliverables") List<Deliverable> deliverables,
    @JsonProperty("includes") List<URI> includes,
    @JsonProperty("publications") List<Publication> publications,
    @JsonProperty("contexts") List<Context> contexts
  ) {
    this.deliverables = deliverables;
    this.includes = includes;
    this.publications = publications;
    this.contexts = contexts;
  }

  public static class Deliverable {

    public final String name;
    public final String id;
    public final Context context;
    public final URI output;
    public final Publication publication;

    @JsonCreator
    public Deliverable(
      @JsonProperty("name") String name,
      @JsonProperty("id") String id,
      @JsonProperty("context") Context context,
      @JsonProperty("output") URI output,
      @JsonProperty("publication") Publication publication
    ) {
      this.name = name;
      this.id = id;
      this.context = context;
      this.output = output;
      this.publication = publication;
    }

    public static class Profile {

      public final List<URI> ditavals;

      @JsonCreator
      public Profile(@JsonProperty("ditavals") List<URI> ditavals) {
        this.ditavals = ditavals;
      }
    }
  }

  public static class Context {

    public final String name;
    public final String id;
    public final String idref;
    public final List<URI> input;
    public final Deliverable.Profile profiles;

    @JsonCreator
    public Context(
      @JsonProperty("name") String name,
      @JsonProperty("id") String id,
      @JsonProperty("idref") String idref,
      @JsonProperty("input") List<URI> input,
      @JsonProperty("profiles") Deliverable.Profile profiles
    ) {
      this.name = name;
      this.id = id;
      this.idref = idref;
      this.input = input;
      this.profiles = profiles;
    }
  }

  public static class Publication {

    public final String name;
    public final String id;
    public final String idref;
    public final String transtype;
    public final List<Param> params;
    public final Deliverable.Profile profiles;

    @JsonCreator
    public Publication(
      @JsonProperty("name") String name,
      @JsonProperty("id") String id,
      @JsonProperty("idref") String idref,
      @JsonProperty("transtype") String transtype,
      @JsonProperty("params") List<Param> params,
      @JsonProperty("profiles") Deliverable.Profile profiles
    ) {
      this.name = name;
      this.id = id;
      this.idref = idref;
      this.transtype = transtype;
      this.params = params;
      this.profiles = profiles;
    }

    public static class Param {

      public final String name;
      public final String value;
      public final URI href;
      public final URI path;

      @JsonCreator
      public Param(
        @JsonProperty("name") String name,
        @JsonProperty("value") String value,
        @JsonProperty("href") URI href,
        @JsonProperty("file") URI path
      ) {
        this.name = name;
        this.value = value;
        this.href = href;
        this.path = path;
      }
    }
  }
}
