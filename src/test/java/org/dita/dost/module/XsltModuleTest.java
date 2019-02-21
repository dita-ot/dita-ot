/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2019 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.module;

import net.sf.saxon.functions.FunctionLibraryList;
import net.sf.saxon.jaxp.SaxonTransformerFactory;
import net.sf.saxon.lib.CollationURIResolver;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.SymbolicName;
import org.junit.Before;
import org.junit.Test;

import javax.xml.transform.TransformerFactory;

import static org.junit.Assert.*;

public class XsltModuleTest {

    private XsltModule xsltModule;
    private SaxonTransformerFactory tf;

    @Before
    public void setUp() {
        xsltModule = new XsltModule();
        tf = (SaxonTransformerFactory) TransformerFactory.newInstance("net.sf.saxon.jaxp.SaxonTransformerFactory", getClass().getClassLoader());
    }

    @Test
    public void configureCollationResolvers() {
        xsltModule.configureCollationResolvers(tf);
        final CollationURIResolver collationURIResolver = tf.getConfiguration().getCollationURIResolver();
        assertTrue(collationURIResolver.getClass().isAssignableFrom(DelegatingCollationUriResolverTest.class));
    }

    @Test
    public void configureExtensions() {
        xsltModule.configureExtensions(tf);
        final SymbolicName.F functionName = new SymbolicName.F(new StructuredQName("x", "y", "z"), 0);
        assertTrue(tf.getConfiguration().getIntegratedFunctionLibrary().isAvailable(functionName));
    }
}