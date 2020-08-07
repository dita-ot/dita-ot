/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2004, 2005 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.

 */
package org.dita.dost.module;

import net.sf.saxon.s9api.*;
import net.sf.saxon.trans.UncheckedXPathException;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.util.CatalogUtils;
import org.dita.dost.util.DelegatingURIResolver;
import org.dita.dost.util.Job.FileInfo;
import org.dita.dost.util.XMLUtils;
import org.dita.dost.writer.DitaLinksWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.URLUtils.*;
import static org.dita.dost.util.XMLUtils.toErrorListener;

/**
 * MoveLinksModule implements move links step in preprocess. It reads the map links
 * information from the input map and inserts the links into topics.
 */
final class MoveLinksModule extends AbstractPipelineModuleImpl {

    /**
     * execution point of MoveLinksModule.
     *
     * @param input input parameters and resources
     * @return always {@code null}
     * @throws DITAOTException if process fails
     */
    @Override
    public AbstractPipelineOutput execute(final AbstractPipelineInput input) throws DITAOTException {
        final FileInfo fi = job.getFileInfo(f -> f.isInput).iterator().next();
        if (!ATTR_FORMAT_VALUE_DITAMAP.equals(fi.format)) {
            return null;
        }
        final File inputFile = new File(job.tempDirURI.resolve(fi.uri));
        final File styleFile = new File(input.getAttribute(ANT_INVOKER_EXT_PARAM_STYLE));

        Document doc;
        try {
            final XsltCompiler xsltCompiler = xmlUtils.getProcessor().newXsltCompiler();

            final XsltTransformer transformer = xsltCompiler.compile(new StreamSource(styleFile)).load();
            transformer.setErrorListener(toErrorListener(logger));
            transformer.setURIResolver(new DelegatingURIResolver(CatalogUtils.getCatalogResolver(), job.getStore()));

            if (input.getAttribute("include.rellinks") != null) {
                transformer.setParameter(new QName("include.rellinks"),
                        XdmItem.makeValue(input.getAttribute("include.rellinks")));
            }
            transformer.setParameter(new QName("INPUTMAP"), XdmItem.makeValue(job.getInputMap()));

            final Source source = job.getStore().getSource(inputFile.toURI());
            doc = XMLUtils.getDocumentBuilder().newDocument();
            final DOMDestination result = new DOMDestination(doc);

            transformer.setSource(source);
            transformer.setDestination(result);

            transformer.transform();
        } catch (final UncheckedXPathException e) {
            throw new DITAOTException("Failed to read links from " + inputFile, e);
        } catch (final RuntimeException e) {
            throw e;
        } catch (final SaxonApiException e) {
            throw new DITAOTException("Failed to read links from " + inputFile + ": " + e.getMessage(), e);
        } catch (final Exception e) {
            throw new DITAOTException("Failed to read links from " + inputFile + ": " + e.getMessage(), e);
        }

        final Map<File, Map<String, Element>> mapSet = getMapping(doc);

        if (!mapSet.isEmpty()) {
            final DitaLinksWriter linkInserter = new DitaLinksWriter();
            linkInserter.setLogger(logger);
            linkInserter.setJob(job);
            for (final Map.Entry<File, Map<String, Element>> entry: mapSet.entrySet()) {
                final URI uri = inputFile.toURI().resolve(toURI(entry.getKey().getPath()));
                logger.info("Processing " + uri);
                linkInserter.setLinks(entry.getValue());
                linkInserter.setCurrentFile(uri);
                try {
                    linkInserter.write(new File(uri));
                } catch (final DITAOTException e) {
                    logger.error("Failed to insert links: " + e.getMessage(), e);
                }
            }
        }
        return null;
    }

    private Map<File, Map<String, Element>> getMapping(Document doc) {
        final Map<File, Map<String, Element>> map = new HashMap<>();
        final NodeList maplinks = doc.getDocumentElement().getChildNodes();
        for (int i = 0; i < maplinks.getLength(); i++) {
            final Node n = maplinks.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                final Element maplink = (Element) n;
                final URI href = toURI(maplink.getAttribute(ATTRIBUTE_NAME_HREF));
                final File path = toFile(stripFragment(href));
                String fragment = href.getFragment();
                if (fragment == null) {
                    fragment = SHARP;
                }
                Map<String, Element> m = map.computeIfAbsent(path, k -> new HashMap<>());
                Element stub = m.computeIfAbsent(fragment, k -> doc.createElement("stub"));
                Node c = maplink.getFirstChild();
                while (c != null) {
                    final Node nextSibling = c.getNextSibling();
                    stub.appendChild(maplink.removeChild(c));
                    c = nextSibling;
                }
            }
        }
        return map;
    }

}
