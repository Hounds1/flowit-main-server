package dev.runtime_lab.flowit.global.jpa.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class CustomJpaRepo<T, ID> {

	private final Class<T> entityClass;
	private final EntityManager entityManager;

	@Transactional
	public T save(T entity) {
		entityManager.persist(entity);
		return entity;
	}

	@Transactional
	public T update(T entity) {
		return entityManager.merge(entity);
	}

	public Optional<T> findById(ID id) {
		return Optional.ofNullable(entityManager.find(entityClass, id));
	}

	public List<T> findAll() {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<T> query = criteriaBuilder.createQuery(entityClass);
		Root<T> root = query.from(entityClass);

		query.select(root);

		return entityManager.createQuery(query).getResultList();
	}

	public boolean existsById(ID id) {
		return findById(id).isPresent();
	}

	public long count() {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> query = criteriaBuilder.createQuery(Long.class);
		Root<T> root = query.from(entityClass);

		query.select(criteriaBuilder.count(root));

		return entityManager.createQuery(query).getSingleResult();
	}

	@Transactional
	public void delete(T entity) {
		if (entityManager.contains(entity)) {
			entityManager.remove(entity);
			return;
		}

		entityManager.remove(entityManager.merge(entity));
	}

	@Transactional
	public void deleteById(ID id) {
		findById(id).ifPresent(this::delete);
	}

	protected EntityManager entityManager() {
		return entityManager;
	}

	protected Class<T> entityClass() {
		return entityClass;
	}
}
