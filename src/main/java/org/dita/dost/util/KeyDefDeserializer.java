package org.dita.dost.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
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

public class KeyDefDeserializer extends StdDeserializer<KeyDef> {

    public KeyDefDeserializer() {
        this(null);
    }

    public KeyDefDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public KeyDef deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode node = jp.getCodec().readTree(jp);

        try {
            String keys = node.get("keys").asText();
            URI href = new URI(node.get("href").asText());
            String scope = node.get("scope").asText();
            URI source = new URI(node.get("source").asText());
            String format = node.get("format").asText();
            Element element = toElement(node.get("element").asText());
            boolean filtered = node.get("filtered").asBoolean();

            return new KeyDef(keys,href,scope,format,source,element,filtered);
        } catch (URISyntaxException | ParserConfigurationException | SAXException e) {
            throw new IOException("Couldn't deserialize KeyDef", e);
        }

    }

    private Element toElement(String element) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        Document document = factory.newDocumentBuilder().parse(new InputSource(new StringReader(element)));
        return document.getDocumentElement();
    }
}
