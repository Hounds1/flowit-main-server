package dev.runtime_lab.flowit.domain.user.exception;

public class DuplicateActiveEmailException extends RuntimeException {

	public DuplicateActiveEmailException(String email) {
		super("Active user email already exists: " + email);
	}
}
