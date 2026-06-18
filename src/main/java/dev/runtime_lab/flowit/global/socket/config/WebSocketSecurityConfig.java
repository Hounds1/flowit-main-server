package dev.runtime_lab.flowit.global.socket.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;

@Configuration
@EnableWebSocketSecurity
public class WebSocketSecurityConfig {

	@Bean
	public AuthorizationManager<Message<?>> messageAuthorizationManager(
		MessageMatcherDelegatingAuthorizationManager.Builder messages
	) {
		return messages
			.nullDestMatcher().authenticated()
			.simpDestMatchers("/app/**").authenticated()
			.simpSubscribeDestMatchers("/topic/**", "/user/queue/**").authenticated()
			.simpTypeMatchers(SimpMessageType.MESSAGE, SimpMessageType.SUBSCRIBE).denyAll()
			.anyMessage().denyAll()
			.build();
	}
}
