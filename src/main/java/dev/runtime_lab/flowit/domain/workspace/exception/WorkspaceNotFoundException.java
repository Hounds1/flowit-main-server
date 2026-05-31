package dev.runtime_lab.flowit.domain.workspace.exception;

public class WorkspaceNotFoundException extends RuntimeException {

	public WorkspaceNotFoundException() {
		super("Workspace not found.");
	}
}
