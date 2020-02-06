package org.dita.dost.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.std.MapSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.databind.type.MapType;

import java.io.IOException;

import static com.fasterxml.jackson.databind.ser.std.MapSerializer.MARKER_FOR_EMPTY;

public class KeyScopeSerializer extends StdSerializer<KeyScope> {

    public static final String MAP_WITHOUT_NULLS = "MapWithoutNullValues";

    public KeyScopeSerializer() {
        this(null);
    }

    public KeyScopeSerializer(Class<KeyScope> t) {
        super(t);
    }

    public ObjectWriter newSerializer() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

        SimpleModule module = new SimpleModule();
        module.addSerializer(KeyScope.class, new KeyScopeSerializer());
        module.addSerializer(KeyDef.class, new KeydefSerializer());
        module.setSerializerModifier(newMapWithoutNullModule());

        objectMapper = objectMapper.registerModule(module);

        return objectMapper.writer();
    }

    private BeanSerializerModifier newMapWithoutNullModule() {
        return new BeanSerializerModifier() {

            @Override
            public JsonSerializer<?> modifyMapSerializer(SerializationConfig config, MapType valueType, BeanDescription beanDesc, JsonSerializer<?> serializer) {
                if (!(serializer instanceof MapSerializer)) {
                    return super.modifyMapSerializer(config, valueType, beanDesc, serializer);
                }

                MapSerializer mapSerializer = (MapSerializer) serializer;
                return mapSerializer.withContentInclusion(MARKER_FOR_EMPTY, true);
            }
        };
    }

    @Override
    public void serialize(KeyScope value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (value.keyDefinition.isEmpty() && value.childScopes.isEmpty()) {
            return;
        }
        gen.writeStartObject();
        gen.writeStringField("id", value.id);
        gen.writeStringField("name", value.name);
        gen.writeObjectField("keyDefinition", value.keyDefinition);
        gen.writeObjectField("childScopes", value.childScopes);
        gen.writeEndObject();
    }

    @Override
    public boolean isEmpty(SerializerProvider provider, KeyScope value) {
        return super.isEmpty(provider, value) || !(value.childScopes.isEmpty() && value.keyDefinition.isEmpty());
    }
}
