/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2019 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.store;

import net.sf.saxon.s9api.XsltTransformer;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.util.XMLUtils;
import org.xml.sax.XMLFilter;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static org.dita.dost.util.Constants.FILE_EXTENSION_TEMP;
import static org.dita.dost.util.URLUtils.setFragment;
import static org.dita.dost.util.URLUtils.toURI;

/**
 * Common base class for store implementations.
 */
public abstract class AbstractStore implements Store {

    static final boolean LOG = false;

    protected final XMLUtils xmlUtils;
    public final File tempDir;
    public final URI tempDirUri;

//    final TransformerFactory tf;

    public AbstractStore(final File tempDir, final XMLUtils xmlUtils) {
        if (!tempDir.isAbsolute()) {
            throw new IllegalArgumentException("Temporary directory " + tempDir + " must be absolute");
        }
        this.tempDirUri = tempDir.toURI();
        this.tempDir = tempDir;
        this.xmlUtils = xmlUtils;
//        tf = TransformerFactory.newInstance();
    }

    @Override
    public URI getUri(final URI path) {
//        if (path.isAbsolute()) {
//            if (path.normalize().toString().startsWith(tempDirUri.toString())) {
//                return URI.create(path.normalize().toString() + ".xxx");
//            } else {
//                return path.normalize();
//            }
//        } else {
//            return URI.create(tempDirUri.resolve(path).normalize().toString() + ".xxx");
//        }
        return tempDirUri.resolve(path).normalize();
    }

    protected boolean isTempFile(final URI f) {
        return f.toString().startsWith(tempDirUri.toString());
    }

//    @Override
//    public void transform(final URI src, final ContentHandler dst) throws DITAOTException {
//        try {
////            final Transformer serializer = tf.newTransformer();
////            serializer.setURIResolver(this);
//
//            final Source source = getSource(src);
//            final SAXDestination destination = new SAXDestination(dst);
////            final Result result = new SAXResult(dst);
//
////            xmlUtils.getProcessor().se
////            serializer.transform(source, result);
//            xmlUtils.getProcessor().writeXdmValue(source, destination);
//        } catch (final RuntimeException e) {
//            throw e;
//        } catch (final Exception e) {
//            throw new DITAOTException("Failed to transform " + src + ": " + e.getMessage(), e);
//        }
//    }

    @Override
    public void transform(final URI input, final URI output, final List<XMLFilter> filters) throws DITAOTException {
        final URI src = input.normalize();
        final URI dst = output.normalize();
        if (src.equals(dst)) {
            transform(src, filters);
        } else {
            transformURI(src, dst, filters);
        }
    }

    @Override
    public void transform(URI src, final List<XMLFilter> filters) throws DITAOTException {
//        assert input.isAbsolute();
//        if (!input.getScheme().equals("file")) {
//            throw new IllegalArgumentException("Only file URI scheme supported: " + input);
//        }
        final URI srcFile = setFragment(src, null);
        final URI dst = toURI(srcFile.toString() + FILE_EXTENSION_TEMP).normalize();
        transformURI(srcFile, dst, filters);
        try {
            move(dst, srcFile);
        } catch (final RuntimeException e) {
            throw e;
        } catch (final IOException e) {
            throw new DITAOTException("Failed to replace " + srcFile + ": " + e.getMessage());
        }
    }

    abstract void transformURI(final URI input, final URI output, final List<XMLFilter> filters) throws DITAOTException;

    abstract void transformUri(final URI input, final URI output, final XsltTransformer transformer) throws DITAOTException;

//    @Override
//    public void transform(final URI src, final URI outputFile, final List<XMLFilter> filters) throws DITAOTException {
//        try {
//            final IdentityTransformer serializer = (IdentityTransformer) tf.newTransformer();
//            serializer.getConfiguration().setErrorListener(new ErrorGatherer(new ArrayList<>()));
//            final URIResolver resolver = new DelegatingURIResolver(CatalogUtils.getCatalogResolver(), this);
//            serializer.setURIResolver(resolver);
//
//            final Source source = getSource(src);
//            final Result result = getResult(outputFile);
//
//            ContentHandler handler;
//            final TransformerHandler th = ((SAXTransformerFactory) tf).newTransformerHandler();
//            th.getTransformer().setURIResolver(this);
//            th.setResult(result);
//            handler = th;
//
//            final ArrayList<XMLFilter> fs = new ArrayList<>(filters);
//            Collections.reverse(fs);
//            for (final XMLFilter filter : fs) {
//                filter.setContentHandler(handler);
//                handler = (ContentHandler) filter;
//            }
//
//            final Result intermediate = new SAXResult(handler);
//
//            serializer.transform(source, intermediate);
//        } catch (final RuntimeException e) {
//            throw e;
//        } catch (final Exception e) {
//            throw new DITAOTException("Failed to transform " + src + ": " + e.getMessage(), e);
//        }
//    }
}
