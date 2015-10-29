/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.module;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.util.CatalogUtils;
import org.dita.dost.writer.DitaLinksWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.URLUtils.*;

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
        final File inputFile = new File(job.tempDir, input.getAttribute(ANT_INVOKER_PARAM_INPUTMAP));
        final File styleFile = new File(input.getAttribute(ANT_INVOKER_EXT_PARAM_STYLE));

        Document doc;
        InputStream in = null;
        try {
            doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            final Transformer transformer = TransformerFactory.newInstance().newTransformer(new StreamSource(styleFile));
            transformer.setURIResolver(CatalogUtils.getCatalogResolver());
            if (input.getAttribute("include.rellinks") != null) {
                transformer.setParameter("include.rellinks", input.getAttribute("include.rellinks"));
            }
            in = new BufferedInputStream(new FileInputStream(inputFile));
            final Source source = new StreamSource(in);
            source.setSystemId(inputFile.toURI().toString());
            final DOMResult result = new DOMResult(doc);
            transformer.transform(source, result);
        } catch (final RuntimeException e) {
            throw e;
        } catch (final Exception e) {
            throw new DITAOTException("Failed to read links from " + inputFile + ": " + e.getMessage(), e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (final IOException e) {
                    logger.error("Failed to close input stream: " + e.getMessage(), e);
                }
            }
        }

        final Map<File, Map<String, Element>> mapSet = getMapping(doc);

        if (!mapSet.isEmpty()) {
            final DitaLinksWriter linkInserter = new DitaLinksWriter();
            linkInserter.setLogger(logger);
            linkInserter.setJob(job);
            for (final Map.Entry<File, Map<String, Element>> entry: mapSet.entrySet()) {
                final File f = new File(job.tempDir, entry.getKey().getPath());
                logger.info("Processing " + f);
                linkInserter.setLinks(entry.getValue());
                try {
                    linkInserter.write(f);
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
                Map<String, Element> m = map.get(path);
                if (m == null) {
                    m = new HashMap<>();
                    map.put(path, m);
                }
                Element stub = m.get(fragment);
                if (stub == null) {
                    stub = doc.createElement("stub");
                    m.put(fragment, stub);
                }
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
