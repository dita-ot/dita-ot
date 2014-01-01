/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */
package org.dita.dost.platform;

import static javax.xml.XMLConstants.NULL_NS_URI;
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

    public static final String PLUGIN_URI_SCHEME = "plugin";

    public ImportPluginCatalogAction() {
        super();
    }

    @Override
    public void getResult(final ContentHandler buf) throws SAXException {
        // plugin properties
        for (final Entry<String, Features> e: featureTable.entrySet()) {
            final Features f = e.getValue();
            final String name = PLUGIN_URI_SCHEME + ":" + e.getKey() + ":";
            final StringBuilder location = new StringBuilder();
            
            final List<String> baseDirValues = f.getFeature("dita.basedir-resource-directory");
            if (Boolean.parseBoolean(baseDirValues == null || baseDirValues.isEmpty() ? null : baseDirValues.get(0))) {
                location.append("./");
            } else if (f.getLocation().getAbsolutePath().startsWith(f.getDitaDir().getAbsolutePath())) {
                location.append(
                        FileUtils.getRelativeUnixPath(
                                new File(f.getDitaDir(), "plugin.xml").toURI().toString(),
                                f.getLocation().toURI().toString()));
            } else {
                location.append(f.getLocation().toURI().toString());
            }
            if (location.length() > 0 && !location.substring(location.length() - 1).equals(UNIX_SEPARATOR)) {
                location.append(UNIX_SEPARATOR);
            }
            buf.startElement(NULL_NS_URI, "rewriteURI", "rewriteURI", new AttributesBuilder()
                .add("uriStartString", name)
                .add("rewritePrefix", location.toString())
                .build());
            buf.endElement(NULL_NS_URI, "rewriteURI", "rewriteURI");
        }
    }

}
