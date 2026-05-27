package dev.runtime_lab.flowit.domain.file.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(
	name = "files",
	indexes = {
		@Index(name = "idx_files_deleted_at", columnList = "deleted_at")
	}
)
public class FileMetadata {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "storage_key", nullable = false, length = 500, unique = true)
	private String storageKey;

	@Column(name = "original_filename")
	private String originalFilename;

	@Column(name = "content_type", nullable = false, length = 100)
	private String contentType;

	@Column(name = "size_bytes", nullable = false)
	private Long sizeBytes;

	@Column(name = "width")
	private Integer width;

	@Column(name = "height")
	private Integer height;

	@Column(name = "created_at", nullable = false)
	private Long createdAt;

	@Column(name = "updated_at", nullable = false)
	private Long updatedAt;

	@Column(name = "deleted_at")
	private Long deletedAt;
}
