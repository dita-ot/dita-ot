/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2011 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.platform;

import static org.dita.dost.util.Constants.*;

import java.io.File;
import java.util.List;
import java.util.Map.Entry;

import org.dita.dost.util.FileUtils;
import org.dita.dost.util.XMLUtils.AttributesBuilder;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Integration action to output plugin information to catalog file.
 *
 * @author Jarno Elovirta
 * @since 1.5.4
 */
final class ImportPluginCatalogAction extends ImportAction {

    private static final String PLUGIN_URI_SCHEME = "plugin";

    public ImportPluginCatalogAction() {
        super();
    }

    @Override
    public void getResult(final ContentHandler buf) throws SAXException {
        final File basePluginDir = featureTable.get("org.dita.base").getPluginDir();
        for (final Entry<String, Features> e: featureTable.entrySet()) {
            final Features f = e.getValue();
            final String name = PLUGIN_URI_SCHEME + ":" + e.getKey() + ":";
            final StringBuilder location = new StringBuilder();

            final List<String> baseDirValues = f.getFeature("dita.basedir-resource-directory");
            if (Boolean.parseBoolean(baseDirValues == null || baseDirValues.isEmpty() ? null : baseDirValues.get(0))) {
                location.append("./");
            } else if (f.getPluginDir().getAbsolutePath().startsWith(f.getDitaDir().getAbsolutePath())) {
                location.append(
                        FileUtils.getRelativeUnixPath(
                                new File(basePluginDir, "plugin.xml").toURI().toString(),
                                f.getPluginDir().toURI().toString()));
            } else {
                location.append(f.getPluginDir().toURI().toString());
            }
            if (location.length() > 0 && !location.substring(location.length() - 1).equals(UNIX_SEPARATOR)) {
                location.append(UNIX_SEPARATOR);
            }
            buf.startElement(OASIS_CATALOG_NAMESPACE, "rewriteURI", "rewriteURI", new AttributesBuilder()
                .add("uriStartString", name)
                .add("rewritePrefix", location.toString())
                .build());
            buf.endElement(OASIS_CATALOG_NAMESPACE, "rewriteURI", "rewriteURI");
        }
    }

}
