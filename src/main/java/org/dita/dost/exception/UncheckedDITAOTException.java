/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2020 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.exception;

/**
 * Wraps an {@link DITAOTException} with an unchecked exception.
 *
 * @since 3.6
 */
public class UncheckedDITAOTException extends RuntimeException {

    public UncheckedDITAOTException(DITAOTException err) {
        super(err);
    }

    public DITAOTException getDITAOTException() {
        return (DITAOTException) getCause();
    }
}
