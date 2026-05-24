package dev.runtime_lab.flowit.global.utility.engine;

import dev.runtime_lab.flowit.global.utility.engine.unit.response.annotation.ResponseTransform;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UtilityEngineTest {

    private ValueOperations<String, String> valueOperations;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        valueOperations = mock(ValueOperations.class);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        UtilityEngine.initialize(JsonMapper.builder().build(), redisTemplate);
    }

    @Test
    void jsonShortcutUsesInitializedJsonMapper() throws Exception {
        Sample sample = UtilityEngine.json().read("{\"name\":\"홍길동\",\"age\":7}", Sample.class);

        assertEquals("홍길동", sample.name);
        assertEquals(7, sample.age);
    }

    @Test
    void redisShortcutUsesInitializedRedisTemplate() {
        when(valueOperations.get("key")).thenReturn("value");

        assertEquals("value", UtilityEngine.redis().read("key"));
    }

    @Test
    void responseShortcutTransformsObjectWithoutInitializerDependency() {
        ResponseSample response = UtilityEngine.response()
            .transform(new OriginSample("홍길동"), ResponseSample.class);

        assertEquals("홍길동", response.name);
    }

    static class Sample {
        public String name;
        public int age;

        public Sample() {
        }
    }

    static class OriginSample {
        private final String name;

        private OriginSample(String name) {
            this.name = name;
        }
    }

    @ResponseTransform
    static class ResponseSample {
        private String name;

        private ResponseSample() {
        }
    }
}
