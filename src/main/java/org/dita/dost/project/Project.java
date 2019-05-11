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

public class Project {

    public static Project build(final ProjectBuilder src, final URI base) {
        final Project project = new Project(
                toStream(src.deliverables)
                        .map(deliverable -> new Deliverable(
                                deliverable.name,
                                deliverable.id,
                                build(deliverable.context, base),
                                deliverable.output,
                                build(deliverable.publication, base)
                        ))
                        .collect(Collectors.toList()),
                toStream(src.includes)
                        .map(include -> new ProjectRef(resolveURI(include, base)))
                        .collect(Collectors.toList()),
                toStream(src.publications)
                        .map(publication -> build(publication, base))
                        .collect(Collectors.toList()),
                toStream(src.contexts)
                        .map(context -> build(context, base))
                        .collect(Collectors.toList())
        );
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
                        .map(param -> new Publication.Param(
                                param.name,
                                param.value,
                                resolveURI(param.href, base),
                                resolvePath(param.path, base))
                        )
                        .collect(Collectors.toList())
        );
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
                                .collect(Collectors.toList())
                )
                        : new Deliverable.Inputs(Collections.emptyList()),
                context.profiles != null
                        ? new Deliverable.Profile(
                        context.profiles.ditavals.stream()
                                .map(ditaval -> new Deliverable.Profile.DitaVal(resolveURI(ditaval, base)))
                                .collect(Collectors.toList())
                )
                        : new Deliverable.Profile(Collections.emptyList())
        );
    }

    public final List<Deliverable> deliverables;
    public final List<ProjectRef> includes;
    public final List<Publication> publications;
    public final List<Context> contexts;

    public Project(List<Deliverable> deliverables,
                   List<ProjectRef> includes,
                   List<Publication> publications,
                   List<Context> contexts) {
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

        public Deliverable(String name,
                           String id,
                           Context context,
                           URI output,
                           Publication publication) {
            this.name = name;
            this.id = id;
            this.context = context;
            this.output = output;
            this.publication = publication;
        }

        public static class Inputs {
            public final List<Input> inputs;

            public Inputs(List<Input> inputs) {
                this.inputs = inputs;
            }

            public static class Input {
                public final URI href;

                public Input(URI href) {
                    this.href = href;
                }
            }
        }

        public static class Profile {
            public final List<DitaVal> ditavals;

            public Profile(List<DitaVal> ditavals) {
                this.ditavals = ditavals;
            }

            public static class DitaVal {
                public final URI href;

                public DitaVal(URI href) {
                    this.href = href;
                }
            }
        }

    }

    public static class ProjectRef {
        public final URI href;

        public ProjectRef(URI href) {
            this.href = href;
        }
    }

    public static class Context {
        public final String name;
        public final String id;
        public final String idref;
        public final Deliverable.Inputs inputs;
        public final Deliverable.Profile profiles;

        public Context(String name,
                       String id,
                       String idref,
                       Deliverable.Inputs inputs,
                       Deliverable.Profile profiles) {
            this.name = name;
            this.id = id;
            this.idref = idref;
            this.inputs = inputs;
            this.profiles = profiles;
        }
    }

    public static class Publication {
        public final String name;
        public final String id;
        public final String idref;
        public final String transtype;
        public final List<Param> params;

        public Publication(String name,
                           String id,
                           String idref,
                           String transtype,
                           List<Param> params) {
            this.name = name;
            this.id = id;
            this.idref = idref;
            this.transtype = transtype;
            this.params = params;
        }

        public static class Param {
            public final String name;
            public final String value;
            public final URI href;
            public final Path path;

            public Param(
                    String name,
                    String value,
                    URI href,
                    Path path) {
                this.name = Objects.requireNonNull(name);
                if (value == null && href == null && path == null) {
                    throw new NullPointerException();
                }
                this.value = value;
                this.href = href;
                this.path = path;
            }
        }
    }
}
