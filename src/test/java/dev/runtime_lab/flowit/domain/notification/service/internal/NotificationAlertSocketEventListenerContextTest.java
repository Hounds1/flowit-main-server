package dev.runtime_lab.flowit.domain.notification.service.internal;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import static org.mockito.Mockito.mock;

@SpringJUnitConfig(NotificationAlertSocketEventListenerContextTest.TestConfig.class)
class NotificationAlertSocketEventListenerContextTest {

	@Test
	void registersTransactionalEventListenerWithoutOpeningListenerTransaction() {
	}

	@Configuration
	@EnableTransactionManagement
	static class TestConfig {

		@Bean
		NotificationAlertSocketEventListener notificationAlertSocketEventListener(
			NotificationRecipientSocketDeliveryService notificationRecipientSocketDeliveryService
		) {
			return new NotificationAlertSocketEventListener(
				notificationRecipientSocketDeliveryService
			);
		}

		@Bean
		NotificationRecipientSocketDeliveryService notificationRecipientSocketDeliveryService() {
			return mock(NotificationRecipientSocketDeliveryService.class);
		}

		@Bean
		PlatformTransactionManager transactionManager() {
			return mock(PlatformTransactionManager.class);
		}
	}
}
