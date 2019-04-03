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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
                        .map(include -> new ProjectRef(resolve(include, base)))
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

    private static URI resolve(final URI file, final URI base) {
        return file != null && base != null ? base.resolve(file) : file;
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
                        .map(param -> new Publication.Param(param.name, param.value, resolve(param.href, base)))
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
                Optional.ofNullable(context.input)
                        .map(input -> new Deliverable.Inputs.Input(resolve(input, base)))
                        .map(Collections::singletonList)
                        .map(Deliverable.Inputs::new)
                        .orElse(null),
                context.profiles != null
                        ? new Deliverable.Profile(
                        context.profiles.ditavals.stream()
                                .map(ditaval -> new Deliverable.Profile.DitaVal(resolve(ditaval, base)))
                                .collect(Collectors.toList())
                )
                        : null
        );
    }

    public final List<Deliverable> deliverables;
    public final List<ProjectRef> includes;
    public final List<Publication> publications;
    public final List<Context> contexts;

    @JsonCreator
    public Project(@JsonProperty("deliverables") List<Deliverable> deliverables,
                   @JsonProperty("includes") List<ProjectRef> includes,
                   @JsonProperty("publications") List<Publication> publications,
                   @JsonProperty("contexts") List<Context> contexts) {
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
        public Deliverable(@JsonProperty("name") String name,
                           @JsonProperty("id") String id,
                           @JsonProperty("context") Context context,
                           @JsonProperty("output") URI output,
                           @JsonProperty("publication") Publication publication) {
            this.name = name;
            this.id = id;
            this.context = context;
            this.output = output;
            this.publication = publication;
        }

        public static class Inputs {
            public final List<Input> inputs;

            @JsonCreator
            public Inputs(@JsonProperty("inputs") List<Input> inputs) {
                this.inputs = inputs;
            }

            public static class Input {
                public final URI href;

                @JsonCreator
                public Input(@JsonProperty("href") URI href) {
                    this.href = href;
                }
            }
        }

        public static class Profile {
            public final List<DitaVal> ditavals;

            @JsonCreator
            public Profile(@JsonProperty("ditavals") List<DitaVal> ditavals) {
                this.ditavals = ditavals;
            }

            public static class DitaVal {
                public final URI href;

                @JsonCreator
                public DitaVal(@JsonProperty("href") URI href) {
                    this.href = href;
                }
            }
        }

    }

    public static class ProjectRef {
        public final URI href;

        @JsonCreator
        public ProjectRef(@JsonProperty("href") URI href) {
            this.href = href;
        }
    }

    public static class Context {
        public final String name;
        public final String id;
        public final String idref;
        public final Deliverable.Inputs inputs;
        public final Deliverable.Profile profiles;

        @JsonCreator
        public Context(@JsonProperty("name") String name,
                       @JsonProperty("id") String id,
                       @JsonProperty("idref") String idref,
                       @JsonProperty("inputs") Deliverable.Inputs inputs,
                       @JsonProperty("profiles") Deliverable.Profile profiles) {
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

        @JsonCreator
        public Publication(@JsonProperty("name") String name,
                           @JsonProperty("id") String id,
                           @JsonProperty("idref") String idref,
                           @JsonProperty("transtype") String transtype,
                           @JsonProperty("params") List<Param> params) {
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

            @JsonCreator
            public Param(
                    @JsonProperty("name") String name,
                    @JsonProperty("value") String value,
                    @JsonProperty("href") URI href) {
                this.name = name;
                this.value = value;
                this.href = href;
            }
        }
    }
}
