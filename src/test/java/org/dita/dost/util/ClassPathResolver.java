/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2016 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.util;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * URI resolver that support accessing classpath resources.
 */
class ClassPathResolver implements URIResolver {

    public static final String SCHEME = "classpath";

    private final URIResolver parent;

    public ClassPathResolver(final URIResolver uriResolver) {
        parent = uriResolver;
    }

    @Override
    public Source resolve(final String href, final String base) throws TransformerException {
        try {
            final URI abs = new URI(base).resolve(href);
            if (SCHEME.equals(abs.getScheme())) {
                final String path = abs.getPath().substring(1);
                final InputStream in = this.getClass().getClassLoader().getResourceAsStream(path);
                return new StreamSource(in, abs.toString());
            } else {
                return parent.resolve(href, base);
            }
        } catch (URISyntaxException e) {
            throw new TransformerException(e);
        }
    }
}
