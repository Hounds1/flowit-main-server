package dev.runtime_lab.flowit.domain.task.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
	name = "task_tags",
	indexes = {
		@Index(name = "idx_task_tags_normalized_name", columnList = "normalized_name")
	},
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_task_tags_task_normalized_name", columnNames = {"task_id", "normalized_name"})
	}
)
public class TaskTag {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(
		name = "task_id",
		nullable = false,
		foreignKey = @ForeignKey(name = "fk_task_tags_task")
	)
	private Task task;

	@Column(name = "name", nullable = false, length = 30)
	private String name;

	@Column(name = "normalized_name", nullable = false, length = 30)
	private String normalizedName;

	@Column(name = "sort_order", nullable = false)
	private Integer sortOrder;

	@Column(name = "created_at", nullable = false)
	private Long createdAt;
}
