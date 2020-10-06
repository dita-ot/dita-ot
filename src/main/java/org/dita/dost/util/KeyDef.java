/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2013 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.util;

import net.sf.saxon.s9api.XdmNode;

import java.net.URI;

import static org.dita.dost.util.Constants.*;

/**
 * Key definition.
 */
public class KeyDef {

    /** Space delimited list of key names */
    public final String keys;
    public URI href;
    public final String scope;
    public final URI source;
    public final XdmNode element;
    public final String format;

    /**
     * Construct new key definition.
     *
     * @param keys key name
     * @param href href URI, may be {@code null}
     * @param scope link scope, may be {@code null}
     * @param source key definition source, may be {@code null}
     */
    public KeyDef(final String keys, final URI href, final String scope, final String format, final URI source, final XdmNode element) {
        //assert href.isAbsolute();
        this.keys = keys;
        this.href = href == null || href.toString().isEmpty() ? null : href;
        this.scope = scope == null ? ATTR_SCOPE_VALUE_LOCAL : scope;
        this.format = format == null ? ATTR_FORMAT_VALUE_DITA : format;
        this.source = source;
        this.element = element;
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
