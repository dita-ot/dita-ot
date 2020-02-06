package org.dita.dost.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import net.sf.saxon.s9api.XdmNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;

public class KeydefDeserializer extends StdDeserializer<KeyDef> {

    private static final String FIELD_KEYS = "keys";
    private static final String FIELD_HREF = "href";
    private static final String FIELD_SCOPE = "scope";
    private static final String FIELD_SOURCE = "source";
    private static final String FIELD_FORMAT = "format";
    private static final String FIELD_ELEMENT = "element";
    private static final String FIELD_FILTERED = "filtered";

    public KeydefDeserializer() {
        this(null);
    }

    public KeydefDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public KeyDef deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        JsonNode node = jp.getCodec().readTree(jp);

        try {
            String keys = node.hasNonNull(FIELD_KEYS) ? node.get(FIELD_KEYS).asText() : "";
            URI href = node.hasNonNull(FIELD_HREF) ? new URI(node.get(FIELD_HREF).asText()) : null;
            String scope =  node.hasNonNull(FIELD_SCOPE) ? node.get(FIELD_SCOPE).asText() : "";
            URI source = node.hasNonNull(FIELD_SOURCE) ? new URI(node.get(FIELD_SOURCE).asText()) : null;
            String format = node.hasNonNull(FIELD_FORMAT) ? node.get(FIELD_FORMAT).asText() : "";
            XdmNode element = node.hasNonNull(FIELD_ELEMENT) ? toElement(node.get(FIELD_ELEMENT).asText()) : null;
            boolean filtered = node.hasNonNull(FIELD_FILTERED) ? node.get(FIELD_FILTERED).asBoolean() : false;

            return new KeyDef(keys, href, scope, format, source, element, filtered);
        } catch (URISyntaxException | ParserConfigurationException | SAXException e) {
            throw new IOException("Couldn't deserialize KeyDef", e);
        }

    }

    private XdmNode toElement(String element) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        Document document = factory.newDocumentBuilder().parse(new InputSource(new StringReader(element)));
        return new XMLUtils().getProcessor().newDocumentBuilder().wrap(document.getDocumentElement());
    }


}
