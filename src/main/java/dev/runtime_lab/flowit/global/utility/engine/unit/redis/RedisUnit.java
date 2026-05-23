package dev.runtime_lab.flowit.global.utility.engine.unit.redis;

import dev.runtime_lab.flowit.global.utility.engine.unit.redis.executor.RedisIo;
import dev.runtime_lab.flowit.global.utility.engine.unit.redis.executor.RedisKeyManager;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class RedisUnit {

    private final RedisKeyManager keyManager;
    private final RedisIo io;

    protected RedisUnit(StringRedisTemplate stringRedisTemplate) {
        this.keyManager = new RedisKeyManagerDelegator(stringRedisTemplate);
        this.io = new RedisIoDelegator(stringRedisTemplate);
    }

    public RedisKeyManager keyManager() {
        return keyManager;
    }

    public RedisIo io() {
        return io;
    }

    public Cursor<String> cursor(String pattern) {
        return keyManager.cursor(pattern);
    }

    public Cursor<String> cursor(String pattern, long count) {
        return keyManager.cursor(pattern, count);
    }

    public List<String> scan(String pattern) {
        return keyManager.scan(pattern);
    }

    public List<String> scan(String pattern, long count) {
        return keyManager.scan(pattern, count);
    }

    public void scan(String pattern, long count, Consumer<String> consumer) {
        keyManager.scan(pattern, count, consumer);
    }

    public String read(String key) {
        return io.read(key);
    }

    public void write(String key, String value) {
        io.write(key, value);
    }

    public void write(String key, String value, Duration ttl) {
        io.write(key, value, ttl);
    }

    public void writeForSeconds(String key, String value, long seconds) {
        io.writeForSeconds(key, value, seconds);
    }

    public void writeForMinutes(String key, String value, long minutes) {
        io.writeForMinutes(key, value, minutes);
    }

    public void writeForHours(String key, String value, long hours) {
        io.writeForHours(key, value, hours);
    }

    public boolean exists(String key) {
        return io.exists(key);
    }

    public boolean delete(String key) {
        return io.delete(key);
    }

    public long delete(Collection<String> keys) {
        return io.delete(keys);
    }

    private static class RedisKeyManagerDelegator extends RedisKeyManager {
        protected RedisKeyManagerDelegator(StringRedisTemplate stringRedisTemplate) {
            super(stringRedisTemplate);
        }
    }

    private static class RedisIoDelegator extends RedisIo {
        protected RedisIoDelegator(StringRedisTemplate stringRedisTemplate) {
            super(stringRedisTemplate);
        }
    }
}
