package dev.runtime_lab.flowit.domain.workspace.exception;

public class WorkspaceMemberNotFoundException extends RuntimeException {

	public WorkspaceMemberNotFoundException() {
		super("Workspace member not found.");
	}
}
