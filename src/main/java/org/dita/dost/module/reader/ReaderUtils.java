/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2019 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.module.reader;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.util.DitaClass;
import org.dita.dost.util.Job;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.net.URI;
import java.util.List;

import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.Constants.ATTRIBUTE_NAME_HREF;

public class ReaderUtils {

    public static final String GEN_MAP = "_dummy.ditamap";
    public static final DitaClass GENMAP = new DitaClass("+ map/map ditaot-d/genmap ");

    private Job job;
    private DITAOTLogger logger;
    private TempFileNameScheme tempFileNameScheme;

    /**
     * Combines multiple inputs into a single root map.
     *
     * @throws DITAOTException if writing output fails
     */
    public void combine(final URI rootFile, final List<URI> rootFiles) throws DITAOTException {
        final URI rootTemp = tempFileNameScheme.generateTempFileName(rootFile);
        if (rootFiles.size() > 1) {
            final URI rootTempAbs = job.tempDirURI.resolve(rootTemp);
            logger.info("Writing " + rootTempAbs);
            try {
                final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                dbf.setNamespaceAware(true);
                final Document doc = dbf.newDocumentBuilder().newDocument();

                doc.appendChild(doc.createProcessingInstruction(PI_WORKDIR_TARGET_URI, job.tempDirURI.toString()));
                doc.appendChild(doc.createProcessingInstruction(PI_PATH2PROJ_TARGET_URI, "./"));
                doc.appendChild(doc.createProcessingInstruction(PI_PATH2ROOTMAP_TARGET_URI, "./"));

                final Element root = doc.createElement(GENMAP.localName);
                root.setAttribute(ATTRIBUTE_NAME_CLASS, GENMAP.toString());
                root.setAttribute(ATTRIBUTE_NAME_DOMAINS, "(map ditaot-d)");
                root.setAttributeNS(DITA_NAMESPACE, ATTRIBUTE_PREFIX_DITAARCHVERSION + COLON + ATTRIBUTE_NAME_DITAARCHVERSION, "1.3");
                for (final URI file : rootFiles) {
                    final Job.FileInfo fi = job.getFileInfo(file);
                    final URI hrefTempAbs = job.tempDirURI.resolve(fi.uri);
                    final URI href = rootTempAbs.resolve(".").relativize(hrefTempAbs);

                    final Element ref = doc.createElement(MAP_TOPICREF.localName);
                    ref.setAttribute(ATTRIBUTE_NAME_CLASS, MAP_TOPICREF.toString());
                    ref.setAttribute(ATTRIBUTE_NAME_FORMAT, fi.format);
                    ref.setAttribute(ATTRIBUTE_NAME_HREF, href.toString());
                    root.appendChild(ref);
                }
                doc.appendChild(root);

                final Transformer serializer = TransformerFactory.newInstance().newTransformer();
                serializer.transform(new DOMSource(doc), new StreamResult(rootTempAbs.toString()));
            } catch (ParserConfigurationException | TransformerConfigurationException e) {
                throw new RuntimeException(e);
            } catch (TransformerException e) {
                throw new DITAOTException("Failed to serialize root file: " + e.getMessage(), e);
            }
        }
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public void setLogger(DITAOTLogger logger) {
        this.logger = logger;
    }

    public void setTempFileNameScheme(TempFileNameScheme tempFileNameScheme) {
        this.tempFileNameScheme = tempFileNameScheme;
    }
}
