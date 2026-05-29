package dev.runtime_lab.flowit.global.security.authentication;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthenticatedUserArgumentResolverTest {

	private final AuthenticatedUserArgumentResolver resolver = new AuthenticatedUserArgumentResolver();

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void supportsAuthenticatedCurrentUserParameter() throws Exception {
		assertTrue(resolver.supportsParameter(parameter("authenticatedUser")));
	}

	@Test
	void resolvesCurrentUserFromJwtAuthentication() throws Exception {
		Jwt jwt = jwt("1001", "user@example.com", "nickname");
		SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt, List.of()));

		CurrentUser currentUser = (CurrentUser) resolver.resolveArgument(
			parameter("authenticatedUser"),
			null,
			null,
			null
		);

		assertEquals(1001L, currentUser.id());
		assertEquals("user@example.com", currentUser.email());
		assertEquals("nickname", currentUser.name());
	}

	@Test
	void rejectsMissingAuthentication() throws Exception {
		assertThrows(
			InvalidAuthenticatedUserException.class,
			() -> resolver.resolveArgument(parameter("authenticatedUser"), null, null, null)
		);
	}

	@Test
	void rejectsInvalidSubject() throws Exception {
		Jwt jwt = jwt("not-number", "user@example.com", "nickname");
		SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt, List.of()));

		assertThrows(
			InvalidAuthenticatedUserException.class,
			() -> resolver.resolveArgument(parameter("authenticatedUser"), null, null, null)
		);
	}

	@SuppressWarnings("unused")
	private void authenticatedUser(@AuthenticatedUser CurrentUser currentUser) {
	}

	private MethodParameter parameter(String methodName) throws Exception {
		Method method = AuthenticatedUserArgumentResolverTest.class.getDeclaredMethod(methodName, CurrentUser.class);
		return new MethodParameter(method, 0);
	}

	private Jwt jwt(String subject, String email, String name) {
		return Jwt.withTokenValue("token")
			.header("alg", "none")
			.subject(subject)
			.claim("email", email)
			.claim("name", name)
			.issuedAt(Instant.now())
			.expiresAt(Instant.now().plusSeconds(60))
			.build();
	}
}
