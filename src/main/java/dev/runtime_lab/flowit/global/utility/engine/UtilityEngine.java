package dev.runtime_lab.flowit.global.utility.engine;

import dev.runtime_lab.flowit.global.utility.engine.unit.json.JsonUnit;
import dev.runtime_lab.flowit.global.utility.engine.unit.redis.RedisUnit;
import dev.runtime_lab.flowit.global.utility.engine.unit.response.ResponseUnit;
import org.springframework.data.redis.core.StringRedisTemplate;
import tools.jackson.databind.json.JsonMapper;

import java.util.Objects;

public final class UtilityEngine {

    private static final ResponseUnit singletonResponseUnit = new ResponseUnitDelegator();
    private static volatile JsonUnit singletonJsonUnit;
    private static volatile RedisUnit singletonRedisUnit;

    private UtilityEngine() {
    }

    static void initialize(JsonMapper jsonMapper, StringRedisTemplate stringRedisTemplate) {
        singletonJsonUnit = new JsonUnitDelegator(Objects.requireNonNull(jsonMapper));
        singletonRedisUnit = new RedisUnitDelegator(Objects.requireNonNull(stringRedisTemplate));
    }

    public static JsonUnit json() {
        if (singletonJsonUnit == null) {
            throw new IllegalStateException("UtilityEngine has not been initialized.");
        }

        return singletonJsonUnit;
    }

    public static RedisUnit redis() {
        if (singletonRedisUnit == null) {
            throw new IllegalStateException("UtilityEngine has not been initialized.");
        }

        return singletonRedisUnit;
    }

    public static ResponseUnit response() {
        return singletonResponseUnit;
    }

    private static class JsonUnitDelegator extends JsonUnit {
        protected JsonUnitDelegator(JsonMapper jsonMapper) {
            super(jsonMapper);
        }
    }

    private static class RedisUnitDelegator extends RedisUnit {
        protected RedisUnitDelegator(StringRedisTemplate stringRedisTemplate) {
            super(stringRedisTemplate);
        }
    }

    private static class ResponseUnitDelegator extends ResponseUnit {
        protected ResponseUnitDelegator() {
        }
    }
}
