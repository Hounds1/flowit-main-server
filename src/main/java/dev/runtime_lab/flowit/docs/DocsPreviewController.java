package dev.runtime_lab.flowit.docs;

import java.net.URI;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/docs-preview")
public class DocsPreviewController {

	@PostMapping("/projects/{projectId}/tasks")
	public ResponseEntity<DocsPreviewResponse> createTask(
			@PathVariable Long projectId,
			@RequestParam(defaultValue = "false") boolean notify,
			@Valid @RequestBody DocsPreviewRequest request
	) {
		DocsPreviewResponse response = new DocsPreviewResponse(
			1001L,
			projectId,
			request.title(),
			"READY",
			request.priority(),
			notify,
			request.assigneeEmail(),
			1779613200L
		);

		return ResponseEntity
			.created(URI.create("/api/docs-preview/projects/%d/tasks/%d".formatted(projectId, response.id())))
			.body(response);
	}

	public record DocsPreviewRequest(
		@NotBlank
		@Size(max = 80)
		String title,

		@Size(max = 500)
		String description,

		@NotBlank
		String priority,

		@Email
		String assigneeEmail
	) {
	}

	public record DocsPreviewResponse(
		Long id,
		Long projectId,
		String title,
		String status,
		String priority,
		boolean notificationEnabled,
		String assigneeEmail,
		long createdAt
	) {
	}
}
