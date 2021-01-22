package org.dita.dost.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class KeydefSerializer extends StdSerializer<KeyDef> {

    public KeydefSerializer() {
        this(null);
    }

    public KeydefSerializer(Class<KeyDef> t) {
        super(t);
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
