/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.module;

import static org.dita.dost.util.Constants.*;

import java.io.*;
import java.util.Map;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.reader.MapLinksReader;
import org.dita.dost.writer.DitaLinksWriter;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * MoveLinksModule implements move links step in preprocess. It reads the map links
 * information from the temp file "maplinks.unordered" and move these information
 * to different corresponding dita topic file.
 * 
 * @author Zhang, Yuan Peng
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

        final MapLinksReader linkReader = new MapLinksReader();
        linkReader.setLogger(logger);
        linkReader.setJob(job);

        InputStream in = null;
        try {
            final Transformer transformer = TransformerFactory.newInstance().newTransformer(new StreamSource(styleFile));
            if (input.getAttribute("include.rellinks") != null) {
                transformer.setParameter("include.rellinks", input.getAttribute("include.rellinks"));
            }
            in = new BufferedInputStream(new FileInputStream(inputFile));
            final Source source = new StreamSource(in);
            source.setSystemId(inputFile.toURI().toString());
            final Result result = new SAXResult(linkReader);
            transformer.transform(source, result);
        } catch (final RuntimeException e) {
            throw e;
        } catch (final Exception e) {
            throw new DITAOTException("Failed to transform " + inputFile + ": " + e.getMessage(), e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (final IOException e) {
                    logger.error("Failed to close input stream: " + e.getMessage(), e);
                }
            }
        }

        final Map<File, Map<String, Element>> mapSet = linkReader.getMapping();
        
        if (!mapSet.isEmpty()) {
            final DitaLinksWriter linkInserter = new DitaLinksWriter();
            linkInserter.setLogger(logger);
            linkInserter.setJob(job);
            for (final Map.Entry<File, Map<String, Element>> entry: mapSet.entrySet()) {
                logger.info("Processing " + entry.getKey());
                linkInserter.setLinks(entry.getValue());
                linkInserter.write(entry.getKey());
            }
        }
        return null;
    }

}
