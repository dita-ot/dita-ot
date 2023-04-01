/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2019 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.project;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record Project(
    List<Deliverable> deliverables,
    List<ProjectRef> includes,
    List<Publication> publications,
    List<Context> contexts) {

  public static Project build(final ProjectBuilder src, final URI base) {
    final Project project =
        new Project(
            toStream(src.deliverables)
                .map(
                    deliverable ->
                        new Deliverable(
                            deliverable.name,
                            deliverable.id,
                            build(deliverable.context, base),
                            deliverable.output,
                            build(deliverable.publication, base)))
                .collect(Collectors.toList()),
            toStream(src.includes)
                .map(include -> new ProjectRef(resolveURI(include, base)))
                .collect(Collectors.toList()),
            toStream(src.publications)
                .map(publication -> build(publication, base))
                .collect(Collectors.toList()),
            toStream(src.contexts)
                .map(context -> build(context, base))
                .collect(Collectors.toList()));
    return project;
  }

  private static <T> Stream<T> toStream(List<T> src) {
    return src != null ? src.stream() : Stream.empty();
  }

  private static URI resolveURI(final URI file, final URI base) {
    if (file == null) {
      return null;
    }
    return base != null ? base.resolve(file) : file;
  }

  private static Path resolvePath(final URI file, final URI base) {
    if (file == null) {
      return null;
    }
    return base != null ? Paths.get(base.resolve(file)) : Paths.get(file);
  }

  private static Publication build(final ProjectBuilder.Publication publication, final URI base) {
    if (publication == null) {
      return null;
    }
    return new Publication(
        publication.name,
        publication.id,
        publication.idref,
        publication.transtype,
        toStream(publication.params)
            .map(
                param ->
                    new Publication.Param(
                        param.name,
                        param.value,
                        resolveURI(param.href, base),
                        resolvePath(param.path, base)))
            .collect(Collectors.toList()),
        new Deliverable.Profile(
            publication.profiles != null
                ? publication.profiles.ditavals.stream()
                    .map(ditaval -> new Deliverable.Profile.DitaVal(resolveURI(ditaval, base)))
                    .collect(Collectors.toList())
                : Collections.emptyList()));
  }

  private static Context build(final ProjectBuilder.Context context, final URI base) {
    if (context == null) {
      return null;
    }
    return new Context(
        context.name,
        context.id,
        context.idref,
        context.input != null
            ? new Deliverable.Inputs(
                context.input.stream()
                    .map(input -> new Deliverable.Inputs.Input(resolveURI(input, base)))
                    .collect(Collectors.toList()))
            : new Deliverable.Inputs(Collections.emptyList()),
        new Deliverable.Profile(
            context.profiles != null
                ? context.profiles.ditavals.stream()
                    .map(ditaval -> new Deliverable.Profile.DitaVal(resolveURI(ditaval, base)))
                    .collect(Collectors.toList())
                : Collections.emptyList()));
  }

  public record Deliverable(
      String name, String id, Context context, URI output, Publication publication) {
    public record Inputs(List<Input> inputs) {
      public record Input(URI href) {}
    }

    public record Profile(List<DitaVal> ditavals) {
      public record DitaVal(URI href) {}
    }
  }

  public record ProjectRef(URI href) {}

  public record Context(
      String name,
      String id,
      String idref,
      Deliverable.Inputs inputs,
      Deliverable.Profile profiles) {}

  public record Publication(
      String name,
      String id,
      String idref,
      String transtype,
      List<Param> params,
      Deliverable.Profile profiles) {

    public record Param(String name, String value, URI href, Path path) {
      public Param {
        Objects.requireNonNull(name);
        if (value == null && href == null && path == null) {
          throw new NullPointerException();
        }
      }
    }
  }
}
