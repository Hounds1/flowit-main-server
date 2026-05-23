package dev.runtime_lab.flowit.global.utility.engine.unit.redis.executor;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RedisKeyManagerTest {

    @Test
    void scanReturnsKeysAndClosesCursor() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        FakeCursor cursor = new FakeCursor("user:1", "user:2");
        RedisKeyManager keyManager = new RedisKeyManager(redisTemplate);

        when(redisTemplate.scan(any(ScanOptions.class))).thenReturn(cursor);

        List<String> keys = keyManager.scan("user:*", 25);

        assertEquals(List.of("user:1", "user:2"), keys);
        assertTrue(cursor.isClosed());

        ArgumentCaptor<ScanOptions> optionsCaptor = ArgumentCaptor.forClass(ScanOptions.class);
        verify(redisTemplate).scan(optionsCaptor.capture());
        assertEquals("user:*", optionsCaptor.getValue().getPattern());
        assertEquals(25L, optionsCaptor.getValue().getCount());
    }

    @Test
    void scanConsumesKeysWithConsumer() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        FakeCursor cursor = new FakeCursor("session:1", "session:2");
        RedisKeyManager keyManager = new RedisKeyManager(redisTemplate);
        StringBuilder visited = new StringBuilder();

        when(redisTemplate.scan(any(ScanOptions.class))).thenReturn(cursor);

        keyManager.scan("session:*", 10, key -> visited.append(key).append("|"));

        assertEquals("session:1|session:2|", visited.toString());
        assertTrue(cursor.isClosed());
    }

    @Test
    void cursorReturnsOpenCursorToCaller() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        FakeCursor cursor = new FakeCursor("key");
        RedisKeyManager keyManager = new RedisKeyManager(redisTemplate);

        when(redisTemplate.scan(any(ScanOptions.class))).thenReturn(cursor);

        Cursor<String> openedCursor = keyManager.cursor("*", 10);

        assertFalse(openedCursor.isClosed());
        openedCursor.close();
        assertTrue(openedCursor.isClosed());
    }

    @Test
    void rejectsBlankPattern() {
        RedisKeyManager keyManager = new RedisKeyManager(mock(StringRedisTemplate.class));

        assertThrows(IllegalArgumentException.class, () -> keyManager.scan(" ", 10));
    }

    @Test
    void rejectsNonPositiveCount() {
        RedisKeyManager keyManager = new RedisKeyManager(mock(StringRedisTemplate.class));

        assertThrows(IllegalArgumentException.class, () -> keyManager.scan("*", 0));
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
