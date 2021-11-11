/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2005, 2006 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.

 */
package org.dita.dost.platform;

import net.sf.saxon.trans.UncheckedXPathException;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.util.XMLUtils.AttributesBuilder;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.*;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Generate outputfile with templates.
 * @author Zhang, Yuan Peng
 */
final class FileGenerator extends XMLFilterImpl {

    private static final String EXTENSION_ID_ATTR = "id";
    private static final String EXTENSION_ELEM = "extension";
    private static final String EXTENSION_ATTR = "extension";
    private static final String BEHAVIOR_ATTR = "behavior";

    public static final String PARAM_LOCALNAME = "localname";
    public static final String PARAM_TEMPLATE = "template";

    private static final String DITA_OT_NS = "http://dita-ot.sourceforge.net";
    private static final String TEMPLATE_PREFIX = "_template.";

    private static final Map<String, String> DEFAULT_EXTENSIONS = Collections.emptyMap();

    private DITAOTLogger logger;
    /** Plug-in features. */
    private final Map<String, List<Value>> featureTable;
    private final Map<String, Features> pluginTable;
    /** Template file. */
    private File templateFile;

    /**
     * Default Constructor.
     */
    public FileGenerator() {
        this(null, null);
    }

    /**
     * Constructor init featureTable.
     * @param featureTbl featureTbl
     */
    public FileGenerator(final Hashtable<String, List<Value>> featureTbl, final Map<String, Features> pluginTable) {
        featureTable = featureTbl;
        this.pluginTable = pluginTable;
        templateFile = null;
    }

    public void setLogger(final DITAOTLogger logger) {
        this.logger = logger;
    }

    /**
     * Generator the output file.
     * @param fileName filename
     */
    public void generate(final File fileName) {
        final File outputFile = removeTemplatePrefix(fileName);
        templateFile = fileName;

        try (final InputStream in = new BufferedInputStream(new FileInputStream(fileName));
             final OutputStream out = new BufferedOutputStream(new FileOutputStream(outputFile))) {
            final Transformer serializer = TransformerFactory.newInstance().newTransformer();
            final SAXParserFactory parserFactory = SAXParserFactory.newInstance();
            parserFactory.setNamespaceAware(true);
            XMLReader reader = parserFactory.newSAXParser().getXMLReader();
            this.setContentHandler(null);
            this.setParent(reader);
            reader = this;
            final Source source = new SAXSource(reader, new InputSource(in));
            source.setSystemId(fileName.toURI().toString());
            final Result result = new StreamResult(out);
            serializer.transform(source, result);
        } catch (final UncheckedXPathException e) {
            logger.error(e.getXPathException().getMessageAndLocation());
        } catch (final RuntimeException e) {
            throw e;
        } catch (final TransformerException e) {
            logger.error("Failed to transform " + fileName + ": " + e.getMessageAndLocation(), e);
        } catch (final Exception e) {
            logger.error("Failed to transform " + fileName + ": " + e.getMessage(), e);
        }
    }

    private File removeTemplatePrefix(final File templateFile) {
        final String f = templateFile.getAbsolutePath();
        final int i = f.lastIndexOf(TEMPLATE_PREFIX);
        if (i != -1) {
            return new File(f.substring(0, i) + f.substring(i + TEMPLATE_PREFIX.length() - 1));
        } else {
            throw new IllegalArgumentException("File " + templateFile + " does not contain template prefix");
        }
    }

    // XMLFilter methods

    @Override
    public void endElement(final String uri, final String localName, final String qName) throws SAXException {
        if (!(DITA_OT_NS.equals(uri) && EXTENSION_ELEM.equals(localName))) {
            getContentHandler().endElement(uri, localName, qName);
        }
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
        try {
            if (DITA_OT_NS.equals(uri) && EXTENSION_ELEM.equals(localName)) {
                final IAction action = (IAction)Class.forName(attributes.getValue(BEHAVIOR_ATTR)).newInstance();
                action.setLogger(logger);
                action.addParam(PARAM_TEMPLATE, templateFile.getAbsolutePath());
                for (int i = 0; i <  attributes.getLength(); i++) {
                    action.addParam(attributes.getLocalName(i), attributes.getValue(i));
                }
                final String extension = attributes.getValue(EXTENSION_ID_ATTR);
                //action.addParam("extension", extension);
                if (featureTable.containsKey(extension)) {
                    action.setInput(featureTable.get(extension));
                }
                action.setFeatures(pluginTable);
                action.getResult(getContentHandler());
            } else {
                final Map<String, String> extensions = parseExtensions(attributes.getValue(DITA_OT_NS, EXTENSION_ATTR));
                final AttributesBuilder atts = new AttributesBuilder();
                final int attLen = attributes.getLength();
                for (int i = 0; i < attLen; i++) {
                    final String name = attributes.getLocalName(i);
                    if (DITA_OT_NS.equals(attributes.getURI(i))) {
                        if (!(EXTENSION_ATTR.equals(name))) {
                            if (extensions.containsKey(name)) {
                                final IAction action = (IAction)Class.forName(extensions.get(name)).newInstance();
                                action.setLogger(logger);
                                action.setFeatures(pluginTable);
                                action.addParam(PARAM_TEMPLATE, templateFile.getAbsolutePath());
                                final List<Value> value = Stream.of(attributes.getValue(i).split(Integrator.FEAT_VALUE_SEPARATOR))
                                        .map(val -> new Value(null, val))
                                        .collect(Collectors.toList());
                                action.setInput(value);
                                final String result = action.getResult();
                                atts.add(name, result);
                            } else {
                                throw new IllegalArgumentException("Extension attribute " + name + " not defined");
                            }
                        }
                    } else {
                        atts.add(attributes.getURI(i), name, attributes.getQName(i), attributes.getType(i), attributes.getValue(i));
                    }
                }
                getContentHandler().startElement(uri, localName, qName, atts.build());
            }
        } catch (SAXException | RuntimeException e) {
            throw e;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, String> parseExtensions(final String extensions) {
        if (extensions == null) {
            return DEFAULT_EXTENSIONS;
        }
        final Map<String, String> res = new HashMap<>(DEFAULT_EXTENSIONS);
        final StringTokenizer extensionTokenizer = new StringTokenizer(extensions);
        while (extensionTokenizer.hasMoreTokens()) {
            final String thisExtension = extensionTokenizer.nextToken();
            final String thisExtensionClass = extensionTokenizer.nextToken();
            res.put(thisExtension, thisExtensionClass);
        }
        return res;
    }

}
