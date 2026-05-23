package dev.runtime_lab.flowit.global.utility.engine;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
@RequiredArgsConstructor
class UtilityEngineInitializer {

    private final JsonMapper jsonMapper;
    private final StringRedisTemplate stringRedisTemplate;

    @PostConstruct
    void initialize() {
        UtilityEngine.initialize(jsonMapper, stringRedisTemplate);
    }
}
