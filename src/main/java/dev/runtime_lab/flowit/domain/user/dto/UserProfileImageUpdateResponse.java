package dev.runtime_lab.flowit.domain.user.dto;

import dev.runtime_lab.flowit.domain.file.entity.FileMetadata;

public record UserProfileImageUpdateResponse(
	Long fileId,
	String contentType,
	Long sizeBytes,
	Integer width,
	Integer height
) {

	public static UserProfileImageUpdateResponse from(FileMetadata fileMetadata) {
		return new UserProfileImageUpdateResponse(
			fileMetadata.getId(),
			fileMetadata.getContentType(),
			fileMetadata.getSizeBytes(),
			fileMetadata.getWidth(),
			fileMetadata.getHeight()
		);
	}
}
