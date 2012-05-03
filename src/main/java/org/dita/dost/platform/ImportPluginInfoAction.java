/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */
package org.dita.dost.platform;

import java.io.File;
import java.util.Map.Entry;

import org.dita.dost.util.FileUtils;
import org.dita.dost.util.StringUtils;

/**
 * Integration action to output plugin information to Ant build.
 * 
 * @author Jarno Elovirta
 * @since 1.5.3
 */
final class ImportPluginInfoAction extends ImportAction {

    public ImportPluginInfoAction() {
        super();
    }

    @Override
    public String getResult() {
        final StringBuffer buf = new StringBuffer();
        // plugin properties
        for (final Entry<String, Features> e: featureTable.entrySet()) {
            final Features f = e.getValue();
            final String name = "dita.plugin."+ e.getKey() + ".dir";
            final StringBuilder location = new StringBuilder();
            if (Boolean.parseBoolean(f.getFeature("dita.basedir-resource-directory"))) {
                location.append("${dita.dir}");
            } else if (f.getLocation().getAbsolutePath().startsWith(f.getDitaDir().getAbsolutePath())) {
                location.append("${dita.dir}").append(File.separator)
                .append(FileUtils.getRelativePath(
                        new File(f.getDitaDir(), "plugin.xml").getAbsolutePath(),
                        f.getLocation().getAbsolutePath()));
            } else {
                location.append(f.getLocation().getAbsolutePath());
            }
            buf.append("<property name='")
            .append(StringUtils.escapeXML(name))
            .append("' location='")
            .append(StringUtils.escapeXML(location.toString()))
            .append("'/>");
        }
        return buf.toString();
    }

}
