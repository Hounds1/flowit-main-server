package dev.runtime_lab.flowit.domain.file.repository;

import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import dev.runtime_lab.flowit.domain.file.entity.FileMetadata;
import dev.runtime_lab.flowit.domain.file.entity.QFileMetadata;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FileMetadataRepositoryTest {

	private final EntityManager entityManager = mock(EntityManager.class);
	private final JPAQueryFactory queryFactory = mock(JPAQueryFactory.class);
	private final FileMetadataRepository repository = new FileMetadataRepository(entityManager, queryFactory);

	@Test
	@SuppressWarnings("unchecked")
	void findActiveByStorageKeyReturnsFileMetadataWhenFound() {
		JPAQuery<FileMetadata> query = mock(JPAQuery.class);
		FileMetadata fileMetadata = FileMetadata.builder()
			.storageKey("profiles/user-1.png")
			.contentType("image/png")
			.sizeBytes(1024L)
			.createdAt(1L)
			.updatedAt(1L)
			.build();

		when(queryFactory.selectFrom(QFileMetadata.fileMetadata)).thenReturn(query);
		when(query.where(any(Predicate.class), any(Predicate.class))).thenReturn(query);
		when(query.fetchOne()).thenReturn(fileMetadata);

		Optional<FileMetadata> found = repository.findActiveByStorageKey("profiles/user-1.png");

		assertTrue(found.isPresent());
		assertSame(fileMetadata, found.get());
		verify(queryFactory).selectFrom(QFileMetadata.fileMetadata);
		verify(query).where(any(Predicate.class), any(Predicate.class));
		verify(query).fetchOne();
	}

	@Test
	@SuppressWarnings("unchecked")
	void existsActiveByStorageKeyReturnsFalseWhenMissing() {
		JPAQuery<FileMetadata> query = mock(JPAQuery.class);

		when(queryFactory.selectFrom(QFileMetadata.fileMetadata)).thenReturn(query);
		when(query.where(any(Predicate.class), any(Predicate.class))).thenReturn(query);
		when(query.fetchOne()).thenReturn(null);

		boolean exists = repository.existsActiveByStorageKey("missing.png");

		assertFalse(exists);
	}
}
