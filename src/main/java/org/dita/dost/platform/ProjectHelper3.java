/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2024 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
/* Derived from Apache Ant. */
/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.dita.dost.platform;

import static org.dita.dost.platform.FileGenerator.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.helper.AntXMLContext;
import org.apache.tools.ant.helper.ProjectHelper2;
import org.dita.dost.log.DITAOTAntLogger;
import org.dita.dost.util.XMLUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Sax2 based project reader.
 *
 * <p>This is a modified copy ot {@link org.apache.tools.ant.helper.ProjectHelper2 ProjectHelper2}.
 */
public class ProjectHelper3 extends ProjectHelper2 {

  private static final AntHandler mainHandler = new MainHandler();
  /** Specific to ProjectHelper2 so not a true Ant "magic name:" */
  private static final String REFID_CONTEXT = "ant.parsing.context";

  /**
   * Parse a source xml input.
   *
   * @param project the current project
   * @param source the xml source
   * @exception BuildException if an error occurs
   */
  public void parse(Project project, Object source) throws BuildException {
    getImportStack().addElement(source);
    AntXMLContext context = project.getReference(REFID_CONTEXT);
    if (context == null) {
      context = new AntXMLContext(project);
      project.addReference(REFID_CONTEXT, context);
      project.addReference(REFID_TARGETS, context.getTargets());
    }
    if (getImportStack().size() > 1) {
      // we are in an imported file.
      context.setIgnoreProjectTag(true);
      Target currentTarget = context.getCurrentTarget();
      Target currentImplicit = context.getImplicitTarget();
      Map<String, Target> currentTargets = context.getCurrentTargets();
      try {
        Target newCurrent = new Target();
        newCurrent.setProject(project);
        newCurrent.setName("");
        context.setCurrentTarget(newCurrent);
        context.setCurrentTargets(new HashMap<>());
        context.setImplicitTarget(newCurrent);
        parse(project, source, new ExtensionRootHandler(context, mainHandler));
        newCurrent.execute();
      } finally {
        context.setCurrentTarget(currentTarget);
        context.setImplicitTarget(currentImplicit);
        context.setCurrentTargets(currentTargets);
      }
    } else {
      // top level file
      context.setCurrentTargets(new HashMap<>());
      parse(project, source, new ExtensionRootHandler(context, mainHandler));
      // Execute the top-level target
      context.getImplicitTarget().execute();
      // resolve extensionOf attributes
      resolveExtensionOfAttributes(project);
    }
  }

  public static class ExtensionRootHandler extends ProjectHelper2.RootHandler {

    private final AntXMLContext context;
    /** Plug-in features. */
    private final Map<String, List<Value>> featureTable;
    private final Map<String, Plugin> pluginTable;
    private final DITAOTAntLogger logger;

    public ExtensionRootHandler(AntXMLContext context, AntHandler rootHandler) {
      super(context, rootHandler);
      this.context = context;
      featureTable = new HashMap<>();
      pluginTable = new HashMap<>();
      logger = new DITAOTAntLogger(context.getProject());

      final Plugins plugins = new Plugins();
      plugins.setDitaDir(context.getProject().getBaseDir());
      plugins.setLogger(logger);
      for (Plugin p : plugins.getPlugins()) {
        pluginTable.put(p.pluginId(), p);
        for (Map.Entry<String, List<String>> f : p.features().entrySet()) {
          var vs = f.getValue().stream().map(v -> new Value(null, v)).toList();
          if (featureTable.containsKey(f.getKey())) {
            var buf = new ArrayList<Value>(featureTable.get(f.getKey()));
            buf.addAll(vs);
            featureTable.put(f.getKey(), buf);
          } else {
            featureTable.put(f.getKey(), vs);
          }
        }
      }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes)
      throws SAXParseException {
      try {
        if (DITA_OT_NS.equals(uri) && EXTENSION_ELEM.equals(localName)) {
          final IAction action = (IAction) Class
            .forName(attributes.getValue(BEHAVIOR_ATTR))
            .getDeclaredConstructor()
            .newInstance();
          action.setLogger(logger);
          action.addParam(PARAM_TEMPLATE, context.getBuildFile().getAbsolutePath());
          for (int i = 0; i < attributes.getLength(); i++) {
            action.addParam(attributes.getLocalName(i), attributes.getValue(i));
          }
          final String extension = attributes.getValue(EXTENSION_ID_ATTR);
          if (featureTable.containsKey(extension)) {
            action.setInput(featureTable.get(extension));
          }
          action.setFeatures(pluginTable);
          action.getResult(this);
        } else {
          final Map<String, String> extensions = parseExtensions(attributes.getValue(DITA_OT_NS, EXTENSION_ATTR));
          final XMLUtils.AttributesBuilder atts = new XMLUtils.AttributesBuilder();
          final int attLen = attributes.getLength();
          for (int i = 0; i < attLen; i++) {
            final String name = attributes.getLocalName(i);
            if (DITA_OT_NS.equals(attributes.getURI(i))) {
              if (!(EXTENSION_ATTR.equals(name))) {
                if (extensions.containsKey(name)) {
                  final IAction action = (IAction) Class.forName(extensions.get(name)).newInstance();
                  action.setLogger(logger);
                  action.setFeatures(pluginTable);
                  action.addParam(PARAM_TEMPLATE, context.getBuildFile().getAbsolutePath());
                  final List<Value> value = Stream
                    .of(attributes.getValue(i).split(Integrator.FEAT_VALUE_SEPARATOR))
                    .map(val -> new Value(null, val.trim()))
                    .collect(Collectors.toList());
                  action.setInput(value);
                  final String result = action.getResult();
                  atts.add(name, result);
                } else {
                  throw new IllegalArgumentException("Extension attribute " + name + " not defined");
                }
              }
            } else {
              atts.add(
                attributes.getURI(i),
                name,
                attributes.getQName(i),
                attributes.getType(i),
                attributes.getValue(i)
              );
            }
          }
          super.startElement(uri, localName, qName, atts.build());
        }
      } catch (SAXException | RuntimeException e) {
        throw new RuntimeException(e);
      } catch (final Exception e) {
        throw new RuntimeException(e);
      }
    }

    private Map<String, String> parseExtensions(final String extensions) {
      if (extensions == null) {
        return Map.of();
      }
      final Map<String, String> res = new HashMap<>();
      final StringTokenizer extensionTokenizer = new StringTokenizer(extensions);
      while (extensionTokenizer.hasMoreTokens()) {
        final String thisExtension = extensionTokenizer.nextToken();
        final String thisExtensionClass = extensionTokenizer.nextToken();
        res.put(thisExtension, thisExtensionClass);
      }
      return res;
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName) throws SAXException {
      if (!(DITA_OT_NS.equals(uri) && EXTENSION_ELEM.equals(localName))) {
        super.endElement(uri, localName, qName);
      }
    }
  }
}
