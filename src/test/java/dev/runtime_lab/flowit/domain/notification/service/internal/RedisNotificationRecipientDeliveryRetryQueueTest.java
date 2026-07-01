package dev.runtime_lab.flowit.domain.notification.service.internal;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@SuppressWarnings({"rawtypes", "unchecked"})
class RedisNotificationRecipientDeliveryRetryQueueTest {

	private static final String RETRY_QUEUE_KEY = "flowit:notification:recipient-delivery:retry";

	private final StringRedisTemplate stringRedisTemplate = mock(StringRedisTemplate.class);
	private final Clock clock = Clock.fixed(Instant.ofEpochSecond(1782013300L), ZoneOffset.UTC);
	private final RedisNotificationRecipientDeliveryRetryQueue retryQueue =
		new RedisNotificationRecipientDeliveryRetryQueue(stringRedisTemplate, clock);

	@Test
	void schedulesUserWithShortDelay() {
		retryQueue.schedule(34L);

		verify(stringRedisTemplate).execute(
			any(DefaultRedisScript.class),
			eq(List.of(RETRY_QUEUE_KEY)),
			eq("34"),
			eq("1782013302")
		);
	}

	@Test
	void pollsDueUsers() {
		when(stringRedisTemplate.execute(
			any(DefaultRedisScript.class),
			eq(List.of(RETRY_QUEUE_KEY)),
			eq("1782013300"),
			eq("100"),
			eq("1782013330")
		)).thenReturn(List.of("34", "35"));

		List<Long> userIds = retryQueue.pollDue(100);

		assertEquals(List.of(34L, 35L), userIds);
	}

	@Test
	void doesNotPollWhenSizeIsNotPositive() {
		List<Long> userIds = retryQueue.pollDue(0);

		assertEquals(List.of(), userIds);
		verifyNoInteractions(stringRedisTemplate);
	}

	@Test
	void completesUser() {
		retryQueue.complete(34L);

		verify(stringRedisTemplate).execute(
			any(DefaultRedisScript.class),
			eq(List.of(RETRY_QUEUE_KEY)),
			eq("34")
		);
	}
}
