package dev.runtime_lab.flowit.domain.file.storage;

public record StoredProfileImageFile(
	String storageKey,
	String originalFilename,
	String contentType,
	long sizeBytes,
	int width,
	int height
) {
}
