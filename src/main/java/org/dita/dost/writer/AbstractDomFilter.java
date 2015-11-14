package org.dita.dost.writer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.dita.dost.exception.DITAOTXMLErrorHandler;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.reader.AbstractReader;
import org.dita.dost.util.Job;
import org.dita.dost.util.XMLUtils;
import org.w3c.dom.Document;

/**
 * Reads XML into DOM, modifies it, and serializes back into XML.
 */
public abstract class AbstractDomFilter implements AbstractReader {

    protected DITAOTLogger logger;
    protected Job job;

    @Override
    public void read(final File filename) {
        assert filename.isAbsolute();
        logger.info("Processing " + filename.toURI());
        Document doc = null;
        try {
            final DocumentBuilder builder = XMLUtils.getDocumentBuilder();
            builder.setErrorHandler(new DITAOTXMLErrorHandler(filename.getPath(), logger));
            logger.debug("Reading " + filename.toURI());
            doc = builder.parse(filename);
        } catch (final RuntimeException e) {
            throw e;
        } catch (final Exception e) {
            logger.error("Failed to parse " + filename.getAbsolutePath() + ":" + e.getMessage(), e);
            return;
        }

        final Document resDoc = process(doc);

        if (resDoc != null) {
            FileOutputStream file = null;
            try {
                file = new FileOutputStream(filename);
                final StreamResult res = new StreamResult(file);
                final DOMSource ds = new DOMSource(resDoc);
                final Transformer tf = TransformerFactory.newInstance().newTransformer();
                logger.debug("Writing " + filename.toURI());
                tf.transform(ds, res);
            } catch (final RuntimeException e) {
                throw e;
            } catch (final Exception e) {
                logger.error("Failed to serialize " + filename.getAbsolutePath() + ": " + e.getMessage(), e);
            } finally {
                if (file != null) {
                    try {
                        file.close();
                    } catch (final IOException e) {
                        // NOOP
                    }
                }
            }
        }
    }

    @Override
    public void setLogger(final DITAOTLogger logger) {
        this.logger = logger;
    }

    public void setJob(final Job job) {
        this.job = job;
    }

    /**
     * Modify document.
     * 
     * @param doc document to modify
     * @return modified document, may be argument document; if {@code null}, document is not serialized
     */
    protected abstract Document process(final Document doc);

}
