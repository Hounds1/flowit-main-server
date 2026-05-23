package dev.runtime_lab.flowit.global.utility.engine.unit.redis.executor;

import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class RedisIo {

    private final StringRedisTemplate stringRedisTemplate;

    protected RedisIo(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = Objects.requireNonNull(stringRedisTemplate);
    }

    public String read(String key) {
        return stringRedisTemplate.opsForValue().get(requireKey(key));
    }

    public void write(String key, String value) {
        stringRedisTemplate.opsForValue().set(requireKey(key), Objects.requireNonNull(value));
    }

    public void write(String key, String value, Duration ttl) {
        stringRedisTemplate.opsForValue().set(
            requireKey(key),
            Objects.requireNonNull(value),
            requirePositiveTtl(ttl)
        );
    }

    public void writeForSeconds(String key, String value, long seconds) {
        write(key, value, Duration.ofSeconds(requirePositiveAmount(seconds, "Seconds")));
    }

    public void writeForMinutes(String key, String value, long minutes) {
        write(key, value, Duration.ofMinutes(requirePositiveAmount(minutes, "Minutes")));
    }

    public void writeForHours(String key, String value, long hours) {
        write(key, value, Duration.ofHours(requirePositiveAmount(hours, "Hours")));
    }

    public boolean exists(String key) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(requireKey(key)));
    }

    public boolean delete(String key) {
        return Boolean.TRUE.equals(stringRedisTemplate.delete(requireKey(key)));
    }

    public long delete(Collection<String> keys) {
        List<String> normalizedKeys = Objects.requireNonNull(keys).stream()
            .map(RedisIo::requireKey)
            .toList();

        if (normalizedKeys.isEmpty()) {
            return 0L;
        }

        Long deletedCount = stringRedisTemplate.delete(normalizedKeys);
        return deletedCount == null ? 0L : deletedCount;
    }

    private static String requireKey(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Key cannot be blank.");
        }

        return key;
    }

    private static Duration requirePositiveTtl(Duration ttl) {
        Objects.requireNonNull(ttl);

        if (ttl.isZero() || ttl.isNegative()) {
            throw new IllegalArgumentException("TTL must be positive.");
        }

        return ttl;
    }

    private static long requirePositiveAmount(long amount, String label) {
        if (amount <= 0) {
            throw new IllegalArgumentException(label + " must be positive.");
        }

        return amount;
    }
}
