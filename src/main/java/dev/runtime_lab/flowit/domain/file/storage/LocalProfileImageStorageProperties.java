package dev.runtime_lab.flowit.domain.file.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;
import org.springframework.util.unit.DataSize;

@ConfigurationProperties(prefix = "flowit.storage.local.profile-images")
public record LocalProfileImageStorageProperties(
	String directory,
	DataSize maxSize,
	boolean cleanupOnStartup
) {

	private static final DataSize DEFAULT_MAX_SIZE = DataSize.ofMegabytes(5);

	public LocalProfileImageStorageProperties {
		directory = StringUtils.hasText(directory) ? directory.trim() : null;
		maxSize = maxSize == null ? DEFAULT_MAX_SIZE : maxSize;
		if (maxSize.isNegative() || maxSize.toBytes() == 0) {
			throw new IllegalArgumentException("flowit.storage.local.profile-images.max-size must be positive");
		}
	}
}
