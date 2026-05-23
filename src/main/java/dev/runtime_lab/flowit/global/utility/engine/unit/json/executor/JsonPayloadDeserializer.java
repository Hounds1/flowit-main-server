package dev.runtime_lab.flowit.global.utility.engine.unit.json.executor;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.type.CollectionType;

import java.util.List;
import java.util.Objects;

public class JsonPayloadDeserializer {

    private final JsonMapper singletonMapper;

    protected JsonPayloadDeserializer(JsonMapper jsonMapper) {
        this.singletonMapper = jsonMapper;
    }

    public <T> SingleDeserializer<T> singleOf(Class<T> type) {
        return new SingleDeserializer<>(singletonMapper, type);
    }

    public <T> ListDeserializer<T> listOf(Class<T> elementType) {
        return new ListDeserializer<>(singletonMapper, elementType);
    }

    public static class SingleDeserializer<T> {
        private final JsonMapper jsonMapper;
        private final Class<T> type;

        private SingleDeserializer(JsonMapper jsonMapper, Class<T> type) {
            this.jsonMapper = Objects.requireNonNull(jsonMapper);
            this.type = Objects.requireNonNull(type);
        }

        public T fromJson(String json) throws JacksonException {
            return jsonMapper.readValue(json, type);
        }
    }

    public static class ListDeserializer<T> {
        private final JsonMapper jsonMapper;
        private final Class<T> elementType;

        private ListDeserializer(JsonMapper jsonMapper, Class<T> elementType) {
            this.jsonMapper = Objects.requireNonNull(jsonMapper);
            this.elementType = Objects.requireNonNull(elementType);
        }

        public List<T> fromJson(String json) throws JacksonException {
            CollectionType listType = jsonMapper.getTypeFactory().constructCollectionType(List.class, elementType);
            return jsonMapper.readValue(json, listType);
        }
    }
}
