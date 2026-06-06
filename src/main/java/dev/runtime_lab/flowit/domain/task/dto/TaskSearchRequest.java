package dev.runtime_lab.flowit.domain.task.dto;

import dev.runtime_lab.flowit.domain.task.entity.TaskStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

import static dev.runtime_lab.flowit.domain.task.validation.TaskConstraints.MAX_SEARCH_PAGE_SIZE;
import static dev.runtime_lab.flowit.domain.task.validation.TaskConstraints.MIN_PAGE;
import static dev.runtime_lab.flowit.domain.task.validation.TaskConstraints.MIN_PAGE_SIZE;

public record TaskSearchRequest(
	TaskStatus status,
	Long assigneeMemberId,
	String tag,
	String keyword,

	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	LocalDate dueFrom,

	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	LocalDate dueTo,

	@Min(MIN_PAGE)
	Integer page,

	@Min(MIN_PAGE_SIZE)
	@Max(MAX_SEARCH_PAGE_SIZE)
	Integer size
) {

	public TaskListQuery toQuery() {
		return new TaskListQuery(status, assigneeMemberId, tag, keyword, dueFrom, dueTo, page, size);
	}
}
