/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2005, 2006 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.

 */
package org.dita.dost.util;

import static org.dita.dost.util.Constants.FILE_NAME_CATALOG;
import static org.dita.dost.util.XMLUtils.toList;

import java.io.File;
import java.io.StringReader;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import org.dita.dost.platform.Plugins;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xmlresolver.Resolver;
import org.xmlresolver.ResolverFeature;
import org.xmlresolver.XMLResolverConfiguration;

/**
 * General catalog file resolving utilities.
 * @version 1.0 2005-4-11
 * @author Zhang, Yuan Peng
 */

public final class CatalogUtils {

  /**apache catalogResolver.*/
  private static Resolver catalogResolver = null;
  /** Absolute directory to find catalog-dita.xml.*/
  private static File ditaDir;

  /**
   * Instances should NOT be constructed in standard programming.
   */
  private CatalogUtils() {
    // leave blank as designed
  }

  /**
   * Set directory to find catalog-dita.xml.
   * @param ditaDir ditaDir
   */
  public static synchronized void setDitaDir(final File ditaDir) {
    catalogResolver = null;
    CatalogUtils.ditaDir = ditaDir;
  }

  /**
   * Get CatalogResolver.
   * @return CatalogResolver
   */
  public static synchronized Resolver getCatalogResolver() {
    if (catalogResolver == null) {
      final XMLResolverConfiguration config = new XMLResolverConfiguration(List.of());
      config.setFeature(ResolverFeature.PREFER_PUBLIC, true);
      config.setFeature(ResolverFeature.CACHE_DIRECTORY, null);
      config.setFeature(ResolverFeature.CACHE_UNDER_HOME, false);

      if (ditaDir != null) {
        final File catalogFilePath = new File(
          ditaDir,
          Configuration.pluginResourceDirs.get("org.dita.base") + File.separator + FILE_NAME_CATALOG
        );
        config.addCatalog(catalogFilePath.toURI(), new InputSource(catalogFilePath.toURI().toString()));
      } else {
        // platform URI scheme
        config.addCatalog(
          URI.create("platform.xml"),
          new InputSource(
            new StringReader(
              """
              <catalog xmlns="urn:oasis:names:tc:entity:xmlns:xml:catalog">
                <rewriteURI uriStartString="platform:" rewritePrefix="../../"/>
              </catalog>
              """
            )
          )
        );

        // plugin directories
        final List<Element> plugins = toList(Plugins.getPluginConfiguration().getElementsByTagName("plugin"));
        final StringBuilder bufInfo = new StringBuilder();
        bufInfo.append("<catalog xmlns='urn:oasis:names:tc:entity:xmlns:xml:catalog'>");
        for (Element plugin : plugins) {
          final String id = plugin.getAttribute("id");
          bufInfo.append("<rewriteURI uriStartString='plugin:%s:' rewritePrefix='classpath:%s/'/>".formatted(id, id));
        }
        bufInfo.append("</catalog>");
        config.addCatalog(URI.create("plugins.xml"), new InputSource(new StringReader(bufInfo.toString())));

        // includes
        for (Element plugin : plugins) {
          final String id = plugin.getAttribute("id");
          final List<Element> features = toList(plugin.getElementsByTagName("feature"));
          for (Element feature : features) {
            if (Objects.equals(feature.getAttribute("extension"), "dita.specialization.catalog.relative")) {
              String file = feature.getAttribute("file");
              if (file.isEmpty()) {
                file = feature.getAttribute("value");
              }
              config.addCatalog("classpath:%s/%s".formatted(id, file));
            }
          }
        }

        // root catalog
        config.addCatalog("classpath:catalog.xml");
      }

      catalogResolver = new Resolver(config);
    }

    return catalogResolver;
  }
}
