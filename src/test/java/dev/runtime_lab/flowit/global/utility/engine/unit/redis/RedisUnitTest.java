package dev.runtime_lab.flowit.global.utility.engine.unit.redis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RedisUnitTest {

    private StringRedisTemplate redisTemplate;
    private ValueOperations<String, String> valueOperations;
    private RedisUnit redisUnit;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        redisTemplate = mock(StringRedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        redisUnit = new TestRedisUnit(redisTemplate);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void readShortcutDelegatesToIo() {
        when(valueOperations.get("key")).thenReturn("value");

        assertEquals("value", redisUnit.read("key"));
    }

    @Test
    void writeForMinutesShortcutDelegatesToIo() {
        redisUnit.writeForMinutes("key", "value", 10);

        verify(valueOperations).set("key", "value", Duration.ofMinutes(10));
    }

    @Test
    void scanShortcutDelegatesToKeyManager() {
        FakeCursor cursor = new FakeCursor("a", "b");
        when(redisTemplate.scan(any(ScanOptions.class))).thenReturn(cursor);

        assertEquals(List.of("a", "b"), redisUnit.scan("*"));
        assertTrue(cursor.isClosed());
    }

    private static class TestRedisUnit extends RedisUnit {
        protected TestRedisUnit(StringRedisTemplate stringRedisTemplate) {
            super(stringRedisTemplate);
        }
    }

    private static final class FakeCursor implements Cursor<String> {
        private final Iterator<String> delegate;
        private boolean closed;
        private long position;

        private FakeCursor(String... values) {
            this.delegate = List.of(values).iterator();
        }

        @Override
        public CursorId getId() {
            return CursorId.of(0);
        }

        @Override
        public long getCursorId() {
            return 0L;
        }

        @Override
        public boolean isClosed() {
            return closed;
        }

        @Override
        public void close() {
            closed = true;
        }

        @Override
        public boolean hasNext() {
            return delegate.hasNext();
        }

        @Override
        public String next() {
            position++;
            return delegate.next();
        }

        @Override
        public long getPosition() {
            return position;
        }

        @Override
        public void forEachRemaining(Consumer<? super String> action) {
            delegate.forEachRemaining(value -> {
                position++;
                action.accept(value);
            });
        }
    }
}
