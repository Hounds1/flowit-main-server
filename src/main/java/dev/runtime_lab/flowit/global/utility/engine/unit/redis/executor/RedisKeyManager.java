package dev.runtime_lab.flowit.global.utility.engine.unit.redis.executor;

import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class RedisKeyManager {

    private static final long DEFAULT_SCAN_COUNT = 1_000L;

    private final StringRedisTemplate stringRedisTemplate;

    protected RedisKeyManager(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = Objects.requireNonNull(stringRedisTemplate);
    }

    public Cursor<String> cursor(String pattern) {
        return cursor(pattern, DEFAULT_SCAN_COUNT);
    }

    public Cursor<String> cursor(String pattern, long count) {
        return stringRedisTemplate.scan(scanOptions(pattern, count));
    }

    public List<String> scan(String pattern) {
        return scan(pattern, DEFAULT_SCAN_COUNT);
    }

    public List<String> scan(String pattern, long count) {
        List<String> keys = new ArrayList<>();
        scan(pattern, count, keys::add);
        return keys;
    }

    public void scan(String pattern, long count, Consumer<String> consumer) {
        Objects.requireNonNull(consumer);

        try (Cursor<String> cursor = cursor(pattern, count)) {
            cursor.forEachRemaining(consumer);
        }
    }

    private static ScanOptions scanOptions(String pattern, long count) {
        return ScanOptions.scanOptions()
            .match(requirePattern(pattern))
            .count(requirePositiveCount(count))
            .build();
    }

    private static String requirePattern(String pattern) {
        if (pattern == null || pattern.isBlank()) {
            throw new IllegalArgumentException("Pattern cannot be blank.");
        }

        return pattern;
    }

    private static long requirePositiveCount(long count) {
        if (count <= 0) {
            throw new IllegalArgumentException("Scan count must be positive.");
        }

        return count;
    }
}
