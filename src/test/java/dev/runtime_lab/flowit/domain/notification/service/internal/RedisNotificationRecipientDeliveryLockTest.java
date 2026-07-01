package dev.runtime_lab.flowit.domain.notification.service.internal;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings({"rawtypes", "unchecked"})
class RedisNotificationRecipientDeliveryLockTest {

	private static final String LOCK_KEY = "flowit:notification:recipient-delivery:34";
	private static final Duration LOCK_TTL = Duration.ofSeconds(30);

	private final StringRedisTemplate stringRedisTemplate = mock(StringRedisTemplate.class);
	private final ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
	private final RedisNotificationRecipientDeliveryLock lock =
		new RedisNotificationRecipientDeliveryLock(stringRedisTemplate);

	@Test
	void runsActionAndReleasesLockWhenAcquired() {
		AtomicBoolean executed = new AtomicBoolean(false);

		when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
		when(valueOperations.setIfAbsent(eq(LOCK_KEY), anyString(), eq(LOCK_TTL))).thenReturn(true);

		boolean locked = lock.executeWithLock(34L, () -> executed.set(true));

		assertTrue(locked);
		assertTrue(executed.get());
		verify(stringRedisTemplate).execute(any(DefaultRedisScript.class), eq(List.of(LOCK_KEY)), anyString());
	}

	@Test
	void doesNotRunActionWhenLockIsNotAcquired() {
		AtomicBoolean executed = new AtomicBoolean(false);

		when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
		when(valueOperations.setIfAbsent(eq(LOCK_KEY), anyString(), eq(LOCK_TTL))).thenReturn(false);

		boolean locked = lock.executeWithLock(34L, () -> executed.set(true));

		assertFalse(locked);
		assertFalse(executed.get());
		verify(stringRedisTemplate, never()).execute(any(DefaultRedisScript.class), anyList(), anyString());
	}

	@Test
	void releasesLockWhenActionThrows() {
		when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
		when(valueOperations.setIfAbsent(eq(LOCK_KEY), anyString(), eq(LOCK_TTL))).thenReturn(true);

		assertThrows(
			IllegalStateException.class,
			() -> lock.executeWithLock(34L, () -> {
				throw new IllegalStateException("failed");
			})
		);

		verify(stringRedisTemplate).execute(any(DefaultRedisScript.class), eq(List.of(LOCK_KEY)), anyString());
	}
}
