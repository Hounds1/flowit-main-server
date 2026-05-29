package dev.runtime_lab.flowit.global.security.authentication;

public class InvalidAuthenticatedUserException extends RuntimeException {

	public InvalidAuthenticatedUserException() {
		super("Invalid authenticated user.");
	}
}
