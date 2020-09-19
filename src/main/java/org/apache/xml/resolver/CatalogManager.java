/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2020 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.apache.xml.resolver;

public class CatalogManager {

    private static final CatalogManager catalogManager = new CatalogManager();

    public static Debug debug = new Debug();

    public static CatalogManager getStaticManager() {
        return catalogManager;
    }

    public void setIgnoreMissingProperties(boolean ignoreMissing) {
        // NOOP
    }

    public void setUseStaticCatalog(boolean useStaticCatalog) {
        // NOOP
    }

    public static class Debug {

        public void message(int level, String msg) {
            System.err.println(msg);
        }
    }
}
