package org.dita.dost.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.Test;

import java.io.InputStream;

import static org.junit.Assert.*;

public class KeyScopeTest {

    @Test
    public void deserialize() throws Exception {
        // given
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(KeyDef.class, new KeyDefDeserializer());
        objectMapper.registerModule(module);
        InputStream stream = KeyScopeTest.class.getResourceAsStream("/KeyScopeTest/keyscope.json");

        // when
        KeyScope scope = objectMapper.readValue(stream, KeyScope.class);

        // then
        assertEquals(2,scope.keyDefinition.size());
        assertTrue(scope.get("ktopic4").isFiltered());
        assertNotNull(scope.get("ktopic4").element);
        assertFalse(scope.get("ktopic2").isFiltered());
        assertNotNull(scope.get("ktopic2").element);
    }

}
