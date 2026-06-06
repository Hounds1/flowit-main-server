package dev.runtime_lab.flowit.domain.workspace.statemachine;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class WorkspaceJoinStateMachineIds {

	private static final String JOIN_REQUEST_ID_FORMAT = "workspace-join-request-%d";

	static String joinRequest(Long joinRequestId) {
		return JOIN_REQUEST_ID_FORMAT.formatted(joinRequestId);
	}
}
