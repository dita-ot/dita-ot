package org.dita.dost.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class KeyScopeTest {

    private KeydefSerializer serializer;

    @Before
    public void before() {
        serializer = new KeydefSerializer();
    }

    private KeyScope buildKeyScope() {
        Map<String, KeyDef> keyDefinitions = new HashMap<>();
        KeyDef keyDef = new KeyDef("keys", URI.create("https://www.google.de"),"scope","format",
                URI.create("https://www.google.de"), null, false);
        KeyDef filteredKeyDef = new KeyDef("keys", URI.create("http://www.google.de"),"scope","format",
                URI.create("http://www.google.de"), null, true);
        keyDefinitions.put("not-filtered", keyDef);
        keyDefinitions.put("filtered", filteredKeyDef);
        keyDefinitions.put("empty", null);
        String id = "id";
        String name = null;
        return new KeyScope(id, name, keyDefinitions,new ArrayList<>());
    }

    @Test
    public void serialize_notFilteredKeydef() throws Exception {
        // given
        ObjectWriter writer = serializer.newSerializer();
        StringWriter stringWriter = new StringWriter();

        // when
        writer.writeValue(stringWriter, buildKeyScope());
        String result = stringWriter.toString();

        // then
        assertFalse(result.contains("not-filtered"));
    }

    @Test
    public void serialize_mapNullValue() throws Exception {
        // given
        ObjectWriter writer = serializer.newSerializer();
        StringWriter stringWriter = new StringWriter();

        // when
        writer.writeValue(stringWriter, buildKeyScope());
        String result = stringWriter.toString();

        // then
        assertFalse(result.contains("empty"));
    }

    @Test
    public void serialize_keyScopeNameNull() throws Exception {
        // given
        ObjectWriter writer = serializer.newSerializer();
        StringWriter stringWriter = new StringWriter();

        // when
        writer.writeValue(stringWriter, buildKeyScope());
        String result = stringWriter.toString();

        // then
        assertTrue(result.contains("\"name\":null"));
    }

    @Test
    public void deserialize() throws Exception {
        // given
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(KeyDef.class, new KeydefDeserializer());
        objectMapper.registerModule(module);
        InputStream stream = KeyScopeTest.class.getResourceAsStream("/KeyScopeTest/keyscope.json");

        // when
        KeyScope scope = objectMapper.readValue(stream, KeyScope.class);

        // then
        assertEquals(2,scope.keyDefinition.size());
        assertTrue(scope.get("ktopic4").isFiltered());
        assertEquals("local", scope.get("ktopic4").scope);

        assertEquals("dita", scope.get("ktopic2").format);
        assertFalse(scope.get("ktopic2").isFiltered());
        assertNotNull(scope.get("ktopic2").element);
    }

}
