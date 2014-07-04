package org.dita.dost.writer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.dita.dost.exception.DITAOTXMLErrorHandler;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.reader.AbstractReader;
import org.w3c.dom.Document;

/**
 * Reads XML into DOM, modifies it, and serializes back into XML.
 */
public abstract class AbstractDomFilter implements AbstractReader {

    protected DITAOTLogger logger;

    @Override
    public void read(final File filename) {
        Document doc = null;
        try {
            final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            builder.setErrorHandler(new DITAOTXMLErrorHandler(filename.getPath(), logger));
            doc = builder.parse(filename);
        } catch (final Exception e) {
            logger.error("Failed to parse " + filename.getAbsolutePath() + ":" + e.getMessage(), e);
            return;
        }

        process(doc);

        FileOutputStream file = null;
        try {
            file = new FileOutputStream(filename);
            final StreamResult res = new StreamResult(file);
            final DOMSource ds = new DOMSource(doc);
            final Transformer tf = TransformerFactory.newInstance().newTransformer();
            tf.transform(ds, res);
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

    @Override
    public void setLogger(final DITAOTLogger logger) {
        this.logger = logger;
    }

    /**
     * Modify document.
     * 
     * @param doc document to modify
     */
    public abstract void process(final Document doc);

}
