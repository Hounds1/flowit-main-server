package dev.runtime_lab.flowit.domain.file.storage;

import dev.runtime_lab.flowit.domain.file.repository.FileMetadataRepository;
import java.util.HashSet;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LocalProfileImageCleanupRunner implements ApplicationRunner {

	private final FileMetadataRepository fileMetadataRepository;
	private final LocalProfileImageStorage localProfileImageStorage;
	private final LocalProfileImageStorageProperties properties;

	@Override
	public void run(ApplicationArguments args) {
		if (!properties.cleanupOnStartup()) {
			return;
		}

		localProfileImageStorage.deleteOrphanFiles(
			new HashSet<>(fileMetadataRepository.findUserProfileImageStorageKeys())
		);
	}
}
