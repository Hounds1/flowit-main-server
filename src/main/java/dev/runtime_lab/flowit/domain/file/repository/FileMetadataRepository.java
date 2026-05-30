package dev.runtime_lab.flowit.domain.file.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import dev.runtime_lab.flowit.domain.file.entity.FileMetadata;
import dev.runtime_lab.flowit.domain.file.entity.QFileMetadata;
import dev.runtime_lab.flowit.domain.user.entity.QUser;
import dev.runtime_lab.flowit.global.jpa.repository.CustomJpaRepo;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class FileMetadataRepository extends CustomJpaRepo<FileMetadata, Long> {

	private final JPAQueryFactory queryFactory;

	public FileMetadataRepository(EntityManager entityManager, JPAQueryFactory queryFactory) {
		super(FileMetadata.class, entityManager);
		this.queryFactory = queryFactory;
	}

	public Optional<FileMetadata> findActiveByStorageKey(String storageKey) {
		QFileMetadata fileMetadata = QFileMetadata.fileMetadata;

		return Optional.ofNullable(
			queryFactory.selectFrom(fileMetadata)
				.where(
					fileMetadata.storageKey.eq(storageKey),
					fileMetadata.deletedAt.isNull()
				)
				.fetchOne()
		);
	}

	public boolean existsActiveByStorageKey(String storageKey) {
		return findActiveByStorageKey(storageKey).isPresent();
	}

	public List<String> findUserProfileImageStorageKeys() {
		QUser user = QUser.user;
		QFileMetadata fileMetadata = QFileMetadata.fileMetadata;

		return queryFactory.select(fileMetadata.storageKey)
			.from(user)
			.join(user.profileImageFile, fileMetadata)
			.where(
				fileMetadata.deletedAt.isNull()
			)
			.fetch();
	}
}
