package dev.runtime_lab.flowit.domain.notification.service.internal;

import dev.runtime_lab.flowit.domain.notification.queue.NotificationRecipientDeliveryRetryQueue;
import dev.runtime_lab.flowit.global.stereotype.InternalService;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

@InternalService
@RequiredArgsConstructor
@Slf4j
public class RedisNotificationRecipientDeliveryRetryQueue implements NotificationRecipientDeliveryRetryQueue {

	private static final String RETRY_QUEUE_KEY = "flowit:notification:recipient-delivery:retry";
	private static final Duration RETRY_DELAY = Duration.ofSeconds(2);
	private static final Duration RETRY_LEASE = Duration.ofSeconds(30);
	private static final DefaultRedisScript<Long> SCHEDULE_SCRIPT = new DefaultRedisScript<>(
		"local current = redis.call('zscore', KEYS[1], ARGV[1]) " +
			"if (not current) or tonumber(current) > tonumber(ARGV[2]) then " +
			"return redis.call('zadd', KEYS[1], ARGV[2], ARGV[1]) " +
			"end " +
			"return 0",
		Long.class
	);
	@SuppressWarnings("rawtypes")
	private static final DefaultRedisScript<List> POLL_SCRIPT = new DefaultRedisScript<>(
		"local values = redis.call('zrangebyscore', KEYS[1], '-inf', ARGV[1], 'LIMIT', 0, ARGV[2]) " +
			"for index, value in ipairs(values) do redis.call('zadd', KEYS[1], ARGV[3], value) end " +
			"return values",
		List.class
	);
	private static final DefaultRedisScript<Long> COMPLETE_SCRIPT = new DefaultRedisScript<>(
		"return redis.call('zrem', KEYS[1], ARGV[1])",
		Long.class
	);

	private final StringRedisTemplate stringRedisTemplate;
	private final Clock clock;

	@Override
	public void schedule(Long userId) {
		Objects.requireNonNull(userId, "userId must not be null");

		long dueAt = Instant.now(clock).plus(RETRY_DELAY).getEpochSecond();
		stringRedisTemplate.execute(
			SCHEDULE_SCRIPT,
			List.of(RETRY_QUEUE_KEY),
			String.valueOf(userId),
			String.valueOf(dueAt)
		);
	}

	@Override
	public List<Long> pollDue(int size) {
		if (size <= 0) {
			return List.of();
		}

		Instant now = Instant.now(clock);
		long nowEpochSecond = now.getEpochSecond();
		long leasedUntil = now.plus(RETRY_LEASE).getEpochSecond();
		@SuppressWarnings("unchecked")
		List<String> rawUserIds = (List<String>) stringRedisTemplate.execute(
			POLL_SCRIPT,
			List.of(RETRY_QUEUE_KEY),
			String.valueOf(nowEpochSecond),
			String.valueOf(size),
			String.valueOf(leasedUntil)
		);
		if (rawUserIds == null || rawUserIds.isEmpty()) {
			return List.of();
		}

		return rawUserIds.stream()
			.map(this::parseUserId)
			.filter(Objects::nonNull)
			.toList();
	}

	@Override
	public void complete(Long userId) {
		Objects.requireNonNull(userId, "userId must not be null");

		stringRedisTemplate.execute(
			COMPLETE_SCRIPT,
			List.of(RETRY_QUEUE_KEY),
			String.valueOf(userId)
		);
	}

	private Long parseUserId(String value) {
		try {
			return Long.valueOf(value);
		}
		catch (NumberFormatException exception) {
			log.warn("Invalid notification recipient delivery retry user id. value={}", value, exception);
			return null;
		}
	}
}
