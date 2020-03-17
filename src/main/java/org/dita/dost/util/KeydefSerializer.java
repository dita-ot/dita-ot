package org.dita.dost.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.std.MapSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.databind.type.MapType;

import java.io.IOException;

import static com.fasterxml.jackson.databind.ser.std.MapSerializer.MARKER_FOR_EMPTY;

public class KeydefSerializer extends StdSerializer<KeyDef> {

    public static final String MAP_WITHOUT_NULLS = "MapWithoutNullValues";

    public KeydefSerializer() {
        this(null);
    }

    public KeydefSerializer(Class<KeyDef> t) {
        super(t);
    }

    public ObjectWriter newSerializer() {
        ObjectMapper objectMapper = new ObjectMapper();

        SimpleModule module = new SimpleModule();
        module.addSerializer(KeyDef.class, new KeydefSerializer());
        objectMapper = objectMapper.registerModule(module).registerModule(newMapWithoutNullModule());

        return objectMapper.writer();
    }

    public Module newMapWithoutNullModule() {
        SimpleModule module = new SimpleModule(MAP_WITHOUT_NULLS);
        module.setSerializerModifier(new BeanSerializerModifier() {
            @Override
            public JsonSerializer<?> modifyMapSerializer(SerializationConfig config, MapType valueType, BeanDescription beanDesc, JsonSerializer<?> serializer) {
                if (!(serializer instanceof MapSerializer)) {
                    return super.modifyMapSerializer(config, valueType, beanDesc, serializer);
                }

                MapSerializer mapSerializer = (MapSerializer) serializer;
                return mapSerializer.withContentInclusion(MARKER_FOR_EMPTY, true);
            }
        });
        return module;
    }

    @Override
    public void serialize(KeyDef value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("scope", value.scope);
        gen.writeBooleanField("filtered", value.isFiltered());
        gen.writeEndObject();
    }

    @Override
    public boolean isEmpty(SerializerProvider provider, KeyDef value) {
        return super.isEmpty(provider, value) || !(value != null && value.isFiltered());
    }
}
