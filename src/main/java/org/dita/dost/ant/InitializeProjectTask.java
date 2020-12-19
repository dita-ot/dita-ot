/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2020 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.dita.dost.log.DITAOTAntLogger;
import org.dita.dost.store.Store;
import org.dita.dost.store.StoreBuilder;
import org.dita.dost.util.CatalogUtils;
import org.dita.dost.util.XMLUtils;

import java.io.File;
import java.util.ServiceLoader;

import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.URLUtils.toFile;

/**
 * Initialize Ant references.
 *
 * @since 3.5
 */
public final class InitializeProjectTask extends Task {

    private static ServiceLoader<StoreBuilder> storeBuilderLoader = ServiceLoader.load(StoreBuilder.class);

    private String storeType = "file";

    @Override
    public void execute() throws BuildException {
        log("Initializing project", Project.MSG_INFO);
        final File ditaDir = toFile(getProject().getProperty("dita.dir"));
        if (!ditaDir.isAbsolute()) {
            throw new IllegalArgumentException("DITA-OT installation directory " + ditaDir + " must be absolute");
        }
        CatalogUtils.setDitaDir(ditaDir);
        XMLUtils xmlUtils = getProject().getReference(ANT_REFERENCE_XML_UTILS);
        if (xmlUtils == null) {
            xmlUtils = new XMLUtils();
            xmlUtils.setLogger(new DITAOTAntLogger(getProject()));
            getProject().addReference(ANT_REFERENCE_XML_UTILS, xmlUtils);
        }
        final Store store = getStore(xmlUtils);
        getProject().addReference(ANT_REFERENCE_STORE, store);
    }

    private Store getStore(XMLUtils xmlUtils) {
        Store store = getProject().getReference(ANT_REFERENCE_STORE);
        if (store != null) {
            return store;
        }
        File tempDir = toFile(getProject().getUserProperty(ANT_TEMP_DIR));
        if (tempDir == null) {
            tempDir = toFile(getProject().getProperty(ANT_TEMP_DIR));
        }
        for (StoreBuilder storeBuilder : storeBuilderLoader) {
            if (storeBuilder.getType().equals(storeType)) {
                return storeBuilder.setTempDir(tempDir).setXmlUtils(xmlUtils).build();
            }
        }
        throw new BuildException(String.format("Unsupported store type %s", storeType));
    }

    public void setStoreType(final String storeType) {
        this.storeType = storeType;
    }
}