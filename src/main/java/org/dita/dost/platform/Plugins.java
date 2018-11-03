/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2018 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.platform;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.dita.dost.util.Constants.PLUGIN_CONF;
import static org.dita.dost.util.XMLUtils.toList;

public class Plugins {

    /**
     * Read the list of installed plugins
     */
    public static List<String> getInstalledPlugins() {
        final List<Element> plugins = toList(getPluginConfiguration().getElementsByTagName("plugin"));
        return plugins.stream()
                .map((Element elem) -> elem.getAttributeNode("id"))
                .filter(Objects::nonNull)
                .map(Attr::getValue)
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Read plugin configuration
     */
    public static Document getPluginConfiguration() {
        try (final InputStream in = Plugins.class.getClassLoader().getResourceAsStream(PLUGIN_CONF)) {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
        } catch (final ParserConfigurationException | SAXException | IOException e) {
            throw new RuntimeException("Failed to read plugin configuration: " + e.getMessage(), e);
        }
    }

}
