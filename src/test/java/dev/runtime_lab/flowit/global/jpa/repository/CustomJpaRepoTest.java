package dev.runtime_lab.flowit.global.jpa.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Root;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CustomJpaRepoTest {

	private final EntityManager entityManager = mock(EntityManager.class);
	private final SampleRepo repository = new SampleRepo(entityManager);

	@Test
	void savePersistsAndReturnsEntity() {
		SampleEntity entity = new SampleEntity();

		SampleEntity saved = repository.save(entity);

		assertSame(entity, saved);
		verify(entityManager).persist(entity);
	}

	@Test
	void updateMergesAndReturnsManagedEntity() {
		SampleEntity entity = new SampleEntity();
		SampleEntity merged = new SampleEntity();

		when(entityManager.merge(entity)).thenReturn(merged);

		SampleEntity updated = repository.update(entity);

		assertSame(merged, updated);
		verify(entityManager).merge(entity);
	}

	@Test
	void findByIdReturnsEntityWhenFound() {
		SampleEntity entity = new SampleEntity();

		when(entityManager.find(SampleEntity.class, 1L)).thenReturn(entity);

		Optional<SampleEntity> found = repository.findById(1L);

		assertTrue(found.isPresent());
		assertSame(entity, found.get());
	}

	@Test
	void findByIdReturnsEmptyWhenMissing() {
		when(entityManager.find(SampleEntity.class, 1L)).thenReturn(null);

		Optional<SampleEntity> found = repository.findById(1L);

		assertFalse(found.isPresent());
	}

	@Test
	@SuppressWarnings("unchecked")
	void findAllReturnsAllEntities() {
		CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);
		CriteriaQuery<SampleEntity> criteriaQuery = mock(CriteriaQuery.class);
		Root<SampleEntity> root = mock(Root.class);
		TypedQuery<SampleEntity> typedQuery = mock(TypedQuery.class);
		List<SampleEntity> entities = List.of(new SampleEntity(), new SampleEntity());

		when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
		when(criteriaBuilder.createQuery(SampleEntity.class)).thenReturn(criteriaQuery);
		when(criteriaQuery.from(SampleEntity.class)).thenReturn(root);
		when(criteriaQuery.select(root)).thenReturn(criteriaQuery);
		when(entityManager.createQuery(criteriaQuery)).thenReturn(typedQuery);
		when(typedQuery.getResultList()).thenReturn(entities);

		List<SampleEntity> found = repository.findAll();

		assertEquals(entities, found);
	}

	@Test
	@SuppressWarnings("unchecked")
	void countReturnsEntityCount() {
		CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);
		CriteriaQuery<Long> criteriaQuery = mock(CriteriaQuery.class);
		Root<SampleEntity> root = mock(Root.class);
		Expression<Long> countExpression = mock(Expression.class);
		TypedQuery<Long> typedQuery = mock(TypedQuery.class);

		when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
		when(criteriaBuilder.createQuery(Long.class)).thenReturn(criteriaQuery);
		when(criteriaQuery.from(SampleEntity.class)).thenReturn(root);
		when(criteriaBuilder.count(root)).thenReturn(countExpression);
		when(criteriaQuery.select(countExpression)).thenReturn(criteriaQuery);
		when(entityManager.createQuery(criteriaQuery)).thenReturn(typedQuery);
		when(typedQuery.getSingleResult()).thenReturn(2L);

		long count = repository.count();

		assertEquals(2L, count);
	}

	@Test
	void existsByIdReturnsTrueWhenEntityExists() {
		when(entityManager.find(SampleEntity.class, 1L)).thenReturn(new SampleEntity());

		assertTrue(repository.existsById(1L));
	}

	@Test
	void deleteRemovesManagedEntityDirectly() {
		SampleEntity entity = new SampleEntity();

		when(entityManager.contains(entity)).thenReturn(true);

		repository.delete(entity);

		verify(entityManager).remove(entity);
	}

	@Test
	void deleteMergesDetachedEntityBeforeRemove() {
		SampleEntity entity = new SampleEntity();
		SampleEntity merged = new SampleEntity();

		when(entityManager.contains(entity)).thenReturn(false);
		when(entityManager.merge(entity)).thenReturn(merged);

		repository.delete(entity);

		verify(entityManager).remove(merged);
	}

	@Test
	void deleteByIdRemovesEntityWhenFound() {
		SampleEntity entity = new SampleEntity();

		when(entityManager.find(SampleEntity.class, 1L)).thenReturn(entity);
		when(entityManager.contains(entity)).thenReturn(true);

		repository.deleteById(1L);

		verify(entityManager).remove(entity);
	}

	private static final class SampleRepo extends CustomJpaRepo<SampleEntity, Long> {

		private SampleRepo(EntityManager entityManager) {
			super(SampleEntity.class, entityManager);
		}
	}

	private static final class SampleEntity {
	}
}
