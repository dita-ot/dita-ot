/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2019 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.project;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.net.URI;
import java.util.List;

@JacksonXmlRootElement(localName = "project")
public class Project {
    @JacksonXmlProperty(localName = "deliverable")
    @JacksonXmlElementWrapper(useWrapping = false)
    public final List<Deliverable> deliverables;
    @JacksonXmlProperty(localName = "include")
    @JacksonXmlElementWrapper(useWrapping = false)
    public final List<ProjectRef> includes;
    @JacksonXmlProperty(localName = "publication")
    @JacksonXmlElementWrapper(useWrapping = false)
    public final List<Publication> publications;
    @JacksonXmlProperty(localName = "context")
    @JacksonXmlElementWrapper(useWrapping = false)
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
        @JacksonXmlProperty(isAttribute = true)
        public final String name;
        @JacksonXmlProperty(localName = "context")
//        @JacksonXmlElementWrapper(useWrapping = false)
        public final Context context;
//        @JacksonXmlElementWrapper(localName = "output")
        public final URI output;
//        @JacksonXmlProperty(localName = "publication")
//        @JacksonXmlElementWrapper(useWrapping = false)
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
            @JacksonXmlProperty(localName = "ditaval")
            @JacksonXmlElementWrapper(useWrapping = false)
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

    }

    public static class ProjectRef {
        @JacksonXmlProperty(isAttribute = true)
        public final URI href;

        @JsonCreator
        public ProjectRef(@JsonProperty("href") URI href) {
            this.href = href;
        }
    }

    public static class Context {
        @JacksonXmlProperty(isAttribute = true)
        public final String name;
        @JacksonXmlProperty(isAttribute = true)
        public final String id;
        @JacksonXmlProperty(isAttribute = true)
        public final String idref;
//        @JacksonXmlProperty(localName = "input")
        //            @JacksonXmlElementWrapper(useWrapping = false)
        public final Deliverable.Inputs inputs;
//        @JacksonXmlProperty(localName = "profile")
//            @JacksonXmlElementWrapper(useWrapping = false)
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
        @JacksonXmlProperty(isAttribute = true)
        public final String name;
        @JacksonXmlProperty(isAttribute = true)
        public final String id;
        @JacksonXmlProperty(isAttribute = true)
        public final String idref;
        @JacksonXmlProperty(isAttribute = true)
        public final String transtype;
        @JacksonXmlProperty(localName = "param")
        @JacksonXmlElementWrapper(useWrapping = false)
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
