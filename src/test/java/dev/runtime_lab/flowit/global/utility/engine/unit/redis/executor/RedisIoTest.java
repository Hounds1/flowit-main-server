package dev.runtime_lab.flowit.global.utility.engine.unit.redis.executor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RedisIoTest {

    private StringRedisTemplate redisTemplate;
    private ValueOperations<String, String> valueOperations;
    private RedisIo redisIo;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        redisTemplate = mock(StringRedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        redisIo = new RedisIo(redisTemplate);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void readGetsStringValue() {
        when(valueOperations.get("key")).thenReturn("value");

        assertEquals("value", redisIo.read("key"));
        verify(valueOperations).get("key");
    }

    @Test
    void writeSetsStringValue() {
        redisIo.write("key", "value");

        verify(valueOperations).set("key", "value");
    }

    @Test
    void writeSetsStringValueWithDurationTtl() {
        Duration ttl = Duration.ofSeconds(30);

        redisIo.write("key", "value", ttl);

        verify(valueOperations).set("key", "value", ttl);
    }

    @Test
    void writeForSecondsCreatesDurationTtl() {
        redisIo.writeForSeconds("key", "value", 30);

        verify(valueOperations).set("key", "value", Duration.ofSeconds(30));
    }

    @Test
    void writeForMinutesCreatesDurationTtl() {
        redisIo.writeForMinutes("key", "value", 10);

        verify(valueOperations).set("key", "value", Duration.ofMinutes(10));
    }

    @Test
    void writeForHoursCreatesDurationTtl() {
        redisIo.writeForHours("key", "value", 2);

        verify(valueOperations).set("key", "value", Duration.ofHours(2));
    }

    @Test
    void existsReturnsBooleanResult() {
        when(redisTemplate.hasKey("key")).thenReturn(true);
        when(redisTemplate.hasKey("missing")).thenReturn(false);

        assertTrue(redisIo.exists("key"));
        assertFalse(redisIo.exists("missing"));
    }

    @Test
    void deleteSingleKeyReturnsBooleanResult() {
        when(redisTemplate.delete("key")).thenReturn(true);

        assertTrue(redisIo.delete("key"));
    }

    @Test
    void deleteMultipleKeysReturnsDeletedCount() {
        when(redisTemplate.delete(List.of("a", "b"))).thenReturn(2L);

        assertEquals(2L, redisIo.delete(List.of("a", "b")));
    }

    @Test
    void deleteEmptyCollectionReturnsZeroWithoutCallingRedis() {
        assertEquals(0L, redisIo.delete(List.of()));
    }

    @Test
    void rejectsBlankKey() {
        assertThrows(IllegalArgumentException.class, () -> redisIo.read(" "));
    }

    @Test
    void rejectsNonPositiveTtl() {
        assertThrows(IllegalArgumentException.class, () -> redisIo.writeForSeconds("key", "value", 0));
    }
}
