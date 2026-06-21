package dev.runtime_lab.flowit.global.socket.config;

import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableWebSocketSecurity
public class WebSocketSecurityConfig {

	private static final String CSRF_CHANNEL_INTERCEPTOR_BEAN_NAME = "csrfChannelInterceptor";

	@Bean
	public AuthorizationManager<Message<?>> messageAuthorizationManager(
		MessageMatcherDelegatingAuthorizationManager.Builder messages
	) {
		return messages
			.nullDestMatcher().authenticated()
			.simpDestMatchers("/app/**").authenticated()
			.simpSubscribeDestMatchers("/user/queue/**").authenticated()
			.simpTypeMatchers(SimpMessageType.MESSAGE, SimpMessageType.SUBSCRIBE).denyAll()
			.anyMessage().denyAll()
			.build();
	}

	@Bean(CSRF_CHANNEL_INTERCEPTOR_BEAN_NAME)
	ChannelInterceptor csrfChannelInterceptor() {
		return new ChannelInterceptor() {
		};
	}
}
