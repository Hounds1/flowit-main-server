package dev.runtime_lab.flowit.domain.workspace.exception;

public class WorkspaceAccessDeniedException extends RuntimeException {

	public WorkspaceAccessDeniedException() {
		super("Workspace owner permission is required.");
	}
}
