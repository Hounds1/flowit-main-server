package dev.runtime_lab.flowit.domain.notification.service.internal;

import dev.runtime_lab.flowit.domain.notification.lock.NotificationRecipientDeliveryLock;
import dev.runtime_lab.flowit.global.stereotype.InternalService;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

@InternalService
@RequiredArgsConstructor
public class RedisNotificationRecipientDeliveryLock implements NotificationRecipientDeliveryLock {

	private static final String KEY_PREFIX = "flowit:notification:recipient-delivery:";
	private static final Duration LOCK_TTL = Duration.ofSeconds(30);
	private static final DefaultRedisScript<Long> RELEASE_SCRIPT = new DefaultRedisScript<>(
		"if redis.call('get', KEYS[1]) == ARGV[1] then " +
			"return redis.call('del', KEYS[1]) " +
			"else return 0 end",
		Long.class
	);

	private final StringRedisTemplate stringRedisTemplate;

	@Override
	public boolean executeWithLock(Long userId, Runnable action) {
		Objects.requireNonNull(userId, "userId must not be null");
		Objects.requireNonNull(action, "action must not be null");

		String key = key(userId);
		String token = UUID.randomUUID().toString();
		Boolean acquired = stringRedisTemplate.opsForValue().setIfAbsent(key, token, LOCK_TTL);
		if (!Boolean.TRUE.equals(acquired)) {
			return false;
		}

		try {
			action.run();
			return true;
		}
		finally {
			release(key, token);
		}
	}

	private void release(String key, String token) {
		stringRedisTemplate.execute(RELEASE_SCRIPT, List.of(key), token);
	}

	private String key(Long userId) {
		return KEY_PREFIX + userId;
	}
}
