/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for
 * applicable licenses.
 */
/*
 * (c) Copyright IBM Corp. 2011 All Rights Reserved.
 */
package org.dita.dost.writer;

import static org.dita.dost.util.Constants.FILE_EXTENSION_TEMP;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.module.Content;
import org.dita.dost.util.StringUtils;

/**
 * Base for XML filters.
 *
 * @since 1.5.4
 */
abstract class AbstractXMLFilter extends XMLFilterImpl implements AbstractWriter {

    protected DITAOTLogger logger;

    public abstract void setContent(Content content);

    public void write(final String filename) throws DITAOTException {
        final File inputFile = new File(filename);
        final File outputFile = new File(filename + FILE_EXTENSION_TEMP);
        InputStream in = null;
        OutputStream out = null;
        try {
            final Transformer transformer = TransformerFactory.newInstance().newTransformer();
            final XMLReader reader = StringUtils.getXMLReader();
            //reader.setErrorHandler(new DITAOTXMLErrorHandler(filename));
            setParent(reader);
            // ContentHandler must be reset so e.g. Saxon 9.1 will reassign ContentHandler
            // when reusing filter with multiple Transformers.
            setContentHandler(null);
            in = new BufferedInputStream(new FileInputStream(inputFile));
            out = new BufferedOutputStream(new FileOutputStream(outputFile));
            final Source source = new SAXSource(this, new InputSource(in));
            final Result result = new StreamResult(out);

            transformer.transform(source, result);
        } catch (final Exception e) {
            logger.logException(e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (final IOException e) {
                    logger.logException(e);
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (final IOException e) {
                    logger.logException(e);
                }
            }
        }
        // replace original file
        try {
            if (!inputFile.delete()) {
                final Properties prop = new Properties();
                prop.put("%1", inputFile.getPath());
                prop.put("%2", outputFile.getPath());
                logger.logError(MessageUtils.getMessage("DOTJ009E", prop).toString());
            }
            if (!outputFile.renameTo(inputFile)) {
                final Properties prop = new Properties();
                prop.put("%1", inputFile.getPath());
                prop.put("%2", outputFile.getPath());
                logger.logError(MessageUtils.getMessage("DOTJ009E", prop).toString());
            }
        } catch (final Exception e) {
            logger.logException(e);
        }
    }

    public final void setLogger(final DITAOTLogger logger) {
        this.logger = logger;
    }

}
