package dev.runtime_lab.flowit.global.utility.engine.unit.json;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonUnitTest {

    private final JsonUnit jsonUnit = new TestJsonUnit(JsonMapper.builder().build());

    @Test
    void writeSerializesObjectToJson() throws Exception {
        String json = jsonUnit.write(new Sample("홍길동", 7));

        assertTrue(json.contains("\"name\":\"홍길동\""));
        assertTrue(json.contains("\"age\":7"));
    }

    @Test
    void writeRejectsNullValue() {
        assertThrows(IllegalArgumentException.class, () -> jsonUnit.write(null));
    }

    @Test
    void readDeserializesJsonToSingleObject() throws Exception {
        Sample sample = jsonUnit.read("{\"name\":\"홍길동\",\"age\":7}", Sample.class);

        assertEquals("홍길동", sample.name);
        assertEquals(7, sample.age);
    }

    @Test
    void readListDeserializesJsonArrayToList() throws Exception {
        List<Sample> samples = jsonUnit.readList(
            "[{\"name\":\"홍길동\",\"age\":7},{\"name\":\"김철수\",\"age\":9}]",
            Sample.class
        );

        assertEquals(2, samples.size());
        assertEquals("홍길동", samples.get(0).name);
        assertEquals("김철수", samples.get(1).name);
    }

    static class Sample {
        public String name;
        public int age;

        public Sample() {
        }

        Sample(String name, int age) {
            this.name = name;
            this.age = age;
        }
    }

    private static class TestJsonUnit extends JsonUnit {
        protected TestJsonUnit(JsonMapper jsonMapper) {
            super(jsonMapper);
        }
    }
}
