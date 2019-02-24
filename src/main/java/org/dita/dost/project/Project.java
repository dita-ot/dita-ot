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

    @JsonCreator
    public Project(@JsonProperty("deliverables") List<Deliverable> deliverables) {
        this.deliverables = deliverables;
    }

    public static class Deliverable {
        @JacksonXmlProperty(isAttribute = true)
        public final String name;
        @JacksonXmlElementWrapper(useWrapping = false)
        public final Inputs inputs;
        @JacksonXmlElementWrapper(useWrapping = false)
        public final Profile profiles;
        @JacksonXmlElementWrapper(useWrapping = false)
        public final Publication publications;

        @JsonCreator
        public Deliverable(@JsonProperty("name") String name,
                           @JsonProperty("inputs") Inputs inputs,
                           @JsonProperty("profiles") Profile profiles,
                           @JsonProperty("publications") Publication publications) {
            this.name = name;
            this.inputs = inputs;
            this.profiles = profiles;
            this.publications = publications;
        }

        public static class Inputs {
            @JacksonXmlProperty(isAttribute = true)
            public final String name;
            @JacksonXmlProperty(isAttribute = true)
            public final String ref;
            @JacksonXmlProperty(localName = "input")
            @JacksonXmlElementWrapper(useWrapping = false)
            public final List<Input> inputs;

            @JsonCreator
            public Inputs(@JsonProperty("name") String name,
                          @JsonProperty("ref") String ref,
                          @JsonProperty("inputs") List<Input> inputs) {
                this.name = name;
                this.ref = ref;
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
            @JacksonXmlProperty(isAttribute = true)
            public final String name;
            @JacksonXmlProperty(isAttribute = true)
            public final String ref;
            @JacksonXmlElementWrapper(useWrapping = false)
            @JacksonXmlProperty(localName = "ditaval")
            public final List<DitaVal> ditavals;

            @JsonCreator
            public Profile(@JsonProperty("name") String name,
                           @JsonProperty("ref") String ref,
                           @JsonProperty("ditavals") List<DitaVal> ditavals) {
                this.name = name;
                this.ref = ref;
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
            @JacksonXmlElementWrapper(useWrapping = false)
            @JacksonXmlProperty(localName = "transtype")
            public final List<String> transtypes;
            @JacksonXmlElementWrapper(useWrapping = false)
            @JacksonXmlProperty(localName = "param")
            public final List<Param> params;

            @JsonCreator
            public Publication(@JsonProperty("transtypes") List<String> transtypes,
                               @JsonProperty("params") List<Param> params) {
                this.transtypes = transtypes;
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
}
