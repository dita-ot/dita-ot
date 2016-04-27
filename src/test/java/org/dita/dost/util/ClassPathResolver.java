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
                final InputStream in = this.getClass().getClassLoader().getResourceAsStream(abs.getPath().substring(1));
                return new StreamSource(in, abs.toString());
            } else {
                return parent.resolve(href, base);
            }
        } catch (URISyntaxException e) {
            throw new TransformerException(e);
        }
    }
}
