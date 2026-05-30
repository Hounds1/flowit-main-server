package dev.runtime_lab.flowit.domain.file.exception;

public class ProfileImageStorageException extends RuntimeException {

	public ProfileImageStorageException(String message) {
		super(message);
	}

	public ProfileImageStorageException(String message, Throwable cause) {
		super(message, cause);
	}
}
