/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */
package org.dita.dost.writer;

import static java.util.Arrays.asList;

import java.io.File;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.module.Content;
import org.dita.dost.util.XMLUtils;
import org.xml.sax.XMLFilter;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * Base for XML filters.
 *
 * @since 1.5.4
 * @author Jarno Elovirta
 */
abstract class AbstractXMLFilter extends XMLFilterImpl implements AbstractWriter {

    protected DITAOTLogger logger;

    @Override
    public abstract void setContent(Content content);

    @Override
    public void write(final String filename) throws DITAOTException {
        try {
            XMLUtils.transform(new File(filename), asList((XMLFilter) this));
        } catch (final Exception e) {
            logger.logError(e.getMessage(), e) ;
        }
    }

    @Override
    public final void setLogger(final DITAOTLogger logger) {
        this.logger = logger;
    }

}
