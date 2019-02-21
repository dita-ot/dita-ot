/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2019 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.module;

import net.sf.saxon.Configuration;
import net.sf.saxon.lib.CollationURIResolver;
import net.sf.saxon.lib.StringCollator;
import net.sf.saxon.trans.XPathException;
import org.dita.dost.module.saxon.DelegatingCollationUriResolver;

public class DelegatingCollationUriResolverTest implements DelegatingCollationUriResolver {

    @Override
    public void setBaseResolver(CollationURIResolver baseResolver) {

    }

    @Override
    public StringCollator resolve(String collationURI, Configuration config) throws XPathException {
        return null;
    }
}
