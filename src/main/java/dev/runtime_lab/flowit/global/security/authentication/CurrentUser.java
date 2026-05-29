package dev.runtime_lab.flowit.global.security.authentication;

public record CurrentUser(
	Long id,
	String email,
	String name
) {
}
