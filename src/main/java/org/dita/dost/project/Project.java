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
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.net.URI;
import java.util.List;

@JacksonXmlRootElement(localName = "project")
public class Project {
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "deliverable")
    public final List<Deliverable> deliverables;
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "include")
    public final List<ProjectRef> includes;
    @JacksonXmlProperty(localName = "publication")
    @JacksonXmlElementWrapper(useWrapping = false)
    public final List<Deliverable.Publication> publications;

    @JsonCreator
    public Project(@JsonProperty("deliverables") List<Deliverable> deliverables,
                   @JsonProperty("includes") List<ProjectRef> includes,
                   @JsonProperty("publications") List<Deliverable.Publication> publications) {
        this.deliverables = deliverables;
        this.includes = includes;
        this.publications = publications;
    }

    public static class Deliverable {
        @JacksonXmlProperty(isAttribute = true)
        public final String name;
        @JacksonXmlElementWrapper(localName = "context")
        public final Context context;
        @JacksonXmlElementWrapper(localName = "output")
        public final URI output;
        @JacksonXmlProperty(localName = "publication")
        @JacksonXmlElementWrapper(useWrapping = false)
        public final Publication publication;

        @JsonCreator
        public Deliverable(@JsonProperty("name") String name,
                           @JsonProperty("context") Context context,
                           @JsonProperty("output") URI output,
                           @JsonProperty("publication") Publication publication) {
            this.name = name;
            this.context = context;
            this.output = output;
            this.publication = publication;
        }

        public static class Context {
            @JacksonXmlProperty(isAttribute = true)
            public final String name;
            @JacksonXmlProperty(isAttribute = true)
            public final String id;
            @JacksonXmlElementWrapper(useWrapping = false)
            public final Inputs inputs;
            @JacksonXmlProperty(localName = "profile")
            @JacksonXmlElementWrapper(useWrapping = false)
            public final Profile profiles;

            @JsonCreator
            public Context(@JsonProperty("name") String name,
                           @JsonProperty("id") String id,
                           @JsonProperty("inputs") Inputs inputs,
                           @JsonProperty("profiles") Profile profiles) {
                this.name = name;
                this.id = id;
                this.inputs = inputs;
                this.profiles = profiles;
            }
        }

        public static class Inputs {
            //            @JacksonXmlProperty(isAttribute = true)
//            public final String name;
//            @JacksonXmlProperty(isAttribute = true)
//            public final String ref;
            @JacksonXmlProperty(localName = "input")
            @JacksonXmlElementWrapper(useWrapping = false)
            public final List<Input> inputs;

            @JsonCreator
            public Inputs(//@JsonProperty("name") String name,
//                          @JsonProperty("ref") String ref,
                          @JsonProperty("inputs") List<Input> inputs) {
//                this.name = name;
//                this.ref = ref;
                this.inputs = inputs;
            }

            public static class Input {
                @JacksonXmlProperty(isAttribute = true)
                public final URI href;

                @JsonCreator
                public Input(@JsonProperty("href") URI href) {
                    this.href = href;
                }
            }
        }

        public static class Profile {
            //            @JacksonXmlProperty(isAttribute = true)
//            public final String name;
//            @JacksonXmlProperty(isAttribute = true)
//            public final String ref;
            @JacksonXmlElementWrapper(useWrapping = false)
            @JacksonXmlProperty(localName = "ditaval")
            public final List<DitaVal> ditavals;

            @JsonCreator
            public Profile(//@JsonProperty("name") String name,
//                           @JsonProperty("ref") String ref,
                           @JsonProperty("ditavals") List<DitaVal> ditavals) {
//                this.name = name;
//                this.ref = ref;
                this.ditavals = ditavals;
            }

            public static class DitaVal {
                @JacksonXmlProperty(isAttribute = true)
                public final URI href;

                @JsonCreator
                public DitaVal(@JsonProperty("href") URI href) {
                    this.href = href;
                }
            }
        }

        public static class Publication {
            @JacksonXmlProperty(isAttribute = true)
            public final String name;
            @JacksonXmlProperty(isAttribute = true)
            public final String id;
            @JacksonXmlProperty(isAttribute = true)
            public final String idref;
            @JacksonXmlProperty(isAttribute = true)
            public final String transtype;
            @JacksonXmlElementWrapper(useWrapping = false)
            @JacksonXmlProperty(localName = "param")
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
                @JacksonXmlProperty(isAttribute = true)
                public final String name;
                @JacksonXmlProperty(isAttribute = true)
                public final String value;
                @JacksonXmlProperty(isAttribute = true)
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

    public static class ProjectRef {
        @JacksonXmlProperty(isAttribute = true)
        public final URI href;

        @JsonCreator
        public ProjectRef(@JsonProperty("href") URI href) {
            this.href = href;
        }
    }
}
