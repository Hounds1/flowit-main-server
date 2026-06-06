package dev.runtime_lab.flowit.domain.task.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import static dev.runtime_lab.flowit.domain.task.validation.TaskConstraints.PROGRESS_MAX;
import static dev.runtime_lab.flowit.domain.task.validation.TaskConstraints.PROGRESS_MIN;

public record TaskProgressUpdateRequest(
	@NotNull
	@Min(PROGRESS_MIN)
	@Max(PROGRESS_MAX)
	Integer progress
) {
}
