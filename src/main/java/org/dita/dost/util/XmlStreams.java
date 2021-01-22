package org.dita.dost.util;

import org.dita.dost.log.DITAOTLogger;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.io.InputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.xml.stream.XMLInputFactory.*;

public class XmlStreams {

    private DITAOTLogger log;

    public XmlStreams(DITAOTLogger log) {
        this.log = log;
    }

    /**
     * Following properties of the XMLInputFactory are modified:
     * <p/>
     *
     * <code>SUPPORT_DTD</code>, set to false<br/>
     * Whether DTD subset (definition) processing is enabled or not. If enabled, DTD
     * definitions are read (both internal and external), and parsed entities are
     * expanded. If disabled, internal DTD subset is skipped and external subset is
     * not read. NOTE: if disabled, no DTD validation occurs, regardless of other
     * settings
     * <p/>
     *
     * <code>IS_VALIDATING</code>, set to false<br/>
     * Whether DTD validation is enabled or not (note: does not affect XML Schema,
     * Relax NG, or other validation settings). NOTE: only takes effects if
     * SUPPORT_DTD is also enabled
     * <p/>
     *
     * <code>IS_SUPPORTING_EXTERNAL_ENTITIES</code>, set to false<br/>
     * If DTD processing is enabled (see SUPPORT_DTD), external entities (references
     * to external resources outside of XML document or DTD subset itself) are
     * recognized and processed. However, their expansion may be disabled if this
     * setting is disabled. This is typically done for security reasons: if XML
     * content comes from untrusted sources, enabling expansion is not a good idea.
     * If disabled, entities are only reported as entity references; if enabled,
     * entities are expanded as per XML specification and reported as XML tokens.
     * <p/>
     *
     * <code>IS_REPLACING_ENTITY_REFERENCES</code>, set to false<br/>
     * Requires the parser to replace internal entity references with their
     * replacement text and report them as characters
     * <p/>
     *
     * <code>IS_NAMESPACE_AWARE</code>, set to false<br/>
     * Whether namespace-processing is enabled or not: if disabled,
     * namespace-binding does not occur, and full element/attribute name is reported
     * as “local name” (for example: <xml:space> would have local name of
     * “xml:space”, and no namespace prefix or URI). If enabled, namespace
     * declarations are handled and prefix/namespace binding applied as expected
     * <p/>
     * Full documentation can be found here:
     * <ul>
     * <li>https://docs.oracle.com/javase/8/docs/api/javax/xml/stream/XMLInputFactory.html</li>
     * <li>https://medium.com/@cowtowncoder/configuring-woodstox-xml-parser-basic-stax-properties-39bdf88c18ec</li>
     * </ul>
     */
    public XMLEventReader initializeReader(InputStream inputStream) throws XMLStreamException {
        XMLInputFactory inFactory = XMLInputFactory.newInstance();
        inFactory.setProperty(IS_VALIDATING, false);
        inFactory.setProperty(SUPPORT_DTD, false);
        inFactory.setProperty(IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        inFactory.setProperty(IS_REPLACING_ENTITY_REFERENCES, false);
        inFactory.setProperty(IS_NAMESPACE_AWARE, false);
        return inFactory.createXMLEventReader(inputStream, UTF_8.name());
    }

    public Attributes collectRootAttributes(XMLEventReader eventReader) throws FactoryConfigurationError, XMLStreamException {
        final AttributesImpl result = new AttributesImpl();

        while (eventReader.hasNext()) {
            XMLEvent event = eventReader.nextEvent();

            if (!event.isStartElement()) {
                continue;
            }

            event.asStartElement().getAttributes().forEachRemaining(attribute -> addAttribute(result, (Attribute) attribute));
            return result;
        }

        return result;
    }

    private void addAttribute(AttributesImpl result, Attribute attribute) {
        String namespace = attribute.getName().getNamespaceURI() != null ? attribute.getName().getNamespaceURI() : "";
        String prefix = attribute.getName().getPrefix() != null ? attribute.getName().getPrefix() : "";
        String localPart = attribute.getName().getLocalPart() != null ? attribute.getName().getLocalPart() : "";
        String name = attribute.getName().toString() != null ? attribute.getName().toString() : "";
        String schemaLocalPart = getSchemaLocalPart(attribute);
        String value = attribute.getValue() != null ? attribute.getValue() : "";
        result.addAttribute(namespace, prefix+localPart, name, schemaLocalPart, value);
    }

    private String getSchemaLocalPart(Attribute attribute) {
        if (attribute.getSchemaType() == null) {
            return "";
        }
        return attribute.getSchemaType().getLocalPart() != null ? attribute.getSchemaType().getLocalPart() : "";
    }

    public void freeResources(XMLEventReader eventReader, InputStream inputStream) {
        if (eventReader != null) {
            try {
                eventReader.close();
            } catch (XMLStreamException e) {
                log.warn("Couldn't close event reader", e);
            }
        }

        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                log.warn("Couldn't close inputstream", e);
            }
        }
    }
}
