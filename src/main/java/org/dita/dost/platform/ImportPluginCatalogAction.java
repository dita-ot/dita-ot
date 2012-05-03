/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */
package org.dita.dost.platform;

import static org.dita.dost.util.Constants.*;

import java.io.File;
import java.util.Map.Entry;

import org.dita.dost.util.FileUtils;
import org.dita.dost.util.StringUtils;

/**
 * Integration action to output plugin information to catalog file.
 * 
 * @author Jarno Elovirta
 * @since 1.5.4
 */
final class ImportPluginCatalogAction extends ImportAction {

    public static final String PLUGIN_URI_SCHEME = "plugin";

    public ImportPluginCatalogAction() {
        super();
    }

    @Override
    public String getResult() {
        final StringBuffer buf = new StringBuffer();
        // plugin properties
        for (final Entry<String, Features> e: featureTable.entrySet()) {
            final Features f = e.getValue();
            final String name = PLUGIN_URI_SCHEME + ":" + e.getKey() + ":";
            final StringBuilder location = new StringBuilder();
            
            if (Boolean.parseBoolean(f.getFeature("dita.basedir-resource-directory"))) {
                location.append("./");
            } else if (f.getLocation().getAbsolutePath().startsWith(f.getDitaDir().getAbsolutePath())) {
                location.append(
                        FileUtils.getRelativePath(
                                new File(f.getDitaDir(), "plugin.xml").toURI().toString(),
                                f.getLocation().toURI().toString()));
            } else {
                location.append(f.getLocation().toURI().toString());
            }
            if (location.length() > 0 && !location.substring(location.length() - 1).equals(UNIX_SEPARATOR)) {
                location.append(UNIX_SEPARATOR);
            }
            buf.append("<rewriteURI uriStartString='")
               .append(StringUtils.escapeXML(name))
               .append("' rewritePrefix='")
               .append(StringUtils.escapeXML(location.toString()))
               .append("'/>");
        }
        return buf.toString();
    }

}
