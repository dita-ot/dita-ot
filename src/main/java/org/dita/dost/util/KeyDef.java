/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2013 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.util;

import static org.dita.dost.util.Constants.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Collection;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.dita.dost.exception.DITAOTException;
import org.w3c.dom.Element;

/**
 * Key definition.
 */
public class KeyDef {

    public static final String ELEMENT_STUB = "stub";
    private static final String ELEMENT_KEYDEF = "keydef";
    private static final String ATTRIBUTE_SOURCE = "source";
    private static final String ATTRIBUTE_HREF = "href";
    private static final String ATTRIBUTE_SCOPE = "scope";
    private static final String ATTRIBUTE_FORMAT = "format";
    private static final String ATTRIBUTE_KEYS = "keys";
    private static final String ATTRIBUTE_FILTERED = "filtered";
    

    /** Space delimited list of key names */
    public final String keys;
    public final URI href;
    public final String scope;
    public final URI source;
    public final Element element;
    public final String format;
    private boolean filtered;

    /**
     * Construct new key definition.
     *
     * @param keys key name
     * @param href href URI, may be {@code null}
     * @param scope link scope, may be {@code null}
     * @param source key definition source, may be {@code null}
     */
    public KeyDef(String keys, final URI href, final String scope, final String format, final URI source, final Element element) {
        this.keys = keys;
        this.href = href == null || href.toString().isEmpty() ? null : href;
        this.scope = scope == null ? ATTR_SCOPE_VALUE_LOCAL : scope;
        this.format = format == null ? ATTR_FORMAT_VALUE_DITA : format;
        this.source = source;
        this.element = element;
    }

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public KeyDef(@JsonProperty("keys")final String keys, @JsonProperty("href")final URI href,
                  @JsonProperty("scope")final String scope, @JsonProperty("format")final String format,
                  @JsonProperty("source")final URI source, @JsonProperty("element")final Element element,
                  @JsonProperty("filtered")boolean filtered) {
        this.keys = keys;
        this.href = href == null || href.toString().isEmpty() ? null : href;
        this.scope = scope == null ? ATTR_SCOPE_VALUE_LOCAL : scope;
        this.format = format == null ? ATTR_FORMAT_VALUE_DITA : format;
        this.source = source;
        this.element = element;
        this.filtered = filtered;
    }
    
    public void setFiltered(boolean filtered) {
    	this.filtered = filtered;
    }
    
    public boolean isFiltered() {
		return filtered;
	}

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder().append(keys).append(EQUAL);
        if (href != null) {
            buf.append(href.toString());
        }
        if (scope != null) {
            buf.append(LEFT_BRACKET).append(scope).append(RIGHT_BRACKET);
        }
        if (source != null) {
            buf.append(LEFT_BRACKET).append(source.toString()).append(RIGHT_BRACKET);
        }
        return buf.toString();
    }

    /**
     * Write key definition XML configuration file
     *
     * @param keydefFile key definition file
     * @param keydefs list of key definitions
     * @throws DITAOTException if writing configuration file failed
     */
    public static void writeKeydef(final File keydefFile, final Collection<KeyDef> keydefs) throws DITAOTException {
        XMLStreamWriter writer = null;
        try (OutputStream out = new FileOutputStream(keydefFile)) {
            writer = XMLOutputFactory.newInstance().createXMLStreamWriter(out, "UTF-8");
            writer.writeStartDocument();
            writer.writeStartElement(ELEMENT_STUB);
            for (final KeyDef keydef : keydefs) {
                writer.writeStartElement(ELEMENT_KEYDEF);
                writer.writeAttribute(ATTRIBUTE_KEYS, keydef.keys);
                if (keydef.href != null) {
                    writer.writeAttribute(ATTRIBUTE_HREF, keydef.href.toString());
                }
                if (keydef.scope != null) {
                    writer.writeAttribute(ATTRIBUTE_SCOPE, keydef.scope);
                }
                if (keydef.format != null) {
                    writer.writeAttribute(ATTRIBUTE_FORMAT, keydef.format);
                }
                if (keydef.source != null) {
                    writer.writeAttribute(ATTRIBUTE_SOURCE, keydef.source.toString());
                }
                writer.writeAttribute(ATTRIBUTE_FILTERED, Boolean.toString(keydef.filtered));
                writer.writeEndElement();
            }
            writer.writeEndDocument();
        } catch (final XMLStreamException | IOException e) {
            throw new DITAOTException("Failed to write key definition file " + keydefFile + ": " + e.getMessage(), e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (final XMLStreamException e) {
                }
            }
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((href == null) ? 0 : href.hashCode());
        result = prime * result + ((keys == null) ? 0 : keys.hashCode());
        result = prime * result + ((scope == null) ? 0 : scope.hashCode());
        result = prime * result + ((format == null) ? 0 : format.hashCode());
        result = prime * result + ((source == null) ? 0 : source.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof KeyDef)) {
            return false;
        }
        KeyDef other = (KeyDef) obj;
        if (href == null) {
            if (other.href != null) {
                return false;
            }
        } else if (!href.equals(other.href)) {
            return false;
        }
        if (keys == null) {
            if (other.keys != null) {
                return false;
            }
        } else if (!keys.equals(other.keys)) {
            return false;
        }
        if (scope == null) {
            if (other.scope != null) {
                return false;
            }
        } else if (!scope.equals(other.scope)) {
            return false;
        }
        if (format == null) {
            if (other.format != null) {
                return false;
            }
        } else if (!format.equals(other.format)) {
            return false;
        }
        if (source == null) {
            if (other.source != null) {
                return false;
            }
        } else if (!source.equals(other.source)) {
            return false;
        }
        return true;
    }

}
