/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2020 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.store;

import org.dita.dost.util.XMLUtils;

import java.io.File;

/**
 * File store builder
 *
 * @since 3.5
 */
public class StreamStoreBuilder implements StoreBuilder {

    private File tempDir;
    private XMLUtils xmlUtils;

    @Override
    public String getType() {
        return "file";
    }

    @Override
    public StoreBuilder setTempDir(File tempDir) {
        this.tempDir = tempDir;
        return this;
    }

    @Override
    public StoreBuilder setXmlUtils(XMLUtils xmlUtils) {
        this.xmlUtils = xmlUtils;
        return this;
    }

    @Override
    public Store build() {
        return new StreamStore(tempDir, xmlUtils);
    }
}
