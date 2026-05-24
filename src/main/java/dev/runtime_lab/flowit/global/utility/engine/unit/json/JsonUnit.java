package dev.runtime_lab.flowit.global.utility.engine.unit.json;

import dev.runtime_lab.flowit.global.utility.engine.unit.json.executor.JsonPayloadDeserializer;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

public class JsonUnit {

    private final JsonMapper singletonMapper;
    private final JsonPayloadDeserializer deserializer;

    protected JsonUnit(JsonMapper jsonMapper) {
        this.singletonMapper = jsonMapper;
        this.deserializer = new DeserializerDelegator(jsonMapper);
    }

    public String writeToJson(Object o) throws JacksonException {
        if (o == null) throw new IllegalArgumentException("Parameter cannot be null.");

        return singletonMapper.writeValueAsString(o);
    }

    public String write(Object o) throws JacksonException {
        return writeToJson(o);
    }

    public <T> T read(String json, Class<T> type) throws JacksonException {
        return deserializer.singleOf(type).fromJson(json);
    }

    public <T> List<T> readList(String json, Class<T> elementType) throws JacksonException {
        return deserializer.listOf(elementType).fromJson(json);
    }

    public JsonPayloadDeserializer deserializer() {
        return this.deserializer;
    }

    private static class DeserializerDelegator extends JsonPayloadDeserializer {
        protected DeserializerDelegator(JsonMapper jsonMapper) {
            super(jsonMapper);
        }
    }
}
