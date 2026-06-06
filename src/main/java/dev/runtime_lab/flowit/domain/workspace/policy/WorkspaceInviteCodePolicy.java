package dev.runtime_lab.flowit.domain.workspace.policy;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class WorkspaceInviteCodePolicy {

	public static final int MAX_GENERATION_ATTEMPTS = 10;
}
