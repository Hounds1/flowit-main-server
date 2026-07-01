package dev.runtime_lab.flowit.domain.task.service.internal;

import dev.runtime_lab.flowit.domain.task.entity.Task;
import dev.runtime_lab.flowit.domain.task.repository.TaskRepository;
import dev.runtime_lab.flowit.global.stereotype.InternalService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@InternalService
@RequiredArgsConstructor
public class TaskNotificationTargetQueryService {

	private final TaskRepository taskRepository;

	@Transactional(readOnly = true)
	public Optional<Task> findActiveTask(Long workspaceId, Long taskId) {
		return taskRepository.findActiveByWorkspaceIdAndTaskId(workspaceId, taskId);
	}
}
