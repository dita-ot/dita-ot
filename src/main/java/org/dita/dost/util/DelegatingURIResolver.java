/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2019 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.util;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

/**
 * URI resolver that folds over multiple resolvers and returns the final result.
 */
public class DelegatingURIResolver implements URIResolver {

    private final URIResolver[] resolvers;

    public DelegatingURIResolver(URIResolver... resolvers) {
        this.resolvers = resolvers;
    }

    @Override
    public Source resolve(String href, String base) throws TransformerException {
//        System.out.println(" DelegatingURIResolver resolve: " + href);
        Source src = null;
        for (final URIResolver resolver : resolvers) {
            // XXX: This will create a redundant XMLReader for each call to resolve
            final Source res = resolver.resolve(src != null ? src.getSystemId() : href, base);
            if (res != null) {
                src = res;
            }
        }
        return src;
    }
}
