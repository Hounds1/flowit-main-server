package dev.runtime_lab.flowit.global.utility.engine.unit.json.executor;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JsonPayloadDeserializerTest {

    private final JsonPayloadDeserializer deserializer = new TestJsonPayloadDeserializer(
        JsonMapper.builder().build()
    );

    @Test
    void singleOfDeserializesJsonToTypedObject() throws Exception {
        Sample sample = deserializer.singleOf(Sample.class)
            .fromJson("{\"name\":\"홍길동\",\"age\":7}");

        assertEquals("홍길동", sample.name);
        assertEquals(7, sample.age);
    }

    @Test
    void listOfDeserializesJsonArrayToTypedList() throws Exception {
        List<Sample> samples = deserializer.listOf(Sample.class)
            .fromJson("[{\"name\":\"홍길동\",\"age\":7},{\"name\":\"김철수\",\"age\":9}]");

        assertEquals(2, samples.size());
        assertEquals("홍길동", samples.get(0).name);
        assertEquals("김철수", samples.get(1).name);
    }

    static class Sample {
        public String name;
        public int age;

        public Sample() {
        }
    }

    private static class TestJsonPayloadDeserializer extends JsonPayloadDeserializer {
        protected TestJsonPayloadDeserializer(JsonMapper jsonMapper) {
            super(jsonMapper);
        }
    }
}
