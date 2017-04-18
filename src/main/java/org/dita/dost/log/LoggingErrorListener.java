/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2017 Jarno Elovirta
 *
 *  See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.log;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;

public class LoggingErrorListener implements ErrorListener {

    private final DITAOTLogger logger;

    public LoggingErrorListener(final DITAOTLogger logger) {
        this.logger = logger;
    }

    @Override
    public void warning(TransformerException exception) throws TransformerException {
        logger.warn(exception.getMessage());
    }

    @Override
    public void error(TransformerException exception) throws TransformerException {
        logger.error(exception.getMessage());
    }

    @Override
    public void fatalError(TransformerException exception) throws TransformerException {
        throw exception;
    }
}
